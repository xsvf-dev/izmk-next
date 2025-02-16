package ovo.xsvf.izmk.injection.mixin.impl.special;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import ovo.xsvf.izmk.graphics.GRenderSystem;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.*;

@Mixin(Gui.class)
public class MixinGui {

    @Inject(method = "render", desc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V")
    public static void renderGui(GuiGraphics pGuiGraphics, float pPartialTick, CallbackInfo callbackInfo) {
        GRenderSystem.getInstance().onRender2D(pGuiGraphics, pPartialTick);
        GlStateManager._enableBlend();
        GlStateManager._disableCull();
        GlStateManager._disableDepthTest();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

}
