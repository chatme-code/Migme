package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.EnumUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.fdl.enums.MessageType;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.MessageDestinationDataIce;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;

public class MessageData implements Serializable {
   public Integer id;
   public String username;
   public Date dateCreated;
   public Date requestReceivedTimestamp;
   public MessageType type;
   public String messageText;
   public Integer messageColour;
   public MessageData.ContentTypeEnum contentType;
   public byte[] binaryData;
   public MessageData.SendReceiveEnum sendReceive;
   public Integer sourceContactID;
   public String source;
   public MessageData.SourceTypeEnum sourceType;
   public String sourceDisplayPicture;
   public Integer sourceColour;
   public Boolean fromAdministrator;
   public List<String> emoticonKeys;
   public List<MessageDestinationData> messageDestinations;
   public String guid;
   public Long messageTimestamp;
   public String groupChatName;
   public String groupChatOwner;
   public MessageData.EmoteContentTypeEnum emoteContentType;
   public String mimeType;
   public String mimeTypeData;

   public MessageData() {
   }

   public MessageData(MessageData cloneFrom) {
      this(cloneFrom.toIceObject());
   }

   public MessageData(MessageDataIce messageIce) {
      this.id = messageIce.id == Integer.MIN_VALUE ? null : messageIce.id;
      this.username = messageIce.username.equals("\u0000") ? null : messageIce.username;
      this.dateCreated = messageIce.dateCreated == Long.MIN_VALUE ? null : new Date(messageIce.dateCreated);
      this.requestReceivedTimestamp = messageIce.requestReceivedTimestamp == Long.MIN_VALUE ? null : new Date(messageIce.requestReceivedTimestamp);
      this.type = messageIce.type == Integer.MIN_VALUE ? null : MessageType.fromValue(messageIce.type);
      this.messageText = messageIce.messageText.equals("\u0000") ? null : messageIce.messageText;
      this.messageColour = messageIce.messageColour == Integer.MIN_VALUE ? null : messageIce.messageColour;
      this.contentType = messageIce.contentType == Integer.MIN_VALUE ? null : MessageData.ContentTypeEnum.fromValue(messageIce.contentType);
      this.binaryData = messageIce.binaryData.length == 0 ? null : messageIce.binaryData;
      this.sendReceive = messageIce.sendReceive == Integer.MIN_VALUE ? null : MessageData.SendReceiveEnum.fromValue(messageIce.sendReceive);
      this.sourceContactID = messageIce.sourceContactID == Integer.MIN_VALUE ? null : messageIce.sourceContactID;
      this.source = messageIce.source.equals("\u0000") ? null : messageIce.source;
      this.sourceType = messageIce.sourceType == Integer.MIN_VALUE ? null : MessageData.SourceTypeEnum.fromValue(messageIce.sourceType);
      this.sourceDisplayPicture = messageIce.sourceDisplayPicture.equals("\u0000") ? null : messageIce.sourceDisplayPicture;
      this.sourceColour = messageIce.sourceColour == Integer.MIN_VALUE ? null : messageIce.sourceColour;
      this.fromAdministrator = messageIce.fromAdministrator == Integer.MIN_VALUE ? null : messageIce.fromAdministrator == 1;
      int len$;
      int i$;
      if (messageIce.emoticonKeys != null) {
         String[] arr$ = messageIce.emoticonKeys;
         len$ = arr$.length;

         for(i$ = 0; i$ < len$; ++i$) {
            String emoticonKey = arr$[i$];
            if (this.emoticonKeys == null) {
               this.emoticonKeys = new LinkedList();
            }

            this.emoticonKeys.add(emoticonKey);
         }
      }

      if (messageIce.messageDestinations != null) {
         MessageDestinationDataIce[] arr$ = messageIce.messageDestinations;
         len$ = arr$.length;

         for(i$ = 0; i$ < len$; ++i$) {
            MessageDestinationDataIce messageDestIce = arr$[i$];
            if (this.messageDestinations == null) {
               this.messageDestinations = new LinkedList();
            }

            this.messageDestinations.add(new MessageDestinationData(messageDestIce));
         }
      }

      this.guid = messageIce.guid != null && !messageIce.guid.equals("\u0000") ? messageIce.guid : null;
      this.messageTimestamp = messageIce.messageTimestamp == Long.MIN_VALUE ? null : messageIce.messageTimestamp;
      this.groupChatName = messageIce.groupChatName != null && !messageIce.groupChatName.equals("\u0000") ? messageIce.groupChatName : null;
      this.groupChatOwner = messageIce.groupChatOwner != null && !messageIce.groupChatOwner.equals("\u0000") ? messageIce.groupChatOwner : null;
      this.emoteContentType = MessageData.EmoteContentTypeEnum.fromValue(messageIce.emoteContentType);
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED)) {
         this.mimeType = messageIce.mimeType.equals("\u0000") ? null : messageIce.mimeType;
         this.mimeTypeData = messageIce.mimeTypeData.equals("\u0000") ? null : messageIce.mimeTypeData;
      }

   }

   public MessageDataIce toIceObject() {
      MessageDataIce messageIce = new MessageDataIce();
      messageIce.id = this.id == null ? Integer.MIN_VALUE : this.id;
      messageIce.username = this.username == null ? "\u0000" : this.username;
      messageIce.dateCreated = this.dateCreated == null ? Long.MIN_VALUE : this.dateCreated.getTime();
      messageIce.requestReceivedTimestamp = this.requestReceivedTimestamp == null ? Long.MIN_VALUE : this.requestReceivedTimestamp.getTime();
      messageIce.type = this.type == null ? Integer.MIN_VALUE : this.type.value();
      messageIce.messageText = this.messageText == null ? "\u0000" : this.messageText;
      messageIce.messageColour = this.messageColour == null ? Integer.MIN_VALUE : this.messageColour;
      messageIce.contentType = this.contentType == null ? Integer.MIN_VALUE : this.contentType.value();
      messageIce.binaryData = this.binaryData == null ? new byte[0] : this.binaryData;
      messageIce.sendReceive = this.sendReceive == null ? Integer.MIN_VALUE : this.sendReceive.value();
      messageIce.sourceContactID = this.sourceContactID == null ? Integer.MIN_VALUE : this.sourceContactID;
      messageIce.source = this.source == null ? "\u0000" : this.source;
      messageIce.sourceType = this.sourceType == null ? Integer.MIN_VALUE : this.sourceType.value();
      messageIce.sourceDisplayPicture = this.sourceDisplayPicture == null ? "\u0000" : this.sourceDisplayPicture;
      messageIce.sourceColour = this.sourceColour == null ? Integer.MIN_VALUE : this.sourceColour;
      messageIce.fromAdministrator = this.fromAdministrator == null ? Integer.MIN_VALUE : (this.fromAdministrator ? 1 : 0);
      int i;
      if (this.emoticonKeys != null && this.emoticonKeys.size() > 0) {
         messageIce.emoticonKeys = new String[this.emoticonKeys.size()];

         for(i = 0; i < this.emoticonKeys.size(); ++i) {
            messageIce.emoticonKeys[i] = (String)this.emoticonKeys.get(i);
         }
      }

      if (this.messageDestinations != null && this.messageDestinations.size() > 0) {
         messageIce.messageDestinations = new MessageDestinationDataIce[this.messageDestinations.size()];

         for(i = 0; i < this.messageDestinations.size(); ++i) {
            messageIce.messageDestinations[i] = ((MessageDestinationData)this.messageDestinations.get(i)).toIceObject();
         }
      }

      messageIce.guid = this.guid == null ? "\u0000" : this.guid;
      messageIce.messageTimestamp = this.messageTimestamp == null ? Long.MIN_VALUE : this.messageTimestamp;
      messageIce.groupChatName = this.groupChatName == null ? "\u0000" : this.groupChatName;
      messageIce.groupChatOwner = this.groupChatOwner == null ? "\u0000" : this.groupChatOwner;
      messageIce.emoteContentType = this.emoteContentType != null ? this.emoteContentType.value() : MessageData.EmoteContentTypeEnum.PLAIN.value();
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED)) {
         messageIce.mimeType = this.mimeType == null ? "\u0000" : this.mimeType;
         messageIce.mimeTypeData = this.mimeTypeData == null ? "\u0000" : this.mimeTypeData;
      }

      return messageIce;
   }

   public boolean isSystemMessage() {
      return this.sourceType.value() >= MessageData.SourceTypeEnum.SYSTEM_GENERAL.value();
   }

   public static boolean isSystemMessage(MessageDataIce message) {
      return message.sourceType >= MessageData.SourceTypeEnum.SYSTEM_GENERAL.value();
   }

   public static boolean isFusionMessage(MessageDataIce message) {
      return message.type == MessageType.FUSION.value();
   }

   public static boolean isMessageToAChatRoom(MessageDataIce message) {
      return message.messageDestinations[0].type == MessageDestinationData.TypeEnum.CHAT_ROOM.value();
   }

   public static boolean isMessageToAGroupChat(MessageDataIce message) {
      return message.messageDestinations[0].type == MessageDestinationData.TypeEnum.GROUP.value();
   }

   public static boolean isMessageToAnIndividual(MessageDataIce message) {
      return message.messageDestinations[0].type == MessageDestinationData.TypeEnum.INDIVIDUAL.value();
   }

   public boolean isMessageToAnIndividual() {
      return ((MessageDestinationData)this.messageDestinations.get(0)).type == MessageDestinationData.TypeEnum.INDIVIDUAL;
   }

   public static boolean hasDestinations(MessageDataIce message) {
      return message.messageDestinations != null && message.messageDestinations.length > 0;
   }

   public static MessageData newChatRoomMessage(String chatRoomName, String message, String[] emoticonKeys) {
      MessageDestinationData destinationData = new MessageDestinationData();
      destinationData.type = MessageDestinationData.TypeEnum.CHAT_ROOM;
      destinationData.destination = chatRoomName;
      MessageData messageData = new MessageData();
      messageData.type = MessageType.FUSION;
      messageData.sendReceive = MessageData.SendReceiveEnum.SEND;
      messageData.source = chatRoomName;
      messageData.sourceType = MessageData.SourceTypeEnum.CHATROOM;
      messageData.contentType = MessageData.ContentTypeEnum.TEXT;
      messageData.messageText = message;
      messageData.messageDestinations = new ArrayList();
      messageData.messageDestinations.add(destinationData);
      if (emoticonKeys != null) {
         messageData.emoticonKeys = Arrays.asList(emoticonKeys);
      }

      return messageData;
   }

   public static String toString(MessageDataIce i) {
      return (new MessageData(i)).toString();
   }

   private String nullOrString(Object o) {
      return o == null ? "null" : o.toString();
   }

   private String nullOrStringForColor(Integer c) {
      return c == null ? "null" : String.format("%06X", c);
   }

   public String toString() {
      String binaryDataStr = convertByteArrayToString(this.binaryData);
      ArrayList mdl;
      if (this.messageDestinations != null && this.messageDestinations.size() > 0) {
         mdl = new ArrayList(this.messageDestinations == null ? 0 : this.messageDestinations.size());
         Iterator i$ = this.messageDestinations.iterator();

         while(i$.hasNext()) {
            MessageDestinationData md = (MessageDestinationData)i$.next();
            mdl.add(md.toString());
         }
      } else {
         mdl = new ArrayList();
      }

      return String.format("MsgData: id=%s, username=%s, dateCreated=%s, reqReceivedTS=%s, type=%s, messageText=%s, messageColour=%s, contentType=%s, binaryData=%s, sendReceive=%s, sourceContactID=%s, source=%s, sourceType=%s, sourceDisplayPicture=%s, sourceColour=%s, fromAdministrator=%s, emoticonKeys=[%s], messageDestinations=[%s], mimeType=[%s], mimeTypeData=[%s]", this.nullOrString(this.id), this.username, this.dateCreated, this.requestReceivedTimestamp, this.nullOrString(this.type), this.messageText, this.nullOrStringForColor(this.messageColour), this.nullOrString(this.contentType), binaryDataStr, this.nullOrString(this.sendReceive), this.nullOrString(this.sourceContactID), this.source, this.nullOrString(this.sourceType), this.sourceDisplayPicture, this.nullOrStringForColor(this.sourceColour), this.nullOrString(this.fromAdministrator), StringUtil.join((Collection)this.emoticonKeys, ","), StringUtil.join((Collection)mdl, ","), this.nullOrString(this.mimeType), this.nullOrString(this.mimeTypeData));
   }

   public String[] getArgs() {
      return this.messageText.toLowerCase().split(" ");
   }

   private static String convertByteArrayToString(byte[] data) {
      String str = "";
      if (data != null && data.length > 0) {
         StringBuilder sb = new StringBuilder(2 * data.length);
         byte[] arr$ = data;
         int len$ = data.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            byte b = arr$[i$];
            sb.append(String.format("%02X", b));
         }

         str = sb.toString();
      }

      return str;
   }

   public void clearMimeTypeAndData() {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED)) {
         this.mimeType = null;
         this.mimeTypeData = null;
      }
   }

   public void setMimeTypeAndData(VirtualGiftData gift, String sender, List<String> allRecipients, VirtualGiftData.GiftingType giftingType, String giftMessage) {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CoreChatSettings.MIME_TYPE_MESSAGES_ENABLED)) {
         if (gift != null && !StringUtil.isBlank(sender) && allRecipients != null && giftingType != null) {
            JSONObject json = new JSONObject();

            try {
               json.put("name", gift.getName());
               json.put("hotkey", gift.getHotKey());
               json.put("type", giftingType.name());
               json.put("message", giftMessage);
               json.put("sender", sender);
               json.put("recipient", StringUtil.join((Collection)allRecipients, ","));
            } catch (JSONException var8) {
            }

            this.mimeType = "emote/gift";
            this.mimeTypeData = json.toString();
         }
      }
   }

   public static enum SendReceiveEnum {
      SEND(1),
      RECEIVE(2);

      private int value;

      private SendReceiveEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MessageData.SendReceiveEnum fromValue(int value) {
         MessageData.SendReceiveEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MessageData.SendReceiveEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum SourceTypeEnum {
      USER(1, Integer.MIN_VALUE),
      ADMIN_USER(2, Integer.MIN_VALUE),
      GROUP_ADMIN_USER(3, 16565508),
      MODERATOR_USER(4, 16565508),
      SYSTEM_GENERAL(10, Integer.MIN_VALUE),
      CHATROOM(11, Integer.MIN_VALUE),
      TOP_MERCHANT_LVL1(12, 10027161),
      TOP_MERCHANT_LVL2(13, 16723623),
      TOP_MERCHANT_LVL3(15, 16711680),
      BOT(16, 8112384),
      GLOBAL_ADMIN(17, 16020514);

      private int value;
      private int colorHex;

      private SourceTypeEnum(int value, int colorHex) {
         this.value = value;
         this.colorHex = colorHex;
      }

      public int value() {
         return this.value;
      }

      public int colorHex() {
         return this.colorHex;
      }

      public static MessageData.SourceTypeEnum fromValue(int value) {
         MessageData.SourceTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MessageData.SourceTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ContentTypeEnum {
      TEXT(1),
      IMAGE(2),
      AUDIO(3),
      VIDEO(4),
      EXISTING_FILE(5),
      EMOTE(6);

      private int value;

      private ContentTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static MessageData.ContentTypeEnum fromValue(int value) {
         MessageData.ContentTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            MessageData.ContentTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum EmoteContentTypeEnum implements EnumUtils.IEnumValueGetter<Byte> {
      PLAIN((byte)0),
      STICKERS((byte)1);

      private Byte value;

      private EmoteContentTypeEnum(Byte value) {
         this.value = value;
      }

      public Byte getEnumValue() {
         return this.value;
      }

      public static MessageData.EmoteContentTypeEnum fromValue(byte value) {
         return (MessageData.EmoteContentTypeEnum)MessageData.EmoteContentTypeEnum.SingletonHolder.lookupByCode.get(value);
      }

      public byte value() {
         return this.value;
      }

      private static class SingletonHolder {
         public static final HashMap<Byte, MessageData.EmoteContentTypeEnum> lookupByCode = (HashMap)EnumUtils.buildLookUpMap(new HashMap(), MessageData.EmoteContentTypeEnum.class);
      }
   }
}
