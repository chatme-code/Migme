package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.HashMap;

public class FusionPktDataImSessionStatus extends FusionPacket {
   public FusionPktDataImSessionStatus() {
      super(PacketType.IM_SESSION_STATUS);
   }

   public FusionPktDataImSessionStatus(short transactionId) {
      super(PacketType.IM_SESSION_STATUS, transactionId);
   }

   public FusionPktDataImSessionStatus(FusionPacket packet) {
      super(packet);
   }

   public final ImType getImType() {
      return ImType.fromValue(this.getByteField((short)1));
   }

   public final void setImType(ImType imType) {
      this.setField((short)1, imType.value());
   }

   public final FusionPktDataImSessionStatus.StatusType getStatus() {
      return FusionPktDataImSessionStatus.StatusType.fromValue(this.getByteField((short)2));
   }

   public final void setStatus(FusionPktDataImSessionStatus.StatusType status) {
      this.setField((short)2, status.value());
   }

   public final String getReason() {
      return this.getStringField((short)3);
   }

   public final void setReason(String reason) {
      this.setField((short)3, reason);
   }

   public final Boolean getSupportsConference() {
      return this.getBooleanField((short)4);
   }

   public final void setSupportsConference(boolean supportsConference) {
      this.setField((short)4, supportsConference);
   }

   public static enum StatusType {
      LOGGED_IN((byte)1),
      LOGGED_OUT((byte)2);

      private byte value;
      private static final HashMap<Byte, FusionPktDataImSessionStatus.StatusType> LOOKUP = new HashMap();

      private StatusType(byte value) {
         this.value = value;
      }

      public byte value() {
         return this.value;
      }

      public static FusionPktDataImSessionStatus.StatusType fromValue(int value) {
         return (FusionPktDataImSessionStatus.StatusType)LOOKUP.get((byte)value);
      }

      public static FusionPktDataImSessionStatus.StatusType fromValue(Byte value) {
         return (FusionPktDataImSessionStatus.StatusType)LOOKUP.get(value);
      }

      static {
         FusionPktDataImSessionStatus.StatusType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktDataImSessionStatus.StatusType statusType = arr$[i$];
            LOOKUP.put(statusType.value, statusType);
         }

      }
   }
}
