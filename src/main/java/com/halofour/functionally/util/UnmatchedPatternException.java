package com.halofour.functionally.util;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Thrown to indicate that the {@link Try#match(Consumer)} did not match the result of the computation.
 */
public final class UnmatchedPatternException extends RuntimeException implements Serializable {
    private static final long serialVersionUID = 817906161283094653L;

    private final Object actual;

    /**
     * Constructs the {@link UnmatchedPatternException} with the actual computation result
     * @param actual the actual result of the computation
     */
    public UnmatchedPatternException(Object actual) {
        super(String.format("Failed to match value: %s", actual));
        this.actual = actual;
    }

    /**
     * Gets the actual result of the computation
     * @return the actual result
     */
    public Object getActualValue() {
        return actual;
    }
}
