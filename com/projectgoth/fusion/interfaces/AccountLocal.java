package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BankTransferIntentData;
import com.projectgoth.fusion.data.BankTransferReceivedData;
import com.projectgoth.fusion.data.BasicMerchantTagDetailsData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CreditCardPaymentData;
import com.projectgoth.fusion.data.CreditTransferData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.DiscountTierData;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.MoneyTransferData;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.data.PotData;
import com.projectgoth.fusion.data.PotStakeData;
import com.projectgoth.fusion.data.ServiceData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.data.UserPotEligibilityData;
import com.projectgoth.fusion.data.UserReferralData;
import com.projectgoth.fusion.data.pot.PayoutData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentMetaDetails;
import com.projectgoth.fusion.payment.PaymentSummaryData;
import com.projectgoth.fusion.payment.creditcard.CreditCardPaymentUserAndCountryInfo;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface AccountLocal extends EJBLocalObject {
   AccountEntryData createAccountEntry(Connection var1, AccountEntryData var2, AccountEntrySourceData var3) throws EJBException;

   AccountEntryData refundAccountEntry(long var1, String var3, String var4, AccountEntrySourceData var5) throws EJBException;

   AccountBalanceData getAccountBalance(String var1) throws EJBException;

   AccountEntryData getAccountEntry(long var1) throws EJBException;

   AccountEntryData getAccountEntryFromSlave(long var1) throws EJBException;

   AccountEntryData getAccountEntryFromReference(AccountEntryData.TypeEnum var1, String var2) throws EJBException;

   List getAccountEntries(String var1, boolean var2) throws EJBException;

   MoneyTransferData getMoneyTransferEntry(int var1) throws EJBException;

   List getMoneyTransferEntries(String var1) throws EJBException;

   AccountEntryData giveActivationCredit(String var1, int var2, String var3, AccountEntrySourceData var4) throws EJBException;

   AccountEntryData creditReferrer(UserReferralData var1, AccountEntrySourceData var2) throws EJBException;

   CreditTransferData transferPartnerCredit(int var1, String var2, double var3, String var5, AccountEntryData.TypeEnum var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData refundCreditTransferFee(long var1, AccountEntrySourceData var3) throws EJBException;

   boolean isChargeableWithCreditTransferFee(int var1) throws FusionEJBException;

   AccountEntryData chargeCreditTransferFee(String var1, String var2, AccountEntryData var3, Connection var4) throws EJBExceptionWithErrorCause, EJBException;

   CreditTransferData transferCredit(String var1, String var2, double var3, boolean var5, String var6, AccountEntrySourceData var7) throws EJBException;

   Map getMinTopMerchantToNonTopMerchantTagAmount(int var1) throws Exception;

   Map getMinTopMerchantToNonTopMerchantTagAmount(int var1, String var2) throws Exception;

   double getNETTTransfer(Connection var1, String var2, String var3, Date var4, int var5) throws SQLException;

   BasicMerchantTagDetailsData getMerchantTagFromUsername(Connection var1, String var2, boolean var3) throws EJBException;

   MerchantTagData getInactiveMerchantTagBetweenUsers(Connection var1, int var2, int var3, boolean var4) throws EJBException;

   void tagMerchantPending(Connection var1, int var2, String var3, int var4, String var5) throws EJBException;

   boolean tagMerchant(Connection var1, int var2, String var3, int var4, String var5) throws EJBException;

   boolean tagMerchant(Connection var1, int var2, String var3, int var4, String var5, MerchantTagData var6, Date var7, Long var8, Integer var9) throws Exception;

   void untagMerchant(Connection var1, String var2) throws EJBException;

   void untagMerchant(Connection var1, MerchantTagData var2, String var3) throws EJBException;

   void untagMerchant(Connection var1, List var2) throws EJBException;

   MerchantTagData fixNullAccountentryInMerchantTag(Connection var1, MerchantTagData var2) throws EJBException;

   void activatePendingMerchantTag(Connection var1, String var2) throws EJBException;

   void reverseTransferCredit(long var1, String var3, AccountEntrySourceData var4) throws EJBException;

   void creditAndNotifyUser(String var1, double var2, double var4, String var6, AccountEntrySourceData var7, boolean var8) throws Exception;

   void reverseTTCredit(long var1, String var3, AccountEntrySourceData var4) throws EJBException;

   AccountEntryData chargeUserForChatRoomKick(String var1, String var2, String var3, double var4, AccountEntrySourceData var6) throws EJBException;

   AccountEntryData chargeUserForSMS(String var1, String var2, String var3, double var4, AccountEntrySourceData var6) throws EJBException;

   AccountEntryData refundUserForSMS(int var1, String var2, String var3, AccountEntrySourceData var4) throws EJBException;

   AccountEntryData chargeUserForSystemSMS(String var1, String var2, String var3, double var4, AccountEntrySourceData var6) throws EJBException;

   void updateWholesaleSMSCost(String var1, double var2) throws EJBException;

   void updateWholesaleSystemSMSCost(String var1, double var2) throws EJBException;

   AccountEntryData chargeUserForCall(String var1, String var2, String var3, double var4, double var6, AccountEntrySourceData var8) throws EJBException;

   void refundUserForCall(long var1, String var3, AccountEntrySourceData var4) throws EJBException;

   void reverseExpiredCredit(long var1, String var3, AccountEntrySourceData var4) throws EJBException;

   MoneyTransferData moneyTransferTopup(MoneyTransferData var1) throws EJBException;

   CreditCardPaymentData creditCardPayment(CreditCardPaymentData var1, AccountEntrySourceData var2) throws FusionEJBException, EJBException;

   CreditCardPaymentUserAndCountryInfo getUserAndCountryInfoForCreditCardPayment(Connection var1, int var2) throws FusionEJBException;

   boolean isOnCreditCardWhiteList(Connection var1, String var2, String var3) throws SQLException;

   CreditCardPaymentData validatePastCreditCardHMLTransactions(CreditCardPaymentData var1, CountryData var2, double var3, double var5, double var7, boolean var9) throws EJBException, SQLException, NoSuchFieldException, Exception;

   CreditCardPaymentData getCreditCardPayment(int var1) throws EJBException;

   CreditCardPaymentData approveCreditCardPayment(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   CreditCardPaymentData rejectCreditCardPayment(String var1, int var2, String var3, AccountEntrySourceData var4) throws EJBException;

   AccountEntryData creditCardRefund(int var1, AccountEntrySourceData var2) throws EJBException;

   void creditCardChargeBack(String var1, Date var2, String var3) throws EJBException;

   CreditCardPaymentData updateCreditCardPaymentStatus(CreditCardPaymentData var1, AccountEntrySourceData var2) throws EJBException;

   void creditUserFromCreditCardPayment(CreditCardPaymentData var1, AccountEntrySourceData var2, Connection var3);

   Vector getCreditCardTransactions(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, int var9) throws Exception;

   Vector getCreditCardHMLTransactions(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, int var9) throws Exception;

   CurrencyData getUsersLocalCurrency(String var1) throws EJBException;

   void setUsersLocalCurrency(String var1, String var2, AccountEntrySourceData var3) throws EJBException;

   int getPaymentType(String var1) throws EJBException;

   List getPaymentsByMetaData(PaymentData.TypeEnum var1, PaymentMetaDetails[] var2) throws FusionEJBException;

   int getBankTransferProductID(int var1) throws EJBException;

   BankTransferIntentData bankTransfer(BankTransferIntentData var1) throws EJBException;

   BankTransferReceivedData updateBankTransferStatus(BankTransferReceivedData var1, AccountEntrySourceData var2) throws EJBException;

   DiscountTierData getApplicableDiscountTier(Enums.PaymentEnum var1, String var2, double var3, CurrencyData var5, boolean var6);

   Vector getEligibleDiscountTiers(Enums.PaymentEnum var1, String var2, CurrencyData var3);

   double discountTierRounding(double var1);

   double[] getCreditCardPaymentAmounts(String var1, boolean var2, String var3) throws EJBException;

   ServiceData getService(int var1) throws EJBException;

   SubscriptionData getSubscription(int var1) throws EJBException;

   List getSubscriptions(String var1, Integer var2) throws EJBException;

   List getExpiringSubscriptions(int var1, int var2) throws EJBException;

   List getExpiredSubscriptions(int var1, int var2) throws EJBException;

   List getPendingSubscriptions(int var1, int var2) throws EJBException;

   SubscriptionData subscribeService(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   void updateSubscriptionBillingStatus(String var1, int var2, boolean var3, AccountEntrySourceData var4) throws EJBException;

   void cancelSubscription(String var1, int var2) throws EJBException;

   void expireSubscription(String var1, int var2) throws EJBException;

   void sendSubscriptionExpiryReminderSMS(String var1, int var2, String var3, AccountEntrySourceData var4) throws EJBException;

   AccountEntryData chargeUserForGame(String var1, String var2, AccountEntryData.TypeEnum var3, String var4, double var5, AccountEntrySourceData var7) throws EJBException;

   boolean userCanAffordCost(String var1, double var2, String var4, Connection var5) throws EJBException;

   UserPotEligibilityData userCanAffordToEnterPot(String var1, double var2, String var4, String var5) throws EJBException;

   PotData createPot(int var1, String var2) throws EJBException;

   PotStakeData enterUserIntoPot(String var1, int var2, int var3, String var4, double var5, String var7, AccountEntrySourceData var8) throws EJBException;

   void removeUserFromPot(int var1) throws EJBException;

   /** @deprecated */
   double payoutPot(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   PayoutData performPayout(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   double payoutPotAndNotify(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   void cancelPot(int var1, AccountEntrySourceData var2) throws EJBException;

   void cancelAllPots(AccountEntrySourceData var1) throws EJBException;

   double convertCurrency(double var1, String var3, String var4) throws EJBException;

   List getUnpaidUserReferrals(int var1);

   AccountEntryData chargeUserForGameItem(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData giveGameReward(String var1, String var2, String var3, double var4, double var6, String var8, AccountEntrySourceData var9) throws EJBException;

   AccountEntryData giveMarketingReward(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData giveUnfundedCredits(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData deductUnfundedCredits(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   Map getMerchantRevenueTrailReport(Date var1, Date var2) throws EJBException;

   Map getMerchantRevenueGameTrailReport(Date var1, Date var2) throws EJBException;

   Map getMerchantRevenueThirdPartyTrailReport(Date var1, Date var2) throws EJBException;

   List getExpiredNonTopMerchantTags(int var1, int var2) throws EJBException;

   List getExpiredTopMerchantTags(int var1, int var2) throws EJBException;

   Map getSuperMerchantRevenueTrailReport(Date var1, Date var2) throws EJBException;

   AccountEntryData giveMerchantRevenueTrail(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData giveMerchantRevenueGameTrail(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData giveMerchantRevenueThirdPartyTrail(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   AccountEntryData thirdPartyAPIDebit(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws EJBException;

   boolean userCanAffordPaidEmote(String var1, PaidEmoteData var2, Connection var3) throws EJBException;

   boolean sendRechargeCreditRewardProgramTrigger(String var1, double var2, String var4, int var5) throws EJBException;

   PaymentData createPayment(PaymentData var1) throws EJBException;

   PaymentData getPaymentById(int var1) throws EJBException;

   PaymentData getPaymentByVoucher(int var1, Integer var2, String var3, Date var4, int var5) throws EJBException;

   List getPendingPaymentTransactionsForRequery(int var1, Integer var2);

   List getPaymentTransactions(String var1, Integer var2, Integer var3, Integer var4) throws EJBException;

   PaymentData updatePayment(PaymentData var1, AccountEntrySourceData var2) throws PaymentException;

   boolean isPaymentAllowedToUser(int var1, int var2) throws FusionEJBException;

   int getPendingPaymentsCount(int var1, Integer var2) throws FusionEJBException;

   PaymentSummaryData getPaymentSummaryByUserType(int var1, int var2, int var3, Date var4, Date var5);

   PaymentSummaryData getPaymentSummaryByCountryAndUserType(int var1, Integer var2, int var3, int var4, Date var5, Date var6);

   PaymentSummaryData getPaymentSummaryByUserID(int var1, int var2, int var3, Date var4, Date var5);

   PaymentSummaryData getPaymentSummaryByMetaDetails(int var1, int var2, int var3, String var4, Date var5, Date var6);

   double getNonTransferableFundsAtSystemBaseCurrency(String var1, Date var2, int var3, Connection var4, boolean var5);
}
