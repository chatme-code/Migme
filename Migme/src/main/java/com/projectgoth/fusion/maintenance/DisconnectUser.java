package com.projectgoth.fusion.maintenance;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;

public class DisconnectUser {
   private static Communicator iceCommunicator = Util.initialize(new String[0]);
   private static RegistryPrx registryPrx;

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

   public static void main(String[] args) throws Exception {
      if (args.length < 3) {
         System.err.println("Usage: DisconnectUser <registry hostname> <username to inspect> <reason>");
         System.exit(1);
      }

      String hostname = args[0];
      String username = args[1];
      String reason = args[2];

      try {
         UserPrx userProxy = getRegistry(hostname).findUserObject(username);
         SessionPrx[] sessions = userProxy.getSessions();
         if (sessions == null || sessions.length < 1) {
            System.out.println("no sessions found for user [" + username + "]");
            return;
         }

         System.out.println("found " + sessions.length + " sessions:");
         SessionPrx[] arr$ = sessions;
         int len$ = sessions.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            SessionPrx session = arr$[i$];
            String sessionID = session.getSessionID();
            System.out.println("Disconnecting session [" + sessionID + "] ...");
         }

         userProxy.disconnect(reason);
      } catch (ObjectNotFoundException var11) {
         System.err.println(username + " was not found in the registry, no problem.");
      }

      iceCommunicator.shutdown();
      System.exit(0);
   }
}
