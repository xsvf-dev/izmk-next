package ovo.xsvf.izmk.module.hud

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.ChatScreen
import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventListener
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.utils.RenderUtils
import ovo.xsvf.izmk.mod.hud.HUD
import ovo.xsvf.izmk.module.hud.impl.NeneHud
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.ConcurrentHashMap

/**
 * @author xiaojiang233
 * @since 2025/2/22
 */
object HUDManager {
    private val hudMap = ConcurrentHashMap<String, HUD>()
    private var draggingHUD: HUD? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private val mc = IZMK.mc

    fun init(classes: Array<Class<*>>) {
        registerHUD(NeneHud())

        for (clazz in classes) {
            if (HUD::class.java.isAssignableFrom(clazz) && clazz != HUD::class.java) {
                loadHUD(clazz)
            }
        }
        IZMK.logger.info("HUD map size: ${hudMap.size}")
    }

    private fun loadHUD(clazz: Class<*>) {
        val name = clazz.name
        IZMK.logger.info("Loading HUD: $name")
        try {
            val hud = clazz.getDeclaredConstructor().newInstance() as HUD
            registerHUD(hud)
        } catch (e: Exception) {
            handleHUDLoadException(name, e)
        }
    }

    private fun handleHUDLoadException(name: String, e: Exception) {
        val message = when (e) {
            is NoSuchMethodException -> "does not have a default constructor."
            is IllegalAccessException -> "constructor is not accessible."
            is InstantiationException -> "cannot be instantiated."
            is InvocationTargetException -> "constructor threw an exception: ${e.targetException.message}"
            else -> "unexpected error occurred."
        }
        IZMK.logger.error("HUD $name $message", e)
        throw e
    }

    fun registerHUD(hud: HUD) {
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
            checkMouseInput(mouseX, mouseY, event.guiGraphics.pose())
        }

        hudMap.values
            .filter { it.isEnabled }
            .forEach { it.render(event) }
    }

    private fun checkMouseInput(mouseX: Float, mouseY: Float, stack: PoseStack) {
        val window = mc.window.window
        val isMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS

        if (isMousePressed) {
            val currentDraggingHUD = draggingHUD
            if (currentDraggingHUD == null) {
                hudMap.values
                    .filter { it.isEnabled && it.isMouseOver(mouseX, mouseY) }
                    .firstOrNull()?.let { hud ->
                        drawHUDBorder(stack, hud)
                        draggingHUD = hud
                        dragOffsetX = mouseX - hud.x
                        dragOffsetY = mouseY - hud.y
                    }
            } else {
                currentDraggingHUD.x = mouseX - dragOffsetX
                currentDraggingHUD.y = mouseY - dragOffsetY
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