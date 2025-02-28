package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.RenderableModule

object FPSDisplay: RenderableModule(
    name = "fps-display",
    description = "Displays the FPS on the screen",
    defaultX = 20f, defaultY = 20f,
    width = 40f, height = 15f
) {
    private val drawString by setting("draw-string", true)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (drawString) {
            FontRenderers.drawString(
                "FPS: ${IZMK.mc.fps}",
                x, y, ColorRGB.WHITE
            )
        }
    }
}