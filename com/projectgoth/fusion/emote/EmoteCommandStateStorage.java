package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.objectcache.ChatSourceGroup;
import com.projectgoth.fusion.objectcache.ChatSourceRoom;
import com.projectgoth.fusion.objectcache.ChatSourceUser;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class EmoteCommandStateStorage {
   private Map<String, EmoteCommandState> emoteCommandStats = new HashMap();
   private static final long DEFAULT_STATE_CLEANUP_PERIOD = 60000L;
   private long lastCleanUpTime = 0L;
   private IcePrxFinder icePrxFinder;

   public EmoteCommandStateStorage(IcePrxFinder icePrxFinder) {
      this.icePrxFinder = icePrxFinder;
   }

   public EmoteCommandState getEmoteCommandState(String emoteCommand, ChatSource.ChatType chatType) {
      synchronized(this.emoteCommandStats) {
         EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(emoteCommand, chatType, this.icePrxFinder);
         EmoteCommandState s = null;
         if (ec != null) {
            String stateKey = ec.getEmoteCommandData().getCommandStateName();
            if (stateKey != null) {
               s = (EmoteCommandState)this.emoteCommandStats.get(stateKey);
               if (s == null) {
                  s = ec.createDefaultState(chatType);
                  if (s != null) {
                     this.emoteCommandStats.put(emoteCommand, s);
                  }
               }
            }
         }

         if (System.currentTimeMillis() - this.lastCleanUpTime > 60000L) {
            Iterator i$ = this.emoteCommandStats.values().iterator();

            while(i$.hasNext()) {
               EmoteCommandState si = (EmoteCommandState)i$.next();
               si.cleanUp();
            }

            this.lastCleanUpTime = System.currentTimeMillis();
         }

         return s;
      }
   }

   public void removeEmoteCommandState(String emoteCommand) {
      synchronized(this.emoteCommandStats) {
         this.emoteCommandStats.remove(emoteCommand);
      }
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, ChatSourceRoom chatRoom) throws FusionException {
      EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(emoteCommand, ChatSource.ChatType.CHATROOM_CHAT, this.icePrxFinder);
      if (ec == null) {
         return EmoteCommand.ResultType.NOTHANDLED.value();
      } else {
         EmoteCommandState ecState = this.getEmoteCommandState(emoteCommand, ChatSource.ChatType.CHATROOM_CHAT);
         return ecState != null ? ecState.execute(ec.getEmoteCommandData(), new MessageData(message), ChatSource.createChatSourceForChatRoom(sessionProxy, chatRoom)).value() : EmoteCommand.ResultType.NOTHANDLED.value();
      }
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, ChatSourceGroup chatGroup) throws FusionException {
      EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(emoteCommand, ChatSource.ChatType.GROUP_CHAT, this.icePrxFinder);
      if (ec == null) {
         return EmoteCommand.ResultType.NOTHANDLED.value();
      } else {
         EmoteCommandState ecState = this.getEmoteCommandState(emoteCommand, ChatSource.ChatType.GROUP_CHAT);
         return ecState != null ? ecState.execute(ec.getEmoteCommandData(), new MessageData(message), ChatSource.createChatSourceForGroupChat(sessionProxy, chatGroup)).value() : EmoteCommand.ResultType.NOTHANDLED.value();
      }
   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, ChatSourceUser userI) throws FusionException {
      EmoteCommand ec = EmoteCommandFactory.getEmoteCommand(emoteCommand, ChatSource.ChatType.PRIVATE_CHAT, this.icePrxFinder);
      if (ec == null) {
         return EmoteCommand.ResultType.NOTHANDLED.value();
      } else {
         EmoteCommandState ecState = this.getEmoteCommandState(emoteCommand, ChatSource.ChatType.PRIVATE_CHAT);
         return ecState != null ? ecState.execute(ec.getEmoteCommandData(), new MessageData(message), ChatSource.createChatSourceForPrivateChat(sessionProxy, userI, message.source, message.messageDestinations[0].destination)).value() : EmoteCommand.ResultType.NOTHANDLED.value();
      }
   }
}
