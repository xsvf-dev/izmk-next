package ovo.xsvf.izmk.module.impl

import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render3DEvent
import ovo.xsvf.izmk.graphics.GlHelper
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.buffer.drawArrays
import ovo.xsvf.izmk.graphics.shader.impl.MotionBlurShader
import ovo.xsvf.izmk.module.Module

object MotionBlur : Module(
    name = "motion-blur",
) {
    val strength by setting("strength", 3.0f, 0.0f..10.0f)

    @EventTarget(priority = Int.MAX_VALUE)
    private fun onRender3D(e: Render3DEvent) {
        GlHelper.depth = false

        GlHelper.bindTexture(0, MotionBlurShader.currentTex.id)
        GlHelper.bindTexture(1, MotionBlurShader.prevTex.id)

        VertexBufferObjects.MotionBlur.drawArrays(GL_TRIANGLE_STRIP) {
            vertex(1f, 0f)
            vertex(0f, 0f)
            vertex(1f, 1f)
            vertex(0f, 1f)
        }

        GlHelper.bindTexture(0, 0)
        GlHelper.bindTexture(1, 0)

        glCopyTextureSubImage2D(MotionBlurShader.prevTex.id, 0, 0, 0, 0, 0,
            mc.mainRenderTarget.width, mc.mainRenderTarget.height
        )
    }

}
