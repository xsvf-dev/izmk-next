package ovo.xsvf.common;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class BMWClassLoader extends ClassLoader {
    private final HashMap<String, byte[]> classMap = new HashMap<>();
    private final Function<String, URL> resourceProvider;
    private final ClassLoader parentCLassLoader;

    public BMWClassLoader(Function<byte[], String> classNameProvider,
                          Supplier<List<byte[]>> classBytesProvider,
                          Function<String, URL> resourceProvider,
                          ClassLoader parent) {
        super(getPlatformClassLoader());
        this.parentCLassLoader = parent;
        this.resourceProvider = resourceProvider;

        for (byte[] bytes : classBytesProvider.get()) {
            String replaced = classNameProvider.apply(bytes).replace('/', '.');
            classMap.put(replaced, bytes);
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (classMap.containsKey(name)) {
            byte[] bytes = classMap.get(name);
            return defineClass(name.replace('/', '.'), bytes, 0, bytes.length);
        } else return parentCLassLoader.loadClass(name);
    }

    @Override
    protected URL findResource(String name) {
        return resourceProvider.apply(name);
    }
}
