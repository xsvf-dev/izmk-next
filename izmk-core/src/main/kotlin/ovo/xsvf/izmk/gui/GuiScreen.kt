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
open class GuiScreen(val name: String) {
    private var mouseX = 0f
    private var mouseY = 0f
    private var screen: Screen? = null

    val mc by lazy { IZMK.mc }

    open fun drawScreen(mouseX: Float, mouseY: Float, partialTicks: Float) {}
    open fun mouseClicked(buttonID: Int, mouseX: Float, mouseY: Float) {}
    open fun mouseReleased(buttonID: Int, mouseX: Float, mouseY: Float) {}
    open fun keyPressed(keyCode: Int, scanCode: Int): Boolean { return false }

    open fun shouldCloseOnEsc(): Boolean = true
    open fun onClose() {}

    fun openScreen() {
        if (screen == null) {
            screen = object : Screen(Component.literal("izmk-$name")) {
                override fun shouldCloseOnEsc() = true
                override fun isPauseScreen() = false

                override fun render(pGuiGraphics: GuiGraphics, pMouseX: Int, pMouseY: Int, pPartialTick: Float) {
                    mouseX = pMouseX.toFloat()
                    mouseY = pMouseY.toFloat()
                }

                override fun mouseClicked(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
                    mouseClicked(pButton, pMouseX.toFloat(), pMouseY.toFloat())
                    return super.mouseClicked(pMouseX, pMouseY, pButton)
                }

                override fun mouseReleased(pMouseX: Double, pMouseY: Double, pButton: Int): Boolean {
                    mouseReleased(pButton, pMouseX.toFloat(), pMouseY.toFloat())
                    return super.mouseReleased(pMouseX, pMouseY, pButton)
                }

                override fun keyPressed(pKeyCode: Int, pScanCode: Int, pModifiers: Int): Boolean {
                    return keyPressed(pKeyCode, pScanCode) ||
                            super.keyPressed(pKeyCode, pScanCode, pModifiers)
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
        screen?.let { EventBus.register(it) }
        mc.setScreen(screen)
    }

    fun closeScreen() {
        onClose()
        screen?.let { EventBus.unregister(it) }
        if (mc.screen?.title == screen?.title) mc.setScreen(null)
    }
}
