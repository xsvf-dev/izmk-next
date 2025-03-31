package ovo.xsvf.izmk.injection.accessor;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.phys.Vec3;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;
import ovo.xsvf.patchify.annotation.MethodAccessor;

@Accessor(Entity.class)
public interface EntityAccessor {
    @FieldAccessor("stuckSpeedMultiplier")
    Vec3 getStuckSpeedMultiplier();

    @MethodAccessor
    boolean canEnterPose(Pose pPose);
}
