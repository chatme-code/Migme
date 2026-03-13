package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.UserPrx;
import org.apache.log4j.Logger;

public class FusionPktEmoticonHotKeysOld extends FusionPacket {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktEmoticonHotKeysOld.class));

   public FusionPktEmoticonHotKeysOld() {
      super((short)916);
   }

   public FusionPktEmoticonHotKeysOld(short transactionId) {
      super((short)916, transactionId);
   }

   public FusionPktEmoticonHotKeysOld(FusionPacket packet) {
      super(packet);
   }

   public FusionPktEmoticonHotKeysOld(short transactionId, UserPrx userPrx) {
      super((short)916, transactionId);
      if (userPrx == null) {
         log.error("UserPrx is null");
         throw new IllegalArgumentException();
      } else {
         this.setHotKeys(StringUtil.join((Object[])userPrx.getEmoticonHotKeys(), " "));
         this.setAlternateKeys(StringUtil.join((Object[])userPrx.getEmoticonAlternateKeys(), " "));
      }
   }

   public String getHotKeys() {
      return this.getStringField((short)1);
   }

   public void setHotKeys(String hotKeys) {
      this.setField((short)1, hotKeys);
   }

   public String getAlternateKeys() {
      return this.getStringField((short)2);
   }

   public void setAlternateKeys(String alternateKeys) {
      this.setField((short)2, alternateKeys);
   }
}
