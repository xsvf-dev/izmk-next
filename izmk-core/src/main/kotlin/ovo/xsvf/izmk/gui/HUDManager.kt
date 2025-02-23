package ovo.xsvf.izmk.gui

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.gui.screens.ChatScreen
import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.easing.AnimationFlag
import ovo.xsvf.izmk.graphics.easing.Easing
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.gui.impl.NeneHud
import java.util.concurrent.ConcurrentHashMap

/**
 * @author xiaojiang233
 * @since 2025/2/22
 */
object HUDManager {
    private val hudMap = ConcurrentHashMap<String, HUD>()
    private val fadeAnimations = ConcurrentHashMap<String, AnimationFlag>()
    private val mc by lazy { IZMK.mc }

    private var draggingHUD: HUD? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    fun init() {
        registerHUD(NeneHud())
        hudMap["NeneHud"]?.enabled = true
    }

    private fun registerHUD(hud: HUD) {
        hudMap[hud::class.java.simpleName] = hud
    }

    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        val window = mc.window.window
        val xpos = DoubleArray(1)
        val ypos = DoubleArray(1)

        GLFW.glfwGetCursorPos(window, xpos, ypos)
        val mouseX = xpos[0].toFloat() / mc.window.guiScale.toFloat()
        val mouseY = ypos[0].toFloat() / mc.window.guiScale.toFloat()

        hudMap.values.filter { it.enabled }.forEach { hud ->
            hud.render(event)

            if (mc.screen is ChatScreen) {
                val fadeAnimation = fadeAnimations.getOrPut(hud.name) {
                    AnimationFlag(Easing.OUT_CUBIC, 300f)
                }

                val targetFade = if (hud.isMouseOver(mouseX, mouseY)) 1f else 0f
                fadeAnimation.update(targetFade)
                val currentFade = fadeAnimation.get()
                if (currentFade > 0) {
                    drawHUDBorder(event.guiGraphics.pose(), hud, currentFade)
                }
            }
        }

        if (mc.screen is ChatScreen) {
            onMouseInput(mouseX, mouseY, event.guiGraphics.pose())
        }
    }

    private fun onMouseInput(mouseX: Float, mouseY: Float, stack: PoseStack) {
        val window = mc.window.window
        val isMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
        val screenWidth = mc.window.guiScaledWidth.toFloat()
        val screenHeight = mc.window.guiScaledHeight.toFloat()

        if (isMousePressed) {
            if (draggingHUD == null) {
                hudMap.values.firstOrNull { it.enabled && it.isMouseOver(mouseX, mouseY) }
                    ?.let { hud ->
                        draggingHUD = hud
                        dragOffsetX = mouseX - hud.x
                        dragOffsetY = mouseY - hud.y
                    }
            } else {
                val newX = mouseX - dragOffsetX
                val newY = mouseY - dragOffsetY
                val hud = draggingHUD!!

                if (newX >= 0 && newX + hud.width <= screenWidth) {
                    hud.x = newX
                }
                if (newY >= 0 && newY + hud.height <= screenHeight) {
                    hud.y = newY
                }

                drawHUDBorder(stack, hud, 1f)
            }
        } else {
            draggingHUD = null
        }
    }

    private fun drawHUDBorder(stack: PoseStack, hud: HUD, alpha: Float) {
        val borderColor = ColorRGB(255, 255, 255, (150 * alpha).toInt())
        val outlineWidth = 1f

        RenderUtils2D.drawRectOutline(
            hud.x - outlineWidth,
            hud.y - outlineWidth,
            hud.width + (outlineWidth * 2),
            hud.height + (outlineWidth * 2),
            borderColor
        )
    }

    fun enableHUD(name: String) = hudMap[name]?.let { it.enabled = true }

    fun disableHUD(name: String) = hudMap[name]?.let { it.enabled = false }

    fun getHUD(name: String) = hudMap.values.find { it.name.equals(name, ignoreCase = true) }

    fun getHUD(cls: Class<out HUD>) = hudMap.values.find { it::class.java == cls }

    val hUDs: Collection<HUD> get() = hudMap.values
}