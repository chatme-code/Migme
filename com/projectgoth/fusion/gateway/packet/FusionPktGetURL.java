package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import org.apache.log4j.Logger;

public class FusionPktGetURL extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetURL.class));

   public FusionPktGetURL() {
      super((short)910);
   }

   public FusionPktGetURL(short transactionId) {
      super((short)910, transactionId);
   }

   public FusionPktGetURL(FusionPacket packet) {
      super(packet);
   }

   public Byte getURLType() {
      return this.getByteField((short)1);
   }

   public void setURLType(byte urlType) {
      this.setField((short)1, urlType);
   }

   public String getProfileName() {
      return this.getStringField((short)2);
   }

   public void setProfileName(String profileName) {
      this.setField((short)2, profileName);
   }

   public boolean sessionRequired() {
      return false;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Byte urlTypeVal = this.getURLType();
         if (null == urlTypeVal) {
            throw new Exception("URL type not specified");
         } else {
            URLType urlType = URLType.fromValue(urlTypeVal);
            if (null == urlType) {
               throw new Exception("Unsupported URL Type " + urlTypeVal);
            } else {
               String url;
               switch(urlType) {
               case USER_PROFILE:
                  url = SystemProperty.get(urlType.getSystemProperty());
                  String profileName = this.getProfileName();
                  if (null != profileName) {
                     url = SystemProperty.get("UserProfileURL") + profileName;
                  }
                  break;
               case MY_PROFILE:
               case MY_ACCOUNT:
                  url = SystemProperty.get(urlType.getSystemProperty());
                  url = url + connection.getUsername();
                  break;
               case MIGBO_DATASVC_API:
               case MIGBO_UPLOAD_API:
               case MIGBO_IMAGES_URL:
               case MIGBO_ALERTS:
                  url = SystemProperty.get(urlType.getSystemProperty());
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GuardsetEnabled(GuardCapabilityEnum.VALHALLA_TEST)))) {
                     try {
                        User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                        if (userEJB.isUserInMigboAccessList(connection.getUserID(), MigboAccessMemberTypeEnum.WHITELIST.value(), GuardCapabilityEnum.VALHALLA_TEST.value())) {
                           url = URLUtil.replaceUrlHost(url, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Migbo.VALHALLA_MIGBO_URL));
                        }
                     } catch (Exception var7) {
                        log.error("Failed to get valhalla url for urltype " + urlType + ", userid " + connection.getUserID(), var7);
                     }
                  }
                  break;
               default:
                  url = SystemProperty.get(urlType.getSystemProperty());
               }

               if (urlType.getIsViewSensitive()) {
                  url = URLUtil.replaceViewTypeToken(url, connection.getDeviceType());
               }

               FusionPktOk pkt = new FusionPktOk(this.transactionId);
               pkt.setServerResponse(url);
               return new FusionPacket[]{pkt};
            }
         }
      } catch (Exception var8) {
         FusionPktError pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, var8.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
