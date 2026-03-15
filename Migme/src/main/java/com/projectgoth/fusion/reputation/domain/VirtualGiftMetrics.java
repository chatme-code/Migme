package com.projectgoth.fusion.reputation.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VirtualGiftMetrics implements Metrics {
   private static final Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(VirtualGiftMetrics.class));
   private String username;
   private int virtualGiftsReceived;
   private int virtualGiftsSent;

   public void reset(String username) {
      this.username = username;
      this.virtualGiftsReceived = 0;
      this.virtualGiftsSent = 0;
   }

   public String getUsername() {
      return this.username;
   }

   public int getVirtualGiftsReceived() {
      return this.virtualGiftsReceived;
   }

   public int getVirtualGiftsSent() {
      return this.virtualGiftsSent;
   }

   public void addVirtualGiftsReceived(int gifts) {
      this.virtualGiftsReceived += gifts;
   }

   public void addVirtualGiftsSent(int gifts) {
      this.virtualGiftsSent += gifts;
   }

   public boolean hasMetrics() {
      return this.virtualGiftsReceived != 0 || this.virtualGiftsSent != 0;
   }

   public String toLine() {
      StringBuilder builder = new StringBuilder();
      builder.append(this.username).append(',');
      if (this.virtualGiftsReceived > 0) {
         builder.append(this.virtualGiftsReceived);
      } else if (this.virtualGiftsSent > 0) {
         builder.append(this.virtualGiftsSent);
      } else {
         log.warn("virtualGiftsReceived and virtualGiftsSent == 0?");
      }

      return builder.toString();
   }
}
