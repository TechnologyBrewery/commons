package org.technologybrewery.shell.exec;

/**
 * Specific runtime exception used to indicate that this came from the shell execution lib.  Other javadoc not
 * overridden as this is not needed unless adding to the original javadoc.
 */
public class ShellExecutionException extends RuntimeException {

    public ShellExecutionException() {
        super();
    }

    public ShellExecutionException(String message, Throwable cause) {
        super(message, cause);
    }

    public ShellExecutionException(String message) {
        super(message);
    }

    public ShellExecutionException(Throwable cause) {
        super(cause);
    }
}
