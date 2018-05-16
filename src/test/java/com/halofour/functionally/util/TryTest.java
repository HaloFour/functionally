package com.halofour.functionally.util;

import com.halofour.functionally.util.function.TrySupplier;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@RunWith(MockitoJUnitRunner.class)
public class TryTest {
    private static final String SUCCESS = "SUCCESS";
    private static final String FAILURE = "FAILURE";
    private static final Exception EXCEPTION = new IllegalArgumentException(FAILURE);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TrySupplier<String> supplier;

    @Test
    public void testSuccess() throws Throwable {
        Try<String> t = Try.success(SUCCESS);

        assertThat(t).isInstanceOf(Success.class);
        assertThat(t.get()).isEqualTo(SUCCESS);
    }

    @Test
    public void testFailure() {
        Try<String> t = Try.failure(EXCEPTION);

        assertThat(t).isInstanceOf(Failure.class);
    }

    @Test
    public void testFailureTyped() {
        Try<String> t = Try.failure(String.class, EXCEPTION);

        assertThat(t).isInstanceOf(Failure.class);
    }

    @Test
    public void testFrom() throws Throwable {
        doReturn(SUCCESS).when(supplier).get();

        Try<String> t = Try.from(supplier);

        assertThat(t).isInstanceOf(Success.class);
        verify(supplier, times(1)).get();
    }

    @Test
    public void testFromThrows() throws Throwable {
        doThrow(EXCEPTION).when(supplier).get();

        Try<String> t = Try.from(supplier);

        assertThat(t).isInstanceOf(Failure.class);
        verify(supplier, times(1)).get();
    }
}
