package ovo.xsvf.izmk.util.math.vectors

data class Vec3f(val x: Float, val y: Float, val z: Float) {
    companion object {
        @JvmField
        val ZERO = Vec3f(0.0f, 0.0f, 0.0f)
    }
}