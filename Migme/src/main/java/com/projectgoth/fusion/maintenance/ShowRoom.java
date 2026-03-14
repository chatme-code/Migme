package com.projectgoth.fusion.maintenance;

import Ice.Communicator;
import Ice.ObjectPrx;
import Ice.Util;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;

public class ShowRoom {
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
      if (args.length < 2) {
         System.err.println("Usage: ShowRoom <registry hostname> <room name to inspect>");
         System.exit(1);
      }

      String hostname = args[0];
      String roomname = args[1];

      try {
         ChatRoomPrx roomProxy = getRegistry(hostname).findChatRoomObject("RC Stadium");
         System.out.println(roomProxy);
         ChatRoomDataIce roomData = roomProxy.getRoomData();
         System.out.println(roomData.name);
         System.out.println(roomData.description);
      } catch (ObjectNotFoundException var5) {
         System.err.println(roomname + " was not found in the registry");
      }

      iceCommunicator.shutdown();
      System.exit(0);
   }
}
