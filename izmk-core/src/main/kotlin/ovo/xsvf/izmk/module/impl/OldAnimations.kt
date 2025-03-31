package ovo.xsvf.izmk.module.impl

import net.minecraft.world.InteractionHand
import net.minecraft.world.effect.MobEffectUtil
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.SwordItem
import net.minecraft.world.level.block.*
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.PreTickEvent
import ovo.xsvf.izmk.module.Module

object OldAnimations : Module(name = "old-animations") {
    val swordBlocking by setting("sword-blocking", false)
    val swing by setting("swing", false)
    val oldCamera by setting("old-camera", false)
    val roteteBackwards by setting("rotate-backwards", false)
    val heldItemPosition by setting("held-item-position", false)
    val fishingRod by setting("fishing-rod", false)

    private val consumables = listOf(
        ChestBlock::class.java,
        EnderChestBlock::class.java,
        ShulkerBoxBlock::class.java,
        FurnaceBlock::class.java,
        CraftingTableBlock::class.java,
        SmokerBlock::class.java,
        BlastFurnaceBlock::class.java,
        CartographyTableBlock::class.java,
        AnvilBlock::class.java,
        BellBlock::class.java,
        BeaconBlock::class.java,
        DragonEggBlock::class.java,
        LeverBlock::class.java,
        ButtonBlock::class.java,
        GrindstoneBlock::class.java,
        LoomBlock::class.java,
        NoteBlock::class.java,
        FenceGateBlock::class.java,
        DoorBlock::class.java,
        StonecutterBlock::class.java,
        SignBlock::class.java,
        WallSignBlock::class.java,
        WallHangingSignBlock::class.java,
        RepeaterBlock::class.java,
        ComparatorBlock::class.java,
        DispenserBlock::class.java,
        JigsawBlock::class.java,
        CommandBlock::class.java,
        StructureBlock::class.java,
        HopperBlock::class.java,
        BedBlock::class.java,
        BarrelBlock::class.java,
        CakeBlock::class.java,
        CandleCakeBlock::class.java,
        BrewingStandBlock::class.java,
        DaylightDetectorBlock::class.java
    )

    @EventTarget
    fun onTick(event: PreTickEvent) {
        mc.player ?: return
        val hand = mc.player!!.usedItemHand
        if (!swing || mc.player!!.getItemInHand(hand).isEmpty ||
            !mc.options.keyAttack.isDown || !mc.player!!.isUsingItem
        ) {
            return
        }

        if (mc.hitResult != null && mc.hitResult!!.type === HitResult.Type.BLOCK &&
            hand == InteractionHand.MAIN_HAND
        ) {
            fakeHandSwing(mc.player!!, hand)
        }
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

    fun shouldBlock(): Boolean {
        if (!enabled || !swordBlocking || mc.player == null || mc.level == null) return false

        val hitResult = mc.player!!.pick(5.0, 0.0f, false)
        if (hitResult.type == HitResult.Type.BLOCK) {
            val block = mc.level!!.getBlockState((hitResult as BlockHitResult).blockPos).block
            if (consumables.any { it.isInstance(block) }) return false
        }

        return mc.player!!.mainHandItem.item is SwordItem && mc.options.keyUse.isDown
    }
}