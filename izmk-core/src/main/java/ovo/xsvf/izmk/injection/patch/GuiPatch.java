package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import ovo.xsvf.izmk.graphics.RenderSystem;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(Gui.class)
public class GuiPatch {
    @Inject(method = "render", desc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V", at = @At(At.Type.TAIL))
    public static void renderGui(GuiGraphics pGuiGraphics, float pPartialTick, CallbackInfo callbackInfo) {
        RenderSystem.INSTANCE.onRender2d(pPartialTick);
    }
}
