@file:Suppress("nothing_to_inline", "unused")
package ovo.xsvf.izmk.util.math

import kotlin.math.*

object MathUtil {
    fun round(value: Float, places: Int): Float {
        val scale = 10.0f.pow(places)
        return round(value * scale) / scale
    }

    fun round(value: Double, places: Int): Double {
        val scale = 10.0.pow(places)
        return round(value * scale) / scale
    }

    fun decimalPlaces(value: Double) = value.toString().split('.').getOrElse(1) { "0" }.length

    fun decimalPlaces(value: Float) = value.toString().split('.').getOrElse(1) { "0" }.length

    fun isNumberEven(i: Int): Boolean {
        return i and 1 == 0
    }

    fun reverseNumber(num: Int, min: Int, max: Int): Int {
        return max + min - num
    }

    fun convertRange(valueIn: Int, minIn: Int, maxIn: Int, minOut: Int, maxOut: Int): Int {
        return convertRange(
            valueIn.toDouble(),
            minIn.toDouble(),
            maxIn.toDouble(),
            minOut.toDouble(),
            maxOut.toDouble()
        ).toInt()
    }

    fun convertRange(valueIn: Float, minIn: Float, maxIn: Float, minOut: Float, maxOut: Float): Float {
        return convertRange(
            valueIn.toDouble(),
            minIn.toDouble(),
            maxIn.toDouble(),
            minOut.toDouble(),
            maxOut.toDouble()
        ).toFloat()
    }

    fun convertRange(valueIn: Double, minIn: Double, maxIn: Double, minOut: Double, maxOut: Double): Double {
        val rangeIn = maxIn - minIn
        val rangeOut = maxOut - minOut
        val convertedIn = (valueIn - minIn) * (rangeOut / rangeIn) + minOut
        val actualMin = min(minOut, maxOut)
        val actualMax = max(minOut, maxOut)
        return min(max(convertedIn, actualMin), actualMax)
    }

    fun lerp(from: Double, to: Double, delta: Double): Double {
        return from + (to - from) * delta
    }

    fun lerp(from: Double, to: Double, delta: Float): Double {
        return from + (to - from) * delta
    }

    fun lerp(from: Float, to: Float, delta: Double): Float {
        return from + (to - from) * delta.toFloat()
    }
    
    fun lerp(from: Float, to: Float, delta: Float): Float {
        return from + (to - from) * delta
    }
    
    fun clamp(value: Double, min: Double, max: Double): Double {
        return value.coerceIn(min, max)
    }

    fun clamp(value: Float, min: Float, max: Float): Float {
        return value.coerceIn(min, max)
    }
    
    fun clamp(value: Int, min: Int, max: Int): Int {
        return value.coerceIn(min, max)
    }

    fun approxEq(a: Double, b: Double, epsilon: Double = 0.0001): Boolean {
        return abs(a - b) < epsilon
    }

    fun approxEq(a: Float, b: Float, epsilon: Float = 0.0001f): Boolean {
        return abs(a - b) < epsilon
    }

    fun frac(value: Double): Double {
        return value - floor(value)
    }
    
    fun frac(value: Float): Float {
        return value - floor(value)
    }

    fun roundToDecimal(n: Double, point: Int): Double {
        if (point == 0) {
            return floor(n)
        }
        val factor = 10.0.pow(point.toDouble())
        return round(n * factor) / factor
    }
}