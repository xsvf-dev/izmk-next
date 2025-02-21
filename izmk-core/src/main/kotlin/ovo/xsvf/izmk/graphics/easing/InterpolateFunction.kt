package ovo.xsvf.izmk.graphics.easing

fun interface InterpolateFunction {
    fun invoke(time: Long, prev: Float, current: Float): Float
}