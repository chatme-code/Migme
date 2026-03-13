package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import org.apache.log4j.Logger;

public class Bump extends FilteringEmoteCommand {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Bump.class));

   public Bump(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected Logger getLog() {
      return log;
   }

   protected String getRateLimitThreshold(String[] cmdArgs, MessageData messageData, ChatSource chatSource) {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.BUMP_RATE_LIMIT);
   }

   protected void checkSyntax(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws FusionException {
      if (cmdArgs.length != 2) {
         String cmd = this.getCommand(cmdArgs);
         throw new FusionException(String.format("Usage: %s [username]", cmd));
      }
   }

   protected FilteringEmoteCommand.ProcessingResult doExecute(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws EmoteCommandException {
      boolean accepted = false;

      try {
         String targetUsername = cmdArgs[1];
         String sanitizedTargetUsername = EmoteCommandUtils.getSanitizedUsername(targetUsername);
         ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().chatRoomPrx;
         ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
         chatRoomPrx.bumpUser(messageData.source, sanitizedTargetUsername);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
            ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, targetUsername, cmdArgs[0], roomData.id, roomData.groupID, -1, (String)null);
            chatSource.getSessionI().logEmoteData(logData);
         }

         return new FilteringEmoteCommand.ProcessingResult(EmoteCommand.ResultType.HANDLED_AND_STOP, accepted);
      } catch (EmoteCommandException var10) {
         throw var10;
      } catch (FusionException var11) {
         throw new EmoteCommandException(var11);
      }
   }
}
