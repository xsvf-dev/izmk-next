package ovo.xsvf.izmk.module.impl

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.phys.Vec3
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.injection.accessor.EntityAccessor
import ovo.xsvf.izmk.module.Module
import java.util.*

object ItemPhysics : Module(
    name = "item-physics"
) {
    private val random: Random = Random()
    private var lastRenderTime = System.nanoTime()

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        lastRenderTime = System.nanoTime()
    }

    fun render(
        entity: ItemEntity, pose: PoseStack,
        buffer: MultiBufferSource, packedLight: Int, itemRenderer: ItemRenderer
    ) {
        pose.pushPose()
        val itemStack = entity.item
        random.setSeed((if (itemStack.isEmpty) 187 else Item.getId(itemStack.item) + itemStack.damageValue).toLong())

        val bakedModel = itemRenderer.getModel(itemStack, entity.level(), null, entity.id)
        val isThreeDimensional = bakedModel.isGui3d

        pose.mulPose(Axis.XP.rotation(Math.PI.toFloat() / 2))
        pose.mulPose(Axis.ZP.rotation(entity.yRot))

        val applyEffects =
            entity.age != 0

        // Handle Rotations
        if (applyEffects) {
            var rotateBy: Float = (getRotation() * 0.2f)
            if (mc.isPaused) rotateBy = 0f

            val motionMultiplier: Vec3? = stuckSpeedMultiplier(entity)
            if (motionMultiplier != null && motionMultiplier.lengthSqr() > 0) {
                rotateBy *= (motionMultiplier.x * 0.2).toFloat()
            }

            if (isThreeDimensional) {
                if (!entity.onGround()) {
                    rotateBy *= 2f
                    var fluid: Fluid? = fluid(entity)
                    if (fluid == null) {
                        fluid = fluid(entity, true)
                    }

                    if (fluid != null) {
                        rotateBy /= (1 + visosity(fluid, entity.level()))
                    }

                    entity.xRot += rotateBy
                }
            } else if (!java.lang.Double.isNaN(entity.x) && !java.lang.Double.isNaN(entity.y) && !java.lang.Double.isNaN(
                    entity.z
                )
            ) {
                if (entity.onGround()) {
                    entity.xRot = 0f
                } else {
                    rotateBy *= 2f
                    val fluid: Fluid? = fluid(entity)
                    if (fluid != null) {
                        rotateBy /= (1 + visosity(fluid, entity.level()))
                    }

                    entity.xRot += rotateBy
                }
            }

            if (isThreeDimensional) {
                pose.translate(0.0, -0.2, -0.08)
            } else if (entity.level().getBlockState(entity.blockPosition()).block === Blocks.SNOW
                || entity.level().getBlockState(entity.blockPosition().below())
                    .block === Blocks.SOUL_SAND
            ) {
                pose.translate(0.0, 0.0, -0.14)
            } else {
                pose.translate(0.0, 0.0, -0.04)
            }

            val height = 0.2
            if (isThreeDimensional) {
                pose.translate(0.0, height, 0.0)
            }

            pose.mulPose(Axis.YP.rotation(entity.xRot))
            if (isThreeDimensional) {
                pose.translate(0.0, -height, 0.0)
            }
        }

        val modelAmount: Int = getRenderAmount(itemStack)
        if (!isThreeDimensional) {
            val f7 = -0.0f * (modelAmount - 1) * 0.5f
            val f8 = -0.0f * (modelAmount - 1) * 0.5f
            val f9 = -0.09375f * (modelAmount - 1) * 0.5f
            pose.translate(f7, f8, f9)
        }

        for (k in 0..<modelAmount) {
            pose.pushPose()
            if (k > 0 && isThreeDimensional) {
                val f11: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
                val f13: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
                val f10: Float = (random.nextFloat() * 2.0f - 1.0f) * 0.15f
                pose.translate(f11, f13, f10)
            }

            itemRenderer.render(
                itemStack, ItemDisplayContext.GROUND, false, pose, buffer,
                packedLight, OverlayTexture.NO_OVERLAY, bakedModel
            )
            pose.popPose()
            if (!isThreeDimensional) {
                pose.translate(0.0, 0.0, 0.09375)
            }
        }

        pose.popPose()
    }

    private fun fluid(item: ItemEntity): Fluid? {
        return fluid(item, false)
    }

    private fun fluid(item: ItemEntity, below: Boolean): Fluid? {
        val d0 = item.position().y
        var pos = item.blockPosition()
        if (below) pos = pos.below()
        val state = item.level().getFluidState(pos)
        val fluid = state.type
        if (fluid.getTickDelay(item.level()) == 0) return null
        if (below) return fluid
        val filled = state.getHeight(item.level(), pos).toDouble()
        if (d0 - pos.y - 0.2 <= filled) return fluid
        return null
    }

    private fun stuckSpeedMultiplier(entity: Entity): Vec3? {
        return (entity as EntityAccessor).stuckSpeedMultiplier
    }

    private fun visosity(fluid: Fluid?, level: Level): Float {
        if (fluid == null) return 0f
        return fluid.getTickDelay(level).toFloat()
    }

    private fun getRotation(): Float {
        return (System.nanoTime() - lastRenderTime) / 100000000f
    }

    private fun getRenderAmount(itemStack: ItemStack): Int {
        var i = 1
        if (itemStack.count > 48) {
            i = 5
        } else if (itemStack.count > 32) {
            i = 4
        } else if (itemStack.count > 16) {
            i = 3
        } else if (itemStack.count > 1) {
            i = 2
        }
        return i
    }
}