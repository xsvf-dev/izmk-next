package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.NumberSetting
import kotlin.math.roundToInt

class NumberSettingWidget<N: Number>(screen: GuiScreen, override val setting: NumberSetting<N>): AbstractSettingWidget(screen, setting) {
    private var dragging = false
    private var mouseX = -1f
    private var mouseY = -1f

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        this.mouseX = mouseX
        this.mouseY = mouseY
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)

        if (dragging && mouseX in renderX..(renderX + (screenWidth - 2 * 5f))) {
            setting.value(((mouseX - renderX) / (screenWidth - 2 * 5f) * setting.maxValue.toFloat()) as N)
        }

        rectMulti.addRect(
            renderX, renderY,
            (screenWidth - 2 * 5f) * (setting.value.toFloat() / setting.maxValue.toFloat()), getHeight(),
            ColorRGB(0.5f, 0.5f, 0.5f).alpha(0.4f)
        )
        fontMulti.addText(
            "${setting.name.translation} : ${(setting.value.toFloat() * 100).roundToInt() / 100f}",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        dragging = true
    }

    override fun mouseReleased(mouseX: Float, mouseY: Float, isLeftClick: Boolean) : Boolean {
        if (dragging) {
            dragging = false
            return true
        }
        return false
    }
}