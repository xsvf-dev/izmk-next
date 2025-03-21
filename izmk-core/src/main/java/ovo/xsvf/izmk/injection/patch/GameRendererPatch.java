package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.event.impl.ResolutionUpdateEvent;
import ovo.xsvf.izmk.graphics.RenderSystem;
import ovo.xsvf.izmk.module.impl.MinmalBobbing;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.*;
import ovo.xsvf.patchify.api.Invocation;

@Patch(GameRenderer.class)
public class GameRendererPatch {
    private static final Logger log = LogManager.getLogger(GameRendererPatch.class);

    @WrapInvoke(method = "renderLevel", desc = "(FJLcom/mojang/blaze3d/vertex/PoseStack;)V",
            target = "net/minecraft/client/renderer/GameRenderer/bobView",
            targetDesc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public static void renderLevelBobView(GameRenderer instance, float f, long finishTimeNano,
                                          PoseStack poseStack, Invocation<GameRenderer, Void> original) throws Exception {
        if (MinmalBobbing.INSTANCE.getEnabled()) return;
        original.call();
    }

    @Inject(method = "render", desc = "(FJZ)V",
            at = @At(value = At.Type.AFTER_INVOKE,
                    method = "net/minecraft/client/gui/Gui/render",
                    desc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V"))
    public static void render(GameRenderer instance, float partialTicks, long finishTimeNano,
                              boolean renderLevel, @Local(10) GuiGraphics guiGraphics,
                              CallbackInfo ci) throws Exception {
        RenderSystem.INSTANCE.onRender2d(guiGraphics, partialTicks);
    }

    @Inject(method = "resize", desc = "(II)V",
            at = @At(value = At.Type.HEAD))
    public static void onResize(GameRenderer instance, int width, int height, CallbackInfo ci) {
        new ResolutionUpdateEvent(width, height).post();
    }
}
