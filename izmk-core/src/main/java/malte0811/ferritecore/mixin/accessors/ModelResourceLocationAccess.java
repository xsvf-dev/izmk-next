package malte0811.ferritecore.mixin.accessors;

import net.minecraft.client.resources.model.ModelResourceLocation;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;
import ovo.xsvf.izmk.injection.accessor.annotation.Final;

@Accessor(ModelResourceLocation.class)
public interface ModelResourceLocationAccess {
    @Final
    @FieldAccessor("variant")
    String getVariant();

    @Final
    @FieldAccessor(value = "variant", getter = false)
    void setVariant(String variant);
}
