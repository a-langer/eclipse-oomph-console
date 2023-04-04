package org.eclipse.oomph.console.installer;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.equinox.p2.metadata.ILicense;
import org.eclipse.oomph.console.Activator;
import org.eclipse.oomph.console.configuration.NotFoundException;
import org.eclipse.oomph.console.configuration.ProductVersionSelector;
import org.eclipse.oomph.console.core.p2.AcceptCacheUsageConfirmer;
import org.eclipse.oomph.console.core.p2.P2ServiceUI;
import org.eclipse.oomph.console.core.parameters.Parameters;
import org.eclipse.oomph.console.core.util.InstallationInitializer;
import org.eclipse.oomph.console.core.util.LaunchUtil;
import org.eclipse.oomph.console.core.util.NonStrictSSL;
import org.eclipse.oomph.console.core.util.ScopeAdjuster;
import org.eclipse.oomph.internal.setup.SetupPrompter;
import org.eclipse.oomph.p2.internal.core.CacheUsageConfirmer;
import org.eclipse.oomph.setup.CertificatePolicy;
import org.eclipse.oomph.setup.CompoundTask;
import org.eclipse.oomph.setup.Configuration;
import org.eclipse.oomph.setup.Installation;
import org.eclipse.oomph.setup.Product;
import org.eclipse.oomph.setup.ProductVersion;
import org.eclipse.oomph.setup.SetupTask;
import org.eclipse.oomph.setup.SetupTaskContext;
import org.eclipse.oomph.setup.Stream;
import org.eclipse.oomph.setup.Trigger;
import org.eclipse.oomph.setup.UnsignedPolicy;
import org.eclipse.oomph.setup.User;
import org.eclipse.oomph.setup.VariableTask;
import org.eclipse.oomph.setup.Workspace;
import org.eclipse.oomph.setup.internal.core.SetupContext;
import org.eclipse.oomph.setup.internal.core.SetupTaskPerformer;
import org.eclipse.oomph.setup.p2.P2Task;
import org.eclipse.oomph.util.Confirmer;
import org.eclipse.oomph.util.OS;
import org.eclipse.oomph.util.UserCallback;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class ConsoleInstaller {

    private SetupContext context;
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
        this.version = product.contains(":") ? product.split(":")[1] : Parameters.VERSION;
        this.project = Parameters.PROJECT.replaceAll("\\s+", "");
        this.redirection = Parameters.REDIRECTION;
        this.location = Parameters.INSTALLATION_LOCATION;
        this.folder = Parameters.INSTALLATION_PRODUCT_FOLDER;
        this.workspace = Parameters.WORKSPACE_LOCATION;
        this.launch = Parameters.LAUNCH_AUTOMATICALLY;
        this.verbose = Parameters.VERBOSE;
        if (Parameters.SSL_INSECURE)
            disableSSLVerification();
    }

    public void run() throws Exception {
        init(this.product, this.version, this.project);
        URIConverter uriConverter = this.resourceSet.getURIConverter();

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
                for (SetupTaskContext setupTaskContext : performers) {
                    SetupTaskPerformer promptedPerformer = (SetupTaskPerformer) setupTaskContext;
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
        adjuster.setWorkspaceLacation(this.context.getUser(), this.workspace);
        adjuster.setInstallationLocation(this.context.getUser(), this.location);
        adjuster.setProductFolderName(this.context.getUser(), this.folder);
        adjuster.setProductFolderName(this.context.getInstallation().getProductVersion(), this.folder);

        Trigger trigger = Trigger.getByName(Parameters.TRIGGER);
        SetupTaskPerformer performer = SetupTaskPerformer.create(uriConverter, prompter, trigger, context, false);

        performer.recordVariables(this.context.getInstallation(), this.context.getWorkspace(), this.context.getUser());
        if (Parameters.CLEAN_UNRESOLVED)
            performer.getUnresolvedVariables().clear();
        performer.put(UIServices.class, P2ServiceUI.SERVICE_UI);
        performer.put(CacheUsageConfirmer.class, new AcceptCacheUsageConfirmer());
        performer.put(ILicense.class, Confirmer.ACCEPT);
        performer.put(Certificate.class, Confirmer.ACCEPT);
        performer.setProgress(new ConsoleProgressLog(Parameters.TEXT_LAYOUT));
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

        performer.perform(new ConsoleProgressMonitor(this.verbose, getHeaderText(this.context)));

        saveResources(performer);

        if (this.launch)
            LaunchUtil.launchProduct(performer, true);
    }

    private void saveResources(SetupTaskPerformer performer) throws IOException {
        Installation installation = performer.getInstallation();
        Resource installationResource = installation.eResource();
        Objects.requireNonNull(installationResource, "installationResource is null");
        installationResource.setURI(URI.createFileURI(new File(performer.getProductConfigurationLocation(),
                "org.eclipse.oomph.setup/installation.setup").toString()));

        Workspace workspace = performer.getWorkspace();
        Resource workspaceResource = workspace != null ? workspace.eResource() : null;
        if (workspaceResource != null) {
            workspaceResource.setURI(URI.createFileURI(new File(performer.getWorkspaceLocation(),
                    ".metadata/.plugins/org.eclipse.oomph.setup/workspace.setup").toString()));
        }
        performer.savePasswords();

        if (performer.getProductConfigurationLocation() != null)
            installationResource.save(Collections.emptyMap());
        if (workspaceResource != null && performer.getWorkspaceLocation() != null) {
            workspaceResource.save(Collections.emptyMap());
        }
    }

    private void init(String productId, String versionId, String projectId) throws NotFoundException {
        InstallationInitializer installationHelper = new InstallationInitializer();
        this.resourceSet = installationHelper.getResourceSet();
        initInstallation(productId, versionId, projectId);
    }

    private void initInstallation(String productId, String versionId, String projectId) throws NotFoundException {
        ProductVersionSelector selector = new ProductVersionSelector(this.resourceSet);
        ProductVersion version = null;
        Installation installation = null;
        List<Stream> streams = new LinkedList<>();

        Configuration configuration = selector.selectConfiguration();
        if (configuration != null) {
            version = configuration.getInstallation().getProductVersion();
            if (version != null) {
                this.context = SetupContext.create(this.resourceSet, version);
                installation = this.context.getInstallation();
                installation.getSetupTasks().addAll(configuration.getInstallation().getSetupTasks());
            }
            Workspace workspace = configuration.getWorkspace();
            if (workspace != null) {
                streams.addAll(workspace.getStreams());
            }
        }

        if (version == null) {
            Product product = selector.selectProduct(productId);
            version = selector.selectProductVersion(product, versionId);
            this.context = SetupContext.create(this.resourceSet, version);
            installation = this.context.getInstallation();
        }

        installation.setProductVersion(version);

        if (!projectId.isEmpty()) {
            streams.addAll(selector.selectProjectStreams(Arrays.asList(projectId.split(","))));
        }

        User user = this.context.getUser();
        user.setUnsignedPolicy(UnsignedPolicy.ACCEPT);
        user.setCertificatePolicy(CertificatePolicy.ACCEPT);

        this.context = SetupContext.create(installation, streams, user);
    }

    private void disableSSLVerification() {
        BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
        bundleContext.registerService(SSLSocketFactory.class.getName(), NonStrictSSL.getSSLSocketFactory(), null);
        bundleContext.registerService(HostnameVerifier.class.getName(), NonStrictSSL.getHostnameVerifier(), null);
    }

    private String getHeaderText(SetupContext context) {
        String versionTasks = getChilds(context.getInstallation().getProductVersion().getSetupTasks(),
                new LinkedList<>()).stream()
                .filter(t -> t instanceof P2Task || t instanceof CompoundTask)
                .map(t -> t instanceof P2Task ? ((P2Task) t).getLabel() : ((CompoundTask) t).getName())
                .collect(Collectors.joining(" + "));
        String tasks = getChilds(context.getInstallation().getSetupTasks(), new LinkedList<>()).stream()
                .filter(t -> t instanceof P2Task || t instanceof CompoundTask)
                .map(t -> t instanceof P2Task ? ((P2Task) t).getLabel() : ((CompoundTask) t).getName())
                .collect(Collectors.joining(" + "));
        String streams = context.getWorkspace().getStreams().stream()
                .map(s -> s.getProject().getName() != null ? s.getProject().getName() : s.getProject().getLabel())
                .collect(Collectors.joining(" + "));
        return "Performing P2 Director (" + versionTasks
                + (!tasks.isEmpty() ? " + " + tasks : "")
                + (!streams.isEmpty() ? " + " + streams : "") + ")";
    }

    private List<SetupTask> getChilds(List<SetupTask> src, List<SetupTask> dst) {
        for (SetupTask t : src) {
            if (t instanceof CompoundTask) {
                getChilds(((CompoundTask) t).getSetupTasks(), dst);
            } else {
                dst.add(t);
            }
        }
        return dst;
    }

}
