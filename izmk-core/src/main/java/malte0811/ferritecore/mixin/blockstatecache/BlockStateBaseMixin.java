package malte0811.ferritecore.mixin.blockstatecache;

import malte0811.ferritecore.impl.BlockStateCacheImpl;
import malte0811.ferritecore.mixin.accessors.BlockStateBaseAccessor;
import net.minecraft.world.level.block.state.BlockBehaviour;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.At;
import ovo.xsvf.izmk.injection.mixin.annotation.Inject;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

@Mixin(BlockBehaviour.BlockStateBase.class)
public abstract class BlockStateBaseMixin {
    @Inject(method = "initCache", desc = "()V")
    public void cacheStateHead(BlockBehaviour.BlockStateBase state, CallbackInfo ci) {
        BlockStateCacheImpl.deduplicateCachePre(((BlockStateBaseAccessor) state).asState());
    }

    @Inject(method = "initCache", desc = "()V", at = @At(At.Type.TAIL))
    public void cacheStateTail(BlockBehaviour.BlockStateBase state, CallbackInfo ci) {
        BlockStateCacheImpl.deduplicateCachePost(((BlockStateBaseAccessor) state).asState());
    }
}
