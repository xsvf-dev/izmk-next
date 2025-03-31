package ovo.xsvf.task;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import ovo.xsvf.accessor.AccessorProcessor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class AccessorTask extends DefaultTask {
    public File targetFile;
    public File outputFile;
    public List<Path> libs = new ArrayList<>();

    @TaskAction
    public void run() throws IOException {
        assert targetFile != null && outputFile != null;
        final AccessorProcessor accessorProcessor = AccessorProcessor.builder()
                .inputJarFile(outputFile)
                .outputFile(outputFile)
                .libraryJars(libs)
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
