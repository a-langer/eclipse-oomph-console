package org.eclipse.oomph.console.application;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.eclipse.oomph.console.core.application.AbstractLauncherApplication;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.console.installer.ConsoleInstaller;

public class ConsoleLauncherApplication extends AbstractLauncherApplication {
    @Override
    public void run() throws Exception {
        long startTime = System.currentTimeMillis();
        try {
            for (String product : Arrays.asList(Parameters.PRODUCT.split(","))) {
                try {
                    ConsoleInstaller installer = new ConsoleInstaller(product);
                    installer.run();
                } catch (Exception e) {
                    System.err.println("ABORTING: " + e.getMessage()
                            + (e.getCause() != null ? " " + e.getCause().getMessage() : ""));
                    throw e;
                }
            }
        } finally {
            long endTime = System.currentTimeMillis();
            long millis = endTime - startTime;
            String startDuration = String.format("%02d min, %02d sec", TimeUnit.MILLISECONDS.toMinutes(millis),
                    TimeUnit.MILLISECONDS.toSeconds(millis)
                            - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));
            System.out.println("Duration: " + startDuration);
        }
    }
}
