package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;

public class FusionPktGetUserProfile extends FusionRequest {
   public FusionPktGetUserProfile() {
      super((short)905);
   }

   public FusionPktGetUserProfile(short transactionId) {
      super((short)905, transactionId);
   }

   public FusionPktGetUserProfile(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new Exception("You are no longer logged in");
         } else {
            UserDataIce userDataIce = userPrx.getUserData();
            if (userDataIce == null) {
               throw new Exception("Unable to get user data from user proxy");
            } else {
               UserData userData = new UserData(userDataIce);
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               UserProfileData userProfileData = userEJB.getUserProfile(userData.username, userData.username, true);
               Credential[] credentials = connection.findAuthenticationService().getAllCredentials(userData.userID);
               return (new FusionPktUserProfile(this.transactionId, userData, userProfileData, credentials)).toArray();
            }
         }
      } catch (LocalException var8) {
         return (new FusionPktInternalServerError(this.transactionId, var8, "Failed to get user profile")).toArray();
      } catch (Exception var9) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get user profile - " + var9.getMessage())).toArray();
      }
   }
}
