package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.multidraw.RectMultiDraw
import ovo.xsvf.izmk.module.Module

object RenderTest: Module("RenderTest") {

    private val multiDraw = RectMultiDraw()

    init {
        enabled = true
    }

    @EventTarget
    private fun onRender2D(e: Render2DEvent) {
        repeat(300) {
            multiDraw.addRect(0f, 0f, 50f, 50f, ColorRGB(255, 0, 0))
            multiDraw.addRect(50f, 50f, 50f, 50f, ColorRGB(0, 255, 0))
            multiDraw.addRect(100f, 100f, 50f, 50f, ColorRGB(0, 0, 255))
        }

        multiDraw.draw()
    }

}