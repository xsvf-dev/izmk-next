package ovo.xsvf.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import ovo.xsvf.encrypt.EncryptUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MergeTaskB extends DefaultTask {
    public File loader;
    public File core;
    public File output;

    @TaskAction
    public void run() throws IOException {
        assert loader != null && core != null && output != null;
        if (!output.exists()) output.getParentFile().mkdirs();
        try (FileInputStream loaderZip = new FileInputStream(loader);
             FileInputStream coreZip = new FileInputStream(core);
             FileOutputStream outputZip = new FileOutputStream(output)) {
            byte[] core = coreZip.readAllBytes();
            for (int i = 0; i < core.length; i++) {
                core[i] = (byte) (core[i] ^ 0xCAFEDEEF);
            }
            outputZip.write(EncryptUtil.encodeInt(core.length));
            outputZip.write(core);
            outputZip.write(loaderZip.readAllBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
