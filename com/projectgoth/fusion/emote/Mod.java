package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class Mod extends FilteringEmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Mod.class));
   private static final String LIST_COMMAND = "list";

   public Mod(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected String getRateLimitThreshold(String[] cmdArgs, MessageData messageData, ChatSource chatSource) {
      SystemPropertyEntities.Emote rateLimiterPropertyEntity;
      if (cmdArgs[1].equalsIgnoreCase("list")) {
         rateLimiterPropertyEntity = SystemPropertyEntities.Emote.LIST_MODERATOR_RATE_LIMIT;
      } else {
         rateLimiterPropertyEntity = SystemPropertyEntities.Emote.ADD_MODERATOR_RATE_LIMIT;
      }

      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)rateLimiterPropertyEntity);
   }

   protected String getInstigatorRateLimitLocalKeySuffix(String[] cmdArgs, MessageData messageData, ChatSource chatSource) {
      return cmdArgs[1].equalsIgnoreCase("list") ? "L" : "A";
   }

   protected void checkSyntax(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws FusionException {
      if (cmdArgs.length != 2) {
         String cmd = this.getCommand(cmdArgs);
         throw new FusionException(String.format("Usage: %s [username] or %s list", cmd, cmd));
      }
   }

   protected FilteringEmoteCommand.ProcessingResult addModerator(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws EmoteCommandException, CreateException, RemoteException, FusionException {
      boolean accepted = false;
      String targetUsername = cmdArgs[1];
      String sanitizedTargetUsername = EmoteCommandUtils.getSanitizedUsername(targetUsername);
      ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().chatRoomPrx;
      ChatRoomDataIce roomDataIce = chatRoomPrx.getRoomData();
      chatRoomPrx.addGroupModerator(messageData.source, sanitizedTargetUsername);
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
         ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, targetUsername, cmdArgs[0], roomDataIce.id, roomDataIce.groupID, -1, (String)null);
         chatSource.getSessionI().logEmoteData(logData);
      }

      return new FilteringEmoteCommand.ProcessingResult(EmoteCommand.ResultType.HANDLED_AND_STOP, accepted);
   }

   protected FilteringEmoteCommand.ProcessingResult getModerators(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws EmoteCommandException, CreateException, RemoteException, FusionException {
      ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().chatRoomPrx;
      ChatRoomDataIce roomDataIce = chatRoomPrx.getRoomData();
      String[] moderators = chatRoomPrx.getGroupModerators(messageData.source);
      messageData.messageText = StringUtil.truncateWithEllipsis(String.format("The following users are group moderators:- %s.", StringUtil.join((Object[])moderators, "|")), SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.MAX_MODERATOR_LIST_LENGTH_DISPLAY));
      this.emoteCommandData.updateMessageData(messageData);
      chatSource.sendMessageToSender(messageData);
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
         ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, cmdArgs[1], cmdArgs[0], roomDataIce.id, roomDataIce.groupID, -1, (String)null);
         chatSource.getSessionI().logEmoteData(logData);
      }

      return new FilteringEmoteCommand.ProcessingResult(EmoteCommand.ResultType.HANDLED_AND_STOP, true);
   }

   protected FilteringEmoteCommand.ProcessingResult doExecute(String[] cmdArgs, MessageData messageData, ChatSource chatSource) throws EmoteCommandException {
      try {
         return cmdArgs[1].equalsIgnoreCase("list") ? this.getModerators(cmdArgs, messageData, chatSource) : this.addModerator(cmdArgs, messageData, chatSource);
      } catch (EmoteCommandException var5) {
         throw var5;
      } catch (FusionException var6) {
         throw new EmoteCommandException(var6);
      } catch (RemoteException var7) {
         throw new EmoteCommandException(var7);
      } catch (CreateException var8) {
         throw new EmoteCommandException(var8);
      }
   }

   protected Logger getLog() {
      return log;
   }
}
