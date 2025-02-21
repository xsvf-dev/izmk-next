package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventListener
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2025/2/16
 */
class Test : Module("Test") {
    init {
        enabled = true
    }

    @EventListener
    fun onRender2D(event: Render2DEvent) {
        event.guiGraphics.drawString(mc.font, "Test",5,5,-1)
    }
}
