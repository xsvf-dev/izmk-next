package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.renderer.block.model.BakedQuad;
import ovo.xsvf.patchify.annotation.Accessor;
import ovo.xsvf.patchify.annotation.FieldAccessor;

@Accessor(BakedQuad.class)
public interface BakedQuadAccess {
    @FieldAccessor(value = "vertexData", getter = false)
    void setVertices(int[] newVertexData);
}
