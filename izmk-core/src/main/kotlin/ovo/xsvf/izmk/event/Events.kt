package ovo.xsvf.izmk.event

import net.minecraft.client.gui.GuiGraphics

class TickEvent: Event()

class Render2DEvent(val guiGraphics: GuiGraphics?, val partialTick: Float) : Event()