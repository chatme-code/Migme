package com.projectgoth.fusion.botservice.bot;

import com.projectgoth.fusion.botservice.BotService;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.PotData;
import com.projectgoth.fusion.data.PotStakeData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.Logger;

public class Pot {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Pot.class));
   private PotData potData;
   private Bot bot;
   private Map<String, PotStakeData> stakes = new HashMap();
   Account accountEJB;

   public Pot(Bot bot) throws Exception {
      this.bot = bot;
      this.accountEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
      this.potData = this.accountEJB.createPot((int)bot.getBotData().getId(), bot.getInstanceID());
      if (log.isDebugEnabled()) {
         log.debug("Creating the pot " + this.potData.getId() + " for the game " + bot.getBotData().getDisplayName());
      }

   }

   public int getPotID() {
      return this.potData.getId();
   }

   public synchronized void enterPlayer(String username, double amount, String currency) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug(username + " enters the pot " + this.potData.getId() + " with " + amount + " " + currency);
      }

      PotStakeData stake = this.accountEJB.enterUserIntoPot(this.bot.getBotData().getDisplayName(), (int)this.bot.getBotData().getId(), this.potData.getId(), username, amount, currency, new AccountEntrySourceData(BotService.class));
      this.stakes.put(username, stake);
      if (log.isDebugEnabled()) {
         log.debug("New pot: " + this.toString());
      }

   }

   public synchronized void removePlayer(String username) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("Removing " + username + " from the pot " + this.potData.getId());
      }

      PotStakeData stake = (PotStakeData)this.stakes.get(username);
      if (stake == null) {
         log.warn("Request to remove " + username + " from the pot " + this.potData.getId() + " and no stake for the user exists");
      } else {
         this.accountEJB.removeUserFromPot(stake.getId());
         stake.setEligible(false);
         if (log.isDebugEnabled()) {
            log.debug("New pot: " + this.toString());
         }

      }
   }

   public double payout(boolean cancelOnException) throws Exception {
      try {
         return this.payout();
      } catch (Throwable var3) {
         log.error("payout for bot:[" + this.bot.getBotData().getId() + "] pot:[" + this.potData.getId() + "] failed. Exception:" + var3, var3);
         if (cancelOnException) {
            this.cancel();
         }

         throw var3 instanceof Exception ? (Exception)var3 : new Exception("Throwable error detected:" + var3, var3);
      }
   }

   public double payout(String username, boolean cancelOnException) throws Exception {
      if (!this.stakes.containsKey(username)) {
         throw new Exception("User is not eligible for the pot");
      } else {
         Iterator i$ = this.stakes.keySet().iterator();

         while(i$.hasNext()) {
            String u = (String)i$.next();
            if (!u.equalsIgnoreCase(username)) {
               this.removePlayer(u);
            }
         }

         return this.payout(cancelOnException);
      }
   }

   public synchronized double payout() throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("Paying out the pot " + this.potData.getId());
      }

      return this.accountEJB.payoutPotAndNotify(this.bot.getBotData().getDisplayName(), this.potData.getId(), new AccountEntrySourceData(BotService.class));
   }

   public synchronized double payout(String username) throws Exception {
      try {
         return this.payout(username, false);
      } catch (Exception var3) {
         log.error("Payout to user [" + username + "] failed.Exception:" + var3, var3);
         throw var3;
      }
   }

   public void cancel() throws Exception {
      this.accountEJB.cancelPot(this.potData.getId(), new AccountEntrySourceData(BotService.class));
   }

   public double getTotalAmountInBaseCurrency() {
      double totalAmount = 0.0D;

      PotStakeData stake;
      for(Iterator i$ = this.stakes.values().iterator(); i$.hasNext(); totalAmount += stake.getAmountInBaseCurrency()) {
         stake = (PotStakeData)i$.next();
      }

      return totalAmount;
   }

   public double getCurrentPayout(String currency) throws Exception {
      double totalAmount = this.accountEJB.convertCurrency(this.getTotalAmountInBaseCurrency(), CurrencyData.baseCurrency, currency);
      double currentPayout = totalAmount * (1.0D - SystemProperty.getDouble("RakePercent") / 100.0D);
      return currentPayout;
   }

   public double getTotalAmount(String currency) throws Exception {
      return this.accountEJB.convertCurrency(this.getTotalAmountInBaseCurrency(), CurrencyData.baseCurrency, currency);
   }

   public String toString() {
      String s = "Pot " + this.potData.getId() + " total size (base currency): " + String.format("%.2f", this.getTotalAmountInBaseCurrency()) + " " + CurrencyData.baseCurrency + "\n";
      s = s + "Stakes:\n";

      String username;
      PotStakeData stake;
      for(Iterator i$ = this.stakes.keySet().iterator(); i$.hasNext(); s = s + String.format("User %s: %.2f %s (%.2f %s base).", username, stake.getAmount(), stake.getCurrency(), stake.getAmountInBaseCurrency(), CurrencyData.baseCurrency) + " Eligible? " + (stake.isEligible() ? "yes" : "no") + "\n") {
         username = (String)i$.next();
         stake = (PotStakeData)this.stakes.get(username);
      }

      return s;
   }
}
