package ovo.xsvf.izmk.event.impl

import net.minecraft.client.gui.GuiGraphics
import ovo.xsvf.izmk.event.Event

class Render2DEvent(val guiGraphics: GuiGraphics, val partialTick: Float) : Event()
