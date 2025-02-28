package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.module.RenderableModule
import java.awt.Color

object TestHud: RenderableModule("test-hud", defaultX = 0f, defaultY = 0f, width = 100f, height = 100f) {
    override fun render(event: Render2DEvent) {
        FontRenderers.drawString(
            "Hello World",
            x, y,
            ColorRGB(Color.WHITE.rgb),
            shadow = true
        )
    }
}