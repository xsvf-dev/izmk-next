package ovo.xsvf.task;

import net.minecraftforge.fart.api.Renamer;
import net.minecraftforge.fart.api.Transformer;
import net.minecraftforge.srgutils.IMappingFile;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class RemapTask extends DefaultTask {
    public File mapFile;
    public File targetFile;
    public File outputFile;
    public @NotNull Set<File> libs = new HashSet<>();

    @TaskAction
    public void run() throws IOException {
        assert mapFile != null && targetFile != null && outputFile != null;
        runFART(targetFile, outputFile, libs, mapFile, getLogger()::debug);
    }

    private static void runFART(File input, File output, @NotNull Set<File> libs, @NotNull File mapFile,
                             @NotNull Consumer<String> log) throws IOException {
        Renamer.Builder builder = Renamer.builder();
        builder.withJvmClasspath();
        builder.logger(log);

        for (File lib : libs) builder.lib(lib);
        builder.threads(12);

        IMappingFile mappings = IMappingFile.load(mapFile).reverse();

        builder.add(Transformer.renamerFactory(mappings, true));
        builder.add(Transformer.parameterAnnotationFixerFactory());
        builder.add(Transformer.recordFixerFactory());
        try (Renamer renamer = builder.build()) {
            renamer.run(input, output);
        }
    }
}
