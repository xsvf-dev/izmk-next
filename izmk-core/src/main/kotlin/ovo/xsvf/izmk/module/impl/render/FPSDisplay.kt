package ovo.xsvf.izmk.module.impl.render

import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.RenderableModule

object FPSDisplay: RenderableModule(
    name = "fps-display",
    defaultX = 20f, defaultY = 20f,
    width = 40f, height = 15f
) {
    private val drawString by setting("draw-string", true)
    private val fontScale by setting("font-scale",1f,1f..3f)
    private val stringColor by setting("string-color", ColorRGB.WHITE)

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (drawString) {
            val text = "FPS: ${IZMK.mc.fps}"
            width = FontRenderers.getStringWidth(text, fontScale)
            height = FontRenderers.getHeight(fontScale)
            FontRenderers.drawString(
                text, x, y, stringColor, true, fontScale
            )
        }
    }
}