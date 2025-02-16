package ovo.xsvf.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import ovo.xsvf.accessor.AccessorProcessor;

import java.io.File;
import java.io.IOException;

public class AccessorTask extends DefaultTask {
    public File targetFile;
    public File outputFile;

    @TaskAction
    public void run() throws IOException {
        assert targetFile != null && outputFile != null;
        final AccessorProcessor accessorProcessor = AccessorProcessor.builder()
                .inputJarFile(outputFile)
                .outputFile(outputFile)
                .log(getLogger()::info)
                .readFlags(ClassReader.EXPAND_FRAMES | ClassReader.SKIP_DEBUG)
                .writeFlags(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS)
                .build();

        accessorProcessor.load();
        accessorProcessor.preProcess();
        accessorProcessor.postProcess();
        accessorProcessor.write();
    }
}
