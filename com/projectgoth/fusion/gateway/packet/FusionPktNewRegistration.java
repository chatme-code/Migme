package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.DataUtils;
import com.projectgoth.fusion.common.PasswordUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.data.UserRegistrationContextData;
import com.projectgoth.fusion.data.ValidateCredentialResult;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.restapi.enums.RegistrationType;
import java.rmi.RemoteException;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktNewRegistration extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktNewRegistration.class));

   public FusionPktNewRegistration() {
      super((short)100);
   }

   public FusionPktNewRegistration(short transactionId) {
      super((short)100, transactionId);
   }

   public FusionPktNewRegistration(FusionPacket packet) {
      super(packet);
   }

   public Byte getServiceType() {
      return this.getByteField((short)1);
   }

   public void setServiceType(byte serviceType) {
      this.setField((short)1, serviceType);
   }

   public String getUsername() {
      String s = this.getStringField((short)2);
      return s == null ? null : s.trim().toLowerCase();
   }

   public void setUsername(String username) {
      this.setField((short)2, username);
   }

   public String getPassword() {
      return this.getStringField((short)3);
   }

   public void setPassword(String password) {
      this.setField((short)3, password);
   }

   public String getMobilePhone() {
      return this.getStringField((short)4);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)4, mobilePhone);
   }

   public String getDisplayName() {
      return this.getStringField((short)5);
   }

   public void setDisplayName(String displayName) {
      this.setField((short)5, displayName);
   }

   public String getFirstName() {
      return this.getStringField((short)6);
   }

   public void setFirstName(String firstName) {
      this.setField((short)6, firstName);
   }

   public String getLastName() {
      return this.getStringField((short)7);
   }

   public void setLastName(String lastName) {
      this.setField((short)7, lastName);
   }

   public String getDateOfBirth() {
      return this.getStringField((short)8);
   }

   public void setDateOfBirth(String dateOfBirth) {
      this.setField((short)8, dateOfBirth);
   }

   public String getEmailAddress() {
      return this.getStringField((short)9);
   }

   public void setEmailAddress(String emailAddress) {
      this.setField((short)9, emailAddress);
   }

   public Integer getStartupCardNumber() {
      return this.getIntField((short)10);
   }

   public void setStartupCardNumber(int startupCardNumber) {
      this.setField((short)10, startupCardNumber);
   }

   public Integer getStartupCardPIN() {
      return this.getIntField((short)11);
   }

   public void setStartupCardPIN(int startupCardPIN) {
      this.setField((short)11, startupCardPIN);
   }

   public Integer getGroupID() {
      return this.getIntField((short)12);
   }

   public void setGroupID(int groupID) {
      this.setField((short)12, groupID);
   }

   public String getUserAgent() {
      return this.getStringField((short)13);
   }

   public void setUserAgent(String userAgent) {
      this.setField((short)13, userAgent);
   }

   public String getMobileDevice() {
      return this.getStringField((short)14);
   }

   public void setMobileDevice(String mobileDevice) {
      this.setField((short)14, mobileDevice);
   }

   public String getIMEI() {
      return this.getStringField((short)15);
   }

   public void setIMEI(String imei) {
      this.setField((short)15, imei);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      return this.processRequest(connection, false);
   }

   public FusionPacket[] processRequest(ConnectionI connection, boolean bypassBlackList) {
      try {
         String offlineMessage = connection.getGateway().getOfflineMessage();
         if (offlineMessage != null && offlineMessage.length() > 0) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, offlineMessage)).toArray();
         }

         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         if (!bypassBlackList && (SystemProperty.getBool("MidletRegistrationDisabled", false) || userEJB.midletRegistrationDisabled(this.getUsername(), this.getPassword(), connection.getRemoteAddress()))) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "This feature is not available at the moment. Please go to m.mig.me to register. We apologize for the inconvenience.")).toArray();
         }

         Byte serviceType = this.getServiceType();
         if (serviceType == null) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Service Type not set in new registration packet")).toArray();
         }

         UserData userData = new UserData();
         userData.type = UserData.TypeEnum.fromValue(serviceType.intValue());
         userData.username = this.getUsername();
         userData.password = this.getPassword();
         userData.mobilePhone = this.getMobilePhone();
         userData.displayName = this.getDisplayName();
         userData.emailAddress = this.getEmailAddress();
         String mobileDevice = DataUtils.truncateMobileDevice(this.getMobileDevice(), true, String.format("new user registration '%s'", userData.username));
         userData.registrationDevice = mobileDevice;
         userData.registrationIPAddress = connection.getRemoteAddress();
         UserProfileData userProfileData = new UserProfileData();
         userProfileData.username = userData.username;
         userProfileData.firstName = this.getFirstName();
         userProfileData.lastName = this.getLastName();
         AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(connection);
         accountEntrySourceData.mobileDevice = mobileDevice;
         accountEntrySourceData.userAgent = DataUtils.truncateUserAgent(this.getUserAgent(), true, String.format("new user registration '%s'", userData.username));
         accountEntrySourceData.imei = this.getIMEI();
         ValidateCredentialResult validationResult = PasswordUtils.validatePassword(userData.username, userData.password);
         if (!validationResult.valid) {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, validationResult.reason)).toArray();
         }

         String dob = this.getDateOfBirth();
         if (dob != null) {
            try {
               Pattern p = Pattern.compile("(\\d{4}+)(\\d{2}+)(\\d{2}+)");
               Matcher m = p.matcher(dob);
               if (!m.find() || m.groupCount() != 3) {
                  return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invalid date of birth")).toArray();
               }

               Calendar calendar = Calendar.getInstance();
               calendar.set(Integer.parseInt(m.group(1)), Integer.parseInt(m.group(2)) - 1, Integer.parseInt(m.group(3)));
               userProfileData.dateOfBirth = calendar.getTime();
            } catch (Exception var16) {
               return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to parse date of birth")).toArray();
            }
         }

         userData = userEJB.createUser(userData, userProfileData, true, new UserRegistrationContextData((String)null, false, RegistrationType.MOBILE_REGISTRATION), accountEntrySourceData);
         Integer groupId = this.getGroupID();
         if (groupId != null) {
            try {
               Web webEJB = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
               webEJB.joinGroup(userData.username, groupId, 0, connection.getRemoteAddress(), connection.getSessionID(), connection.getMobileDevice(), connection.getUserAgent(), false, true, true, false, false, false);
            } catch (Exception var15) {
               log.warn("Unable to add " + userData.username + " to group " + groupId, var15);
            }
         }
      } catch (CreateException var17) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "EJB create exception - " + var17.getMessage())).toArray();
      } catch (RemoteException var18) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "New registration failed - " + RMIExceptionHelper.getRootMessage(var18))).toArray();
      }

      return (new FusionPktOk(this.transactionId, 13)).toArray();
   }
}
