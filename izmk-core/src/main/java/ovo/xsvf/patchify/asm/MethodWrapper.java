package ovo.xsvf.patchify.asm;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.Type;
import ovo.xsvf.patchify.Mapping;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

public final class MethodWrapper {
    private static final MethodHandles.Lookup lookup = MethodHandles.lookup();
    private static final Object2ObjectMap<Pair<String, String>, MethodHandle> cachedMethods = new Object2ObjectOpenHashMap<>();
    public static Mapping mapping = null;
    private final List<Object> methodParams = new LinkedList<>();
    private final MethodHandle lookup0;

    private MethodWrapper(MethodHandle lookup) {
        this.lookup0 = lookup;
    }

    public static MethodWrapper getInstance(String classOwner, String methodName, String methodDesc) throws Exception {
        Class<?> clazz = ReflectionUtil.forName(classOwner);
        MethodHandle method = findMethod(clazz, methodName, methodDesc);
        if (method == null) {
            throw new IllegalArgumentException("Method " + methodName + " not found in class " + clazz.getName());
        }
        return new MethodWrapper(method);
    }

    private static MethodHandle findMethod(Class<?> clazz, String methodName, String methodDesc) throws Exception {
        Pair<String, String> pair = Pair.of(clazz.getName() + "/" + methodName, methodDesc);
        if (cachedMethods.containsKey(pair)) {
            return cachedMethods.get(pair);
        }
        if (mapping != null) {
            pair = mapping.revMethodsMapping.getOrDefault(pair, pair);
        }
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) &&
                    Type.getMethodDescriptor(method).equals(methodDesc)) {
                method.setAccessible(true);
                var handle = lookup.unreflect(method);
                cachedMethods.put(pair, handle);
                return handle;
            }
        }
        throw new RuntimeException("Method " + methodName + " not found in class " + clazz.getName());
    }

    public List<Object> getMethodParams() {
        return methodParams;
    }

    public MethodWrapper addParam(Object param) {
        methodParams.add(0, param);
        return this;
    }

    public Object call(Object instance) throws Exception {
        return invokeMethod(instance);
    }

    private Object invokeMethod(Object instance) {
        try {
            if (instance == null) {
                if (methodParams.isEmpty()) return lookup0.invoke();
                return lookup0.invoke(methodParams.toArray(new Object[0]));
            } else {
                if (methodParams.isEmpty()) return lookup0.invoke(instance);
                Object[] params = methodParams.toArray(new Object[0]);
                Object[] paramsWithInstance = new Object[params.length + 1];
                paramsWithInstance[0] = instance;
                System.arraycopy(params, 0, paramsWithInstance, 1, params.length);
                return lookup0.invokeWithArguments(paramsWithInstance);
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
