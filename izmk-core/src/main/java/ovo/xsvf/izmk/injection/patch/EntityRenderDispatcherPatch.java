package ovo.xsvf.izmk.injection.patch;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ovo.xsvf.izmk.graphics.color.ColorRGB;
import ovo.xsvf.izmk.module.impl.Hitboxes;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Inject;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(EntityRenderDispatcher.class)
public class EntityRenderDispatcherPatch {
    private static final Logger log = LogManager.getLogger(EntityRenderDispatcherPatch.class);

    @Inject(method = "renderHitbox", desc = "(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/world/entity/Entity;F)V")
    public static void renderHitbox(PoseStack poseStack, VertexConsumer vertexConsumer, Entity entity, float tickDelta, CallbackInfo callbackInfo) {
        if (Hitboxes.INSTANCE.getEnabled()) {
            callbackInfo.cancelled = true;
            if (entity instanceof ItemEntity && !Hitboxes.INSTANCE.getItems()) {
                return;
            } else if (entity instanceof Mob && !Hitboxes.INSTANCE.getMobs()) {
                return;
            } else if (entity instanceof Projectile && !Hitboxes.INSTANCE.getProjectiles()) {
                return;
            } else if (entity instanceof Player && !Hitboxes.INSTANCE.getPlayers()) {
                return;
            }

            AABB box = entity.getBoundingBox().move(-entity.getX(), -entity.getY(), -entity.getZ());
            ColorRGB color = Hitboxes.INSTANCE.getColor();
            RenderSystem.enableDepthTest();
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            LevelRenderer.renderLineBox(poseStack, vertexConsumer, box,
                    color.getRFloat(), color.getGFloat(), color.getBFloat(), 255 - color.getA());

            if (Hitboxes.INSTANCE.getShowViewVector()) {
                ColorRGB viewVectorColor = Hitboxes.INSTANCE.getViewVectorColor();

                Vec3 vec31 = entity.getViewVector(tickDelta);
                PoseStack.Pose posestack$pose = poseStack.last();
                vertexConsumer.vertex(posestack$pose.pose(), 0.0F, entity.getEyeHeight(), 0.0F)
                        .color(viewVectorColor.getRFloat(), viewVectorColor.getGFloat(),
                                viewVectorColor.getBFloat(), 255 - viewVectorColor.getA())
                        .normal(posestack$pose.normal(), (float) vec31.x, (float) vec31.y, (float) vec31.z)
                        .endVertex();
                vertexConsumer.vertex(posestack$pose.pose(), (float) (vec31.x * (double) 2.0F),
                                (float) ((double) entity.getEyeHeight() + vec31.y * (double) 2.0F),
                                (float) (vec31.z * (double) 2.0F))
                        .color(viewVectorColor.getRFloat(), viewVectorColor.getGFloat(),
                                viewVectorColor.getBFloat(), 255 - viewVectorColor.getA())
                        .normal(posestack$pose.normal(), (float) vec31.x, (float) vec31.y, (float) vec31.z)
                        .endVertex();
            }

            RenderSystem.disableDepthTest();
        }
    }

    @Inject(method = "setRenderHitBoxes", desc = "(Z)V")
    public static void setRenderHitBoxes(boolean renderHitBoxes, CallbackInfo callbackInfo) {
        if (Hitboxes.INSTANCE.getEnabled() != renderHitBoxes) {
            Hitboxes.INSTANCE.setEnabled(renderHitBoxes);
        }
    }
}
