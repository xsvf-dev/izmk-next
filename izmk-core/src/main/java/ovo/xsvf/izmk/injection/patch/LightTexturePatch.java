package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
import ovo.xsvf.izmk.module.impl.FullBright;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.WrapInvoke;
import ovo.xsvf.patchify.api.Invocation;

@Patch(LightTexture.class)
public class LightTexturePatch {
    @WrapInvoke(method = "updateLightTexture", desc = "(F)V",
            target = "net/minecraft/client/OptionInstance/get", targetDesc = "()Ljava/lang/Object;")
    public static Object updateLightTexture(LightTexture instance, float partialTicks, Invocation<OptionInstance<Double>, Double> ci) throws Exception {
        if (FullBright.INSTANCE.getEnabled()) {
            return FullBright.INSTANCE.getGamma();
        } else {
            return ci.call();
        }
    }
}
