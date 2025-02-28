package ovo.xsvf.izmk.injection.mixin;

import net.minecraft.SharedConstants;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

/**
 * Skidded from <a href="https://github.com/astei/lazydfu">LazyDFU</a>
 */
@Patch(SharedConstants.class)
public class SharedConstantsPatch {
    @Inject(method = "enableDataFixerOptimizations", desc = "()V")
    public static void enableDataFixerOptimizations(CallbackInfo ci) {
        ci.cancelled = true;
    }
}
