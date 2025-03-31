package ovo.xsvf.izmk.module.impl

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.item.enchantment.Enchantments
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.ItemRenderEvent
import ovo.xsvf.izmk.module.Module

object ItemScale : Module("item-scale") {
    private val showFirstPerson by setting("show-first-person", true)

    private val itemChecks = listOf(
        ItemData("instant-axe", Items.GOLDEN_AXE) { false },
        ItemData("kb-ball", Items.SLIME_BALL) { true },
        ItemData("totem", Items.TOTEM_OF_UNDYING) { true },
        ItemData("enchanted-golden-apple", Items.ENCHANTED_GOLDEN_APPLE) { true },
        ItemData("power-5-bow", Items.BOW) {
            EnchantmentHelper.getEnchantments(it)
                .getOrDefault(Enchantments.POWER_ARROWS, 0) >= 5
        },
        ItemData("punch-3-bow", Items.BOW) {
            EnchantmentHelper.getEnchantments(it)
                .getOrDefault(Enchantments.PUNCH_ARROWS, 0) >= 3
        },
        ItemData("flame-bow", Items.BOW) {
            EnchantmentHelper.getEnchantments(it).contains(Enchantments.FLAMING_ARROWS)
        },
        ItemData("end-crystal", Items.END_CRYSTAL) { true },
        ItemData("sharp-8", Items.DIAMOND_SWORD) {
            EnchantmentHelper.getEnchantments(it)
                .getOrDefault(Enchantments.SHARPNESS, 0) >= 8
        }
    )

    @EventTarget
    fun onItemRender(e: ItemRenderEvent) {
        if (showFirstPerson && e.itemDisplayContext.firstPerson()) return

        itemChecks.forEach { if (it.checkAndApply(e.itemStack, e.poseStack)) return@forEach }
    }

    data class ItemData(
        private val name: String,
        private val item: Item,
        private val checker: (ItemStack) -> Boolean
    ) {
        private val enabled by setting(name, false)

        private val scale by setting("$name-scale", 1f, 0.1f..10f, 0.05f)
            .visibility { enabled }
        private val xRotation by setting("$name-x-rotation", 0f, -180f..180f)
            .visibility { enabled }
        private val yRotation by setting("$name-y-rotation", 0f, -180f..180f)
            .visibility { enabled }
        private val zRotation by setting("$name-z-rotation", 0f, -180f..180f)
            .visibility { enabled }
        private val xOffset by setting("$name-x-offset", 0f, -1.5f..1.5f, 0.01f)
            .visibility { enabled }
        private val yOffset by setting("$name-y-offset", 0f, -1.5f..1.5f, 0.01f)
            .visibility { enabled }
        private val zOffset by setting("$name-z-offset", 0f, -1.5f..1.5f, 0.01f)
            .visibility { enabled }

        fun check(stack: ItemStack) = enabled && stack.item == item && checker(stack)

        fun apply(poseStack: PoseStack) {
            poseStack.scale(scale, scale, scale)
            poseStack.translate(xOffset, yOffset, zOffset)
            poseStack.mulPose(Axis.XP.rotationDegrees(xRotation))
            poseStack.mulPose(Axis.YP.rotationDegrees(yRotation))
            poseStack.mulPose(Axis.ZP.rotationDegrees(zRotation))
        }

        fun checkAndApply(stack: ItemStack, poseStack: PoseStack) =
            check(stack).also { if (it) apply(poseStack) }
    }
}
