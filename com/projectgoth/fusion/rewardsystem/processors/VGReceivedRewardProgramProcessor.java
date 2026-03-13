package com.projectgoth.fusion.rewardsystem.processors;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.rewardsystem.RewardProgramParamName;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.VGReceivedTrigger;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class VGReceivedRewardProgramProcessor extends RewardProgramProcessor {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VGReceivedRewardProgramProcessor.class));
   @RewardProgramParamName
   public static final String UNIQUE_GIFT_PARAM_KEY = "virtualGiftID";
   @RewardProgramParamName
   public static final String GIFT_PARAM_LIST_KEY = "virtualGiftIDList";
   @RewardProgramParamName
   public static final String IS_FROM_INVENTORY = "isFromInventory";
   @RewardProgramParamName
   public static final String MAX_TRIGGER_AMOUNT_PARAM_KEY = "maxTriggerAmount";
   @RewardProgramParamName
   public static final String MIN_TRIGGER_AMOUNT_PARAM_KEY = "minTriggerAmount";
   @RewardProgramParamName
   public static final String TRIGGER_RULE_AMOUNT_CCY_PARAM_KEY = "triggerRuleAmountCcy";
   @RewardProgramParamName
   public static final String IS_INCLUSIVE_MAX_AMOUNT_PARAM_KEY = "isInclusiveMaxAmount";
   @RewardProgramParamName
   public static final String IS_INCLUSIVE_MIN_AMOUNT_PARAM_KEY = "isInclusiveMinAmount";

   private boolean matchesGiftParamKey(RewardProgramData programData, VGReceivedTrigger vgTrigger) {
      int giftid = programData.getIntParam("virtualGiftID", -1);
      if (giftid > 0) {
         return giftid == vgTrigger.virtualGiftID;
      } else {
         return true;
      }
   }

   private boolean belowMaxTriggerAmountCcy(double triggerAmountInRequiredCurrency, RewardProgramData programData, VGReceivedTrigger vgTrigger) {
      if (programData.hasParameter("maxTriggerAmount")) {
         boolean isInclusiveMaxAmt = programData.getBoolParam("isInclusiveMaxAmount", true);
         double maxTriggerAmt = programData.getDoubleParam("maxTriggerAmount", Double.MAX_VALUE);
         boolean valid = isInclusiveMaxAmt ? triggerAmountInRequiredCurrency <= maxTriggerAmt : triggerAmountInRequiredCurrency < maxTriggerAmt;
         if (log.isDebugEnabled()) {
            log.debug("belowMaxTriggerAmountCcy():Trigger amount:" + vgTrigger.amountDelta + " triggerCcy:[" + vgTrigger.currency + "] triggerAmountInRequiredCurrency:[" + triggerAmountInRequiredCurrency + " maxTriggerAmt:[" + maxTriggerAmt + "]  maxAmtInclusive:[" + isInclusiveMaxAmt + "]. Valid:[" + valid + "]");
         }

         return valid;
      } else {
         return true;
      }
   }

   private boolean aboveMinTriggerAmountCcy(double triggerAmountInRequiredCurrency, RewardProgramData programData, VGReceivedTrigger vgTrigger) {
      if (programData.hasParameter("minTriggerAmount")) {
         boolean isInclusiveMinAmt = programData.getBoolParam("isInclusiveMinAmount", true);
         double minTriggerAmt = programData.getDoubleParam("minTriggerAmount", 0.0D);
         boolean valid = isInclusiveMinAmt ? triggerAmountInRequiredCurrency >= minTriggerAmt : triggerAmountInRequiredCurrency > minTriggerAmt;
         if (log.isDebugEnabled()) {
            log.debug("aboveMinTriggerAmountCcy():Trigger amount:" + vgTrigger.amountDelta + " triggerCcy:[" + vgTrigger.currency + "] triggerAmountInRequiredCurrency:[" + triggerAmountInRequiredCurrency + "] minTriggerAmt:[" + minTriggerAmt + "] isInclusiveMinAmt:[" + isInclusiveMinAmt + "]. Valid:[" + valid + "]");
         }

         return valid;
      } else {
         return true;
      }
   }

   private boolean withinBounds(RewardProgramData programData, VGReceivedTrigger vgTrigger) {
      try {
         if (!programData.hasParameter("maxTriggerAmount") && !programData.hasParameter("minTriggerAmount")) {
            return true;
         } else {
            String triggerRuleAmtCcy = programData.getStringParam("triggerRuleAmountCcy", (String)null);
            if (null != triggerRuleAmtCcy) {
               double triggerAmountInRequiredCurrency;
               if (StringUtil.equalsIgnoreCase(triggerRuleAmtCcy, vgTrigger.currency)) {
                  triggerAmountInRequiredCurrency = vgTrigger.amountDelta;
               } else {
                  triggerAmountInRequiredCurrency = this.convertCurrency(vgTrigger.amountDelta, vgTrigger.currency, triggerRuleAmtCcy);
               }

               if (log.isDebugEnabled()) {
                  log.debug("withinBounds():Trigger amount:[" + vgTrigger.amountDelta + "] triggerCcy:[" + vgTrigger.currency + "] equates to triggerAmountInRequiredCurrency:[" + triggerAmountInRequiredCurrency + "] triggerRuleAmtCcy:[" + triggerRuleAmtCcy + "]");
               }

               return this.aboveMinTriggerAmountCcy(triggerAmountInRequiredCurrency, programData, vgTrigger) && this.belowMaxTriggerAmountCcy(triggerAmountInRequiredCurrency, programData, vgTrigger);
            } else {
               log.error("withinBounds():Unable to process trigger. Undefined parameter[triggerRuleAmountCcy]");
               return false;
            }
         }
      } catch (Exception var6) {
         log.error("withinBound(): Failed to process trigger. Exception:" + var6, var6);
         return false;
      }
   }

   private boolean matchesGiftListParamKey(RewardProgramData programData, VGReceivedTrigger vgTrigger) {
      if (!programData.hasParameter("virtualGiftIDList")) {
         return true;
      } else {
         List<String> giftIds = programData.getStringListParam("virtualGiftIDList");
         if (null != giftIds && !giftIds.isEmpty()) {
            Iterator i$ = giftIds.iterator();

            while(i$.hasNext()) {
               String giftId = (String)i$.next();

               try {
                  int id = Integer.parseInt(giftId);
                  if (id > 0 && id == vgTrigger.virtualGiftID) {
                     return true;
                  }
               } catch (NumberFormatException var7) {
                  log.error("Item [" + giftId + "] found in program [" + programData.id + "] parameter [" + "virtualGiftIDList" + "] is not numeric.", var7);
               }
            }
         }

         return false;
      }
   }

   private boolean matchesFromSenderInventory(RewardProgramData programData, VGReceivedTrigger vgTrigger) {
      if (programData.hasParameter("isFromInventory")) {
         return vgTrigger.fromSenderInventory == programData.getBoolParam("isFromInventory", false);
      } else {
         return true;
      }
   }

   protected boolean processInternal(RewardProgramData programData, RewardProgramTrigger trigger) {
      if (!(trigger instanceof VGReceivedTrigger)) {
         return false;
      } else {
         VGReceivedTrigger vgTrigger = (VGReceivedTrigger)trigger;
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.REWARD_PROCESSOR_MATCH_FROM_INVENTORY_ENABLED)) {
            return this.matchesGiftParamKey(programData, vgTrigger) && this.withinBounds(programData, vgTrigger) && this.matchesGiftListParamKey(programData, vgTrigger) && this.matchesFromSenderInventory(programData, vgTrigger);
         } else {
            return this.matchesGiftParamKey(programData, vgTrigger) && this.withinBounds(programData, vgTrigger) && this.matchesGiftListParamKey(programData, vgTrigger);
         }
      }
   }

   private double convertCurrency(double amount, String fromCurrency, String toCurrency) throws CreateException, RemoteException {
      if (toCurrency.equalsIgnoreCase(fromCurrency)) {
         return amount;
      } else {
         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         return accountBean.convertCurrency(amount, fromCurrency, toCurrency);
      }
   }
}
