package ovo.xsvf.izmk;

import ovo.xsvf.izmk.injection.mixin.MixinLoader;
import ovo.xsvf.izmk.injection.mixin.impl.MixinChatComponent;
import ovo.xsvf.izmk.injection.mixin.impl.MixinGui;
import ovo.xsvf.izmk.injection.mixin.impl.MixinMinecraft;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.izmk.util.resources.ResourceUtil;
import ovo.xsvf.logging.Logger;

import java.lang.instrument.Instrumentation;
import java.nio.file.Paths;

public class Entry {
    public static void entry(Instrumentation inst, int logPort, String jar, boolean devMode) throws Exception {
        ClassUtil.init(inst);
        IZMK.INSTANCE.setLogger(Logger.Companion.of("IZMK", logPort));
        IZMK.INSTANCE.setObfuscated(!devMode);
        ResourceUtil.INSTANCE.init(Paths.get(jar));

        MixinLoader.INSTANCE.loadMixin(MixinMinecraft.class);
        MixinLoader.INSTANCE.loadMixin(MixinGui.class);
        MixinLoader.INSTANCE.loadMixin(MixinChatComponent.class);
    }
}
