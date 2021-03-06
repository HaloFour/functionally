package com.halofour.functionally.util;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.halofour.functionally.util.function.TryBiFunction;
import com.halofour.functionally.util.function.TryFunction;
import com.halofour.functionally.util.function.TrySupplier;

/**
 * Represents a computation that has failed with an exception
 * @param <T> the type of the result of the computation had it been successful
 */
public final class Failure<T> implements Try<T> {
    private static final long serialVersionUID = 2452948373057856082L;

    private final Throwable exception;

    private Failure(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public boolean isFailure(Class<? extends Throwable> exceptionClass) {
        return exceptionClass.isInstance(exception);
    }

    @Override
    public T get() throws Throwable {
        throw exception;
    }

    @Override
    public Optional<Throwable> getException() {
        return Optional.of(exception);
    }

    @Override
    public T getOrElse(T defaultValue) {
        return defaultValue;
    }

    @Override
    public Try<T> orElse(Try<T> other) {
        return other;
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.empty();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Failure<R> map(TryFunction<? super T, ? extends R> function) {
        return (Failure<R>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> Failure<R> flatMap(TryFunction<? super T, Try<R>> function) {
        return (Failure<R>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U, R> Try<R> combineMap(Try<U> other, TryBiFunction<? super T, ? super U, ? extends R> function) {
        return (Failure<R>) this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U, R> Try<R> combineFlatMap(Try<U> other, TryBiFunction<? super T, ? super U, Try<R>> function) {
        return (Failure<R>) this;
    }

    @Override
    public Try<T> filter(Predicate<T> predicate) {
        return this;
    }

    @Override
    public Try<T> recover(TryFunction<? super Throwable, ? extends T> function) {
        return recover(Throwable.class, function);
    }

    @Override
    public <E extends Throwable> Try<T> recover(Class<E> exceptionClass, TryFunction<? super E, ? extends T> function) {
        if (exceptionClass.isInstance(exception)) {
            try {
                return Success.of(function.apply(exceptionClass.cast(exception)));
            } catch (Throwable exception) {
                return Failure.of(exception);
            }
        }
        return this;
    }

    @Override
    public Try<T> recoverWith(TryFunction<? super Throwable, Try<T>> function) {
        return recoverWith(Throwable.class, function);
    }

    @Override
    public <E extends Throwable> Try<T> recoverWith(Class<E> exceptionClass, TryFunction<? super E, Try<T>> function) {
        if (exceptionClass.isInstance(exception)) {
            try {
                return function.apply(exceptionClass.cast(exception));
            } catch (Throwable exception) {
                return Failure.of(exception);
            }
        }
        return this;
    }

    @Override
    public <R> Try<R> fold(TryFunction<? super Throwable, ? extends R> onFailure, TryFunction<? super T, ? extends R> onSuccess) {
        try {
            return Success.of(onFailure.apply(exception));
        } catch (Throwable exception) {
            return Failure.of(exception);
        }
    }

    @Override
    public Try<Throwable> failed() {
        return Success.of(exception);
    }

    @Override
    public void ifSuccess(Consumer<? super T> consumer) { }

    @Override
    public void ifFailure(Consumer<? super Throwable> consumer) {
        consumer.accept(exception);
    }

    @Override
    public <E extends Throwable> void ifFailure(Class<E> exceptionClass, Consumer<? super E> consumer) {
        if (exceptionClass.isInstance(exception)) {
            consumer.accept(exceptionClass.cast(exception));
        }
    }

    @Override
    public <R> Try<R> match(Consumer<TryMatcher<T, R>> match) {
        Matcher<T, R> matcher = new Matcher<>(this);
        match.accept(matcher);

        return Optional.ofNullable(matcher.result)
                .orElseThrow(() -> new UnmatchedPatternException(Failure.this));
    }

    @Override
    public int hashCode() {
        return exception.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Failure) {
            Failure<?> other = (Failure<?>) obj;

            if (exception == other.exception) {
                return true;
            }
            return exception.equals(other.exception);
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format("Failure[%s]", exception);
    }

    /**
     * Creates a {@link Failure} computation of the given {@code exception}
     * @param exception the exception
     * @param <T> the type of the result of the computation had it been successful
     * @return a {@link Failure}
     */
    public static <T> Failure<T> of(Throwable exception) {
        Objects.requireNonNull(exception, "exception must not be null.");
        if (isFatal(exception)) {
            rethrow(exception);
        }
        return new Failure<>(exception);
    }

    /**
     * Evaluates if a given exception is non-fatal and can be wrapped by a {@link Failure}
     * @param exception the exception
     * @return {@code true} if the exception is non-fatal; otherwise, {@code false}
     */
    public static boolean isNonFatal(Throwable exception) {
        return !isFatal(exception);
    }

    /**
     * Evaluates if a given exception is fatal and cannot be wrapped by a {@link Failure}
     * @param exception the exception
     * @return {@code true} if the exception is fatal; otherwise, {@code false}
     */
    public static boolean isFatal(Throwable exception) {
        return exception instanceof VirtualMachineError ||
                exception instanceof ThreadDeath ||
                exception instanceof LinkageError ||
                exception instanceof InterruptedException;
    }

    /**
     * Rethrows the given exception, wrapping it in a {@link RuntimeException} if necessary
     * @param exception the exception to rethrow
     */
    public static void rethrow(Throwable exception) {
        if (exception instanceof RuntimeException) {
            throw (RuntimeException) exception;
        } else if (exception instanceof Error) {
            throw (Error) exception;
        } else {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Creates a {@link Failure computation of the given {@code exception}
     * @param valueClass the class of the result of the computation, for generic inference
     * @param exception the exception
     * @param <T> the type of the result of the computation had it been successful
     * @return a {@link Failure}
     */
    public static <T> Failure<T> of(Class<T> valueClass, Throwable exception) {
        return Failure.of(exception);
    }

    private static final class Matcher<T, R> implements TryMatcher<T, R> {
        private final Failure<R> failure;
        private Try<R> result;

        @SuppressWarnings("unchecked")
        Matcher(Failure<T> failure) {
            this.failure = (Failure<R>) failure;
        }

        @Override
        public TryMatcher<T, R> failure(TryFunction<Throwable, ? extends R> function) {
            if (result == null) {
                result = failure.recover(function);
            }
            return this;
        }

        @Override
        public <E extends Throwable> TryMatcher<T, R> failure(Class<E> exceptionClass, TryFunction<? super E, ? extends R> function) {
            if (result == null && exceptionClass.isInstance(failure.exception)) {
                result = failure.recover(exceptionClass, function);
            }
            return this;
        }

        @Override
        public TryMatcher<T, R> failureWhen(Predicate<? super Throwable> predicate, TryFunction<? super Throwable, ? extends R> function) {
            if (result == null && predicate.test(failure.exception)) {
                result = failure.recover(function);
            }
            return this;
        }

        @Override
        public void orElse(TrySupplier<? extends R> supplier) {
            if (result == null) {
                result = Try.from(supplier);
            }
        }

        @Override
        public void orElseSuccess(R defaultValue) {
            if (result == null) {
                result = Try.success(defaultValue);
            }
        }

        @Override
        public void orElseFailure(Throwable exception) {
            if (result == null) {
                result = Try.failure(exception);
            }
        }
    }
}
