package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.CashReceiptData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.FileData;
import com.projectgoth.fusion.data.HandsetData;
import com.projectgoth.fusion.data.HandsetInstructionsData;
import com.projectgoth.fusion.data.MenuData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.ResellerData;
import com.projectgoth.fusion.data.ScrapbookData;
import com.projectgoth.hashtag.data.HashTagData;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBObject;

public interface MIS extends EJBObject {
   List getApplicationMenuOptions(int var1) throws RemoteException;

   int getApplicationMenuVersion(int var1, short var2, String var3) throws RemoteException;

   void resetCachedSystemProperties() throws RemoteException;

   Map getSystemProperties() throws RemoteException;

   void updateSystemProperty(String var1, String var2) throws RemoteException;

   String getHelpText(int var1) throws RemoteException;

   String getInfoText(int var1) throws RemoteException;

   List getCountries() throws RemoteException;

   List getCountriesSupportedHashtag() throws RemoteException;

   List getCurrencies() throws RemoteException;

   List getMenus(int var1, short var2, int var3) throws RemoteException;

   MenuData getMenu(int var1) throws RemoteException;

   CountryData getCountry(int var1) throws RemoteException;

   CountryData getCountryByISOCode(String var1) throws RemoteException;

   CountryData getCountryByLocation(int var1) throws RemoteException;

   CountryData getCountryByIDDCode(int var1, String var2) throws RemoteException;

   CountryData getCountryFromIPNumber(Double var1) throws RemoteException;

   double getVoiceRate(int var1, int var2, int var3, int var4) throws Exception, RemoteException;

   CurrencyData getCurrency(String var1) throws RemoteException;

   void setCurrency(CurrencyData var1) throws RemoteException;

   double getSMSRate(String var1) throws Exception, RemoteException;

   double getPremiumCost(int var1) throws Exception, RemoteException;

   double getPremiumFee(int var1) throws Exception, RemoteException;

   String getCountryCurrency(int var1) throws Exception, RemoteException;

   void addToMailList(String var1) throws RemoteException;

   boolean isVASBuild(String var1) throws RemoteException;

   CashReceiptData getCashReceiptData(int var1) throws RemoteException;

   CashReceiptData createCashReceipt(CashReceiptData var1, AccountEntrySourceData var2) throws RemoteException;

   void matchCashReceipt(CashReceiptData var1, AccountEntrySourceData var2) throws RemoteException;

   void deleteCashReceipt(String var1) throws RemoteException;

   void logMISLogin(String var1, String var2, boolean var3) throws RemoteException;

   double calculateBonusCredit(double var1) throws RemoteException;

   void allowUserToReRegister(String var1) throws RemoteException;

   AlertMessageData getAlertMessageData(String var1) throws RemoteException;

   void createAlertMessage(AlertMessageData var1) throws RemoteException;

   void updateAlertMessage(AlertMessageData var1) throws RemoteException;

   ResellerData getResellerData(String var1) throws RemoteException;

   void createReseller(ResellerData var1) throws RemoteException;

   void updateReseller(ResellerData var1) throws RemoteException;

   void updateCountry(CountryData var1) throws RemoteException;

   void changeStaffPassword(String var1, String var2, String var3) throws RemoteException;

   String newFileID() throws RemoteException;

   FileData getFile(String var1) throws RemoteException;

   ScrapbookData saveFile(FileData var1, String var2) throws RemoteException;

   void saveFileToScrapbooks(String var1, String[] var2, String var3, String var4) throws RemoteException;

   void updateFileFromScrapbook(ScrapbookData var1) throws RemoteException;

   void publishFileFromScrapbook(String var1, int var2, String var3, boolean var4) throws RemoteException;

   void unpublishFileFromScrapbook(String var1, int var2) throws RemoteException;

   void deleteFileFromScrapbook(String var1, int var2) throws RemoteException;

   void setFileReportedFromScrapbook(String var1, int var2) throws RemoteException;

   void setFilePrivateFromScrapbook(String var1) throws RemoteException;

   void removeAllInstancesOfFile(String var1) throws RemoteException;

   ScrapbookData getFileFromScrapbook(int var1) throws RemoteException;

   List getScrapbook(String var1) throws RemoteException;

   List getScrapbook(String var1, boolean var2) throws RemoteException;

   List getGallery(String var1, String var2) throws RemoteException;

   Double getScrapbookSize(String var1) throws RemoteException;

   List getHandsetVendors() throws RemoteException;

   List getHandsetInstructions() throws RemoteException;

   HandsetInstructionsData getHandsetInstruction(int var1) throws RemoteException;

   List getHandsetVendorPrefixes() throws RemoteException;

   List getHandsetDetails(String var1) throws RemoteException;

   List getDefaultHandsetDetails() throws RemoteException;

   HandsetData getHandsetDetail(String var1, String var2) throws RemoteException;

   HandsetData getHandsetDetail(Integer var1) throws RemoteException;

   void createHandset(HandsetData var1) throws RemoteException;

   void updateHandsetDetail(HandsetData var1) throws RemoteException;

   void createHandsetInstructions(HandsetInstructionsData var1) throws RemoteException;

   void updateHandsetInstructions(HandsetInstructionsData var1) throws RemoteException;

   void removeMerchantPinAuthentication(int var1) throws RemoteException;

   String getMerchantPinEmail(int var1) throws RemoteException;

   void sendMerchantResetPinNotification(int var1) throws RemoteException;

   List getEmoteCommands() throws RemoteException;

   List getAuthenticatedAccessControlData() throws RemoteException;

   HashTagData getHashTagData(String var1, int var2) throws RemoteException;

   void updateHashTagData(String var1, int var2, String var3) throws RemoteException;

   void updatePromotedPost(List var1) throws RemoteException;

   List getPromotedPost(int var1, boolean var2, int var3) throws RemoteException;

   ReputationLevelData getReputationLevelDataForLevel(int var1) throws RemoteException;

   int getMaximumSystemMigLevel() throws RemoteException;

   boolean logMig33UserAction(int var1, Enums.Mig33UserActionMisLogEnum var2, String var3) throws Exception, RemoteException;
}
