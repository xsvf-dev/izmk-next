package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(Minecraft.class)
public class PatchTest {
    @Inject(method = "tick", desc = "()V")
    public static void tick(Minecraft mc, CallbackInfo ci) {
        IZMK.logger.info("PatchTest: tick() called");
    }
}
