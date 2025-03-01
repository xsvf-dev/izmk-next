package ovo.xsvf.izmk.gui.screen

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.GuiScreen
import ovo.xsvf.izmk.gui.widget.impl.ModuleWidget
import ovo.xsvf.izmk.module.ModuleManager

class ModuleListScreen : GuiScreen("module-list") {
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()

    private val widgets = mutableListOf<ModuleWidget>()

    private val window = DraggableWindow(50, 50, 300, 400)
    private val padding = 35f

    init {
        ModuleManager.modules().forEach {
            widgets.add(ModuleWidget(this, it))
        }
    }

    override fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {
        window.update(mouseX, mouseY)

        fontMulti.addText("IZMK-Next",
            window.x.toFloat() + 5f,
            window.y.toFloat() + 5f,
            ColorRGB.WHITE,
            false,2f
        )

        // 绘制列表背景
        rectMulti.addRect(window.x.toFloat(), window.y.toFloat(),
            window.width.toFloat(), window.height.toFloat(),
            ColorRGB(0.15f, 0.15f, 0.15f)
        )

        var offsetY = window.y + padding

        widgets.forEach {
            it.draw(window.width, window.height, window.x + 5f,
                offsetY, fontMulti, rectMulti, partialTicks)
            offsetY += it.getHeight() + 5f
        }

        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {
        if (window.shouldDrag(mouseX.toInt(), mouseY.toInt(), 20)) {
            window.startDrag(mouseX.toInt(), mouseY.toInt())
            return
        }

        var offsetY = window.y + padding

        widgets.forEach {
            val moduleX = window.x + 5f
            val moduleY = offsetY

            val isMouseOver = RenderUtils2D.isMouseOver(
                mouseX.toFloat(), mouseY.toFloat(),
                moduleX, moduleY,
                (moduleX + window.width - 2 * 5f),
                (moduleY + it.getHeight())
            )

            if (isMouseOver) {
                it.mouseClicked(mouseX, mouseY, buttonID == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                return
            }

            offsetY += it.getHeight() + 5f
        }
    }

    override fun mouseReleased(buttonID: Int, mouseX: Double, mouseY: Double) {
        window.stopDrag()
    }
}
