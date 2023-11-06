package org.technologybrewery.commons.credentials;

/**
 * Specific runtime exception used to indicate that this came from Orphedomos.  Other javadoc not overridden as this is
 * not needed unless adding to the original javadoc.
 */
public class CredentialException extends RuntimeException {

    public CredentialException() {
        super();
    }

    public CredentialException(String message, Throwable cause) {
        super(message, cause);
    }

    public CredentialException(String message) {
        super(message);
    }

    public CredentialException(Throwable cause) {
        super(cause);
    }
}
