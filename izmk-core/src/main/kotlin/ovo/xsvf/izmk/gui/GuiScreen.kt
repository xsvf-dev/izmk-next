package ovo.xsvf.izmk.gui

import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import ovo.xsvf.izmk.IZMK
import ovo.xsvf.izmk.event.EventBus
import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent

/**
 * @author LangYa466
 * @since 2/27/2025
 */
abstract class GuiScreen(private val name: String) {
    private var mouseX = 0
    private var mouseY = 0

    private var screen: Screen? = null
    private var tempScreen: GuiScreen? = null

    val mc by lazy { IZMK.mc }

    open fun drawScreen(mouseX: Int, mouseY: Int, partialTicks: Float) {}
    open fun mouseClicked(buttonID: Int, mouseX: Double, mouseY: Double) {}
    open fun mouseReleased(buttonID: Int, mouseX: Double, mouseY: Double) {}

    fun openScreen(lastScreen: GuiScreen?) {
        tempScreen = lastScreen
        if (screen == null) {
            screen = object : Screen(Component.literal("izmk-$name")) {
                override fun shouldCloseOnEsc() = true
                override fun isPauseScreen() = false

                override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
                    mouseX = pMouseX
                    mouseY = pMouseY
                }

                override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
                    mouseClicked(pButton, pMouseX, pMouseY)
                    return super.mouseClicked(pMouseX, pMouseY, pButton)
                }

                override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
                    mouseReleased(pButton, pMouseX, pMouseY)
                    return super.mouseReleased(pMouseX, pMouseY, pButton)
                }

                override fun onClose() {
                    super.onClose()
                    closeScreen()
                }

                @EventTarget(priority = 1000)
                private fun onRender2D(event: Render2DEvent) {
                    drawScreen(mouseX, mouseY, event.partialTick)
                }
            }
        }
        EventBus.register(screen!!)
        mc.setScreen(screen)
    }

    fun closeScreen() {
        EventBus.unregister(screen!!)
        if (tempScreen != null) tempScreen!!.openScreen(null)
        else mc.setScreen(null)
    }
}
