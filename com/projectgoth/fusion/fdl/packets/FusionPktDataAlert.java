package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.HashMap;

public class FusionPktDataAlert extends FusionPacket {
   public FusionPktDataAlert() {
      super(PacketType.ALERT);
   }

   public FusionPktDataAlert(short transactionId) {
      super(PacketType.ALERT, transactionId);
   }

   public FusionPktDataAlert(FusionPacket packet) {
      super(packet);
   }

   public final FusionPktDataAlert.AlertType getAlertType() {
      return FusionPktDataAlert.AlertType.fromValue(this.getShortField((short)1));
   }

   public final void setAlertType(FusionPktDataAlert.AlertType alertType) {
      this.setField((short)1, alertType.value());
   }

   public final String getContent() {
      return this.getStringField((short)2);
   }

   public final void setContent(String content) {
      this.setField((short)2, content);
   }

   public final AlertContentType getContentType() {
      return AlertContentType.fromValue(this.getByteField((short)3));
   }

   public final void setContentType(AlertContentType contentType) {
      this.setField((short)3, contentType.value());
   }

   public final String getTitle() {
      return this.getStringField((short)4);
   }

   public final void setTitle(String title) {
      this.setField((short)4, title);
   }

   public final Short getTimeout() {
      return this.getShortField((short)5);
   }

   public final void setTimeout(short timeout) {
      this.setField((short)5, timeout);
   }

   public static enum AlertType {
      INFORMATION((short)1),
      WARNING((short)2),
      ERROR((short)3);

      private short value;
      private static final HashMap<Short, FusionPktDataAlert.AlertType> LOOKUP = new HashMap();

      private AlertType(short value) {
         this.value = value;
      }

      public short value() {
         return this.value;
      }

      public static FusionPktDataAlert.AlertType fromValue(int value) {
         return (FusionPktDataAlert.AlertType)LOOKUP.get((short)value);
      }

      public static FusionPktDataAlert.AlertType fromValue(Short value) {
         return (FusionPktDataAlert.AlertType)LOOKUP.get(value);
      }

      static {
         FusionPktDataAlert.AlertType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktDataAlert.AlertType alertType = arr$[i$];
            LOOKUP.put(alertType.value, alertType);
         }

      }
   }
}
