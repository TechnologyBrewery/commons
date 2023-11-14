package org.technologybrewery.commons.json;

/**
 * Specific exceptions relating to common json processing and allows json-specific processing exceptions to be caught
 * and dealt with in a more granular manner, if appropriate.
 */
public class JsonException extends RuntimeException {

    /**
     * {@inheritDoc}
     */
    public JsonException() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    public JsonException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     */
    public JsonException(String message) {
        super(message);
    }

    /**
     * {@inheritDoc}
     */
    public JsonException(Throwable cause) {
        super(cause);
    }

}
