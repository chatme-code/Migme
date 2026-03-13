package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AvatarItemData;
import com.projectgoth.fusion.data.ContentData;
import com.projectgoth.fusion.data.ContentPurchasedData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.data.ListDataWrapper;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.data.StoreCategoryData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.StoreItemInventoryData;
import com.projectgoth.fusion.data.StoreItemInventorySummaryData;
import com.projectgoth.fusion.data.ThemeData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.leto.common.impl.outcome.MMv2Outcomes;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.ejb.EJBObject;

public interface Content extends EJBObject {
   int getOptimalEmoticonHeight(String var1, int var2) throws FusionEJBException, RemoteException;

   EmoticonData getEmoticon(int var1) throws FusionEJBException, RemoteException;

   EmoticonData getEmoticon(String var1, int var2) throws FusionEJBException, RemoteException;

   List getAllEmoticonDataByHotKey(String var1) throws FusionEJBException, RemoteException;

   EmoticonData getByHotKey(String var1, int var2, int var3) throws FusionEJBException, RemoteException;

   List getEmoticons(String var1) throws FusionEJBException, RemoteException;

   List getSecurityQeustions() throws FusionEJBException, RemoteException;

   List getStickerDataListForUser(String var1) throws RemoteException;

   EmoticonData getStickerDataByNameForUser(String var1, String var2) throws RemoteException;

   List getStickerPackIDListForUser(String var1) throws RemoteException;

   List getStickerPackDataListForUser(String var1) throws RemoteException;

   int getEmoticonPackCountForUser(String var1) throws RemoteException;

   List getAllEmoticons() throws FusionEJBException, RemoteException;

   List getEmoticonPack(int var1) throws FusionEJBException, RemoteException;

   List getStickerDataListForStickerPack(int var1) throws RemoteException;

   EmoticonPackData getStickerPackData(int var1) throws RemoteException;

   List getStickerPackDataList(Collection var1) throws RemoteException;

   EmoticonData getStickerData(int var1) throws RemoteException;

   List getTopWallpaper(int var1) throws Exception, RemoteException;

   List getTopRingtones(int var1) throws Exception, RemoteException;

   List getEmotes() throws RemoteException;

   void updateEmoticonPackStatus(String var1, int var2, int var3) throws RemoteException;

   void buyEmoticonPack(String var1, int var2, AccountEntrySourceData var3) throws FusionEJBException, RemoteException;

   void saveMobileContentItem(ContentData var1, String var2, int var3) throws RemoteException;

   String buyMobileContentItem(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   void processILoopAPICall(String var1, String var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   void refundMobileContentItem(String var1, String var2, ContentPurchasedData.RefundReasonEnum var3, AccountEntrySourceData var4) throws RemoteException;

   void refundMobileContentItemFromMIS(String var1, int var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   void sendMobileContentDownloadURLInSMS(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   void onPurchaseVirtualGift(String var1, Map var2, VirtualGiftData var3, boolean var4, String var5, String var6, boolean var7) throws RemoteException;

   void onPurchaseVirtualGift(String var1, Map var2, VirtualGiftData var3, boolean var4, String var5, String var6) throws RemoteException;

   List getVirtualGifts(String var1, int var2, int var3) throws RemoteException;

   ArrayList getVirtualGiftCategoryNames() throws RemoteException;

   List getVirtualGiftForCategory(String var1, String var2, int var3, int var4) throws RemoteException;

   List getFeaturedPopularNewVirtualGifts(String var1, int var2, int var3) throws RemoteException;

   List getFeaturedVirtualGifts(String var1, int var2, int var3) throws RemoteException;

   List getPopularVirtualGifts(String var1, int var2, int var3) throws RemoteException;

   List getNewVirtualGifts(String var1, int var2, int var3) throws RemoteException;

   List getRecentVirtualGifts(String var1, int var2, int var3) throws RemoteException;

   List searchVirtualGifts(String var1, int var2, String var3, int var4, boolean var5) throws RemoteException;

   List getRecentGiftsReceivedBy(String var1, String var2, int var3, int var4) throws RemoteException;

   List getRecentGiftsSentBy(String var1, String var2, int var3, int var4) throws RemoteException;

   VirtualGiftData getVirtualGiftDetails(VirtualGiftData var1) throws RemoteException;

   VirtualGiftData getVirtualGiftByHotKey(String var1) throws RemoteException;

   VirtualGiftData getVirtualGift(Integer var1, String var2, String var3) throws FusionEJBException, RemoteException;

   double isGiftLowPrice(VirtualGiftData var1, String var2) throws FusionEJBException, RemoteException;

   void buyVirtualGift(String var1, String var2, int var3, int var4, boolean var5, String var6, String var7, AccountEntrySourceData var8) throws FusionEJBException, RemoteException;

   Map buyVirtualGiftForMultipleUsers(String var1, List var2, VirtualGiftData var3, int var4, boolean var5, String var6, boolean var7, boolean var8, AccountEntrySourceData var9) throws FusionEJBException, RemoteException;

   boolean rateVirtualGift(int var1, String var2, int var3) throws FusionEJBException, RemoteException;

   void buyAvatarItem(int var1, int var2, int var3, AccountEntrySourceData var4) throws RemoteException;

   void incrementStoreItemSoldByNumber(StoreItemData.TypeEnum var1, int var2, int var3, Connection var4) throws SQLException, RemoteException;

   StoreItemData getStoreItem(int var1) throws RemoteException;

   void resetCachedRewardPrograms() throws RemoteException;

   List getRewardProgramProcessorMapping() throws RemoteException;

   List getRewardPrograms() throws RemoteException;

   /** @deprecated */
   void giveRewards(int var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   void giveRewards(int var1, int var2, AccountEntrySourceData var3, List var4, Map var5) throws RemoteException;

   int getRewardProgramsCompletionCount(int var1, int var2) throws RemoteException;

   void updateUserOwnedChatRoomSizes(String var1, int var2) throws RemoteException;

   boolean buyPaidEmote(String var1, PaidEmoteData var2, int var3, AccountEntrySourceData var4) throws RemoteException;

   Vector getPaintWarsSpecialItems() throws RemoteException;

   String createMigboTextPostForUser(int var1, String var2, String var3, String var4, String var5, ClientType var6, SSOEnums.View var7) throws RemoteException;

   void enforceFreeGiftToReferrerRules(String var1, String var2) throws FusionEJBException, RemoteException;

   int recordVirtualGift(String var1, String var2, VirtualGiftData var3, int var4, boolean var5, String var6) throws FusionEJBException, RemoteException;

   void billVirtualGiftForMultipleUsers(String var1, VirtualGiftData var2, HashMap var3, AccountEntrySourceData var4) throws FusionEJBException, RemoteException;

   StoreItemData getStoreItem(String var1, int var2) throws RemoteException;

   VirtualGiftData getVirtualGift(Integer var1, String var2) throws FusionEJBException, RemoteException;

   AvatarItemData getAvatarItem(int var1) throws RemoteException;

   ThemeData getThemeById(int var1) throws RemoteException;

   VirtualGiftReceivedData getVirtualGiftReceived(String var1, String var2, int var3) throws RemoteException;

   ListDataWrapper getVirtualGiftsReceived(String var1, String var2, int var3, int var4) throws RemoteException;

   ListDataWrapper getStoreItemsByType_old(String var1, int var2, String var3, String var4, int var5, int var6, boolean var7) throws FusionEJBException, RemoteException;

   ListDataWrapper searchStoreItems(String var1, String var2, StoreItemData.TypeEnum var3, Integer var4, double var5, double var7, String var9, String var10, int var11, int var12, boolean var13) throws FusionEJBException, RemoteException;

   ListDataWrapper getStoreItemsByType(String var1, int var2, String var3, String var4, int var5, int var6, boolean var7) throws FusionEJBException, RemoteException;

   List getStoreCategories(int var1, int var2, boolean var3) throws FusionEJBException, RemoteException;

   StoreCategoryData getStoreCategory(int var1, int var2, boolean var3) throws FusionEJBException, RemoteException;

   ListDataWrapper getStoreItemsByCategory(String var1, int var2, String var3, String var4, int var5, int var6, boolean var7) throws FusionEJBException, RemoteException;

   List addStoreItemsInventory(Map var1, int var2, StoreItemInventoryData.StoreItemInventoryLocationEnum var3) throws RemoteException;

   long addStoreItemInventory(int var1, int var2, StoreItemInventoryData.StoreItemInventoryLocationEnum var3) throws RemoteException;

   void addStoreItemInventoryReceived(long var1, int var3, Integer var4, StoreItemData.TypeEnum var5) throws RemoteException;

   StoreItemInventorySummaryData getStoreItemInventory(int var1, int var2) throws RemoteException;

   List getInventoryVirtualGifts(int var1) throws RemoteException;

   List getStoreItemsInventoryByType(int var1, StoreItemData.TypeEnum var2) throws RemoteException;

   Map giveVirtualGiftForMultipleUsers(String var1, List var2, StoreItemData var3, int var4, boolean var5, String var6) throws FusionEJBException, RemoteException;

   void giveRewards(MMv2Outcomes var1, AccountEntrySourceData var2) throws RemoteException;
}
