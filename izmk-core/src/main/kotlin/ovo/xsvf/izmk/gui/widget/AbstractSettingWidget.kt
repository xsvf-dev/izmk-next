package ovo.xsvf.izmk.gui.widget

import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.settings.AbstractSetting

abstract class AbstractSettingWidget(
    screen: GuiScreen,
    open val setting: AbstractSetting<*>
): AbstractWidget(screen) {
    open fun draw0(
        screenWidth: Float, screenHeight: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
    }

    final override fun draw(
        screenWidth: Float, screenHeight: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        if (!setting.visibility()) return
        draw0(screenWidth, screenHeight, renderX, renderY, fontMulti, rectMulti, partialTicks)
    }

    open fun getHeight0(): Float {
        return 20f
    }

    final override fun getHeight(): Float {
        return if (setting.visibility()) getHeight0() else 0f
    }
}