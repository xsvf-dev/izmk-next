package ovo.xsvf.izmk;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.injection.mixin.MixinLoader;
import ovo.xsvf.izmk.injection.mixin.impl.special.MixinMinecraft;
import ovo.xsvf.izmk.misc.ClassUtil;
import ovo.xsvf.izmk.module.ModuleManager;
import ovo.xsvf.logging.Logger;

import java.util.List;

public class IZMK {
    public static Logger logger;
    public static final List<Class<?>> excludedLoading = List.of(MixinMinecraft.class);
    public static Minecraft mc;
    public static boolean runHeypixel = false;

    public static void init() throws Throwable{
        logger.info("Start initializing IZMK...");

        Class<?>[] allLoadedClasses = ClassUtil.getIns().getAllLoadedClasses();
        MixinLoader.loadMixins(allLoadedClasses);
        ModuleManager.getInstance().init(allLoadedClasses);
    }
}
