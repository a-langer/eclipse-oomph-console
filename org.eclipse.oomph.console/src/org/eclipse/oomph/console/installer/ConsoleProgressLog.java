package org.eclipse.oomph.console.installer;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.setup.SetupTask;
import org.eclipse.oomph.setup.log.ProgressLog;

@SuppressWarnings("restriction")
public class ConsoleProgressLog implements ProgressLog {

    private boolean verbose = Parameters.VERBOSE;

    public ConsoleProgressLog() {
    }

    public ConsoleProgressLog(boolean verbose) {
        this.verbose = verbose;
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void log(String line) {
        if (verbose && !line.isEmpty()) {
            System.out.println(line);
        }
    }

    @Override
    public void log(String line, Severity severity) {
        if (severity != Severity.ERROR) {
            log(line);
        } else {
            log(line);
        }
    }

    @Override
    public void log(String line, boolean filter) {
        if (!filter || verbose) {
            log(line);
        }
    }

    @Override
    public void log(String line, boolean filter, Severity severity) {
        if (!filter || verbose) {
            log(line, severity);
        }
    }

    @Override
    public void log(IStatus status) {
        // nothing
    }

    @Override
    public void log(Throwable t) {
        log(t.getMessage());
    }

    @Override
    public void task(SetupTask setupTask) {
        String description = setupTask.getDescription();
        if (description != null && !description.isEmpty()) {
            log(description);
        }
    }

    @Override
    public void setTerminating() {
        // nothing
    }

}
