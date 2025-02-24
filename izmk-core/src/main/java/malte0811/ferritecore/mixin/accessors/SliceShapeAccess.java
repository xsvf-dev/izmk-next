package malte0811.ferritecore.mixin.accessors;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.SliceShape;
import net.minecraft.world.phys.shapes.VoxelShape;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(SliceShape.class)
public interface SliceShapeAccess extends VoxelShapeAccess {
    @FieldAccessor(value = "delegate")
    VoxelShape getDelegate();

    @FieldAccessor(value = "axis")
    Direction.Axis getAxis();
}
