package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.fdl.packets.FusionPktDataLanguage;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import org.apache.log4j.Logger;

public class FusionPktLanguage extends FusionPktDataLanguage {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktLanguage.class));

   public FusionPktLanguage(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktLanguage(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError = null;

      try {
         log.debug("Setting Language to [" + this.getLanguage() + "]");
         FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), new FloodControl.Action[]{FloodControl.Action.DEFAULT_DAILY, FloodControl.Action.DEFAULT_PER_MINUTE});
         connection.setLanguage(this.getLanguage());
         connection.getSessionPrx().setLanguage(this.getLanguage());
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (Exception var4) {
         log.error("FusionPktLanguage error: [" + connection.getUsername() + "] [" + this.getLanguage() + "]", var4);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to process request. Please try again later.");
         return pktError.toArray();
      }
   }
}
