package ovo.xsvf.izmk.util.math

import ovo.xsvf.izmk.util.extensions.sq
import kotlin.math.sqrt

fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    return length(x2 - x1, y2 - y1)
}

fun distance(x1: Float, y1: Float, x2: Float, y2: Float): Double {
    return length(x2 - x1, y2 - y1)
}

fun distance(x1: Int, y1: Int, x2: Int, y2: Int): Double {
    return length(x2 - x1, y2 - y1)
}

fun length(x: Double, y: Double): Double {
    return sqrt(lengthSq(x, y))
}

fun length(x: Float, y: Float): Double {
    return sqrt(lengthSq(x.toDouble(), y.toDouble()))
}

fun length(x: Int, y: Int): Double {
    return sqrt(lengthSq(x.toDouble(), y.toDouble()))
}

fun distanceSq(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    return lengthSq(x2 - x1, y2 - y1)
}

fun distanceSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    return lengthSq(x2 - x1, y2 - y1)
}

fun distanceSq(x1: Int, y1: Int, x2: Int, y2: Int): Int {
    return lengthSq(x2 - x1, y2 - y1)
}

fun lengthSq(x: Double, y: Double): Double {
    return x.sq + y.sq
}

fun lengthSq(x: Float, y: Float): Float {
    return x.sq + y.sq
}

fun lengthSq(x: Int, y: Int): Int {
    return x.sq + y.sq
}

fun distance(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
    return sqrt(distanceSq(x1, y1, z1, x2, y2, z2))
}

fun distance(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Double {
    return sqrt(distanceSq(x1, y1, z1, x2, y2, z2).toDouble())
}

fun distance(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Double {
    return sqrt(distanceSq(x1, y1, z1, x2, y2, z2).toDouble())
}

fun length(x: Double, y: Double, z: Double): Double {
    return sqrt(lengthSq(x, y, z))
}

fun length(x: Float, y: Float, z: Float): Double {
    return sqrt(lengthSq(x.toDouble(), y.toDouble(), z.toDouble()))
}

fun length(x: Int, y: Int, z: Int): Double {
    return sqrt(lengthSq(x.toDouble(), y.toDouble(), z.toDouble()))
}

fun distanceSq(x1: Double, y1: Double, z1: Double, x2: Double, y2: Double, z2: Double): Double {
    return lengthSq(x2 - x1, y2 - y1, z2 - z1)
}

fun distanceSq(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float): Float {
    return lengthSq(x2 - x1, y2 - y1, z2 - z1)
}

fun distanceSq(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int): Int {
    return lengthSq(x2 - x1, y2 - y1, z2 - z1)
}

fun lengthSq(x: Double, y: Double, z: Double): Double {
    return x.sq + y.sq + z.sq
}

fun lengthSq(x: Float, y: Float, z: Float): Float {
    return x.sq + y.sq + z.sq
}

fun lengthSq(x: Int, y: Int, z: Int): Int {
    return x.sq + y.sq + z.sq
}
