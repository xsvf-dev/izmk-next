package malte0811.ferritecore.mixin.dedupmultipart;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.MultiPartBakedModel$BuilderAccessor;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;
import ovo.xsvf.izmk.injection.mixin.annotation.Overwrite;

@Mixin(MultiPartBakedModel.Builder.class)
public class MixinMultipartBuilder {
    @Overwrite(method = "build", desc = "()Lnet/minecraft/client/resources/model/MultiPartBakedModel;")
    public MultiPartBakedModel build(MultiPartBakedModel.Builder self) {
        return Deduplicator.makeMultipartModel(((MultiPartBakedModel$BuilderAccessor) self).selectors());
    }
}
