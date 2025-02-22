package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import net.minecraft.world.level.block.state.BlockState;
import org.apache.commons.lang3.tuple.Pair;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;

import java.util.List;
import java.util.function.Predicate;

@Accessor(MultiPartBakedModel.Builder.class)
public interface MultiPartBakedModel$BuilderAccessor {
    @FieldAccessor("selectors")
    public abstract List<Pair<Predicate<BlockState>, BakedModel>> selectors();
}
