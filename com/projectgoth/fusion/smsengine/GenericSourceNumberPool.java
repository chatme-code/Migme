package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class GenericSourceNumberPool {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GenericSourceNumberPool.class));
   private static final int cacheTime = 300000;
   private static Map<String, List<String>> sourceNumberMap = new ConcurrentHashMap();
   private static SecureRandom random = new SecureRandom();
   private static Map<String, Long> lastUpdatedMap = new ConcurrentHashMap();

   private static boolean validateMDNNumbers(List<String> numbers) {
      Iterator i$ = numbers.iterator();

      String number;
      do {
         if (!i$.hasNext()) {
            return true;
         }

         number = (String)i$.next();
      } while(number.matches("[0-9]+$"));

      return false;
   }

   private static void loadSourceNumbers(String gatewayName) throws IOException, Exception {
      Properties properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "resource/" + "smsengine/" + gatewayName.toLowerCase() + ".properties";
      new LinkedList();

      LinkedList numbers;
      try {
         InputStream inputStream = new FileInputStream(new File(propertiesLocation));
         properties.load(inputStream);
         numbers = new LinkedList(properties.values());
         if (!validateMDNNumbers(numbers)) {
            throw new Exception("property file " + gatewayName.toLowerCase() + ".properties, contains an incorrectly formatted MDN number");
         }

         log.info("mdn number pool for " + gatewayName + " successfully loaded with " + numbers.size() + " entries");
      } catch (IOException var6) {
         log.error("unable to load " + gatewayName + ".properties file", var6);
         throw var6;
      }

      lastUpdatedMap.put(gatewayName, System.currentTimeMillis());
      sourceNumberMap.put(gatewayName, numbers);
   }

   private static List<String> get(String gatewayName) throws IOException, Exception {
      if (!lastUpdatedMap.containsKey(gatewayName) || System.currentTimeMillis() - (Long)lastUpdatedMap.get(gatewayName) > 300000L) {
         loadSourceNumbers(gatewayName);
      }

      return (List)sourceNumberMap.get(gatewayName);
   }

   public static String getSourceNumber(String gatewayName) throws Exception {
      List<String> pool = get(gatewayName);
      if (pool.isEmpty()) {
         throw new Exception("gateway source pool for " + gatewayName + " is empty");
      } else {
         return (String)pool.get(random.nextInt(pool.size()));
      }
   }

   public static void main(String[] args) {
      try {
         String mdn = getSourceNumber("iris");

         assert !mdn.equals(getSourceNumber("iris"));

         mdn = getSourceNumber("smarttelcom");

         assert !mdn.equals(getSourceNumber("smarttelcom"));

         int i = false;

         for(int i = 0; i < 10; ++i) {
            System.out.println(getSourceNumber("smarttelcom"));
            Thread.sleep(2000L);
         }
      } catch (Exception var3) {
         var3.printStackTrace();
      }

   }
}
