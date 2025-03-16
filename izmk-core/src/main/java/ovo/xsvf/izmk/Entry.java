package ovo.xsvf.izmk;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.event.impl.EntryEvent;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.patchify.ASMUtil;
import ovo.xsvf.patchify.PatchLoader;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.asm.ReflectionUtil;

import java.io.InputStream;
import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Entry {
    private static final Logger log = LogManager.getLogger(Entry.class);
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, String jar, boolean devMode,
                             Map<String, byte[]> classes, byte[] mapping) throws Throwable {
        log.info("Initializing IZMK...");
        ClassUtil.init(inst);
        IZMK.INSTANCE.setObfuscated(!devMode);
        IZMK.INSTANCE.setClasses(classes);
        
        InputStream resource = Entry.class.getResourceAsStream("/assets/izmk/text.txt");
        System.out.println(resource == null ? "Resource is null" : "Resource is not null, length: " + resource.available());
        if (resource != null) {
            resource.close();
        }

        IZMK.classes.entrySet().stream()
                .filter((entry) ->
                        ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(entry.getValue()), Patch.class))
                .forEach(entry ->
                        PATCHES.add(ReflectionUtil.forName(entry.getKey())));
        new PatchLoader().loadPatches(PATCHES, ClassUtil::getClassBytes, ClassUtil::redefineClass);
        log.info("Loaded {} patch, total classes: {}", PATCHES.size(), classes.size());
        new EntryEvent().post();
    }
}
