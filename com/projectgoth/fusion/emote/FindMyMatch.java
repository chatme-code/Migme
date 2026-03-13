package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import org.apache.log4j.Logger;

public class FindMyMatch extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FindMyMatch.class));
   private static final String DEFAULT_FINDMYMATCH_NOMATCH_MESSAGE = "No Match - there are no other users in the chat";

   public FindMyMatch(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] allUsers = chatSource.getVisibleUsernamesInChat(false);
      if (allUsers.length == 0) {
         throw new FusionException(SystemProperty.get("EmoteFindMyMatchNoMatch", "No Match - there are no other users in the chat"));
      } else {
         int maxScore = -1;
         String maxUsername = null;
         int myCode = LoveMatch.getLoveCode(messageData.source);
         String[] arr$ = allUsers;
         int len$ = allUsers.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String username = arr$[i$];
            int curScore = LoveMatch.getLoveMatchScore(myCode, LoveMatch.getLoveCode(username));
            if (curScore > maxScore) {
               maxScore = curScore;
               maxUsername = username;
            }
         }

         messageData.messageText = String.format(this.emoteCommandData.getMessageText(), maxUsername, maxScore);
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToAllUsersInChat(messageData);
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }
}
