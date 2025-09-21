package com.infoworks.utils.rest.base;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public final class SSLContextFactory {

    /**
     * trustStorePath: e.g. /src/../resources/mytruststore.jks
     * @param trustStorePath
     * @param password
     * @return a SSLContext with TLS protocol.
     */
    public static SSLContext createContext(Path trustStorePath, String password)
            throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException {
        //Load certificate:
        char[] pass = password.toCharArray();
        KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream is = Files.newInputStream(trustStorePath)) {
            trustStore.load(is, pass);
        } catch (Exception e) {
            throw new CertificateException(e);
        }
        //Prepare SSLContext:
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);
        return sslContext;
    }

    /**
     * Bypass SSL Validation
     * For testing purposes, disable SSL validation
     * @return a SSLContext with TLS protocol.
     */
    public static SSLContext createDefaultContext() throws KeyManagementException, NoSuchAlgorithmException {
        TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                    public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                }
        };
        //Generate the Context:
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new SecureRandom());
        return sslContext;
    }

}
