package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.renderer.block.model.BakedQuad;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;

@Accessor(BakedQuad.class)
public interface BakedQuadAccess {
    @FieldAccessor(value = "vertexData", getter = false)
    void setVertices(int[] newVertexData);
}
