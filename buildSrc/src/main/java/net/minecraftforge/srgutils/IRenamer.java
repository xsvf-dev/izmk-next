/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.srgutils;

import net.minecraftforge.srgutils.IMappingFile.*;
import org.jetbrains.annotations.NotNull;

public interface IRenamer {
    default String rename(@NotNull IPackage value) {
        return value.getMapped();
    }

    default String rename(@NotNull IClass value) {
        return value.getMapped();
    }

    default String rename(@NotNull IField value) {
        return value.getMapped();
    }

    default String rename(@NotNull IMethod value) {
        return value.getMapped();
    }

    default String rename(@NotNull IParameter value) {
        return value.getMapped();
    }
}
