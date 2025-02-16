package ovo.xsvf.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import ovo.xsvf.encrypt.EncryptUtil;
import ovo.xsvf.encrypt.RandomUtil;

import java.io.*;
import java.util.Random;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

// trashes and merge core into jar file
public class EncryptMergeTask2 extends DefaultTask {
    public File loader;
    public File core;
    public File output;

    private final Random random = new Random();

    @TaskAction
    public void run() throws IOException {
        assert loader != null && core != null && output != null;
        if (!output.exists()) output.getParentFile().mkdirs();
        try (ZipInputStream loaderZip = new ZipInputStream(new FileInputStream(loader));
             ZipInputStream coreZip = new ZipInputStream(new FileInputStream(core));
             InputStream coreFileProvider = MergeTask.class.getResourceAsStream("/ovo/xsvf/encrypt/EncryptCoreFileProvider.class");
             FileOutputStream outputZip = new FileOutputStream(output)) {
            // first, write some trash datas
            int trashLength = random.nextInt(1024, 4096);
            byte[] trashBytes = EncryptUtil.getRandomBytes(trashLength);
            outputZip.write(EncryptUtil.encodeInt(trashLength));
            outputZip.write(trashBytes);

            // next, write core zip
            ByteArrayOutputStream coreStream = new ByteArrayOutputStream();
            ZipOutputStream coreZipStream = new ZipOutputStream(coreStream);
            ZipEntry coreEntry;
            while ((coreEntry = coreZip.getNextEntry())!= null) {
                if (!coreEntry.isDirectory()) {
                    if (coreEntry.getName().endsWith(".class")) {
                        // encrypt class file
                        byte[] classBytes = EncryptUtil.encodeClassBytes(coreZip.readAllBytes());
                        coreZipStream.putNextEntry(new ZipEntry(RandomUtil.generateRandomFileName(2) + ".json"));
                        coreZipStream.write(classBytes);
                        coreZipStream.closeEntry();
                    } else {
                        // copy other files
                        coreZipStream.putNextEntry(coreEntry);
                        coreZipStream.write(coreZip.readAllBytes());
                        coreZipStream.closeEntry();
                    }
                }
            }
            // add some trash entries
            for (int i = 0; i < RandomUtil.generateRandomNumber(100, 200); i++) {
                String name = RandomUtil.generateRandomFileName(2) + ".json";
                byte[] bytes = EncryptUtil.getRandomBytes(random.nextInt(1024, 4096));
                coreZipStream.putNextEntry(new ZipEntry(name));
                coreZipStream.write(0x00); // to make it not start with CA FE BA BE
                coreZipStream.write(bytes);
                coreZipStream.closeEntry();
            }
            byte[] coreBytes = coreStream.toByteArray();
            for (int i = 0; i < coreBytes.length; i++) coreBytes[i] ^= (byte) 0xCAFEBEEF;
            coreZipStream.close(); coreStream.close();

            // write ez encrypted core zip
            outputZip.write(EncryptUtil.encodeInt(coreBytes.length));
            outputZip.write(coreBytes);

            // write some trash data to crash bandizip
            int length = RandomUtil.generateRandomNumber(4096, 16384);
            outputZip.write(EncryptUtil.encodeInt(length));
            outputZip.write(EncryptUtil.getRandomBytes(length));

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(baos);

            // first, add the loader
            ZipEntry entry;
            while ((entry = loaderZip.getNextEntry())!= null) {
                zipOutputStream.putNextEntry(entry);
                zipOutputStream.write(loaderZip.readAllBytes());
                zipOutputStream.closeEntry();
            }

            // add some trash entries
            for (int i = 0; i < RandomUtil.generateRandomNumber(100, 200); i++) {
                zipOutputStream.putNextEntry(new ZipEntry(RandomUtil.generateRandomFileName(2) + ".class"));
                zipOutputStream.write(0xCA);
                zipOutputStream.write(0xFE);
                zipOutputStream.write(0xBA);
                zipOutputStream.write(0xBE); // to make reverse engineers confused
                zipOutputStream.write(EncryptUtil.getRandomBytes(random.nextInt(1024, 4096)));
                zipOutputStream.closeEntry();
            }
            zipOutputStream.close();

            // write the loader into output
            outputZip.write(baos.toByteArray());

            // the final file looks like:
            // trash data
            // encrypted (core zip with some trash entries)
            // trash data
            // loader zip (with some trash entries)
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
