package malte0811.ferritecore.mixin;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.MultiPartBakedModel$BuilderAccess;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;
import ovo.xsvf.izmk.injection.mixin.annotation.Overwrite;

@Mixin(MultiPartBakedModel.Builder.class)
public class MixinMultipartBuilder {
    @Overwrite(method = "build", desc = "()Lnet/minecraft/client/resources/model/MultiPartBakedModel;")
    public static MultiPartBakedModel build(MultiPartBakedModel.Builder self, CallbackInfo callbackInfo) {
        return Deduplicator.makeMultipartModel(((MultiPartBakedModel$BuilderAccess) self).selectors());
    }
}
