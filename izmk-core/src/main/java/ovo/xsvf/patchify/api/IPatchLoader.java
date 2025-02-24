package ovo.xsvf.patchify.api;

import it.unimi.dsi.fastutil.Pair;
import org.objectweb.asm.Opcodes;
import ovo.xsvf.patchify.annotation.Patch;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Function;

public interface IPatchLoader extends Opcodes {
    /**
     * Patch the target class with the provided patch class.
     * <p>
     *     <b>Note:</b> If the provided class is not annotated with {@link Patch},
     *     an exception will be thrown.
     * </p>
     * @param bytesProvider a function that can be used to get the byte data of the target class
     * @param patchClass a class annotated with {@link Patch}
     * @return a pair of the patched class and its bytecode
     * @throws Exception if any error occurs during patch generation
     */
    Pair<Class<?>, byte[]> patch(Function<Class<?>, byte[]> bytesProvider,
                                 Class<?> patchClass) throws Exception;

    /**
     * Load a mixin class into the current JVM.
     * <p>
     *     <b>Note:</b> If the provided class is not annotated with {@link Patch},
     *     an exception will be thrown.
     * </p>
     * @see #loadPatch(Class, Function, BiConsumer)
     *
     * @param patchClass a class annotated with {@link Patch}
     * @param bytesProvider a function that can be used to get the byte data of the target class
     * @param classTransformer a consumer that can be used to transform the loaded class
     * @throws Exception if any error occurs during mixin loading
     */
    default void loadPatch(Class<?> patchClass, Function<Class<?>, byte[]> bytesProvider,
                           BiConsumer<Class<?>, byte[]> classTransformer) throws Exception {
        Pair<Class<?>, byte[]> patched = patch(bytesProvider, patchClass);
        classTransformer.accept(patched.first(), patched.second());
    }

    /**
     * Load multiple mixin classes into the current JVM.
     * <p>
     *     <b>Note:</b> If any of the provided classes is not annotated with {@link Patch},
     *     an exception will be thrown.
     * </p>
     * @param patchClasses an array of classes annotated with {@link Patch}
     * @param bytesProvider a function that can be used to get the byte data of the target class
     * @param classTransformer a consumer that can be used to transform the loaded classes
     * @throws Exception if any error occurs during mixin loading
     */
    void loadPatches(Collection<Class<?>> patchClasses, Function<Class<?>, byte[]> bytesProvider,
                     BiConsumer<Class<?>, byte[]> classTransformer) throws Exception;

    /**
     * @see #loadPatch(Class, Function, BiConsumer)
     */
    default void loadPatches(BiConsumer<Class<?>, byte[]> classTransformer,
                             Function<Class<?>, byte[]> bytesProvider,
                             Class<?>... patchClasses) throws Exception {
        loadPatches(Arrays.asList(patchClasses), bytesProvider, classTransformer);
    }
}
