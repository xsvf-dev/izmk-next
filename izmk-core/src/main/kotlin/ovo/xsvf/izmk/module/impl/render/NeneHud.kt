package ovo.xsvf.izmk.module.impl.render

import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.texture.ImageFileUtils
import ovo.xsvf.izmk.graphics.texture.Texture
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.module.RenderableModule
import ovo.xsvf.izmk.resource.Resource

object NeneHud : RenderableModule(
    name = "nene-hud",
    defaultX = 0f, defaultY = 0f,
    width = 300f, height = 120f,
) {
    private val imageTexture: Texture = ImageFileUtils.loadTextureFromResource(Resource("image/nene.png"))

    override fun render(event: Render2DEvent) {
        FontRenderers.drawString("起爆器", x, y, ColorRGB.WHITE)

        RenderUtils2D.drawTextureRect(
            x + FontRenderers.getStringWidth("起爆器 "),
            y,
            200f,
            120f,
            imageTexture,
            ColorRGB.WHITE
        )
    }
}