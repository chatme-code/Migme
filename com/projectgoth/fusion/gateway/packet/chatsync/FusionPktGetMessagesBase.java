package com.projectgoth.fusion.gateway.packet.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import org.apache.log4j.Logger;

public abstract class FusionPktGetMessagesBase extends FusionRequest {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetMessagesBase.class));
   private static final short CHAT_ID_FIELD = 1;
   private static final short CHAT_TYPE_FIELD = 2;
   private static final short LATEST_MESSAGE_TIMESTAMP_FIELD = 3;
   private static final short OLDEST_MESSAGE_TIMESTAMP_FIELD = 4;
   private static final short LIMIT_FIELD = 5;

   public FusionPktGetMessagesBase(short packetType) {
      super(packetType);
   }

   public FusionPktGetMessagesBase(short packetType, short transactionId) {
      super(packetType, transactionId);
   }

   public FusionPktGetMessagesBase(FusionPacket packet) {
      super(packet);
   }

   public String getChatIdentifier() {
      return this.getStringField((short)1);
   }

   public void setChatIdentifier(String id) {
      this.setField((short)1, id);
   }

   public Byte getChatType() {
      return this.getByteField((short)2);
   }

   public void setChatType(byte chatType) {
      this.setField((short)2, chatType);
   }

   public Long getLatestMessageTimestamp() {
      return this.getLongField((short)3);
   }

   public void setLatestMessageTimestamp(long lmts) {
      this.setField((short)3, lmts);
   }

   public Long getOldestMessageTimestamp() {
      return this.getLongField((short)4);
   }

   public void setOldestMessageTimestamp(long omts) {
      this.setField((short)4, omts);
   }

   public Integer getLimit() {
      return this.getIntField((short)5);
   }

   public void setLimit(int limit) {
      this.setField((short)5, limit);
   }

   public boolean sessionRequired() {
      return true;
   }
}
