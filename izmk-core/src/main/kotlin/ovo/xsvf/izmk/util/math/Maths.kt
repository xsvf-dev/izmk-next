@file:Suppress("nothing_to_inline", "unused")
package ovo.xsvf.izmk.util.math

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

const val PI_FLOAT = 3.14159265358979323846f

const val FLOOR_DOUBLE_D: Double = 1_073_741_824.0
const val FLOOR_DOUBLE_I: Int = 1_073_741_824

const val FLOOR_FLOAT_F: Float = 4_194_304.0f
const val FLOOR_FLOAT_I: Int = 4_194_304

fun Float.toRadians() = this / 180.0f * PI_FLOAT
fun Double.toRadians() = this / 180.0 * PI

fun Float.toDegree() = this * 180.0f / PI_FLOAT
fun Double.toDegree() = this * 180.0 / PI

inline fun Double.floorToInt(): Int = floor(this).toInt()
inline fun Float.floorToInt(): Int = floor(this).toInt()

inline fun Double.ceilToInt(): Int = ceil(this).toInt()
inline fun Float.ceilToInt(): Int = ceil(this).toInt()

inline fun Double.fastFloor(): Int = (this + FLOOR_DOUBLE_D).toInt() - FLOOR_DOUBLE_I
inline fun Double.fastFloorToDouble(): Double = this.fastFloor().toDouble()
inline fun Float.fastFloor(): Int = (this + FLOOR_FLOAT_F).toInt() - FLOOR_FLOAT_I
inline fun Float.fastFloorToFloat(): Float = this.fastFloor().toFloat()

inline fun Double.fastCeil(): Int = FLOOR_DOUBLE_I - (FLOOR_DOUBLE_D - this).toInt()
inline fun Float.fastCeil(): Int = FLOOR_FLOAT_I - (FLOOR_FLOAT_F - this).toInt()

val Double.sq: Double get() = this * this
val Float.sq: Float get() = this * this
val Int.sq: Int get() = this * this

val Double.cubic: Double get() = this * this * this
val Float.cubic: Float get() = this * this * this
val Int.cubic: Int get() = this * this * this

val Double.quart: Double get() = this * this * this * this
val Float.quart: Float get() = this * this * this * this
val Int.quart: Int get() = this * this * this * this

val Double.quint: Double get() = this * this * this * this * this
val Float.quint: Float get() = this * this * this * this * this
val Int.quint: Int get() = this * this * this * this * this

val Int.isEven: Boolean get() = this and 1 == 0
val Int.isOdd: Boolean get() = this and 1 == 1
