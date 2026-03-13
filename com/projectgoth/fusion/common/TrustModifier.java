package com.projectgoth.fusion.common;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;

public class TrustModifier {
   public static void relaxHostChecking(DefaultHttpClient client) throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
      ClientConnectionManager cm = client.getConnectionManager();
      SchemeRegistry sr = cm.getSchemeRegistry();
      sr.register(new Scheme("https", getSocketFactory(), 443));
   }

   public static SSLSocketFactory getSocketFactory() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
      SSLContext ctx = SSLContext.getInstance("TLS");
      ctx.init((KeyManager[])null, new TrustManager[]{new TrustModifier.AlwaysTrustManager()}, (SecureRandom)null);
      SSLSocketFactory ssf = new SSLSocketFactory(ctx);
      ssf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
      return ssf;
   }

   private static class AlwaysTrustManager implements X509TrustManager {
      private AlwaysTrustManager() {
      }

      public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
      }

      public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
      }

      public X509Certificate[] getAcceptedIssuers() {
         return null;
      }

      // $FF: synthetic method
      AlwaysTrustManager(Object x0) {
         this();
      }
   }
}
