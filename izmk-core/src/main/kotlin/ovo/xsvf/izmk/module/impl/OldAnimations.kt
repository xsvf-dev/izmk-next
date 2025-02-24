package ovo.xsvf.izmk.module.impl

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.util.Mth
import net.minecraft.world.entity.HumanoidArm
import ovo.xsvf.izmk.module.Module

object OldAnimations: Module("OldAnimations", "旧版本动画效果") {
    private val swordBlocking by setting("swordBlocking")

    fun transform(matrices: PoseStack, arm: HumanoidArm, equipProgress: Float, swingProgress: Float) {
        matrices.translate(if (arm == HumanoidArm.RIGHT) -0.1f else 0.1f, 0.1f, 0.0f)
        applySwingOffset(matrices, arm, swingProgress * 0.9f)
        matrices.mulPose(Axis.XP.rotationDegrees(-102.25f))
        matrices.mulPose(
            (if (arm == HumanoidArm.RIGHT) Axis.YP else Axis.YN)
                .rotationDegrees(13.365f)
        )
        matrices.mulPose(
            (if (arm == HumanoidArm.RIGHT) Axis.ZP else Axis.ZN)
                .rotationDegrees(78.05f)
        )
    }

    private fun applySwingOffset(matrices: PoseStack, arm: HumanoidArm, swingProgress: Float) {
        val armSide = if (arm == HumanoidArm.RIGHT) 1 else -1
        val f = Mth.sin(swingProgress * swingProgress * Math.PI.toFloat())
        matrices.mulPose(Axis.YP.rotationDegrees(armSide.toFloat() * (45.0f + f * -20.0f)))
        val g = Mth.sin(Mth.sqrt(swingProgress) * Math.PI.toFloat())
        matrices.mulPose(Axis.ZP.rotationDegrees(armSide.toFloat() * g * -20.0f))
        matrices.mulPose(Axis.XP.rotationDegrees(g * -80.0f))
        matrices.mulPose(Axis.YP.rotationDegrees(armSide.toFloat() * -45.0f))
    }
}