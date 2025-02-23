package ovo.xsvf.izmk.module.impl

import ovo.xsvf.izmk.event.EventTarget
import ovo.xsvf.izmk.event.impl.Render2DEvent
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
    }

}