package ovo.xsvf.izmk.injection.mixin.impl;

import net.minecraft.SharedConstants;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

/**
 * Skidded from <a href="https://github.com/astei/lazydfu">LazyDFU</a>
 */
@Mixin(SharedConstants.class)
public class MixinSharedConstants {
    @Inject(method = "enableDataFixerOptimizations", desc = "()V")
    public static void enableDataFixerOptimizations(CallbackInfo ci) {
        ci.cancelled = true;
    }
}
