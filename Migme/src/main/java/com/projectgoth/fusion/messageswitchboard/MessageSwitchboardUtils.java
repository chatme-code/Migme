package com.projectgoth.fusion.messageswitchboard;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.slice.FusionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageSwitchboardUtils {
   private static String chatMessageBlacklistRegexString;
   private static Pattern chatMessageBlacklistRegexPattern;

   public static boolean scrubMessageText(String messageText, String senderPassword) throws FusionException {
      String messageLowerCase = messageText.toLowerCase();
      if (senderPassword != null && messageLowerCase.contains(senderPassword.toLowerCase())) {
         FusionException fe = new FusionException();

         try {
            MIS misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
            fe.message = misBean.getInfoText(22);
         } catch (Exception var5) {
            fe.message = "The message must not contain your password";
         }

         throw fe;
      } else {
         return !messageLowerCase.contains("document.cookie") && !messageLowerCase.contains("<script");
      }
   }

   public static boolean containsBlacklistedPatterns(String messageText) throws FusionException {
      if (messageText.startsWith("/")) {
         return false;
      } else {
         String trimmed_message = messageText.toLowerCase();
         trimmed_message = trimmed_message.replace(" ", "");
         String[] blacklistedPatternsSimple = SystemProperty.getArray("ChatMessageBlacklistSimple", new String[0]);

         for(int i = 0; i < blacklistedPatternsSimple.length; ++i) {
            if (trimmed_message.contains(blacklistedPatternsSimple[i].trim())) {
               return true;
            }
         }

         if (!SystemProperty.get("ChatMessageBlacklistRegex", "").equals(chatMessageBlacklistRegexString)) {
            chatMessageBlacklistRegexString = SystemProperty.get("ChatMessageBlacklistRegex", "");
            if (!StringUtil.isBlank(chatMessageBlacklistRegexString)) {
               chatMessageBlacklistRegexPattern = Pattern.compile(chatMessageBlacklistRegexString, 2);
            } else {
               chatMessageBlacklistRegexPattern = null;
            }
         }

         if (chatMessageBlacklistRegexPattern != null) {
            Matcher m = chatMessageBlacklistRegexPattern.matcher(messageText);
            if (m.matches()) {
               return true;
            }
         }

         return false;
      }
   }
}
