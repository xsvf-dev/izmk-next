package ovo.xsvf.izmk.event

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component

class TickEvent: Event()

class Render2DEvent(val guiGraphics: GuiGraphics?, val partialTick: Float) : Event()

class SendMessageEvent(val component: Component) : CancellableEvent()
