package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.MessageData;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class EmoteCommandData implements Serializable {
   private static final long serialVersionUID = -1830700933936519357L;
   private long id;
   private String commandName;
   private String commandStateName;
   private String description;
   private String handlerClassName;
   private int supportedChatTypes;
   private int messageColour;
   private String messageText;
   private java.util.List<String> emoticonKeys;
   private boolean status;
   private EmoteCommand emoteCommandHandler;
   private long parentId;
   private boolean free;
   private double price;
   private String currency;
   private EmoteCommandData parentEmoteCommandData;
   private boolean offlineEnabled;

   public EmoteCommandData(ResultSet rs) throws SQLException {
      this.id = (long)rs.getInt("ID");
      this.description = rs.getString("Description");
      this.commandName = rs.getString("CommandName");
      this.commandStateName = rs.getString("CommandStateName");
      this.handlerClassName = rs.getString("HandlerClassName");
      this.supportedChatTypes = rs.getInt("SupportedChatTypes");
      String messageColourStr = rs.getString("MessageColour");
      this.messageColour = StringUtil.isBlank(messageColourStr) ? -1 : Integer.parseInt(messageColourStr, 16);
      this.messageText = rs.getString("MessageText");
      String emoticonKeyList = rs.getString("EmoticonKeyList");
      this.emoticonKeys = (java.util.List)(StringUtil.isBlank(emoticonKeyList) ? new ArrayList(0) : Arrays.asList(emoticonKeyList.split(" ")));
      this.price = rs.getDouble("Price");
      this.free = rs.wasNull();
      this.currency = rs.getString("Currency");
      this.status = rs.getBoolean("Status");
      this.parentId = (long)rs.getInt("ParentID");
      this.emoteCommandHandler = null;
      this.parentEmoteCommandData = null;
      this.offlineEnabled = rs.getBoolean("OfflineEnabled");
   }

   public long getId() {
      return this.id;
   }

   public void setId(long id) {
      this.id = id;
   }

   public String getCommandName() {
      return this.commandName;
   }

   public void setCommandName(String commandName) {
      this.commandName = commandName;
   }

   public void setCommandStateName(String commandStateName) {
      this.commandStateName = commandStateName;
   }

   public String getCommandStateName() {
      return this.commandStateName;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getHandlerClassName() {
      return this.handlerClassName;
   }

   public void setHandlerClassName(String handlerClassName) {
      this.handlerClassName = handlerClassName;
   }

   public int getSupportedChatTypes() {
      return this.supportedChatTypes;
   }

   public void setSupportedChatTypes(int supportedChatTypes) {
      this.supportedChatTypes = supportedChatTypes;
   }

   public boolean supportChatType(ChatSource.ChatType chatType) {
      return chatType != null && chatType.isSupported(this.supportedChatTypes);
   }

   public int getMessageColour() {
      return this.messageColour;
   }

   public void setMessageColour(int messageColour) {
      this.messageColour = messageColour;
   }

   public String getMessageText() {
      return this.messageText;
   }

   public void setMessageText(String messageText) {
      this.messageText = messageText;
   }

   public java.util.List<String> getEmoticonKeys() {
      return this.emoticonKeys;
   }

   public java.util.List<String> getUsedEmoticonKeys() {
      return this.getUsedEmoticonKeys(this.messageText);
   }

   public java.util.List<String> getUsedEmoticonKeys(String message) {
      java.util.List<String> keys = new ArrayList();
      if (this.emoticonKeys != null) {
         Iterator i$ = this.emoticonKeys.iterator();

         while(i$.hasNext()) {
            String key = (String)i$.next();
            if (message.contains(key)) {
               keys.add(key);
            }
         }
      }

      return keys;
   }

   public void setEmoticonKeys(java.util.List<String> emoticonKeys) {
      this.emoticonKeys = emoticonKeys;
   }

   public boolean isEnabled() {
      return this.status;
   }

   public void setEnabled(boolean enabled) {
      this.status = enabled;
   }

   public double getPrice() {
      return this.price;
   }

   public void setPrice(double price) {
      this.price = price;
   }

   public String getCurrency() {
      return this.currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public boolean isFree() {
      return this.free;
   }

   public void setFree(boolean free) {
      this.free = free;
   }

   public String getPriceWithCurrency() {
      if (this.isFree()) {
         return "free";
      } else {
         DecimalFormat priceFormat = new DecimalFormat("0.00");
         return this.currency + priceFormat.format(this.price);
      }
   }

   public EmoteCommand getEmoteCommandHandler() {
      return this.emoteCommandHandler;
   }

   public void setEmoteCommandHandler(EmoteCommand emoteCommandHandler) {
      this.emoteCommandHandler = emoteCommandHandler;
      emoteCommandHandler.setEmoteCommandData(this);
   }

   public void instantiateEmoteCommandHandler() throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
      Class<?> cmdClass = Class.forName(this.handlerClassName);
      Constructor<?> cmdConstructor = cmdClass.getConstructor(EmoteCommandData.class);
      this.emoteCommandHandler = (EmoteCommand)cmdConstructor.newInstance(this);
   }

   public long getParentId() {
      return this.parentId;
   }

   public void setParentId(long parentId) {
      this.parentId = parentId;
   }

   public EmoteCommandData getParentEmoteCommandData() {
      return this.parentEmoteCommandData;
   }

   public void setParentEmoteCommandData(EmoteCommandData parentEmoteCommandData) {
      this.parentEmoteCommandData = parentEmoteCommandData;
   }

   public void updateMessageData(MessageData messageData) {
      this.updateMessageData(messageData, true);
   }

   public void updateMessageData(MessageData messageData, boolean updateEmoticonKeys) {
      messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
      if (updateEmoticonKeys) {
         messageData.emoticonKeys = this.getUsedEmoticonKeys(messageData.messageText);
      }

      if (this.messageColour >= 0) {
         messageData.messageColour = this.messageColour;
      }

   }

   public boolean isOfflineEnabled() {
      return this.offlineEnabled;
   }

   public void setOfflineEnabled(boolean offlineEnabled) {
      this.offlineEnabled = offlineEnabled;
   }
}
