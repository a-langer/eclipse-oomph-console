package org.eclipse.oomph.console.core.util;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.oomph.setup.Index;
import org.eclipse.oomph.setup.SetupPackage;
import org.eclipse.oomph.setup.internal.core.SetupContext;
import org.eclipse.oomph.setup.internal.core.util.CatalogManager;
import org.eclipse.oomph.setup.internal.core.util.ECFURIHandlerImpl;
import org.eclipse.oomph.setup.internal.core.util.ResourceMirror;
import org.eclipse.oomph.setup.internal.core.util.SetupCoreUtil;
import org.eclipse.oomph.util.CollectionUtil;

@SuppressWarnings("restriction")
public class InstallationInitializer {

    private final CatalogManager catalogManager;

    private ResourceSet resourceSet;

    public InstallationInitializer() {
        catalogManager = new CatalogManager();
        resourceSet = SetupCoreUtil.createResourceSet();
        resourceSet.getLoadOptions().put(ECFURIHandlerImpl.OPTION_CACHE_HANDLING,
                ECFURIHandlerImpl.CacheHandling.CACHE_WITH_ETAG_CHECKING);
        loadResourceSet(SetupContext.INDEX_SETUP_URI, SetupContext.USER_SETUP_URI);
        loadIndex(new NullProgressMonitor());
    }

    private void loadResourceSet(final URI... uris) {
        ResourceMirror resourceMirror = new ResourceMirror(resourceSet) {
            @Override
            protected void run(String taskName, IProgressMonitor monitor) {
                perform(uris);
            }
        };

        resourceMirror.begin(new NullProgressMonitor());
    }

    public void loadIndex(IProgressMonitor progressMonitor) {
        Map<EClass, Set<URI>> uriMap = new LinkedHashMap<>();
        final Map<URI, Resource> resourceMap = new LinkedHashMap<>();
        URIConverter uriConverter = resourceSet.getURIConverter();
        for (Resource resource : resourceSet.getResources()) {
            EList<EObject> contents = resource.getContents();
            if (!contents.isEmpty()) {
                // Allow subclasses to override which types of objects are of
                // interest for reloading.
                // The simple installer is only interested in the index,
                // products, and product catalogs.
                EClass eClass = contents.get(0).eClass();
                // If the scheme is remote...
                URI uri = uriConverter.normalize(resource.getURI());
                String scheme = uri.scheme();
                if ("http".equals(scheme) || "https".equals(scheme)) {
                    // Group the URIs by object type so we can reload "the most
                    // import" types of objects first.
                    CollectionUtil.add(uriMap, eClass, uri);
                    resourceMap.put(uri, resource);
                }
            }
        }

        // Collect the URIs is order of importance.
        Set<URI> resourceURIs = new LinkedHashSet<>();
        for (EClass eClass : new EClass[] { SetupPackage.Literals.INDEX, SetupPackage.Literals.PRODUCT_CATALOG,
                SetupPackage.Literals.PRODUCT, SetupPackage.Literals.PROJECT_CATALOG, SetupPackage.Literals.PROJECT }) {
            Set<URI> uris = uriMap.remove(eClass);
            if (uris != null) {
                resourceURIs.addAll(uris);
            }
        }

        for (Set<URI> uris : uriMap.values()) {
            resourceURIs.addAll(uris);
        }
        final Set<Resource> updatedResources = new HashSet<>();
        // If there are resources to consider...
        if (!resourceURIs.isEmpty()) {
            // Remember which resource actually need updating based on detected
            // remote changes by the ETag mirror.
            new ECFURIHandlerImpl.ETagMirror() {
                @Override
                protected synchronized void cacheUpdated(URI uri) {
                    updatedResources.add(resourceMap.get(uri));
                }
            }.begin(resourceURIs, progressMonitor);
        }
        Resource resource = resourceSet.getResource(SetupContext.INDEX_SETUP_URI, false);
        catalogManager
                .indexLoaded((Index) EcoreUtil.getObjectByType(resource.getContents(), SetupPackage.Literals.INDEX));
    }

    public ResourceSet getResourceSet() {
        return resourceSet;
    }
}
