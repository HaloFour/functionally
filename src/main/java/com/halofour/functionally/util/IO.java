package com.halofour.functionally.util;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.halofour.functionally.util.function.TryConsumer;
import com.halofour.functionally.util.function.TryFunction;
import com.halofour.functionally.util.function.TrySupplier;

public final class IO<T> {
    private final TrySupplier<CompletionStage<T>> parent;

    private IO(TrySupplier<CompletionStage<T>> parent) {
        this.parent = parent;
    }

    public T get() throws Throwable {
        return parent.get()
                .toCompletableFuture()
                .get();
    }

    public <R> IO<R> map(TryFunction<? super T, ? extends R> function) {
        return new IO<>(() -> parent.get().thenCompose(value -> {
            try {
                R result = function.apply(value);
                return CompletableFuture.completedFuture(result);
            } catch (Throwable exception) {
                return IO.exceptionally(exception);
            }
        }));
    }

    public <R> IO<R> flatMap(TryFunction<? super T, IO<R>> function) {
        return new IO<>(() -> parent.get()
                .thenCompose(value -> {
                    try {
                        IO<R> io = function.apply(value);
                        return io.parent.get();
                    } catch (Throwable exception) {
                        return IO.exceptionally(exception);
                    }
                }));
    }

    public static <T> IO<T> apply(TrySupplier<? extends T> supplier) {
        return async(() -> CompletableFuture.completedFuture(supplier.get()));
    }

    public static <T> IO<T> async(TrySupplier<CompletionStage<T>> supplier) {
        return new IO<>(supplier);
    }

    public static <T> IO<T> async(TryConsumer<TryConsumer<Try<T>>> callback) {
        return async(() -> {
            CompletableFuture<T> future = new CompletableFuture<>();
            callback.accept(completed -> completed.fold(future::completeExceptionally, future::complete));
            return future;
        });
    }

    private static <R> CompletionStage<R> exceptionally(Throwable exception) {
        CompletableFuture<R> future = new CompletableFuture<>();
        future.completeExceptionally(exception);
        return future;
    }
}
