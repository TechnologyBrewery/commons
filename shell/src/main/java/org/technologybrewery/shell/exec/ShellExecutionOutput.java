package org.technologybrewery.shell.exec;

public class ShellExecutionOutput {
    private final String stdout;
    private final String stderr;

    public ShellExecutionOutput(String stdout, String stderr) {
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public String getStdout() { return this.stdout; }
    public String getStderr() { return this.stderr; }
}
