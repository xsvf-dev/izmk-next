package ovo.xsvf.izmk;

import ovo.xsvf.izmk.injection.PatchTest;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.logging.Logger;
import ovo.xsvf.patchify.PatchLoader;
import ovo.xsvf.patchify.api.IPatchLoader;

import java.lang.instrument.Instrumentation;
import java.util.List;

public class Entry {
    public static void entry(Instrumentation inst, int logPort, String jar, boolean devMode) throws Exception {
        ClassUtil.init(inst);
        IZMK.INSTANCE.setLogger(Logger.of("IZMK", logPort));
        IZMK.INSTANCE.setObfuscated(!devMode);
//        ResourceUtil.INSTANCE.init(Paths.get(jar));
//        ModClientForge.init();

        IPatchLoader patchLoader = new PatchLoader();
        patchLoader.loadPatches(
                List.of(
                        PatchTest.class
                ),
                ClassUtil::getClassBytes,
                ClassUtil::redefineClass
        );
    }
}
