package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.core.Direction;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;
import ovo.xsvf.izmk.injection.accessor.annotation.Final;

import java.util.List;
import java.util.Map;

@Accessor(SimpleBakedModel.class)
public interface SimpleBakedModelAccess {
    @Final
    @FieldAccessor("unculledFaces")
    List<BakedQuad> getUnculledFaces();

    @Final
    @FieldAccessor(value = "unculledFaces", getter = false)
    void setUnculledFaces(List<BakedQuad> unculledFaces);

    @Final
    @FieldAccessor("culledFaces")
    Map<Direction, List<BakedQuad>> getCulledFaces();

    @Final
    @FieldAccessor(value = "culledFaces", getter = false)
    void setCulledFaces(Map<Direction, List<BakedQuad>> culledFaces);
}
