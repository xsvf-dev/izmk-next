package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.module.Module

object MotionBlur : Module(
    name = "motion-blur",
) {
    val strength by setting("strength", 3.0f, 0.0f..10.0f)

    @EventTarget(priority = Int.MAX_VALUE)
    private fun onRender2D(event: Render2DEvent) {
//        MotionBlurShader.copyDepthBuf()
//
//        GlHelper.depth = true
//        val fb = mc.mainRenderTarget
//        glActiveTexture(GL_TEXTURE0)
//        glBindTexture(GL_TEXTURE_2D, fb.colorTextureId)
//        glActiveTexture(GL_TEXTURE1)
//        GlHelper.bindTexture(1, MotionBlurShader.depthAttachment)
//        glBindFramebuffer(GL_FRAMEBUFFER, fb.frameBufferId)
//
//        VertexBufferObjects.MotionBlur.drawArrays(GL_TRIANGLE_FAN) {
//            vertex(0.0f, 0.0f)
//            vertex(1.0f, 0.0f)
//            vertex(1.0f, 1.0f)
//            vertex(0.0f, 1.0f)
//        }
//
//        glBindTexture(GL_TEXTURE_2D, 0)
//        glActiveTexture(GL_TEXTURE0)
//        glBindTexture(GL_TEXTURE_2D, 0)
    }

}