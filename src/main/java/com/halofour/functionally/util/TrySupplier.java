package com.halofour.functionally.util;

/**
 * Represents a supplier of results that can fail with an exception.
 * @param <T> the type of results supplied by this supplier
 */
@FunctionalInterface
public interface TrySupplier<T> {
    /**
     * Gets a result.
     * @return the result
     * @throws Exception an exception
     */
    T get() throws Exception;
}
