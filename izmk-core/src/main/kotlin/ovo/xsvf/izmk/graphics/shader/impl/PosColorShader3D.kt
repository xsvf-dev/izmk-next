package ovo.xsvf.izmk.graphics.shader.impl

import ovo.xsvf.izmk.graphics.matrix.MatrixStack
import ovo.xsvf.izmk.graphics.shader.Shader
import org.lwjgl.opengl.GL20.glGetUniformLocation
import ovo.xsvf.izmk.IZMK

object PosColorShader3D: Shader(
    vertShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/general/PosColor3D.vert",
    fragShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/general/PosColor.frag"
) {

    private val matrixLocation = glGetUniformLocation(id, "MVPMatrix")

    override fun default() {
        set(matrixLocation, MatrixStack.peek().mvpMatrix)
    }

}