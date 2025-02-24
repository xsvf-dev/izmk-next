package malte0811.ferritecore.mixin;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.AndConditionAccess;
import malte0811.ferritecore.util.PredicateHelper;
import net.minecraft.client.renderer.block.model.multipart.AndCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import ovo.xsvf.patchify.annotation.Overwrite;
import ovo.xsvf.patchify.annotation.Patch;

import java.util.function.Predicate;

@Patch(AndCondition.class)
public class AndConditionMixin {
    /**
     * Use cached result predicates
     * @author malte0811
     */
    @Overwrite(method = "getPredicate", desc = "(Lnet/minecraft/world/level/block/state/StateDefinition;)Ljava/util/function/Predicate;")
    public static Predicate<BlockState> getPredicate(AndCondition instance, StateDefinition<Block, BlockState> stateContainer) {
        return Deduplicator.and(PredicateHelper.toCanonicalList(((AndConditionAccess) instance).getConditions(), stateContainer));
    }
}
