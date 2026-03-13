package com.projectgoth.fusion.emote.gift;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

public class GiftAllBiller implements Callable<Boolean>, Serializable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GiftAllBiller.class));
   private final String buyerUsername;
   private final VirtualGiftData gift;
   private final int recipientCount;
   private final String recipientWithVGReceivedIDMapGUID;
   private final AccountEntrySourceData accountEntrySourceData;

   public GiftAllBiller(String buyerUsername, VirtualGiftData gift, int recipientCount, String recipientWithVGReceivedIDMapGUID, AccountEntrySourceData accountEntrySourceData) {
      if (log.isDebugEnabled()) {
         log.debug("Constructing GiftAllBiller: buyer=" + buyerUsername + " gift=" + gift);
      }

      this.buyerUsername = buyerUsername;
      this.gift = gift;
      this.recipientCount = recipientCount;
      this.recipientWithVGReceivedIDMapGUID = recipientWithVGReceivedIDMapGUID;
      this.accountEntrySourceData = accountEntrySourceData;
   }

   public Boolean call() {
      Boolean var2;
      try {
         if (log.isDebugEnabled()) {
            log.debug("GiftAllBiller.call: buyer=" + this.buyerUsername + " gift=" + this.gift + " no of recipients=" + this.recipientCount);
         }

         int timeoutSeconds = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GiftSettings.FUTURE_TIMEOUT_SECONDS);
         Map<String, Integer> results = GiftAsync.getGiftGrid().getStringIntMap(this.recipientWithVGReceivedIDMapGUID);
         long endTime = System.currentTimeMillis() + (long)timeoutSeconds * 1000L;

         do {
            try {
               Thread.sleep(500L);
            } catch (Exception var13) {
            }
         } while(results.size() < this.recipientCount && System.currentTimeMillis() < endTime);

         if (results.size() < this.recipientCount) {
            log.warn("GiftAllBiller timed out waiting for all recipients to receive their gifts: discrepancy=" + (this.recipientCount - results.size()));
         }

         HashMap<String, Integer> resultsClone = new HashMap();
         resultsClone.putAll(results);
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         contentEJB.billVirtualGiftForMultipleUsers(this.buyerUsername, this.gift, resultsClone, this.accountEntrySourceData);
         if (log.isDebugEnabled()) {
            log.debug("GiftAllBiller.call: billed virtual gift");
         }

         Boolean var7 = true;
         return var7;
      } catch (Exception var14) {
         log.error("Exception in GiftAllBiller: e=" + var14, var14);
         var2 = false;
      } finally {
         GiftAsync.getGiftGrid().destroyMap(this.recipientWithVGReceivedIDMapGUID);
      }

      return var2;
   }
}
