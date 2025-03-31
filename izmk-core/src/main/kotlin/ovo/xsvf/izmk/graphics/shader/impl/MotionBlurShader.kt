package ovo.xsvf.izmk.graphics.shader.impl

import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.IZMK.mc
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.ResolutionUpdateEvent
import ovo.xsvf.izmk.graphics.shader.Shader
import ovo.xsvf.izmk.graphics.texture.Texture
import ovo.xsvf.izmk.module.impl.MotionBlur

object MotionBlurShader: Shader(
    vertShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/effect/MotionBlur.vert",
    fragShaderPath = "${IZMK.ASSETS_DIRECTORY}/shader/effect/MotionBlur.frag",
) {

    private val prevSamplerLoc = glGetUniformLocation(id, "u_PrevSampler")
    private val currentSamplerLoc = glGetUniformLocation(id, "u_CurrentSampler")
    private val strengthLoc = glGetUniformLocation(id, "u_Strength")

    var prevTex = Texture().apply {
        glTextureStorage2D(id, 1, GL_RGBA8, mc.mainRenderTarget.width, mc.mainRenderTarget.height)
    }; private set

    var currentTex = Texture().apply {
        glTextureStorage2D(id, 1, GL_RGBA8, mc.mainRenderTarget.width, mc.mainRenderTarget.height)
    }; private set

    init {
        EventBus.register(this)
    }

    fun saveTex() {
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0)
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0)
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0)

        // Copy frame buffer to current texture DSA
        glCopyTextureSubImage2D(currentTex.id, 0, 0, 0, 0, 0, mc.mainRenderTarget.width, mc.mainRenderTarget.height)
    }

    @EventTarget
    fun onResize(event: ResolutionUpdateEvent) {
        prevTex.delete()
        prevTex = Texture().apply {
            glTextureStorage2D(id, 1, GL_RGBA8, event.width, event.height)
        }

        currentTex.delete()
        currentTex = Texture().apply {
            glTextureStorage2D(id, 1, GL_RGBA8, event.width, event.height)
        }
    }

    override fun default() {
        int1(prevSamplerLoc, 0)
        int1(currentSamplerLoc, 1)
        float1(strengthLoc, MotionBlur.strength)
    }

}