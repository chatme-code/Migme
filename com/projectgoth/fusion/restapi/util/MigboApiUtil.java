package com.projectgoth.fusion.restapi.util;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.TrustModifier;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

public class MigboApiUtil {
   private static final String APPLICATION_VND_MIG33API_V1_JSON = "application/vnd.mig33api-v1+json;charset=UTF-8";
   private static final String DEFAULT_CHARSET = "UTF-8";
   public static final String MIGBO_SEND_EMAIL_URL = "/user/%s/email/%s?follower=%s";
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MigboApiUtil.class));
   public static final String JSONKEY_ERROR = "error";
   public static final String JSONKEY_ERRNO = "errno";
   public static final String JSONKEY_ERRORMESSAGE = "message";
   public static final String JSONKEY_DATA = "data";
   private String migboDatasvcHost;
   private int migboDatasvcPort;
   private String migboDatasvcPathPrefix;
   private boolean migboDatasvcUseHttps;
   private boolean migboDatasvcUseGzipCompression;
   private int maxTotalConnectionsToMigbo;
   private long timeOutInMillis;
   private int keepAliveInSeconds;
   private String protocol;
   private HttpHost hcHost;
   DefaultHttpClient httpclient;
   private static MigboApiUtil theInstance = new MigboApiUtil();
   private static MigboApiUtil theInstanceForValhalla = null;
   private static final Pattern API_PATH_USER_ID = Pattern.compile("/user/([0-9]+)/.*");

   private MigboApiUtil() {
      this(false);
   }

   private MigboApiUtil(boolean useValhallaMigboDatasvc) {
      Properties props = null;
      String propFile = System.getProperty("fusion.datasvc.config", "/usr/fusion/etc/datasvc.properties");
      log.info(String.format("Attempting to load from %s", propFile));
      File f = new File(propFile);
      if (f.exists()) {
         props = new Properties();

         try {
            log.info(String.format("Loading config from file %s", propFile));
            props.load(new FileInputStream(f));
         } catch (FileNotFoundException var6) {
            log.error(String.format("Unable to find config file from %s", propFile));
            props = null;
         } catch (IOException var7) {
            log.error(String.format("Unable to load config from %s", propFile), var7);
            props = null;
         }
      } else {
         log.info(String.format("Config file %s not found", propFile));
      }

      if (props == null) {
         log.info(String.format("Loading config from System.properties"));
         props = System.getProperties();
      }

      this.migboDatasvcHost = props.getProperty("migbo.datasvc.host", "localhost");
      this.migboDatasvcPort = Integer.parseInt(props.getProperty("migbo.datasvc.port", "80"));
      this.migboDatasvcPathPrefix = props.getProperty("migbo.datasvc.pathPrefix", "migbo_datasvc");
      this.migboDatasvcUseHttps = Boolean.parseBoolean(props.getProperty("migbo.datasvc.useHttps", "false"));
      this.migboDatasvcUseGzipCompression = Boolean.parseBoolean(props.getProperty("migbo.datasvc.useGzipCompression", "false"));
      if (useValhallaMigboDatasvc) {
         this.migboDatasvcHost = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.VALHALLA_MIGBO_DATASVC_HOST);
         this.migboDatasvcPort = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.VALHALLA_MIGBO_DATASVC_PORT);
         this.migboDatasvcUseHttps = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.VALHALLA_MIGBO_DATASVC_USEHTTPS);
      }

      this.maxTotalConnectionsToMigbo = Integer.parseInt(props.getProperty("migbo.datasvc.maxconn", "200"));
      this.timeOutInMillis = Long.parseLong(props.getProperty("migbo.datasvc.conn_timeout_millis", "5000"));
      this.keepAliveInSeconds = Integer.parseInt(props.getProperty("migbo.datasvc.keepalive_seconds", "60"));
      this.protocol = this.migboDatasvcUseHttps ? "https" : "http";
      this.hcHost = new HttpHost(this.migboDatasvcHost, this.migboDatasvcPort, this.protocol);
      log.info(String.format("api url is %s", this.hcHost.toString()));
   }

   public static MigboApiUtil getInstance() {
      return theInstance;
   }

   public static synchronized void resetMigboApiUtil() {
      if (theInstance != null) {
         if (theInstance.httpclient != null) {
            theInstance.httpclient.getConnectionManager().shutdown();
            theInstance.httpclient = null;
         }

         theInstance = new MigboApiUtil();
      }
   }

   private static synchronized MigboApiUtil getInstanceForValhalla() {
      if (theInstanceForValhalla == null) {
         theInstanceForValhalla = new MigboApiUtil(true);
      }

      return theInstanceForValhalla;
   }

   public static synchronized void resetMigboApiUtilForValhalla() {
      if (theInstanceForValhalla != null) {
         if (theInstanceForValhalla.httpclient != null) {
            theInstanceForValhalla.httpclient.getConnectionManager().shutdown();
            theInstanceForValhalla.httpclient = null;
         }

      }
   }

   private HttpClient getHttpClient() throws MigboApiUtil.MigboApiException {
      if (this.httpclient == null) {
         this.createHttpClient();
      }

      if (this.httpclient == null) {
         String errMsg = "Unable to create httpclient";
         log.error(errMsg);
         throw new MigboApiUtil.MigboApiException(-1, errMsg);
      } else {
         return this.httpclient;
      }
   }

   private synchronized boolean createHttpClient() {
      if (this.httpclient != null) {
         return true;
      } else {
         try {
            log.info("Creating http client");
            SchemeRegistry schemeRegistry = new SchemeRegistry();
            schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, (int)this.timeOutInMillis);
            HttpConnectionParams.setSoTimeout(params, (int)this.timeOutInMillis);
            ConnManagerParams.setMaxTotalConnections(params, this.maxTotalConnectionsToMigbo);
            ConnManagerParams.setTimeout(params, this.timeOutInMillis);
            ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRouteBean(this.maxTotalConnectionsToMigbo));
            ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
            DefaultHttpClient client = new DefaultHttpClient(cm, params);
            client.setKeepAliveStrategy(this.getKeepAliveStrategy());
            if (this.migboDatasvcUseHttps) {
               try {
                  TrustModifier.relaxHostChecking(client);
               } catch (NoSuchAlgorithmException var6) {
                  log.error(String.format("Unable to add https support for httpclient"), var6);
                  return false;
               } catch (KeyManagementException var7) {
                  log.error(String.format("Unable to add https support for httpclient"), var7);
                  return false;
               }
            }

            if (this.migboDatasvcUseGzipCompression) {
               client.addRequestInterceptor(new HttpRequestInterceptor() {
                  public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {
                     if (!request.containsHeader("Accept-Encoding")) {
                        request.addHeader("Accept-Encoding", "gzip");
                     }

                  }
               });
               client.addResponseInterceptor(new HttpResponseInterceptor() {
                  public void process(HttpResponse response, HttpContext context) throws HttpException, IOException {
                     HttpEntity entity = response.getEntity();
                     if (entity != null) {
                        Header ceheader = entity.getContentEncoding();
                        if (ceheader != null) {
                           HeaderElement[] codecs = ceheader.getElements();

                           for(int i = 0; i < codecs.length; ++i) {
                              if (codecs[i].getName().equalsIgnoreCase("gzip")) {
                                 response.setEntity(new GzipDecompressingEntity(response.getEntity()));
                                 return;
                              }
                           }
                        }
                     }

                  }
               });
            }

            this.httpclient = client;
            log.info("Http client created");
            return true;
         } catch (Exception var8) {
            log.error("Unexpected Exception while attempting to create new HTTP Client :" + var8.getMessage(), var8);
            return false;
         }
      }
   }

   private boolean isValhallaApiPath(String path, String method) {
      if (this != theInstanceForValhalla && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.VALHALLA_TEST)))) {
         Matcher m = API_PATH_USER_ID.matcher(path);
         if (m.matches()) {
            try {
               int userId = Integer.parseInt(m.group(1));
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               return userEJB.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.VALHALLA_TEST.value());
            } catch (Exception var6) {
               log.error("Failed to check valhalla data svc api for path " + path + ", method " + method, var6);
            }
         }
      }

      return false;
   }

   private JSONObject createMigboDisabledResponse(String method, String pathPrefix, String postData) {
      JSONObject root = new JSONObject();
      JSONObject data = new JSONObject();

      try {
         data.put("message", "migbo is disabled");
         data.put("method", method);
         data.put("pathPrefix", pathPrefix);
         data.put("postData", postData);
         root.put("data", data);
      } catch (JSONException var7) {
      }

      return root;
   }

   public JSONObject post(String pathPrefix, String postData) throws MigboApiUtil.MigboApiException {
      if (SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.MIGBO_DISABLED) != 0) {
         log.warn("Migbo api call is not made because migbo is disabled now. type: POST, pathPrefix: " + pathPrefix + ", postData: " + postData);
         return this.createMigboDisabledResponse("post", pathPrefix, postData);
      } else if (this.isValhallaApiPath(pathPrefix, "post")) {
         return getInstanceForValhalla().post(pathPrefix, postData);
      } else {
         HttpClient httpclient = this.getHttpClient();
         String path = this.migboDatasvcPathPrefix + pathPrefix;
         HttpPost httppost = new HttpPost(path);
         httppost.addHeader("Content-Type", "application/vnd.mig33api-v1+json;charset=UTF-8");
         httppost.addHeader("Connection", "close");

         try {
            httppost.setEntity(new StringEntity(postData, "UTF-8"));
         } catch (UnsupportedEncodingException var11) {
            String errMsg = String.format("Error in create request to make migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var11);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         HttpResponse response = null;

         String errMsg;
         try {
            log.info(String.format("Making POST migbo data svc api call to %s%s", this.hcHost.toString(), path));
            response = httpclient.execute(this.hcHost, httppost);
            log.info(String.format("Received response from migbo data svc api call %s", this.hcHost.toString()));
         } catch (ClientProtocolException var9) {
            errMsg = String.format("Error in making migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var9);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         } catch (IOException var10) {
            errMsg = String.format("IOException in making migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var10);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         return this.parseHttpResponse(response);
      }
   }

   public void postOneWay(String pathPrefix, String postData) throws MigboApiUtil.MigboApiException {
      if (SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.MIGBO_DISABLED) != 0) {
         log.warn("Migbo api call is not made because migbo is disabled now. type: POST oneway, pathPrefix: " + pathPrefix + ", postData: " + postData);
      } else if (this.isValhallaApiPath(pathPrefix, "postOneWay")) {
         getInstanceForValhalla().postOneWay(pathPrefix, postData);
      } else if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.ONEWAY_MIGBO_API_CALLS_ENABLED)) {
         JSONObject result = this.post(pathPrefix, postData);
         log.info(String.format("One Way Post DISABLE! Make a normal post instead, pathPrefix:%s, postData:%s, return result:%s", pathPrefix, postData, result == null ? "NULL" : result.toString()));
      } else {
         HttpClient httpclient = this.getHttpClient();
         StringBuilder sb = (new StringBuilder()).append(this.migboDatasvcPathPrefix).append(pathPrefix).append(pathPrefix.contains("?") ? "&" : "?").append("oneway=1");
         String path = sb.toString();
         HttpPost httppost = new HttpPost(path);
         httppost.addHeader("Content-Type", "application/vnd.mig33api-v1+json;charset=UTF-8");

         String errMsg;
         try {
            httppost.setEntity(new StringEntity(postData, "UTF-8"));
         } catch (UnsupportedEncodingException var11) {
            errMsg = String.format("Error in create request to make oneway migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var11);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         try {
            log.info(String.format("Making oneway POST migbo data svc api call to %s%s", this.hcHost.toString(), path));
            this.consumeHttpResponse(httpclient.execute(this.hcHost, httppost));
         } catch (ClientProtocolException var9) {
            errMsg = String.format("Error in making oneway migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var9);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         } catch (IOException var10) {
            errMsg = String.format("IOException in making oneway migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var10);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }
      }
   }

   public JSONObject put(String pathPrefix, String putData) throws MigboApiUtil.MigboApiException {
      if (SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.MIGBO_DISABLED) != 0) {
         log.warn("Migbo api call is not made because migbo is disabled now. type: PUT, pathPrefix: " + pathPrefix + ", postData: " + putData);
         return this.createMigboDisabledResponse("put", pathPrefix, putData);
      } else if (this.isValhallaApiPath(pathPrefix, "put")) {
         return getInstanceForValhalla().put(pathPrefix, putData);
      } else {
         HttpClient httpclient = this.getHttpClient();
         String path = this.migboDatasvcPathPrefix + pathPrefix;
         HttpPut httpput = new HttpPut(path);
         httpput.addHeader("Content-Type", "application/vnd.mig33api-v1+json;charset=UTF-8");
         httpput.addHeader("Connection", "close");

         try {
            httpput.setEntity(new StringEntity(putData, "UTF-8"));
         } catch (UnsupportedEncodingException var11) {
            String errMsg = String.format("Error in create request to make migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var11);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         HttpResponse response = null;

         String errMsg;
         try {
            log.info(String.format("Making PUT migbo data svc api call to %s%s", this.hcHost.toString(), path));
            response = httpclient.execute(this.hcHost, httpput);
            log.info(String.format("Received response from migbo data svc api call %s", this.hcHost.toString()));
         } catch (ClientProtocolException var9) {
            errMsg = String.format("Error in making migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var9);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         } catch (IOException var10) {
            errMsg = String.format("IOException in making migbo data svc api call to %s", this.hcHost.toString());
            log.error(errMsg, var10);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         return this.parseHttpResponse(response);
      }
   }

   public JSONObject get(String pathPrefix) throws MigboApiUtil.MigboApiException {
      if (SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.MIGBO_DISABLED) != 0) {
         log.warn("Migbo api call is not made because migbo is disabled now. type: GET, pathPrefix: " + pathPrefix);
         return this.createMigboDisabledResponse("get", pathPrefix, (String)null);
      } else if (this.isValhallaApiPath(pathPrefix, "get")) {
         return getInstanceForValhalla().get(pathPrefix);
      } else {
         HttpClient httpclient = this.getHttpClient();
         String path = this.migboDatasvcPathPrefix + pathPrefix;
         HttpGet httpget = new HttpGet(path);
         httpget.addHeader("Content-Type", "application/vnd.mig33api-v1+json;charset=UTF-8");
         httpget.addHeader("Connection", "close");
         HttpResponse response = null;

         String errMsg;
         try {
            log.info(String.format("Making GET migbo data svc api call to %s%s", this.hcHost.toString(), path));
            response = httpclient.execute(this.hcHost, httpget);
            log.info(String.format("Received response from migbo data svc api call %s%s", this.hcHost.toString(), path));
         } catch (ClientProtocolException var8) {
            errMsg = String.format("Error in making migbo data svc api call to %s%s", this.hcHost.toString(), path);
            log.error(errMsg, var8);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         } catch (IOException var9) {
            errMsg = String.format("IOException in making migbo data svc api call to %s%s", this.hcHost.toString(), path);
            log.error(errMsg, var9);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         return this.parseHttpResponse(response);
      }
   }

   public JSONObject delete(String pathPrefix) throws MigboApiUtil.MigboApiException {
      if (SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.MIGBO_DISABLED) != 0) {
         log.warn("Migbo api call is not made because migbo is disabled now. type: DELETE, pathPrefix: " + pathPrefix);
         return this.createMigboDisabledResponse("delete", pathPrefix, (String)null);
      } else if (this.isValhallaApiPath(pathPrefix, "delete")) {
         return getInstanceForValhalla().delete(pathPrefix);
      } else {
         HttpClient httpclient = this.getHttpClient();
         String path = this.migboDatasvcPathPrefix + pathPrefix;
         HttpDelete httpdel = new HttpDelete(path);
         httpdel.addHeader("Content-Type", "application/vnd.mig33api-v1+json;charset=UTF-8");
         httpdel.addHeader("Connection", "close");
         HttpResponse response = null;

         String errMsg;
         try {
            log.info(String.format("Making DELETE migbo data svc api call to %s%s", this.hcHost.toString(), path));
            response = httpclient.execute(this.hcHost, httpdel);
            log.info(String.format("Received response from migbo data svc api call %s%s", this.hcHost.toString(), path));
         } catch (ClientProtocolException var8) {
            errMsg = String.format("Error in making migbo data svc api call to %s%s", this.hcHost.toString(), path);
            log.error(errMsg, var8);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         } catch (IOException var9) {
            errMsg = String.format("IOException in making migbo data svc api call to %s%s", this.hcHost.toString(), path);
            log.error(errMsg, var9);
            throw new MigboApiUtil.MigboApiException(-1, errMsg);
         }

         return this.parseHttpResponse(response);
      }
   }

   private JSONObject parseHttpResponse(HttpResponse response) throws MigboApiUtil.MigboApiException {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
         JSONObject error;
         try {
            String errMsg;
            try {
               String res = EntityUtils.toString(entity);

               String errMsg;
               try {
                  JSONObject root = new JSONObject(res);
                  if (!root.isNull("error")) {
                     error = root.getJSONObject("error");
                     int errno = error.getInt("errno");
                     String errmessage = error.getString("message");
                     String errMsg = String.format("Error returned by migbo data svc api call, api %s, %d:%s", this.hcHost.toString(), errno, errmessage);
                     log.error(errMsg);
                     throw new MigboApiUtil.MigboApiException(errno, errmessage);
                  }

                  if (root.isNull("data")) {
                     errMsg = String.format("Result returned by migbo data svc api call contains neither error nor data, api %s, %s", this.hcHost.toString(), res);
                     log.error(errMsg);
                     throw new MigboApiUtil.MigboApiException(-1, errMsg);
                  }

                  error = root;
               } catch (Exception var19) {
                  errMsg = String.format("Unable to parse migbo data svc api call result into JSON array, api %s, %s", this.hcHost.toString(), var19.getMessage());
                  log.error(errMsg, var19);
                  throw new MigboApiUtil.MigboApiException(-1, var19.getMessage());
               }
            } catch (ParseException var20) {
               errMsg = String.format("Unable to parse migbo data svc api call result, api %s", this.hcHost.toString());
               log.error(errMsg, var20);
               throw new MigboApiUtil.MigboApiException(-1, errMsg);
            } catch (IOException var21) {
               errMsg = String.format("Unable to parse migbo data svc api call result, api %s", this.hcHost.toString());
               log.error(errMsg, var21);
               throw new MigboApiUtil.MigboApiException(-1, errMsg);
            }
         } finally {
            if (entity != null) {
               try {
                  entity.consumeContent();
               } catch (IOException var18) {
                  log.warn(String.format("IOException caught while trying to clean up http connection resource: %s", var18.getMessage()));
               }
            }

         }

         return error;
      } else {
         log.error(String.format("null entity in making migbo data svc api call to %s", this.hcHost.toString()));
         throw new MigboApiUtil.MigboApiException(-1, "null entity");
      }
   }

   private String consumeHttpResponse(HttpResponse response) throws MigboApiUtil.MigboApiException {
      HttpEntity entity = response.getEntity();
      if (entity != null) {
         String var3;
         try {
            String errMsg;
            try {
               var3 = EntityUtils.toString(entity);
            } catch (ParseException var14) {
               errMsg = String.format("Unable to parse migbo data svc api call result, api %s", this.hcHost.toString());
               log.error(errMsg, var14);
               throw new MigboApiUtil.MigboApiException(-1, errMsg);
            } catch (IOException var15) {
               errMsg = String.format("Unable to parse migbo data svc api call result, api %s", this.hcHost.toString());
               log.error(errMsg, var15);
               throw new MigboApiUtil.MigboApiException(-1, errMsg);
            }
         } finally {
            if (entity != null) {
               try {
                  entity.consumeContent();
               } catch (IOException var13) {
                  log.warn(String.format("IOException caught while trying to clean up http connection resource: %s", var13.getMessage()));
               }
            }

         }

         return var3;
      } else {
         log.error(String.format("null entity in making migbo data svc api call to %s", this.hcHost.toString()));
         throw new MigboApiUtil.MigboApiException(-1, "null entity");
      }
   }

   public boolean postAndCheckOk(String pathPrefix, String postData) throws MigboApiUtil.MigboApiException {
      JSONObject root = this.post(pathPrefix, postData);

      try {
         return "ok".equalsIgnoreCase(root.getString("data"));
      } catch (JSONException var5) {
         log.error("JSONException caught while parsing " + root.toString());
         return false;
      }
   }

   private ConnectionKeepAliveStrategy getKeepAliveStrategy() {
      return new ConnectionKeepAliveStrategy() {
         public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            BasicHeaderElementIterator it = new BasicHeaderElementIterator(response.headerIterator("Keep-Alive"));

            while(true) {
               String param;
               String value;
               do {
                  do {
                     if (!it.hasNext()) {
                        return (long)(1000 * MigboApiUtil.this.keepAliveInSeconds);
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
      };
   }

   public static class MigboApiException extends Exception {
      private static final long serialVersionUID = 40782198099192882L;
      public int errno;

      public MigboApiException(int errno, String errMsg) {
         super(errMsg);
         this.errno = errno;
      }

      public boolean isLocalException() {
         return this.errno <= 100;
      }
   }
}
