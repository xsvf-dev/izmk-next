package ovo.xsvf.patchify.asm;

import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Type;
import ovo.xsvf.izmk.IZMK;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MethodHelper {
    private static final HashMap<Pair<String, String>, Method> cachedMethods = new HashMap<>();

    private final List<Object> methodParams = new ArrayList<>();

    public static MethodHelper getInstance() {
        return new MethodHelper();
    }

    public List<Object> getMethodParams() {
        return methodParams;
    }

    public MethodHelper addParam(Object param) {
        methodParams.add(param);
        return this;
    }

    public MethodHelper addParams(Object[] params) {
        methodParams.addAll(List.of(params));
        return this;
    }

    public Object call(Object instance, String classOwner, String methodName, String methodDesc) {

        methodParams.forEach(it -> IZMK.logger.debug("Method param: {}", it));

        Class<?> clazz = ReflectionUtil.forName(classOwner);
        Pair<String, String> key = Pair.of(classOwner + "/" + methodName, methodDesc);
        Method method = cachedMethods.get(key);

        if (method == null) {
            method = findMethod(clazz, methodName, methodDesc);
            if (method == null) {
                IZMK.INSTANCE.getLogger().error("Method %s not found in class %s", methodName, classOwner);
                throw new IllegalArgumentException("Method " + methodName + " not found in class " + classOwner);
            }
            cachedMethods.put(key, method);
        }

        return invokeMethod(method, instance);
    }

    public Object callStatic(String classOwner, String methodName, String methodDesc) {
        Class<?> clazz = ReflectionUtil.forName(classOwner);
        Pair<String, String> key = Pair.of(clazz.getName() + "/" + methodName, methodDesc);
        Method method = cachedMethods.get(key);

        if (method == null) {
            method = findMethod(clazz, methodName, methodDesc);
            if (method == null) {
                IZMK.INSTANCE.getLogger().error("Method %s not found in class %s", methodName, clazz.getName());
                throw new IllegalArgumentException("Method " + methodName + " not found in class " + clazz.getName());
            }
            cachedMethods.put(key, method);
        }

        return invokeMethod(method, null);
    }

    private Method findMethod(Class<?> clazz, String methodName, String methodDesc) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getName().equals(methodName) &&
                    Type.getMethodDescriptor(method).equals(methodDesc)) {
                method.setAccessible(true);
                return method;
            }
        }
        return null;
    }

    private Object invokeMethod(Method method, Object instance) {
        try {
            return method.invoke(instance, methodParams.toArray());
        } catch (IllegalAccessException e) {
            IZMK.INSTANCE.getLogger().error("IllegalAccessException occurred while invoking method %s: %s", e, method.getName());
            throw new RuntimeException("IllegalAccessException occurred while invoking method " + method.getName(), e);
        } catch (InvocationTargetException e) {
            IZMK.INSTANCE.getLogger().error("InvocationTargetException occurred while invoking method %s: %s", e.getTargetException(), method.getName());
            throw new RuntimeException("InvocationTargetException occurred while invoking method " + method.getName(), e.getTargetException());
        }
    }
}
