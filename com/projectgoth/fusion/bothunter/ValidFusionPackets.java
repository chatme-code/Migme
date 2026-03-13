package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.packet.FusionPacket;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class ValidFusionPackets {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ValidFusionPackets.class));
   private final ConcurrentHashMap<Short, String> validTypes;

   private ValidFusionPackets() {
      this.validTypes = new ConcurrentHashMap();

      try {
         this.populate();
      } catch (Exception var2) {
         log.error("Unable to populate ValidFusionPackets: e=" + var2);
      }

   }

   public static ValidFusionPackets getInstance() {
      return ValidFusionPackets.SingletonHolder.INSTANCE;
   }

   private void populate() throws Exception {
      int modMask = true;
      Field[] fields = FusionPacket.class.getDeclaredFields();
      Field[] arr$ = fields;
      int len$ = fields.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Field f = arr$[i$];

         try {
            if ((f.getModifiers() & 25) == 25) {
               boolean equality = f.getType() != null && f.getType().equals(Short.TYPE);
               if (equality) {
                  f.setAccessible(true);
                  Short value = (Short)f.get((Object)null);
                  this.validTypes.put(value, f.getName());
                  if (log.isDebugEnabled()) {
                     log.debug("Adding fusion packet=" + value + " " + f.getName());
                  }
               }
            }
         } catch (Exception var9) {
            log.error("Exception parsing field=" + f.getName() + " e=" + var9);
         }
      }

      log.info("Found " + this.validTypes.size() + " fusion packet types");
   }

   public boolean contains(short packetType) {
      return this.validTypes.get(packetType) != null;
   }

   // $FF: synthetic method
   ValidFusionPackets(Object x0) {
      this();
   }

   private static class SingletonHolder {
      public static final ValidFusionPackets INSTANCE = new ValidFusionPackets();
   }
}
