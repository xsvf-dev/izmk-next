package net.optifine.config;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.state.BlockState;
import net.optifine.Config;
import net.optifine.util.BlockUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MatchBlock {
    private static final Logger log = LogManager.getLogger(MatchBlock.class);
    private int blockId = -1;
    private int[] metadatas = null;

    public MatchBlock(int blockId) {
        this.blockId = blockId;
    }

    public MatchBlock(int blockId, int metadata) {
        this.blockId = blockId;
        if (metadata >= 0) {
            this.metadatas = new int[]{metadata};
        }

    }

    public MatchBlock(int blockId, int[] metadatas) {
        this.blockId = blockId;
        this.metadatas = metadatas;
    }

    private static int getMetadata(BlockState blockState) {
        int metadata = BlockUtils.getMetadata(blockState);
        if (metadata < 0) {
            log.warn("Invalid metadata: {}, block: {}", metadata, blockState.getBlock().getName());
            metadata = 0;
        }
        return metadata;
    }

    public int getBlockId() {
        return this.blockId;
    }

    public int[] getMetadatas() {
        return this.metadatas;
    }

    public boolean matches(BlockState blockState) {
        if (BuiltInRegistries.BLOCK.getId(blockState.getBlock()) != this.blockId) {
            return false;
        } else {
            return Matches.metadata(getMetadata(blockState), this.metadatas);
        }
    }

    public boolean matches(int id, int metadata) {
        if (id != this.blockId) {
            return false;
        } else {
            return Matches.metadata(metadata, this.metadatas);
        }
    }

    public void addMetadata(int metadata) {
        if (this.metadatas != null) {
            if (metadata >= 0) {
                for (int j : this.metadatas) {
                    if (j == metadata) {
                        return;
                    }
                }

                this.metadatas = Config.addIntToArray(this.metadatas, metadata);
            }
        }
    }

    public void addMetadatas(int[] mds) {
        for (int j : mds) {
            this.addMetadata(j);
        }

    }

    public String toString() {
        return this.blockId + ":" + Config.arrayToString(this.metadatas);
    }
}