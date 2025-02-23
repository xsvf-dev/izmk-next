import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.io.PrintStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

public class ASMTest {
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
    private static final Object o = new Object();

    private static final long TEST_TIMES = 10_000_000;

    private static final ASMTest INSTANCE = new ASMTest();

    private static void empty(Object o) {
        // do nothing
    }

    public void test() throws Throwable {
        testDirect();
        testReflection();
        testVarHandle();
        testASM();
    }

    private static void testDirect() throws Throwable {
        doAndPrintTime("Direct", () -> {
            for (int i = 0; i < TEST_TIMES; i++) {
                empty(Test.TEST_STRING);
            }
        });
    }

    private static void testVarHandle() throws Throwable {
        VarHandle handle = MethodHandles.privateLookupIn(Test.class, LOOKUP)
                .findStaticVarHandle(Test.class, "TEST_STRING", String.class);
        doAndPrintTime("VarHandle", () -> {
            for (int i = 0; i < TEST_TIMES; i++) {
                empty(handle.get());
            }
        });
    }

    private static void testReflection() throws NoSuchFieldException, IllegalAccessException {
        Field field = Test.class.getDeclaredField("TEST_STRING");
        field.setAccessible(true);
        doAndPrintTime("Reflection", () -> {
            for (int i = 0; i < TEST_TIMES; i++) {
                try {
                    empty(field.get(null));
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private static void testASM() throws IllegalAccessException, NoSuchMethodException {
        ClassNode node = ASMUtil.createStaticFieldGetter(
                "call",
                Type.getInternalName(Test.class),
                "TEST_STRING",
                Type.getDescriptor(String.class)
        );
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        node.accept(cw);
        byte[] bytes = cw.toByteArray();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Test.class, LOOKUP);
        MethodHandles.Lookup defined = lookup.defineHiddenClass(bytes, true, MethodHandles.Lookup.ClassOption.NESTMATE);
        MethodHandle handle = defined.findStatic(defined.lookupClass(), "call", MethodType.methodType(String.class));

        doAndPrintTime("ASM", () -> {
            for (int i = 0; i < TEST_TIMES; i++) {
                try {
                    handle.invoke();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public static void main(String[] args) throws Throwable {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        INSTANCE.test();
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
