package org.eclipse.oomph.console.core.application;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.UIServices;
import org.eclipse.oomph.console.core.Activator;
import org.eclipse.oomph.console.core.p2.P2ServiceUI;
import org.eclipse.oomph.p2.core.Agent;
import org.eclipse.oomph.p2.core.AgentManager;
import org.eclipse.oomph.p2.core.BundlePool;
import org.eclipse.oomph.p2.core.P2Util;
import org.osgi.framework.Version;

@SuppressWarnings("restriction")
public abstract class AbstractLauncherApplication implements IApplication {

    @Override
    public Object start(final IApplicationContext context) throws Exception {
        List<String> list = Arrays.asList(Platform.getCommandLineArgs());
        if (list.contains("-version")) {
            // Note: This one returns the version of the defining bundle, not the version in
            // the product definition file (.product file) since it's not available at
            // runtime
            Version v = Platform.getProduct().getDefiningBundle().getVersion();
            String semVer = String.format("%d.%d.%d+%s", v.getMajor(), v.getMinor(), v.getMicro(), v.getQualifier());
            System.out.println("Version: " + semVer);
            return EXIT_OK;
            // https://bugs.eclipse.org/bugs/show_bug.cgi?id=297922
        }

        P2Util.getCurrentProvisioningAgent().registerService(UIServices.SERVICE_NAME, P2ServiceUI.SERVICE_UI);
        IProvisioningAgent agent = (IProvisioningAgent) org.eclipse.equinox.internal.p2.core.helpers.ServiceHelper
                .getService(org.eclipse.equinox.internal.p2.repository.Activator.getContext(),
                        IProvisioningAgent.SERVICE_NAME);
        agent.registerService(UIServices.SERVICE_NAME, P2ServiceUI.SERVICE_UI);
        initBundlePool();
        setSharedBundlePoolPermissions(System.getProperty("setup.p2.agent"));
        run();
        setSharedBundlePoolPermissions(System.getProperty("setup.p2.agent"));

        return null;
    }

    public abstract void run() throws Exception;

    private void initBundlePool() {
        String customBundlePoolLocation = System.getProperty("setup.p2.agent");
        BundlePool pool = null;
        if (customBundlePoolLocation != null) {
            pool = P2Util.getAgentManager().getBundlePool(new File(customBundlePoolLocation));
            if (pool == null) {
                Agent agent = P2Util.getAgentManager().addAgent(new File(customBundlePoolLocation));
                pool = agent.addBundlePool(new File(customBundlePoolLocation, BundlePool.DEFAULT_NAME));
                P2Util.getAgentManager().setDefaultBundlePool(Activator.PLUGIN_ID, pool);
            }
        }
        setCurrentBundlePool(pool);
    }

    private void setCurrentBundlePool(BundlePool pool) {
        if (pool != null) {
            P2Util.getAgentManager().setDefaultBundlePool(Activator.PLUGIN_ID, pool);
            System.setProperty(AgentManager.PROP_BUNDLE_POOL_LOCATION, pool.getLocation().getAbsolutePath());
        } else {
            System.clearProperty(AgentManager.PROP_BUNDLE_POOL_LOCATION);
        }
    }

    @Override
    public void stop() {
    }

    private void setSharedBundlePoolPermissions(String location) {
        if (location == null) {
            return;
        }
        try {
            Files.walkFileTree(Paths.get(location), new FilePermissionVisitor());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }

}
