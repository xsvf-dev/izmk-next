package malte0811.ferritecore.mixin.accessors;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.MethodAccessor;

@Accessor(BlockBehaviour.BlockStateBase.class)
public interface BlockStateBaseAccessor {
    @MethodAccessor
    BlockState asState();
}
