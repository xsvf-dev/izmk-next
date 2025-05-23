package ovo.xsvf.izmk.graphics.buffer

import dev.luna5ama.kmogus.Arr
import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.graphics.GlHelper
import ovo.xsvf.izmk.graphics.shader.Shader

open class VertexMode(
    val shader: Shader,
    protected val attribute: VertexAttribute,
    sizeFactor: Long = 4L,
) {
    val vbo = PersistentMappedVBO(attribute.stride, sizeFactor)
    private val vao = createVao(vbo, attribute)
    private var ebo = 0

    protected val arr get() = vbo.arr
    protected var vertexSize = 0

    private fun bindElementBuffer(ebo: Int) {
        if (this.ebo == ebo) return
        this.ebo = ebo
        glVertexArrayElementBuffer(vao, ebo)
    }

    fun drawArrays(shader: Shader, mode: Int) {
        if (vertexSize == 0) return

        shader.bind()
        shader.default()

        glVertexArrayVertexBuffer(vao, 0, vbo.id, this.vbo.offset * attribute.stride, attribute.stride)
        GlHelper.vertexArray = this.vao

        glDrawArrays(mode, 0, vertexSize)

        vbo.end()
        vertexSize = 0
    }

    fun drawElements(shader: Shader, ebo: ElementBufferObject, mode: Int) {
        if (vertexSize == 0) return

        shader.bind()
        shader.default()

        glVertexArrayVertexBuffer(vao, 0, vbo.id, vbo.offset * attribute.stride, attribute.stride)
        GlHelper.vertexArray = vao

        bindElementBuffer(ebo.eboId)
        glDrawElements(mode, ebo.indicesCount, GL_UNSIGNED_INT, 0)

        vbo.end()
        vertexSize = 0
    }

    fun multiDrawArrays(shader: Shader, first: Arr, counts: Arr, drawCount: Int, mode: Int) {
        if (vertexSize == 0) return

        shader.bind()
        shader.default()

        glVertexArrayVertexBuffer(vao, 0, vbo.id, this.vbo.offset * attribute.stride, attribute.stride)
        GlHelper.vertexArray = this.vao

        nglMultiDrawArrays(mode, first.ptr.address, counts.ptr.address, drawCount)

        vbo.end()
        vertexSize = 0
    }

    /* Vertex Array Object */
    private fun createVao(vbo: PersistentMappedVBO, vertexAttribute: VertexAttribute): Int {
        val vaoID = glCreateVertexArrays()
        vertexAttribute.apply(vaoID)
        glVertexArrayVertexBuffer(vaoID, 0, vbo.id, 0, vertexAttribute.stride)
        return vaoID
    }
}