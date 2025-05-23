/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.fart.internal;

import net.minecraftforge.fart.api.ClassProvider;
import net.minecraftforge.fart.api.Renamer;
import net.minecraftforge.fart.api.Transformer;
import net.minecraftforge.fart.api.Transformer.ClassEntry;
import net.minecraftforge.fart.api.Transformer.Entry;
import net.minecraftforge.fart.api.Transformer.ManifestEntry;
import net.minecraftforge.fart.api.Transformer.ResourceEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

class RenamerImpl implements Renamer {
    static final int MAX_ASM_VERSION = Opcodes.ASM9;
    private static final String MANIFEST_NAME = "META-INF/MANIFEST.MF";
    private final List<File> libraries;
    private final List<Transformer> transformers;
    private final SortedClassProvider sortedClassProvider;
    private final @NotNull List<ClassProvider> classProviders;
    private final int threads;
    private final Consumer<String> logger;
    @SuppressWarnings("unused")
    private final Consumer<String> debug;
    private boolean setup = false;
    private ClassProvider libraryClasses;

    RenamerImpl(List<File> libraries, List<Transformer> transformers, SortedClassProvider sortedClassProvider, @NotNull List<ClassProvider> classProviders,
                int threads, Consumer<String> logger, Consumer<String> debug) {
        this.libraries = libraries;
        this.transformers = transformers;
        this.sortedClassProvider = sortedClassProvider;
        this.classProviders = Collections.unmodifiableList(classProviders);
        this.threads = threads;
        this.logger = logger;
        this.debug = debug;
    }

    private void setup() {
        if (this.setup)
            return;

        this.setup = true;

        ClassProvider.Builder libraryClassesBuilder = ClassProvider.builder().shouldCacheAll(true);
        this.logger.accept("Adding Libraries to Inheritance");
        this.libraries.forEach(f -> libraryClassesBuilder.addLibrary(f.toPath()));

        this.libraryClasses = libraryClassesBuilder.build();
    }

    @Override
    public void run(@NotNull File input, @NotNull File output) {
        if (!this.setup)
            this.setup();

        if (input == null)
            throw new IllegalArgumentException("input argument can't be null");
        if (output == null)
            throw new IllegalArgumentException("output argument can't be null");
        if (!input.exists())
            throw new IllegalArgumentException("Input file not found: " + input.getAbsolutePath());

        input = input.getAbsoluteFile();
        output = output.getAbsoluteFile();

        logger.accept("Reading Input: " + input.getAbsolutePath());
        // Read everything from the input jar!
        List<Entry> oldEntries = new ArrayList<>();
        try (ZipFile in = new ZipFile(input)) {
            Util.forZip(in, e -> {
                if (e.isDirectory())
                    return;
                String name = e.getName();
                byte[] data = Util.toByteArray(in.getInputStream(e));

                if (name.endsWith(".class"))
                    oldEntries.add(ClassEntry.create(name, e.getTime(), data));
                else if (name.equals(MANIFEST_NAME))
                    oldEntries.add(ManifestEntry.create(e.getTime(), data));
                else
                    oldEntries.add(ResourceEntry.create(name, e.getTime(), data));
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not parse input: " + input.getAbsolutePath(), e);
        }

        this.sortedClassProvider.clearCache();
        ArrayList<ClassProvider> classProviders = new ArrayList<>(this.classProviders);
        classProviders.add(0, this.libraryClasses);
        this.sortedClassProvider.classProviders = classProviders;

        AsyncHelper async = new AsyncHelper(threads);
        try {

            /* Disabled until we do something with it
            // Gather original file Hashes, so that we can detect changes and update the manifest if necessary
            log("Gathering original hashes");
            Map<String, String> oldHashes = async.invokeAll(oldEntries,
                e -> new Pair<>(e.getName(), HashFunction.SHA256.hash(e.getData()))
            ).stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            */

            List<ClassEntry> ourClasses = oldEntries.stream()
                .filter(e -> e instanceof ClassEntry && !e.getName().startsWith("META-INF/"))
                .map(ClassEntry.class::cast)
                .collect(Collectors.toList());

            // Add the original classes to the inheritance map, TODO: Multi-Release somehow?
            logger.accept("Adding input to inheritance map");
            ClassProvider.Builder inputClassesBuilder = ClassProvider.builder();
            async.consumeAll(ourClasses, ClassEntry::getClassName, c ->
                inputClassesBuilder.addClass(c.getName().substring(0, c.getName().length() - 6), c.getData())
            );
            classProviders.add(0, inputClassesBuilder.build());

            // Process everything
            logger.accept("Processing entries");
            List<Entry> newEntries = async.invokeAll(oldEntries, Entry::getName, this::processEntry);

            logger.accept("Adding extras");
            transformers.forEach(t -> newEntries.addAll(t.getExtras()));

            Set<String> seen = new HashSet<>();
            String dupes = newEntries.stream().map(Entry::getName)
                .filter(n -> !seen.add(n))
                .sorted()
                .collect(Collectors.joining(", "));
            if (!dupes.isEmpty())
                throw new IllegalStateException("Duplicate entries detected: " + dupes);

            /*
            log("Collecting new hashes");
            Map<String, String> newHashes = async.invokeAll(newEntries,
                e -> new Pair<>(e.getName(), HashFunction.SHA256.hash(e.getData()))
            ).stream().collect(Collectors.toMap(Pair::getLeft, Pair::getRight));
            */

            // We care about stable output, so sort, and single thread write.
            logger.accept("Sorting");
            newEntries.sort(this::compare);

            if (!output.getParentFile().exists())
                output.getParentFile().mkdirs();

            seen.clear();
            logger.accept("Writing Output: " + output.getAbsolutePath());
            try (FileOutputStream fos = new FileOutputStream(output);
                ZipOutputStream zos = new ZipOutputStream(fos)) {
                // Explicitly set compression level because of potential differences based on environment.
                // See https://github.com/MinecraftForge/JarSplitter/pull/2
                zos.setLevel(6);

                for (Entry e : newEntries) {
                    String name = e.getName();
                    int idx = name.lastIndexOf('/');
                    if (idx != -1)
                        addDirectory(zos, seen, name.substring(0, idx));

                    logger.accept("  " + name);
                    ZipEntry entry = new ZipEntry(name);
                    entry.setTime(e.getTime());
                    zos.putNextEntry(entry);
                    zos.write(e.getData());
                    zos.closeEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException("Could not write output to file: " + output.getAbsolutePath(), e);
            }
        } finally {
            async.shutdown();
        }
    }

    // Tho Directory entries are not strictly necessary, we add them because some bad implementations of Zip extractors
    // attempt to extract files without making sure the parents exist.
    private void addDirectory(@NotNull ZipOutputStream zos, @NotNull Set<String> seen, @NotNull String path) throws IOException {
        if (!seen.add(path))
            return;

        int idx = path.lastIndexOf('/');
        if (idx != -1)
            addDirectory(zos, seen, path.substring(0, idx));

        logger.accept("  " + path + '/');
        ZipEntry dir = new ZipEntry(path + '/');
        dir.setTime(Entry.STABLE_TIMESTAMP);
        zos.putNextEntry(dir);
        zos.closeEntry();
    }

    private @Nullable Entry processEntry(final Entry start) {
        Entry entry = start;
        for (Transformer transformer : RenamerImpl.this.transformers) {
            entry = entry.process(transformer);
            if (entry == null)
                return null;
        }
        return entry;
    }

    private int compare(@NotNull Entry o1, @NotNull Entry o2) {
        // In order for JarInputStream to work, MANIFEST has to be the first entry, so make it first!
        if (MANIFEST_NAME.equals(o1.getName()))
            return MANIFEST_NAME.equals(o2.getName()) ? 0 : -1;
        if (MANIFEST_NAME.equals(o2.getName()))
            return MANIFEST_NAME.equals(o1.getName()) ? 0 :  1;
        return o1.getName().compareTo(o2.getName());
    }

    @Override
    public void close() throws IOException {
        this.sortedClassProvider.close();
    }
}
