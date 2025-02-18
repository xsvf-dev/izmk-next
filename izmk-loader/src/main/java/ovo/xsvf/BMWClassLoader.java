package ovo.xsvf;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class BMWClassLoader extends ClassLoader {
    private final HashMap<String, byte[]> classMap = new HashMap<>();
    private final BiFunction<String, byte[], Class<?>> defineClass;

    public BMWClassLoader(Path jar, Consumer<String> pkgConsumer, BiFunction<String, byte[], Class<?>> defineClass) {
        this.defineClass = defineClass;
        for (byte[] bytes : CoreFileProvider.getBinaryFiles(jar.toString())) {
            String replaced = ASMUtil.node(bytes).name.replace('/', '.');
            classMap.put(replaced, bytes);
            pkgConsumer.accept(classToPackage(replaced));
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            byte[] bytes = classMap.get(name);
            return defineClass.apply(name.replace('.', '/'), bytes);
        }
        throw new ClassNotFoundException(name);
    }

    private static String classToPackage(String name) {
        int idx = name.lastIndexOf(46);
        return idx != -1 && idx != name.length() - 1 ? name.substring(0, idx) : "";
    }
}
