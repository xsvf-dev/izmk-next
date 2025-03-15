package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.graphics.RenderSystem;
import ovo.xsvf.izmk.module.impl.MinmalBobbing;
import ovo.xsvf.izmk.module.impl.NoHurtcam;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.At;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.WrapInvoke;
import ovo.xsvf.patchify.api.Invocation;

@Patch(GameRenderer.class)
public class GameRendererPatch {
    private static final Logger log = LogManager.getLogger(GameRendererPatch.class);

    @Inject(method = "bobHurt", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public static void bobHurt(GameRenderer instance, PoseStack poseStack, float f, CallbackInfo callbackInfo) {
        callbackInfo.cancelled = NoHurtcam.INSTANCE.getEnabled();
    }

    @WrapInvoke(method = "renderLevel", desc = "(FJ)V", target = "net/minecraft/client/renderer/GameRenderer/bobView", targetDesc = "(Lcom/mojang/blaze3d/vertex/PoseStack;F)V")
    public static void renderLevelBobView(GameRenderer instance, float f, long finishTimeNano, Invocation original) throws Exception {
        if (MinmalBobbing.INSTANCE.getEnabled()) return;
        original.call();
    }

    @WrapInvoke(method = "render", desc = "(FJZ)V",
            target = "net/minecraft/client/gui/Gui/render",
            targetDesc = "(Lnet/minecraft/client/gui/GuiGraphics;F)V")
    public static void render(GameRenderer instance, float partialTicks, long finishTimeNano, boolean renderLevel, Invocation<Gui, Void> original) throws Exception {
        original.call();
        RenderSystem.INSTANCE.onRender2d((GuiGraphics) original.args().getFirst(), partialTicks);
    }
}
