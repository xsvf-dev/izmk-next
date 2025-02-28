package ovo.xsvf.izmk.graphics.shader.impl

import org.lwjgl.opengl.GL45
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.graphics.matrix.MatrixStack
import ovo.xsvf.izmk.graphics.shader.Shader

object PosTexShader2D: Shader(
    "${IZMK.ASSETS_DIRECTORY}/shader/general/PosTex2D.vert",
    "${IZMK.ASSETS_DIRECTORY}/shader/general/PosTex2D.frag",
) {

    private val matrixLocation = GL45.glGetUniformLocation(id, "MVPMatrix")
    private val samplerLocation = GL45.glGetUniformLocation(id, "u_Texture")

    override fun default() {
        set(matrixLocation, MatrixStack.peek().mvpMatrix)
        set(samplerLocation, 0)
    }
}