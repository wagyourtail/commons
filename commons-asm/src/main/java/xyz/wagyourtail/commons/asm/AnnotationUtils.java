package xyz.wagyourtail.commons.asm;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnnotationUtils {

    private AnnotationUtils() {
    }

    /**
     * Creates an annotation from an annotation node using the classloader of AnnotationUtils to find the annotation class (and the context classloader)
     *
     * @param annotationNode the node to construct the annotation from
     * @return the annotation
     * @param <T> the type of the annotation
     * @throws ClassNotFoundException if the annotation class cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createAnnotation(AnnotationNode annotationNode) throws ClassNotFoundException {
        Class<?> annotationClass;
        try {
            annotationClass = ASMUtils.getClass(Type.getType(annotationNode.desc), AnnotationUtils.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            annotationClass = ASMUtils.getClass(Type.getType(annotationNode.desc), Thread.currentThread().getContextClassLoader());
        }
        return createAnnotation(annotationNode, (Class<T>) annotationClass);
    }

    /**
     *
     * This is the recommended method in this class to create annotations
     *
     * @param annotationNode the node to construct the annotation from
     * @param annotationClass the class of the annotation
     * @return the annotation
     * @param <T> the type of the annotation
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createAnnotation(AnnotationNode annotationNode, Class<T> annotationClass) {
        ClassLoader loader = annotationClass.getClassLoader();
        if (!ASMUtils.equals(Type.getType(annotationNode.desc), annotationClass)) {
            throw new IllegalArgumentException("AnnotationNode type (" + annotationNode.desc + ") does not match annotation class (" + annotationClass.getName() + ")");
        }
        return (T) Proxy.newProxyInstance(
                loader,
                new Class[]{annotationClass},
                new Handler(annotationClass, annotationNode, loader)
        );
    }

    /**
     * Creates an annotation from an AnnotationNode using the specified classloader to find the annotation class
     *
     * @param annotationNode the node to construct the annotation from
     * @param loader the classloader to find the annotation class
     * @return the annotation
     * @param <T> the type of the annotation
     * @throws ClassNotFoundException if the annotation class cannot be found
     */
    @SuppressWarnings("unchecked")
    public static <T extends Annotation> T createAnnotation(AnnotationNode annotationNode, ClassLoader loader) throws ClassNotFoundException {
        Class<?> annotationClass = ASMUtils.getClass(Type.getType(annotationNode.desc), loader);
        return createAnnotation(annotationNode, (Class<T>) annotationClass);
    }


    static class Handler implements InvocationHandler {
        final Class<?> annotationClass;
        final Map<String, Object> values;
        final ClassLoader loader;

        Handler(Class<?> annotationClass, AnnotationNode node, ClassLoader loader) {
            this.annotationClass = annotationClass;
            values = new HashMap<>();
            for (int i = 0; i < node.values.size(); i += 2) {
                values.put((String) node.values.get(i), node.values.get(i + 1));
            }
            this.loader = loader;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws ClassNotFoundException {
            if (!values.containsKey(method.getName())) {
                Object annDefault = method.getDefaultValue();
                if (annDefault != null) {
                    return annDefault;
                }
                throw new NullPointerException("No default value for " + method.getName());
            }
            return convertType(values.get(method.getName()), method.getReturnType());
        }

        @SuppressWarnings("unchecked")
        private <T> T convertType(Object value, Class<T> type) throws ClassNotFoundException {
            if (value instanceof Type) {
                return (T) ASMUtils.getClass((Type) value, loader);
            }
            if (value instanceof AnnotationNode) {
                return AnnotationUtils.createAnnotation((AnnotationNode) value, loader);
            }
            if (value instanceof List<?>) {
                Class<?> componentType = type.getComponentType();
                Object arr = Array.newInstance(componentType, ((List<?>) value).size());
                for (int i = 0; i < ((List<?>) value).size(); i++) {
                    Array.set(arr, i, convertType(((List<?>) value).get(i), componentType));
                }
                return (T) arr;
            }
            if (value instanceof String[]) {
                String[] arr = (String[]) value;
                if (arr.length != 2) {
                    throw new IllegalArgumentException("Invalid array length for enum");
                }
                return (T) ASMUtils.enumValueOf(arr[0], arr[1], loader);
            }
            return (T) value;
        }

    }

}
