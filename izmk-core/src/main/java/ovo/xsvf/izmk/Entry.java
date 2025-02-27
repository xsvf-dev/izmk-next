package ovo.xsvf.izmk;

import malte0811.ferritecore.ModClientForge;
import ovo.xsvf.BMWClassLoader;
import ovo.xsvf.izmk.event.impl.EntryEvent;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.izmk.resource.ResourceUtil;
import ovo.xsvf.logging.Logger;
import ovo.xsvf.patchify.ASMUtil;
import ovo.xsvf.patchify.PatchLoader;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.asm.ReflectionUtil;

import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Entry {
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, int logPort, String jar, boolean devMode) throws Throwable {
        ClassUtil.init(inst);
        IZMK.INSTANCE.setLogger(Logger.of("IZMK", logPort));
        IZMK.INSTANCE.setObfuscated(!devMode);
        ResourceUtil.INSTANCE.init(Paths.get(jar));

        ModClientForge.init();

        BMWClassLoader.getClasses().stream()
                .filter((entry) ->
                        ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(entry.getValue()), Patch.class))
                .forEach(entry ->
                        PATCHES.add(ReflectionUtil.forName(entry.getKey().replace("/", "."))));
        IZMK.INSTANCE.getLogger().info("Loaded {} patches", PATCHES.size());
        new PatchLoader(IZMK.INSTANCE.getLogger()::debug, IZMK.INSTANCE.getLogger()::info, IZMK.INSTANCE.getLogger()::warn)
                .loadPatches(PATCHES, ClassUtil::getClassBytes, ClassUtil::redefineClass);

        new EntryEvent().post();
    }
}
