package org.eclipse.oomph.console.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Paths;
import java.security.KeyStore;

import org.eclipse.oomph.console.application.ConsoleLauncherApplication;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.internal.setup.SetupProperties;

@SuppressWarnings("restriction")
public class InstallerTest extends ConsoleLauncherApplication {

    public static KeyStore createKeyStore(String jksPath, String password) throws Exception {
        File file = new File(jksPath);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        if (!file.exists()) {
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file), password.toCharArray());
        }
        return keyStore;
    }

    @Before
    public void setup() throws Exception {
        String projectBasedir = System.getProperty("project.basedir", Paths.get("").toFile().getAbsolutePath());
        String installLacation = projectBasedir + "/target/ide";

        String jksPath = projectBasedir + "/target/empty.jks";
        String jksPass = "changeit";
        createKeyStore(jksPath, jksPass);
        System.setProperty("javax.net.ssl.trustStore", jksPath);
        System.setProperty("javax.net.ssl.trustStorePassword", jksPass);
        System.setProperty(Parameters.INSTALLER_SSL_INSECURE_ID, "true");

        System.setProperty(Parameters.INSTALLER_VERBOSE_ID,
                System.getProperty(Parameters.INSTALLER_VERBOSE_ID, "false"));
        System.setProperty(SetupProperties.PROP_SETUP_ECF_TRACE,
                System.getProperty(SetupProperties.PROP_SETUP_ECF_TRACE, "false"));

        System.setProperty("user.home", installLacation);
        System.setProperty(SetupProperties.PROP_SETUP_USER_HOME_REDIRECT, "true");

        System.setProperty(Parameters.INSTALLATION_LOCATION_ID, installLacation);
        System.setProperty(Parameters.OOMPH_PRODUCT_ID, "epp.package.java_custom");

        System.setProperty(Parameters.WORKSPACE_LOCATION_ID, installLacation);
        System.setProperty(Parameters.OOMPH_PROJECT_ID, "bash.editor,oomph.console");
    }

    @Test
    public void installEclipseApp() throws Exception {
        Assert.assertNull(start(null));
    }

}
