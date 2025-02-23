package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.FontMultiDraw
import ovo.xsvf.izmk.graphics.multidraw.RectMultiDraw
import ovo.xsvf.izmk.module.Module

object RenderTest: Module("render-test") {

    private val rectMulti = RectMultiDraw()
    private val fontMulti = FontMultiDraw()

    init {
        enabled = true
    }

    @EventTarget
    private fun onRender2D(e: Render2DEvent) {
        return  // Low fps, skip rendering

        repeat(300) {
            rectMulti.addRect(0f, 0f, 50f, 50f, ColorRGB(255, 0, 0))
            rectMulti.addRect(50f, 50f, 50f, 50f, ColorRGB(0, 255, 0))
            rectMulti.addRect(100f, 100f, 50f, 50f, ColorRGB(0, 0, 255))
        }
        rectMulti.draw()

        repeat(100) {
            fontMulti.addText("Hello World", 0f, 0f, ColorRGB(255, 255, 255))
            fontMulti.addText("Hello World", 50f, 50f, ColorRGB(255, 255, 255))
            fontMulti.addText("Hello World", 100f, 100f, ColorRGB(255, 255, 255))
        }
        fontMulti.draw()
    }

}