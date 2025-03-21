package ovo.xsvf.izmk.injection.patch;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.graphics.RenderSystem;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Local;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(GameRenderer.class)
public class GameRendererPatch {
    private static final Logger log = LogManager.getLogger(GameRendererPatch.class);

    @Inject(method = "render", desc = "(FJZ)V",
            at = @At(value = At.Type.AFTER_INVOKE,
                    method = "net/minecraft/client/gui/Gui/render",
                    desc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    public static void render(GameRenderer instance, float partialTicks, long finishTimeNano,
                              boolean renderLevel, @Local(10) GuiGraphics guiGraphics,
                              CallbackInfo ci) throws Exception {
        RenderSystem.INSTANCE.onRender2d(guiGraphics, partialTicks);
    }
}
