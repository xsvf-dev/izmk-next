package ovo.xsvf.izmk;

import malte0811.ferritecore.ModClientForge;
import net.minecraft.client.Minecraft;
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
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Entry {
    private static final Logger log = LogManager.getLogger(Entry.class);
    private static final List<Class<?>> PATCHES = new ArrayList<>();

    public static void entry(Instrumentation inst, String jar, boolean devMode, Map<String, byte[]> classes) throws Throwable {
        log.info("Initializing IZMK");
        log.info(Arrays.stream(Minecraft.class.getDeclaredMethods())
                .map(Method::getName).collect(Collectors.joining(", ")));
        ClassUtil.init(inst);
        IZMK.INSTANCE.setObfuscated(!devMode);
        ResourceUtil.INSTANCE.init(Paths.get(jar));

        ModClientForge.init();

        classes.entrySet().stream()
                .filter((entry) ->
                        ASMUtil.isVisibleAnnotationPresent(ASMUtil.node(entry.getValue()), Patch.class))
                .forEach(entry ->
                        PATCHES.add(ReflectionUtil.forName(entry.getKey())));
        new PatchLoader().loadPatches(PATCHES, ClassUtil::getClassBytes, ClassUtil::redefineClass);
        log.info("Loaded {} patch, total classes: {}", PATCHES.size(), classes.size());
        new EntryEvent().post();
    }
}
