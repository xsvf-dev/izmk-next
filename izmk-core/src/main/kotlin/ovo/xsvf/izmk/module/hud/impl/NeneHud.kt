package ovo.xsvf.izmk.module.hud.impl

import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.mod.hud.HUD

/**
 * @author xiaojiang233
 * @since 2025/2/22
 * Test Only
 */
class NeneHud : HUD("NeneHud", 0f, 0f, 100f,20f) {
    override fun render(event: Render2DEvent) {
        FontRenderers.drawString("谁敢反对宁宁，我就打爆他的狗头", x, y, ColorRGB.WHITE)
    }


}
