package ovo.xsvf.izmk.injection.mixin.impl;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.misc.Constants;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(Minecraft.class)
public class MixinMinecraft implements Constants {
    @Inject(method = "tick", desc = "()V")
    public static void tick(Minecraft minecraft, CallbackInfo callbackInfo) {

    }
}
