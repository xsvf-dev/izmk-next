package ovo.xsvf.izmk.util.extensions

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import ovo.xsvf.izmk.util.math.distance
import ovo.xsvf.izmk.util.math.distanceSq

fun Vec3i.distanceToCenter(x: Double, y: Double, z: Double): Double {
    return distance(this.x + 0.5, this.y + 0.5, this.z + 0.5, x, y, z)
}

fun Vec3i.distanceSqToCenter(x: Double, y: Double, z: Double): Double {
    return distanceSq(this.x + 0.5, this.y + 0.5, this.z + 0.5, x, y, z)
}


fun Vec3i.distanceToCenter(vec3d: Vec3): Double {
    return distance(this.x + 0.5, this.y + 0.5, this.z + 0.5, vec3d.x, vec3d.y, vec3d.z)
}

fun Vec3i.distanceSqToCenter(vec3d: Vec3): Double {
    return distanceSq(this.x + 0.5, this.y + 0.5, this.z + 0.5, vec3d.x, vec3d.y, vec3d.z)
}


fun Vec3i.distanceTo(x: Int, y: Int, z: Int): Double {
    return distance(this.x, this.y, this.z, x, y, z)
}

fun Vec3i.distanceSqTo(x: Int, y: Int, z: Int): Int {
    return distanceSq(this.x, this.y, this.z, x, y, z)
}


fun Vec3i.distanceTo(vec3i: Vec3i): Double {
    return distance(this.x, this.y, this.z, vec3i.x, vec3i.y, vec3i.z)
}

fun Vec3i.distanceSqTo(vec3i: Vec3i): Int {
    return distanceSq(this.x, this.y, this.z, vec3i.x, vec3i.y, vec3i.z)
}