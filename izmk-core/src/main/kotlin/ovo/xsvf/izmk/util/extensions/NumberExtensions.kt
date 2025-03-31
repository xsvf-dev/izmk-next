package ovo.xsvf.izmk.util.extensions

import ovo.xsvf.izmk.util.math.*
import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor

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

fun Float.toRadians() = this / 180.0f * PI_FLOAT
fun Double.toRadians() = this / 180.0 * PI

fun Float.toDegree() = this * 180.0f / PI_FLOAT
fun Double.toDegree() = this * 180.0 / PI

fun Double.floorToInt(): Int = floor(this).toInt()
fun Float.floorToInt(): Int = floor(this).toInt()

fun Double.ceilToInt(): Int = ceil(this).toInt()
fun Float.ceilToInt(): Int = ceil(this).toInt()

fun Double.fastFloor(): Int = (this + FLOOR_DOUBLE_D).toInt() - FLOOR_DOUBLE_I
fun Double.fastFloorToDouble(): Double = this.fastFloor().toDouble()
fun Float.fastFloor(): Int = (this + FLOOR_FLOAT_F).toInt() - FLOOR_FLOAT_I
fun Float.fastFloorToFloat(): Float = this.fastFloor().toFloat()

fun Double.fastCeil(): Int = FLOOR_DOUBLE_I - (FLOOR_DOUBLE_D - this).toInt()
fun Float.fastCeil(): Int = FLOOR_FLOAT_I - (FLOOR_FLOAT_F - this).toInt()