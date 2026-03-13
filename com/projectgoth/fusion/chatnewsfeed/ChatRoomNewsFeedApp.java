package com.projectgoth.fusion.chatnewsfeed;

import Ice.Application;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.Properties;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.RegistryPrxHelper;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;

public abstract class ChatRoomNewsFeedApp extends Application {
   protected static Logger logger;
   public static String hostName = null;
   public static Properties properties = null;
   public static RegistryPrx registryPrx;
   protected String[] chatRoomNames;

   public int run(String[] arg0) {
      properties = communicator().getProperties();

      try {
         hostName = InetAddress.getLocalHost().getHostName().toUpperCase();
      } catch (UnknownHostException var6) {
         hostName = "UNKNOWN";
      }

      String registryStringifiedProxy = communicator().getProperties().getProperty("RegistryProxy");
      ObjectPrx basePrx = communicator().stringToProxy(registryStringifiedProxy);
      logger.info("Connecting to [" + basePrx + "]");

      try {
         registryPrx = RegistryPrxHelper.checkedCast(basePrx);
      } catch (LocalException var5) {
         logger.fatal("Chat Room NewsFeed: " + hostName + ": Connection to [" + registryPrx + "] failed. ", var5);
         return 1;
      }

      if (registryPrx == null) {
         logger.fatal("Chat Room NewsFeed: " + hostName + ": Connection to [" + registryPrx + "] failed");
         return 1;
      } else {
         return 0;
      }
   }

   protected void sendMessagesToChatRooms(List<MessageData> messages, String roomDescription) throws Exception {
      if (messages != null && !messages.isEmpty()) {
         ChatRoomPrx[] chatRoomProxies = this.getChatRooms();
         this.updateRoomDescription(roomDescription, chatRoomProxies);
         Iterator i$ = messages.iterator();

         while(i$.hasNext()) {
            MessageData message = (MessageData)i$.next();
            ChatRoomPrx[] arr$ = chatRoomProxies;
            int len$ = chatRoomProxies.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               ChatRoomPrx chatRoomPrx = arr$[i$];
               if (chatRoomPrx != null) {
                  List<String> emoticonKeys = message.emoticonKeys;
                  chatRoomPrx.putSystemMessage(message.messageText, emoticonKeys != null ? (String[])emoticonKeys.toArray(new String[0]) : null);
                  if (logger.isInfoEnabled()) {
                     logger.info("Message sent successfully to " + chatRoomPrx);
                  }
               }
            }
         }

      } else {
         logger.error("No messages to send.");
      }
   }

   private void updateRoomDescription(String roomDescription, ChatRoomPrx[] chatRoomProxies) throws FusionException {
      if (roomDescription != null) {
         try {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            messageEJB.updateRoomDescriptions(this.chatRoomNames, roomDescription);
         } catch (Exception var4) {
            logger.warn("Unable to update chat room description in the DB. ", var4);
            throw new FusionException("Unable to obtain details of the chat room from the DB");
         }
      }

   }

   public void createGroupPost(String username, int groupModuleID, String title, String text, int status) {
      if (groupModuleID != -1 && !StringUtils.isEmpty(title)) {
         try {
            Web webEJB = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
            Hashtable result = webEJB.createGroupPost(username, groupModuleID, title, text, status);
            if (logger.isInfoEnabled()) {
               logger.info("Post result: " + result);
            }
         } catch (Exception var8) {
            logger.warn("Unable to create group post in the DB with title '" + title + "' and groupModuleID " + groupModuleID, var8);
         }
      }

   }

   protected ChatRoomPrx[] getChatRooms() {
      if (this.chatRoomNames != null && this.chatRoomNames.length != 0) {
         ChatRoomPrx[] chatRoomPrxs = this.getChatRoomProxies();
         return chatRoomPrxs;
      } else {
         logger.error("No subscribed chatrooms found.");
         return null;
      }
   }

   protected void updateChatRoomDescription(String text) throws Exception {
      ChatRoomPrx[] chatRoomProxies = this.getChatRooms();
      this.updateRoomDescription(text, chatRoomProxies);
   }

   private ChatRoomPrx[] getChatRoomProxies() {
      ChatRoomPrx[] chatRoomPrxs = new ChatRoomPrx[this.chatRoomNames.length];

      try {
         if (registryPrx != null) {
            chatRoomPrxs = registryPrx.findChatRoomObjects(this.chatRoomNames);
         }
      } catch (Exception var3) {
         logger.error("One or more chat rooms in list starting'" + this.chatRoomNames[0] + "'... could not be found", var3);
      }

      return chatRoomPrxs;
   }
}
