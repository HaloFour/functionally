package com.halofour.functionally.util;

/**
 * Represents a function that accepts one argument and produces a result and can fail with an exception.
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
@FunctionalInterface
public interface TryFunction<T, R> {
    /**
     * Applies this function to the given argument.
     * @param value the function argument
     * @return the function result
     * @throws Exception an exception
     */
    R apply(T value) throws Exception;
}
