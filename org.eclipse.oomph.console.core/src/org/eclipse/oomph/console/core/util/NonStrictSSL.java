package org.eclipse.oomph.console.core.util;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NonStrictSSL {

    private NonStrictSSL() {
    }

    private static TrustManager[] trustManager = new TrustManager[] { new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[] {};
        }

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // nothing
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                throws CertificateException {
            // nothing
        }
    } };

    private static SecureRandom secRandom = new SecureRandom();

    private static KeyManager[] keyManager = new KeyManager[0];

    public static TrustManager[] getTrustManager() {
        return trustManager;
    }

    public static SecureRandom getSecureRandom() {
        return secRandom;
    }

    public static KeyManager[] getKeyManager() {
        return keyManager;
    }

    public static SSLContext getSSLContext() {
        SSLContext ctx = null;
        try {
            ctx = SSLContext.getInstance("SSL"); // TLSv1.2
            ctx.init(getKeyManager(), getTrustManager(), getSecureRandom());
            SSLContext.setDefault(ctx);
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        return ctx;
    }

    public static SSLSocketFactory getSSLSocketFactory() {
        return getSSLContext().getSocketFactory();
    }

    public static HostnameVerifier getHostnameVerifier() {
        return (String hostname, SSLSession session) -> {
            return true;
        };
    }
}
