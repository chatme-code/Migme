package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.packet.chatsync.FusionPktChat;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;

public class ChatRenamer implements ChatSyncEntity {
   private static final LogFilter log;
   private ChatDefinition chatDef;
   private String chatName;
   private RegistryPrx registryPrx;
   private String renamingUser;

   public ChatRenamer(ChatDefinition chatDef, String chatName, RegistryPrx registryPrx, String renamingUser) throws FusionException {
      this.chatDef = chatDef;
      this.chatName = chatName;
      this.registryPrx = registryPrx;
      this.renamingUser = renamingUser;
   }

   public void store(ChatSyncStore[] stores) throws FusionException {
      ChatSyncStore store = stores[0];
      this.chatDef.retrieve(stores);
      this.chatDef.setChatName(this.chatName);
      this.chatDef.store(stores);

      try {
         this.informParticipants(this.chatDef, stores);
      } catch (Exception var4) {
         log.error("Unable to inform participants of renaming of chat=" + this.chatDef, var4);
      }

   }

   private void informParticipants(ChatDefinition def, ChatSyncStore[] stores) throws Exception {
      if (stores != null && stores.length != 0) {
         ChatSyncStore store = stores[0];
         Set<String> participants = new HashSet(Arrays.asList(def.getParticipantUsernames()));
         participants.remove(this.renamingUser);
         ArrayList<ChatSyncPipelineOp> readOps = new ArrayList();
         Map<String, UserPrx> users = new HashMap();
         Map<String, ChatListVersion> chatListVersions = new HashMap();
         Iterator i$ = participants.iterator();

         String username;
         UserPrx user;
         ChatListVersion clv;
         while(i$.hasNext()) {
            username = (String)i$.next();

            try {
               user = this.registryPrx.findUserObject(username);
               clv = new ChatListVersion(user.getUserData().userID);
               readOps.add(new ChatSyncPipelineOp(clv, ChatSyncPipelineOp.OpType.READ));
               users.put(username, user);
               chatListVersions.put(username, clv);
            } catch (Exception var13) {
               log.warn("Unable to forward chat renaming to user=" + username + " (couldnt get userprx)", var13);
            }
         }

         store.setSlave();
         store.pipelinedBinary((List)readOps);
         i$ = participants.iterator();

         while(i$.hasNext()) {
            username = (String)i$.next();

            try {
               user = (UserPrx)users.get(username);
               clv = (ChatListVersion)chatListVersions.get(username);
               this.forwardToUser(def, username, user, clv);
            } catch (Exception var12) {
               log.warn("Unable to forward chat renaming to user=" + username, var12);
            }
         }

      } else {
         log.error("Invalid chatsync stores passed to informParticipants");
      }
   }

   private void forwardToUser(ChatDefinition def, String username, UserPrx user, ChatListVersion clv) throws Exception {
      SessionPrx[] sessions = user.getSessions();
      FusionPktChat chat = def.toFusionPktChat((short)0, clv.getVersion(), System.currentTimeMillis(), username);
      SessionPrx[] arr$ = sessions;
      int len$ = sessions.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SessionPrx sessionPrx = arr$[i$];

         try {
            sessionPrx.putSerializedPacket(chat.toSerializedBytes());
            if (log.isDebugEnabled()) {
               log.debug("Forwarded renaming of chatID=" + this.chatDef + " ok to " + username + " at session=" + sessionPrx);
            }
         } catch (Exception var12) {
            log.warn("Unable to forward chat renaming to session=" + sessionPrx + " of user=" + username, var12);
         }
      }

   }

   public String getKey() {
      return this.chatDef.getKey();
   }

   public ChatSyncEntity.ChatSyncEntityType getEntityType() {
      return ChatSyncEntity.ChatSyncEntityType.CONVERSATION;
   }

   public String getValue() {
      return null;
   }

   public void unpack(String storedKey, String storedValue) throws FusionException {
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
   }

   public void retrievePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int loadPipeline(List<Object> pipelineResults, int startIndex) throws FusionException {
      return 0;
   }

   public void storePipeline(ChatSyncStore pipelineStore) throws FusionException {
   }

   public int processPipelineStoreResults(List<Object> results, int startIndex) throws FusionException {
      return 0;
   }

   public boolean canRetryReads() throws FusionException {
      return false;
   }

   public boolean canRetryWrites() throws FusionException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.RETRY_RENAME_CHAT_ENABLED);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatRenamer.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
