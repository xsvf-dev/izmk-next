package ovo.xsvf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class BMWClassLoader extends ClassLoader {
    private static final HashMap<String, byte[]> classMap = new HashMap<>();
    private final BiFunction<String, byte[], Class<?>> defineClass;

    public BMWClassLoader(Function<byte[], String> classNameProvider,
                          Supplier<List<byte[]>> classBytesProvider,
                          BiFunction<String, byte[], Class<?>> defineClass) {
        this.defineClass = defineClass;

        for (byte[] bytes : classBytesProvider.get()) {
            String replaced = classNameProvider.apply(bytes).replace('/', '.');
            classMap.put(replaced, bytes);
        }
    }

    public static Set<Map.Entry<String, byte[]>> getClasses() {
        return classMap.entrySet();
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            byte[] bytes = classMap.get(name);
            return defineClass.apply(name.replace('.', '/'), bytes);
        }
        throw new ClassNotFoundException(name);
    }
}
