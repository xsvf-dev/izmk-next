package ovo.xsvf.izmk;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.injection.mixin.MixinLoader;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.logging.Logger;

import java.util.ArrayList;
import java.util.List;

public class IZMK {
    public static Logger logger;
    public static final List<Class<?>> excludedLoading = new ArrayList<>();
    public static Minecraft mc;
    public static boolean runHeypixel = false;

    public static void init() throws Exception {
        logger.info("Start initializing IZMK...");
        MixinLoader.loadMixins(ClassUtil.getIns().getAllLoadedClasses());
    }
}
