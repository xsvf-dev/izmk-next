package ovo.xsvf.izmk.graphics

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.GuiGraphics
import org.lwjgl.opengl.GL46
import ovo.xsvf.izmk.event.impl.Render2DEvent

object GRenderSystem{
    private val poseStack = PoseStack()

    fun onRender2D(guiGraphics: GuiGraphics?, partialTick: Float) {
        // renderPre();
        poseStack.pushPose()
        ovo.xsvf.izmk.event.EventBus.call(Render2DEvent(guiGraphics, partialTick))
        poseStack.popPose()

        // renderPost();
    }

    private fun renderPre() {
        preAttrib()
    }

    private fun renderPost() {
        postAttrib()
    }

    /*
     * Gl States
     * Save and restore the last state of the OpenGL context.
     */
    private var vaoLast = -1
    private var vboLast = -1
    private var eboLast = -1
    private var programLast = -1

    private fun preAttrib() {
        vaoLast = GL46.glGetInteger(GL46.GL_VERTEX_ARRAY_BINDING)
        vboLast = GL46.glGetInteger(GL46.GL_ARRAY_BUFFER_BINDING)
        eboLast = GL46.glGetInteger(GL46.GL_ELEMENT_ARRAY_BUFFER_BINDING)
        programLast = GL46.glGetInteger(GL46.GL_CURRENT_PROGRAM)
    }

    private fun postAttrib() {
        GL46.glBindVertexArray(vaoLast)
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vboLast)
        GL46.glBindBuffer(GL46.GL_ELEMENT_ARRAY_BUFFER, eboLast)
        GL46.glUseProgram(programLast)
    }
}
