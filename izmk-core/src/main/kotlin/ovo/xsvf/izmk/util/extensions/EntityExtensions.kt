package ovo.xsvf.izmk.util.extensions;

import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3
import ovo.xsvf.izmk.util.math.distance
import ovo.xsvf.izmk.util.math.distanceSq

fun Entity.distanceTo(x: Double, y: Double, z: Double): Double {
    return distance(this.x, this.y, this.z, x, y, z)
}

fun Entity.distanceSqTo(x: Double, y: Double, z: Double): Double {
    return distanceSq(this.x, this.y, this.z, x, y, z)
}

fun Entity.distanceTo(vec3d:Vec3): Double {
    return distanceTo(vec3d.x, vec3d.y, vec3d.z)
}

fun Entity.distanceSqTo(vec3d: Vec3): Double {
    return distanceSqTo(vec3d.x, vec3d.y, vec3d.z)
}

fun Entity.distanceSqTo(entity: Entity): Double {
    return distanceSqTo(entity.x, entity.y, entity.z)
}


fun Entity.distanceToCenter(x: Int, y: Int, z: Int): Double {
    return distance(this.x, this.y, this.z, x + 0.5, y + 0.5, z + 0.5)
}

fun Entity.distanceSqToCenter(x: Int, y: Int, z: Int): Double {
    return distanceSq(this.x, this.y, this.z, x + 0.5, y + 0.5, z + 0.5)
}

fun Entity.distanceToCenter(vec3i:Vec3i): Double {
    return distanceToCenter(vec3i.x, vec3i.y, vec3i.z)
}

fun Entity.distanceSqToCenter(vec3i: Vec3i): Double {
    return distanceSqToCenter(vec3i.x, vec3i.y, vec3i.z)
}

fun Entity.hDistanceTo(x: Double, y: Double): Double {
    return distance(this.x, this.y, x, y)
}

fun Entity.hDistanceSqTo(x: Double, y: Double): Double {
    return distanceSq(this.x, this.y, x, y)
}

fun Entity.hDistanceToCenter(x: Int, y: Int): Double {
    return distance(this.x, this.y, x + 0.5, y + 0.5)
}

fun Entity.hDistanceSqToCenter(x: Int, y: Int): Double {
    return distanceSq(this.x, this.y, x + 0.5, y + 0.5)
}

fun Entity.hDistanceToCenter(chunkPos:ChunkPos): Double {
    return hDistanceToCenter(chunkPos.x * 16 + 8, chunkPos.z * 16 + 8)
}

fun Entity.hDistanceSqToCenter(chunkPos: ChunkPos): Double {
    return hDistanceSqToCenter(chunkPos.x * 16 + 8, chunkPos.z * 16 + 8)
}