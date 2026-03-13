package com.projectgoth.fusion.userevent;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.EventSystemPrxHelper;

public class CreateEventForUser {
   private static Communicator iceCommunicator = Util.initialize(new String[0]);
   private static EventSystemPrx eventSystemPrx;

   public static EventSystemPrx getEventSystem(String hostname) throws Exception {
      if (eventSystemPrx == null) {
         if (iceCommunicator == null) {
            throw new Exception("Ice communicator has not been initialized");
         }

         ObjectPrx base = iceCommunicator.stringToProxy("EventSystem: tcp -h " + hostname + " -p 21500 -t 5000");
         eventSystemPrx = EventSystemPrxHelper.checkedCast(base);
         if (eventSystemPrx == null) {
            throw new Exception("Invalid EventSystem proxy");
         }
      }

      return eventSystemPrx;
   }

   public static void main(String[] args) throws Exception {
      if (args.length < 2) {
         System.err.println("Usage: CreateEventForUser <event system hostname> <username>");
         System.exit(1);
      }

      String hostname = args[0];
      String username = args[1];

      try {
         EventSystemPrx eventSystemProxy = getEventSystem(hostname);
         System.out.println(eventSystemProxy);
         eventSystemProxy.madeGroupUserPost(username, 38, 7);
         System.out.println("done submitting event");
      } catch (Exception var4) {
         var4.printStackTrace();
      }

      iceCommunicator.shutdown();
      System.exit(0);
   }
}
