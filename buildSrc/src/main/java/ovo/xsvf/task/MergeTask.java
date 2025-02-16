package ovo.xsvf.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import ovo.xsvf.encrypt.EncryptUtil;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class MergeTask extends DefaultTask {
    public File loader;
    public File core;
    public File output;

    @TaskAction
    public void run() throws IOException {
        assert loader != null && core != null && output != null;
        if (!output.exists()) output.getParentFile().mkdirs();
        try (ZipInputStream loaderZip = new ZipInputStream(new FileInputStream(loader));
                FileInputStream coreZip = new FileInputStream(core);
                InputStream coreFileProvider = MergeTask.class.getResourceAsStream("/ovo/xsvf/encrypt/SimpleCoreFileProvider.class");
                FileOutputStream outputZip = new FileOutputStream(output)) {
            byte[] core = coreZip.readAllBytes();
            outputZip.write(EncryptUtil.encodeInt(core.length));
            outputZip.write(core);

            // now create a new zip file
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(baos);
            // first, add the loader
            ZipEntry entry;
            while ((entry = loaderZip.getNextEntry()) != null) {
                if (entry.getName().equals("ovo/xsvf/CoreFileProvider.class")) continue;
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(loaderZip.readAllBytes());
                zipOutputStream.closeEntry();
            }
            // add simple core file provider
            zipOutputStream.putNextEntry(new ZipEntry("ovo/xsvf/CoreFileProvider.class"));
            assert coreFileProvider != null;

            byte[] b = coreFileProvider.readAllBytes();
            ClassReader cr = new ClassReader(b);
            ClassNode cn = new ClassNode();
            cr.accept(cn, 0);
            cn.name = "ovo/xsvf/CoreFileProvider";
            cn.methods.forEach(m -> {
                m.instructions.forEach(it -> {
                    if (it instanceof MethodInsnNode methodInsnNode && methodInsnNode.owner.equals("ovo/xsvf/encrypt/SimpleCoreFileProvider")) {
                        methodInsnNode.owner = "ovo/xsvf/CoreFileProvider";
                    } else if (it instanceof FieldInsnNode fieldInsnNode && fieldInsnNode.owner.equals("ovo/xsvf/encrypt/SimpleCoreFileProvider")) {
                        fieldInsnNode.owner = "ovo/xsvf/CoreFileProvider";
                    }
                });
            });
            ClassWriter cw = new ClassWriter(0);
            cn.accept(cw);

            zipOutputStream.write(cw.toByteArray());
            zipOutputStream.close();

            // write the loader into output
            byte[] loaderBytes = baos.toByteArray();
            outputZip.write(loaderBytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
