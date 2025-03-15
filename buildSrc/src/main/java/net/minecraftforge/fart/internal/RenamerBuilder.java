/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.fart.internal;

import net.minecraftforge.fart.api.ClassProvider;
import net.minecraftforge.fart.api.Renamer;
import net.minecraftforge.fart.api.Renamer.Builder;
import net.minecraftforge.fart.api.Transformer;
import net.minecraftforge.srgutils.IMappingFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static java.util.Objects.requireNonNull;

public class RenamerBuilder implements Builder {
    private final List<File> libraries = new ArrayList<>();
    private final List<ClassProvider> classProviders = new ArrayList<>();
    private final List<Transformer.Factory> transformerFactories = new ArrayList<>();
    private int threads = Runtime.getRuntime().availableProcessors();
    private boolean withJvmClasspath = false;
    private @NotNull Consumer<String> logger = System.out::println;
    private @NotNull Consumer<String> debug = s -> {};
    private boolean collectAbstractParams = true;

    @Override
    public @NotNull Builder lib(File value) {
        this.libraries.add(value);
        return this;
    }

    @Override
    public @NotNull Builder map(@NotNull File value) {
        try {
            add(Transformer.renamerFactory(IMappingFile.load(value), collectAbstractParams));
        } catch (IOException e) {
            throw new RuntimeException("Could not map file: " + value.getAbsolutePath(), e);
        }
        return this;
    }

    @Override
    public @NotNull Builder addClassProvider(ClassProvider classProvider) {
        this.classProviders.add(classProvider);
        return this;
    }

    @Override
    public @NotNull Builder withJvmClasspath() {
        // We use a property to ensure the JVM classpath is always added last
        this.withJvmClasspath = true;
        return this;
    }

    @Override
    public @NotNull Builder add(Transformer value) {
        this.transformerFactories.add(Transformer.Factory.always(requireNonNull(value, "value")));
        return this;
    }

    @Override
    public @NotNull Builder add(Transformer.Factory factory) {
        this.transformerFactories.add(requireNonNull(factory, "factory"));
        return this;
    }

    @Override
    public @NotNull Builder threads(int value) {
        this.threads = value;
        return this;
    }

    @Override
    public @NotNull Builder logger(Consumer<String> out) {
        this.logger = requireNonNull(out, "out");
        return this;
    }

    @Override
    public @NotNull Builder debug(Consumer<String> debug) {
        this.debug = requireNonNull(debug, "debug");
        return this;
    }

    @Override
    public @NotNull Builder setCollectAbstractParams(boolean collectAbstractParams) {
        this.collectAbstractParams = collectAbstractParams;
        return this;
    }

    @Override
    public @NotNull Renamer build() {
        List<ClassProvider> classProviders = new ArrayList<>(this.classProviders);
        if (this.withJvmClasspath)
            classProviders.add(ClassProvider.fromJvmClasspath());

        SortedClassProvider sortedClassProvider = new SortedClassProvider(classProviders, this.logger);
        final Transformer.Context ctx = new Transformer.Context() {
            @Override
            public @NotNull Consumer<String> getLog() {
                return logger;
            }

            @Override
            public @NotNull Consumer<String> getDebug() {
                return debug;
            }

            @Override
            public @NotNull ClassProvider getClassProvider() {
                return sortedClassProvider;
            }
        };

        final List<Transformer> transformers = new ArrayList<>(transformerFactories.size());
        for (Transformer.Factory factory : transformerFactories) {
            transformers.add(requireNonNull(factory.create(ctx), "output of " + factory));
        }
        return new RenamerImpl(libraries, transformers, sortedClassProvider, classProviders, threads, logger, debug);
    }
}
