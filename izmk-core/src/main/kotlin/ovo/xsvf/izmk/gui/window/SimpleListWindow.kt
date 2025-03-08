package ovo.xsvf.izmk.gui.window

import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.widget.AbstractWidget

class SimpleListWindow(private val widgets: MutableList<AbstractWidget>, title: String = ""): AbstractWindow(
    title, 50f, 50f, 300f, 400f,
) {
    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()

    private val window = DraggableWindow(x, y, width, height)
    private val padding = 35f

    override fun draw(mouseX: Float, mouseY: Float, partialTicks: Float) {
        window.update(mouseX, mouseY)

        fontMulti.addText(title,
            window.x.toFloat() + 5f,
            window.y.toFloat() + 5f,
            ColorRGB.WHITE,
            false, 2f
        )

        // 绘制列表背景
        rectMulti.addRect(window.x.toFloat(), window.y.toFloat(),
            window.width.toFloat(), window.height.toFloat(),
            ColorRGB(0.15f, 0.15f, 0.15f)
        )

        var offsetY = window.y + padding

        widgets.forEach {
            it.draw(window.width, window.height, mouseX, mouseY,
                window.x + 5f, offsetY, fontMulti, rectMulti, partialTicks)
            offsetY += it.getHeight() + 5f
        }

        rectMulti.draw()
        fontMulti.draw()
    }

    override fun mouseClicked(buttonId: Int, mouseX: Float, mouseY: Float): Boolean {
        if (window.shouldDrag(mouseX, mouseY, padding)) {
            window.startDrag(mouseX, mouseY)
            return true
        }

        var offsetY = window.y + padding
        widgets.forEach {
            val moduleX = window.x + 5f
            val moduleY = offsetY

            if (RenderUtils2D.isMouseOver(mouseX, mouseY,
                    moduleX, moduleY,
                    (moduleX + window.width - 2 * 5f),
                    (moduleY + it.getHeight())
            )) {
                it.mouseClicked(mouseX, mouseY, buttonId == GLFW.GLFW_MOUSE_BUTTON_LEFT)
                return true
            }

            offsetY += it.getHeight() + 5f
        }

        return false
    }

    override fun keyPressed(keyCode: Int, scanCode: Int): Boolean {
        widgets.forEach {
            if (it.keyPressed(keyCode, scanCode)) {
                return true
            }
        }
        return false
    }

    override fun mouseReleased(buttonId: Int, mouseX: Float, mouseY: Float): Boolean {
        var offsetY = window.y + padding

        widgets.forEach {
            if (it.mouseReleased(mouseX, mouseY, buttonId == GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
                return true
            }
            offsetY += it.getHeight() + 5f
        }
        return window.stopDrag()
    }
}
