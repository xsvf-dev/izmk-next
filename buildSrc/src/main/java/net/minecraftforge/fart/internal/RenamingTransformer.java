/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.fart.internal;

import net.minecraftforge.fart.api.ClassProvider;
import net.minecraftforge.fart.api.Transformer;
import net.minecraftforge.srgutils.IMappingFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RenamingTransformer implements Transformer {
    private static final String ABSTRACT_FILE = "fernflower_abstract_parameter_names.txt";
    private final @NotNull EnhancedRemapper remapper;
    private final Set<String> abstractParams = ConcurrentHashMap.newKeySet();
    private final boolean collectAbstractParams;

    public RenamingTransformer(ClassProvider classProvider,
                               IMappingFile map, Consumer<String> log) {
        this(classProvider, map, log, true);
    }

    public RenamingTransformer(ClassProvider classProvider, IMappingFile map,
                               Consumer<String> log, boolean collectAbstractParams) {
        this.collectAbstractParams = collectAbstractParams;
        this.remapper = new EnhancedRemapper(classProvider, map, log);
    }

    @Override
    public @NotNull ClassEntry process(@NotNull ClassEntry entry) {
        ClassReader reader = new ClassReader(entry.getData());
        ClassWriter writer = new ClassWriter(0);
        ClassRemapper remapper = new EnhancedClassRemapper(writer, this.remapper, this);

        reader.accept(remapper, 0);

        byte[] data = writer.toByteArray();
        String newName = this.remapper.map(entry.getClassName());

        if (entry.isMultiRelease())
            return ClassEntry.create(newName, entry.getTime(), data, entry.getVersion());
        return ClassEntry.create(newName + ".class", entry.getTime(), data);
    }

    @Override
    public @Nullable ResourceEntry process(@NotNull ResourceEntry entry) {
        if (ABSTRACT_FILE.equals(entry.getName()))
            return null;

        return entry;
    }

    @Override
    public @NotNull Collection<? extends Entry> getExtras() {
        if (abstractParams.isEmpty() || !collectAbstractParams)
            return Collections.emptyList();
        byte[] data = abstractParams.stream().sorted().collect(Collectors.joining("\n")).getBytes(StandardCharsets.UTF_8);
        return Collections.singletonList(ResourceEntry.create(ABSTRACT_FILE, Entry.STABLE_TIMESTAMP, data));
    }

    void storeNames(String className, String methodName, String methodDescriptor, @NotNull Collection<String> paramNames) {
        abstractParams.add(className + ' ' + methodName + ' ' + methodDescriptor + ' ' + String.join(" ", paramNames));
    }
}
