package malte0811.ferritecore.mixin.accessors;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(VoxelShape.class)
public interface VoxelShapeAccess {
    @FieldAccessor("shape")
    DiscreteVoxelShape getShape();

    @FieldAccessor("faces")
    VoxelShape[] getFaces();

    @FieldAccessor(value = "shape", getter = false)
    void setShape(DiscreteVoxelShape newPart);

    @FieldAccessor(value = "faces", getter = false)
    void setFaces(VoxelShape[] newCache);
}
