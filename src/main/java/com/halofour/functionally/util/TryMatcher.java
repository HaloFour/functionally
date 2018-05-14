package com.halofour.functionally.util;

import java.util.function.Predicate;

/**
 * Configures a matcher to translate the expected results of a computation
 * @param <T> the type of the value of the computation
 * @param <R> the type of the translated value
 */
public interface TryMatcher<T, R> {
    /**
     * Matches on any successful computation
     * @param function the function to apply to the successful value
     * @return the translated value
     */
    default TryMatcher<T, R> success(TryFunction<? super T, ? extends R> function) {
        return this;
    }

    /**
     * Matches on a successful computation of a specified value
     * @param value the expected successful value
     * @param function the function to apply to the successful value
     * @return the translated value
     */
    default TryMatcher<T, R> success(T value, TryFunction<? super T, ? extends R> function) {
        return this;
    }

    /**
     * Matches on a successful computation of a value satisfying the {@code predicate}
     * @param predicate the predicate to match against the successful value
     * @param function the function to apply to the successful value
     * @return the translated value
     */
    default TryMatcher<T, R> successWhen(Predicate<T> predicate, TryFunction<? super T, ? extends R> function) {
        return this;
    }

    /**
     * Matches on a failed computation
     * @param function the function to apply to the exception
     * @return the translated value
     */
    default TryMatcher<T, R> failure(TryFunction<Exception, ? extends R> function) {
        return this;
    }

    /**
     * Matches on a failed computation of the given exception class
     * @param exceptionClass the class of the exception
     * @param function the function to apply to the exception
     * @param <E> the type of the exception
     * @return the translated value
     */
    default <E extends Exception> TryMatcher<T, R> failure(Class<E> exceptionClass, TryFunction<? super E, ? extends R> function) {
        return this;
    }

    /**
     * Matches on a failed computation and returns the supplied value
     * @param supplier the supplier of the value
     */
    default void orElse(TrySupplier<? extends R> supplier) { }

    /**
     * Matches on a failed computation and returns the {@code defaultValue}
     * @param defaultValue the default value
     */
    default void orElseSuccess(R defaultValue) { }

    /**
     * Matches on a failed computation with another failed computation
     * @param exception the exception of the failed computation
     */
    default void orElseFailure(Exception exception) { }
}
