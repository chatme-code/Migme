package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import org.apache.log4j.Logger;

public class LoveMatch extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(LoveMatch.class));
   private static final int LOVEMATCH_DIVISOR = 101;

   public LoveMatch(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   public static int getLoveCode(String username) {
      if (username == null) {
         return 0;
      } else {
         String v = username.trim().toLowerCase();
         if (v.length() == 0) {
            return 0;
         } else {
            int code = 0;
            char[] arr$ = v.toCharArray();
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               char c = arr$[i$];
               code = (code + c) % 101;
            }

            code %= 101;
            if (code == 100) {
               ++code;
            }

            return code;
         }
      }
   }

   public static int getLoveMatchScore(int code1, int code2) {
      return (code2 * code1 + code1 + code2) % 101;
   }

   public static int getLoveMatchScore(String username1, String username2) {
      return getLoveMatchScore(getLoveCode(username1), getLoveCode(username2));
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      EmoteCommandUtils.TwoUsernameCommand tuc = EmoteCommandUtils.parseTwoUsernameCommand(this.emoteCommandData.getCommandName(), messageData, chatSource, false);
      int loveCode = getLoveMatchScore(tuc.username1, tuc.username2);
      messageData.messageText = String.format(this.emoteCommandData.getMessageText(), tuc.username1, tuc.username2, loveCode);
      this.emoteCommandData.updateMessageData(messageData);
      chatSource.sendMessageToAllUsersInChat(messageData);
      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }
}
