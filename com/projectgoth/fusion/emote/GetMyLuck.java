package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class GetMyLuck extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GetMyLuck.class));
   private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();
   private static final Pattern VALUE_PATTERN = Pattern.compile("([1-5]):([1-5]):([1-5]):([1-5])", 2);

   public GetMyLuck(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      int[] luckValues = new int[4];
      String luckValue = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.EMOTE_GETMYLUCK, messageData.source);
      if (luckValue == null) {
         for(int i = 0; i < 4; ++i) {
            luckValues[i] = RANDOM_GENERATOR.nextInt(5) + 1;
         }

         luckValue = String.format("%d:%d:%d:%d", luckValues[0], luckValues[1], luckValues[2], luckValues[3]);
         if (!MemCachedClientWrapper.add(MemCachedKeySpaces.CommonKeySpace.EMOTE_GETMYLUCK, messageData.source, luckValue)) {
            luckValue = MemCachedClientWrapper.getString(MemCachedKeySpaces.CommonKeySpace.EMOTE_GETMYLUCK, messageData.source);
            if (luckValue == null) {
               log.warn(String.format("Failed to store value for /getmyluck for user '%s'", messageData.source));
               luckValue = String.format("%d:%d:%d:%d", luckValues[0], luckValues[1], luckValues[2], luckValues[3]);
            }
         }
      }

      Matcher m = VALUE_PATTERN.matcher(luckValue);
      int i;
      if (!m.matches()) {
         log.error(String.format("Incorrect luck value '%s' for user '%s', re-generating a new one.", luckValue, messageData.source));

         for(i = 0; i < 4; ++i) {
            luckValues[i] = RANDOM_GENERATOR.nextInt(5) + 1;
         }

         luckValue = String.format("%d:%d:%d:%d", luckValues[0], luckValues[1], luckValues[2], luckValues[3]);
         if (!MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.EMOTE_GETMYLUCK, messageData.source, luckValue)) {
            log.error(String.format("Failed to update re-generated luck value '%s' for '%s' to memcached.", luckValue, messageData.source));
         }
      } else {
         try {
            for(i = 0; i < 4; ++i) {
               luckValues[i] = Integer.parseInt(m.group(i + 1));
            }
         } catch (NumberFormatException var8) {
            log.error(String.format("Failed to convert the luck values in '%s' to integers for user '%s', re-generating luck values", luckValue, messageData.source));

            for(int i = 0; i < 4; ++i) {
               luckValues[i] = RANDOM_GENERATOR.nextInt(5) + 1;
            }

            luckValue = String.format("%d:%d:%d:%d", luckValues[0], luckValues[1], luckValues[2], luckValues[3]);
            if (!MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.EMOTE_GETMYLUCK, messageData.source, luckValue)) {
               log.error(String.format("Failed to update re-generated luck value '%s' for '%s' to memcached.", luckValue, messageData.source));
            }
         }
      }

      messageData.messageText = String.format(this.emoteCommandData.getMessageText(), luckValues[0], luckValues[1], luckValues[2], luckValues[3]);
      this.emoteCommandData.updateMessageData(messageData);
      chatSource.sendMessageToAllUsersInChat(messageData);
      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }
}
