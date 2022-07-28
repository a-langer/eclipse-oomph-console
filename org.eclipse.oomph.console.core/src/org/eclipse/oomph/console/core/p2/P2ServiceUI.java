package org.eclipse.oomph.console.core.p2;

import java.security.cert.Certificate;

import org.eclipse.equinox.p2.core.UIServices;

public class P2ServiceUI extends UIServices {

    public static final P2ServiceUI SERVICE_UI = new P2ServiceUI();

    @Override
    public AuthenticationInfo getUsernamePassword(String location) {
        return null;
    }

    @Override
    public AuthenticationInfo getUsernamePassword(String location, AuthenticationInfo previousInfo) {
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public TrustInfo getTrustInfo(Certificate[][] untrustedChain, String[] unsignedDetail) {
        final Certificate[] trusted;
        if (untrustedChain == null) {
            trusted = null;
        } else {
            trusted = new Certificate[untrustedChain.length];
            for (int i = 0; i < untrustedChain.length; i++) {
                trusted[i] = untrustedChain[i][0];
            }
        }
        return new TrustInfo(trusted, false, true);
    }

}
