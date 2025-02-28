package ovo.xsvf.izmk.module

import it.unimi.dsi.fastutil.objects.Object2ObjectAVLTreeMap
import net.minecraft.client.gui.screens.ChatScreen
import org.lwjgl.glfw.GLFW
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.easing.AnimationFlag
import ovo.xsvf.izmk.graphics.easing.Easing
import ovo.xsvf.izmk.graphics.utils.RenderUtils2D
import ovo.xsvf.izmk.module.impl.*
import java.util.concurrent.ConcurrentHashMap

object ModuleManager {
    val modulesMap = Object2ObjectAVLTreeMap<String, Module>()

    private val fadeAnimations = ConcurrentHashMap<String, AnimationFlag>()
    private val mc by lazy { IZMK.mc }

    private var draggingHUD: RenderableModule? = null
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f

    fun init() {
        addModule(NoHurtcam)
        addModule(FPSDisplay)
        addModule(RenderTest)
        addModule(OldAnimations)
        addModule(ClickGUI)

        /* render modules */
        addModule(TestHud)
        addModule(NeneHud)
        addModule(PotionStatus)
    }

    private fun addModule(module: Module) {
        modulesMap[module.name] = module
        module.onLoad()
    }

    operator fun get(name: String): Module? {
        return modulesMap[name]
    }

    inline fun <reified T: Module> get(): T {
        return modulesMap.values.first { it is T } as T
    }

    fun modules() : List<Module> {
        return modulesMap.values.toList()
    }
    
    /* render stuff */
    @EventTarget
    fun onRender2d(event: Render2DEvent) {
        val window = mc.window.window
        val xpos = DoubleArray(1)
        val ypos = DoubleArray(1)

        GLFW.glfwGetCursorPos(window, xpos, ypos)
        val mouseX = xpos[0].toFloat() / mc.window.guiScale.toFloat()
        val mouseY = ypos[0].toFloat() / mc.window.guiScale.toFloat()

        modules()
            .filterIsInstance<RenderableModule>()
            .filter { it.enabled }
            .forEach { hud ->
                hud.render(event)
    
                if (mc.screen is ChatScreen) {
                    val fadeAnimation = fadeAnimations.getOrPut(hud.name) {
                        AnimationFlag(Easing.OUT_CUBIC, 300f)
                    }
    
                    val targetFade = if (hud.isMouseOver(mouseX, mouseY)) 1f else 0f
                    fadeAnimation.update(targetFade)
                    val currentFade = fadeAnimation.get()
                    if (currentFade > 0) {
                        drawHUDBorder(hud, currentFade)
                    }
                }
            }

        if (mc.screen is ChatScreen) {
            onMouseInput(mouseX, mouseY)
        }
    }

    private fun onMouseInput(mouseX: Float, mouseY: Float) {
        val window = mc.window.window
        val isMousePressed = GLFW.glfwGetMouseButton(window, GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS
        val screenWidth = mc.window.guiScaledWidth.toFloat()
        val screenHeight = mc.window.guiScaledHeight.toFloat()

        if (isMousePressed) {
            if (draggingHUD == null) {
                modules()
                    .filterIsInstance<RenderableModule>()
                    .firstOrNull { it.enabled && it.isMouseOver(mouseX, mouseY) }
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

                drawHUDBorder(hud, 1f)
            }
        } else {
            draggingHUD = null
        }
    }

    private fun drawHUDBorder(hud: RenderableModule, alpha: Float) {
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
}
