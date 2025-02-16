package ovo.xsvf.izmk.injection.mixin.api;

import org.objectweb.asm.Opcodes;
import ovo.xsvf.izmk.misc.Constants;
import ovo.xsvf.izmk.injection.mixin.annotation.Mixin;

public interface IMixinLoader extends Constants, Opcodes {
    /**
     * Load a mixin class into the current JVM.
     * <p>
     *     <b>Note:</b> If the provided class is not annotated with {@link Mixin},
     *     an exception will be thrown.
     * </p>
     * @param mixinClass a class annotated with {@link Mixin}
     * @throws Exception if any error occurs during mixin loading
     */
    void loadMixin(Class<?> mixinClass) throws Exception;
}
