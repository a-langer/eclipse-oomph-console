package org.eclipse.oomph.console.core.parameters;

import org.eclipse.oomph.internal.setup.SetupProperties;

@SuppressWarnings("restriction")
public class Parameters {

    private Parameters() {
    }

    public static final String OOMPH_PRODUCT_ID = "oomph.product.id";
    public static final String OOMPH_VERSION_ID = "oomph.product.version";
    public static final String OOMPH_PROJECT_ID = "oomph.project.id";
    public static final String OOMPH_STREAM_ID = "oomph.project.stream";
    public static final String REDIRECTION_ID = "oomph.main.redirection";
    public static final String TRIGGER_ID = "oomph.performer.trigger";
    public static final String CLEAN_UNRESOLVED_ID = "oomph.performer.clean.unresolved";
    public static final String LAUNCH_AUTOMATICALLY_ID = SetupProperties.PROP_SETUP_LAUNCH_AUTOMATICALLY;
    public static final String INSTALLER_VERBOSE_ID = "oomph.installer.verbose";
    public static final String INSTALLATION_LOCATION_ID = "oomph.installation.location";
    public static final String INSTALLATION_PRODUCT_FOLDER_ID = "oomph.installation.id";
    public static final String WORKSPACE_LOCATION_ID = "oomph.workspace.location";

    public static final String PRODUCT = System.getProperty(OOMPH_PRODUCT_ID, "Not selected");
    public static final String VERSION = System.getProperty(OOMPH_VERSION_ID, "latest");
    public static final String PROJECT = System.getProperty(OOMPH_PROJECT_ID, "");
    public static final String STREAM = System.getProperty(OOMPH_STREAM_ID, "master");
    public static final String REDIRECTION = System.getProperty(REDIRECTION_ID, "oomph.redirection.setups");
    public static final String TRIGGER = System.getProperty(TRIGGER_ID, "BOOTSTRAP").toUpperCase();
    public static final boolean CLEAN_UNRESOLVED = Boolean
            .parseBoolean(System.getProperty(CLEAN_UNRESOLVED_ID, "true"));
    public static final boolean LAUNCH_AUTOMATICALLY = Boolean.getBoolean(LAUNCH_AUTOMATICALLY_ID);
    public static final boolean VERBOSE = Boolean.getBoolean(INSTALLER_VERBOSE_ID);
    public static final String INSTALLATION_LOCATION = System.getProperty(INSTALLATION_LOCATION_ID);
    public static final String INSTALLATION_PRODUCT_FOLDER = System.getProperty(INSTALLATION_PRODUCT_FOLDER_ID);
    public static final String WORKSPACE_LOCATION = System.getProperty(WORKSPACE_LOCATION_ID,
            INSTALLATION_LOCATION);
    public static final boolean SETUP_OFFLINE = Boolean
            .parseBoolean(System.getProperty(SetupProperties.PROP_SETUP_OFFLINE, "true"));
    public static final boolean SETUP_MIRRORS = Boolean
            .parseBoolean(System.getProperty(SetupProperties.PROP_SETUP_MIRRORS, "false"));
}
