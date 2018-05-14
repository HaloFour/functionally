package com.halofour.functionally.util;

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
    private TryFunction<Exception, String> recoverFunction;

    @Mock
    private TryFunction<Exception, Try<String>> recoverWithFunction;

    @Mock
    private Consumer<String> ifSuccessConsumer;

    @Mock
    private Consumer<Exception> ifFailureConsumer;

    @Before
    public void setUp() throws Exception {
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
    public void testGet() throws Exception {
        assertThat(underTest.get()).isEqualTo(SUCCESS);
    }

    @Test
    public void testGetException() {
        Optional<Exception> result = underTest.getException();
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
    public void testMap() throws Exception {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.map(mapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMapThrows() throws Exception {
        doThrow(EXCEPTION).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.map(mapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException().get()).isEqualTo(EXCEPTION);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testFlatMap() throws Exception {
        Success<String> expected = Success.of(SUCCESS);
        doReturn(expected).when(flatMapFunction).apply(SUCCESS);

        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isEqualTo(expected);

        verify(flatMapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testFlatMapFailure() throws Exception {
        Try<String> expected = Failure.of(EXCEPTION);
        doReturn(expected).when(flatMapFunction).apply(SUCCESS);

        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isEqualTo(expected);

        verify(flatMapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testFlatMapThrows() throws Exception {
        doThrow(EXCEPTION).when(flatMapFunction).apply(SUCCESS);

        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException().get()).isEqualTo(EXCEPTION);

        verify(flatMapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testCombineMap() throws Exception {
        doReturn(COMBINED).when(combineMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(COMBINED);

        verify(combineMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineMapOtherFailure() throws Exception {
        Try<String> other = Failure.of(EXCEPTION);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result).isEqualTo(other);

        verify(combineMapFunction, never()).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineMapThrows() throws Exception {
        doThrow(EXCEPTION).when(combineMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException().get()).isEqualTo(EXCEPTION);

        verify(combineMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMap() throws Exception {
        doReturn(Success.of(COMBINED)).when(combineFlatMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(COMBINED);

        verify(combineFlatMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMapOtherFailure() throws Exception {
        Try<String> other = Failure.of(EXCEPTION);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result).isEqualTo(other);

        verify(combineFlatMapFunction, never()).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMapReturnsFailure() throws Exception {
        doReturn(Failure.of(EXCEPTION)).when(combineFlatMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException().get()).isEqualTo(EXCEPTION);

        verify(combineFlatMapFunction, times(1)).apply(SUCCESS, OTHER);
    }

    @Test
    public void testCombineFlatMapThrows() throws Exception {
        doThrow(EXCEPTION).when(combineFlatMapFunction).apply(SUCCESS, OTHER);

        Try<String> other = Success.of(OTHER);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException().get()).isEqualTo(EXCEPTION);

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
        assertThat(result.getException().get()).isEqualTo(EXCEPTION);

        verify(predicate, times(1)).test(SUCCESS);

    }

    @Test
    public void testRecoverReturnsSelf() throws Exception {
        Try<String> result = underTest.recover(recoverFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testRecoverSpecificExceptionReturnsSelf() throws Exception {
        Try<String> result = underTest.recover(Exception.class, recoverFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testRecoverWithReturnsSelf() throws Exception {
        Try<String> result = underTest.recoverWith(recoverWithFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testRecoverWithSpecificExceptionReturnsSelf() throws Exception {
        Try<String> result = underTest.recoverWith(Exception.class, recoverWithFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testFailed() {
        Try<Exception> inverted = underTest.failed();

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
    public void testMatchAnySuccess() throws Exception {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .success(mapFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMatchSpecificSuccess() throws Exception {
        doReturn(OTHER).when(mapFunction).apply(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .success(SUCCESS, mapFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, times(1)).apply(SUCCESS);
    }

    @Test
    public void testMatchSpecificSuccessMismatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);
        expectedException.expect(hasProperty("actualValue", equalTo(underTest)));

        underTest.match(m -> m
                .success(OTHER, mapFunction)
        );
    }

    @Test
    public void testMatchSpecificSuccessNullMismatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        underTest.match(m -> m
                .success(null, mapFunction)
        );
    }

    @Test
    public void testMatchSuccessWhen() throws Exception {
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
    public void testMatchSuccessWhenDoesNotMatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        doReturn(false).when(predicate).test(SUCCESS);

        Try<String> result = underTest.match(m -> m
                .successWhen(predicate, mapFunction)
        );
    }

    @Test
    public void testMatchSpecificSuccessMismatchFallthrough() throws Exception {
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
    public void testMatchSpecificSuccessOrdered() throws Exception {
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
    public void testMatchFailureDoesNotMatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        underTest.match(m -> m.failure(recoverWithFunction));
    }

    @Test
    public void testMatchFailureSpecificExceptionDoesNotMatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        underTest.match(m -> m.failure(Exception.class, recoverWithFunction));
    }

    @Test
    public void testMatchOrElseDoesNotMatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        underTest.match(m -> m.orElse(() -> SUCCESS));
    }

    @Test
    public void testMatchOrElseSuccessDoesNotMatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        underTest.match(m -> m.orElseSuccess(SUCCESS));
    }

    @Test
    public void testMatchOrElseFailureDoesNotMatch() throws Exception {
        expectedException.expect(UnmatchedPatternException.class);

        underTest.match(m -> m.orElseFailure(EXCEPTION));
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
