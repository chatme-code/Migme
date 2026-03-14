package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FusionPktGetIMIconsOld extends FusionRequest {
   public FusionPktGetIMIconsOld() {
      super((short)926);
   }

   public FusionPktGetIMIconsOld(short transactionId) {
      super((short)926, transactionId);
   }

   public FusionPktGetIMIconsOld(FusionPacket packet) {
      super(packet);
   }

   public byte[] getIMTypes() {
      return this.getByteArrayField((short)1);
   }

   public void setIMTypes(byte[] imTypes) {
      this.setField((short)1, imTypes);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         byte[] imTypes = this.getIMTypes();
         if (imTypes != null && imTypes.length != 0) {
            List<FusionPacket> packetsToReturn = new LinkedList();
            byte[] arr$ = imTypes;
            int len$ = imTypes.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               byte imType = arr$[i$];
               ImType imEnum = ImType.fromValue(imType);
               if (imEnum == null || imEnum == ImType.FUSION) {
                  return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invalid IM type " + imType)).toArray();
               }

               String path = connection.getGateway().getIMFilePath() + File.separator + imEnum.toString().toLowerCase() + File.separator;
               if (connection.getDeviceType() == ClientType.ANDROID && connection.getClientVersion() >= 300) {
                  path = path + "android" + File.separator;
               }

               FusionPktIMIconsOld iconsPkt = new FusionPktIMIconsOld(this.transactionId);
               iconsPkt.setIMType(imType);
               iconsPkt.setOnline(ByteBufferHelper.readFile(new File(path + "Online.png")).array());
               iconsPkt.setRoaming(ByteBufferHelper.readFile(new File(path + "Roaming.png")).array());
               iconsPkt.setBusy(ByteBufferHelper.readFile(new File(path + "Busy.png")).array());
               iconsPkt.setAway(ByteBufferHelper.readFile(new File(path + "Away.png")).array());
               iconsPkt.setOffline(ByteBufferHelper.readFile(new File(path + "Offline.png")).array());
               packetsToReturn.add(iconsPkt);
            }

            return (FusionPacket[])packetsToReturn.toArray(new FusionPacket[packetsToReturn.size()]);
         } else {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "IM types not specified")).toArray();
         }
      } catch (Exception var11) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get IM icons - " + var11.getMessage())).toArray();
      }
   }
}
