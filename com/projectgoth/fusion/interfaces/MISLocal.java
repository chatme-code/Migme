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
import java.util.List;
import java.util.Map;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface MISLocal extends EJBLocalObject {
   List getApplicationMenuOptions(int var1) throws EJBException;

   int getApplicationMenuVersion(int var1, short var2, String var3) throws EJBException;

   void resetCachedSystemProperties();

   Map getSystemProperties() throws EJBException;

   void updateSystemProperty(String var1, String var2) throws EJBException;

   String getHelpText(int var1) throws EJBException;

   String getInfoText(int var1) throws EJBException;

   List getCountries() throws EJBException;

   List getCountriesSupportedHashtag() throws EJBException;

   List getCurrencies() throws EJBException;

   List getMenus(int var1, short var2, int var3) throws EJBException;

   MenuData getMenu(int var1) throws EJBException;

   CountryData getCountry(int var1) throws EJBException;

   CountryData getCountryByISOCode(String var1) throws EJBException;

   CountryData getCountryByLocation(int var1) throws EJBException;

   CountryData getCountryByIDDCode(int var1, String var2) throws EJBException;

   CountryData getCountryFromIPNumber(Double var1) throws EJBException;

   CurrencyData getCurrency(String var1) throws EJBException;

   CashReceiptData getCashReceiptData(int var1);

   CashReceiptData createCashReceipt(CashReceiptData var1, AccountEntrySourceData var2) throws EJBException;

   void matchCashReceipt(CashReceiptData var1, AccountEntrySourceData var2) throws EJBException;

   void deleteCashReceipt(String var1) throws EJBException;

   void logMISLogin(String var1, String var2, boolean var3) throws EJBException;

   double calculateBonusCredit(double var1) throws EJBException;

   void allowUserToReRegister(String var1) throws EJBException;

   AlertMessageData getAlertMessageData(String var1) throws EJBException;

   void createAlertMessage(AlertMessageData var1) throws EJBException;

   void updateAlertMessage(AlertMessageData var1) throws EJBException;

   ResellerData getResellerData(String var1) throws EJBException;

   void createReseller(ResellerData var1) throws EJBException;

   void updateReseller(ResellerData var1) throws EJBException;

   void updateCountry(CountryData var1) throws EJBException;

   void changeStaffPassword(String var1, String var2, String var3) throws EJBException;

   String newFileID();

   FileData getFile(String var1) throws EJBException;

   ScrapbookData saveFile(FileData var1, String var2) throws EJBException;

   void saveFileToScrapbooks(String var1, String[] var2, String var3, String var4) throws EJBException;

   void updateFileFromScrapbook(ScrapbookData var1) throws EJBException;

   void publishFileFromScrapbook(String var1, int var2, String var3, boolean var4) throws EJBException;

   void unpublishFileFromScrapbook(String var1, int var2) throws EJBException;

   void deleteFileFromScrapbook(String var1, int var2) throws EJBException;

   void setFileReportedFromScrapbook(String var1, int var2) throws EJBException;

   void setFilePrivateFromScrapbook(String var1) throws EJBException;

   void removeAllInstancesOfFile(String var1) throws EJBException;

   ScrapbookData getFileFromScrapbook(int var1) throws EJBException;

   List getScrapbook(String var1) throws EJBException;

   List getScrapbook(String var1, boolean var2) throws EJBException;

   List getGallery(String var1, String var2) throws EJBException;

   Double getScrapbookSize(String var1) throws EJBException;

   List getHandsetVendors() throws EJBException;

   List getHandsetInstructions() throws EJBException;

   HandsetInstructionsData getHandsetInstruction(int var1) throws EJBException;

   List getHandsetVendorPrefixes() throws EJBException;

   List getHandsetDetails(String var1) throws EJBException;

   List getDefaultHandsetDetails() throws EJBException;

   HandsetData getHandsetDetail(String var1, String var2) throws EJBException;

   HandsetData getHandsetDetail(Integer var1) throws EJBException;

   void createHandset(HandsetData var1) throws EJBException;

   void updateHandsetDetail(HandsetData var1) throws EJBException;

   void createHandsetInstructions(HandsetInstructionsData var1) throws EJBException;

   void updateHandsetInstructions(HandsetInstructionsData var1) throws EJBException;

   void removeMerchantPinAuthentication(int var1);

   String getMerchantPinEmail(int var1);

   void sendMerchantResetPinNotification(int var1);

   List getEmoteCommands() throws EJBException;

   List getAuthenticatedAccessControlData() throws EJBException;

   HashTagData getHashTagData(String var1, int var2) throws EJBException;

   void updateHashTagData(String var1, int var2, String var3) throws EJBException;

   void updatePromotedPost(List var1) throws EJBException;

   List getPromotedPost(int var1, boolean var2, int var3) throws EJBException;

   ReputationLevelData getReputationLevelDataForLevel(int var1) throws EJBException;

   int getMaximumSystemMigLevel() throws EJBException;

   boolean logMig33UserAction(int var1, Enums.Mig33UserActionMisLogEnum var2, String var3) throws Exception;
}
