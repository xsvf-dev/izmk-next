package ovo.xsvf.izmk.graphics.utils

import net.minecraft.client.Minecraft
import net.minecraft.world.phys.AABB
import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.opengl.GL45.*
import ovo.xsvf.izmk.graphics.GlHelper
import ovo.xsvf.izmk.graphics.buffer.VertexBufferObjects
import ovo.xsvf.izmk.graphics.buffer.drawArrays
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.util.math.vectors.Vec3f

object RenderUtils3D {

    var lastMvpMatrix: Matrix4f = Matrix4f(); private set
    var lastPosMatrix: Matrix4f = Matrix4f(); private set

    private val viewport = IntArray(4)

    init {

        glGetIntegerv(GL_VIEWPORT, viewport)

        // FIXME: EventBus hasn't finished yet
//        listener<Render3DEvent>(alwaysListening = true, priority = Int.MIN_VALUE) {
//            lastMvpMatrix = Matrix4f(MatrixStack.peek().mvpMatrix)
//            lastPosMatrix = Matrix4f(MatrixStack.peek().positionMatrix)
//        }
//
//        listener<WindowResizeEvent>(alwaysListening = true) {
//            glGetIntegerv(GL_VIEWPORT, viewport)
//        }

    }

    /**
     * Convert a vector in world space to screen space.
     */
    fun worldSpaceToScreenSpace(pos: Vec3f): Vec3f {
        val mc = Minecraft.getInstance()

        val camera = mc.gameRenderer.mainCamera
        val displayHeight = mc.window.height

        val target = Vector3f()

        val deltaX = pos.x - camera.position.x
        val deltaY = pos.y - camera.position.y
        val deltaZ = pos.z - camera.position.z

        val transformedCoordinates =
            Vector4f(deltaX.toFloat(), deltaY.toFloat(), deltaZ.toFloat(), 1f).mul(lastPosMatrix)

        lastMvpMatrix.project(
            transformedCoordinates.x(),
            transformedCoordinates.y(),
            transformedCoordinates.z(),
            viewport,
            target
        )

        return Vec3f(
            target.x / mc.window.guiScale.toFloat(),
            (displayHeight - target.y) / mc.window.guiScale.toFloat(), target.z
        )
    }

    fun drawBoxOutline(bb: AABB, color: ColorRGB, lineWidth: Float = 1f, height: Double = 1.0) {
        val mc = Minecraft.getInstance()
        val camera = mc.gameRenderer.mainCamera
        val box = bb.contract(0.0, 1 - height, 0.0)
        val minX = (box.minX - camera.position.x).toFloat()
        val minY = (box.minY - camera.position.y).toFloat()
        val minZ = (box.minZ - camera.position.z).toFloat()
        val maxX = (box.maxX - camera.position.x).toFloat()
        val maxY = (box.maxY - camera.position.y).toFloat()
        val maxZ = (box.maxZ - camera.position.z).toFloat()

        // fixme: line width not working
        GlHelper.lineWidth = lineWidth

        VertexBufferObjects.PosColor3D.drawArrays(GL_LINES) {
            vertex(minX, maxY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(minX, maxY, maxZ, color)
            vertex(minX, maxY, maxZ, color)
            vertex(minX, maxY, minZ, color)

            vertex(minX, minY, minZ, color)
            vertex(maxX, minY, minZ, color)
            vertex(maxX, minY, minZ, color)
            vertex(maxX, minY, maxZ, color)
            vertex(maxX, minY, maxZ, color)
            vertex(minX, minY, maxZ, color)
            vertex(minX, minY, maxZ, color)
            vertex(minX, minY, minZ, color)

            vertex(minX, minY, minZ, color)
            vertex(minX, maxY, minZ, color)
            vertex(maxX, minY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(maxX, minY, maxZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(minX, minY, maxZ, color)
            vertex(minX, maxY, maxZ, color)
        }
    }

    fun drawFilledBox(bb: AABB, color: ColorRGB, height: Double = 1.0) {
        val mc = Minecraft.getInstance()
        val camera = mc.gameRenderer.mainCamera
        val box = bb.contract(0.0, 1 - height, 0.0)
        val minX = (box.minX - camera.position.x).toFloat()
        val minY = (box.minY - camera.position.y).toFloat()
        val minZ = (box.minZ - camera.position.z).toFloat()
        val maxX = (box.maxX - camera.position.x).toFloat()
        val maxY = (box.maxY - camera.position.y).toFloat()
        val maxZ = (box.maxZ - camera.position.z).toFloat()

        VertexBufferObjects.PosColor3D.drawArrays(GL_TRIANGLES) {
            vertex(minX, minY, minZ, color)
            vertex(maxX, minY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(minX, minY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(minX, maxY, minZ, color)

            vertex(minX, minY, minZ, color)
            vertex(maxX, minY, minZ, color)
            vertex(maxX, minY, maxZ, color)
            vertex(minX, minY, minZ, color)
            vertex(maxX, minY, maxZ, color)
            vertex(minX, minY, maxZ, color)

            vertex(maxX, minY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(maxX, minY, minZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(maxX, minY, maxZ, color)

            vertex(minX, minY, maxZ, color)
            vertex(maxX, minY, maxZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(minX, minY, maxZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(minX, maxY, maxZ, color)

            vertex(minX, minY, minZ, color)
            vertex(minX, maxY, minZ, color)
            vertex(minX, maxY, maxZ, color)
            vertex(minX, minY, minZ, color)
            vertex(minX, maxY, maxZ, color)
            vertex(minX, minY, maxZ, color)

            vertex(minX, maxY, minZ, color)
            vertex(maxX, maxY, minZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(minX, maxY, minZ, color)
            vertex(maxX, maxY, maxZ, color)
            vertex(minX, maxY, maxZ, color)
        }
    }

}