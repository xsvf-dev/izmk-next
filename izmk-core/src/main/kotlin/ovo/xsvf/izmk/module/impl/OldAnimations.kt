package ovo.xsvf.izmk.module.impl

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.util.Mth
import net.minecraft.world.InteractionHand
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.HitResult
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.PreTickEvent
import ovo.xsvf.izmk.module.Module

object OldAnimations: Module(name = "old-animations") {
    val swordBlocking by setting("sword-blocking", false)
    val swing by setting("swing", false)

    @EventTarget
    fun onTick(event: PreTickEvent) {
        mc.player ?: return
        val hand = mc.player!!.usedItemHand
        if (!swing || mc.player!!.getItemInHand(hand).isEmpty ||
            !mc.options.keyAttack.isDown || !mc.player!!.isUsingItem) {
            return
        }

        if (mc.hitResult != null && mc.hitResult!!.type === HitResult.Type.BLOCK &&
            hand == InteractionHand.MAIN_HAND) {
            fakeHandSwing(mc.player!!, hand)
        }
    }

    fun transformBlockFirstPerson(matrixStack: PoseStack, hand: HumanoidArm) {
        val direction = if (hand == HumanoidArm.RIGHT) 1 else -1
        // values taken from Minecraft snapshot 15w33b
        matrixStack.translate(direction * -0.14142136f, 0.08f, 0.14142136f)
        matrixStack.mulPose(Axis.XP.rotationDegrees(-102.25f))
        matrixStack.mulPose(Axis.YP.rotationDegrees(direction * 13.365f))
        matrixStack.mulPose(Axis.ZP.rotationDegrees(direction * 78.05f))
    }

    fun applySwingOffset(matrices: PoseStack, arm: HumanoidArm, swingProgress: Float) {
        val armSide = if (arm == HumanoidArm.RIGHT) 1 else -1
        val f = Mth.sin(swingProgress * swingProgress * Math.PI.toFloat())
        matrices.mulPose(Axis.YP.rotationDegrees(armSide.toFloat() * (45.0f + f * -20.0f)))
        val g = Mth.sin(Mth.sqrt(swingProgress) * Math.PI.toFloat())
        matrices.mulPose(Axis.ZP.rotationDegrees(armSide.toFloat() * g * -20.0f))
        matrices.mulPose(Axis.XP.rotationDegrees(g * -80.0f))
        matrices.mulPose(Axis.YP.rotationDegrees(armSide.toFloat() * -45.0f))
    }

    private fun fakeHandSwing(player: Player, hand: InteractionHand) {
        if (!player.swinging || player.swingTime >= getHandSwingDuration(player) / 2 || player.swingTime < 0) {
            player.swingTime = -1
            player.swinging = true
            player.swingingArm = hand
        }
    }

    private fun getHandSwingDuration(entity: LivingEntity): Int {
        if (MobEffectUtil.hasDigSpeed(entity)) {
            return 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity))
        } else if (entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            val effect = entity.getEffect(MobEffects.DIG_SLOWDOWN)
            return if (effect == null) 6 else 6 + (1 + effect.amplifier) * 2
        } else {
            return 6
        }
    }
}