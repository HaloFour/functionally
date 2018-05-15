package com.halofour.functionally.util;

import java.io.Serializable;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * The {@link Try} type represents a computation that may either result
 * in an exception, or return a successfully computed value.
 *
 * @param <T> the type of the successfully computed value
 */
public interface Try<T> extends Serializable {
    /**
     * Returns {@code true} if computation completed successfully; otherwise, returns {@code false}.
     * @return {@code true} is successful
     */
    boolean isSuccess();

    /**
     * Returns {@code true} if failed to complete successfully; otherwise, returns {@code false}.
     * @return {@code true} if failed
     */
    boolean isFailure();

    /**
     * Returns {@code true} if failed to complete successfully with the specified exception class; otherwise, returns @{code false}.
     * @param exceptionClass the class of the exception
     * @return {@code true} if failed with the specified exception class
     */
    boolean isFailure(Class<? extends Exception> exceptionClass);

    /**
     * Returns the value of the computation if successful; otherwise, throws the exception
     * @return the value of the computation
     * @throws Exception the exception which caused the computation to fail
     */
    T get() throws Exception;

    /**
     * Returns the exception of the computation if it failed; otherwise {@link Optional#empty()}
     * @return the exception of the failed computation
     */
    Optional<Exception> getException();

    /**
     * Returns the value of the computation if successful; otherwise, returns {@code defaultValue}
     * @param defaultValue The default value to return if the computation failed
     * @return the value of the computation or the {@code defaultValue}
     */
    T getOrElse(T defaultValue);

    /**
     * Returns this instance if the computation is successful; otherwise, returns {@code other}
     * @param other The other computation
     * @return the current computation or the {@code other}
     */
    Try<T> orElse(Try<T> other);

    /**
     * Returns the value of the computation if successful; otherwise, returns {@link Optional#empty()}
     * @return An {@link Optional} of the value of the computation
     */
    Optional<T> toOptional();

    /**
     * Maps the value of the computation using the {@code function} if successful; otherwise, returns {@code this}
     * @param function the function to compute the value of the returned {@link Try}
     * @param <R> the function's return type
     * @return the computation of the {@code function} applied to the value of the current computation
     */
    <R> Try<R> map(TryFunction<? super T, ? extends R> function);

    /**
     * Maps the value of the computation to the return value of {@code function} if successful; otherwise, returns {@code this}
     * @param function the function to compute the returned {@link Try}
     * @param <R> the function's return type
     * @return the return value from {@code function}
     */
    <R> Try<R> flatMap(TryFunction<? super T, Try<R>> function);

    /**
     * Maps the values of both computations using the {@code function} if both are successful; otherwise returns the failed computation
     * @param other the other computation
     * @param function the function to compute the value of the returned {@link Try}
     * @param <U> the type of the other computation
     * @param <R> the function's return type
     * @return the computation of the {@code function} applied to the value of both computations
     */
    <U, R> Try<R> combineMap(Try<U> other, TryBiFunction<? super T, ? super U, ? extends R> function);

    /**
     * Maps the value of both computations to the return value of {@code function} if both are successful; otherwise returns the failed computation
     * @param other the other computation
     * @param function the function to compute the returned {@link Try}
     * @param <U> the type of the other computation
     * @param <R> the function's return type
     * @return the return value from {@code function}
     */
    <U, R> Try<R> combineFlatMap(Try<U> other, TryBiFunction<? super T, ? super U, Try<R>> function);

    /**
     * Filters the value of this successful operation based on the {@code predicate} returning a {@link Failure}
     * of {@link java.util.NoSuchElementException} if the predicate does not match.
     * @param predicate the predicate to apply to the computation
     * @return {@code this} if the predicate matches;
     */
    Try<T> filter(Predicate<T> predicate);

    /**
     * Applies the given {@code function} to the computation if it had failed; otherwise, returns {@code this}
     * @param function the function to apply to the exception of the failed computation
     * @return the computation of the {@code function} applied to the exception of the failed computation
     */
    Try<T> recover(TryFunction<? super Exception, ? extends T> function);

    /**
     * Applies the given {@code function} to the computation if it has failed with the specified exception class; otherwise, returns {@code this}
     * @param exceptionClass the class of the exception
     * @param function the function to apply to the exception of the failed computation
     * @param <E> the type of the exception
     * @return the computation of the {@code function} applied to the exception of the failed computation
     */
    <E extends Exception> Try<T> recover(Class<E> exceptionClass, TryFunction<? super E, ? extends T> function);

    /**
     * Applies the returm value of {@code function} to the computation if it has failed; otherwise, returns {@code this}
     * @param function the function to apply to the exception of the failed computation
     * @return the return value of {@code function}
     */
    Try<T> recoverWith(TryFunction<? super Exception, Try<T>> function);

    /**
     * Applies the return value of {@code function} to the computation if it has failed with the specified exception class; otherwise, returns {@code this}
     * @param exceptionClass the class of the exception
     * @param function the function to apply to the exception of the failed computation
     * @param <E> the type of the exception
     * @return the return value of {@code function}
     */
    <E extends Exception> Try<T> recoverWith(Class<E> exceptionClass, TryFunction<? super E, Try<T>> function);

    /**
     * Applies the function {@code onSuccess} if the computation was successful; otherwise, applies the function {@code onFailure}
     * @param onFailure the function to apply to the exception of the failed computation
     * @param onSuccess the function to apply to the result of the successful computation
     * @param <R> the return type of the functions
     * @return the results of applying either {@code onSuccess} or {@code onFailure}
     */
    <R> Try<R> fold(TryFunction<? super Exception, ? extends R> onFailure, TryFunction<? super T, ? extends R> onSuccess);

    /**
     * Inverts the failed computation to the successful computation of the exception
     * @return the successful computation
     */
    Try<Exception> failed();

    /**
     * Invokes the {@code consumer} with the computed value if the computation is successful.
     * @param consumer the consumer function
     */
    void ifSuccess(Consumer<? super T> consumer);

    /**
     * Invokes the {@code consumer} with the exception if the computation had failed
     * @param consumer the consumer function
     */
    void ifFailure(Consumer<? super Exception> consumer);

    /**
     * Invokes the {@code consumer} with the exception if the computation had failed with the specified exception
     * @param exceptionClass the class of the exception
     * @param consumer the consumer function
     * @param <E> the type of the exception
     */
    <E extends Exception> void ifFailure(Class<E> exceptionClass, Consumer<? super E> consumer);

    /**
     * Transforms the computation using the translation function matching the result
     * @param match the consumer of the {@link TryMatcher} used to match the result of the computation
     * @param <R> the result type of the translation
     * @return the computation based on the translation
     */
    <R> Try<R> match(Consumer<TryMatcher<T, R>> match);

    /**
     * Returns a {@link Success} computation of the given {@code value}
     * @param value the value of the successful computation
     * @param <T> the type of the value
     * @return the successful computation
     */
    static <T> Success<T> success(T value) {
        return Success.of(value);
    }

    /**
     * Returns a {@link Failure} computation for the given {@link Exception}
     * @param exception the exception
     * @param <T> the type of the value of the computation
     * @return the failed computation
     */
    static <T> Failure<T> failure(Exception exception) {
        return Failure.of(exception);
    }

    /**
     * Returns a {@link Failure} computation for the given {@link Exception}
     * @param valueClass the class of the value of the computation, used for generic inference
     * @param exception the exception
     * @param <T> the type of the value of the computation
     * @return the failed computation
     */
    static <T> Failure<T> failure(Class<T> valueClass, Exception exception) {
        return Failure.of(valueClass, exception);
    }

    /**
     * Returns a computation wrapping the result or exception of the {@code supplier}
     * @param supplier the source of the computation
     * @param <T> the type of the value of the computation
     * @return the computation
     */
    static <T> Try<T> from(TrySupplier<? extends T> supplier) {
        try {
            return Success.of(supplier.get());
        } catch (Exception exception) {
            return Failure.of(exception);
        }
    }
}
