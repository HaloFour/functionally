package com.halofour.functionally.util.function;

@FunctionalInterface
public interface TryConsumer<T> {
    void accept(T value) throws Throwable;
}
