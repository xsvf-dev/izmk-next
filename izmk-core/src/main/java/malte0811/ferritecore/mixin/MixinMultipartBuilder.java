package malte0811.ferritecore.mixin;

import malte0811.ferritecore.impl.Deduplicator;
import malte0811.ferritecore.mixin.accessors.MultiPartBakedModel$BuilderAccess;
import net.minecraft.client.resources.model.MultiPartBakedModel;
import ovo.xsvf.patchify.CallbackInfo;
import ovo.xsvf.patchify.annotation.Overwrite;
import ovo.xsvf.patchify.annotation.Patch;

@Patch(MultiPartBakedModel.Builder.class)
public class MixinMultipartBuilder {
    @Overwrite(method = "build", desc = "()Lnet/minecraft/client/resources/model/MultiPartBakedModel;")
    public static MultiPartBakedModel build(MultiPartBakedModel.Builder self, CallbackInfo callbackInfo) {
        return Deduplicator.makeMultipartModel(((MultiPartBakedModel$BuilderAccess) self).selectors());
    }
}
