package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class Announce extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Announce.class));

   public Announce(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length < 2) {
         throw new FusionException("Usage: /announce [message] [time] or /announce off");
      } else {
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
         String logParameters;
         if (args.length == 2 && "off".equals(args[1])) {
            chatRoomPrx.announceOff(messageData.source);
            logParameters = "off";
         } else {
            String message = messageData.messageText.substring("/announce ".length());
            Pattern p = Pattern.compile("^(.*) ([0-9]+)$", 2);
            Matcher m = p.matcher(message);
            String announceMessage = message;
            int waitTime = -1;
            if (m.matches()) {
               announceMessage = m.group(1);
               if (announceMessage.length() > 320) {
                  throw new FusionException("Message can not be longer than 320 characters.");
               }

               String waitTimeStr = m.group(2);
               if (waitTimeStr != null) {
                  boolean waitTimeCheck = false;
                  if (waitTimeStr.length() >= 3 && waitTimeStr.length() <= 4) {
                     try {
                        waitTime = Integer.parseInt(waitTimeStr);
                        if (waitTime >= 120 && waitTime <= 3600) {
                           waitTimeCheck = true;
                        }
                     } catch (NumberFormatException var14) {
                        log.error("Unable to parse /announce wait time '" + waitTimeStr + "' to an integer", var14);
                     }
                  }

                  if (!waitTimeCheck) {
                     throw new FusionException("Incorrect time specified. Can only be from 120 to 3600.");
                  }
               }

               logParameters = "" + waitTime + " " + announceMessage;
            } else {
               logParameters = message;
            }

            chatRoomPrx.announceOn(messageData.source, announceMessage, waitTime);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
            if (logParameters.length() > 128) {
               logParameters = logParameters.substring(0, 128);
            }

            ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, "", args[0], roomData.id, roomData.groupID, -1, logParameters);
            chatSource.getSessionI().logEmoteData(logData);
         }

         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
