package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.CaptchaFallbackPagelet;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktInviteFriend extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktInviteFriend.class));

   public FusionPktInviteFriend() {
      super((short)900);
   }

   public FusionPktInviteFriend(short transactionId) {
      super((short)900, transactionId);
   }

   public FusionPktInviteFriend(FusionPacket packet) {
      super(packet);
   }

   public String getMobilePhone() {
      return this.getStringField((short)1);
   }

   public void setMobilePhone(String mobilePhone) {
      this.setField((short)1, mobilePhone);
   }

   public String getDisplayName() {
      return this.getStringField((short)2);
   }

   public void setDisplayName(String displayName) {
      this.setField((short)2, displayName);
   }

   public Integer getGroupID() {
      return this.getIntField((short)3);
   }

   public void setGroupID(int groupID) {
      this.setField((short)3, groupID);
   }

   public boolean sessionRequired() {
      return true;
   }

   public Boolean isCaptchaRequired(ConnectionI connection) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INVITEFRIEND_CAPTCHA_ENABLED)) {
         try {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            double referralRatio = userEJB.getUserReferralSuccessRate(connection.getUsername(), false);
            return referralRatio >= 0.0D ? referralRatio < SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INVITEFRIEND_CAPTCHA_ODDS) : false;
         } catch (Exception var5) {
            log.error("Failed to check referral success ratio", var5);
            return true;
         }
      } else {
         return false;
      }
   }

   public CaptchaFallbackPagelet getCaptchaFallbackPagelet() {
      return new CaptchaFallbackPagelet(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INVITEFRIEND_CAPTCHAFALLBACK_URL), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INVITEFRIEND_CAPTCHAFALLBACK_MESSAGE));
   }

   public String getPacketUnsupportedMessage() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GatewaySettings.INVITEFRIEND_PAGELETFALLBACK_MESSAGE);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), new FloodControl.Action[]{FloodControl.Action.INVITE_FRIEND_PER_MINUTE, FloodControl.Action.INVITE_FRIEND_DAILY.setMaxHits(SystemProperty.getLong("InviteFriendDailyRateLimit", 100L))});
         String mobilePhone = this.getMobilePhone();
         if (StringUtil.isBlank(mobilePhone)) {
            throw new IllegalArgumentException("Blank mobile phone.");
         } else {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = userEJB.loadUserFromMobilePhone(mobilePhone);
            if (userData == null) {
               userEJB.inviteFriend(connection.getUsername(), this.getDisplayName(), mobilePhone, (Integer)null, (String)null, (String)null, (String)null, new AccountEntrySourceData(connection));
               connection.getSessionPrx().friendInvitedByPhoneNumber();
               return (new FusionPktOk(this.transactionId, 14)).toArray();
            } else {
               ContactData contactData = new ContactData();
               contactData.username = connection.getUsername();
               contactData.fusionUsername = userData.username;
               int showVisible = Math.min(4, userData.username.length() / 2);
               contactData.displayName = StringUtil.maskString(userData.username, showVisible, 'X');
               contactData.mobilePhone = mobilePhone;
               contactData.displayOnPhone = true;
               Contact contactEJB = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               contactData = contactEJB.addPendingFusionContact(connection.getUserID(), contactData);
               connection.getSessionPrx().friendInvitedByUsername();
               FusionPktContactOld contactPkt = new FusionPktContactOld(this.transactionId, contactData, connection);
               FusionPktOk okPkt = new FusionPktOk(this.transactionId);

               try {
                  MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                  String infoText = misEJB.getInfoText(42);
                  if (infoText != null) {
                     okPkt.setServerResponse(infoText.replaceAll("%u", contactData.displayName));
                  }
               } catch (Exception var19) {
               } finally {
                  if (okPkt.getServerResponse() == null) {
                     okPkt.setServerResponse("You have successfully added " + contactData.displayName + " to your contact list");
                  }

               }

               return new FusionPacket[]{contactPkt, okPkt};
            }
         }
      } catch (CreateException var21) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create EJB")).toArray();
      } catch (RemoteException var22) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invite friend failed- " + RMIExceptionHelper.getRootMessage(var22))).toArray();
      } catch (Exception var23) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invite friend failed- " + var23.getMessage())).toArray();
      }
   }
}
