package com.projectgoth.fusion.smsengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.SMSRouteData;
import com.projectgoth.fusion.data.SystemSMSData;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class SMSMessage {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(HTTPGateway.class));
   private Integer id;
   private Integer parentID;
   private String username;
   private SMSRouteData.TypeEnum type;
   private String source;
   private String destination;
   private Integer IDDCode;
   private String messageText;
   private boolean useSourceAsSourceAddress;

   public String getDestination() {
      return this.destination;
   }

   public Integer getID() {
      return this.id;
   }

   public Integer getParentID() {
      return this.parentID;
   }

   public Integer getIDDCode() {
      return this.IDDCode;
   }

   public String getMessageText() {
      return this.messageText;
   }

   public String getSource() {
      return this.source;
   }

   public SMSRouteData.TypeEnum getType() {
      return this.type;
   }

   public String getUsername() {
      return this.username;
   }

   public boolean useSourceAsSourceAddress() {
      return this.useSourceAsSourceAddress;
   }

   public long getKey() {
      int v1 = this.type == null ? 0 : this.type.value();
      int v2 = this.id == null ? 0 : this.id;
      return (long)(v1 + v2 * 10);
   }

   public String getShortDescription() {
      return this.type.toString() + " (" + this.id + ", " + this.destination + ")";
   }

   public static List<SMSMessage> parse(SystemSMSData systemSMS) {
      ArrayList<SMSMessage> list = new ArrayList();
      SMSMessage sms = new SMSMessage();
      sms.id = systemSMS.id;
      sms.parentID = systemSMS.id;
      sms.username = systemSMS.username;
      if (systemSMS.type != null) {
         switch(systemSMS.type) {
         case WAP_PUSH:
            sms.type = SMSRouteData.TypeEnum.SYSTEM_WAP_PUSH;
            break;
         case PREMIUM:
            sms.type = SMSRouteData.TypeEnum.SYSTEM_PREMIUM_SMS;
            break;
         case STANDARD:
            sms.type = SMSRouteData.TypeEnum.SYSTEM_SMS;
            break;
         default:
            sms.type = null;
         }
      }

      sms.source = systemSMS.source;
      sms.destination = systemSMS.destination;
      sms.IDDCode = systemSMS.IDDCode;
      sms.messageText = systemSMS.messageText;
      if (SystemSMSData.SubTypeEnum.USER_REFERRAL == systemSMS.subType) {
         sms.useSourceAsSourceAddress = true;
      }

      list.add(sms);
      return list;
   }

   public static List<SMSMessage> parse(MessageData message) {
      ArrayList<SMSMessage> list = new ArrayList();
      if (message.messageDestinations != null && message.messageDestinations.size() != 0) {
         Iterator i$ = message.messageDestinations.iterator();

         while(i$.hasNext()) {
            MessageDestinationData destination = (MessageDestinationData)i$.next();
            SMSMessage sms = new SMSMessage();
            sms.id = destination.id;
            sms.parentID = message.id;
            sms.username = message.username;
            sms.type = SMSRouteData.TypeEnum.USER_SMS;
            sms.source = message.source;
            sms.destination = destination.destination;
            sms.IDDCode = destination.IDDCode;
            sms.messageText = message.messageText;
            sms.useSourceAsSourceAddress = true;
            list.add(sms);
         }
      } else {
         log.warn("Invalid message " + message.id + " - No message destination(s)");
      }

      return list;
   }
}
