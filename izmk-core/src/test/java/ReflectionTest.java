public class ReflectionTest {
    private static final String staticFinalField = new String("staticFinalField");
    private static Object staticField = new String("staticField");
    private final Object finalField = new String("finalField");
    private Object instanceField = new String("instanceField");

    private static final ReflectionTest instance = new ReflectionTest();
    private static final int TEST_COUNT = 10000;

    private Object empty(Object s) {
        return s;
    }

    private void test() {
        long d1 = doAndPrintTime("direct get staticFinalField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(staticFinalField);
            }
        });
        // cache warmup
        ReflectionUtil2.getField(null, "staticFinalField", "ReflectionTest");
        long d2 = doAndPrintTime("ReflectionUtil get staticFinalField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(ReflectionUtil2.getField(null, "staticFinalField", "ReflectionTest"));
            }
        });
        System.out.printf("ReflectionUtil is %.2f times slower than direct access\n\n", (d2 - d1) / (double) d1);

        // test staticField
        long d3 = doAndPrintTime("direct get staticField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(staticField);
            }
        });
        ReflectionUtil2.getField(null, "staticField", "ReflectionTest");
        long d4 = doAndPrintTime("ReflectionUtil get staticField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(ReflectionUtil2.getField(null, "staticField", "ReflectionTest"));
            }
        });
        System.out.printf("ReflectionUtil is %.2f times slower than direct access\n\n", (d4 - d3) / (double) d3);

        // test finalField
        long d5 = doAndPrintTime("direct get finalField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(finalField);
            }
        });
        ReflectionUtil2.getField(instance, "finalField", "ReflectionTest");
        long d6 = doAndPrintTime("ReflectionUtil get finalField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(ReflectionUtil2.getField(instance, "finalField", "ReflectionTest"));
            }
        });
        System.out.printf("ReflectionUtil is %.2f times slower than direct access\n\n", (d6 - d5) / (double) d5);

        // test instanceField
        long d7 = doAndPrintTime("direct get instanceField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(instanceField);
            }
        });
        ReflectionUtil2.getField(instance, "instanceField", "ReflectionTest");
        long d8 = doAndPrintTime("ReflectionUtil get instanceField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                empty(ReflectionUtil2.getField(instance, "instanceField", "ReflectionTest"));
            }
        });
        System.out.printf("ReflectionUtil is %.2f times slower than direct access\n\n", (d8 - d7) / (double) d7);

        // test set staticField
        String newValue = "new value";

        long d9 = doAndPrintTime("direct set staticField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                staticField = newValue;
            }
        });
        ReflectionUtil2.setFieldStatic(newValue, "staticField", "ReflectionTest");
        long d10 = doAndPrintTime("ReflectionUtil set staticField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                ReflectionUtil2.setFieldStatic(newValue, "staticField", "ReflectionTest");
            }
        });
        System.out.printf("ReflectionUtil is %.2f times slower than direct access\n\n", (d10 - d9) / (double) d9);

        // test set finalField
//        doAndPrintTime("direct set finalField x" + TEST_COUNT, () -> {
//            for (int i = 0; i < TEST_COUNT; i++) {
//                finalField = newValue;
//            }
//        });
        ReflectionUtil2.setFieldFinal(instance, newValue, "finalField");
        doAndPrintTime("ReflectionUtil set finalField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                ReflectionUtil2.setFieldFinal(instance, newValue, "finalField");
            }
        });

        // test set instanceField
        long d11 = doAndPrintTime("direct set instanceField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                instanceField = newValue;
            }
        });
        ReflectionUtil2.setField(instance, newValue, "instanceField", "ReflectionTest");
        long d12 = doAndPrintTime("ReflectionUtil set instanceField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                ReflectionUtil2.setField(instance, newValue, "instanceField", "ReflectionTest");
            }
        });
        System.out.printf("ReflectionUtil is %.2f times slower than direct access\n\n", (d12 - d11) / d11 + 0f);

        // test set staticFinalField
//        doAndPrintTime("direct set staticFinalField x" + TEST_COUNT, () -> {
//            for (int i = 0; i < TEST_COUNT; i++) {
//                staticFinalField = newValue;
//            }
//        });
        ReflectionUtil2.setFieldFinalStatic(newValue, "staticFinalField", "ReflectionTest");
        doAndPrintTime("ReflectionUtil set staticFinalField x" + TEST_COUNT, () -> {
            for (int i = 0; i < TEST_COUNT; i++) {
                ReflectionUtil2.setFieldFinalStatic(newValue, "staticFinalField", "ReflectionTest");
            }
        });
    }

    public static void main(String[] args) throws Exception {
        instance.test();
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
