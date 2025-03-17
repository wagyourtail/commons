package xyz.wagyourtail.commons.asm.type;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

@Getter
@AllArgsConstructor
public class FullyQualifiedMemberNameAndDesc {
    private final Type owner;
    private final String name;
    private final Type desc;
    private final boolean ignoreReturnValue;

    public FullyQualifiedMemberNameAndDesc(Type owner, String name, Type desc) {
        this(owner, name, desc, false);
    }

    public static FullyQualifiedMemberNameAndDesc of(Member member) {
        Type owner = Type.getType(member.getDeclaringClass());
        if (member instanceof Method) {
            return new FullyQualifiedMemberNameAndDesc(owner, member.getName(), Type.getType((Method) member));
        } else if (member instanceof Field) {
            return new FullyQualifiedMemberNameAndDesc(owner, member.getName(), Type.getType(((Field) member).getType()));
        } else if (member instanceof Constructor) {
            return new FullyQualifiedMemberNameAndDesc(owner, "<init>", Type.getType((Constructor<?>) member));
        } else {
            throw new IllegalArgumentException("Unknown member type: " + member.getClass());
        }
    }

    public static FullyQualifiedMemberNameAndDesc of(String value) {
        String[] vals = value.split(";", 3);
        if (vals.length == 2 && vals[1].isEmpty()) vals = new String[]{vals[0]};
        Type owner;
        if (vals.length == 1) {
            if (value.startsWith("L") && value.endsWith(";")) {
                owner = Type.getType(value);
            } else {
                owner = Type.getObjectType(value);
            }
            return FullyQualifiedMemberNameAndDesc.of(owner);
        } else {
            owner = Type.getObjectType(vals[0].substring(1));
        }
        String name = vals[1];
        Type desc = vals.length == 2 ? null : Type.getType(vals[2]);
        return new FullyQualifiedMemberNameAndDesc(owner, name, desc);
    }

    public static FullyQualifiedMemberNameAndDesc of(Class<?> clazz) {
        return new FullyQualifiedMemberNameAndDesc(Type.getType(clazz), null, null);
    }

    public static FullyQualifiedMemberNameAndDesc of(Type type) {
        return new FullyQualifiedMemberNameAndDesc(type, null, null);
    }

    public static FullyQualifiedMemberNameAndDesc of(Handle handle) {
        return new FullyQualifiedMemberNameAndDesc(Type.getObjectType(handle.getOwner()), handle.getName(), Type.getType(handle.getDesc()));
    }

    public static FullyQualifiedMemberNameAndDesc of(MethodInsnNode min) {
        return new FullyQualifiedMemberNameAndDesc(Type.getObjectType(min.owner), min.name, Type.getMethodType(min.desc));
    }

    public static FullyQualifiedMemberNameAndDesc of(FieldInsnNode mn) {
        return new FullyQualifiedMemberNameAndDesc(Type.getObjectType(mn.owner), mn.name, Type.getType(mn.desc));
    }

    public FullyQualifiedMemberNameAndDesc ignoreReturnValue() {
        if (desc != null && !ignoreReturnValue && desc.getSort() == Type.METHOD) {
            return new FullyQualifiedMemberNameAndDesc(owner, name, desc, true);
        }
        return this;
    }

    public boolean isClassRef() {
        return name == null;
    }

    public boolean isMethodRef() {
        return desc != null && desc.getSort() == Type.METHOD;
    }

    public MemberNameAndDesc toMemberNameAndDesc() {
        if (name == null) return null;
        return new MemberNameAndDesc(name, desc);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(owner.getDescriptor());
        if (name != null) {
            sb.append(name);
            if (desc != null) {
                sb.append(";").append(desc.getDescriptor());
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FullyQualifiedMemberNameAndDesc that = (FullyQualifiedMemberNameAndDesc) o;
        if (desc != null && that.desc != null && desc.getSort() != that.desc.getSort()) return false;
        if (!Objects.equals(owner, that.owner) || !Objects.equals(name, that.name)) return false;
        if (ignoreReturnValue && that.ignoreReturnValue && desc != null && desc.getSort() == Type.METHOD) {
            return Arrays.equals(desc.getArgumentTypes(), that.desc.getArgumentTypes());
        }
        return Objects.equals(desc, that.desc);
    }

    @Override
    public int hashCode() {
        if (ignoreReturnValue && desc != null && desc.getSort() == Type.METHOD) {
            return Objects.hash(owner, name, Arrays.hashCode(desc.getArgumentTypes()));
        }
        return Objects.hash(owner, name, desc);
    }

}

