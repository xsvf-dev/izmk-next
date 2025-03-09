package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.patchify.annotation.Patch;
import ovo.xsvf.patchify.annotation.WrapInvoke;
import ovo.xsvf.patchify.api.Invocation;

@SuppressWarnings("rawtypes, unchecked")
@Patch(HumanoidArmorLayer.class)
public class HumanoidArmorLayerPatch {
    private static final Logger log = LogManager.getLogger(HumanoidArmorLayerPatch.class);
    private static int overlayCoords;

    public static void renderWithOverlay(HumanoidArmorLayer instance,
                                  PoseStack poseStack, MultiBufferSource multiBufferSource, int i,
                                  LivingEntity entity, float f, float g, float h, float j, float k, float l,
                                  int overlayCoords0) {
        log.debug("calling renderWithOverlay method");
        overlayCoords = overlayCoords0;
        instance.render(poseStack, multiBufferSource, i, entity, f, g, h, j, k, l);
    }

    @WrapInvoke(method = "renderModel", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/model/Model;FFFLnet/minecraft/resources/ResourceLocation;)V",
                target = "net/minecraft/client/model/Model/renderToBuffer", targetDesc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V")
    public static void renderToBuffer(HumanoidArmorLayer<LivingEntity, HumanoidModel<LivingEntity>, HumanoidModel<LivingEntity>> instance,
                                      PoseStack poseStack, MultiBufferSource multiBufferSource, int i,
                                      Model model, float f, float g, float h, ResourceLocation resourceLocation,
                                      Invocation<Model, Void> invocation) throws Exception {
        if (overlayCoords != -1) invocation.args().set(3, overlayCoords);
        else log.debug("overlayCoords is -1, not setting it");
        invocation.call();
    }
}
