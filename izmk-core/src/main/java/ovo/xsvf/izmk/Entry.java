package ovo.xsvf.izmk;

import malte0811.ferritecore.ModClientForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.BMWClassLoader;
import ovo.xsvf.izmk.event.impl.EntryEvent;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.izmk.resource.ResourceUtil;
import ovo.xsvf.patchify.ASMUtil;
import ovo.xsvf.patchify.PatchLoader;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.asm.ReflectionUtil;

import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Entry {
    private static final Logger log = LogManager.getLogger(Entry.class);
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, String jar, boolean devMode) throws Throwable {
        ClassUtil.init(inst);
        IZMK.INSTANCE.setObfuscated(!devMode);
        ResourceUtil.INSTANCE.init(Paths.get(jar));

        ModClientForge.init();

        BMWClassLoader.getClasses().stream()
                .filter((entry) ->
                        ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(entry.getValue()), Patch.class))
                .forEach(entry ->
                        PATCHES.add(ReflectionUtil.forName(entry.getKey().replace("/", "."))));
        log.info("Loaded {} patch", PATCHES.size());
        new PatchLoader().loadPatches(PATCHES, ClassUtil::getClassBytes, ClassUtil::redefineClass);

        new EntryEvent().post();
    }
}
