package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class FusionPktDataReport extends FusionRequest {
   public FusionPktDataReport() {
      super(PacketType.REPORT);
   }

   public FusionPktDataReport(short transactionId) {
      super(PacketType.REPORT, transactionId);
   }

   public FusionPktDataReport(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataReport(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return false;
   }

   public final FusionPktDataReport.ReportType getReportType() {
      return FusionPktDataReport.ReportType.fromValue(this.getShortField((short)1));
   }

   public final void setReportType(FusionPktDataReport.ReportType reportType) {
      this.setField((short)1, reportType.value());
   }

   public final String getDescription() {
      return this.getStringField((short)2);
   }

   public final void setDescription(String description) {
      this.setField((short)2, description);
   }

   public static enum ReportType {
      CONNECTION_STATISTICS((short)1);

      private short value;
      private static final HashMap<Short, FusionPktDataReport.ReportType> LOOKUP = new HashMap();

      private ReportType(short value) {
         this.value = value;
      }

      public short value() {
         return this.value;
      }

      public static FusionPktDataReport.ReportType fromValue(int value) {
         return (FusionPktDataReport.ReportType)LOOKUP.get((short)value);
      }

      public static FusionPktDataReport.ReportType fromValue(Short value) {
         return (FusionPktDataReport.ReportType)LOOKUP.get(value);
      }

      static {
         FusionPktDataReport.ReportType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktDataReport.ReportType reportType = arr$[i$];
            LOOKUP.put(reportType.value, reportType);
         }

      }
   }
}
