package ovo.xsvf.izmk.gui.widget.impl.setting

import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.EnumSetting
import ovo.xsvf.izmk.translation.DirectTranslationEnum
import ovo.xsvf.izmk.translation.TranslationEnum
import ovo.xsvf.izmk.translation.TranslationManager
import ovo.xsvf.izmk.translation.TranslationString

class EnumSettingWidget<E: Enum<E>>(screen: GuiScreen, override val setting: EnumSetting<E>):
    AbstractSettingWidget(screen, setting) {
    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        val valueName = when (setting.value) {
            is TranslationEnum -> TranslationString(setting.name.key.fullKey, (setting.value as TranslationEnum).keyString).translation
            is DirectTranslationEnum -> TranslationManager.getTranslation((setting.value as DirectTranslationEnum).keyString)
            else -> setting.value.name
        }

        fontMulti.addText(
            "${setting.name.translation}: $valueName",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        setting.forwardLoop()
    }
}