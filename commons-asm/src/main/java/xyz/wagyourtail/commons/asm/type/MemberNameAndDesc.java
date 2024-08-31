package xyz.wagyourtail.commons.asm.type;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

public class MemberNameAndDesc {
    private final String name;
    private final Type desc;
    private final boolean ignoreReturnValue;

    public MemberNameAndDesc(String name, Type desc) {
        this(name, desc, false);
    }

    public MemberNameAndDesc(String name, Type desc, boolean ignoreReturnValue) {
        this.name = name;
        this.desc = desc;
        this.ignoreReturnValue = ignoreReturnValue;
    }

    public static MemberNameAndDesc of(Member member) {
        if (member instanceof Method) {
            return new MemberNameAndDesc(member.getName(), Type.getType((Method) member));
        } else if (member instanceof Field) {
            return new MemberNameAndDesc(member.getName(), Type.getType(((Field) member).getType()));
        } else if (member instanceof Constructor) {
            return new MemberNameAndDesc("<init>", Type.getType((Constructor<?>) member));
        } else {
            throw new IllegalArgumentException("Unknown member type: " + member.getClass());
        }
    }

    public static MemberNameAndDesc of(MethodNode mNode) {
        return new MemberNameAndDesc(mNode.name, Type.getMethodType(mNode.desc));
    }

    public static MemberNameAndDesc of(FieldNode fNode) {
        return new MemberNameAndDesc(fNode.name, Type.getType(fNode.desc));
    }

    public MemberNameAndDesc ignoreReturnValue() {
        if (ignoreReturnValue || desc.getSort() != Type.METHOD) {
            return this;
        }
        return new MemberNameAndDesc(name, desc, true);
    }

    public String getName() {
        return name;
    }

    public Type getDesc() {
        return desc;
    }

    public boolean isIgnoreReturnValue() {
        return ignoreReturnValue;
    }

    public boolean isMethodRef() {
        return desc.getSort() == Type.METHOD;
    }

    public FullyQualifiedMemberNameAndDesc toFullyQualified(Type owner) {
        return new FullyQualifiedMemberNameAndDesc(owner, name, desc);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemberNameAndDesc that = (MemberNameAndDesc) o;
        if (desc.getSort() != that.desc.getSort()) return false;
        if (!name.equals(that.name)) return false;
        if (ignoreReturnValue && that.ignoreReturnValue && desc.getSort() == Type.METHOD) {
            return Arrays.equals(desc.getArgumentTypes(), that.desc.getArgumentTypes());
        }
        return Objects.equals(desc, that.desc);
    }

    @Override
    public String toString() {
        return name + ";" + desc.getDescriptor();
    }

    @Override
    public int hashCode() {
        if (ignoreReturnValue && desc.getSort() == Type.METHOD) {
            return Objects.hash(name, Arrays.hashCode(desc.getArgumentTypes()));
        }
        return Objects.hash(name, desc);
    }

}

