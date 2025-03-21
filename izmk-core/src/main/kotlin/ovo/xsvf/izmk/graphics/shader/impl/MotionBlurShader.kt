package ovo.xsvf.izmk.graphics.shader.impl

import com.mojang.blaze3d.systems.RenderSystem
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.IZMK.mc
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.ResolutionUpdateEvent
import ovo.xsvf.izmk.graphics.buffer.FrameBuffer
import ovo.xsvf.izmk.graphics.shader.Shader
import ovo.xsvf.izmk.module.impl.MotionBlur

object MotionBlurShader: Shader(
    vertShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/effect/MotionBlur.vert",
    fragShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/effect/MotionBlur.frag",
) {
    private val cameraPosition = Vector3f()
    private val previousCameraPosition = Vector3f()
    private val gbufferProjection = Matrix4f()
    private val gbufferPreviousProjection = Matrix4f()
    private val gbufferProjectionInverse = Matrix4f()
    private val gbufferModelView = Matrix4f()
    private val gbufferPreviousModelView = Matrix4f()
    private val gbufferModelViewInverse = Matrix4f()
    val depthAttachment: Int
    val depthBuffer: FrameBuffer

    private val viewWidthLoc = glGetUniformLocation(id, "viewWidth")
    private val viewHeightLoc = glGetUniformLocation(id, "viewHeight")
    private val aspectRatioLoc = glGetUniformLocation(id, "aspectRatio")
    private val strengthLoc = glGetUniformLocation(id, "strength")
    private val cameraPositionLoc = glGetUniformLocation(id, "cameraPosition")
    private val previousCameraPositionLoc = glGetUniformLocation(id, "previousCameraPosition")
    private val gbufferPreviousProjectionLoc = glGetUniformLocation(id, "gbufferPreviousProjection")
    private val gbufferProjectionInverseLoc = glGetUniformLocation(id, "gbufferProjectionInverse")
    private val gbufferModelViewLoc = glGetUniformLocation(id, "gbufferModelView")
    private val gbufferPreviousModelViewLoc = glGetUniformLocation(id, "gbufferPreviousModelView")
    private val gbufferModelViewInverseLoc = glGetUniformLocation(id, "gbufferModelViewInverse")
    private val colorTex0Loc = glGetUniformLocation(id, "colortex0")
    private val depthTex0Loc = glGetUniformLocation(id, "depthtex0")

    init {
        EventBus.register(this)

        depthBuffer = FrameBuffer()
        depthAttachment = depthBuffer.depthStencilAtt
    }

    @EventTarget
    fun onFrameBufferResize(event: ResolutionUpdateEvent) {
        depthBuffer.resize()
    }

    fun copyDepthBuf() {
        if (!MotionBlur.enabled) return

        val frameBuf = mc.mainRenderTarget

        glBlitNamedFramebuffer(
            frameBuf.frameBufferId, depthBuffer.id,
            0, 0, frameBuf.width, frameBuf.height,
            0, 0, frameBuf.width, frameBuf.height,
            GL_DEPTH_BUFFER_BIT, GL_NEAREST
        )
    }
    
    private fun updateMatrix(modelView: Matrix4f, projection: Matrix4f) {
        gbufferPreviousModelView.set(gbufferModelView)
        gbufferPreviousProjection.set(gbufferProjection)
        gbufferModelView.set(modelView)
        gbufferProjection.set(projection)
        gbufferProjectionInverse.set(projection).invert()
        gbufferModelViewInverse.set(modelView).invert()

        val cameraPos = mc.gameRenderer.mainCamera.position
        previousCameraPosition.set(cameraPosition)
        cameraPosition.set(cameraPos.x, cameraPos.y, cameraPos.z)

        val viewWidth = depthBuffer.width.toFloat()
        val viewHeight = depthBuffer.height.toFloat()

        float1(viewWidthLoc, viewWidth)
        float1(viewHeightLoc, viewHeight)
        float1(aspectRatioLoc, viewWidth / viewHeight)
        float1(strengthLoc, MotionBlur.strength)

        vec3f(cameraPositionLoc, cameraPosition)
        vec3f(previousCameraPositionLoc, previousCameraPosition)

        matrix4f(gbufferPreviousProjectionLoc, gbufferPreviousProjection)
        matrix4f(gbufferProjectionInverseLoc, gbufferProjectionInverse)

        matrix4f(gbufferModelViewLoc, gbufferModelView)
        matrix4f(gbufferPreviousModelViewLoc, gbufferPreviousModelView)
        matrix4f(gbufferModelViewInverseLoc, gbufferModelViewInverse)
    }

    override fun default() {
        updateMatrix(RenderSystem.getModelViewMatrix(), RenderSystem.getProjectionMatrix())
        int1(colorTex0Loc, 0)
        int1(depthTex0Loc, 1)
    }

}