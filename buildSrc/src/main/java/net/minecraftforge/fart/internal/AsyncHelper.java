/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */
package net.minecraftforge.fart.internal;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

class AsyncHelper {
    private final @NotNull ExecutorService exec;
    AsyncHelper(int threads) {
        if (threads <= 0)
            throw new IllegalArgumentException("Really.. no threads to process things? What do you want me to use a genie?");
        else if (threads == 1)
            exec = Executors.newSingleThreadExecutor();
        else
            exec = Executors.newWorkStealingPool(threads);
    }

    public <I> void consumeAll(@NotNull Collection<? extends I> inputs, @NotNull Function<I, String> namer, @NotNull Consumer<I> consumer) {
        Function<I, Pair<String, Callable<Void>>> toCallable = i -> new Pair<>(namer.apply(i), () -> {
            consumer.accept(i);
            return null;
        });
        invokeAll(inputs.stream().map(toCallable).collect(Collectors.toList()));
    }

    public <I,O> @NotNull List<O> invokeAll(@NotNull Collection<? extends I> inputs, @NotNull Function<I, String> namer, @NotNull Function<I, O> converter) {
        Function<I, Pair<String, Callable<O>>> toCallable = i -> new Pair<>(namer.apply(i), () -> converter.apply(i));
        return invokeAll(inputs.stream().map(toCallable).collect(Collectors.toList()));
    }

    public <O> @NotNull List<O> invokeAll(@NotNull Collection<Pair<String, ? extends Callable<O>>> tasks) {
            List<O> ret = new ArrayList<>(tasks.size());
            List<Pair<String, Future<O>>> processed = new ArrayList<>(tasks.size());
            for (Pair<String, ? extends Callable<O>> task : tasks) {
                processed.add(new Pair<>(task.getLeft(), exec.submit(task.getRight())));
            }
            for (Pair<String, Future<O>> future : processed) {
                try {
                    O done = future.getRight().get();
                    if (done != null)
                        ret.add(done);
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException("Failed to execute task " + future.getLeft(), e);
                }
            }
            return ret;
    }

    public void shutdown() {
        exec.shutdown();
    }
}
