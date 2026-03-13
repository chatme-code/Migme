package com.projectgoth.fusion.common;

import com.google.gson.Gson;
import com.projectgoth.fusion.slice.Message;
import org.apache.log4j.Logger;

public class NotificationUtils {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(NotificationUtils.class));
   public static final String COLLAPSE_INVITER_USERID = "inviterUserId";
   public static final String COLLAPSE_INVITER_USERID_KEY = "collapseInviterUserIdKey";
   public static final String COLLAPSE_SPLITE_KEY = ":";
   public static Gson gson = new Gson();

   public static Message getMessageFromString(String jsonStr) {
      Message message = null;

      try {
         message = (Message)gson.fromJson(jsonStr, Message.class);
      } catch (Exception var3) {
         log.error("Failed to convert String into Message", var3);
      }

      return message;
   }
}
