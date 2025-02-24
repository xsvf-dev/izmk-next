import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.Type;
import ovo.xsvf.patchify.asm.MethodWrapper;

import java.lang.invoke.MethodHandle;

public class MethodHelperTest {
    private static final int TIMES = 10000;
    private static final Object2ObjectMap<Pair<String, String>, MethodHandle> cachedMethods =
            new Object2ObjectOpenHashMap<>(1000);

    private void testMethod() {
        // do nothing
    }

    private void test() throws Exception {
        doAndPrintTime("map", () -> {
            for (int i = 0; i < TIMES; i++) {

            }
        });
        doAndPrintTime("direct call", () -> {
            for (int i = 0; i < TIMES; i++) {
                testMethod();
            }
        });
        MethodWrapper helper = MethodWrapper.getInstance(Type.getInternalName(MethodHelperTest.class),
                "testMethod", "()V");
        doAndPrintTime("method helper call",
                () -> {
                    for (int i = 0; i < TIMES; i++) {
                        try {
                            helper.call(this);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        new MethodHelperTest().test();
    }

    private static long doAndPrintTime(String message, Runnable runnable) {
        long start = System.nanoTime();
        System.out.println("==== Running " + message + " ====");
        runnable.run();
        long took = System.nanoTime() - start;
        System.out.printf("Time elapsed: %.3f ns = %.3f ms\n", took + 0f, took / 1000000f);
        System.out.println("============\n");
        return took;
    }
}
