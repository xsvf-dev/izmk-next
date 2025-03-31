package ovo.xsvf.izmk.gui.widget.impl.setting

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.font.FontRenderers
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.AbstractSettingWidget
import ovo.xsvf.izmk.settings.TextSetting
import ovo.xsvf.izmk.util.timing.Duration
import ovo.xsvf.izmk.util.timing.Timer

class TextSettingWidget(screen: GuiScreen, override val setting: TextSetting) : AbstractSettingWidget(screen, setting) {
    private var editMode = false
    private var cacheValue = setting.value
    private var cursorTimer = Timer(Duration.Millisecond)
    private var cursorIndex = cacheValue.length

    override fun draw0(
        screenWidth: Float, screenHeight: Float,
        mouseX: Float, mouseY: Float,
        renderX: Float, renderY: Float,
        fontMulti: FontMultiDraw, rectMulti: PosColor2DMultiDraw,
        partialTicks: Float
    ) {
        drawDefaultBackground(rectMulti, renderX, renderY, screenWidth)
        if (!editMode) {
            fontMulti.addText(
                "${setting.name.translation} : ${setting.value}",
                renderX + 2f,
                renderY + 3f,
                ColorRGB.WHITE
            )
        } else {
            val prefix = "${setting.name.translation} : "
            fontMulti.addText(
                "$prefix$cacheValue",
                renderX + 2f,
                renderY + 3f,
                ColorRGB.WHITE
            )
            if (cursorTimer.passed(250)) {
                fontMulti.addText(
                    "|",
                    renderX + 2f +
                            FontRenderers.getStringWidth(prefix) +
                            FontRenderers.getStringWidth(cacheValue.substring(0, cursorIndex)) - 1f,
                    renderY + 3f,
                    ColorRGB.WHITE
                )
                if (cursorTimer.passed(500)) cursorTimer.reset()
            }
        }
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        if (editMode) {
            when (keyCode) {
                GLFW.GLFW_KEY_BACKSPACE -> {
                    if (cursorIndex > 0) {
                        cacheValue = cacheValue.substring(0, cursorIndex - 1) +
                                cacheValue.substring(cursorIndex)
                        cursorIndex--
                    }
                    return true
                }
                GLFW.GLFW_KEY_DELETE -> {
                    if (cursorIndex < cacheValue.length) {
                        cacheValue = cacheValue.substring(0, cursorIndex) +
                                cacheValue.substring(cursorIndex + 1)
                    }
                    return true
                }
                GLFW.GLFW_KEY_LEFT -> {
                    cursorIndex = maxOf(0, cursorIndex - 1)
                    return true
                }
                GLFW.GLFW_KEY_RIGHT -> {
                    cursorIndex = minOf(cacheValue.length, cursorIndex + 1)
                    return true
                }
                GLFW.GLFW_KEY_HOME -> {
                    cursorIndex = 0
                    return true
                }
                GLFW.GLFW_KEY_END -> {
                    cursorIndex = cacheValue.length
                    return true
                }
                GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                    setting.value(cacheValue)
                    editMode = false
                    return true
                }
            }
        }
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z ||
            keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9) {
            cacheValue = cacheValue.substring(0, cursorIndex) + keyCode.toChar() + cacheValue.substring(cursorIndex)
            cursorIndex += 1
            return true
        }
        return false
    }

    override fun mouseClicked(mouseX: Float, mouseY: Float, isLeftClick: Boolean) {
        if (isLeftClick && !editMode) {
            editMode = true
        }
    }
}