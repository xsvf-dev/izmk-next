package ovo.xsvf.izmk.gui.impl

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.*
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.module.Module
import ovo.xsvf.izmk.module.ModuleManager

class ModuleListScreen : GuiScreen("module-list") {
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()
    private val valueListScreen = HashMap<Module, GuiScreen>()
    private val window = DragWindow(50, 50, 300, 400)

    private val entryHeight = 20f
    private val padding = 35f

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        window.drag(mouseX, mouseY)

        fontMulti.addText("IZMK-Next",window.x.toFloat() + 5f, window.y.toFloat() + 5f,ColorRGB.WHITE,false,2f)

        // 绘制列表背景
        rectMulti.addRect(window.x.toFloat(), window.y.toFloat(), window.width.toFloat(), window.height.toFloat(), ColorRGB(0.15f, 0.15f, 0.15f))

        var offsetY = window.y + padding

        ModuleManager.modulesMap.values.forEach {
            val moduleX = window.x + 5f
            val moduleY = offsetY

            // 背景渐变
            rectMulti.addRectGradientHorizontal(
                moduleX, moduleY, (window.width - 2 * 5f), entryHeight,
                ColorRGB(0.2f, 0.2f, 0.2f), ColorRGB(0.25f, 0.25f, 0.25f)
            )

            // 模块名称
            fontMulti.addText(
                it.getDisplayName(),
                moduleX + 7f,
                moduleY + 2f,
                if (it.enabled) ColorRGB.WHITE else ColorRGB.GRAY,
                false, 1.2f
            )

            offsetY += entryHeight + 5f
        }

        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        if (window.isHoveringHeader(mouseX.toInt(), mouseY.toInt(), 20)) {
            window.startDrag(mouseX.toInt(), mouseY.toInt())
            return
        }

        var offsetY = window.y + padding

        for (module in ModuleManager.modulesMap.values) {
            val moduleX = window.x + 5f
            val moduleY = offsetY

            val isMouseOver = RenderUtils2D.isMouseOver(
                mouseX.toFloat(),
                mouseY.toFloat(),
                moduleX, moduleY,
                (moduleX + window.width - 2 * 5f),
                (moduleY + entryHeight)
            )

            if (isMouseOver) {
                when (buttonID) {
                    GLFW.GLFW_MOUSE_BUTTON_LEFT -> module.toggle()
                    GLFW.GLFW_MOUSE_BUTTON_RIGHT -> {
                        valueListScreen.getOrPut(module) { ValueListScreen(module) }.openScreen(this)
                    }
                }
                break
            }

            offsetY += entryHeight + 5f
        }
    }

    override fun mouseReleased(buttonID: Int, mouseX: Double, mouseY: Double) {
        window.stopDrag()
    }
}
