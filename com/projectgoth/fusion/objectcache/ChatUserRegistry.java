package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice.UserPrxHelper;
import java.util.Map;

public class ChatUserRegistry {
   private RegistryPrx registryPrx;

   public ChatUserRegistry(RegistryPrx registryPrx) {
      this.registryPrx = registryPrx;
   }

   private UserPrx[] getOneWayUserProxies(String[] userList) {
      UserPrx[] userProxies = this.registryPrx.findUserObjects(userList);
      if (userProxies != null && userProxies.length != 0) {
         for(int i = 0; i < userProxies.length; ++i) {
            userProxies[i] = UserPrxHelper.uncheckedCast(userProxies[i].ice_oneway());
            userProxies[i] = (UserPrx)userProxies[i].ice_connectionId("OneWayProxyGroup");
         }

         return userProxies;
      } else {
         return null;
      }
   }

   public void contactChangedStatusMessage(String[] broadcastListArray, String username, String statusMessage, long timeStamp) {
      UserPrx[] userProxies = this.getOneWayUserProxies(broadcastListArray);
      if (userProxies != null) {
         UserPrx[] arr$ = userProxies;
         int len$ = userProxies.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserPrx userPrx = arr$[i$];

            try {
               userPrx.contactChangedStatusMessageOneWay(username, statusMessage, timeStamp);
            } catch (Exception var12) {
            }
         }

      }
   }

   public void contactChangedDisplayPicture(String[] broadcastListArray, String username, String displayPicture, long timeStamp) {
      UserPrx[] userProxies = this.getOneWayUserProxies(broadcastListArray);
      if (userProxies != null) {
         UserPrx[] arr$ = userProxies;
         int len$ = userProxies.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserPrx userPrx = arr$[i$];

            try {
               userPrx.contactChangedDisplayPictureOneWay(username, displayPicture, timeStamp);
            } catch (Exception var12) {
            }
         }

      }
   }

   public void contactChangedPresence(String[] watchers, int imTypeValue, String username, int presenceValue) {
      UserPrx[] userProxies = this.getOneWayUserProxies(watchers);
      if (userProxies != null) {
         UserPrx[] arr$ = userProxies;
         int len$ = userProxies.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserPrx userPrx = arr$[i$];

            try {
               userPrx.contactChangedPresenceOneWay(imTypeValue, username, presenceValue);
            } catch (Exception var11) {
            }
         }

      }
   }

   public Map<String, UserPrx> findUserObjectsMap(String[] users) {
      return this.registryPrx.findUserObjectsMap(users);
   }
}
