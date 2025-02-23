package malte0811.ferritecore.mixin.accessors;

import com.google.common.base.Splitter;
import net.minecraft.client.renderer.block.model.multipart.KeyValueCondition;
import ovo.xsvf.izmk.injection.accessor.annotation.Accessor;
import ovo.xsvf.izmk.injection.accessor.annotation.FieldAccessor;
import ovo.xsvf.izmk.injection.accessor.annotation.Final;

@Accessor(KeyValueCondition.class)
public interface KeyValueConditionAccess {
    @Final
    @FieldAccessor("key")
    String key();

    @Final
    @FieldAccessor("value")
    String value();

    @Final
    @FieldAccessor("PIPE_SPLITTER")
    static Splitter PIPE_SPLITTER() { return null; }
}
