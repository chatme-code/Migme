package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AvatarItemData;
import com.projectgoth.fusion.data.ContentData;
import com.projectgoth.fusion.data.ContentPurchasedData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.data.ListDataWrapper;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.data.RewardProgramCompletionData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.StoreCategoryData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.StoreItemInventoryData;
import com.projectgoth.fusion.data.StoreItemInventorySummaryData;
import com.projectgoth.fusion.data.ThemeData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.leto.common.impl.outcome.MMv2Outcomes;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface ContentLocal extends EJBLocalObject {
   void invalidateLocalCachedItems();

   int getOptimalEmoticonHeight(String var1, int var2) throws FusionEJBException, EJBException;

   EmoticonData getEmoticon(int var1) throws EJBException, FusionEJBException;

   EmoticonData getEmoticon(String var1, int var2) throws FusionEJBException, EJBException;

   List getAllEmoticonDataByHotKey(String var1) throws FusionEJBException, EJBException;

   EmoticonData getByHotKey(String var1, int var2, int var3) throws FusionEJBException, EJBException;

   List getEmoticons(String var1) throws FusionEJBException, EJBException;

   List getSecurityQeustions() throws FusionEJBException, EJBException;

   List getStickerDataListForUser(String var1) throws EJBException;

   EmoticonData getStickerDataByNameForUser(String var1, String var2);

   List getStickerPackIDListForUser(String var1) throws EJBException;

   List getStickerPackDataListForUser(String var1) throws EJBException;

   int getEmoticonPackCountForUser(String var1);

   List getAllEmoticons() throws FusionEJBException, EJBException;

   List getEmoticonPack(int var1) throws FusionEJBException, EJBException;

   List getStickerDataListForStickerPack(int var1) throws EJBException;

   EmoticonPackData getStickerPackData(int var1) throws EJBException;

   List getStickerPackDataList(Collection var1) throws EJBException;

   EmoticonData getStickerData(int var1) throws EJBException;

   List getTopWallpaper(int var1) throws Exception;

   List getTopRingtones(int var1) throws Exception;

   List getEmotes() throws EJBException;

   void updateEmoticonPackStatus(String var1, int var2, int var3) throws EJBException;

   void buyEmoticonPack(String var1, int var2, AccountEntrySourceData var3) throws FusionEJBException, EJBException;

   void saveMobileContentItem(ContentData var1, String var2, int var3) throws EJBException;

   String buyMobileContentItem(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   void processILoopAPICall(String var1, String var2, String var3, AccountEntrySourceData var4) throws EJBException;

   void refundMobileContentItem(String var1, String var2, ContentPurchasedData.RefundReasonEnum var3, AccountEntrySourceData var4) throws EJBException;

   void refundMobileContentItemFromMIS(String var1, int var2, String var3, AccountEntrySourceData var4) throws EJBException;

   void sendMobileContentDownloadURLInSMS(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   void onPurchaseVirtualGift(String var1, Map var2, VirtualGiftData var3, boolean var4, String var5, String var6, boolean var7);

   void onPurchaseVirtualGift(String var1, Map var2, VirtualGiftData var3, boolean var4, String var5, String var6);

   List getVirtualGifts(String var1, int var2, int var3) throws EJBException;

   ArrayList getVirtualGiftCategoryNames() throws EJBException;

   List getVirtualGiftForCategory(String var1, String var2, int var3, int var4) throws EJBException;

   List getFeaturedPopularNewVirtualGifts(String var1, int var2, int var3) throws EJBException;

   List getFeaturedVirtualGifts(String var1, int var2, int var3) throws EJBException;

   List getPopularVirtualGifts(String var1, int var2, int var3) throws EJBException;

   List getNewVirtualGifts(String var1, int var2, int var3) throws EJBException;

   List getRecentVirtualGifts(String var1, int var2, int var3) throws EJBException;

   List searchVirtualGifts(String var1, int var2, String var3, int var4, boolean var5) throws EJBException;

   List getRecentGiftsReceivedBy(String var1, String var2, int var3, int var4) throws EJBException;

   List getRecentGiftsSentBy(String var1, String var2, int var3, int var4) throws EJBException;

   VirtualGiftData getVirtualGiftDetails(VirtualGiftData var1);

   VirtualGiftData getVirtualGiftByHotKey(String var1) throws EJBException;

   VirtualGiftData getVirtualGift(Integer var1, String var2, String var3) throws EJBException, FusionEJBException;

   double isGiftLowPrice(VirtualGiftData var1, String var2) throws FusionEJBException;

   void buyVirtualGift(String var1, String var2, int var3, int var4, boolean var5, String var6, String var7, AccountEntrySourceData var8) throws FusionEJBException;

   Map buyVirtualGiftForMultipleUsers(String var1, List var2, VirtualGiftData var3, int var4, boolean var5, String var6, boolean var7, boolean var8, AccountEntrySourceData var9) throws EJBException, FusionEJBException;

   boolean rateVirtualGift(int var1, String var2, int var3) throws EJBException, FusionEJBException;

   void buyAvatarItem(int var1, int var2, int var3, AccountEntrySourceData var4) throws EJBException;

   void incrementStoreItemSold(StoreItemData.TypeEnum var1, int var2, Connection var3) throws SQLException;

   void incrementStoreItemSoldByNumber(StoreItemData.TypeEnum var1, int var2, int var3, Connection var4) throws SQLException;

   StoreItemData getStoreItem(int var1) throws EJBException;

   void resetCachedRewardPrograms();

   List getRewardProgramProcessorMapping() throws EJBException;

   void giveRewards(int var1, int var2, AccountEntrySourceData var3, List var4, Map var5) throws EJBException;

   RewardProgramCompletionData awardUser(RewardProgramData var1, UserData var2, AccountEntrySourceData var3, List var4, Map var5) throws EJBException;

   int getRewardProgramsCompletionCount(int var1, int var2) throws EJBException;

   void updateUserOwnedChatRoomSizes(String var1, int var2);

   boolean buyPaidEmote(String var1, PaidEmoteData var2, int var3, AccountEntrySourceData var4) throws EJBException;

   Vector getPaintWarsSpecialItems();

   String createMigboTextPostForUser(int var1, String var2, String var3, String var4, String var5, ClientType var6, SSOEnums.View var7) throws EJBException;

   void enforceFreeGiftToReferrerRules(String var1, String var2) throws FusionEJBException, EJBException;

   int recordVirtualGift(String var1, String var2, VirtualGiftData var3, int var4, boolean var5, String var6) throws FusionEJBException, EJBException;

   void billVirtualGiftForMultipleUsers(String var1, VirtualGiftData var2, HashMap var3, AccountEntrySourceData var4) throws EJBException, FusionEJBException;

   StoreItemData getStoreItem(String var1, int var2) throws EJBException;

   VirtualGiftData getVirtualGift(Integer var1, String var2) throws EJBException, FusionEJBException;

   AvatarItemData getAvatarItem(int var1) throws EJBException;

   ThemeData getThemeById(int var1) throws EJBException;

   VirtualGiftReceivedData getVirtualGiftReceived(String var1, String var2, int var3);

   ListDataWrapper getVirtualGiftsReceived(String var1, String var2, int var3, int var4);

   ListDataWrapper getStoreItemsByType_old(String var1, int var2, String var3, String var4, int var5, int var6, boolean var7) throws FusionEJBException;

   ListDataWrapper searchStoreItems(String var1, String var2, StoreItemData.TypeEnum var3, Integer var4, double var5, double var7, String var9, String var10, int var11, int var12, boolean var13) throws FusionEJBException;

   ListDataWrapper getStoreItemsByType(String var1, int var2, String var3, String var4, int var5, int var6, boolean var7) throws FusionEJBException;

   List getStoreCategories(int var1, int var2, boolean var3) throws FusionEJBException;

   StoreCategoryData getStoreCategory(int var1, int var2, boolean var3) throws FusionEJBException;

   ListDataWrapper getStoreItemsByCategory(String var1, int var2, String var3, String var4, int var5, int var6, boolean var7) throws FusionEJBException;

   List addStoreItemsInventory(Map var1, int var2, StoreItemInventoryData.StoreItemInventoryLocationEnum var3);

   long addStoreItemInventory(int var1, int var2, StoreItemInventoryData.StoreItemInventoryLocationEnum var3);

   void addStoreItemInventoryReceived(long var1, int var3, Integer var4, StoreItemData.TypeEnum var5);

   StoreItemInventorySummaryData getStoreItemInventory(int var1, int var2);

   List getInventoryVirtualGifts(int var1);

   List getStoreItemsInventoryByType(int var1, StoreItemData.TypeEnum var2);

   Map giveVirtualGiftForMultipleUsers(String var1, List var2, StoreItemData var3, int var4, boolean var5, String var6) throws EJBException, FusionEJBException;

   void giveRewards(MMv2Outcomes var1, AccountEntrySourceData var2) throws EJBException;

   RewardProgramCompletionData awardUser(RewardProgramData var1, UserData var2, AccountEntrySourceData var3, List var4) throws EJBException;
}
