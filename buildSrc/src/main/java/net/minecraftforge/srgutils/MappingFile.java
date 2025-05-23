/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.srgutils;

import net.minecraftforge.srgutils.InternalUtils.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.minecraftforge.srgutils.InternalUtils.Element.*;
import static net.minecraftforge.srgutils.InternalUtils.writeMeta;

class MappingFile implements IMappingFile {
    private @NotNull Map<String, Package> packages = new HashMap<>();
    private @NotNull Collection<Package> packagesView = Collections.unmodifiableCollection(packages.values());
    private @NotNull Map<String, Cls> classes = new HashMap<>();
    private @NotNull Collection<Cls> classesView = Collections.unmodifiableCollection(classes.values());
    private final Map<String, String> cache = new ConcurrentHashMap<>();
    static final Pattern DESC = Pattern.compile("L(?<cls>[^;]+);");

    MappingFile(){}
    MappingFile(@NotNull NamedMappingFile source, int from, int to) {
        source.getPackages().forEach(pkg -> addPackage(pkg.getName(from), pkg.getName(to), pkg.meta));
        source.getClasses().forEach(cls -> {
            Cls c = addClass(cls.getName(from), cls.getName(to), cls.meta);
            cls.getFields().forEach(fld -> c.addField(fld.getName(from), fld.getName(to), fld.getDescriptor(from), fld.meta));
            cls.getMethods().forEach(mtd -> {
                Cls.Method m = c.addMethod(mtd.getName(from), mtd.getDescriptor(from), mtd.getName(to), mtd.meta);
                mtd.getParameters().forEach(par -> m.addParameter(par.getIndex(), par.getName(from), par.getName(to), par.meta));
            });
        });
    }

    @Override
    public @NotNull Collection<Package> getPackages() {
        return this.packagesView;
    }

    @Override
    public Package getPackage(String original) {
        return packages.get(original);
    }

    private @Nullable Package addPackage(String original, String mapped, @NotNull Map<String, String> metadata) {
        return packages.put(original, new Package(original, mapped, metadata));
    }

    @Override
    public @NotNull Collection<Cls> getClasses() {
        return this.classesView;
    }

    @Override
    
    public Cls getClass(String original) {
        return classes.get(original);
    }

    private @NotNull Cls addClass(String original, String mapped, @NotNull Map<String, String> metadata) {
        return retPut(this.classes, original, new Cls(original, mapped, metadata));
    }

    @Override
    public String remapPackage(String pkg) {
        //TODO: Package bulk moves? Issue: moving default package will move EVERYTHING, it's what its meant to do but we shouldn't.
        Package ipkg = packages.get(pkg);
        return ipkg == null ? pkg : ipkg.getMapped();
    }

    @Override
    public String remapClass(@NotNull String cls) {
        String ret = cache.get(cls);
        if (ret == null) {
            Cls _cls = classes.get(cls);
            if (_cls == null) {
                int idx = cls.lastIndexOf('$');
                if (idx != -1)
                    ret = remapClass(cls.substring(0, idx)) + '$' + cls.substring(idx + 1);
                else
                    ret = cls;
            } else
                ret = _cls.getMapped();
            //TODO: Package bulk moves? Issue: moving default package will move EVERYTHING, it's what its meant to do but we shouldn't.
            cache.put(cls, ret);
        }
        return ret;
    }

    @Override
    public @NotNull String remapDescriptor(@NotNull String desc) {
        Matcher matcher = DESC.matcher(desc);
        StringBuffer buf = new StringBuffer();
        while (matcher.find())
            matcher.appendReplacement(buf, Matcher.quoteReplacement("L" + remapClass(matcher.group("cls")) + ";"));
        matcher.appendTail(buf);
        return buf.toString();
    }

    @Override
    public void write(@NotNull Path path, @NotNull Format format, boolean reversed) throws IOException {
        List<String> lines = new ArrayList<>();
        Comparator<INode> sort = reversed ? (a,b) -> a.getMapped().compareTo(b.getMapped()) : (a,b) -> a.getOriginal().compareTo(b.getOriginal());

        getPackages().stream().sorted(sort).forEachOrdered(pkg ->
            write(lines, format, reversed, PACKAGE, pkg)
        );
        getClasses().stream().sorted(sort).forEachOrdered(cls -> {
            write(lines, format, reversed, CLASS, cls);

            cls.getFields().stream().sorted(sort).forEachOrdered(fld ->
                write(lines, format, reversed, FIELD, fld)
            );

            cls.getMethods().stream().sorted(sort).forEachOrdered(mtd -> {
                write(lines, format, reversed, METHOD, mtd);

                mtd.getParameters().stream().sorted((a,b) -> a.getIndex() - b.getIndex()).forEachOrdered(par ->
                    write(lines, format, reversed, PARAMETER, par)
                );
            });
        });

        lines.removeIf(Objects::isNull);

        if (!format.isOrdered()) {
            Comparator<String> linesort = (format == Format.SRG || format == Format.XSRG) ? InternalUtils::compareLines : (o1, o2) -> o1.compareTo(o2);
            lines.sort(linesort);
        }

        switch (format) {
            case TINY1:
                lines.add(0, "v1\tleft\tright");
                break;
            case TINY:
                lines.add(0, "tiny\t2\t0\tleft\tright");
                break;
            case TSRG2:
                lines.add(0, "tsrg2 left right");
                break;
            default:
                break;
        }

        Files.createDirectories(path.getParent());
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            for (String line : lines) {
                writer.write(line);
                writer.write('\n');
            }
        }
    }

    private static void write(@NotNull List<String> lines, @NotNull Format format, boolean reversed, @NotNull Element element, @NotNull INode node) {
        String line = node.write(format, reversed);
        if (line != null) {
            lines.add(line);
            writeMeta(format, lines, element, node.getMetadata());
        }
    }

    @Override
    public @NotNull MappingFile reverse() {
        MappingFile ret = new MappingFile();
        getPackages().forEach(pkg -> ret.addPackage(pkg.getMapped(), pkg.getOriginal(), pkg.getMetadata()));
        getClasses().forEach(cls -> {
            Cls c = ret.addClass(cls.getMapped(), cls.getOriginal(), cls.getMetadata());
            cls.getFields().forEach(fld -> c.addField(fld.getMapped(), fld.getOriginal(), fld.getMappedDescriptor(), fld.getMetadata()));
            cls.getMethods().forEach(mtd -> {
                Cls.Method m = c.addMethod(mtd.getMapped(), mtd.getMappedDescriptor(), mtd.getOriginal(), mtd.getMetadata());
                mtd.getParameters().forEach(par -> m.addParameter(par.getIndex(), par.getMapped(), par.getOriginal(), par.getMetadata()));
            });
        });
        return ret;
    }

    @Override
    public @NotNull MappingFile rename(@NotNull IRenamer renamer) {
        MappingFile ret = new MappingFile();
        getPackages().forEach(pkg -> ret.addPackage(pkg.getOriginal(), renamer.rename(pkg), pkg.getMetadata()));
        getClasses().forEach(cls -> {
            Cls c = ret.addClass(cls.getOriginal(), renamer.rename(cls), cls.getMetadata());
            cls.getFields().forEach(fld -> c.addField(fld.getOriginal(), renamer.rename(fld), fld.getDescriptor(), fld.getMetadata()));
            cls.getMethods().forEach(mtd -> {
                Cls.Method m = c.addMethod(mtd.getOriginal(), mtd.getDescriptor(), renamer.rename(mtd), mtd.getMetadata());
                mtd.getParameters().forEach(par -> m.addParameter(par.getIndex(), par.getOriginal(), renamer.rename(par), par.getMetadata()));
            });
        });
        return ret;
    }

    @Override
    public @NotNull MappingFile chain(final @NotNull IMappingFile link) {
        return rename(new IRenamer() {
            public String rename(@NotNull IPackage value) {
                return link.remapPackage(value.getMapped());
            }

            public String rename(@NotNull IClass value) {
                return link.remapClass(value.getMapped());
            }

            public String rename(@NotNull IField value) {
                IClass cls = link.getClass(value.getParent().getMapped());
                return cls == null ? value.getMapped() : cls.remapField(value.getMapped());
            }

            public String rename(@NotNull IMethod value) {
                IClass cls = link.getClass(value.getParent().getMapped());
                return cls == null ? value.getMapped() : cls.remapMethod(value.getMapped(), value.getMappedDescriptor());
            }

            public String rename(@NotNull IParameter value) {
                IMethod mtd = value.getParent();
                IClass cls = link.getClass(mtd.getParent().getMapped());
                mtd = cls == null ? null : cls.getMethod(mtd.getMapped(), mtd.getMappedDescriptor());
                return mtd == null ? value.getMapped() : mtd.remapParameter(value.getIndex(), value.getMapped());
            }
        });
    }

    @Override
    public @NotNull MappingFile merge(@NotNull IMappingFile other) {
        MappingFile ret = new MappingFile();
        getPackages().forEach(pkg -> ret.addPackage(pkg.getOriginal(), pkg.getMapped(), pkg.getMetadata()));
        getClasses().forEach(cls -> copyClass(ret, cls));

        other.getPackages().forEach(pkg -> {
            Package existingPkg = ret.getPackage(pkg.getOriginal());
            if (existingPkg == null) {
                ret.addPackage(pkg.getOriginal(), pkg.getMapped(), pkg.getMetadata());
            } else {
                ret.addPackage(pkg.getOriginal(), existingPkg.getMapped(), mergeMetadata(existingPkg.getMetadata(), pkg.getMetadata()));
            }
        });
        other.getClasses().forEach(cls -> {
            Cls existingCls = ret.getClass(cls.getOriginal());
            if (existingCls == null) {
                copyClass(ret, cls);
                return;
            }

            Cls newCls = ret.addClass(cls.getOriginal(), existingCls.getMapped(), mergeMetadata(existingCls.getMetadata(), cls.getMetadata()));
            newCls.methods.putAll(existingCls.methods);
            newCls.fields.putAll(existingCls.fields);
            cls.getFields().forEach(fld -> {
                IField existingFld = existingCls.getField(fld.getOriginal());
                if (existingFld == null) {
                    newCls.addField(fld.getOriginal(), fld.getMapped(), fld.getDescriptor(), fld.getMetadata());
                } else {
                    newCls.addField(fld.getOriginal(), existingFld.getMapped(), existingFld.getDescriptor(), mergeMetadata(existingFld.getMetadata(), fld.getMetadata()));
                }
            });
            cls.getMethods().forEach(mtd -> {
                Cls.Method existingMtd = existingCls.getMethod(mtd.getOriginal(), mtd.getDescriptor());
                if (existingMtd == null) {
                    copyMethod(newCls, mtd);
                    return;
                }

                Cls.Method newMtd = newCls.addMethod(mtd.getOriginal(), existingMtd.getDescriptor(), existingMtd.getMapped(), mergeMetadata(existingMtd.getMetadata(), mtd.getMetadata()));
                newMtd.params.putAll(existingMtd.params);
                mtd.getParameters().forEach(par -> {
                    IParameter existingPar = existingMtd.getParameter(par.getIndex());
                    if (existingPar == null) {
                        newMtd.addParameter(par.getIndex(), par.getOriginal(), par.getMapped(), par.getMetadata());
                    } else {
                        newMtd.addParameter(par.getIndex(), par.getOriginal(), existingPar.getMapped(), mergeMetadata(existingPar.getMetadata(), par.getMetadata()));
                    }
                });
            });
        });

        return ret;
    }

    private static void copyClass(@NotNull MappingFile ret, @NotNull IClass cls) {
        Cls c = ret.addClass(cls.getOriginal(), cls.getMapped(), cls.getMetadata());
        cls.getFields().forEach(fld -> c.addField(fld.getOriginal(), fld.getMapped(), fld.getDescriptor(), fld.getMetadata()));
        cls.getMethods().forEach(mtd -> copyMethod(c, mtd));
    }

    private static void copyMethod(@NotNull Cls c, @NotNull IMethod mtd) {
        Cls.Method m = c.addMethod(mtd.getOriginal(), mtd.getDescriptor(), mtd.getMapped(), mtd.getMetadata());
        mtd.getParameters().forEach(par -> m.addParameter(par.getIndex(), par.getOriginal(), par.getMapped(), par.getMetadata()));
    }

    private static @NotNull Map<String, String> mergeMetadata(@NotNull Map<String, String> base, @NotNull Map<String, String> extra) {
        Map<String, String> merged = new HashMap<>(base);

        for (Map.Entry<String, String> entry : extra.entrySet()) {
            if (!merged.containsKey(entry.getKey())) {
                merged.put(entry.getKey(), entry.getValue());
            }
        }

        return merged;
    }

    abstract class Node implements INode {
        private final String original;
        private final String mapped;
        private final @NotNull Map<String, String> metadata;

        protected Node(String original, String mapped, @NotNull Map<String, String> metadata) {
            this.original = original;
            this.mapped = mapped;
            this.metadata = metadata.isEmpty() ? Collections.emptyMap() : Collections.unmodifiableMap(metadata);
        }

        @Override
        public String getOriginal() {
            return this.original;
        }

        @Override
        public String getMapped() {
            return this.mapped;
        }

        @Override
        public @NotNull Map<String, String> getMetadata() {
            return this.metadata;
        }
    }

    class Package extends Node implements IPackage {
        protected Package(String original, String mapped, @NotNull Map<String, String> metadata) {
            super(original, mapped, metadata);
        }

        @Override
        
        public @Nullable String write(@NotNull Format format, boolean reversed) {
            String sorig = getOriginal().isEmpty() ? "." : getOriginal();
            String smap = getMapped().isEmpty() ? "." : getMapped();

            if (reversed) {
                String tmp = sorig;
                sorig = smap;
                smap = tmp;
            }

            switch (format) {
                case SRG:
                case XSRG: return "PK: " + sorig + ' ' + smap;
                case CSRG:
                case TSRG:
                case TSRG2: return getOriginal() + "/ " + getMapped() + '/';
                case PG:
                case TINY1: return null;
                default: throw new UnsupportedOperationException("Unknown format: " + format);
            }
        }

        @Override
        public @Nullable String toString() {
            return this.write(Format.SRG, false);
        }
    }

    class Cls extends Node implements IClass {
        private @NotNull Map<String, Field> fields = new HashMap<>();
        private @NotNull Collection<Field> fieldsView = Collections.unmodifiableCollection(fields.values());
        private @NotNull Map<String, Method> methods = new HashMap<>();
        private @NotNull Collection<Method> methodsView = Collections.unmodifiableCollection(methods.values());

        protected Cls(String original, String mapped, @NotNull Map<String, String> metadata) {
            super(original, mapped, metadata);
        }

        @Override
        
        public @NotNull String write(@NotNull Format format, boolean reversed) {
            String oName = !reversed ? getOriginal() : getMapped();
            String mName = !reversed ? getMapped() : getOriginal();
            switch (format) {
                case SRG:
                case XSRG: return "CL: " + oName + ' ' + mName;
                case CSRG:
                case TSRG:
                case TSRG2: return oName + ' ' + mName;
                case PG: return oName.replace('/', '.') + " -> " + mName.replace('/', '.') + ':';
                case TINY1: return "CLASS\t" + oName + '\t' + mName;
                case TINY:  return "c\t" + oName + '\t' + mName;
                default: throw new UnsupportedOperationException("Unknown format: " + format);
            }
        }

        @Override
        public @NotNull Collection<Field> getFields() {
            return this.fieldsView;
        }

        @Override
        
        public IField getField(String name) {
            return this.fields.get(name);
        }

        @Override
        public String remapField(String field) {
            Field fld = fields.get(field);
            return fld  == null ? field : fld.getMapped();
        }

        private @NotNull Field addField(String original, String mapped, String desc, @NotNull Map<String, String> metadata) {
            return retPut(this.fields, original, new Field(original, mapped, desc, metadata));
        }

        @Override
        public @NotNull Collection<Method> getMethods() {
            return this.methodsView;
        }

        @Override
        
        public Method getMethod(String name, String desc) {
            return this.methods.get(name + desc);
        }

        private @NotNull Method addMethod(String original, String desc, String mapped, @NotNull Map<String, String> metadata) {
            return retPut(this.methods, original + desc, new Method(original, desc, mapped, metadata));
        }

        @Override
        public String remapMethod(String name, String desc) {
            Method mtd = methods.get(name + desc);
            return mtd == null ? name : mtd.getMapped();
        }

        @Override
        public @NotNull String toString() {
            return this.write(Format.SRG, false);
        }

        class Field extends Node implements IField {
            private final String desc;

            private Field(String original, String mapped, String desc, @NotNull Map<String, String> metadata) {
                super(original, mapped, metadata);
                this.desc = desc;
            }

            @Override
            public String getDescriptor() {
                return desc;
            }

            @Override
            public @Nullable String getMappedDescriptor() {
                return this.desc == null ? null : MappingFile.this.remapDescriptor(this.desc);
            }

            @Override
            
            public @NotNull String write(@NotNull Format format, boolean reversed) {
                if (format != Format.TSRG2 && format.hasFieldTypes() && this.desc == null)
                    throw new IllegalStateException("Can not write " + format.name() + " format, field is missing descriptor");

                String oOwner = !reversed ? Cls.this.getOriginal() : Cls.this.getMapped();
                String mOwner = !reversed ? Cls.this.getMapped() : Cls.this.getOriginal();
                String oName = !reversed ? this.getOriginal() : this.getMapped();
                String mName = !reversed ? this.getMapped() : this.getOriginal();
                String oDesc = !reversed ? this.getDescriptor() : this.getMappedDescriptor();
                String mDesc = !reversed ? this.getMappedDescriptor() : this.getDescriptor();

                switch (format) {
                    case SRG:  return "FD: " + oOwner+ '/' + oName + ' ' + mOwner + '/' + mName + (oDesc == null ? "" : " # " + oDesc + " " + mDesc);
                    case XSRG: return "FD: " + oOwner + '/' + oName + (oDesc == null ? "" : ' ' + oDesc) + ' ' + mOwner + '/' + mName + (mDesc == null ? "" : ' ' + mDesc);
                    case CSRG: return oOwner + ' ' + oName + ' ' + mName;
                    case TSRG: return '\t' + oName + ' ' + mName;
                    case TSRG2: return '\t' + oName + (oDesc == null ? "" : ' ' + oDesc) + ' ' + mName;
                    case PG:   return "    " + InternalUtils.toSource(oDesc) + ' ' + oName + " -> " + mName;
                    case TINY1: return "FIELD\t" + oOwner + '\t' + oDesc + '\t' + oName + '\t' + mName;
                    case TINY: return "\tf\t" + oDesc + '\t' + oName + '\t' + mName;
                    default: throw new UnsupportedOperationException("Unknown format: " + format);
                }
            }

            @Override
            public @NotNull String toString() {
                return this.write(Format.SRG, false);
            }

            @Override
            public @NotNull Cls getParent() {
                return Cls.this;
            }
        }

        class Method extends Node implements IMethod {
            private final String desc;
            private final Map<Integer, Parameter> params = new HashMap<>();
            private final Collection<Parameter> paramsView = Collections.unmodifiableCollection(params.values());

            private Method(String original, String desc, String mapped, @NotNull Map<String, String> metadata) {
                super(original, mapped, metadata);
                this.desc = desc;
            }

            @Override
            public String getDescriptor() {
                return this.desc;
            }
            @Override
            public @NotNull String getMappedDescriptor() {
                return MappingFile.this.remapDescriptor(this.desc);
            }

            @Override
            public Collection<Parameter> getParameters() {
                return this.paramsView;
            }

            private @NotNull Parameter addParameter(int index, String original, String mapped, @NotNull Map<String, String> metadata) {
                return retPut(this.params, index, new Parameter(index, original, mapped, metadata));
            }

            
            @Override
            public IParameter getParameter(int index) {
                return this.params.get(index);
            }

            @Override
            public String remapParameter(int index, String name) {
                Parameter param = this.params.get(index);
                return param == null ? name : param.getMapped();
            }

            @Override
            public @NotNull String write(@NotNull Format format, boolean reversed) {
                String oName = !reversed ? getOriginal() : getMapped();
                String mName = !reversed ? getMapped() : getOriginal();
                String oOwner = !reversed ? Cls.this.getOriginal() : Cls.this.getMapped();
                String mOwner = !reversed ? Cls.this.getMapped() : Cls.this.getOriginal();
                String oDesc = !reversed ? getDescriptor() : getMappedDescriptor();
                String mDesc = !reversed ? getMappedDescriptor() : getDescriptor();

                switch (format) {
                    case SRG:
                    case XSRG: return "MD: " + oOwner + '/' + oName + ' ' + oDesc + ' ' + mOwner + '/' + mName + ' ' + mDesc;
                    case CSRG: return oOwner + ' ' + oName + ' ' + oDesc + ' ' + mName;
                    case TSRG:
                    case TSRG2: return '\t' + oName + ' ' + oDesc + ' ' + mName;
                    case TINY1: return "METHOD\t" + oOwner + '\t' + oDesc + '\t' + oName + '\t' + mName;
                    case TINY: return "\tm\t" + oDesc + '\t' + oName + '\t' + mName;
                    case PG:
                        int start = Integer.parseInt(getMetadata().getOrDefault("start_line", "0"));
                        int end = Integer.parseInt(getMetadata().getOrDefault("end_line", "0"));
                        return "    " + (start == 0 && end == 0 ? "" : start + ":" + end + ":") + InternalUtils.toSource(oName, oDesc) + " -> " + mName;
                    default: throw new UnsupportedOperationException("Unknown format: " + format);
                }
            }

            @Override
            public @NotNull String toString() {
                return this.write(Format.SRG, false);
            }

            @Override
            public @NotNull Cls getParent() {
                return Cls.this;
            }

            class Parameter extends Node implements IParameter {
                private final int index;
                protected Parameter(int index, String original, String mapped, @NotNull Map<String, String> metadata) {
                    super(original, mapped, metadata);
                    this.index = index;
                }
                @Override
                public @NotNull IMethod getParent() {
                    return Method.this;
                }
                @Override
                public int getIndex() {
                    return this.index;
                }
                @Override
                public @Nullable String write(@NotNull Format format, boolean reversed) {
                    String oName = !reversed ? getOriginal() : getMapped();
                    String mName = !reversed ? getMapped() : getOriginal();
                    switch (format) {
                        case SRG:
                        case XSRG:
                        case CSRG:
                        case TSRG:
                        case PG:
                        case TINY1: return null;
                        case TINY: return "\t\tp\t" + getIndex() + '\t' + oName + '\t' + mName;
                        case TSRG2: return "\t\t" + getIndex() + ' ' + oName + ' ' + mName;
                        default: throw new UnsupportedOperationException("Unknown format: " + format);
                    }
                }

            }
        }
    }

    private static <K, V> V retPut(@NotNull Map<K, V> map, K key, V value) {
        map.put(key, value);
        return value;
    }
}
