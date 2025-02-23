package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.settings.BooleanSetting
import ovo.xsvf.izmk.translation.TranslationKey
import ovo.xsvf.izmk.translation.TranslationString

object FPSDisplay: Module("fps-display", "Displays the FPS on the screen") {
    private val drawString by BooleanSetting(TranslationString(TranslationKey("draw-string")))

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (drawString) {
            FontRenderers.drawString(
                "FPS: ${IZMK.mc.fps}",
                20f, 20f,
                ColorRGB.WHITE
            )
        }
    }
}