package ovo.xsvf.izmk.graphics.multidraw

import dev.luna5ama.kmogus.Arr
import dev.luna5ama.kmogus.asMutable
import org.lwjgl.opengl.GL46.GL_TRIANGLE_STRIP
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.buffer.multiDrawArrays
import ovo.xsvf.izmk.graphics.color.ColorRGB

class PosColor2DMultiDraw {

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
        countArr.pos = 0L
        if (countArr.len < rect.size * 4) countArr.realloc(rect.size * 4L, false)
        repeat(rect.size) {
            countArr.ptr[0] = 4
            countArr.pos += 4L
        }
        countArr.flip()

        countArr.pos = 0L
        if (firstArr.len < rect.size * 4) firstArr.realloc(rect.size * 4L, false)
        var offset = 0
        repeat(rect.size) {
            firstArr.ptr[0] = offset
            firstArr.pos += 4L
            offset += 4
        }
        firstArr.flip()

        VertexBufferObjects.PosColor2D.multiDrawArrays(GL_TRIANGLE_STRIP, firstArr, countArr, rect.size) {
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

    companion object {
        private val countArr = Arr.malloc(0).asMutable()
        private val firstArr = Arr.malloc(0).asMutable()
    }

}