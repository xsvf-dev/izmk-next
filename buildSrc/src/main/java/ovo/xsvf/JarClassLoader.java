package ovo.xsvf;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class JarClassLoader extends ClassLoader {
    private final HashMap<String, byte[]> jarClasses = new HashMap<>();
    public final Consumer<String> log;

    public JarClassLoader(List<Path> jars, ClassLoader parent, Consumer<String> log) {
        super(parent);
        this.log = log;
        for (Path jar : jars) {
            try (ZipInputStream zis = new ZipInputStream(new FileInputStream(jar.toFile()))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
                        String className = entry.getName().replace("/", ".").substring(0, entry.getName().length() - 6);
                        System.out.println("Loading class " + className);
                        jarClasses.put(className, zis.readAllBytes());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (jarClasses.containsKey(name)) {
            byte[] classData = jarClasses.get(name);
            return super.defineClass(name, classData, 0, classData.length);
        }
        return super.findClass(name);
    }
}
