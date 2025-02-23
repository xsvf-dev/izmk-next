package ovo.xsvf.izmk.graphics.multidraw

import org.lwjgl.opengl.GL46.*
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.buffer.multiDrawArrays
import ovo.xsvf.izmk.graphics.color.ColorRGB

class RectMultiDraw {

    private val rect = mutableListOf<Rect>()

    data class Rect(
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val color: ColorRGB,
    )

    fun addRect(x: Float, y: Float, width: Float, height: Float, color: ColorRGB) {
        rect.add(Rect(x, y, width, height, color))
    }

    fun draw() {
        VertexBufferObjects.PosColor2D.multiDrawArrays(GL_TRIANGLE_STRIP, IntArray(rect.size) { 4 }) {
            rect.forEach { rect ->
                val startX = rect.x
                val startY = rect.y
                val endX = startX + rect.width
                val endY = startY + rect.height

                vertex(endX, startY, rect.color)
                vertex(startX, startY, rect.color)
                vertex(endX, endY, rect.color)
                vertex(startX, endY, rect.color)
            }
        }
        rect.clear()
    }

    fun clear() {
        rect.clear()
    }

}