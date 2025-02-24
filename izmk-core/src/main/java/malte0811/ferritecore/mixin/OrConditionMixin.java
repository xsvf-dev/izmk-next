package malte0811.ferritecore.mixin;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.OrConditionAccess;
import malte0811.ferritecore.util.PredicateHelper;
import net.minecraft.client.renderer.block.model.multipart.OrCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import ovo.xsvf.patchify.annotation.Overwrite;
import ovo.xsvf.patchify.annotation.Patch;

import java.util.function.Predicate;

@Patch(OrCondition.class)
public class OrConditionMixin {
    /**
     * Use cached result predicates
     * @author malte0811
     */
    @Overwrite(method = "getPredicate", desc = "(Lnet/minecraft/world/level/block/state/StateDefinition;)Ljava/util/function/Predicate;")
    public static Predicate<BlockState> getPredicate(OrCondition self, StateDefinition<Block, BlockState> stateContainer) {
        return Deduplicator.or(PredicateHelper.toCanonicalList(((OrConditionAccess) self).getConditions(),
                stateContainer));
    }
}
