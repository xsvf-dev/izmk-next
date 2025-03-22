package net.optifine.util;

import it.unimi.dsi.fastutil.objects.Object2ByteLinkedOpenHashMap;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Collection;
import java.util.List;

public class BlockUtils {
    private static final ThreadLocal<RenderSideCacheKey> threadLocalKey = ThreadLocal.withInitial(() -> new RenderSideCacheKey(null, null, null));
    private static final ThreadLocal<Object2ByteLinkedOpenHashMap<RenderSideCacheKey>> threadLocalMap = ThreadLocal.withInitial(() -> {
        Object2ByteLinkedOpenHashMap<RenderSideCacheKey> object2bytelinkedopenhashmap = new Object2ByteLinkedOpenHashMap<RenderSideCacheKey>(200) {
            protected void rehash(int p_rehash_1_) {
            }
        };
        object2bytelinkedopenhashmap.defaultReturnValue((byte) 127);
        return object2bytelinkedopenhashmap;
    });

    public static int getBlockId(Block block) {
        return BuiltInRegistries.BLOCK.getId(block);
    }

    public static Block getBlock(ResourceLocation loc) {
        return !BuiltInRegistries.BLOCK.containsKey(loc) ? null : BuiltInRegistries.BLOCK.get(loc);
    }

    public static int getMetadata(BlockState blockState) {
        Block block = blockState.getBlock();
        StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
        List<BlockState> list = statedefinition.getPossibleStates();
        return list.indexOf(blockState);
    }

    public static int getMetadataCount(Block block) {
        StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
        List<BlockState> list = statedefinition.getPossibleStates();
        return list.size();
    }

    public static BlockState getBlockState(Block block, int metadata) {
        StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
        List<BlockState> list = statedefinition.getPossibleStates();
        return metadata >= 0 && metadata < list.size() ? list.get(metadata) : null;
    }

    public static List<BlockState> getBlockStates(Block block) {
        StateDefinition<Block, BlockState> statedefinition = block.getStateDefinition();
        List<BlockState> list = statedefinition.getPossibleStates();
        return list;
    }

    public static Collection<Property<?>> getProperties(BlockState blockState) {
        return blockState.getProperties();
    }

    public static final class RenderSideCacheKey {
        private BlockState blockState1;
        private BlockState blockState2;
        private Direction facing;
        private int hashCode;

        private RenderSideCacheKey(BlockState blockState1In, BlockState blockState2In, Direction facingIn) {
            this.blockState1 = blockState1In;
            this.blockState2 = blockState2In;
            this.facing = facingIn;
        }

        private void init(BlockState blockState1In, BlockState blockState2In, Direction facingIn) {
            this.blockState1 = blockState1In;
            this.blockState2 = blockState2In;
            this.facing = facingIn;
            this.hashCode = 0;
        }

        public RenderSideCacheKey duplicate() {
            return new RenderSideCacheKey(this.blockState1, this.blockState2, this.facing);
        }

        public boolean equals(Object p_equals_1_) {
            if (this == p_equals_1_) {
                return true;
            } else if (!(p_equals_1_ instanceof RenderSideCacheKey blockutils$rendersidecachekey)) {
                return false;
            } else {
                return this.blockState1 == blockutils$rendersidecachekey.blockState1 && this.blockState2 == blockutils$rendersidecachekey.blockState2 && this.facing == blockutils$rendersidecachekey.facing;
            }
        }

        public int hashCode() {
            if (this.hashCode == 0) {
                this.hashCode = 31 * this.hashCode + this.blockState1.hashCode();
                this.hashCode = 31 * this.hashCode + this.blockState2.hashCode();
                this.hashCode = 31 * this.hashCode + this.facing.hashCode();
            }

            return this.hashCode;
        }
    }
}
