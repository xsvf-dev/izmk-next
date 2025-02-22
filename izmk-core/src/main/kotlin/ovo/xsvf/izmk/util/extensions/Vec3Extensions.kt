package ovo.xsvf.izmk.util.extensions

import net.minecraft.core.Vec3i
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import ovo.xsvf.izmk.util.math.distance
import ovo.xsvf.izmk.util.math.distanceSq


fun Vec3.distanceTo(x: Double, y: Double, z: Double): Double {
    return distance(this.x, this.y, this.z, x, y, z)
}

fun Vec3.distanceSqTo(x: Double, y: Double, z: Double): Double {
    return distanceSq(this.x, this.y, this.z, x, y, z)
}


fun Vec3.distanceTo(vec3d: Vec3): Double {
    return distanceTo(vec3d.x, vec3d.y, vec3d.z)
}

fun Vec3.distanceSqTo(vec3d: Vec3): Double {
    return distanceSqTo(vec3d.x, vec3d.y, vec3d.z)
}


fun Vec3.distanceTo(entity: Entity): Double {
    return distanceTo(entity.x, entity.y, entity.z)
}

fun Vec3.distanceSqTo(entity: Entity): Double {
    return distanceSqTo(entity.x, entity.y, entity.z)
}


fun Vec3.distanceToCenter(x: Int, y: Int, z: Int): Double {
    return distance(this.x, this.y, this.z, x + 0.5, y + 0.5, z + 0.5)
}

fun Vec3.distanceSqToCenter(x: Int, y: Int, z: Int): Double {
    return distanceSq(this.x, this.y, this.z, x + 0.5, y + 0.5, z + 0.5)
}


fun Vec3.distanceToCenter(vec3i: Vec3i): Double {
    return distanceToCenter(vec3i.x, vec3i.y, vec3i.z)
}

fun Vec3.distanceSqToCenter(vec3i: Vec3i): Double {
    return distanceSqToCenter(vec3i.x, vec3i.y, vec3i.z)
}
