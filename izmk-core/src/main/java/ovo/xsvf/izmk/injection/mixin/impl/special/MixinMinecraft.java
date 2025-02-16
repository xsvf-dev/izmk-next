package ovo.xsvf.izmk.injection.mixin.impl.special;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;
import ovo.xsvf.izmk.misc.Constants;

/**
 * @author xsvf
 */
@Mixin(Minecraft.class)
public class MixinMinecraft implements Constants {
    private static boolean initialized = false;

    @Inject(method = "tick", desc = "()V")
    public static void tick(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        if (initialized) return;
        IZMK.mc = Minecraft.getInstance();
        IZMK.init();
        initialized = true;
    }
}
