package com.halofour.functionally.util;

import com.halofour.functionally.util.function.TryBiFunction;
import com.halofour.functionally.util.function.TryFunction;
import com.halofour.functionally.util.function.TrySupplier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasProperty;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class SuccessTest {
    private static final String SUCCESS = "SUCCESS";
    private static final String OTHER = "OTHER";
    private static final String COMBINED = "COMBINED";
    private static final String FAILURE = "FAILURE";
    private static final Exception EXCEPTION = new IllegalArgumentException(FAILURE);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Try<String> underTest;

    @Mock
    private TryFunction<String, String> mapFunction;

    @Mock
    private TryFunction<String, String> mapFunction2;

    @Mock
    private TryFunction<String, Try<String>> flatMapFunction;

    @Mock
    private TryBiFunction<String, String, String> combineMapFunction;

    @Mock
    private TryBiFunction<String, String, Try<String>> combineFlatMapFunction;

    @Mock
    private Predicate<String> predicate;

    @Mock
    private TryFunction<Throwable, String> recoverFunction;

    @Mock
    private TryFunction<Throwable, Try<String>> recoverWithFunction;

    @Mock
    private Consumer<String> ifSuccessConsumer;

    @Mock
    private Consumer<Throwable> ifFailureConsumer;

    @Mock
    private Predicate<Throwable> failurePredicate;

    @Mock
    private TrySupplier<String> supplier;

    @Before
    public void setUp() throws Throwable {
        underTest = Success.of(SUCCESS);
    }

    @Test
    public void testIsSuccess() {
        assertThat(underTest.isSuccess()).isTrue();
    }

    @Test
    public void testIsFailure() {
        assertThat(underTest.isFailure()).isFalse();
    }

    @Test
    public void testIsSpecificFailure() {
        assertThat(underTest.isFailure(Exception.class)).isFalse();
    }

    @Test
    public void testGet() throws Throwable {
        assertThat(underTest.get()).isEqualTo(SUCCESS);
    }

    @Test
    public void testGetException() {
        Optional<Throwable> result = underTest.getException();
        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void testGetOrElse() {
        assertThat(underTest.getOrElse(OTHER)).isEqualTo(SUCCESS);
    }

    @Test
    public void testOrElse() {
        assertThat(underTest.orElse(Try.success(OTHER))).isEqualTo(underTest);
    }

    @Test
    public void testToOptional() {
        Optional<String> result = underTest.toOptional();

        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(SUCCESS);
    }

    @Test
    public void testMap() throws Throwable {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.map(mapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMapThrows() throws Throwable {
        doThrow(EXCEPTION).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.map(mapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testFlatMap() throws Throwable {
        Success<String> expected = Success.of(SUCCESS);
        doReturn(expected).when(flatMapFunction).apply(SUCCESS);

        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isEqualTo(expected);

        verify(flatMapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testFlatMapFailure() throws Throwable {
        Try<String> expected = Failure.of(EXCEPTION);
        doReturn(expected).when(flatMapFunction).apply(SUCCESS);

        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isEqualTo(expected);

        verify(flatMapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testFlatMapThrows() throws Throwable {
        doThrow(EXCEPTION).when(flatMapFunction).apply(SUCCESS);

        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(flatMapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testCombineMap() throws Throwable {
        doReturn(COMBINED).when(combineMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(COMBINED);

        verify(combineMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineMapOtherFailure() throws Throwable {
        Try<String> other = Failure.of(EXCEPTION);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result).isEqualTo(other);

        verify(combineMapFunction, never()).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineMapThrows() throws Throwable {
        doThrow(EXCEPTION).when(combineMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(combineMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMap() throws Throwable {
        doReturn(Success.of(COMBINED)).when(combineFlatMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(COMBINED);

        verify(combineFlatMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMapOtherFailure() throws Throwable {
        Try<String> other = Failure.of(EXCEPTION);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result).isEqualTo(other);

        verify(combineFlatMapFunction, never()).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMapReturnsFailure() throws Throwable {
        doReturn(Failure.of(EXCEPTION)).when(combineFlatMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(combineFlatMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMapThrows() throws Throwable {
        doThrow(EXCEPTION).when(combineFlatMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(combineFlatMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testFilter() {
        doReturn(true).when(predicate).test(SUCCESS);

        Try<String> result = underTest.filter(predicate);

        assertThat(result).isEqualTo(underTest);

        verify(predicate, times(1)).test(SUCCESS);
    }

    @Test
    public void testFilterNotMatched() {
        doReturn(false).when(predicate).test(SUCCESS);

        Try<String> result = underTest.filter(predicate);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException().get()).isInstanceOf(NoSuchElementException.class);

        verify(predicate, times(1)).test(SUCCESS);
    }

    @Test
    public void testFilterThrows() {
        doThrow(EXCEPTION).when(predicate).test(SUCCESS);

        Try<String> result = underTest.filter(predicate);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(predicate, times(1)).test(SUCCESS);

    }

    @Test
    public void testRecoverReturnsSelf() throws Throwable {
        Try<String> result = underTest.recover(recoverFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testRecoverSpecificExceptionReturnsSelf() throws Throwable {
        Try<String> result = underTest.recover(Exception.class, recoverFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testRecoverWithReturnsSelf() throws Throwable {
        Try<String> result = underTest.recoverWith(recoverWithFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testRecoverWithSpecificExceptionReturnsSelf() throws Throwable {
        Try<String> result = underTest.recoverWith(Exception.class, recoverWithFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testFold() throws Throwable {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.fold(recoverFunction, mapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testFoldThrows() throws Throwable {
        doThrow(EXCEPTION).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.fold(recoverFunction, mapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));

        verify(mapFunction, times(1)).apply(SUCCESS);
        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testFailed() {
        Try<Throwable> inverted = underTest.failed();

        assertThat(inverted).isInstanceOf(Failure.class);
        assertThat(inverted.getException().get()).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void testIfSuccess() {
        underTest.ifSuccess(ifSuccessConsumer);

        verify(ifSuccessConsumer, times(1)).accept(SUCCESS);
    }

    @Test
    public void testIfFailure() {
        underTest.ifFailure(ifFailureConsumer);

        verify(ifFailureConsumer, never()).accept(any());
    }

    @Test
    public void testIfSpecificFailure() {
        underTest.ifFailure(Exception.class, ifFailureConsumer);

        verify(ifFailureConsumer, never()).accept(any());
    }

    @Test
    public void testMatchAnySuccess() throws Throwable {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .success(mapFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMatchSpecificSuccess() throws Throwable {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .success(SUCCESS, mapFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMatchSpecificSuccessMismatch() throws Throwable {
        underTest.match(m -> m
                .success(OTHER, mapFunction)
                .orElseSuccess(OTHER)
        );

        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testMatchSpecificSuccessNullMismatch() throws Throwable {
        underTest.match(m -> m
                .success(null, mapFunction)
                .orElseSuccess(OTHER)
        );

        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testMatchSuccessWhen() throws Throwable {
        doReturn(true).when(predicate).test(SUCCESS);
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .successWhen(predicate, mapFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMatchSuccessWhenDoesNotMatch() throws Throwable {
        doReturn(false).when(predicate).test(SUCCESS);
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        underTest.match(m -> m
                .successWhen(predicate, mapFunction)
                .orElseSuccess(OTHER)
        );

        verify(predicate, times(1)).test(SUCCESS);
        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testMatchSpecificSuccessMismatchFallthrough() throws Throwable {
        doReturn(OTHER).when(mapFunction2).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .success(OTHER, mapFunction)
                .success(mapFunction2)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, never()).apply(any());
        verify(mapFunction2, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMatchSpecificSuccessOrdered() throws Throwable {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .success(mapFunction)
                .success(SUCCESS, mapFunction2)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
        verify(mapFunction2, never()).apply(any());
    }

    @Test
    public void testMatchFailureDoesNotMatch() throws Throwable {
        underTest.match(m -> m
                .failure(recoverWithFunction)
                .orElseSuccess(OTHER)
        );

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testMatchFailureSpecificExceptionDoesNotMatch() throws Throwable {
        underTest.match(m -> m
                .failure(Exception.class, recoverWithFunction)
                .orElseSuccess(OTHER)
        );

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testMatchFailureWhenDoesNotMatch() throws Throwable {
        underTest.match(m -> m
                .failureWhen(failurePredicate, recoverFunction)
                .orElseSuccess(OTHER)
        );

        verify(failurePredicate, never()).test(any());
        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testMatchOrElse() throws Throwable {
        doReturn(SUCCESS).when(supplier).get();

        Try<String> result = underTest.match(m -> m.orElse(supplier));

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(supplier, times(1)).get();
    }

    @Test
    public void testMatchOrElseSuccess() throws Throwable {
        Try<String> result = underTest.match(m -> m.orElseSuccess(SUCCESS));

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);
    }

    @Test
    public void testMatchOrElseFailureDoesNotMatch() throws Throwable {
        Try<String> result = underTest.match(m -> m
                .orElseFailure(EXCEPTION)
        );

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(EXCEPTION));
    }

    @Test
    public void testMatchNoMatchingPatterns() {
        expectedException.expect(UnmatchedPatternException.class);
        expectedException.expect(hasProperty("actualValue", equalTo(underTest)));

        underTest.match(m -> m.failure(recoverFunction));
    }

    @Test
    public void testToString() {
        assertThat(underTest.toString()).startsWith("Success[");
    }

    @Test
    public void testHashCode() {
        assertThat(underTest.hashCode()).isEqualTo(SUCCESS.hashCode());
    }

    @Test
    public void testEqualsSameValue() {
        Try<String> other = Try.success(SUCCESS);

        assertThat(underTest.equals(other)).isTrue();
    }

    @Test
    public void testEqualsDifferentValue() {
        Try<String> other = Try.success(OTHER);

        assertThat(underTest.equals(other)).isFalse();
    }

    @Test
    public void testEqualsNullValue() {
        Try<String> other = Try.success(null);

        assertThat(underTest.equals(other)).isFalse();
    }

    @Test
    public void testEqualsNull() {
        assertThat(underTest.equals(null)).isFalse();
    }

    @Test
    public void testEqualsOtherObject() {
        assertThat(underTest.equals(new Object())).isFalse();
    }
}
