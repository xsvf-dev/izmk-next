package ovo.xsvf.izmk.graphics.utils

import org.lwjgl.opengl.GL45.GL_STATIC_DRAW
import org.lwjgl.opengl.GL45.glNamedBufferData
import ovo.xsvf.izmk.graphics.buffer.ElementBufferObject

object RenderUtils {

    val rectEbo = ElementBufferObject()

    init {
        // Init RenderUtils2D and RenderUtils3D
        RenderUtils2D
        RenderUtils3D

        initRectEbo()
    }

    private fun initRectEbo() {
        val rectIndices = intArrayOf(0, 1, 2, 2, 3, 1)

        rectEbo.indicesCount = rectIndices.size
        rectEbo.vertexCount = 4
        glNamedBufferData(
            rectEbo.eboId,
            /*
             * 6 indices for a rectangle
             * 2 triangles, 3 vertices per triangle
             * 0   1
             * XXXXX
             * X  XX
             * X X X
             * XX  X
             * XXXXX
             * 2   3
             */
            rectIndices,
            GL_STATIC_DRAW
        )
    }

}