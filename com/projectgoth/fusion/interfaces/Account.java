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
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentMetaDetails;
import com.projectgoth.fusion.payment.PaymentSummaryData;
import com.projectgoth.fusion.payment.creditcard.CreditCardPaymentUserAndCountryInfo;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.ejb.EJBObject;

public interface Account extends EJBObject {
   AccountEntryData createAccountEntry(Connection var1, AccountEntryData var2, AccountEntrySourceData var3) throws RemoteException;

   AccountEntryData refundAccountEntry(long var1, String var3, String var4, AccountEntrySourceData var5) throws RemoteException;

   AccountBalanceData getAccountBalance(String var1) throws RemoteException;

   AccountEntryData getAccountEntry(long var1) throws RemoteException;

   AccountEntryData getAccountEntryFromSlave(long var1) throws RemoteException;

   AccountEntryData getAccountEntryFromReference(AccountEntryData.TypeEnum var1, String var2) throws RemoteException;

   List getAccountEntries(String var1, boolean var2) throws RemoteException;

   MoneyTransferData getMoneyTransferEntry(int var1) throws RemoteException;

   List getMoneyTransferEntries(String var1) throws RemoteException;

   AccountEntryData giveActivationCredit(String var1, int var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   AccountEntryData creditReferrer(UserReferralData var1, AccountEntrySourceData var2) throws RemoteException;

   CreditTransferData transferPartnerCredit(int var1, String var2, double var3, String var5, AccountEntryData.TypeEnum var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData refundCreditTransferFee(long var1, AccountEntrySourceData var3) throws RemoteException;

   boolean isChargeableWithCreditTransferFee(int var1) throws FusionEJBException, RemoteException;

   AccountEntryData chargeCreditTransferFee(String var1, String var2, AccountEntryData var3, Connection var4) throws EJBExceptionWithErrorCause, RemoteException;

   CreditTransferData transferCredit(String var1, String var2, double var3, boolean var5, String var6, AccountEntrySourceData var7) throws RemoteException;

   Map getMinTopMerchantToNonTopMerchantTagAmount(int var1) throws Exception, RemoteException;

   Map getMinTopMerchantToNonTopMerchantTagAmount(int var1, String var2) throws Exception, RemoteException;

   double getNETTTransfer(Connection var1, String var2, String var3, Date var4, int var5) throws SQLException, RemoteException;

   BasicMerchantTagDetailsData getMerchantTagFromUsername(Connection var1, String var2, boolean var3) throws RemoteException;

   MerchantTagData getInactiveMerchantTagBetweenUsers(Connection var1, int var2, int var3, boolean var4) throws RemoteException;

   void tagMerchantPending(Connection var1, int var2, String var3, int var4, String var5) throws RemoteException;

   boolean tagMerchant(Connection var1, int var2, String var3, int var4, String var5) throws RemoteException;

   boolean tagMerchant(Connection var1, int var2, String var3, int var4, String var5, MerchantTagData var6, Date var7, Long var8, Integer var9) throws Exception, RemoteException;

   void untagMerchant(Connection var1, String var2) throws RemoteException;

   void untagMerchant(Connection var1, MerchantTagData var2, String var3) throws RemoteException;

   void untagMerchant(Connection var1, List var2) throws RemoteException;

   MerchantTagData fixNullAccountentryInMerchantTag(Connection var1, MerchantTagData var2) throws RemoteException;

   void activatePendingMerchantTag(Connection var1, String var2) throws RemoteException;

   void reverseTransferCredit(long var1, String var3, AccountEntrySourceData var4) throws RemoteException;

   void creditAndNotifyUser(String var1, double var2, double var4, String var6, AccountEntrySourceData var7, boolean var8) throws Exception, RemoteException;

   void reverseTTCredit(long var1, String var3, AccountEntrySourceData var4) throws RemoteException;

   AccountEntryData chargeUserForChatRoomKick(String var1, String var2, String var3, double var4, AccountEntrySourceData var6) throws RemoteException;

   AccountEntryData chargeUserForSMS(String var1, String var2, String var3, double var4, AccountEntrySourceData var6) throws RemoteException;

   AccountEntryData refundUserForSMS(int var1, String var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   AccountEntryData chargeUserForSystemSMS(String var1, String var2, String var3, double var4, AccountEntrySourceData var6) throws RemoteException;

   void updateWholesaleSMSCost(String var1, double var2) throws RemoteException;

   void updateWholesaleSystemSMSCost(String var1, double var2) throws RemoteException;

   AccountEntryData chargeUserForCall(String var1, String var2, String var3, double var4, double var6, AccountEntrySourceData var8) throws RemoteException;

   void refundUserForCall(long var1, String var3, AccountEntrySourceData var4) throws RemoteException;

   void reverseExpiredCredit(long var1, String var3, AccountEntrySourceData var4) throws RemoteException;

   MoneyTransferData moneyTransferTopup(MoneyTransferData var1) throws RemoteException;

   CreditCardPaymentData creditCardPayment(CreditCardPaymentData var1, AccountEntrySourceData var2) throws FusionEJBException, RemoteException;

   CreditCardPaymentUserAndCountryInfo getUserAndCountryInfoForCreditCardPayment(Connection var1, int var2) throws FusionEJBException, RemoteException;

   boolean isOnCreditCardWhiteList(Connection var1, String var2, String var3) throws SQLException, RemoteException;

   CreditCardPaymentData validatePastCreditCardHMLTransactions(CreditCardPaymentData var1, CountryData var2, double var3, double var5, double var7, boolean var9) throws SQLException, NoSuchFieldException, Exception, RemoteException;

   CreditCardPaymentData getCreditCardPayment(int var1) throws RemoteException;

   CreditCardPaymentData approveCreditCardPayment(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   CreditCardPaymentData rejectCreditCardPayment(String var1, int var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   AccountEntryData creditCardRefund(int var1, AccountEntrySourceData var2) throws RemoteException;

   void creditCardChargeBack(String var1, Date var2, String var3) throws RemoteException;

   Vector getCreditCardTransactions(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, int var9) throws Exception, RemoteException;

   Vector getCreditCardHMLTransactions(String var1, String var2, String var3, String var4, String var5, String var6, String var7, String var8, int var9) throws Exception, RemoteException;

   CurrencyData getUsersLocalCurrency(String var1) throws RemoteException;

   void setUsersLocalCurrency(String var1, String var2, AccountEntrySourceData var3) throws RemoteException;

   int getPaymentType(String var1) throws RemoteException;

   List getPaymentsByMetaData(PaymentData.TypeEnum var1, PaymentMetaDetails[] var2) throws FusionEJBException, RemoteException;

   int getBankTransferProductID(int var1) throws RemoteException;

   BankTransferIntentData bankTransfer(BankTransferIntentData var1) throws RemoteException;

   BankTransferReceivedData updateBankTransferStatus(BankTransferReceivedData var1, AccountEntrySourceData var2) throws RemoteException;

   DiscountTierData getApplicableDiscountTier(Enums.PaymentEnum var1, String var2, double var3, CurrencyData var5, boolean var6) throws RemoteException;

   Vector getEligibleDiscountTiers(Enums.PaymentEnum var1, String var2, CurrencyData var3) throws RemoteException;

   double discountTierRounding(double var1) throws RemoteException;

   double[] getCreditCardPaymentAmounts(String var1, boolean var2, String var3) throws RemoteException;

   ServiceData getService(int var1) throws RemoteException;

   SubscriptionData getSubscription(int var1) throws RemoteException;

   List getSubscriptions(String var1, Integer var2) throws RemoteException;

   List getExpiringSubscriptions(int var1, int var2) throws RemoteException;

   List getExpiredSubscriptions(int var1, int var2) throws RemoteException;

   List getPendingSubscriptions(int var1, int var2) throws RemoteException;

   SubscriptionData subscribeService(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   void updateSubscriptionBillingStatus(String var1, int var2, boolean var3, AccountEntrySourceData var4) throws RemoteException;

   void cancelSubscription(String var1, int var2) throws RemoteException;

   void expireSubscription(String var1, int var2) throws RemoteException;

   void sendSubscriptionExpiryReminderSMS(String var1, int var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   AccountEntryData chargeUserForGame(String var1, String var2, AccountEntryData.TypeEnum var3, String var4, double var5, AccountEntrySourceData var7) throws RemoteException;

   boolean userCanAffordCost(String var1, double var2, String var4, Connection var5) throws RemoteException;

   UserPotEligibilityData userCanAffordToEnterPot(String var1, double var2, String var4, String var5) throws RemoteException;

   PotData createPot(int var1, String var2) throws RemoteException;

   PotStakeData enterUserIntoPot(String var1, int var2, int var3, String var4, double var5, String var7, AccountEntrySourceData var8) throws RemoteException;

   void removeUserFromPot(int var1) throws RemoteException;

   /** @deprecated */
   double payoutPot(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   double payoutPotAndNotify(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   void cancelPot(int var1, AccountEntrySourceData var2) throws RemoteException;

   void cancelAllPots(AccountEntrySourceData var1) throws RemoteException;

   double convertCurrency(double var1, String var3, String var4) throws RemoteException;

   List getUnpaidUserReferrals(int var1) throws RemoteException;

   AccountEntryData chargeUserForGameItem(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData giveGameReward(String var1, String var2, String var3, double var4, double var6, String var8, AccountEntrySourceData var9) throws RemoteException;

   AccountEntryData giveMarketingReward(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData giveUnfundedCredits(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData deductUnfundedCredits(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   Map getMerchantRevenueTrailReport(Date var1, Date var2) throws RemoteException;

   Map getMerchantRevenueGameTrailReport(Date var1, Date var2) throws RemoteException;

   Map getMerchantRevenueThirdPartyTrailReport(Date var1, Date var2) throws RemoteException;

   List getExpiredNonTopMerchantTags(int var1, int var2) throws RemoteException;

   List getExpiredTopMerchantTags(int var1, int var2) throws RemoteException;

   Map getSuperMerchantRevenueTrailReport(Date var1, Date var2) throws RemoteException;

   AccountEntryData giveMerchantRevenueTrail(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData giveMerchantRevenueGameTrail(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData giveMerchantRevenueThirdPartyTrail(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   AccountEntryData thirdPartyAPIDebit(String var1, String var2, String var3, double var4, String var6, AccountEntrySourceData var7) throws RemoteException;

   boolean userCanAffordPaidEmote(String var1, PaidEmoteData var2, Connection var3) throws RemoteException;

   boolean sendRechargeCreditRewardProgramTrigger(String var1, double var2, String var4, int var5) throws RemoteException;

   PaymentData createPayment(PaymentData var1) throws RemoteException;

   PaymentData getPaymentById(int var1) throws RemoteException;

   PaymentData getPaymentByVoucher(int var1, Integer var2, String var3, Date var4, int var5) throws RemoteException;

   List getPendingPaymentTransactionsForRequery(int var1, Integer var2) throws RemoteException;

   List getPaymentTransactions(String var1, Integer var2, Integer var3, Integer var4) throws RemoteException;

   PaymentData updatePayment(PaymentData var1, AccountEntrySourceData var2) throws PaymentException, RemoteException;

   boolean isPaymentAllowedToUser(int var1, int var2) throws FusionEJBException, RemoteException;

   int getPendingPaymentsCount(int var1, Integer var2) throws FusionEJBException, RemoteException;

   PaymentSummaryData getPaymentSummaryByUserType(int var1, int var2, int var3, Date var4, Date var5) throws RemoteException;

   PaymentSummaryData getPaymentSummaryByCountryAndUserType(int var1, Integer var2, int var3, int var4, Date var5, Date var6) throws RemoteException;

   PaymentSummaryData getPaymentSummaryByUserID(int var1, int var2, int var3, Date var4, Date var5) throws RemoteException;

   PaymentSummaryData getPaymentSummaryByMetaDetails(int var1, int var2, int var3, String var4, Date var5, Date var6) throws RemoteException;

   double getNonTransferableFundsAtSystemBaseCurrency(String var1, Date var2, int var3, Connection var4, boolean var5) throws RemoteException;
}
