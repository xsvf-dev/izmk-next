package ovo.xsvf.izmk.event.impl

import net.minecraft.client.gui.GuiGraphics
import ovo.xsvf.izmk.event.Event

/**
 * @author LangYa466
 * @since 2025/2/16
 */
data class Render2DEvent(val graphics: GuiGraphics?, val partialTick: Float) : Event
