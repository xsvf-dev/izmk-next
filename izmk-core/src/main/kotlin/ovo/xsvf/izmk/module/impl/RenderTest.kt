package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.PosColor2DMultiDraw
import ovo.xsvf.izmk.module.Module

object RenderTest: Module("render-test") {

    private val rectMulti = PosColor2DMultiDraw()
    private val fontMulti = FontMultiDraw()

    @EventTarget
    private fun onRender2D(e: Render2DEvent) {
        repeat(300) {
            rectMulti.addRect(0f, 0f, 50f, 50f, ColorRGB(255, 0, 0))
            rectMulti.addRect(50f, 0f, 50f, 50f, ColorRGB(0, 255, 0))
            rectMulti.addRect(100f, 0f, 50f, 50f, ColorRGB(0, 0, 255))
        }

        repeat(100) {
            fontMulti.addText("Hello World", 0f, 100f, ColorRGB(255, 255, 255))
            fontMulti.addText("Hello World", 0f, 125f, ColorRGB(255, 255, 255), shadow = true)
            fontMulti.addText("Hello World", 0f, 150f, ColorRGB(255, 255, 255), scale = 2f)
        }

        rectMulti.draw()
        fontMulti.draw()
    }

}