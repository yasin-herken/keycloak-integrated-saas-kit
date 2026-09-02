package com.archcore.app.exception;

/**
 * Thrown when a billing operation is attempted but billing is disabled
 * in the application configuration ({@code features.billing.enabled=false}).
 *
 * <p>The {@link com.archcore.app.exception.GlobalExceptionHandler} maps this
 * exception to HTTP 404 Not Found, effectively hiding billing endpoints
 * when the feature is turned off.</p>
 */
public class BillingDisabledException extends RuntimeException {

    public BillingDisabledException(String message) {
        super(message);
    }
}
