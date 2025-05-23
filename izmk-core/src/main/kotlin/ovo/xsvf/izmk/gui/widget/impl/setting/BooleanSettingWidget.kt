package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.BooleanSetting

class BooleanSettingWidget(screen: GuiScreen, override val setting: BooleanSetting): AbstractSettingWidget(screen, setting) {
    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        fontMulti.addText(
            setting.name.translation,
            renderX + 2f,
            renderY + 3f,
            if (setting.value) ColorRGB.GREEN else ColorRGB.RED
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        setting.toggle()
    }
}