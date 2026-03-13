package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

public class Flames extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Flames.class));
   private static final String DEFAULT_NO_MATCH_MESSAGE = "**Too bad, %s and %s are not a match**";
   public static final String[][] FLAMES_VALUES = new String[][]{{"S", "Sis/Bro"}, {"F", "Friendship"}, {"L", "Love"}, {"A", "Admiration"}, {"M", "Marriage"}, {"E", "Enemy"}};

   public Flames(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   public static int getFlamesScore(String username1, String username2) {
      Map<Character, Integer> occurrenceUser1 = new HashMap();
      char[] arr$ = username1.toCharArray();
      int total = arr$.length;

      int len$;
      for(len$ = 0; len$ < total; ++len$) {
         char c = arr$[len$];
         if (occurrenceUser1.containsKey(c)) {
            occurrenceUser1.put(c, (Integer)occurrenceUser1.get(c) + 1);
         } else {
            occurrenceUser1.put(c, 1);
         }
      }

      Map<Character, Integer> occurrenceCommon = new HashMap();
      char[] arr$ = username2.toCharArray();
      len$ = arr$.length;

      int i;
      for(i = 0; i < len$; ++i) {
         char c = arr$[i];
         if (occurrenceUser1.containsKey(c)) {
            if (occurrenceCommon.containsKey(c)) {
               occurrenceCommon.put(c, (Integer)occurrenceCommon.get(c) + 1);
            } else {
               occurrenceCommon.put(c, (Integer)occurrenceUser1.get(c) + 1);
            }
         }
      }

      total = 0;

      for(Iterator i$ = occurrenceCommon.values().iterator(); i$.hasNext(); total += i) {
         i = (Integer)i$.next();
      }

      return total;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      EmoteCommandUtils.TwoUsernameCommand tuc = EmoteCommandUtils.parseTwoUsernameCommand(this.emoteCommandData.getCommandName(), messageData, chatSource, false);
      int flameScore = getFlamesScore(tuc.username1, tuc.username2);
      if (flameScore == 0) {
         messageData.messageText = String.format("**Too bad, %s and %s are not a match**", tuc.username1, tuc.username2);
      } else {
         String[] values = FLAMES_VALUES[flameScore % FLAMES_VALUES.length];
         messageData.messageText = String.format(this.emoteCommandData.getMessageText(), tuc.username1, tuc.username2, values[0], values[1]);
      }

      this.emoteCommandData.updateMessageData(messageData);
      chatSource.sendMessageToAllUsersInChat(messageData);
      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }
}
