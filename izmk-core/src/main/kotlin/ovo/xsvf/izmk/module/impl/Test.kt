package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.annotations.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.module.Module

/**
 * @author LangYa466
 * @since 2025/2/16
 */
class Test : Module("Test") {
    init {
        println("Test module loaded")
        enabled = true
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        event.graphics!!.drawString(mc.font, "Hello, World!", 10, 10, -0x1)
    }
}
