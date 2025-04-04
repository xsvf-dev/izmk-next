package ovo.xsvf.izmk.graphics.general

import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import ovo.xsvf.izmk.graphics.GlHelper
import ovo.xsvf.izmk.graphics.color.ColorRGB
import ovo.xsvf.izmk.graphics.utils.RenderUtils3D

class ESPRenderer {
    private var toRender0: MutableList<Info> = ArrayList()

    val toRender: List<Info>
        get() = toRender0

    var aFilled = 0
    var through = true

    val size: Int
        get() = toRender0.size
    fun add(pos: BlockPos, color: ColorRGB) {
        add(pos, color, DirectionMask.ALL)
    }

    fun add(pos: BlockPos, color: ColorRGB, sides: Int) {
        add(AABB(pos), color, sides)
    }

    fun add(box: AABB, color: ColorRGB) {
        add(box, color, DirectionMask.ALL)
    }

    fun add(box: AABB, color: ColorRGB, sides: Int) {
        add(Info(box, color, sides))
    }

    fun add(info: Info) {
        toRender0.add(info)
    }

    fun replaceAll(list: MutableList<Info>) {
        toRender0 = list
    }

    fun clear() {
        toRender0.clear()
    }

    fun render(clear: Boolean,
               renderBox: Boolean = true,
               boxHeight: Double = 1.0,
               outline: Boolean = false,
               lineWidth: Float = 1.0f,
               lineHeight: Double = 1.0
    ) {
        val filled = aFilled != 0
        if (toRender0.isEmpty() || (!filled)) return

        if (through) GlHelper.depth = false

        if (filled) {
            GlHelper.cull = false
            for ((box, color) in toRender0) {
                val a = (aFilled * (color.a / 255.0f)).toInt()
                if (renderBox) RenderUtils3D.drawFilledBox(box, color.alpha(a), boxHeight)
                if (outline) RenderUtils3D.drawBoxOutline(box, color, lineWidth, lineHeight)
            }
        }

        if (clear) clear()
        GlHelper.depth = true
    }

    data class Info(val box: AABB, val color: ColorRGB, val sides: Int) {
        constructor(box: AABB) : this(box, ColorRGB(255, 255, 255), DirectionMask.ALL)
        constructor(box: AABB, color: ColorRGB) : this(box, color, DirectionMask.ALL)
        constructor(pos: BlockPos) : this(AABB(pos), ColorRGB(255, 255, 255), DirectionMask.ALL)
        constructor(pos: BlockPos, color: ColorRGB) : this(AABB(pos), color, DirectionMask.ALL)
        constructor(pos: BlockPos, color: ColorRGB, sides: Int) : this(AABB(pos), color, sides)
    }
}