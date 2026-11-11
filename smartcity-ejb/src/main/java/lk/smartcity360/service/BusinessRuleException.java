package lk.smartcity360.service;

import jakarta.ejb.ApplicationException;

/**
 * Thrown when a request is well-formed but violates a business rule (unknown parent zone,
 * duplicate light code, invalid policy expression, ...).
 *
 * <p>Marked as an application exception so the container delivers it to callers as-is rather
 * than wrapping it in an {@code EJBException}; the transaction is rolled back.
 */
@ApplicationException(rollback = true)
public class BusinessRuleException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public BusinessRuleException(String message) {
        super(message);
    }

    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }
}
