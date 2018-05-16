package com.halofour.functionally.util;

import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.halofour.functionally.util.function.TryBiFunction;
import com.halofour.functionally.util.function.TryFunction;
import com.halofour.functionally.util.function.TrySupplier;

/**
 * Represents a computation that has completed successfully with a value
 * @param <T> the type of the value of the computation
 */
public final class Success<T> implements Try<T>, Serializable {
    private static final long serialVersionUID = -3845326151749903251L;

    private final T value;

    private Success(T value) {
        this.value = value;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public boolean isFailure(Class<? extends Throwable> exceptionClass) {
        return false;
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public Optional<Throwable> getException() {
        return Optional.empty();
    }

    @Override
    public T getOrElse(T defaultValue) {
        return value;
    }

    @Override
    public Success<T> orElse(Try<T> other) {
        return this;
    }

    @Override
    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    @Override
    public <R> Try<R> map(TryFunction<? super T, ? extends R> function) {
        try {
            return Success.of(function.apply(value));
        } catch (Throwable exception) {
            return Failure.of(exception);
        }
    }

    @Override
    public <R> Try<R> flatMap(TryFunction<? super T, Try<R>> function) {
        try {
            return function.apply(value);
        } catch (Throwable exception) {
            return Failure.of(exception);
        }
    }

    @Override
    public <U, R> Try<R> combineMap(Try<U> other, TryBiFunction<? super T, ? super U, ? extends R> function) {
        return other.map(otherValue -> function.apply(value, otherValue));
    }

    @Override
    public <U, R> Try<R> combineFlatMap(Try<U> other, TryBiFunction<? super T, ? super U, Try<R>> function) {
        return other.flatMap(otherValue -> function.apply(value, otherValue));
    }

    @Override
    public Try<T> filter(Predicate<T> predicate) {
        try {
            if (predicate.test(value)) {
                return this;
            }
            return Failure.of(new NoSuchElementException("The value did not meet the predicate."));
        } catch (Throwable exception) {
            return Failure.of(exception);
        }
    }

    @Override
    public Success<T> recover(TryFunction<? super Throwable, ? extends T> function) {
        return this;
    }

    @Override
    public <E extends Throwable> Success<T> recover(Class<E> exceptionClass, TryFunction<? super E, ? extends T> function) {
        return this;
    }

    @Override
    public Success<T> recoverWith(TryFunction<? super Throwable, Try<T>> function) {
        return this;
    }

    @Override
    public <E extends Throwable> Success<T> recoverWith(Class<E> exceptionClass, TryFunction<? super E, Try<T>> function) {
        return this;
    }

    @Override
    public <R> Try<R> fold(TryFunction<? super Throwable, ? extends R> onFailure, TryFunction<? super T, ? extends R> onSuccess) {
        return map(onSuccess);
    }

    @Override
    public Failure<Throwable> failed() {
        return Failure.of(new UnsupportedOperationException("Cannot invert Success."));
    }

    @Override
    public void ifSuccess(Consumer<? super T> consumer) {
        consumer.accept(value);
    }

    @Override
    public void ifFailure(Consumer<? super Throwable> consumer) { }

    @Override
    public <E extends Throwable> void ifFailure(Class<E> exceptionClass, Consumer<? super E> consumer) { }

    @Override
    public <R> Try<R> match(Consumer<TryMatcher<T, R>> match) {
        Matcher<T, R> matcher = new Matcher<>(this);
        match.accept(matcher);

        return Optional.ofNullable(matcher.result)
                .orElseThrow(() -> new UnmatchedPatternException(Success.this));
    }

    @Override
    public int hashCode() {
        return (value != null) ? value.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof Success) {
            Success<?> other = (Success<?>) obj;
            if (other.value == value) {
                return true;
            } else if (other.value == null || value == null) {
                return false;
            }
            return value.equals(other.value);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Success[%s]", value);
    }

    /**
     * Creates a {@link Success} computation of the given {@code value}
     * @param value the value of the computation
     * @param <T> the type of the value of the computation
     * @return the {@link Success}
     */
    public static <T> Success<T> of(T value) {
        return new Success<>(value);
    }

    private static final class Matcher<T, R> implements TryMatcher<T, R> {
        private final Success<T> success;
        private Try<R> result;

        Matcher(Success<T> success) {
            this.success = success;
        }

        @Override
        public TryMatcher<T, R> success(TryFunction<? super T, ? extends R> function) {
            if (result == null) {
                result = success.map(function);
            }
            return this;
        }

        @Override
        public TryMatcher<T, R> success(T value, TryFunction<? super T, ? extends R> function) {
            if (result == null && equals(value, success.value)) {
                result = success.map(function);
            }
            return this;
        }

        @Override
        public TryMatcher<T, R> successWhen(Predicate<T> predicate, TryFunction<? super T, ? extends R> function) {
            if (result == null && predicate.test(success.value)) {
                result = success.map(function);
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
                result = Success.of(defaultValue);
            }
        }

        @Override
        public void orElseFailure(Throwable exception) {
            if (result == null) {
                result = Failure.of(exception);
            }
        }

        private boolean equals(T left, T right) {
            if (left == right) {
                return true;
            } else if (left == null || right == null) {
                return false;
            }
            return left.equals(right);
        }
    }
}
