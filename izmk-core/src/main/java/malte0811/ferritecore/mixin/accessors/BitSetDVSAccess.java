package malte0811.ferritecore.mixin.accessors;

import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;

import java.util.BitSet;

@Accessor(BitSetDiscreteVoxelShape.class)
public interface BitSetDVSAccess extends DiscreteVSAccess {
    @FieldAccessor("storage")
    BitSet getStorage();

    @FieldAccessor("xMin")
    int getXMin();

    @FieldAccessor("yMin")
    int getYMin();

    @FieldAccessor("zMin")
    int getZMin();

    @FieldAccessor("xMax")
    int getXMax();

    @FieldAccessor("yMax")
    int getYMax();

    @FieldAccessor("zMax")
    int getZMax();
}
