package ovo.xsvf.izmk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.event.impl.EntryEvent;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.patchify.ASMUtil;
import ovo.xsvf.patchify.Mapping;
import ovo.xsvf.patchify.PatchLoader;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.asm.MethodWrapper;
import ovo.xsvf.patchify.asm.ReflectionUtil;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Entry {
    private static final Logger log = LogManager.getLogger(Entry.class);
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, boolean obfuscated,
                             Map<String, byte[]> classes, byte[] mapping) throws Throwable {
        log.info("Initializing IZMK...");
        ClassUtil.init(inst);
        IZMK.INSTANCE.setClasses(classes);
        if (obfuscated) {
            Mapping mapping0 = new Mapping(mapping);
            IZMK.INSTANCE.setObfuscated(true);
            PatchLoader.mapping = mapping0;
            MethodWrapper.mapping = mapping0;
            ReflectionUtil.mapping = mapping0;
            log.info("IZMK running in obfuscated mode");
        }

        IZMK.classes.entrySet().stream()
                .filter((entry) -> ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(entry.getValue()), Patch.class))
                .forEach(entry -> PATCHES.add(ReflectionUtil.forName(entry.getKey())));
        PatchLoader.INSTANCE.loadPatches(PATCHES, ClassUtil::getClassBytes, ClassUtil::redefineClass);
        log.info("Loaded {} patch, total classes: {}", PATCHES.size(), classes.size());

        new EntryEvent().post();
    }
}
