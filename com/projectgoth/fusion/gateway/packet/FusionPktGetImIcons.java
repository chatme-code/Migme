package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetImIcons;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class FusionPktGetImIcons extends FusionPktDataGetImIcons {
   public FusionPktGetImIcons(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetImIcons(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ImType[] imTypeList = this.getImTypeList();
         if (imTypeList != null && imTypeList.length != 0) {
            List<FusionPacket> packetsToReturn = new LinkedList();
            ImType[] arr$ = imTypeList;
            int len$ = imTypeList.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               ImType imEnum = arr$[i$];
               if (imEnum == null || imEnum == ImType.FUSION) {
                  return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Invalid IM type")).toArray();
               }

               String path = connection.getGateway().getIMFilePath() + File.separator + imEnum.toString().toLowerCase() + File.separator;
               if (connection.getDeviceType() == ClientType.ANDROID && connection.getClientVersion() >= 300) {
                  path = path + "android" + File.separator;
               }

               FusionPktImIcons iconsPkt = new FusionPktImIcons(this.transactionId);
               iconsPkt.setImType(imEnum);
               iconsPkt.setOnlineIconImageData(ByteBufferHelper.readFile(new File(path + "Online.png")).array());
               iconsPkt.setRoamingIconImageData(ByteBufferHelper.readFile(new File(path + "Roaming.png")).array());
               iconsPkt.setBusyIconImageData(ByteBufferHelper.readFile(new File(path + "Busy.png")).array());
               iconsPkt.setAwayIconImageData(ByteBufferHelper.readFile(new File(path + "Away.png")).array());
               iconsPkt.setOfflineIconImageData(ByteBufferHelper.readFile(new File(path + "Offline.png")).array());
               packetsToReturn.add(iconsPkt);
            }

            return (FusionPacket[])packetsToReturn.toArray(new FusionPacket[packetsToReturn.size()]);
         } else {
            return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "IM types not specified")).toArray();
         }
      } catch (Exception var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get IM icons - " + var10.getMessage())).toArray();
      }
   }
}
