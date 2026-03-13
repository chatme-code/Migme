package com.projectgoth.fusion.common;

import com.danga.MemCached.MemCachedClient;
import com.danga.MemCached.SockIOPool;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

public class MemCachedUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MemCachedUtils.class));
   private static final String PROPERTIES_FILENAME = "memcached.properties";
   public static final String NAMESPACE_SEPERATOR = "/";
   public static final String MEMCACHED_LOCK_PREFIX = "lock:";
   public static final int MEMCACHED_LOCK_TIME = 5000;
   private static Set<String> poolNames = new HashSet();
   private static String fusionNamespace = "F";
   private static Properties properties;
   private static final Map<MemCachedUtils.Instance, MemCachedClient> clients = new HashMap();

   private static void parsePoolNames() {
      String names = properties.getProperty("pools");
      StringTokenizer tokenizer = new StringTokenizer(names, ",");

      while(tokenizer.hasMoreTokens()) {
         poolNames.add(tokenizer.nextToken().trim());
      }

      log.info("memcached.properties has " + poolNames.size() + " indicated pool names");
   }

   private static MemCachedClient configureClient(String poolName) {
      log.info("Configuring memcached client for pool [" + poolName + "]");

      try {
         MemCachedClient memcachedClient = new MemCachedClient(poolName);
         memcachedClient.setCompressEnable(true);
         memcachedClient.setCompressThreshold(16384L);
         if (poolName.equals("common") || poolName.equals("rateLimit")) {
            memcachedClient.setPrimitiveAsString(true);
         }

         if (poolName.equals("common") || poolName.equals("rateLimit") || poolName.equals("surgeMail")) {
            memcachedClient.setSanitizeKeys(false);
         }

         return memcachedClient;
      } catch (Exception var3) {
         log.error("Failed to setup memcached client", var3);
         return null;
      }
   }

   private static String[] getServersForPool(String poolName) {
      int numServers = Integer.parseInt(properties.getProperty(poolName + ".numServers"));
      String[] servers = new String[numServers];

      for(int i = 1; i <= numServers; ++i) {
         servers[i - 1] = properties.getProperty(poolName + ".server" + i);
      }

      return servers;
   }

   private static SockIOPool configurePool(String poolName) {
      log.info("reloading properties");
      loadProperties();
      log.info("Creating pool [" + poolName + "]");
      SockIOPool pool = SockIOPool.getInstance(poolName);
      pool.setServers(getServersForPool(poolName));
      pool.setInitConn(Integer.parseInt(properties.getProperty("pool.initialConnections", "2")));
      pool.setMinConn(Integer.parseInt(properties.getProperty("pool.minConnections", "2")));
      pool.setMaxConn(Integer.parseInt(properties.getProperty("pool.maxConnections", "25")));
      pool.setMaxIdle((long)(1000 * Integer.parseInt(properties.getProperty("pool.maxIdleTime", "21600"))));
      pool.setMaintSleep((long)(1000 * Integer.parseInt(properties.getProperty("maintenanceThread.sleep", "30"))));
      pool.setNagle(false);
      pool.setSocketTO(1000 * Integer.parseInt(properties.getProperty("socket.readTimeout", "3")));
      pool.setSocketConnectTO(1000 * Integer.parseInt(properties.getProperty("socket.connectTimeout", "3")));
      pool.setHashingAlg(0);
      if (poolName.equals("common") || poolName.equals("surgeMail") || poolName.equals("rateLimit")) {
         pool.setHashingAlg(2);
      }

      pool.setFailover(StringUtil.toBooleanOrDefault(properties.getProperty("pool.failover"), false));
      pool.initialize();
      return pool;
   }

   public static MemCachedClient getMemCachedClient(MemCachedUtils.Instance pool) {
      synchronized(pool) {
         MemCachedClient client = null;

         try {
            client = (MemCachedClient)clients.get(pool);
            if (client != null) {
               MemCachedClient var10000 = client;
               return var10000;
            }

            log.info("configuring client [" + pool.toString() + "]");
            configurePool(pool.toString());
            client = configureClient(pool.toString());
            clients.put(pool, client);
         } catch (Exception var5) {
            log.fatal("Unable to get client:" + pool + " from map");
         }

         return client;
      }
   }

   public static String getFusionNamespace() {
      return fusionNamespace;
   }

   public static String getNamespace(String prefix) {
      return getFusionNamespace() + "/" + prefix + "/";
   }

   public static String getCacheKeyInNamespace(String prefix, String key) {
      return getNamespace(prefix) + key;
   }

   public static boolean getLock(MemCachedClient memcache, String namespace, String key, int retries) {
      int count = 0;
      boolean gotLock = false;

      while(!(gotLock = memcache.add(getCacheKeyInNamespace(namespace, "lock:" + key), "locked", new Date(System.currentTimeMillis() + 5000L))) && count < retries) {
         try {
            ++count;
            Thread.sleep(1000L);
         } catch (InterruptedException var7) {
         }
      }

      return gotLock;
   }

   public static boolean releaseLock(MemCachedClient memcache, String namespace, String key) {
      return memcache.delete(getCacheKeyInNamespace(namespace, "lock:" + key));
   }

   private static boolean loadProperties() {
      properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "memcached.properties";

      try {
         InputStream inputStream = new FileInputStream(new File(propertiesLocation));
         properties.load(inputStream);
         if (properties.size() > 0) {
            return true;
         }
      } catch (Exception var2) {
         log.fatal("Failed to load memcached.properties, it should be at [" + propertiesLocation + "], aborting.", var2);
      }

      return false;
   }

   public static void main(String[] args) {
      MemCachedClient client1 = getMemCachedClient(MemCachedUtils.Instance.contactList);
      MemCachedClient client2 = getMemCachedClient(MemCachedUtils.Instance.broadcastList);

      assert client1 != client2;

      System.out.println("client1:");
      System.out.println(client1.stats());
      System.out.println("client2:");
      System.out.println(client2.stats());
      client1.set("key1", "value");
      client2.set("key1", "othervalue");
      System.out.println(client1.get("key1"));
      System.out.println(client2.get("key1"));

      assert !client1.get("key1").equals(client2.get("key2"));

      System.out.println("exited normally");
   }

   static {
      if (properties != null && properties.containsKey("disable")) {
         log.warn("MEMCACHED DISABLED BY 'disable' KEY IN " + ConfigUtils.getConfigDirectory() + "memcached.properties");
      }

      if (loadProperties() && !properties.containsKey("disable")) {
         fusionNamespace = properties.getProperty("fusionNamespace", "F");
         parsePoolNames();
         Iterator i$ = poolNames.iterator();

         while(i$.hasNext()) {
            String poolName = (String)i$.next();
            clients.put(MemCachedUtils.Instance.valueOf(poolName), (Object)null);
         }

         log.info("configured " + clients.size() + " dummy memcached clients");
      }

   }

   public static enum Instance {
      contactList,
      broadcastList,
      profiles,
      userDisplayPictureAndStatus,
      captcha,
      common,
      rateLimit,
      bclPersisted,
      surgeMail,
      recentChatRooms,
      chatRoomSearch,
      authenticationService;
   }
}
