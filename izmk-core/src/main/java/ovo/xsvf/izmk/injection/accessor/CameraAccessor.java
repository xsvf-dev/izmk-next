package ovo.xsvf.izmk.injection.accessor;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;
import ovo.xsvf.patchify.annotation.MethodAccessor;

@Accessor(Camera.class)
public interface CameraAccessor {
    @FieldAccessor("eyeHeight")
    float eyeHeight();

    @FieldAccessor(value = "eyeHeight", getter = false)
    void setEyeHeight(float height);

    @FieldAccessor(value = "eyeHeightOld", getter = false)
    void setEyeHeightOld(float height);

    @MethodAccessor
    void move(double pDistanceOffset, double pVerticalOffset, double pHorizontalOffset);

    @FieldAccessor("entity")
    Entity entity();
}
