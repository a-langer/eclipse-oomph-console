package org.eclipse.oomph.console.installer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.oomph.console.core.parameters.Parameters;

public class ConsoleProgressMonitor implements IProgressMonitor {

    private double totalWork = 0;
    private double worked = 0;
    private boolean headerPrinted = false;
    private String headerText = null;
    private boolean verbose = Parameters.VERBOSE;
    private long oldTime = 0;

    public ConsoleProgressMonitor() {
    }

    public ConsoleProgressMonitor(boolean verbose, String headerText) {
        this.verbose = verbose;
        this.headerText = headerText;
    }

    @Override
    public void beginTask(String name, int totalWork) {
        this.totalWork = totalWork;
    }

    @Override
    public void done() {
        internalWorked(totalWork - worked);
        printLine("");
    }

    @Override
    public void internalWorked(double work) {
        int length = 70;
        long newTime = System.currentTimeMillis();
        if (!headerPrinted) {
            printHeader();
            headerPrinted = true;
        }
        if (!verbose) {
            worked += work;
            int percentageWorked = (int) (worked / totalWork * 100);
            double workDecimal = worked / totalWork;
            if ((newTime - oldTime > 100) || (worked >= 100)) {
                returnToStart();
                print("[");
                for (int i = 0; i <= workDecimal * length; i++) {
                    print("#");
                }
                for (int i = (int) (workDecimal * length); i < length; i++) {
                    print(".");
                }
                print("]");
                print(percentageWorked + "%");
                oldTime = System.currentTimeMillis();
            }
        }
    }

    private void printHeader() {
        if (this.headerText != null) {
            System.out.println(headerText);
        }
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public void setCanceled(boolean value) {
        printLine("Cancelled!");
    }

    @Override
    public void setTaskName(String name) {
        if (verbose) {
            printLine(name);
        }
    }

    @Override
    public void subTask(String name) {
        if (verbose) {
            printLine(name);
        }
    }

    @Override
    public void worked(int work) {
        // nothing
    }

    private void returnToStart() {
        print("\r");
    }

    private void print(String input) {
        System.out.print(input);
    }

    private void printLine(String line) {
        System.out.println(line);
    }

}
