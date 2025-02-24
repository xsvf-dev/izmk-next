package malte0811.ferritecore.mixin.accessors;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraft.world.phys.shapes.SubShape;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(SubShape.class)
public interface SubShapeAccess extends DiscreteVSAccess {
    @FieldAccessor("parent")
    DiscreteVoxelShape getParent();

    @FieldAccessor("startX")
    int getStartX();

    @FieldAccessor("startY")
    int getStartY();

    @FieldAccessor("startZ")
    int getStartZ();

    @FieldAccessor("endX")
    int getEndX();

    @FieldAccessor("endY")
    int getEndY();

    @FieldAccessor("endZ")
    int getEndZ();
}
