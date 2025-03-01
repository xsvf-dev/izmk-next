package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.BooleanSetting

class BooleanSettingWidget(screen: GuiScreen, override val setting: BooleanSetting): AbstractSettingWidget(screen, setting) {
    override fun draw0(
        screenWidth: Int, screenHeight: Int,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        fontMulti.addText(
            "${setting.name.translation} ${if (setting.value) "(ON)" else "(OFF)"}",
            renderX + 2f,
            renderY + 3f,
            if (setting.value) ColorRGB.WHITE else ColorRGB.GRAY
        )
    }

    override fun mouseClicked(mouseX: Double, mouseY: Double, isLeftClick: Boolean) {
        setting.toggle()
    }
}