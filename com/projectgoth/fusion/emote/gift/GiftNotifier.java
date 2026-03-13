package com.projectgoth.fusion.emote.gift;

import Ice.Communicator;
import Ice.Util;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.ejb.ContentBean;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.eventqueue.Event;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.VirtualGiftSentEvent;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.VGReceivedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.VGSentTrigger;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.smsengine.SMSControl;
import java.io.File;
import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

public class GiftNotifier implements Callable<Boolean>, Serializable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GiftNotifier.class));
   private String senderUsername;
   private int virtualGiftReceivedID;
   private UserData recipientUserData;
   private VirtualGiftData gift;
   private boolean privateGift;
   private String message;
   private String chatroomName;
   private boolean isGiftShower;
   private boolean sendGiftShowerEvent;
   private Integer totalGiftShowerRecipients;
   private IcePrxFinder icePrxFinder;

   public GiftNotifier(String senderUsername, int virtualGiftReceivedID, UserData recipientUserData, VirtualGiftData gift, boolean privateGift, String message, String chatroomName, boolean isGiftShower, boolean sendGiftShowerEvent, Integer totalGiftShowerRecipients, IcePrxFinder icePrxFinder) {
      if (log.isDebugEnabled()) {
         log.debug("Constructing GiftNotifier: sender=" + senderUsername + " recipient=" + recipientUserData);
      }

      this.senderUsername = senderUsername;
      this.virtualGiftReceivedID = virtualGiftReceivedID;
      this.recipientUserData = recipientUserData;
      this.gift = gift;
      this.privateGift = privateGift;
      this.message = message;
      this.chatroomName = chatroomName;
      this.isGiftShower = isGiftShower;
      this.sendGiftShowerEvent = sendGiftShowerEvent;
      this.totalGiftShowerRecipients = totalGiftShowerRecipients;
      this.icePrxFinder = icePrxFinder;
   }

   public Boolean call() {
      try {
         if (log.isDebugEnabled()) {
            log.debug("GiftNotifier.call: sender=" + this.senderUsername + " recipient=" + this.recipientUserData);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.TRUE_GRID_ENABLED)) {
            Communicator comm = Util.initialize(new String[0]);
            this.icePrxFinder.relocate(comm);
         }

         this.onPurchaseVirtualGift();
         return true;
      } catch (Exception var2) {
         log.error("Exception trying to send notifications for virtual gift,  sender=" + this.senderUsername + " recipient=" + (this.recipientUserData != null ? this.recipientUserData.username : "null") + " message=" + this.message + " e=" + var2, var2);
         return false;
      }
   }

   private void onPurchaseVirtualGift() {
      UserData buyerUserData = null;

      User chatRoomPrx;
      try {
         chatRoomPrx = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         buyerUserData = chatRoomPrx.loadUser(this.senderUsername, false, false);
      } catch (Exception var10) {
         log.warn("Unable to load UserBean: " + var10.getMessage());
      }

      chatRoomPrx = null;
      EventSystemPrx eventSystem = null;
      if (buyerUserData != null) {
         List<Event> eventList = new ArrayList();
         if (!this.isGiftShower && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.GIFT_EVENT_ENABLED)) {
            try {
               if (eventSystem == null && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
                  eventSystem = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VIRTUALGIFT_ONEWAY_EVENT_TRIGGER) ? this.icePrxFinder.getOnewayEventSystemProxy() : this.icePrxFinder.getEventSystemProxy();
               }

               if (!this.privateGift) {
                  if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
                     eventSystem.virtualGift(this.senderUsername, this.recipientUserData.username, this.gift.getName(), this.virtualGiftReceivedID);
                  }

                  eventList.add(new VirtualGiftSentEvent(this.senderUsername, this.recipientUserData.username, this.gift.getName(), this.virtualGiftReceivedID));
               }
            } catch (Exception var9) {
               log.error("Failed to log virtual gift event for user [" + this.senderUsername + "]", var9);
            }
         }

         new HashMap();

         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.GIFT_ALERT_OR_SMS_ENABLED)) {
               UserPrx userPrx = this.icePrxFinder.findUserPrx(this.recipientUserData.username);
               if (userPrx != null) {
                  this.notifyReceiverViaAlert(userPrx);
               } else {
                  this.notifyReceiverViaSMS();
               }
            }

            this.notifyRecipientViaUNS();
            if (!this.privateGift && this.chatroomName != null) {
               this.sendMessageBackToSender();
            }

            this.notifyRewardSystem(buyerUserData);

            try {
               Leaderboard.sendVirtualGift(this.senderUsername, this.recipientUserData.username);
            } catch (Exception var7) {
               log.error("Error in updating GiftSent and GiftReceived leaderboard.");
            }
         } catch (Exception var8) {
            log.error("Unable to notify recipient of a virtual gift [" + this.senderUsername + ", " + this.recipientUserData.username + ", " + this.gift.getName() + "]", var8);
         }

         if (eventList.size() != 0) {
            EventQueue.enqueueMultipleEvents(eventList);
         }

         if (this.isGiftShower && this.sendGiftShowerEvent && !this.privateGift) {
            this.handleGiftShower();
         }
      }

   }

   private void notifyReceiverViaAlert(UserPrx userPrx) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("GiftNotifier.notifyRecipientViaAlert: sender=" + this.senderUsername + " recipient=" + this.recipientUserData);
      }

      Map<String, String> parameters = new HashMap();
      MIS misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
      String alertMessage = misBean.getInfoText(41);
      if (alertMessage != null) {
         if (this.privateGift) {
            alertMessage = alertMessage.replaceAll("%private%", "private ");
         } else {
            alertMessage = alertMessage.replaceAll("%private%", "");
         }

         alertMessage = alertMessage.replaceAll("%senderusername%", this.senderUsername);
         parameters.put("alertMessage", alertMessage);
         parameters.put("alertURL", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.VIRTUAL_GIFT_RECEIVED_URL) + this.virtualGiftReceivedID);
         userPrx.putServerQuestion(alertMessage, SystemProperty.get("VirtualGiftReceivedURL") + this.virtualGiftReceivedID);
      }

   }

   private void notifyReceiverViaSMS() throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("GiftNotifier.notifyRecipientViaSMS: sender=" + this.senderUsername + " recipient=" + this.recipientUserData);
      }

      if (this.gift.getPrice() > 0.0D && this.recipientUserData.mobilePhone != null && this.recipientUserData.mobileVerified) {
         boolean sendSMS = SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.VIRTUAL_GIFT_NOTIFICATION, this.recipientUserData.username) && ContentBean.decideSendSMS(this.recipientUserData.username);
         if (sendSMS) {
            try {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.username = this.recipientUserData.username;
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.VIRTUAL_GIFT_NOTIFICATION;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = this.recipientUserData.mobilePhone;
               if (this.privateGift) {
                  systemSMSData.messageText = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.VIRTUAL_GIFT_NOTIFICATION_SMS).replaceAll("%private%", "private ");
               } else {
                  systemSMSData.messageText = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.VIRTUAL_GIFT_NOTIFICATION_SMS).replaceAll("%private%", "");
               }

               systemSMSData.messageText = systemSMSData.messageText.replaceAll("%senderusername%", this.senderUsername);
               Message messageBean = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               messageBean.sendSystemSMS(systemSMSData, new AccountEntrySourceData(ContentBean.class));
               log.info("Sending SMS to recipient of virtual gift [" + this.senderUsername + ", " + this.recipientUserData.username + ", " + this.gift.getName() + "]");
            } catch (Exception var4) {
               log.error("Unable to SMS to recipient of virtual gift [" + this.senderUsername + ", " + this.recipientUserData.username + ", " + this.gift.getName() + "]", var4);
            }
         } else if (log.isDebugEnabled()) {
            log.debug("NOT Sending SMS to recipient of virtual gift [" + this.senderUsername + ", " + this.recipientUserData.username + ", " + this.gift.getName() + "]");
         }
      }

   }

   private void notifyRecipientViaUNS() {
      if (log.isDebugEnabled()) {
         log.debug("GiftNotifier.notifyRecipientViaUNS: sender=" + this.senderUsername + " recipient=" + this.recipientUserData);
      }

      UserNotificationServicePrx unsProxy = null;

      try {
         if (unsProxy == null) {
            unsProxy = this.icePrxFinder.getUserNotificationServiceProxy();
         }

         if (unsProxy != null) {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            int senderUserId = userEJB.getUserID(this.senderUsername, (Connection)null);
            Map<String, String> parameters = new HashMap();
            parameters.put("virtualGiftId", this.gift.getId().toString());
            parameters.put("senderUsername", this.senderUsername);
            parameters.put("senderUserId", Integer.toString(senderUserId));
            parameters.put("virtualGiftReceivedId", Integer.toString(this.virtualGiftReceivedID));
            parameters.put("private", this.privateGift ? "1" : "0");
            parameters.put("giftName", this.gift.getName());
            parameters.put("location12x12GIF", this.gift.getLocation12x12GIF());
            parameters.put("location12x12PNG", this.gift.getLocation12x12PNG());
            parameters.put("location14x14GIF", this.gift.getLocation14x14GIF());
            parameters.put("location14x14PNG", this.gift.getLocation14x14PNG());
            parameters.put("location16x16GIF", this.gift.getLocation16x16GIF());
            parameters.put("location16x16PNG", this.gift.getLocation16x16PNG());
            parameters.put("location64x64PNG", this.gift.getLocation64x64PNG());
            String key = this.gift.getId() + "/" + System.currentTimeMillis();
            unsProxy.notifyFusionUser(new com.projectgoth.fusion.slice.Message(key, this.recipientUserData.userID, this.recipientUserData.username, Enums.NotificationTypeEnum.VIRTUALGIFT_ALERT.getType(), System.currentTimeMillis(), parameters));
         }
      } catch (Exception var6) {
         log.error("Failed to push virtual gift notification for user [" + this.recipientUserData.username + "]", var6);
      }

   }

   private void sendMessageBackToSender() {
      try {
         if (log.isDebugEnabled()) {
            log.debug("GiftNotifier.sendMessageBackToSender: sender=" + this.senderUsername + " recipient=" + this.recipientUserData);
         }

         ChatRoomPrx chatRoomPrx = null;
         if (chatRoomPrx == null) {
            chatRoomPrx = this.icePrxFinder.findChatRoomPrx(this.chatroomName);
         }

         if (chatRoomPrx != null) {
            String msg = "<< " + this.senderUsername + " gives ";
            if (StringUtil.startsWithaVowel(this.gift.getName())) {
               msg = msg + "an ";
            } else {
               msg = msg + "a ";
            }

            msg = msg + this.gift.getName() + " " + this.gift.getHotKey() + " to " + this.recipientUserData.username + "! >>";
            String[] emoticonKeys = new String[]{this.gift.getHotKey()};
            chatRoomPrx.putSystemMessage(msg, emoticonKeys);
         }
      } catch (Exception var4) {
         log.error("Unable to send virtual gift notification to chatroom [" + this.senderUsername + ", " + this.recipientUserData.username + ", " + this.gift.getName() + ", " + this.chatroomName + "]", var4);
      }

   }

   private void notifyRewardSystem(UserData buyerUserData) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("GiftNotifier.notifyRewardSystem: sender=" + this.senderUsername + " recipient=" + this.recipientUserData);
         }

         VGSentTrigger vgSentTrigger = new VGSentTrigger(buyerUserData, this.recipientUserData);
         vgSentTrigger.amountDelta = this.gift.getRoundedPrice();
         vgSentTrigger.quantityDelta = 1;
         vgSentTrigger.currency = this.gift.getCurrency();
         vgSentTrigger.virtualGiftID = this.gift.getId();
         if (log.isDebugEnabled()) {
            log.debug("Sending VGSentTrigger[" + vgSentTrigger + "]");
         }

         RewardCentre.getInstance().sendTrigger(vgSentTrigger);
         VGReceivedTrigger vgReceivedTrigger = new VGReceivedTrigger(this.recipientUserData, this.virtualGiftReceivedID);
         vgReceivedTrigger.amountDelta = this.gift.getRoundedPrice();
         vgReceivedTrigger.quantityDelta = 1;
         vgReceivedTrigger.currency = this.gift.getCurrency();
         vgReceivedTrigger.virtualGiftID = this.gift.getId();
         vgReceivedTrigger.senderUserData = buyerUserData;
         vgReceivedTrigger.virtualGiftName = this.gift.getName();
         vgReceivedTrigger.message = StringUtil.truncateWithEllipsis(this.message, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_MESSAGE_LENGTH));
         File file = new File(this.gift.getImageLocation(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
         vgReceivedTrigger.image = file != null ? file.getName() : null;
         if (log.isDebugEnabled()) {
            log.debug("Sending VGReceivedTrigger[" + vgReceivedTrigger + "]");
         }

         RewardCentre.getInstance().sendTrigger(vgReceivedTrigger);
      } catch (Exception var5) {
         log.warn("Unable to notify reward system", var5);
      }

   }

   private void handleGiftShower() {
      try {
         if (log.isDebugEnabled()) {
            log.debug("GiftNotifier.handleGiftShower: sender=" + this.senderUsername + " recipient=" + this.recipientUserData + "gift=" + this.gift.getName() + "virtualGiftReceivedID=" + this.virtualGiftReceivedID + "totalGiftShowerRecipients=" + this.totalGiftShowerRecipients);
         }

         EventSystemPrx eventSystem = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VIRTUALGIFT_ONEWAY_EVENT_TRIGGER) ? this.icePrxFinder.getOnewayEventSystemProxy() : this.icePrxFinder.getEventSystemProxy();
         eventSystem.giftShowerEvent(this.senderUsername, this.recipientUserData.username, this.gift.getName(), this.virtualGiftReceivedID, this.totalGiftShowerRecipients);
      } catch (Exception var2) {
         log.error("Failed to send GiftShowerEvent for [" + this.senderUsername + "] gift[" + this.gift.getName() + "] totalRecipients[" + this.totalGiftShowerRecipients + "]", var2);
      }

   }
}
