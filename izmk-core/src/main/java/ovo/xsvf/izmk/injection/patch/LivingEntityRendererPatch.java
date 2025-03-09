package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.injection.accessor.LivingEntityRendererAccessor;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.WrapInvoke;
import ovo.xsvf.patchify.api.Invocation;

import java.util.List;

@SuppressWarnings("rawtypes, unused")
@Patch(LivingEntityRenderer.class)
public class LivingEntityRendererPatch {
    private static final Logger log = LogManager.getLogger(LivingEntityRendererPatch.class);

    @WrapInvoke(method = "render", desc = "(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
                target = "net/minecraft/client/renderer/entity/layers/RenderLayer/render", targetDesc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V")
    public static void renderWithOverlay(LivingEntityRenderer<?, ?> instance, LivingEntity entity, float yaw, float partialTicks,
                                         PoseStack poseStack, MultiBufferSource vertexConsumers, int light,
                                         Invocation<RenderLayer<?, ?>, Void> invocation) throws Exception {
        if (invocation.instance() instanceof HumanoidArmorLayer) {
            List<Object> args = invocation.args();
            int overlayCoords = LivingEntityRenderer.getOverlayCoords(entity,
                    ((LivingEntityRendererAccessor) instance).getWhiteOverlayProgress(entity, (Float) args.get(6)));
            HumanoidArmorLayerPatch.renderWithOverlay((HumanoidArmorLayer) invocation.instance(),
                    (PoseStack) args.getFirst(), (MultiBufferSource) args.get(1), (Integer) args.get(2),
                    (LivingEntity) args.get(3), (Float) args.get(4), (Float) args.get(5),
                    (Float) args.get(6), (Float) args.get(7), (Float) args.get(8),
                    (Float) args.get(9), overlayCoords);
        } else {
            invocation.call();
        }
    }
}
