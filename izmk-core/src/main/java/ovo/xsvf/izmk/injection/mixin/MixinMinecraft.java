package ovo.xsvf.izmk.injection.mixin;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.event.impl.TickEvents;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

/**
 * @author xsvf
 */
@Patch(Minecraft.class)
public class MixinMinecraft {
    private static boolean initialized = false;

    @Inject(method = "tick", desc = "()V")
    public static void onTickPre(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        if (!initialized) {
            IZMK.INSTANCE.setMc(Minecraft.getInstance());
            IZMK.INSTANCE.init();
            initialized = true;
        }

        TickEvents.Pre.INSTANCE.post();
    }

    @Inject(method = "tick", desc = "()V", at = @At(At.Type.TAIL))
    public static void onTickPost(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        if (!initialized) {
            IZMK.INSTANCE.setMc(Minecraft.getInstance());
            IZMK.INSTANCE.init();
            initialized = true;
        }

        TickEvents.Post.INSTANCE.post();
    }

    @Inject(method = "destroy",desc = "()V")
    public static void destroy(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        IZMK.INSTANCE.shutdown();
    }
}
