package com.projectgoth.fusion.gateway.packet.chatsync;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public class FusionPktLatestMessagesDigest extends FusionPacket {
   private static final Logger log = Logger.getLogger(FusionPktLatestMessagesDigest.class);

   public FusionPktLatestMessagesDigest(FusionPacket packet) {
      super(packet);
   }

   public FusionPktLatestMessagesDigest(short transactionId, FusionPktLatestMessagesDigest.LatestMessage[] contents) {
      super((short)563, transactionId);
      String[] chatIDs = new String[contents.length];
      String[] guids = new String[contents.length];
      String[] timestamps = new String[contents.length];
      byte[] chatTypes = new byte[contents.length];
      String[] messageContents = new String[contents.length];
      int snippetMaxLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ChatSyncSettings.LATEST_MESSAGES_DIGEST_SNIPPET_MAX_LENGTH);
      int i = 0;
      FusionPktLatestMessagesDigest.LatestMessage[] arr$ = contents;
      int len$ = contents.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         FusionPktLatestMessagesDigest.LatestMessage msg = arr$[i$];
         chatIDs[i] = msg.getChatID();
         if (msg.getGUID() != null) {
            guids[i] = msg.getGUID();
         } else {
            log.warn("Null guid for message in chatID=" + msg.getChatID() + " with timestamp=" + msg.getTimestamp());
            guids[i] = "";
         }

         timestamps[i] = Long.toString(msg.getTimestamp());
         chatTypes[i] = msg.getChatType();
         String snippet = msg.getMessageContent();
         if (snippet != null && snippet.length() > snippetMaxLength) {
            snippet = snippet.substring(0, snippetMaxLength);
         }

         if (null != snippet) {
            messageContents[i] = snippet;
         } else {
            log.warn("Null snippet for message in chatID=" + msg.getChatID() + " with timestamp=" + msg.getTimestamp());
            messageContents[i] = "";
         }

         ++i;
      }

      this.setField((short)1, chatIDs);
      this.setField((short)2, guids);
      this.setField((short)3, timestamps);
      this.setField((short)4, chatTypes);
      this.setField((short)5, messageContents);
   }

   public String[] getChatIDs() {
      return super.getStringArrayField((short)1);
   }

   public String[] getGUIDs() {
      return super.getStringArrayField((short)2);
   }

   public String[] getTimestamps() {
      return super.getStringArrayField((short)3);
   }

   public byte[] getChatTypes() {
      return super.getByteArrayField((short)4);
   }

   public String[] getMessageContents() {
      return super.getStringArrayField((short)5);
   }

   public static class LatestMessage {
      private final String externalChatID;
      private final String guid;
      private final long timestamp;
      private final byte chatType;
      private final String messageContent;

      public LatestMessage(String externalChatID, String guid, long timestamp, byte chatType, String msgContent) {
         this.externalChatID = externalChatID;
         this.guid = guid;
         this.timestamp = timestamp;
         this.chatType = chatType;
         this.messageContent = msgContent;
      }

      public String getChatID() {
         return this.externalChatID;
      }

      public String getGUID() {
         return this.guid;
      }

      public long getTimestamp() {
         return this.timestamp;
      }

      public byte getChatType() {
         return this.chatType;
      }

      public String getMessageContent() {
         return this.messageContent;
      }
   }
}
