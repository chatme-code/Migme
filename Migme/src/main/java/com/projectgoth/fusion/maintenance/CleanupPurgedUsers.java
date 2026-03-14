package com.projectgoth.fusion.maintenance;

import Ice.Communicator;
import Ice.ObjectNotExistException;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrx;
import com.projectgoth.fusion.slice.ObjectCacheAdminPrxHelper;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CleanupPurgedUsers {
   private static Communicator iceCommunicator = Util.initialize(new String[0]);
   private static RegistryPrx registryPrx;
   private static Map<String, String> objectCacheHostnames = new HashMap();
   private static Map<String, ObjectCacheAdminPrx> objectCacheAdminProxies = new HashMap();

   public static RegistryPrx getRegistry(String hostname) throws Exception {
      if (registryPrx == null) {
         if (iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         ObjectPrx base = iceCommunicator.stringToProxy("Registry: tcp -h " + hostname + " -p 10000 -t 5000");
         registryPrx = RegistryPrxHelper.checkedCast(base);
         if (registryPrx == null) {
            throw new Exception("Invalid Registry proxy");
         }
      }

      return registryPrx;
   }

   public static ObjectCacheAdminPrx getObjectCacheAdmin(String hostname) throws Exception {
      if (objectCacheAdminProxies.get(hostname) == null) {
         if (iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         ObjectPrx base = iceCommunicator.stringToProxy("ObjectCacheAdmin: tcp -h " + hostname + " -p 9000 -t 5000");
         ObjectCacheAdminPrx proxy = ObjectCacheAdminPrxHelper.checkedCast(base);
         if (proxy == null) {
            throw new Exception("Invalid ObjectCacheAdmin proxy");
         }

         objectCacheAdminProxies.put(hostname, proxy);
      }

      return (ObjectCacheAdminPrx)objectCacheAdminProxies.get(hostname);
   }

   public static void main(String[] args) throws Exception {
      if (args.length < 2) {
         System.err.println("Usage: CleanupPurgedUsers <registry hostname> <username to inspect>");
         System.exit(1);
      }

      String hostname = args[0];
      String username = args[1];

      try {
         UserPrx userPrx = getRegistry(hostname).findUserObject(username);
         System.out.println("considering " + userPrx);

         try {
            userPrx.getBlockList();
         } catch (ObjectNotExistException var10) {
            System.out.println("partially purged user " + userPrx);
            String objectCacheHostname = null;
            Pattern pattern = Pattern.compile("(\\d\\d\\.\\d\\.\\d\\.\\d\\d)");
            Matcher matcher = pattern.matcher(userPrx.toString());
            if (matcher.find()) {
               objectCacheHostname = (String)objectCacheHostnames.get(matcher.group());
            }

            ObjectCacheAdminPrx objectCacheAdminPrx = getObjectCacheAdmin(objectCacheHostname);
            System.out.println("object cache admin proxy " + objectCacheAdminPrx);
            int load = objectCacheAdminPrx.getStats().numUserObjects;
            System.out.println("deregistering user...");
            getRegistry(hostname).deregisterUserObject(username, objectCacheHostname);
         }
      } catch (ObjectNotFoundException var11) {
         System.err.println(username + " was not found in the registry, no problem.");
      }

      iceCommunicator.shutdown();
      System.exit(0);
   }

   static {
      objectCacheHostnames.put("10.3.1.24", "OBJC01.SJC01.PROJECTGOTH.COM");
      objectCacheHostnames.put("10.3.1.25", "OBJC02.SJC01.PROJECTGOTH.COM");
      objectCacheHostnames.put("10.3.1.26", "OBJC03.SJC01.PROJECTGOTH.COM");
      objectCacheHostnames.put("10.3.1.27", "OBJC04.SJC01.PROJECTGOTH.COM");
      objectCacheHostnames.put("10.3.1.54", "OBJC05.SJC01.PROJECTGOTH.COM");
      objectCacheHostnames.put("10.3.1.55", "OBJC06.SJC01.PROJECTGOTH.COM");
      objectCacheHostnames.put("10.3.1.65", "OBJC07.SJC01.PROJECTGOTH.COM");
   }
}
