/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.fart.internal;

import net.minecraftforge.fart.api.Transformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class EntryImpl implements Transformer.Entry {
    private final String name;
    private final long time;
    private final byte[] data;

    protected EntryImpl(String name, long time, byte[] data) {
        this.name = name;
        this.time = time;
        this.data = data;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public long getTime() {
        return this.time;
    }

    @Override
    public byte[] getData() {
        return this.data;
    }

    public static class ClassEntry extends EntryImpl implements Transformer.ClassEntry {
        private static final String VERSION_PREFIX = "META-INF/versions/";
        private final int release;
        private final @NotNull String className;

        public ClassEntry(@NotNull String name, long time, byte[] data) {
            super(name, time, data);
            if (name.startsWith(VERSION_PREFIX)) {
                int start = VERSION_PREFIX.length();
                int idx = name.indexOf('/', start);
                if (idx == -1)
                    throw new IllegalArgumentException("Invalid versioned class entry: " + name);
                release = Integer.parseInt(name.substring(start, idx));
                name = name.substring(idx + 1);
            } else {
                release = -1;
            }
            className = name.substring(0, name.length() - 6);
        }

        @Override
        public Transformer.ClassEntry process(@NotNull Transformer transformer) {
            return transformer.process(this);
        }

        @Override
        public @NotNull String getClassName() {
            return className;
        }

        @Override
        public boolean isMultiRelease() {
            return release != -1;
        }

        @Override
        public int getVersion() {
            return release;
        }
    }

    public static class ResourceEntry extends EntryImpl implements Transformer.ResourceEntry {
        public ResourceEntry(String name, long time, byte[] data) {
            super(name, time, data);
        }

        @Override
        public Transformer.@Nullable ResourceEntry process(@NotNull Transformer transformer) {
            return transformer.process(this);
        }
    }

    public static class ManifestEntry extends EntryImpl implements Transformer.ManifestEntry {
        public ManifestEntry(long time, byte[] data) {
            super("META-INF/MANIFEST.MF", time, data);
        }

        @Override
        public Transformer.ManifestEntry process(@NotNull Transformer transformer) {
            return transformer.process(this);
        }
    }
}
