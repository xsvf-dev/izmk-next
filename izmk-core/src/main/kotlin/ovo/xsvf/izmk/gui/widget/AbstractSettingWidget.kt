package ovo.xsvf.izmk.gui.widget

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.settings.AbstractSetting

abstract class AbstractSettingWidget(
    screen: GuiScreen,
    open val setting: AbstractSetting<*>
): AbstractWidget(screen) {
    abstract fun draw0(
        screenWidth: Int, screenHeight: Int,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    )

    final override fun draw(
        screenWidth: Int, screenHeight: Int,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        if (!setting.visibility()) return
        rectMulti.addRectGradientHorizontal(
            renderX, renderY,
            screenWidth - 2 * 5f, getHeight().toFloat(),
            ColorRGB(0.2f, 0.2f, 0.2f),
            ColorRGB(0.25f, 0.25f, 0.25f)
        )
        draw0(screenWidth, screenHeight, renderX, renderY, fontMulti, rectMulti, partialTicks)
    }

    open fun getHeight0() : Int {
        return 20
    }

    final override fun getHeight(): Int {
        return if (setting.visibility()) getHeight0() else 0
    }
}