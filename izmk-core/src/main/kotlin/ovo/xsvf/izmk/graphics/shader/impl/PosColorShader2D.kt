package ovo.xsvf.izmk.graphics.shader.impl

import org.lwjgl.opengl.GL20.glGetUniformLocation
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.graphics.matrix.MatrixStack
import ovo.xsvf.izmk.graphics.shader.Shader

object PosColorShader2D: Shader(
    vertShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/general/PosColor2D.vert",
    fragShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/general/PosColor.frag"
) {

    private val matrixLocation = glGetUniformLocation(id, "MVPMatrix")

    override fun default() {
        matrix4f(matrixLocation, MatrixStack.peek().mvpMatrix)
    }

}