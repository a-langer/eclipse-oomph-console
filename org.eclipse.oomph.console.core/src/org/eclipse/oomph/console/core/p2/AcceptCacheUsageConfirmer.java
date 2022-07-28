package org.eclipse.oomph.console.core.p2;

import java.io.File;

import org.eclipse.emf.common.util.URI;
import org.eclipse.oomph.p2.internal.core.CacheUsageConfirmer;

@SuppressWarnings("restriction")
public class AcceptCacheUsageConfirmer extends CacheUsageConfirmer {

    public boolean confirmCacheUsage(final URI uri, File file) {
        return true;
    }

    @Override
    public boolean confirmCacheUsage(java.net.URI uri, File file) {
        return true;
    }

}
