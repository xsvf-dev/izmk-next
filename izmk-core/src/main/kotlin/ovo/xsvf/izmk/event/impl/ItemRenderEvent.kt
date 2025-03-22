package ovo.xsvf.izmk.event.impl

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import ovo.xsvf.izmk.event.Event

data class ItemRenderEvent(
    val isPre: Boolean,
    val entity: LivingEntity?,
    val itemStack: ItemStack,
    val itemDisplayContext: ItemDisplayContext,
    val leftHand: Boolean,
    val poseStack: PoseStack,
    val buffer: MultiBufferSource,
    val level: Level
) : Event()
