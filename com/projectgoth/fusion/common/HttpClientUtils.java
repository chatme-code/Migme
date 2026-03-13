package com.projectgoth.fusion.common;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

public class HttpClientUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(HttpClientUtils.class));
   private static final int DEFAULT_HTTP_PORT = 80;
   private static final int DEFAULT_HTTPS_PORT = 443;
   private static final HttpClientUtils.ResponseProcessor<Object, Document> DOCUMENT_EXTRACTOR = new HttpClientUtils.ResponseProcessor<Object, Document>() {
      public Document processOnNullRespose(Object extraData) throws IOException {
         return null;
      }

      public Document processStream(InputStream ins, Object extraData_unused) throws IOException {
         try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            return docBuilder.parse(ins);
         } catch (Exception var5) {
            IOException ioex = new IOException("Error extracting xml data");
            ioex.initCause(var5);
            throw ioex;
         }
      }
   };
   private static final HttpClientUtils.ResponseProcessor<Charset, String> STRING_EXTRACTOR = new HttpClientUtils.ResponseProcessor<Charset, String>() {
      public String processOnNullRespose(Charset extraData_encoding) throws IOException {
         return null;
      }

      public String processStream(InputStream ins, Charset extraData_charSet) throws IOException {
         try {
            StringBuilder result = new StringBuilder();
            BufferedReader in = new BufferedReader(new InputStreamReader(ins, extraData_charSet));

            int charInt;
            try {
               while((charInt = in.read()) >= 0) {
                  result.append((char)charInt);
               }
            } finally {
               in.close();
            }

            return result.toString();
         } catch (Exception var11) {
            IOException ioEx = new IOException("Error extracting xml data");
            ioEx.initCause(var11);
            throw ioEx;
         }
      }
   };

   private HttpClientUtils() {
   }

   private static DefaultHttpClient createHttpClient(HttpClientUtils.HttpClientConfig clientConfig) throws GeneralSecurityException {
      SchemeRegistry schemeRegistry = new SchemeRegistry();
      schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
      HttpParams params = new BasicHttpParams();
      HttpConnectionParams.setConnectionTimeout(params, clientConfig.timeOutInMillis);
      HttpConnectionParams.setSoTimeout(params, clientConfig.timeOutInMillis);
      ConnManagerParams.setMaxTotalConnections(params, clientConfig.maxConnections);
      ConnManagerParams.setTimeout(params, (long)clientConfig.timeOutInMillis);
      ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(clientConfig.maxConnections));
      SingleClientConnManager cm = new SingleClientConnManager(params, schemeRegistry);
      DefaultHttpClient client = new DefaultHttpClient(cm, params);
      client.setKeepAliveStrategy(getKeepAliveStrategy(clientConfig));
      if (clientConfig.strictHttpsCertCheck) {
         SchemeRegistry currSchemeRegistry = client.getConnectionManager().getSchemeRegistry();
         currSchemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
      } else {
         TrustModifier.relaxHostChecking(client);
      }

      return client;
   }

   private static void setCredentials(DefaultHttpClient client, HttpClientUtils.HttpClientConfig config, URI uri) {
      if (config.username != null) {
         if (config.password == null) {
            throw new IllegalArgumentException("Password not supplied for username [" + config.username + "]");
         }

         client.getCredentialsProvider().setCredentials(new AuthScope(uri.getHost(), uri.getPort()), new UsernamePasswordCredentials(config.username, config.password));
      }

   }

   private static ConnectionKeepAliveStrategy getKeepAliveStrategy(HttpClientUtils.HttpClientConfig hcConfig) {
      class CustomConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
         HttpClientUtils.HttpClientConfig hcConfig;

         public CustomConnectionKeepAliveStrategy(HttpClientUtils.HttpClientConfig hcConfig) {
            this.hcConfig = hcConfig;
         }

         public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));

            while(true) {
               String param;
               String value;
               do {
                  do {
                     if (!it.hasNext()) {
                        return (long)(1000 * this.hcConfig.keepAliveInSecs);
                     }

                     HeaderElement he = it.nextElement();
                     param = he.getName();
                     value = he.getValue();
                  } while(value == null);
               } while(!param.equalsIgnoreCase("timeout"));

               try {
                  return Long.parseLong(value) * 1000L;
               } catch (NumberFormatException var8) {
               }
            }
         }
      }

      return new CustomConnectionKeepAliveStrategy(hcConfig);
   }

   private static void cleanUp(HttpClient httpClient) {
      try {
         httpClient.getConnectionManager().shutdown();
      } catch (Exception var2) {
         log.warn("Error cleaning up httpClient", var2);
      }

   }

   public static Document getXMLResponseAsDocument(HttpClientUtils.HttpClientConfig httpClientConfig, HttpUriRequest request) throws GeneralSecurityException, IOException {
      return (Document)processResponse(httpClientConfig, request, DOCUMENT_EXTRACTOR, (Object)null);
   }

   public static String getResponseString(HttpClientUtils.HttpClientConfig httpClientConfig, HttpUriRequest request, Charset charSet) throws GeneralSecurityException, IOException {
      if (charSet == null) {
         charSet = Charset.defaultCharset();
      }

      return (String)processResponse(httpClientConfig, request, STRING_EXTRACTOR, charSet);
   }

   public static <TOutput, TExtra> TOutput processResponse(HttpClientUtils.HttpClientConfig httpClientConfig, HttpUriRequest request, HttpClientUtils.ResponseProcessor<TExtra, TOutput> processor, TExtra extraData) throws GeneralSecurityException, IOException {
      TOutput output = null;
      DefaultHttpClient client = createHttpClient(httpClientConfig);

      try {
         setCredentials(client, httpClientConfig, request.getURI());
         HttpResponse response = client.execute(request);
         HttpEntity entity = response.getEntity();
         if (entity != null) {
            InputStream ins = entity.getContent();

            try {
               output = processor.processStream(ins, extraData);
            } finally {
               ins.close();
            }
         } else {
            output = processor.processOnNullRespose(extraData);
         }
      } finally {
         cleanUp(client);
      }

      return output;
   }

   public interface ResponseProcessor<TExtra, TOutput> {
      TOutput processStream(InputStream var1, TExtra var2) throws IOException;

      TOutput processOnNullRespose(TExtra var1) throws IOException;
   }

   public static class HttpClientConfig {
      public int timeOutInMillis = 30000;
      public int maxConnections = 100;
      public boolean strictHttpsCertCheck = true;
      public int keepAliveInSecs = 60;
      public String username = null;
      public String password = null;
   }
}
