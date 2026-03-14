package com.projectgoth.fusion.gateway.packet;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.projectgoth.fusion.chatsync.MessageStatusEvent;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.ArrayList;
import java.util.List;

public class FusionPktMessageStatusEvents extends FusionPacket {
   public FusionPktMessageStatusEvents() {
      super((short)506);
   }

   public FusionPktMessageStatusEvents(short transactionId) {
      super((short)506, transactionId);
   }

   public FusionPktMessageStatusEvents(FusionPacket packet) {
      super(packet);
   }

   public FusionPktMessageStatusEvents(MessageStatusEvent[] events, short requestTxnId) {
      super((short)506);
      this.setTransactionId(requestTxnId);
      byte messageType = 0;
      byte destinationType = 0;
      List<String> sources = new ArrayList();
      List<String> destinations = new ArrayList();
      List<String> guids = new ArrayList();
      List<Byte> statuses = new ArrayList();
      List<Byte> serverGeneratedFlags = new ArrayList();
      List<Long> messageTimestamps = new ArrayList();
      MessageStatusEvent[] arr$ = events;
      int len$ = events.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageStatusEvent mse = arr$[i$];
         messageType = mse.getMessageType().value();
         sources.add(mse.getMessageSource());
         destinationType = (byte)mse.getMessageDestinationType().value();
         destinations.add(mse.getMessageDestination());
         guids.add(mse.getMessageGUID());
         statuses.add((byte)mse.getMessageStatus().value());
         serverGeneratedFlags.add(Byte.valueOf((byte)(mse.getServerGenerated() ? 1 : 0)));
         messageTimestamps.add(mse.getMessageTimestamp());
      }

      this.setMessageType(messageType);
      this.setMessageSources((String[])sources.toArray(new String[sources.size()]));
      this.setMessageDestinationType(destinationType);
      this.setMessageDestinations((String[])destinations.toArray(new String[destinations.size()]));
      this.setMessageGUIDs((String[])guids.toArray(new String[guids.size()]));
      this.setMessageEventTypes(Bytes.toArray(statuses));
      this.setServerGeneratedFlags(Bytes.toArray(serverGeneratedFlags));
      this.setMessageTimestamps(Longs.toArray(messageTimestamps));
   }

   public byte getMessageType() {
      return this.getByteField((short)1);
   }

   public void setMessageType(byte messageType) {
      this.setField((short)1, messageType);
   }

   public String[] getMessageSources() {
      return this.getStringArrayField((short)2);
   }

   public void setMessageSources(String[] source) {
      this.setField((short)2, source);
   }

   public Byte getMessageDestinationType() {
      return this.getByteField((short)3);
   }

   public void setMessageDestinationType(byte destinationType) {
      this.setField((short)3, destinationType);
   }

   public String[] getMessageDestinations() {
      return this.getStringArrayField((short)4);
   }

   public void setMessageDestinations(String[] destinations) {
      this.setField((short)4, destinations);
   }

   public String[] getMessageGUIDs() {
      return this.getStringArrayField((short)5);
   }

   public void setMessageGUIDs(String[] guids) {
      this.setField((short)5, guids);
   }

   public byte[] getMessageEventTypes() {
      return this.getByteArrayField((short)6);
   }

   public void setMessageEventTypes(byte[] types) {
      this.setField((short)6, types);
   }

   public byte[] getServerGeneratedFlags() {
      return this.getByteArrayField((short)7);
   }

   public void setServerGeneratedFlags(byte[] flags) {
      this.setField((short)7, flags);
   }

   public long[] getMessageTimestamps() {
      return this.getLongArrayField((short)8);
   }

   public void setMessageTimestamps(long[] timestamps) {
      this.setField((short)8, timestamps);
   }
}
