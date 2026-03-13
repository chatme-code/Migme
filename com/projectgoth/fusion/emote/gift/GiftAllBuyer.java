package com.projectgoth.fusion.emote.gift;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.apache.log4j.Logger;

public class GiftAllBuyer implements Callable<Integer>, Serializable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GiftAllBuyer.class));
   private String buyerUsername;
   private String recipient;
   private UserData recipientUserData;
   private VirtualGiftData gift;
   private int purchaseLocation;
   private boolean privateGift;
   private String message;
   private AccountEntrySourceData accountEntrySourceData;
   private boolean isGiftShower;
   private boolean sendGiftShowerEvent;
   private Integer totalGiftShowerRecipients;
   private IcePrxFinder icePrxFinder;
   private String recipientWithVGReceivedIDMapGUID;

   public GiftAllBuyer(String buyerUsername, String recipient, UserData recipientUserData, VirtualGiftData gift, int purchaseLocation, boolean privateGift, String message, AccountEntrySourceData accountEntrySourceData, boolean isGiftShower, boolean sendGiftShowerEvent, Integer totalGiftShowerRecipients, IcePrxFinder icePrxFinder, String recipientWithVGReceivedIDMapGUID) {
      if (log.isDebugEnabled()) {
         log.debug("Constructing GiftAllBuyer: buyer=" + buyerUsername + " recipient=" + recipientUserData);
      }

      this.buyerUsername = buyerUsername;
      this.recipient = recipient;
      this.recipientUserData = recipientUserData;
      this.gift = gift;
      this.purchaseLocation = purchaseLocation;
      this.privateGift = privateGift;
      this.message = message;
      this.accountEntrySourceData = accountEntrySourceData;
      this.isGiftShower = isGiftShower;
      this.sendGiftShowerEvent = sendGiftShowerEvent;
      this.totalGiftShowerRecipients = totalGiftShowerRecipients;
      this.icePrxFinder = icePrxFinder;
      this.recipientWithVGReceivedIDMapGUID = recipientWithVGReceivedIDMapGUID;
   }

   public Integer call() {
      try {
         if (log.isDebugEnabled()) {
            log.debug("GiftAllBuyer.call: buyer=" + this.buyerUsername + " recipient=" + this.recipientUserData);
         }

         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         int virtualGiftReceivedID = contentEJB.recordVirtualGift(this.buyerUsername, this.recipient, this.gift, this.purchaseLocation, this.privateGift, this.message);
         GiftNotifier notifier = new GiftNotifier(this.buyerUsername, virtualGiftReceivedID, this.recipientUserData, this.gift, this.privateGift, this.message, (String)null, this.isGiftShower, this.sendGiftShowerEvent, this.totalGiftShowerRecipients, this.icePrxFinder);
         Future<Boolean> ignoredFuture = GiftAsync.queueWorker(notifier);
         if (log.isDebugEnabled()) {
            log.debug("GiftAllBuyer.call: queued GiftNotifier");
         }

         Map<String, Integer> results = GiftAsync.getGiftGrid().getStringIntMap(this.recipientWithVGReceivedIDMapGUID);
         results.put(this.recipient, virtualGiftReceivedID);
         return virtualGiftReceivedID;
      } catch (Exception var6) {
         log.error("Exception in GiftAllBuyer.call: e=" + var6, var6);
         return null;
      }
   }
}
