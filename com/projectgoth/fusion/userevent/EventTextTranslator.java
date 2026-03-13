package com.projectgoth.fusion.userevent;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.slice.AddingFriendUserEventIce;
import com.projectgoth.fusion.slice.AddingMultipleFriendsUserEventIce;
import com.projectgoth.fusion.slice.AddingTwoFriendsUserEventIce;
import com.projectgoth.fusion.slice.CreatedChatroomUserEventIce;
import com.projectgoth.fusion.slice.GenericApplicationUserEventIce;
import com.projectgoth.fusion.slice.GiftShowerUserEventIce;
import com.projectgoth.fusion.slice.GroupAnnouncementUserEventIce;
import com.projectgoth.fusion.slice.GroupDonationUserEventIce;
import com.projectgoth.fusion.slice.GroupJoinedUserEventIce;
import com.projectgoth.fusion.slice.GroupUserPostUserEventIce;
import com.projectgoth.fusion.slice.PhotoUploadedUserEventIce;
import com.projectgoth.fusion.slice.ProfileUpdatedUserEventIce;
import com.projectgoth.fusion.slice.PurchasedVirtualGoodsUserEventIce;
import com.projectgoth.fusion.slice.ShortTextStatusUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserWallPostUserEventIce;
import com.projectgoth.fusion.slice.VirtualGiftUserEventIce;
import com.projectgoth.fusion.userevent.domain.AddingFriendUserEvent;
import com.projectgoth.fusion.userevent.domain.AddingMultipleFriendsUserEvent;
import com.projectgoth.fusion.userevent.domain.AddingTwoFriendsUserEvent;
import com.projectgoth.fusion.userevent.domain.CreatedChatroomUserEvent;
import com.projectgoth.fusion.userevent.domain.GenericApplicationUserEvent;
import com.projectgoth.fusion.userevent.domain.GiftShowerUserEvent;
import com.projectgoth.fusion.userevent.domain.GroupAnnouncementUserEvent;
import com.projectgoth.fusion.userevent.domain.GroupDonationUserEvent;
import com.projectgoth.fusion.userevent.domain.GroupJoinedUserEvent;
import com.projectgoth.fusion.userevent.domain.GroupUserPostUserEvent;
import com.projectgoth.fusion.userevent.domain.PhotoUploadedUserEvent;
import com.projectgoth.fusion.userevent.domain.ProfileUpdatedUserEvent;
import com.projectgoth.fusion.userevent.domain.PurchasedVirtualGoodsUserEvent;
import com.projectgoth.fusion.userevent.domain.ShortTextStatusUserEvent;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import com.projectgoth.fusion.userevent.domain.UserWallPostUserEvent;
import com.projectgoth.fusion.userevent.domain.VirtualGiftUserEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class EventTextTranslator {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventTextTranslator.class));
   public static final String TRANSLATION_FILENAME = "translation.properties";
   public static final String KBROWSER_TYPE = "kb";
   public static final String TOUCH_TYPE = "touch";
   public static final String AJAXV2_TYPE = "ajaxv2";
   public static final String SEPERATOR = "-";
   private static Properties properties;

   private static boolean loadProperties() {
      properties = new Properties();
      String propertiesLocation = ConfigUtils.getConfigDirectory() + "translation.properties";

      try {
         InputStream inputStream = new FileInputStream(new File(propertiesLocation));
         properties.load(inputStream);
         if (properties.size() > 0) {
            return true;
         }
      } catch (Exception var2) {
         log.fatal("Failed to load translation.properties, it should be at [" + propertiesLocation + "], aborting.", var2);
      }

      return false;
   }

   private boolean isKBrowserType(ClientType deviceType) {
      if (deviceType == ClientType.MIDP2) {
         return true;
      } else {
         return deviceType == ClientType.MIDP1;
      }
   }

   private boolean isTouchBrowserType(ClientType deviceType) {
      return deviceType == ClientType.ANDROID;
   }

   private boolean isAjaxv2Type(ClientType deviceType) {
      return deviceType == ClientType.AJAX2;
   }

   private String getTemplateName(String eventName, ClientType deviceType) {
      StringBuffer key = new StringBuffer();
      if (this.isKBrowserType(deviceType)) {
         key.append("kb");
      } else if (this.isTouchBrowserType(deviceType)) {
         key.append("touch");
      } else if (this.isAjaxv2Type(deviceType)) {
         key.append("ajaxv2");
      } else {
         key.append(deviceType.toString());
      }

      key.append("-");
      key.append(eventName);
      return key.toString();
   }

   private String getTemplate(String templateName) {
      String template = properties.getProperty(templateName);
      if (template == null) {
         log.warn("no template found for key [" + templateName + "]");
         return "";
      } else {
         if (log.isDebugEnabled()) {
            log.debug("using template [" + template + "] for templateName [" + templateName + "]");
         }

         return template;
      }
   }

   private String translate(String template, Map<String, String> keyValue) {
      if (log.isDebugEnabled()) {
         log.debug("translating template [" + template + "] with [" + keyValue.size() + "] keys");
      }

      StringBuffer buffer = new StringBuffer();
      StringTokenizer tokenizer = new StringTokenizer(template, "**");

      while(tokenizer.hasMoreTokens()) {
         String token = tokenizer.nextToken();
         if (keyValue.containsKey(token)) {
            buffer.append((String)keyValue.get(token));
         } else {
            buffer.append(token);
         }
      }

      return buffer.toString();
   }

   private String genericApplicationEventTranslator(GenericApplicationUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("GENERIC_APP_EVENT", deviceType)), GenericApplicationUserEvent.findSubstitutionParameters(event, deviceType));
   }

   private String giftShowerEventTranslator(GiftShowerUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("GIFT_SHOWER", deviceType)), GiftShowerUserEvent.findSubstitutionParameters(event));
   }

   private String addingMultipleFriendsTranslator(AddingMultipleFriendsUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("ADDING_MULTIPLE_FRIENDS", deviceType)), AddingMultipleFriendsUserEvent.findSubstitutionParameters(event));
   }

   private String addingTwoFriendsTranslator(AddingTwoFriendsUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("ADDING_TWO_FRIENDS", deviceType)), AddingTwoFriendsUserEvent.findSubstitutionParameters(event));
   }

   private String addingFriendTranslator(AddingFriendUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("ADDING_FRIEND", deviceType)), AddingFriendUserEvent.findSubstitutionParameters(event));
   }

   private String purchasedVirtualGoodsTranslator(String templateName, Map<String, String> keyValue) {
      return this.translate(this.getTemplate(templateName), keyValue);
   }

   private String shortTextStatusTranslator(ShortTextStatusUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("SHORT_TEXT_STATUS", deviceType)), ShortTextStatusUserEvent.findSubstitutionParameters(event));
   }

   private String profileUpdatedTranslator(ProfileUpdatedUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("PROFILE_UPDATED", deviceType)), ProfileUpdatedUserEvent.findSubstitutionParameters(event));
   }

   private String photoUploadedTranslator(PhotoUploadedUserEventIce event, ClientType deviceType) {
      String templateName = this.getTemplateName("PHOTO_UPLOADED", deviceType);
      if (StringUtils.hasText(event.title)) {
         templateName = templateName + "_WITH_TITLE";
      } else {
         templateName = templateName + "_WITHOUT_TITLE";
      }

      return this.translate(this.getTemplate(templateName), PhotoUploadedUserEvent.findSubstitutionParameters(event));
   }

   private String createdChatroomTranslator(CreatedChatroomUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("CREATE_PUBLIC_CHATROOM", deviceType)), CreatedChatroomUserEvent.findSubstitutionParameters(event));
   }

   private String createdVirtualGiftTranslator(VirtualGiftUserEventIce event, ClientType deviceType) {
      String templateName = null;
      if (StringUtil.startsWithaVowel(event.giftName)) {
         templateName = this.getTemplate(this.getTemplateName("VIRTUAL_GIFT", deviceType) + "_AN");
      } else {
         templateName = this.getTemplate(this.getTemplateName("VIRTUAL_GIFT", deviceType));
      }

      return this.translate(templateName, VirtualGiftUserEvent.findSubstitutionParameters(event));
   }

   private String createUserWallPostTranslator(UserWallPostUserEventIce event, ClientType deviceType, String receivingUsername) {
      String templateName = null;
      if (event.wallOwnerUsername.equals(receivingUsername)) {
         templateName = this.getTemplate(this.getTemplateName("USER_WALL_POST", deviceType) + "_YOUR");
      } else if (event.generatingUsername.equals(event.wallOwnerUsername)) {
         templateName = this.getTemplate(this.getTemplateName("USER_WALL_POST", deviceType) + "_THEIR");
      } else {
         templateName = this.getTemplate(this.getTemplateName("USER_WALL_POST", deviceType));
      }

      return this.translate(templateName, UserWallPostUserEvent.findSubstitutionParameters(event));
   }

   private String createdGroupAnnouncementTranslator(GroupAnnouncementUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("GROUP_ANNOUNCEMENT", deviceType)), GroupAnnouncementUserEvent.findSubstitutionParameters(event));
   }

   private String createdGroupDonationTranslator(GroupDonationUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("GROUP_DONATION", deviceType)), GroupDonationUserEvent.findSubstitutionParameters(event));
   }

   private String createdGroupJoinedTranslator(GroupJoinedUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("GROUP_JOINED", deviceType)), GroupJoinedUserEvent.findSubstitutionParameters(event));
   }

   private String createdGroupUserPostTranslator(GroupUserPostUserEventIce event, ClientType deviceType) {
      return this.translate(this.getTemplate(this.getTemplateName("GROUP_USER_POST", deviceType)), GroupUserPostUserEvent.findSubstitutionParameters(event));
   }

   private String genericEvent() {
      return "";
   }

   private String invokeRelevantTranslator(UserEventIce event, ClientType deviceType, String receivingUsername) {
      if (event instanceof AddingFriendUserEventIce) {
         if (event instanceof AddingMultipleFriendsUserEventIce) {
            return this.addingMultipleFriendsTranslator((AddingMultipleFriendsUserEventIce)event, deviceType);
         } else {
            return event instanceof AddingTwoFriendsUserEventIce ? this.addingTwoFriendsTranslator((AddingTwoFriendsUserEventIce)event, deviceType) : this.addingFriendTranslator((AddingFriendUserEventIce)event, deviceType);
         }
      } else if (event instanceof PurchasedVirtualGoodsUserEventIce) {
         Map<String, String> keyValue = PurchasedVirtualGoodsUserEvent.findSubstitutionParameters((PurchasedVirtualGoodsUserEventIce)event);
         String templateName = this.getTemplateName("PURCHASED_VIRTUAL_GOODS", deviceType);
         templateName = templateName + "-";
         templateName = templateName + ((String)keyValue.get("itemtype")).toUpperCase();
         return this.purchasedVirtualGoodsTranslator(templateName, keyValue);
      } else if (event instanceof ShortTextStatusUserEventIce) {
         return this.shortTextStatusTranslator((ShortTextStatusUserEventIce)event, deviceType);
      } else if (event instanceof ProfileUpdatedUserEventIce) {
         return this.profileUpdatedTranslator((ProfileUpdatedUserEventIce)event, deviceType);
      } else if (event instanceof PhotoUploadedUserEventIce) {
         return this.photoUploadedTranslator((PhotoUploadedUserEventIce)event, deviceType);
      } else if (event instanceof CreatedChatroomUserEventIce) {
         return this.createdChatroomTranslator((CreatedChatroomUserEventIce)event, deviceType);
      } else if (event instanceof VirtualGiftUserEventIce) {
         return this.createdVirtualGiftTranslator((VirtualGiftUserEventIce)event, deviceType);
      } else if (event instanceof UserWallPostUserEventIce) {
         return this.createUserWallPostTranslator((UserWallPostUserEventIce)event, deviceType, receivingUsername);
      } else if (event instanceof GroupAnnouncementUserEventIce) {
         return this.createdGroupAnnouncementTranslator((GroupAnnouncementUserEventIce)event, deviceType);
      } else if (event instanceof GroupDonationUserEventIce) {
         return this.createdGroupDonationTranslator((GroupDonationUserEventIce)event, deviceType);
      } else if (event instanceof GroupJoinedUserEventIce) {
         return this.createdGroupJoinedTranslator((GroupJoinedUserEventIce)event, deviceType);
      } else if (event instanceof GroupUserPostUserEventIce) {
         return this.createdGroupUserPostTranslator((GroupUserPostUserEventIce)event, deviceType);
      } else if (event instanceof GenericApplicationUserEventIce) {
         return this.genericApplicationEventTranslator((GenericApplicationUserEventIce)event, deviceType);
      } else {
         return event instanceof GiftShowerUserEventIce ? this.giftShowerEventTranslator((GiftShowerUserEventIce)event, deviceType) : this.genericEvent();
      }
   }

   public String translate(UserEventIce event, ClientType deviceType, String receivingUsername) {
      try {
         String text = this.invokeRelevantTranslator(event, deviceType, receivingUsername);
         if (log.isDebugEnabled()) {
            log.debug("translating event [" + UserEvent.toString(event) + "], resulting text is [" + text + "]");
         }

         return text;
      } catch (Exception var5) {
         log.error("failed to translate event [" + event + "] for deviceType [" + deviceType + "]", var5);
         return "";
      }
   }

   public static void main(String[] args) {
      VirtualGiftUserEventIce event = new VirtualGiftUserEventIce();
      event.generatingUsername = "thenephilim";
      event.giftName = "poes";
      event.recipient = "nefilim";
      event.virtualGiftReceivedId = 1;
      EventTextTranslator translator = new EventTextTranslator();
      System.out.println(translator.translate(event, ClientType.MIDP2, "koko"));
   }

   static {
      if (!loadProperties()) {
         log.error("failed to load properties from translation.properties");
      }

   }
}
