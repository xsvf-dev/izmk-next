package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.ColorSetting

class ColorSettingWidget(
    screen: GuiScreen,
    override val setting: ColorSetting
): AbstractSettingWidget(screen, setting) {
    private var isExtended = false

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        fontMulti.addText(
            "${setting.name.translation}: ${setting.value}",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        if (!isLeftClick) {
            isExtended = !isExtended
        }
    }

    override fun getHeight0(): Float =
        if (isExtended) 50f else 20f
}