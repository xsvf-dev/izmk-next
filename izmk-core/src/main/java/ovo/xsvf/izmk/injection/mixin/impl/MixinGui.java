package ovo.xsvf.izmk.injection.mixin.impl;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import ovo.xsvf.izmk.graphics.RenderSystem;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(Gui.class)
public class MixinGui {
    @Inject(method = "render", desc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V")
    public static void renderGui(GuiGraphics pGuiGraphics, float pPartialTick, CallbackInfo callbackInfo) {
        RenderSystem.INSTANCE.onRender2d(pGuiGraphics, pPartialTick);
    }
}
