package ovo.xsvf.izmk.graphics.shader.impl

import org.lwjgl.opengl.ARBBindlessTexture.glProgramUniformHandleui64ARB
import org.lwjgl.opengl.GL45
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.graphics.matrix.MatrixStack
import ovo.xsvf.izmk.graphics.shader.Shader

// Only for sparse font mode
object FontShader: Shader(
    "${IZMK.ASSETS_DIRECTORY}/shader/general/FontRenderer.vert",
    "${IZMK.ASSETS_DIRECTORY}/shader/general/FontRenderer.frag",
) {

    private val matrixLocation = GL45.glGetUniformLocation(id, "u_MVPMatrix")
    private val samplerLocation = GL45.glGetUniformLocation(id, "u_Texture")

    override fun default() {
        set(matrixLocation, MatrixStack.peek().mvpMatrix)
        textureUnit?.let { glProgramUniformHandleui64ARB(id, samplerLocation, it) }
    }

    var textureUnit: Long? = 0L
}