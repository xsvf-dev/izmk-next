package ovo.xsvf.izmk.graphics.buffer

import net.minecraft.client.Minecraft
import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.graphics.GlObject

class FrameBuffer : GlObject {

    override var id: Int = glCreateFramebuffers()
    var colorAtt = glCreateTextures(GL_TEXTURE_2D); private set
    var depthStencilAtt = glCreateTextures(GL_TEXTURE_2D); private set

    var width: Int = 0; private set
    var height: Int = 0; private set

    init {
        val mc = Minecraft.getInstance()
        allocateFrameBuffer(mc.window.width, mc.window.height)
    }

    private fun allocateFrameBuffer(width: Int, height: Int) {
        this.width = width
        this.height = height

        id = glCreateFramebuffers()
        colorAtt = glCreateTextures(GL_TEXTURE_2D)
        depthStencilAtt = glCreateTextures(GL_TEXTURE_2D)

        /* Color Attachment */
        glTextureStorage2D(colorAtt, 1, GL_RGBA8, width, height)
        glTextureParameteri(colorAtt, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTextureParameteri(colorAtt, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTextureParameteri(colorAtt, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTextureParameteri(colorAtt, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glNamedFramebufferTexture(id, GL_COLOR_ATTACHMENT0, colorAtt, 0)

        /* Depth Attachment */
        glTextureStorage2D(depthStencilAtt, 1, GL_DEPTH24_STENCIL8, width, height)
        glTextureParameteri(depthStencilAtt, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTextureParameteri(depthStencilAtt, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTextureParameteri(depthStencilAtt, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTextureParameteri(depthStencilAtt, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glNamedFramebufferTexture(id, GL_DEPTH_STENCIL_ATTACHMENT, depthStencilAtt, 0)

        if (glCheckNamedFramebufferStatus(id, GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw IllegalStateException("Could not create frame buffer")
        }
    }

    fun resize() {
        glDeleteFramebuffers(id)
        glDeleteTextures(colorAtt)
        glDeleteTextures(depthStencilAtt)

        val mc = Minecraft.getInstance()
        allocateFrameBuffer(mc.window.width, mc.window.height)
    }

    override fun bind() {
        glBindFramebuffer(GL_FRAMEBUFFER, id)
    }

    override fun unbind() {
        glBindFramebuffer(GL_FRAMEBUFFER, 0)
    }

    override fun delete() {
        glDeleteFramebuffers(id)
        glDeleteTextures(colorAtt)
        glDeleteTextures(depthStencilAtt)
    }

}