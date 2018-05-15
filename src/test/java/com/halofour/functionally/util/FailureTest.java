package com.halofour.functionally.util;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class FailureTest {
    private static final String SUCCESS = "SUCCESS";
    private static final String OTHER = "OTHER";
    private static final String FAILURE = "FAILURE";
    private static final String OTHER_FAILURE = "OTHER_FAILURE";
    private static final Exception EXCEPTION = new IllegalArgumentException(FAILURE);
    private static final Exception OTHER_EXCEPTION = new NullPointerException(OTHER_FAILURE);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private Try<String> underTest;

    @Mock
    private TryFunction<String, String> mapFunction;

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
    private TryFunction<Exception, String> recoverFunction2;

    @Mock
    private TryFunction<Exception, Try<String>> recoverWithFunction;

    @Mock
    private Consumer<String> ifSuccessConsumer;

    @Mock
    private Consumer<Exception> ifFailureConsumer;

    @Mock
    private TrySupplier<String> supplier;

    @Mock
    private Predicate<Exception> failurePredicate;

    @Before
    public void setUp() throws Exception {
        underTest = Try.failure(EXCEPTION);
    }

    @Test
    public void testIsSuccess() {
        assertThat(underTest.isSuccess()).isFalse();
    }

    @Test
    public void testIsFailure() {
        assertThat(underTest.isFailure()).isTrue();
    }

    @Test
    public void testIsSpecificFailure() {
        assertThat(underTest.isFailure(IllegalArgumentException.class)).isTrue();
    }

    @Test
    public void testIsSpecificFailureBaseClass() {
        assertThat(underTest.isFailure(Exception.class)).isTrue();
    }

    @Test
    public void testIsSpecificFailureMismatch() {
        assertThat(underTest.isFailure(NullPointerException.class)).isFalse();
    }

    @Test
    public void testGet() throws Exception {
        expectedException.expect(is(EXCEPTION));

        underTest.get();
    }

    @Test
    public void testGetException() {
        Optional<Exception> result = underTest.getException();
        assertThat(result.isPresent()).isTrue();
        assertThat(result.get()).isEqualTo(EXCEPTION);
    }

    @Test
    public void testGetOrElse() {
        assertThat(underTest.getOrElse(OTHER)).isEqualTo(OTHER);
    }

    @Test
    public void testOrElse() {
        Try<String> expected = Try.success(OTHER);
        assertThat(underTest.orElse(expected)).isEqualTo(expected);
    }

    @Test
    public void testToOptional() {
        Optional<String> result = underTest.toOptional();

        assertThat(result.isPresent()).isFalse();
    }

    @Test
    public void testMap() throws Exception {
        Try<String> result = underTest.map(mapFunction);

        assertThat(result).isEqualTo(underTest);

        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testFlatMap() throws Exception {
        Try<String> result = underTest.flatMap(flatMapFunction);

        assertThat(result).isEqualTo(underTest);

        verify(flatMapFunction, never()).apply(any());
    }

    @Test
    public void testCombineMap() throws Exception {
        Try<String> other = Try.success(SUCCESS);
        Try<String> result = underTest.combineMap(other, combineMapFunction);

        assertThat(result).isEqualTo(underTest);

        verify(combineMapFunction, never()).apply(any(), any());
    }

    @Test
    public void testCombineFlatMap() throws Exception {
        Try<String> other = Try.success(SUCCESS);
        Try<String> result = underTest.combineFlatMap(other, combineFlatMapFunction);

        assertThat(result).isEqualTo(underTest);

        verify(combineFlatMapFunction, never()).apply(any(), any());
    }

    @Test
    public void testFilter() {
        Try<String> result = underTest.filter(predicate);

        assertThat(result).isEqualTo(underTest);

        verify(predicate, never()).test(any());
    }

    @Test
    public void testRecover() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.recover(recoverFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverThrows() throws Exception {
        doThrow(OTHER_EXCEPTION).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.recover(recoverFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(OTHER_EXCEPTION));

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverSpecificException() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.recover(IllegalArgumentException.class, recoverFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverSpecificExceptionThrows() throws Exception {
        doThrow(OTHER_EXCEPTION).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.recover(IllegalArgumentException.class, recoverFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(OTHER_EXCEPTION));

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverSpecificExceptionBaseClass() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.recover(Exception.class, recoverFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverSpecificExceptionMismatch() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.recover(NullPointerException.class, recoverFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testRecoverWithSuccess() throws Exception {
        Try<String> expected = Try.success(SUCCESS);
        doReturn(expected).when(recoverWithFunction).apply(EXCEPTION);

        Try<String> result = underTest.recoverWith(recoverWithFunction);

        assertThat(result).isEqualTo(expected);

        verify(recoverWithFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverWithFailure() throws Exception {
        Try<String> expected = Try.failure(OTHER_EXCEPTION);
        doReturn(expected).when(recoverWithFunction).apply(EXCEPTION);

        Try<String> result = underTest.recoverWith(recoverWithFunction);

        assertThat(result).isEqualTo(expected);

        verify(recoverWithFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverWithThrows() throws Exception {
        doThrow(OTHER_EXCEPTION).when(recoverWithFunction).apply(EXCEPTION);

        Try<String> result = underTest.recoverWith(recoverWithFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(OTHER_EXCEPTION));

        verify(recoverWithFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverWithSpecificException() throws Exception {
        Try<String> expected = Try.success(SUCCESS);
        doReturn(expected).when(recoverWithFunction).apply(EXCEPTION);

        Try<String> result = underTest.recoverWith(IllegalArgumentException.class, recoverWithFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverWithFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverWithSpecificExceptionThrows() throws Exception {
        doThrow(OTHER_EXCEPTION).when(recoverWithFunction).apply(EXCEPTION);

        Try<String> result = underTest.recoverWith(IllegalArgumentException.class, recoverWithFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(OTHER_EXCEPTION));

        verify(recoverWithFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverWithSpecificExceptionBaseClass() throws Exception {
        Try<String> expected = Try.success(SUCCESS);
        doReturn(expected).when(recoverWithFunction).apply(EXCEPTION);

        Try<String> result = underTest.recoverWith(Exception.class, recoverWithFunction);

        assertThat(result).isEqualTo(expected);

        verify(recoverWithFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testRecoverWithSpecificExceptionMismatch() throws Exception {
        Try<String> result = underTest.recoverWith(NullPointerException.class, recoverWithFunction);

        assertThat(result).isEqualTo(underTest);

        verify(recoverWithFunction, never()).apply(any());
    }

    @Test
    public void testFold() throws Exception {
        doReturn(OTHER).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.fold(recoverFunction, mapFunction);

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(OTHER);

        verify(mapFunction, never()).apply(any());
        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testFoldThrows() throws Exception {
        doThrow(OTHER_EXCEPTION).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.fold(recoverFunction, mapFunction);

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(OTHER_EXCEPTION));

        verify(mapFunction, never()).apply(any());
        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testFailed() throws Exception {
        Try<Exception> inverted = underTest.failed();

        assertThat(inverted).isInstanceOf(Success.class);
        assertThat(inverted.get()).isEqualTo(EXCEPTION);
    }

    @Test
    public void testIfSuccess() {
        underTest.ifSuccess(ifSuccessConsumer);

        verify(ifSuccessConsumer, never()).accept(SUCCESS);
    }

    @Test
    public void testIfFailure() {
        underTest.ifFailure(ifFailureConsumer);

        verify(ifFailureConsumer, times(1)).accept(any());
    }

    @Test
    public void testIfSpecificFailure() {
        underTest.ifFailure(IllegalArgumentException.class, ifFailureConsumer);

        verify(ifFailureConsumer, times(1)).accept(any());
    }

    @Test
    public void testIfSpecificFailureBaseClass() {
        underTest.ifFailure(Exception.class, ifFailureConsumer);

        verify(ifFailureConsumer, times(1)).accept(any());
    }

    @Test
    public void testIfSpecificFailureMismatch() {
        underTest.ifFailure(NullPointerException.class, ifFailureConsumer);

        verify(ifFailureConsumer, never()).accept(any());
    }

    @Test
    public void testMatchAnyFailure() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.match(m -> m
                .failure(recoverFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testMatchSpecificFailure() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.match(m -> m
                .failure(IllegalArgumentException.class, recoverFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testMatchSpecificFailureBaseClass() throws Exception {
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.match(m -> m
                .failure(Exception.class, recoverFunction)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testMatchSpecificFailureMismatch() throws Exception {
        underTest.match(m -> m
                .failure(NullPointerException.class, recoverFunction)
                .orElseSuccess(SUCCESS)
        );

        verify(recoverFunction, never()).apply(any());
    }

    @Test
    public void testMatchSpecificFailureMismatchFallthrough() throws Exception {
        doReturn(SUCCESS).when(recoverFunction2).apply(EXCEPTION);

        Try<String> result = underTest.match(m -> m
                .failure(NullPointerException.class, recoverFunction)
                .failure(recoverFunction2)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(recoverFunction, never()).apply(any());
        verify(recoverFunction2, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testMatchExceptionWhen() throws Exception {
        doReturn(true).when(failurePredicate).test(EXCEPTION);
        doReturn(SUCCESS).when(recoverFunction).apply(EXCEPTION);

        Try<String> result = underTest.match(m -> m.failureWhen(failurePredicate, recoverFunction));

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(failurePredicate, times(1)).test(EXCEPTION);
        verify(recoverFunction, times(1)).apply(EXCEPTION);
    }

    @Test
    public void testMatchExceptionWhenDoesNotMatch() throws Exception {
        doReturn(false).when(failurePredicate).test(EXCEPTION);

        underTest.match(m -> m
                .failureWhen(failurePredicate, recoverFunction)
                .orElseSuccess(SUCCESS)
        );

        verify(failurePredicate, times(1)).test(EXCEPTION);
        verify(recoverFunction, never()).apply(EXCEPTION);
    }

    @Test
    public void testMatchOrElse() throws Exception {
        doReturn(SUCCESS).when(supplier).get();

        Try<String> result = underTest.match(m -> m
                .orElse(supplier)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);

        verify(supplier, times(1)).get();
    }

    @Test
    public void testMatchOrElseSuccess() throws Exception {
        Try<String> result = underTest.match(m -> m
                .orElseSuccess(SUCCESS)
        );

        assertThat(result).isInstanceOf(Success.class);
        assertThat(result.get()).isEqualTo(SUCCESS);
    }

    @Test
    public void testMatchOrElseFailure() throws Exception {
        Try<String> result = underTest.match(m -> m
                .orElseFailure(OTHER_EXCEPTION)
        );

        assertThat(result).isInstanceOf(Failure.class);
        assertThat(result.getException()).isEqualTo(Optional.of(OTHER_EXCEPTION));
    }

    @Test
    public void testMatchSuccess() throws Exception{
        underTest.match(m -> m
                .success(mapFunction)
                .orElseSuccess(SUCCESS)
        );

        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testMatchSuccessValue() throws Exception {
        underTest.match(m -> m
                .success(SUCCESS, mapFunction)
                .orElseSuccess(SUCCESS)
        );

        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testMatchSuccessWhen() throws Exception {
        doReturn(true).when(predicate).test(SUCCESS);

        underTest.match(m -> m
                .successWhen(predicate, mapFunction)
                .orElseSuccess(SUCCESS)
        );

        verify(predicate, never()).test(any());
        verify(mapFunction, never()).apply(any());
    }

    @Test
    public void testToString() {
        assertThat(underTest.toString()).startsWith("Failure[");
    }

    @Test
    public void testHashCode() {
        assertThat(underTest.hashCode()).isEqualTo(EXCEPTION.hashCode());
    }

    @Test
    public void testEqualsSameException() {
        Try<String> other = Try.failure(EXCEPTION);

        assertThat(underTest.equals(other)).isTrue();
    }

    @Test
    public void testEqualsDifferentException() {
        Try<String> other = Try.failure(OTHER_EXCEPTION);

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
