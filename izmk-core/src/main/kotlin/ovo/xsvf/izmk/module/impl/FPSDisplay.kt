package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.setting.impl.BoolSetting

object FPSDisplay: Module("FPSDisplay", "Displays the FPS on the screen") {
    private val drawString = BoolSetting("DrawString", true)

    init {
        settings.add(drawString)
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (drawString.value) {
            FontRenderers.drawString(
                "FPS: ${IZMK.mc.fpsString}",
                20f, 20f,
                ColorRGB.WHITE
            )
        }
    }
}