package ovo.xsvf.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.io.*;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// put the core file provider
public class EncryptMergeTask extends DefaultTask {
    public File loader;
    public File output;

    private final Random random = new Random();

    @TaskAction
    public void run() throws IOException {
        assert loader != null && output != null;
        if (!output.exists()) output.getParentFile().mkdirs();
        try (ZipInputStream loaderZip = new ZipInputStream(new FileInputStream(loader));
             InputStream coreFileProvider = MergeTask.class.getResourceAsStream("/ovo/xsvf/encrypt/EncryptCoreFileProvider.class");
             ZipOutputStream outputZip = new ZipOutputStream(new FileOutputStream(output))) {

            // first, add the loader
            ZipEntry entry;
            while ((entry = loaderZip.getNextEntry()) != null) {
                if (entry.getName().equals("ovo/xsvf/CoreFileProvider.class")) continue;
                outputZip.putNextEntry(entry);
                outputZip.write(loaderZip.readAllBytes());
                outputZip.closeEntry();
            }

            // add core file provider
            outputZip.putNextEntry(new ZipEntry("ovo/xsvf/CoreFileProvider.class"));

            assert coreFileProvider != null;

            byte[] b = coreFileProvider.readAllBytes();
            ClassReader cr = new ClassReader(b);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            cn.name = "ovo/xsvf/CoreFileProvider";
            cn.methods.forEach(m -> {
                m.instructions.forEach(it -> {
                    if (it instanceof MethodInsnNode methodInsnNode && methodInsnNode.owner.equals("ovo/xsvf/encrypt/EncryptCoreFileProvider")) {
                        methodInsnNode.owner = "ovo/xsvf/CoreFileProvider";
                    }
                    else if (it instanceof FieldInsnNode fieldInsnNode && fieldInsnNode.owner.equals("ovo/xsvf/encrypt/EncryptCoreFileProvider")) {
                        fieldInsnNode.owner = "ovo/xsvf/CoreFileProvider";
                    }
                });
            });
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);

            outputZip.write(cw.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
