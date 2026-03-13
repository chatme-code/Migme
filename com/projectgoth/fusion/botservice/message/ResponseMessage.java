package com.projectgoth.fusion.botservice.message;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.common.ConfigUtils;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class ResponseMessage extends Message {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ResponseMessage.class));
   private String[] usernames;
   private String message;
   private boolean displayPopUp = false;

   public ResponseMessage(String message) {
      this.usernames = null;
      this.message = message;
   }

   public ResponseMessage(String message, boolean displayPopUp) {
      this.usernames = null;
      this.message = message;
      this.displayPopUp = displayPopUp;
   }

   public ResponseMessage(String username, String message) {
      this.usernames = new String[]{username};
      this.message = message;
   }

   public ResponseMessage(String username, String message, boolean displayPopUp) {
      this.usernames = new String[]{username};
      this.message = message;
      this.displayPopUp = displayPopUp;
   }

   public ResponseMessage(String[] usernames, String message) {
      this.usernames = usernames;
      this.message = message;
   }

   public ResponseMessage(String[] usernames, String message, boolean displayPopUp) {
      this.usernames = usernames;
      this.message = message;
      this.displayPopUp = displayPopUp;
   }

   public void dispatch(Bot bot) {
      try {
         String[] emoticonKeys = this.getEmoticonKeys(this.message, bot.getEmoticonHotKeys());
         if (this.usernames == null) {
            bot.getChannelProxy().putBotMessageToAllUsers(bot.getInstanceID(), this.message, emoticonKeys, this.displayPopUp);
         } else {
            bot.getChannelProxy().putBotMessageToUsers(bot.getInstanceID(), this.usernames, this.message, emoticonKeys, this.displayPopUp);
         }
      } catch (Exception var3) {
         log.warn("Unable to dispatch message to proxy[" + bot.getChannelProxy() + "] exception[" + var3.getMessage() + "]");
      }

   }

   private String[] getEmoticonKeys(String message, String[] emoticonHotKeys) {
      List<String> keys = new ArrayList();
      if (emoticonHotKeys != null) {
         String[] arr$ = emoticonHotKeys;
         int len$ = emoticonHotKeys.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String key = arr$[i$];
            if (message.contains(key)) {
               keys.add(key);
            }
         }
      }

      return (String[])keys.toArray(new String[keys.size()]);
   }
}
