package org.eclipse.oomph.console.core.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;
import org.eclipse.oomph.util.OS;

@SuppressWarnings("restriction")
public class LaunchUtil {

    private LaunchUtil() {
    }

    public static boolean launchProduct(SetupTaskPerformer performer, boolean initialStart) throws IOException {
        OS os = performer.getOS();

        if (os.isCurrentOS()) {

            String relativeProductFolder = performer.getRelativeProductFolder();
            String relativeExecutableFolder = os.getRelativeExecutableFolder();
            String launcherName = "";
            try {
                launcherName = performer.getLauncherName();
            } catch (NullPointerException e) {
                launcherName = "eclipse";
            }
            String executableName = os.getExecutableName(launcherName);
            File eclipseLocation = new File(performer.getInstallationLocation(), relativeProductFolder);
            File executableFolder = new File(eclipseLocation, relativeExecutableFolder);
            String executable = new File(executableFolder, executableName).getAbsolutePath();

            System.out.println("Launching the installed product from " + executable);

            List<String> command = new ArrayList<>();
            command.add(executable);

            File ws = performer.getWorkspaceLocation();
            if (ws != null && initialStart) {
                command.add("-data");
                command.add(ws.toString());
            }

            command.add("-vmargs");
            command.add("-Duser.dir=" + eclipseLocation);

            ProcessBuilder builder = new ProcessBuilder(command);
            Process process = builder.start();
            process.getInputStream().close();
            process.getOutputStream().close();
            process.getErrorStream().close();

            return true;
        }

        performer.log("Launching the installed product is not possible for cross-platform installs. Skipping.");
        return false;
    }
}
