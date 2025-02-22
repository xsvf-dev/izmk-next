package ovo.xsvf.izmk;

import ovo.xsvf.izmk.injection.mixin.MixinLoader;
import ovo.xsvf.izmk.injection.mixin.impl.MixinMinecraft;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.izmk.util.resources.ResourceUtil;
import ovo.xsvf.logging.Logger;

import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;

public class Entry {
    public static void entry(Instrumentation inst, int logPort, String jar, boolean devMode) throws Exception {
        ClassUtil.init(inst);
        IZMK.INSTANCE.setLogger(Logger.of("IZMK", logPort));
        IZMK.INSTANCE.setObfuscated(!devMode);
        ResourceUtil.INSTANCE.init(Paths.get(jar));

        MixinLoader.INSTANCE.loadMixin(MixinMinecraft.class);
        // NOTE: If you want to load other mixins, add them at MixinLoader#loadMixins()
    }
}
