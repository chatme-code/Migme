package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.fdl.packets.FusionPktDataGetEmoticonHotkeys;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;

public class FusionPktGetEmoticonHotkeys extends FusionPktDataGetEmoticonHotkeys {
   public FusionPktGetEmoticonHotkeys(short transactionId) {
      super(transactionId);
   }

   public FusionPktGetEmoticonHotkeys(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetEmoticonHotkeys(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktEmoticonHotkeys emoticonHotKeys = new FusionPktEmoticonHotkeys(this.getTransactionId(), connection.getUserPrx());
      return emoticonHotKeys.toArray();
   }
}
