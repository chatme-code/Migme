package com.projectgoth.fusion.search;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;
import org.apache.log4j.Logger;
import org.json.JSONObject;

public class ElasticSearch {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ElasticSearch.class));
   private static final String PROPERTIES_FILENAME = "elasticsearch.properties";
   private static Properties properties;
   private static String elasticSearchURL;

   public static JSONObject index(ElasticSearch.IndexType indexType, ElasticSearch.DocumentType documentType, int documentID, int version, JSONObject document) throws VersionConflictException, Exception {
      String path = indexType.toString() + '/' + documentType + '/' + documentID;
      if (version == -1) {
         path = path + "?op_type=create";
      } else {
         path = path + "?version=" + version;
      }

      String response = doHTTPRequest("PUT", path, document.toString());
      JSONObject responseJSON = new JSONObject(response);
      if (responseJSON.has("error")) {
         throw new Exception(responseJSON.getString("error"));
      } else {
         return responseJSON;
      }
   }

   public static JSONObject get(ElasticSearch.IndexType indexType, ElasticSearch.DocumentType documentType, int documentID) throws Exception {
      String path = indexType.toString() + '/' + documentType + '/' + documentID;
      String response = doHTTPRequest("GET", path, (String)null);
      if (response == null) {
         return null;
      } else {
         JSONObject jsonObject = new JSONObject(response);
         if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
         } else {
            return jsonObject;
         }
      }
   }

   public static JSONObject delete(ElasticSearch.IndexType indexType, ElasticSearch.DocumentType documentType, int documentID) throws Exception {
      String path = indexType.toString() + '/' + documentType + '/' + documentID;
      String response = doHTTPRequest("DELETE", path, (String)null);
      if (response == null) {
         return null;
      } else {
         JSONObject jsonObject = new JSONObject(response);
         if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
         } else {
            return jsonObject;
         }
      }
   }

   public static JSONObject putMapping(ElasticSearch.IndexType[] indexTypes, ElasticSearch.DocumentType documentType, JSONObject mapping) throws Exception {
      String indexes = null;
      if (indexTypes == null) {
         indexes = "_all";
      } else {
         for(int i = 0; i < indexTypes.length; ++i) {
            if (i > 0) {
               indexes = indexes + ",";
            }

            indexes = indexes + indexTypes[i];
         }
      }

      String path = indexes + '/' + documentType + "/_mapping";
      String response = doHTTPRequest("PUT", path, mapping.toString());
      if (response == null) {
         return null;
      } else {
         JSONObject jsonObject = new JSONObject(response);
         if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
         } else {
            return jsonObject;
         }
      }
   }

   public static JSONObject getMapping(ElasticSearch.IndexType[] indexTypes, ElasticSearch.DocumentType[] documentTypes) throws Exception {
      String indexes = null;
      if (indexTypes == null) {
         indexes = "_all";
      } else {
         for(int i = 0; i < indexTypes.length; ++i) {
            if (i > 0) {
               indexes = indexes + ",";
            }

            indexes = indexes + indexTypes[i];
         }
      }

      String documents = null;
      if (documentTypes != null) {
         for(int i = 0; i < documentTypes.length; ++i) {
            if (i > 0) {
               documents = documents + ",";
            }

            documents = documents + indexTypes[i];
         }
      }

      String path = indexes + '/';
      if (documentTypes != null) {
         path = path + documents + '/';
      }

      path = path + "_mapping";
      String response = doHTTPRequest("GET", path, (String)null);
      if (response == null) {
         return null;
      } else {
         JSONObject jsonObject = new JSONObject(response);
         if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
         } else {
            return jsonObject;
         }
      }
   }

   public static JSONObject search(ElasticSearch.IndexType[] indexTypes, ElasticSearch.DocumentType[] documentTypes, JSONObject query) throws Exception {
      String indexes = "";
      if (indexTypes == null) {
         indexes = "_all";
      } else {
         for(int i = 0; i < indexTypes.length; ++i) {
            if (i > 0) {
               indexes = indexes + ",";
            }

            indexes = indexes + indexTypes[i];
         }
      }

      String documents = "";
      if (documentTypes != null) {
         for(int i = 0; i < documentTypes.length; ++i) {
            if (i > 0) {
               documents = documents + ",";
            }

            documents = documents + documentTypes[i];
         }
      }

      String path = indexes + '/';
      if (documentTypes != null) {
         path = path + documents + '/';
      }

      path = path + "_search";
      String response = doHTTPRequest("GET", path, query.toString());
      if (response == null) {
         return null;
      } else {
         JSONObject jsonObject = new JSONObject(response);
         if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
         } else {
            return jsonObject;
         }
      }
   }

   public static JSONObject count(ElasticSearch.IndexType[] indexTypes, ElasticSearch.DocumentType[] documentTypes, JSONObject query) throws Exception {
      String indexes = null;
      if (indexTypes == null) {
         indexes = "_all";
      } else {
         for(int i = 0; i < indexTypes.length; ++i) {
            if (i > 0) {
               indexes = indexes + ",";
            }

            indexes = indexes + indexTypes[i];
         }
      }

      String documents = null;
      if (documentTypes != null) {
         for(int i = 0; i < documentTypes.length; ++i) {
            if (i > 0) {
               documents = documents + ",";
            }

            documents = documents + indexTypes[i];
         }
      }

      String path = indexes + '/';
      if (documentTypes != null) {
         path = path + documents + '/';
      }

      path = path + "_count";
      String response = doHTTPRequest("GET", path, query.toString());
      if (response == null) {
         return null;
      } else {
         JSONObject jsonObject = new JSONObject(response);
         if (jsonObject.has("error")) {
            throw new Exception(jsonObject.getString("error"));
         } else {
            return jsonObject;
         }
      }
   }

   public static JSONObject status() throws Exception {
      String response = doHTTPRequest("GET", "_status", (String)null);
      JSONObject jsonObject = new JSONObject(response);
      if (jsonObject.has("error")) {
         throw new Exception(jsonObject.getString("error"));
      } else {
         return jsonObject;
      }
   }

   public static JSONObject refresh(ElasticSearch.IndexType indexType) throws Exception {
      String response = doHTTPRequest("PUT", indexType.toString() + "/_refresh", (String)null);
      JSONObject jsonObject = new JSONObject(response);
      if (jsonObject.has("error")) {
         throw new Exception(jsonObject.getString("error"));
      } else {
         return jsonObject;
      }
   }

   private static synchronized void loadProperties() throws Exception {
      properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "elasticsearch.properties";
      log.info("Loading ElasticSearch configuration file " + propertiesLocation);

      try {
         InputStream inputStream = new FileInputStream(new File(propertiesLocation));
         properties.load(inputStream);
         elasticSearchURL = properties.getProperty("elasticSearchURL");
         if (elasticSearchURL == null) {
            throw new Exception("elasticSearchURL not specified");
         }

         if (!elasticSearchURL.endsWith("/")) {
            elasticSearchURL = elasticSearchURL + "/";
         }

         log.info("ElasticSearch URL: " + elasticSearchURL);
         log.info("ElasticSearch configuration successfully loaded");
      } catch (Exception var2) {
         log.error("Unable to load ElasticSearch configuration file: " + var2.getMessage());
      }

   }

   public static String doHTTPRequest(String method, String path, String body) throws VersionConflictException, Exception {
      String url = elasticSearchURL + path;
      HttpURLConnection httpConn = (HttpURLConnection)(new URL(url)).openConnection();
      httpConn.setUseCaches(false);
      httpConn.setRequestMethod(method);
      if (body != null && body.length() > 0) {
         httpConn.setDoOutput(true);
         httpConn.getOutputStream().write(body.getBytes("UTF-8"));
      }

      if (httpConn.getResponseCode() == 404) {
         return null;
      } else if (httpConn.getResponseCode() == 409) {
         throw new VersionConflictException();
      } else if (httpConn.getResponseCode() < 200 && httpConn.getResponseCode() >= 300) {
         throw new Exception("HTTP " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
      } else {
         BufferedReader reader = null;

         try {
            reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            StringBuilder response = new StringBuilder();

            String line;
            while((line = reader.readLine()) != null) {
               response.append(line);
            }

            String var8 = response.toString();
            return var8;
         } finally {
            try {
               if (reader != null) {
                  reader.close();
               }
            } catch (IOException var15) {
            }

         }
      }
   }

   static {
      try {
         loadProperties();
      } catch (Exception var1) {
         log.error(var1.getMessage());
      }

   }

   public static enum DocumentType {
      CHAT_ROOM("chat_room"),
      GROUP("group"),
      PROFILE("profile");

      private String value;

      private DocumentType(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }

   public static enum IndexType {
      CHAT_ROOMS("chat_rooms"),
      GROUPS("groups"),
      PROFILES("profiles");

      private String value;

      private IndexType(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }
}
