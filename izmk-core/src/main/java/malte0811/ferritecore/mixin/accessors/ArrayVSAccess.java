package malte0811.ferritecore.mixin.accessors;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.world.phys.shapes.ArrayVoxelShape;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(ArrayVoxelShape.class)
public interface ArrayVSAccess extends VoxelShapeAccess {
    @FieldAccessor(value = "xs", getter = false)
    void setXPoints(DoubleList newPoints);

    @FieldAccessor(value = "ys", getter = false)
    void setYPoints(DoubleList newPoints);

    @FieldAccessor(value = "zs", getter = false)
    void setZPoints(DoubleList newPoints);

    @FieldAccessor("xs")
    DoubleList getXPoints();

    @FieldAccessor("ys")
    DoubleList getYPoints();

    @FieldAccessor("zs")
    DoubleList getZPoints();
}
