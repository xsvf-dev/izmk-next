package ovo.xsvf.izmk;

import ovo.xsvf.izmk.injection.mixin.MixinLoader;
import ovo.xsvf.logging.Logger;
import ovo.xsvf.izmk.misc.ClassUtil;

import java.lang.instrument.Instrumentation;

public class Entry {
    public static void entry(Instrumentation inst, int logPort, String jar) throws Exception {
        ClassUtil.init(inst);
        IZMK.logger = Logger.of("IZMK", logPort);

        MixinLoader.loadMixins(inst.getAllLoadedClasses());
    }
}
