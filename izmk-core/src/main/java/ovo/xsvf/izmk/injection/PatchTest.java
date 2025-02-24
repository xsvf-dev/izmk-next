package ovo.xsvf.izmk.injection;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.WrapInvoke;
import ovo.xsvf.patchify.api.Invocation;

@Patch(Minecraft.class)
public class PatchTest {
    @WrapInvoke(method = "tick", desc = "()V",
             target = "net/minecraftforge/event/ForgeEventFactory/onPreClientTick", targetDesc = "()V")
    public static void tick(Minecraft mc, Invocation ci) {
        IZMK.logger.info("PatchTest: tick() called");
    }
}
