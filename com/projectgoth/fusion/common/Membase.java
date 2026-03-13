package com.projectgoth.fusion.common;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;
import org.apache.log4j.Logger;

public class Membase {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Membase.class));
   private static final String KEY_SEPERATOR = "/";
   private static final String URL_ENCODED_SPACE = "%20";
   private static final String PROPERTIES_FILENAME = "membase.properties";
   private static Properties properties;
   private static MemCachedClient membaseClient = new MemCachedClient();

   private static boolean loadProperties() {
      properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "membase.properties";

      try {
         InputStream inputStream = new FileInputStream(new File(propertiesLocation));
         properties.load(inputStream);
         if (properties.size() > 0) {
            return true;
         }
      } catch (Exception var2) {
         log.fatal("Failed to load membase.properties, it should be at [" + propertiesLocation + "], aborting.", var2);
      }

      return false;
   }

   public static boolean add(Membase.KeySpace keySpace, String key, Object value) {
      return keySpace.expires() ? membaseClient.add(keySpace.getFullKey(key), value, keySpace.getExpiryDate()) : membaseClient.add(keySpace.getFullKey(key), value);
   }

   public static boolean set(Membase.KeySpace keySpace, String key, Object value) {
      return keySpace.expires() ? membaseClient.set(keySpace.getFullKey(key), value, keySpace.getExpiryDate()) : membaseClient.set(keySpace.getFullKey(key), value);
   }

   public static boolean delete(Membase.KeySpace keySpace, String key) {
      return membaseClient.delete(keySpace.getFullKey(key));
   }

   public static long incr(Membase.KeySpace keySpace, String key) {
      return membaseClient.incr(keySpace.getFullKey(key), 1L);
   }

   public static long addOrIncr(Membase.KeySpace keySpace, String key) {
      return membaseClient.addOrIncr(keySpace.getFullKey(key), 1L);
   }

   public static Object get(Membase.KeySpace keySpace, String key) {
      return membaseClient.get(keySpace.getFullKey(key));
   }

   public static long getCounter(Membase.KeySpace keySpace, String key) {
      return membaseClient.getCounter(keySpace.getFullKey(key));
   }

   public static String getString(Membase.KeySpace keySpace, String key) {
      Object obj = get(keySpace, key);
      return obj == null ? null : obj.toString();
   }

   public static Integer getInt(Membase.KeySpace keySpace, String key) {
      Object obj = get(keySpace, key);
      if (obj != null && !(obj instanceof Integer)) {
         try {
            return Integer.valueOf(obj.toString());
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         return (Integer)obj;
      }
   }

   public static Long getLong(Membase.KeySpace keySpace, String key) {
      Object obj = get(keySpace, key);
      if (obj != null && !(obj instanceof Long)) {
         try {
            return Long.valueOf(obj.toString());
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         return (Long)obj;
      }
   }

   public static Double getDouble(Membase.KeySpace keySpace, String key) {
      Object obj = get(keySpace, key);
      if (obj != null && !(obj instanceof Double)) {
         try {
            return Double.valueOf(obj.toString());
         } catch (NumberFormatException var4) {
            return null;
         }
      } else {
         return (Double)obj;
      }
   }

   static {
      log.info("Initializing Membase connection...");
      if (loadProperties()) {
         try {
            int numServers = Integer.parseInt(properties.getProperty("numServers"));
            String[] servers = new String[numServers];

            for(int i = 1; i <= numServers; ++i) {
               servers[i - 1] = properties.getProperty("server" + i);
               log.info("Adding Membase server " + servers[i - 1]);
            }

            SockIOPool pool = SockIOPool.getInstance();
            pool.setServers(servers);
            pool.setInitConn(Integer.parseInt(properties.getProperty("pool.initialConnections", "2")));
            pool.setMinConn(Integer.parseInt(properties.getProperty("pool.minConnections", "2")));
            pool.setMaxConn(Integer.parseInt(properties.getProperty("pool.maxConnections", "25")));
            pool.setMaxIdle((long)(1000 * Integer.parseInt(properties.getProperty("pool.maxIdleTime", Integer.toString(21600)))));
            pool.setMaintSleep((long)Integer.parseInt(properties.getProperty("maintenanceThread.sleep", "30")));
            pool.setNagle(false);
            pool.setSocketTO(1000 * Integer.parseInt(properties.getProperty("socket.readTimeout", "3")));
            pool.setSocketConnectTO(1000 * Integer.parseInt(properties.getProperty("socket.connectTimeout", "3")));
            pool.setHashingAlg(2);
            membaseClient.setCompressEnable(true);
            membaseClient.setCompressThreshold(16384L);
            membaseClient.setPrimitiveAsString(true);
            membaseClient.setSanitizeKeys(false);
            pool.initialize();
         } catch (Exception var3) {
            log.fatal("Unable to initialize Membase", var3);
         }
      }

   }

   public static enum KeySpace {
      RECENT_CHATROOM_LIST("RCL", 180L, Membase.TimeUnit.DAYS);

      private String name;
      private long cacheTime = 0L;

      private KeySpace(String name) {
         this.name = name;
      }

      private KeySpace(String name, long cacheTime, Membase.TimeUnit cacheTimeUnit) {
         this.name = name;
         if (cacheTime > 0L) {
            switch(cacheTimeUnit) {
            case MILLISECONDS:
               this.cacheTime = cacheTime;
               break;
            case SECONDS:
               this.cacheTime = cacheTime * 1000L;
               break;
            case MINUTES:
               this.cacheTime = cacheTime * 60000L;
               break;
            case HOURS:
               this.cacheTime = cacheTime * 3600000L;
               break;
            case DAYS:
               this.cacheTime = cacheTime * 86400000L;
            }

         }
      }

      public String getFullKey(String key) {
         return this.name + "/" + key.replaceAll(" ", "%20");
      }

      public boolean expires() {
         return this.cacheTime > 0L;
      }

      public Date getExpiryDate() {
         return new Date(System.currentTimeMillis() + this.cacheTime);
      }
   }

   private static enum TimeUnit {
      MILLISECONDS,
      SECONDS,
      MINUTES,
      HOURS,
      DAYS;
   }
}
