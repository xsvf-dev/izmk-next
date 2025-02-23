package malte0811.ferritecore.mixin;

import malte0811.ferritecore.impl.KeyValueConditionImpl;
import malte0811.ferritecore.mixin.accessors.KeyValueConditionAccess;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;
import ovo.xsvf.izmk.injection.mixin.annotation.Overwrite;

import java.util.function.Predicate;

@Mixin(KeyValueCondition.class)
public class KeyValueConditionMixin {
    /**
     * Use cached predicates in the case of multiple specified values
     * A less invasive Mixin would be preferable (especially since only one line really changes), but that would involve
     * redirecting a lambda creation (not currently possible as far as I can tell) and capturing locals (possible, but
     * annoying)
     * @author malte0811
     */
    @Overwrite(method = "getPredicate", desc = "(Lnet/minecraft/world/level/block/state/StateDefinition;)Ljava/util/function/Predicate;")
    public static Predicate<BlockState> getPredicate(KeyValueCondition instance, StateDefinition<Block, BlockState> stateContainer) {
        return KeyValueConditionImpl.getPredicate(stateContainer, (((KeyValueConditionAccess) instance).key()),
                (((KeyValueConditionAccess) instance).value()), KeyValueConditionAccess.PIPE_SPLITTER());
    }
}
