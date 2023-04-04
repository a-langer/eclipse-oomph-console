package org.eclipse.oomph.console.test;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.file.Paths;
import java.security.KeyStore;

import org.eclipse.oomph.console.application.ConsoleLauncherApplication;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.internal.setup.SetupProperties;

@SuppressWarnings("restriction")
public class InstallerTest extends ConsoleLauncherApplication {

    private static final String PROJECT_BASEDIR = System.getProperty("project.basedir",
            Paths.get("").toFile().getAbsolutePath());
    private static final String PROJECT_BUILD_DIR = System.getProperty("project.build.directory",
            PROJECT_BASEDIR + "/target");
    private static final String PROJECT_PARENT_BASEDIR = System.getProperty("project.parent.basedir",
            Paths.get(PROJECT_BASEDIR).toFile().getParent());
    private static final String SETUPS_DIR = PROJECT_PARENT_BASEDIR + "/org.eclipse.oomph.console.product/setups/";

    private static final String REDIRECTION_KEY = "oomph.redirection.setupsDir";
    private static final String REDIRECTION_VALUE = "index:/->" + SETUPS_DIR;

    public static KeyStore createKeyStore(String jksPath, String password) throws Exception {
        File file = new File(jksPath);
        KeyStore keyStore = KeyStore.getInstance("JKS");
        if (!file.exists()) {
            keyStore.load(null, null);
            keyStore.store(new FileOutputStream(file), password.toCharArray());
        }
        return keyStore;
    }

    protected static void setFinalParameter(String fieldName, Object value) throws Exception {
        Field field = Parameters.class.getDeclaredField(fieldName);
        field.setAccessible(true);

        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);

        Object fieldBase = unsafe.staticFieldBase(field);
        long fieldOffset = unsafe.staticFieldOffset(field);
        unsafe.putObject(fieldBase, fieldOffset, value);
    }

    private void setRedirectionDir(String lacation) throws Exception {
        System.setProperty(REDIRECTION_KEY, lacation);
    }

    private void setLocation(String lacation) throws Exception {
        System.setProperty("user.home", lacation);
        System.setProperty("user.dir", lacation);
        System.setProperty(Parameters.INSTALLATION_LOCATION_ID, lacation);
        setFinalParameter("INSTALLATION_LOCATION", lacation);
        System.setProperty(Parameters.WORKSPACE_LOCATION_ID, lacation);
        setFinalParameter("WORKSPACE_LOCATION", lacation);
    }

    private void setConfiguration(String lacation) throws Exception {
        System.setProperty(Parameters.OOMPH_CONFIGURATION_ID, lacation);
        setFinalParameter("CONFIGURATION", lacation);
        System.clearProperty(Parameters.OOMPH_PRODUCT_ID);
        setFinalParameter("PRODUCT", "");
        System.clearProperty(Parameters.OOMPH_PROJECT_ID);
        setFinalParameter("PROJECT", "");
        System.clearProperty(REDIRECTION_KEY);
    }

    private void setProduct(String product) throws Exception {
        System.clearProperty(Parameters.OOMPH_CONFIGURATION_ID);
        setFinalParameter("CONFIGURATION", null);
        System.setProperty(Parameters.OOMPH_PRODUCT_ID, product);
        setFinalParameter("PRODUCT", product);
    }

    private void setProject(String project) throws Exception {
        setFinalParameter("PROJECT", project);
        System.setProperty(Parameters.OOMPH_PROJECT_ID, project);
    }

    @Before
    public void setUp() throws Exception {
        String jksPath = PROJECT_BUILD_DIR + "/empty.jks";
        String jksPass = "changeit";
        createKeyStore(jksPath, jksPass);
        System.setProperty("javax.net.ssl.trustStore", jksPath);
        System.setProperty("javax.net.ssl.trustStorePassword", jksPass);
        System.setProperty(Parameters.INSTALLER_SSL_INSECURE_ID, "true");

        System.setProperty(Parameters.INSTALLER_VERBOSE_ID,
                System.getProperty(Parameters.INSTALLER_VERBOSE_ID, "false"));
        System.setProperty(SetupProperties.PROP_SETUP_ECF_TRACE,
                System.getProperty(SetupProperties.PROP_SETUP_ECF_TRACE, "false"));
        System.setProperty(SetupProperties.PROP_SETUP_USER_HOME_REDIRECT, "true");

        System.out.println();
    }

    @Test
    public void installEclipseApp() throws Exception {
        setLocation(PROJECT_BUILD_DIR + "/ide");
        setRedirectionDir(REDIRECTION_VALUE);
        setProduct("epp.package.java_custom");
        setProject("bash.editor,oomph.console");
        Assert.assertNull(start(null));
    }

    @Test
    public void installEclipseConfigurationFromFile() throws Exception {
        setLocation(PROJECT_BUILD_DIR + "/ide_conf_file");
        setConfiguration(SETUPS_DIR + "/Configuration.setup");
        // Projects must be from the same setups model as the Product in Configuration
        setRedirectionDir(REDIRECTION_VALUE);
        setProject("bash.editor,oomph.console");
        Assert.assertNull(start(null));
    }

    @Test
    public void installEclipseConfigurationFromURL() throws Exception {
        setLocation(PROJECT_BUILD_DIR + "/ide_conf_http");
        // Also these URLs:
        // https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/configurations/OomphConfiguration.setup
        // https://git.eclipse.org/c/oomph/org.eclipse.oomph.git/plain/setups/interim/SimultaneousReleaseTrainConfiguration.setup
        String url = "https://raw.githubusercontent.com/eclipse/birt/master/build/org.eclipse.birt.releng/BIRTConfiguration.setup";
        setConfiguration(url);
        Assert.assertNull(start(null));
    }

}
