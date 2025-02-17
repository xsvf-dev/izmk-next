package ovo.xsvf.izmk.injection.mixin.impl.special;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.event.EventBus;
import ovo.xsvf.izmk.event.impl.TickEvent;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

/**
 * @author xsvf
 */
@Mixin(Minecraft.class)
public class MixinMinecraft {
    private static boolean initialized = false;

    @Inject(method = "tick", desc = "()V")
    public static void tick(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        if (initialized) return;
        IZMK.INSTANCE.setMc(Minecraft.getInstance());
        IZMK.INSTANCE.init();
        initialized = true;

        EventBus.INSTANCE.call(new TickEvent());
    }

    @Inject(method = "destroy",desc = "()V")
    public static void destroy(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        IZMK.INSTANCE.shutdown();
    }
}
