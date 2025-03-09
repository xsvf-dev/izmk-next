package ovo.xsvf.izmk.module.impl.render

import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.RenderableModule

object WaterMark: RenderableModule(
    name = "water-mark",
    defaultX = 5f,
    defaultY = 5f,
    width = FontRenderers.getStringWidth("IZMK")
) {
    private val text by setting("text", "IZMK")
    private val shadow by setting("shadow", false)
    private val color by setting("color", ColorRGB.WHITE)
    private val scale by setting("scale", 1f, 1f..5f, 0.1f)

    override fun render(event: Render2DEvent) {
        width = FontRenderers.getStringWidth(text, scale)
        height = FontRenderers.getHeight(scale)
        FontRenderers.drawString(text, x, y, color, shadow, scale)
    }
}