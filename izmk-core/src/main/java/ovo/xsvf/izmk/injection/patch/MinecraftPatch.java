package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.event.impl.PostTickEvent;
import ovo.xsvf.izmk.event.impl.PreTickEvent;
import ovo.xsvf.izmk.event.impl.ShutdownEvent;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(Minecraft.class)
public class MinecraftPatch {
    private static final Logger log = LogManager.getLogger(MinecraftPatch.class);
    private static volatile boolean initialized = false;

    @Inject(method = "tick", desc = "()V")
    public static void onTickPre(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        if (!initialized) {
            synchronized (MinecraftPatch.class) {
                if (!initialized) {
                    IZMK.INSTANCE.setMc(Minecraft.getInstance());
                    IZMK.INSTANCE.init();
                    initialized = true;
                }
            }
        }

        new PreTickEvent().post();
    }

    @Inject(method = "tick", desc = "()V", at = @At(At.Type.TAIL))
    public static void onTickPost(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        new PostTickEvent().post();
    }

    @Inject(method = "destroy",desc = "()V")
    public static void destroy(Minecraft minecraft, CallbackInfo callbackInfo) throws Throwable {
        new ShutdownEvent().post();
    }
}
