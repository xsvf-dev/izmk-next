/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.srgutils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IMappingBuilder {
    static @NotNull IMappingBuilder create(String @Nullable ... names) {
        return new NamedMappingFile(names == null || names.length == 0 ? new String[] {"left", "right"} : names);
    }

    IPackage addPackage(String... names);
    IClass addClass(String... names);

    INamedMappingFile build();

    public interface IPackage {
        IPackage meta(String key, String value);
        IMappingBuilder build();
    }

    public interface IClass {
        IField field(String... names);
        IMethod method(String descriptor, String... names);
        IClass meta(String key, String value);
        IMappingBuilder build();
    }

    public interface IField {
        IField descriptor(String value);
        IField meta(String key, String value);
        IClass build();
    }

    public interface IMethod {
        IParameter parameter(int index, String... names);
        IMethod meta(String key, String value);
        IClass build();
    }

    public interface IParameter {
        IParameter meta(String key, String value);
        IMethod build();
    }
}
