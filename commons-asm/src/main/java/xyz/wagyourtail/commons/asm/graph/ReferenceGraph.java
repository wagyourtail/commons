package xyz.wagyourtail.commons.asm.graph;

import lombok.Data;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ConstantDynamic;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import xyz.wagyourtail.commons.asm.ASMUtils;
import xyz.wagyourtail.commons.asm.type.FullyQualifiedMemberNameAndDesc;
import xyz.wagyourtail.commons.asm.type.MemberNameAndDesc;
import xyz.wagyourtail.commons.core.Utils;
import xyz.wagyourtail.commons.core.logger.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ExecutionException;

public class ReferenceGraph {
    private final Logger logger;
    private final Set<Integer> versions = new ConcurrentSkipListSet<>();
    private final Map<Type, Map<Integer, References>> references = new ConcurrentHashMap<>();
    private final Map<Type, Map<Integer, ClassNode>> classNodes = new ConcurrentHashMap<>();
    private final boolean retainClassNodes;
    private final boolean retainInsns;

    public ReferenceGraph(Logger logger) {
        this(logger, false);
    }

    public ReferenceGraph(Logger logger, boolean retainClassNodes) {
        this(logger, retainClassNodes, false);
    }

    public ReferenceGraph(Logger logger, boolean retainClassNodes, boolean retainInsns) {
        this.logger = logger.subLogger(ReferenceGraph.class.getSimpleName());
        this.retainClassNodes = retainClassNodes;
        this.retainInsns = retainInsns;
        if (retainInsns && !retainClassNodes) {
            throw new IllegalArgumentException("Cannot retain instructions without retaining class nodes.");
        }
        versions.add(0);
    }


    /**
     * if the {@link ClassNode} is retained, this will return the {@link ClassNode} for the given type.
     *
     * @return the {@link ClassNode} for the given type.
     */
    public ClassNode getClassFor(Type type, int version) {
        if (!retainClassNodes) {
            throw new IllegalStateException("Class nodes are not retained.");
        }
        return resolveVersioned(classNodes.get(type), version);
    }

    public void scan(Path root, Filter filter) throws IOException, ExecutionException, InterruptedException {
        scan(root, preScan(root), filter);
    }

    /**
     * Pre scan the root directory to find all the classes that need to be scanned.
     * <p>
     * you many override this with an async version
     *
     * @return a map of paths to types that need to be scanned.
     */
    public Map<Path, Type> preScan(final Path root) throws IOException, ExecutionException, InterruptedException {
        final Map<Path, Type> newScanTargets = new ConcurrentHashMap<>();
        Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
            @NotNull
            @Override
            public FileVisitResult visitFile(@NotNull Path file, @NotNull BasicFileAttributes attrs) throws IOException {
                Map.Entry<Path, Type> entry = preScanFile(root, file);
                if (entry != null) {
                    newScanTargets.put(entry.getKey(), entry.getValue());
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return newScanTargets;
    }

    protected Map.Entry<Path, Type> preScanFile(final Path root, final Path path) {
        // skip module info, it doesn't have references we care about
        if (path.getFileName().toString().equals("module-info.class")) {
            return null;
        }
        int version;
        Path rel = root.relativize(path);
        String pathStr;
        if (rel.toString().startsWith("META-INF/versions")) {
            version = Integer.parseInt(path.getName(2).toString());
            pathStr = path.subpath(3, path.getNameCount()).toString();
        } else {
            version = 0;
            pathStr = root.relativize(path).toString();
        }
        versions.add(version);
        if (pathStr.endsWith(".class")) {
            pathStr = pathStr.substring(0, pathStr.length() - 6);
            Type type = Type.getObjectType(pathStr);
            if (!references.containsKey(type)) {
                synchronized (references) {
                    if (!references.containsKey(type)) {
                        references.put(type, new ConcurrentHashMap<Integer, References>());
                        classNodes.put(type, new ConcurrentHashMap<Integer, ClassNode>());
                    }
                }
            }
            if (!references.get(type).containsKey(version)) {
                references.get(type).put(version, constructReferences(version));
                return new AbstractMap.SimpleImmutableEntry<>(path, type);
            }
        }
        return null;
    }

    /**
     * Scans the path references in order to determine the reference graphs for the classes.
     * <p>
     * you many override this with an async version
     *
     * @param newScanTargets the paths to scan.
     * @param filter         the filter to determine if a reference should be retained.
     */
    public void scan(final Path rootPath, final Map<Path, Type> newScanTargets, final Filter filter) throws IOException, ExecutionException, InterruptedException {
        for (Map.Entry<Path, Type> pathTypeEntry : newScanTargets.entrySet()) {
            scanFile(rootPath, pathTypeEntry.getKey(), pathTypeEntry.getValue(), filter);
        }
    }

    protected void scanFile(final Path rootPath, final Path path, final Type targetType, final Filter filter) throws IOException {
        int version;
        if (rootPath.relativize(path).toString().startsWith("META-INF/versions")) {
            version = Integer.parseInt(path.getName(2).toString());
        } else {
            version = 0;
        }
        try (InputStream stream = Files.newInputStream(path)) {
            ClassNode node = ASMUtils.bytesToClassNode(Utils.readAllBytes(stream));
            Type type = Type.getObjectType(node.name);
            if (!type.equals(targetType)) {
                throw new IllegalStateException("Expected path to match class name: " + path + " != " + type.getInternalName());
            }
            if (retainClassNodes) {
                classNodes.get(type).put(version, node);
            }
            references.get(type).get(version).scan(node, filter);
        }
    }

    public void debugPrint(Logger logger) {
        if (!logger.isLevel(Logger.Level.DEBUG)) return;
        logger.debug("Reference Graph:");
        for (Map.Entry<Type, Map<Integer, References>> entry : references.entrySet()) {
            for (Map.Entry<Integer, References> e : entry.getValue().entrySet()) {
                logger.debug("* " + e.getKey() + "/" + entry.getKey().getInternalName());
                References refs = e.getValue();
                for (Type t : refs.requiredInstances) {
                    logger.debug("  - " + t.getInternalName());
                }
                for (Map.Entry<MemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> e2 : refs.requiredForMembers.entrySet()) {
                    logger.debug("  - " + e2.getKey());
                    for (FullyQualifiedMemberNameAndDesc f : e2.getValue()) {
                        logger.debug("      " + f);
                    }
                }
            }
        }
    }

    public Set<Integer> getVersions() {
        return new TreeSet<>(versions);
    }

    public Map<Type, Set<Integer>> getKeys() {
        Map<Type, Set<Integer>> keys = new HashMap<>();
        for (Map.Entry<Type, Map<Integer, References>> entry : references.entrySet()) {
            keys.put(entry.getKey(), new HashSet<>(entry.getValue().keySet()));
        }
        return keys;
    }

    /**
     * @return all retained references from the graph.
     */
    public Set<FullyQualifiedMemberNameAndDesc> getAllRefs(int version) {
        Set<FullyQualifiedMemberNameAndDesc> refs = new HashSet<>();
        for (Map.Entry<Type, Map<Integer, References>> entry : references.entrySet()) {
            References value = resolveVersioned(entry.getValue(), version);
            for (Type requiredInstance : value.requiredInstances) {
                refs.add(FullyQualifiedMemberNameAndDesc.of(requiredInstance));
            }
            for (Map.Entry<MemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> entry2 : value.requiredForMembers.entrySet()) {
                refs.addAll(entry2.getValue());
            }
        }
        return refs;
    }

    public <T> T resolveVersioned(Map<Integer, T> map, int version) {
        if (map == null) return null;
        for (int i = version; i >= 0; i--) {
            T t = map.get(i);
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    /**
     * @return all retained references from the graph, and where they came from.
     */
    public Map<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> getAllUsagesForRefs(int version) {
        Map<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> refs = new HashMap<>();
        for (Map.Entry<Type, Map<Integer, References>> entry : references.entrySet()) {
            Type owner = entry.getKey();
            References value = resolveVersioned(entry.getValue(), version);
            for (Type requiredInstance : value.requiredInstances) {
                FullyQualifiedMemberNameAndDesc ref = FullyQualifiedMemberNameAndDesc.of(requiredInstance);
                if (!refs.containsKey(ref)) {
                    refs.put(ref, new HashSet<FullyQualifiedMemberNameAndDesc>());
                }
                refs.get(ref).add(FullyQualifiedMemberNameAndDesc.of(owner));
            }
            for (Map.Entry<MemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> e : value.requiredForMembers.entrySet()) {
                FullyQualifiedMemberNameAndDesc target = e.getKey().toFullyQualified(owner);
                for (FullyQualifiedMemberNameAndDesc ref : e.getValue()) {
                    if (!refs.containsKey(ref)) {
                        refs.put(ref, new HashSet<FullyQualifiedMemberNameAndDesc>());
                    }
                    refs.get(ref).add(target);
                }
            }
        }
        return refs;
    }

    public void debugPrintUsageRefs(Map<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> refs) {
        if (!logger.isLevel(Logger.Level.DEBUG)) return;
        logger.debug("Usage Refs:");
        for (Map.Entry<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> entry : refs.entrySet()) {
            logger.debug("- " + entry.getKey());
            for (FullyQualifiedMemberNameAndDesc f : entry.getValue()) {
                logger.debug("  * " + f);
            }
        }
    }

    /**
     * Given a set of references, this will scan the current reference graph to determine which references are required
     * by the given references.
     *
     * @param starts the references to start from.
     * @return a pair of the references required by the given references, and the resources required by the given references.
     */
    public ClassesAndResources recursiveResolveFrom(Set<FullyQualifiedMemberNameAndDesc> starts, int version) {
        Set<FullyQualifiedMemberNameAndDesc> refs = new HashSet<>(starts);
        Set<String> resources = new HashSet<>();
        Queue<FullyQualifiedMemberNameAndDesc> toAdd = new ArrayDeque<>(starts);
        while (!toAdd.isEmpty()) {
            FullyQualifiedMemberNameAndDesc next = toAdd.poll();
            References refer = resolveVersioned(references.get(next.getOwner()), version);
            if (refer == null) continue;
            if (next.isClassRef()) {
                for (MemberNameAndDesc instanceMember : refer.instanceMembers) {
                    FullyQualifiedMemberNameAndDesc f = instanceMember.toFullyQualified(next.getOwner());
                    if (refs.add(f)) {
                        toAdd.add(f);
                    }
                }
                for (Type t : refer.requiredInstances) {
                    FullyQualifiedMemberNameAndDesc f = FullyQualifiedMemberNameAndDesc.of(t);
                    if (refs.add(f)) {
                        toAdd.add(f);
                    }
                }
            } else {
                Set<FullyQualifiedMemberNameAndDesc> nextRefs = refer.requiredForMembers.get(next.toMemberNameAndDesc());
                if (nextRefs != null) {
                    for (FullyQualifiedMemberNameAndDesc f : nextRefs) {
                        if (refs.add(f)) {
                            toAdd.add(f);
                        }
                    }
                }
            }
        }
        // aggregate resources required
        for (FullyQualifiedMemberNameAndDesc ref : refs) {
            References refer = resolveVersioned(references.get(ref.getOwner()), version);
            if (refer == null) continue;
            MemberNameAndDesc member = ref.toMemberNameAndDesc();
            if (refer.resourceList.containsKey(member)) {
                resources.addAll(refer.resourceList.get(member));
            }
        }
        return new ClassesAndResources(refs, resources);
    }

    /**
     * Given a set of references, this will scan the current reference graph to determine which references are required
     * by the given references, and where they came from.
     *
     * @param starts the references to start from.
     * @return a pair of the references required by the given references, and the resources required by the given references.
     */
    public ClassReferencesAndResources recursivelyResolveUsagesFrom(Map<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> starts, int version) {
        Map<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> usages = new HashMap<>(starts);
        Set<String> resources = new HashSet<>();
        Queue<FullyQualifiedMemberNameAndDesc> toScan = new ArrayDeque<>(starts.keySet());
        while (!toScan.isEmpty()) {
            FullyQualifiedMemberNameAndDesc next = toScan.poll();
            References refer = resolveVersioned(references.get(next.getOwner()), version);
            if (refer == null) continue;
            if (next.isClassRef()) {
                for (MemberNameAndDesc instanceMember : refer.instanceMembers) {
                    FullyQualifiedMemberNameAndDesc f = instanceMember.toFullyQualified(next.getOwner());
                    if (!usages.containsKey(f)) {
                        usages.put(f, new HashSet<FullyQualifiedMemberNameAndDesc>());
                        toScan.add(f);
                    }
                    usages.get(f).add(next);
                }
                for (Type t : refer.requiredInstances) {
                    FullyQualifiedMemberNameAndDesc f = FullyQualifiedMemberNameAndDesc.of(t);
                    if (!usages.containsKey(f)) {
                        usages.put(f, new HashSet<FullyQualifiedMemberNameAndDesc>());
                        toScan.add(f);
                    }
                    usages.get(f).add(next);
                }
            } else {
                Set<FullyQualifiedMemberNameAndDesc> nextRefs = refer.requiredForMembers.get(next.toMemberNameAndDesc());
                if (nextRefs != null) {
                    for (FullyQualifiedMemberNameAndDesc f : nextRefs) {
                        if (!usages.containsKey(f)) {
                            usages.put(f, new HashSet<FullyQualifiedMemberNameAndDesc>());
                            toScan.add(f);
                        }
                        usages.get(f).add(next);
                    }
                }
            }
        }
        // aggregate resources required
        for (Map.Entry<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> entry : usages.entrySet()) {
            FullyQualifiedMemberNameAndDesc ref = entry.getKey();
            References refer = resolveVersioned(references.get(ref.getOwner()), version);
            if (refer == null) continue;
            MemberNameAndDesc member = ref.toMemberNameAndDesc();
            if (refer.resourceList.containsKey(member)) {
                resources.addAll(refer.resourceList.get(member));
            }
        }
        return new ClassReferencesAndResources(usages, resources);
    }

    /**
     * Given a reference, this will scan the current reference graph for all instructions that reference the given reference.
     */
    public Set<AbstractInsnNode> getAllInsnsFor(FullyQualifiedMemberNameAndDesc ref, int version) {
        if (!retainInsns) {
            throw new IllegalStateException("Insns are not retained.");
        }
        Set<AbstractInsnNode> insns = new HashSet<>();
        for (Map<Integer, References> value : references.values()) {
            References v = value.get(version);
            // unlike all the other methods, we only want the insns for the given version.
            if (v == null) continue;
            Set<AbstractInsnNode> ins = v.memberInsns.get(ref);
            if (ins != null) {
                insns.addAll(ins);
            }
        }
        return insns;
    }

    protected References constructReferences(int version) {
        return new References(version);
    }

    /**
     * A filter to determine if a reference should be retained.
     */
    public interface Filter {

        boolean shouldInclude(FullyQualifiedMemberNameAndDesc member);

    }

    @Getter
    public static class ClassesAndResources {
        private final Set<FullyQualifiedMemberNameAndDesc> classes;
        private final Set<String> resources;

        public ClassesAndResources(Set<FullyQualifiedMemberNameAndDesc> classes, Set<String> resources) {
            this.classes = classes;
            this.resources = resources;
        }

    }

    @Data
    public static class ClassReferencesAndResources {
        private final Map<FullyQualifiedMemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> classes;
        private final Set<String> resources;

    }

    public class References {
        /**
         * The multi-release version of the class.
         */
        private final int version;

        /**
         * The required classes to construct the class.
         */
        private final List<Type> requiredInstances = new ArrayList<>();

        /**
         * The required references for each member of the class.
         */
        private final Map<MemberNameAndDesc, Set<FullyQualifiedMemberNameAndDesc>> requiredForMembers = new HashMap<>();

        /**
         * if retainInsns is true, this will retain the instructions for each member referenced by the class.
         */
        private final Map<FullyQualifiedMemberNameAndDesc, Set<AbstractInsnNode>> memberInsns = new HashMap<>();

        /**
         * The list of instance members in the class.
         */
        private final List<MemberNameAndDesc> instanceMembers = new ArrayList<>();

        /**
         * the list of resources required by each member.
         */
        private final Map<MemberNameAndDesc, List<String>> resourceList = new HashMap<>();

        public References(int version) {
            this.version = version;
        }

        public void scan(ClassNode classNode, Filter filter) {
            Type currentType = Type.getObjectType(classNode.name);
            // super
            Type superType = Type.getObjectType(classNode.superName);
            if (filter.shouldInclude(FullyQualifiedMemberNameAndDesc.of(superType))) {
                requiredInstances.add(superType);
            }
            // interfaces
            for (String s : classNode.interfaces) {
                Type interfaceType = Type.getObjectType(s);
                if (filter.shouldInclude(FullyQualifiedMemberNameAndDesc.of(interfaceType))) {
                    requiredInstances.add(interfaceType);
                }
            }

            boolean hasClinit = false;
            for (MethodNode method : classNode.methods) {
                if ("<clinit>".equals(method.name)) {
                    hasClinit = true;
                    break;
                }
            }

            // fields
            for (FieldNode field : classNode.fields) {
                scanField(currentType, hasClinit, field, filter);
            }

            // methods
            for (MethodNode method : classNode.methods) {
                scanMethod(currentType, method, filter);
            }
        }

        protected void scanField(Type currentType, boolean hasClinit, FieldNode field, Filter filter) {
            MemberNameAndDesc fieldMember = new MemberNameAndDesc(field.name, Type.getType(field.desc));
            requiresInstance(fieldMember, Type.getType(field.desc), filter);
            if (field.value == null && hasClinit) {
                requiresMember(fieldMember, new FullyQualifiedMemberNameAndDesc(currentType, "<clinit>", Type.getMethodType("()V")), filter, null);
            }
            if ((field.access & Opcodes.ACC_STATIC) == 0) {
                requiresInstance(fieldMember, currentType, filter);
            }
        }

        protected void scanMethod(Type currentType, MethodNode method, Filter filter) {
            Type methodType = Type.getMethodType(method.desc);
            final MemberNameAndDesc methodMember = new MemberNameAndDesc(method.name, Type.getMethodType(method.desc));

            requiresInstance(methodMember, methodType, filter);
            if ((method.access & Opcodes.ACC_STATIC) == 0) {
                requiresInstance(methodMember, currentType, filter);
                instanceMembers.add(methodMember);
            }

            for (AbstractInsnNode insn : method.instructions) {
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode min = (MethodInsnNode) insn;
                    requiresMember(methodMember, new FullyQualifiedMemberNameAndDesc(Type.getObjectType(min.owner), min.name, Type.getMethodType(min.desc)), filter, insn);
                } else if (insn instanceof TypeInsnNode) {
                    TypeInsnNode tin = (TypeInsnNode) insn;
                    requiresInstance(methodMember, Type.getObjectType(tin.desc), filter);
                } else if (insn instanceof FieldInsnNode) {
                    FieldInsnNode fin = (FieldInsnNode) insn;
                    requiresMember(methodMember, new FullyQualifiedMemberNameAndDesc(Type.getObjectType(fin.owner), fin.name, Type.getType(fin.desc)), filter, insn);
                } else if (insn instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;
                    scanIndy(methodMember, indy, filter);
                } else if (insn instanceof MultiANewArrayInsnNode) {
                    MultiANewArrayInsnNode mana = (MultiANewArrayInsnNode) insn;
                    requiresInstance(methodMember, Type.getType(mana.desc), filter);
                } else if (insn instanceof LdcInsnNode) {
                    LdcInsnNode ldc = (LdcInsnNode) insn;
                    if (ldc.cst instanceof Type) {
                        requiresInstance(methodMember, (Type) ldc.cst, filter);
                    } else if (ldc.cst instanceof ConstantDynamic) {
                        scanCondy(methodMember, (ConstantDynamic) ldc.cst, filter, insn);
                    }
                }
            }
        }

        protected void scanIndy(MemberNameAndDesc member, InvokeDynamicInsnNode indy, Filter filter) {
            requiresInstance(member, Type.getMethodType(indy.desc), filter);
            Handle bsm = indy.bsm;
            requiresMember(member, new FullyQualifiedMemberNameAndDesc(Type.getObjectType(bsm.getOwner()), bsm.getName(), Type.getType(bsm.getDesc())), filter, indy);
            scanBSMArgs(member, indy.bsmArgs, filter, indy);
        }

        protected void scanCondy(MemberNameAndDesc member, ConstantDynamic condy, Filter filter, AbstractInsnNode insn) {
            Handle bsm = condy.getBootstrapMethod();
            requiresMember(member, new FullyQualifiedMemberNameAndDesc(Type.getObjectType(bsm.getOwner()), bsm.getName(), Type.getType(bsm.getDesc())), filter, insn);
            Object[] args = new Object[condy.getBootstrapMethodArgumentCount()];
            for (int i = 0; i < args.length; i++) {
                args[i] = condy.getBootstrapMethodArgument(i);
            }
            scanBSMArgs(member, args, filter, insn);
        }

        protected void scanBSMArgs(MemberNameAndDesc member, Object[] args, Filter filter, AbstractInsnNode insn) {
            for (Object arg : args) {
                if (arg instanceof Type) {
                    requiresInstance(member, (Type) arg, filter);
                } else if (arg instanceof Handle) {
                    Handle h = (Handle) arg;
                    requiresMember(member, new FullyQualifiedMemberNameAndDesc(Type.getObjectType(h.getOwner()), h.getName(), Type.getType(h.getDesc())), filter, insn);
                } else if (arg instanceof ConstantDynamic) {
                    scanCondy(member, (ConstantDynamic) arg, filter, insn);
                }
            }
        }

        protected void requiresInstance(MemberNameAndDesc member, Type required, Filter filter) {
            Set<FullyQualifiedMemberNameAndDesc> list = requiredForMembers.get(member);
            FullyQualifiedMemberNameAndDesc type;
            switch (required.getSort()) {
                case Type.ARRAY:
                    type = FullyQualifiedMemberNameAndDesc.of(required.getElementType());
                    if (filter.shouldInclude(type)) {
                        if (list == null) {
                            list = new HashSet<>();
                            requiredForMembers.put(member, list);
                        }
                        list.add(type);
                    }
                    break;
                case Type.OBJECT:
                    type = FullyQualifiedMemberNameAndDesc.of(required);
                    if (filter.shouldInclude(type)) {
                        if (list == null) {
                            list = new HashSet<>();
                            requiredForMembers.put(member, list);
                        }
                        list.add(type);
                    }
                    break;
                case Type.METHOD:
                    requiresInstance(member, required.getReturnType(), filter);
                    for (Type t : required.getArgumentTypes()) {
                        requiresInstance(member, t, filter);
                    }
                    break;
                default:
                    break;
            }
        }

        protected void requiresMember(MemberNameAndDesc member, FullyQualifiedMemberNameAndDesc required, Filter filter, AbstractInsnNode insn) {
            Set<FullyQualifiedMemberNameAndDesc> list = requiredForMembers.get(member);
            if (filter.shouldInclude(required)) {
                if (list == null) {
                    list = new HashSet<>();
                    requiredForMembers.put(member, list);
                }
                list.add(required);
                if (retainInsns && insn != null) {
                    Set<AbstractInsnNode> insns = memberInsns.get(required);
                    if (insns == null) {
                        insns = new HashSet<>();
                        memberInsns.put(required, insns);
                    }
                    insns.add(insn);
                }
            }
        }

        protected void requiresResource(MemberNameAndDesc member, String resourceLocation) {
            if (!resourceList.containsKey(member)) {
                resourceList.put(member, new ArrayList<String>());
            }
            resourceList.get(member).add(resourceLocation);
        }

    }

}
