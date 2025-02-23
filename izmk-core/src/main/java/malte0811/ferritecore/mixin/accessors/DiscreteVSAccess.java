package malte0811.ferritecore.mixin.accessors;

import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;

@Accessor(DiscreteVoxelShape.class)
public interface DiscreteVSAccess {
    @FieldAccessor("xSize")
    int getXSize();

    @FieldAccessor("ySize")
    int getYSize();

    @FieldAccessor("zSize")
    int getZSize();
}
