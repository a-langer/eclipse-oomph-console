package org.eclipse.oomph.console.installer;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.oomph.console.configuration.NotFoundException;
import org.eclipse.oomph.console.configuration.ProductVersionSelector;
import org.eclipse.oomph.console.core.p2.AcceptCacheUsageConfirmer;
import org.eclipse.oomph.console.core.p2.P2ServiceUI;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.console.core.util.InstallationInitializer;
import org.eclipse.oomph.console.core.util.LaunchUtil;
import org.eclipse.oomph.console.core.util.ScopeAdjuster;
import org.eclipse.oomph.internal.setup.SetupPrompter;
import org.eclipse.oomph.p2.internal.core.CacheUsageConfirmer;
import org.eclipse.oomph.setup.CertificatePolicy;
import org.eclipse.oomph.setup.Installation;
import org.eclipse.oomph.setup.Product;
import org.eclipse.oomph.setup.ProductVersion;
import org.eclipse.oomph.setup.SetupTaskContext;
import org.eclipse.oomph.setup.Stream;
import org.eclipse.oomph.setup.Trigger;
import org.eclipse.oomph.setup.UnsignedPolicy;
import org.eclipse.oomph.setup.User;
import org.eclipse.oomph.setup.VariableTask;
import org.eclipse.oomph.setup.Workspace;
import org.eclipse.oomph.setup.internal.core.SetupContext;
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;
import org.eclipse.oomph.util.Confirmer;
import org.eclipse.oomph.util.OS;
import org.eclipse.oomph.util.UserCallback;

@SuppressWarnings("restriction")
public class ConsoleInstaller {

    private SetupContext context;
    private SetupTaskPerformer performer;
    private ResourceSet resourceSet;

    private final String product;
    private final String version;
    private final String project;
    private final String redirection;
    private final String location;
    private final String folder;
    private final String workspace;
    private final boolean launch;
    private final boolean verbose;

    public ConsoleInstaller(String product) {
        this.product = product.contains(":") ? product.split(":")[0] : product;
        this.version = product.contains(":") ? product.split(":")[1]
                : System.getProperty(Parameters.OOMPH_VERSION_ID + "." + product, Parameters.VERSION);
        this.project = System.getProperty(Parameters.OOMPH_PROJECT_ID + "." + product, Parameters.PROJECT)
                .replaceAll("\\s+", "");
        this.redirection = System.getProperty(Parameters.REDIRECTION_ID + "." + product, Parameters.REDIRECTION);
        this.location = System.getProperty(Parameters.INSTALLATION_LOCATION_ID + "." + product,
                Parameters.INSTALLATION_LOCATION);
        this.folder = System.getProperty(Parameters.INSTALLATION_PRODUCT_FOLDER_ID + "." + product,
                Parameters.INSTALLATION_PRODUCT_FOLDER);
        this.workspace = System.getProperty(Parameters.WORKSPACE_LOCATION_ID + "." + project,
                Parameters.WORKSPACE_LOCATION);
        this.launch = System.getProperty(Parameters.LAUNCH_AUTOMATICALLY_ID + "." + product) == null
                ? Parameters.LAUNCH_AUTOMATICALLY
                : Boolean.getBoolean(Parameters.LAUNCH_AUTOMATICALLY_ID + "." + product);
        this.verbose = System.getProperty(Parameters.INSTALLER_VERBOSE_ID + "." + product) == null
                ? Parameters.VERBOSE
                : Boolean.getBoolean(Parameters.INSTALLER_VERBOSE_ID + "." + product);
    }

    public void run() throws Exception {
        init(this.product, this.version, this.project);
        URIConverter uriConverter = resourceSet.getURIConverter();

        String overrideRedirection = System.getProperty(this.redirection);
        if (overrideRedirection != null) {
            String[] mapping = overrideRedirection.split("->", 2);
            URI sourceURI = URI.createURI(mapping[0]);
            URI targetURI = URI.createURI(mapping[1].replace("\\", "/"));
            uriConverter.getURIMap().put(sourceURI, targetURI);
        }

        SetupPrompter prompter = new SetupPrompter() {

            @Override
            public UserCallback getUserCallback() {
                return null;
            }

            @Override
            public String getValue(VariableTask variable) {
                return System.getProperty(variable.getName());
            }

            @Override
            public OS getOS() {
                return OS.INSTANCE;
            }

            @Override
            public boolean promptVariables(List<? extends SetupTaskContext> performers) {
                for (SetupTaskContext context : performers) {
                    SetupTaskPerformer promptedPerformer = (SetupTaskPerformer) context;
                    List<VariableTask> unresolvedVariables = promptedPerformer.getUnresolvedVariables();
                    for (int i = 0; i <= unresolvedVariables.size() - 1; ++i) {
                        VariableTask variable = unresolvedVariables.get(i);
                        String value = System.getProperty(variable.getName(), variable.getValue());
                        if (value == null) {
                            value = variable.getDefaultValue();
                        }
                        if (value == null && !variable.getChoices().isEmpty()) {
                            value = variable.getChoices().get(0).getValue();
                        }
                        if (value != null) {
                            variable.setValue(value);
                        }
                    }
                }
                return true;
            }

            @Override
            public String getVMPath() {
                return null;
            }
        };

        ScopeAdjuster adjuster = new ScopeAdjuster();
        adjuster.setWorkspaceLacation(context.getUser(), this.workspace);
        adjuster.setInstallationLocation(context.getUser(), this.location);
        adjuster.setProductFolderName(context.getUser(), this.folder);
        adjuster.setProductFolderName(context.getInstallation().getProductVersion(), this.folder);

        Trigger trigger = Trigger.getByName(Parameters.TRIGGER);
        performer = SetupTaskPerformer.create(uriConverter, prompter, trigger, context, false);

        performer.recordVariables(context.getInstallation(), context.getWorkspace(), context.getUser());
        if (Parameters.CLEAN_UNRESOLVED)
            performer.getUnresolvedVariables().clear();
        performer.put(UIServices.class, P2ServiceUI.SERVICE_UI);
        performer.put(CacheUsageConfirmer.class, new AcceptCacheUsageConfirmer());
        performer.put(ILicense.class, Confirmer.ACCEPT);
        performer.put(Certificate.class, Confirmer.ACCEPT);
        performer.setProgress(new ConsoleProgressLog(this.verbose));
        performer.setOffline(Parameters.SETUP_OFFLINE);
        performer.setMirrors(Parameters.SETUP_MIRRORS);
        performer.setSkipConfirmation(true);

        File installationLocation = performer.getInstallationLocation();
        if (this.launch && installationLocation.exists()) {
            try {
                LaunchUtil.launchProduct(performer, false);
                return;
            } catch (IOException | NullPointerException e) {
                System.out.println("Old installation did probably not finish correctly...");
                System.out.println("Reinstalling");
            }
        }
        saveResources();

        String headerText = "Installation (" + this.product + ":" + this.version
                + (this.project.length() > 0 ? " + " + this.project.replace(",", " + ") : "") + ")";
        performer.perform(new ConsoleProgressMonitor(this.verbose, headerText));
        if (this.launch)
            LaunchUtil.launchProduct(performer, true);
    }

    private void saveResources() {
        Installation installation = performer.getInstallation();
        Resource installationResource = installation.eResource();
        Objects.requireNonNull(installationResource, "installationResource is null");
        installationResource.setURI(URI.createFileURI(
                new File(performer.getProductConfigurationLocation(), "org.eclipse.oomph.setup/installation.setup")
                        .toString()));

        Workspace workspace = performer.getWorkspace();
        Resource workspaceResource = null;
        if (workspace != null) {
            workspaceResource = workspace.eResource();
            workspaceResource.setURI(URI.createFileURI(new File(performer.getWorkspaceLocation(),
                    ".metadata/.plugins/org.eclipse.oomph.setup/workspace.setup").toString()));
        }
        performer.savePasswords();
        try {
            installationResource.save(Collections.emptyMap());
            if (workspaceResource != null) {
                workspaceResource.save(Collections.emptyMap());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void init(String productId, String versionId, String projectId) throws NotFoundException {
        InstallationInitializer installationHelper = new InstallationInitializer();
        resourceSet = installationHelper.getResourceSet();
        initInstallation(productId, versionId, projectId);
    }

    private void initInstallation(String productId, String versionId, String projectId) throws NotFoundException {
        ProductVersionSelector selector = new ProductVersionSelector(resourceSet);

        Product product = selector.selectProduct(productId);
        ProductVersion version = selector.selectProductVersion(product, versionId);
        List<Stream> streams = new ArrayList<>();
        if (!projectId.isEmpty()) {
            streams = selector.selectProjectStreams(Arrays.asList(projectId.split(",")));
        }
        context = SetupContext.create(resourceSet, version);
        Installation installation = context.getInstallation();
        installation.setProductVersion(version);

        User user = context.getUser();
        user.setUnsignedPolicy(UnsignedPolicy.ACCEPT);
        user.setCertificatePolicy(CertificatePolicy.ACCEPT);

        context = SetupContext.create(installation, streams, user);
    }

}
