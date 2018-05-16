package com.halofour.functionally.util.function;

/**
 * Represents a function that accepts two arguments and produces a result. This is the two-arity specialization of {@link TryFunction}.
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface TryBiFunction<T, U, R> {
    /**
     * Applies this function to the given arguments.
     * @param value1 the first argument
     * @param value2 the second argument
     * @return the function result
     * @throws Throwable an exception
     */
    R apply(T value1, U value2) throws Throwable;
}
