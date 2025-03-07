package ovo.xsvf.izmk.gui.widget.impl.setting

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.KeyBindSetting
import ovo.xsvf.izmk.util.input.KeyBind

class KeybindSettingWidget(
    screen: GuiScreen,
    override val setting: KeyBindSetting
): AbstractSettingWidget(screen, setting) {
    private var binding = false

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        fontMulti.addText(
            "${setting.name.translation}: " +
                if (binding) "Press a key to bind"
                else if (setting.value.keyCode == -1) "Not bound"
                else "Bound to ${setting.value.keyName.uppercase()}",
            renderX + 2f,
            renderY + 3f,
            ColorRGB.WHITE
        )
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        binding = !binding
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (binding && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            binding = false
            if (keyCode == GLFW.GLFW_KEY_DELETE) {
                setting.value = KeyBind(KeyBind.Type.KEYBOARD, -1, -1)
                return true
            }
            setting.value = KeyBind(KeyBind.Type.KEYBOARD, keyCode, scanCode)
        }
        return false
    }
}