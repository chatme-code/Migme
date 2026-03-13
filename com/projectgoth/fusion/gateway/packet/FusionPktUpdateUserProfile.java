package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import javax.ejb.CreateException;

public class FusionPktUpdateUserProfile extends FusionRequest {
   public FusionPktUpdateUserProfile() {
      super((short)907);
   }

   public FusionPktUpdateUserProfile(short transactionId) {
      super((short)907, transactionId);
   }

   public FusionPktUpdateUserProfile(FusionPacket packet) {
      super(packet);
   }

   public String getDisplayName() {
      return this.getStringField((short)1);
   }

   public void setDisplayName(String displayName) {
      this.setField((short)1, displayName);
   }

   public String getFirstName() {
      return this.getStringField((short)2);
   }

   public void setFirstName(String firstName) {
      this.setField((short)2, firstName);
   }

   public String getLastName() {
      return this.getStringField((short)3);
   }

   public void setLastName(String lastName) {
      this.setField((short)3, lastName);
   }

   public String getCity() {
      return this.getStringField((short)4);
   }

   public void setCity(String city) {
      this.setField((short)4, city);
   }

   public String getState() {
      return this.getStringField((short)5);
   }

   public void setState(String state) {
      this.setField((short)5, state);
   }

   public Integer getCountryId() {
      return this.getIntField((short)6);
   }

   public void setCountryId(int countryId) {
      this.setField((short)6, countryId);
   }

   public Byte getUTCOffset() {
      return this.getByteField((short)7);
   }

   public void setUTCOffset(byte utcOffset) {
      this.setField((short)7, utcOffset);
   }

   public Long getDateOfBirth() {
      return this.getLongField((short)8);
   }

   public void setDateOfBirth(long dateOfBirth) {
      this.setField((short)8, dateOfBirth);
   }

   public Byte getGender() {
      return this.getByteField((short)9);
   }

   public void setGender(byte gender) {
      this.setField((short)9, gender);
   }

   public String getEmailAddress() {
      return this.getStringField((short)10);
   }

   public void setEmailAddress(String emailAddress) {
      this.setField((short)10, emailAddress);
   }

   public Byte getOnMailingList() {
      return this.getByteField((short)11);
   }

   public void setOnMailingList(byte onMailingList) {
      this.setField((short)11, onMailingList);
   }

   public String getMobilePhone() {
      return this.getStringField((short)12);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)12, mobilePhone);
   }

   public String getAwayMessage() {
      return this.getStringField((short)13);
   }

   public void setAwayMessage(String awayMessage) {
      this.setField((short)13, awayMessage);
   }

   public Byte getAutoRecharge() {
      return this.getByteField((short)14);
   }

   public void setAutoRecharge(byte autoRecharge) {
      this.setField((short)14, autoRecharge);
   }

   public String getMSNUsername() {
      return this.getStringField((short)15);
   }

   public void setMSNUsername(String msnUsername) {
      this.setField((short)15, msnUsername);
   }

   public String getMSNPassword() {
      return this.getStringField((short)16);
   }

   public void setMSNPassword(String msnPassword) {
      this.setField((short)16, msnPassword);
   }

   public Byte getMSNAutoLogin() {
      return this.getByteField((short)17);
   }

   public void setMSNAutoLogin(byte msnAutoLogin) {
      this.setField((short)17, msnAutoLogin);
   }

   public String getYahooUsername() {
      return this.getStringField((short)18);
   }

   public void setYahooUsername(String yahooUsername) {
      this.setField((short)18, yahooUsername);
   }

   public String getYahooPassword() {
      return this.getStringField((short)19);
   }

   public void setYahooPassword(String yahooPassword) {
      this.setField((short)19, yahooPassword);
   }

   public Byte getYahooAutoLogin() {
      return this.getByteField((short)20);
   }

   public void setYahooAutoLogin(byte yahooAutoLogin) {
      this.setField((short)20, yahooAutoLogin);
   }

   public String getAIMUsername() {
      return this.getStringField((short)21);
   }

   public void setAIMUsername(String aimUsername) {
      this.setField((short)21, aimUsername);
   }

   public String getAIMPassword() {
      return this.getStringField((short)22);
   }

   public void setAIMPassword(String aimPassword) {
      this.setField((short)22, aimPassword);
   }

   public Byte getAIMAutoLogin() {
      return this.getByteField((short)23);
   }

   public void setAIMAutoLogin(byte aimAutoLogin) {
      this.setField((short)23, aimAutoLogin);
   }

   public String getGTalkUsername() {
      return this.getStringField((short)24);
   }

   public void setGTalkUsername(String gtalkUsername) {
      this.setField((short)24, gtalkUsername);
   }

   public String getGTalkPassword() {
      return this.getStringField((short)25);
   }

   public void setGTalkPassword(String gtalkPassword) {
      this.setField((short)25, gtalkPassword);
   }

   public Byte getGTalkAutoLogin() {
      return this.getByteField((short)26);
   }

   public void setGTalkAutoLogin(byte gtalkAutoLogin) {
      this.setField((short)26, gtalkAutoLogin);
   }

   public String getFacebookUsername() {
      return this.getStringField((short)27);
   }

   public void setFacebookUsername(String facebookUsername) {
      this.setField((short)27, facebookUsername);
   }

   public String getFacebookPassword() {
      return this.getStringField((short)28);
   }

   public void setFacebookPassword(String facebookPassword) {
      this.setField((short)28, facebookPassword);
   }

   public Byte getFacebookAutoLogin() {
      return this.getByteField((short)29);
   }

   public void setFacebookAutoLogin(byte facebookAutoLogin) {
      this.setField((short)29, facebookAutoLogin);
   }

   public Byte getIMTypeToRemove() {
      return this.getByteField((short)30);
   }

   public void setIMTypeToRemove(byte imTypeToRemove) {
      this.setField((short)30, imTypeToRemove);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         UserPrx userPrx = connection.getUserPrx();
         UserDataIce userDataIce = userPrx.getUserData();
         if (userDataIce == null) {
            throw new Exception("Unable to get user data from user proxy");
         } else {
            UserData userData = new UserData(userDataIce);
            if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.EDIT_PROFILE, userData) && SystemProperty.getBool("UpdateProfileDisabledForUnauthenticatedUsers", false)) {
               throw new Exception("You must authenticate your account before updating your profile.");
            } else {
               User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               String strVal = this.getDisplayName();
               if (strVal != null) {
                  userData.displayName = strVal;
               }

               Integer intVal = this.getCountryId();
               if (intVal != null) {
                  userData.countryID = intVal;
               }

               strVal = this.getEmailAddress();
               if (strVal != null) {
                  userData.emailAddress = strVal;
               }

               strVal = this.getMobilePhone();
               if (strVal != null) {
                  userData.mobilePhone = strVal;
               }

               Byte byteVal = this.getOnMailingList();
               if (byteVal != null) {
                  userData.onMailingList = byteVal == 1;
               }

               byteVal = this.getUTCOffset();
               if (byteVal != null) {
                  userData.UTCOffset = byteVal.doubleValue();
               }

               userEJB.updateUserDetail(userData);
               if (this.getMSNUsername() != null && this.getMSNPassword() != null) {
                  userEJB.updateOtherIMDetail(userData.username, ImType.MSN, this.getMSNUsername(), this.getMSNPassword());
               }

               if (this.getYahooUsername() != null && this.getYahooPassword() != null) {
                  userEJB.updateOtherIMDetail(userData.username, ImType.YAHOO, this.getYahooUsername(), this.getYahooPassword());
               }

               if (this.getAIMUsername() != null && this.getAIMPassword() != null) {
                  userEJB.updateOtherIMDetail(userData.username, ImType.AIM, this.getAIMUsername(), this.getAIMPassword());
               }

               if (this.getGTalkUsername() != null && this.getGTalkPassword() != null) {
                  userEJB.updateOtherIMDetail(userData.username, ImType.GTALK, this.getGTalkUsername(), this.getGTalkPassword());
               }

               if (this.getFacebookUsername() != null && this.getFacebookPassword() != null) {
                  userEJB.updateOtherIMDetail(userData.username, ImType.FACEBOOK, this.getFacebookUsername(), this.getFacebookPassword());
               }

               if (this.getIMTypeToRemove() != null) {
                  userEJB.updateOtherIMDetail(userData.username, ImType.fromValue(this.getIMTypeToRemove()), "", "");
               }

               return (new FusionPktOk(this.transactionId)).toArray();
            }
         }
      } catch (CreateException var9) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to update user profile - Failed to create UserEJB")).toArray();
      } catch (RemoteException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to update user profile - " + RMIExceptionHelper.getRootMessage(var10))).toArray();
      } catch (Exception var11) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to update user profile - " + var11.getMessage())).toArray();
      }
   }
}
