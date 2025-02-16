package ovo.xsvf.izmk;

import ovo.xsvf.izmk.injection.mixin.MixinLoader;
import ovo.xsvf.izmk.injection.mixin.impl.special.*;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.logging.Logger;

import java.lang.instrument.Instrumentation;

public class Entry {
    public static void entry(Instrumentation inst, int logPort, String jar) throws Exception {
        ClassUtil.init(inst);
        IZMK.logger = Logger.of("IZMK", logPort);

        MixinLoader.instance.loadMixin(MixinMinecraft.class);
        MixinLoader.instance.loadMixin(MixinGui.class);
    }
}
