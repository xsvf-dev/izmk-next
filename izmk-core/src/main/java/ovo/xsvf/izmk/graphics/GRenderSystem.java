package ovo.xsvf.izmk.graphics;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Getter;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL46;
import ovo.xsvf.izmk.misc.MinecraftInstance;

public class GRenderSystem implements MinecraftInstance {
    private static GRenderSystem INSTANCE;

    @Getter
    private final PoseStack poseStack = new PoseStack();

    public static GRenderSystem getInstance() {
        if (INSTANCE == null) INSTANCE = new GRenderSystem();
        return INSTANCE;
    }

    private GRenderSystem() {
    }

    public void onRender2D(GuiGraphics guiGraphics, float partialTick) {
        renderPre();
        {
            poseStack.pushPose();
            {
                // TODO: Post render event here.
                guiGraphics.drawString(mc.font, "Hello, World!", 10, 10, 0xFFFFFFFF);
            }
            poseStack.popPose();
        }
        renderPost();
    }

    private void renderPre() {
        preAttrib();
    }

    private void renderPost() {
        postAttrib();
    }

    /*
     * Gl States
     * Save and restore the last state of the OpenGL context.
     */
    private int vaoLast = -1;
    private int vboLast = -1;
    private int eboLast = -1;
    private int programLast = -1;

    private void preAttrib() {
        vaoLast = GL46.glGetInteger(GL46.GL_VERTEX_ARRAY_BINDING);
        vboLast = GL46.glGetInteger(GL46.GL_ARRAY_BUFFER_BINDING);
        eboLast = GL46.glGetInteger(GL46.GL_ELEMENT_ARRAY_BUFFER_BINDING);
        programLast = GL46.glGetInteger(GL46.GL_CURRENT_PROGRAM);
    }

    private void postAttrib() {
        GL46.glBindVertexArray(vaoLast);
        GL46.glBindBuffer(GL46.GL_ARRAY_BUFFER, vboLast);
        GL46.glBindBuffer(GL46.GL_ELEMENT_ARRAY_BUFFER, eboLast);
        GL46.glUseProgram(programLast);
    }

}
