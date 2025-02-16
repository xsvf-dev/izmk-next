package ovo.xsvf.izmk.misc;

import net.minecraft.client.Minecraft;
import ovo.xsvf.izmk.Entry;
import ovo.xsvf.izmk.IZMK;
import ovo.xsvf.izmk.injection.mixin.CallbackInfo;
import ovo.xsvf.izmk.injection.mixin.impl.MixinMinecraft;

/**
 * 由于一些神秘原因，我无法在 {@link Entry} 里面使用 {@link Minecraft#getInstance()}。
 * <br />
 * 必须确保这个类在 {@link MixinMinecraft#tick(Minecraft, CallbackInfo)} 后初始化。
 */
public interface MinecraftInstance {
    Minecraft mc = IZMK.mc;
}
