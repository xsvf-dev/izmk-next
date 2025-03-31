/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.fart.internal;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class Util {
    public static void forZip(@NotNull ZipFile zip, @NotNull IOConsumer<ZipEntry> consumer) throws IOException {
        for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
            consumer.accept(entries.nextElement());
        }
    }

    @FunctionalInterface
    public static interface IOConsumer<T> {
        public void accept(T value) throws IOException;
    }

    public static byte[] toByteArray(@NotNull InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    public static void copy(@NotNull InputStream input, @NotNull OutputStream output) throws IOException {
        byte[] buf = new byte[0x100];
        int cnt = 0;
        while ((cnt = input.read(buf, 0, buf.length)) != -1) {
            output.write(buf, 0, cnt);
        }
    }

    public static String nameToBytecode(@Nullable Class<?> cls) {
        return cls == null ? null : cls.getName().replace('.', '/');
    }
    public static String nameToBytecode(@Nullable String cls) {
        return cls == null ? null : cls.replace('.', '/');
    }
}
