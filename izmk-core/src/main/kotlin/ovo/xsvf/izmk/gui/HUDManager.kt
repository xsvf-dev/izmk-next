package ovo.xsvf.izmk.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.ChatScreen
import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventListener
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.gui.impl.NeneHud
import ovo.xsvf.izmk.mod.hud.HUD
import java.util.concurrent.ConcurrentHashMap

/**
 * @author xiaojiang233
 * @since 2025/2/22
 */
object HUDManager {
    private val hudMap = ConcurrentHashMap<String, HUD>()
    private val mc by lazy { IZMK.mc }

    private var draggingHUD: HUD? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    fun init() {
        registerHUD(NeneHud())

        IZMK.logger.info("HUD map size: ${hudMap.size}")
    }

    private fun registerHUD(hud: HUD) {
        hudMap[hud::class.java.simpleName] = hud
    }

    @EventListener
    fun onRender2d(event: Render2DEvent) {
        val window = mc.window.window
        val xpos = DoubleArray(1)
        val ypos = DoubleArray(1)
        GLFW.glfwGetCursorPos(window, xpos, ypos)
        val mouseX = xpos[0].toFloat() / mc.window.guiScale.toFloat()
        val mouseY = ypos[0].toFloat() / mc.window.guiScale.toFloat()

        if (mc.screen is ChatScreen) {
            onMouseInput(mouseX, mouseY, event.guiGraphics.pose())
        }

        hudMap.values
            .filter { it.isEnabled }
            .forEach { it.render(event) }
    }

    private fun onMouseInput(mouseX: Float, mouseY: Float, stack: PoseStack) {
        val window = mc.window.window
        val isMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

        if (isMousePressed) {
            if (draggingHUD == null) {
                hudMap.values.firstOrNull { it.isEnabled && it.isMouseOver(mouseX, mouseY) }
                    ?.let { hud ->
                        drawHUDBorder(stack, hud)
                        draggingHUD = hud
                        dragOffsetX = mouseX - hud.x
                        dragOffsetY = mouseY - hud.y
                    }
            } else {
                draggingHUD?.x = mouseX - dragOffsetX
                draggingHUD?.y = mouseY - dragOffsetY
            }
        } else {
            draggingHUD = null
        }
    }

    private fun drawHUDBorder(stack: PoseStack, hud: HUD) {
        // TODO: Implement border drawing logic
    }

    fun enableHUD(name: String) = hudMap[name]?.let { it.isEnabled = true }

    fun disableHUD(name: String) = hudMap[name]?.let { it.isEnabled = false }

    fun getHUD(name: String) = hudMap.values.find { it.name.equals(name, ignoreCase = true) }

    fun getHUD(cls: Class<out HUD>) = hudMap.values.find { it::class.java == cls }

    val hUDs: Collection<HUD> get() = hudMap.values
}