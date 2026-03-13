package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.clientsession.SSOLogin;
import com.projectgoth.fusion.clientsession.SSOLoginData;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.ListDataWrapper;
import com.projectgoth.fusion.data.StoreCategoryData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.ContentLocal;
import com.projectgoth.fusion.interfaces.ContentLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.util.ResourceUtil;
import com.projectgoth.fusion.slice.ConnectionPrx;
import java.sql.Connection;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.json.JSONObject;

@Provider
@Path("/store")
public class StoreResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(StoreResource.class));

   @POST
   @Path("/item/{id}/purchase")
   @Produces({"application/json"})
   public DataHolder<StoreItemData> purchaseFusionSession(@QueryParam("sessionId") String sessionId, @QueryParam("requestingUserid") int requestingUserId, @PathParam("id") int id, String jsonStr) throws FusionRestException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER62_ENABLED)) {
         ConnectionPrx prx = ResourceUtil.getConnectionProxy(sessionId);
         UserData userData = new UserData(prx.getUserObject().getUserData());
         if (!userData.userID.equals(requestingUserId)) {
            log.error(String.format("User session id for %s is not the same with user %s", userData.username, requestingUserId));
            throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid session id for user %s", requestingUserId));
         } else {
            return this.purchase(requestingUserId, id, jsonStr, prx.getDeviceTypeAsInt(), new AccountEntrySourceData(prx.getSessionObject()));
         }
      } else {
         return this.purchaseOld(sessionId, requestingUserId, id, jsonStr);
      }
   }

   @POST
   @Path("/item/{id}/purchaseWap")
   @Produces({"application/json"})
   public DataHolder<StoreItemData> purchaseWapSession(@QueryParam("ipAddress") String ipAddress, @QueryParam("sessionId") String sessionId, @QueryParam("mobileDevice") String mobileDevice, @QueryParam("userAgent") String userAgent, @QueryParam("requestingUserid") int requestingUserId, @PathParam("id") int id, String jsonStr) throws FusionRestException {
      SSOLoginData loginData = SSOLogin.getLoginDataFromMemcache(ResourceUtil.getSID(sessionId));
      if (loginData != null && loginData.userID.equals(requestingUserId)) {
         return this.purchase(requestingUserId, id, jsonStr, ClientType.WAP.value(), new AccountEntrySourceData(ipAddress, sessionId, mobileDevice, userAgent));
      } else {
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid session id for user %s", requestingUserId));
      }
   }

   public DataHolder<StoreItemData> purchaseOld(@QueryParam("sessionId") String sessionId, @QueryParam("requestingUserid") int requestingUserId, @PathParam("id") int id, String jsonStr) throws FusionRestException {
      ConnectionPrx prx = ResourceUtil.getConnectionProxy(sessionId);
      UserData userData = new UserData(prx.getUserObject().getUserData());
      if (!userData.userID.equals(requestingUserId)) {
         log.error(String.format("User session id for %s is not the same with user %s", userData.username, requestingUserId));
         throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("Invalid session id for user %s", requestingUserId));
      } else {
         AccountEntrySourceData accountEntrySourceData = new AccountEntrySourceData(prx.getSessionObject());

         try {
            ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            DataHolder<StoreItemData> dataHolder = this.getStoreItem(requestingUserId, id);
            StoreItemData sid = (StoreItemData)dataHolder.data;
            if (sid == null) {
               log.error(String.format("Failed to retrieved store item id for %d", id));
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find the store item id for " + id);
            } else {
               switch(sid.type) {
               case VIRTUAL_GIFT:
                  JSONObject json = new JSONObject(jsonStr);
                  String[] recepients = !StringUtil.isBlank(json.getString("to")) ? json.getString("to").split(",") : null;
                  String message = json.getString("message");
                  boolean privateGifts = json.getBoolean("private");
                  VirtualGiftData vgd = (VirtualGiftData)sid.referenceData;
                  Map<String, Integer> virtualGiftReceivedIdMap = contentBean.buyVirtualGiftForMultipleUsers(userData.username, Arrays.asList(recepients), vgd, VirtualGiftReceivedData.PurchaseLocationEnum.STORE.value(), privateGifts, message, false, false, accountEntrySourceData);
                  UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  Map<Integer, UserData> recipientUserDataList = new HashMap();
                  ClientType deviceEnum = ClientType.fromValue(prx.getDeviceTypeAsInt());
                  String receiverStr = "";
                  Iterator i$ = virtualGiftReceivedIdMap.entrySet().iterator();

                  while(i$.hasNext()) {
                     Entry<String, Integer> entry = (Entry)i$.next();
                     String recipient = (String)entry.getKey();
                     UserData recipientUserData = userBean.loadUser(recipient, false, false);
                     if (recipientUserData != null) {
                        receiverStr = receiverStr + " @" + recipient;
                        recipientUserDataList.put(entry.getValue(), recipientUserData);
                     }
                  }

                  if (json.getBoolean("postToMiniblog") && virtualGiftReceivedIdMap.keySet().size() != 0) {
                     String msg;
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW422_ENABLED) && !StringUtil.isBlank(message)) {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGE_CUSTOM), message, vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     } else if (virtualGiftReceivedIdMap.keySet().size() == 1) {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGE), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     } else {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGES), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     }

                     contentBean.createMigboTextPostForUser(userData.userID, msg, (String)null, (String)null, "1", deviceEnum, SSOEnums.View.fromFusionDeviceEnum(deviceEnum));
                  }

                  contentBean.onPurchaseVirtualGift(userData.username, recipientUserDataList, vgd, privateGifts, message, (String)null);
                  break;
               case AVATAR:
                  contentBean.buyAvatarItem(userData.userID, userData.userID, sid.referenceID, accountEntrySourceData);
                  break;
               case STICKER:
               case EMOTICON:
               case SUPER_EMOTICON:
                  contentBean.buyEmoticonPack(userData.username, sid.referenceID, accountEntrySourceData);
               case THEME:
               }

               return dataHolder;
            }
         } catch (FusionRestException var25) {
            throw var25;
         } catch (Exception var26) {
            log.error("Failed to purchase store item.", var26);
            throw new FusionRestException(FusionRestException.RestException.ERROR, var26.getMessage());
         }
      }
   }

   public DataHolder<StoreItemData> purchase(int requestingUserId, int id, String jsonStr, int device, AccountEntrySourceData accountEntrySourceData) throws FusionRestException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         DataHolder<StoreItemData> dataHolder = this.getStoreItem(requestingUserId, id);
         StoreItemData sid = (StoreItemData)dataHolder.data;
         if (sid == null) {
            log.error(String.format("Failed to retrieved store item id for %d", id));
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find the store item id for " + id);
         } else {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String requestingUsername = userBean.getUsernameByUserid(requestingUserId, (Connection)null);
            if (requestingUsername == null) {
               throw new FusionRestException(FusionRestException.RestException.ERROR, "invalid requesting user Id: " + requestingUserId);
            } else {
               switch(sid.type) {
               case VIRTUAL_GIFT:
                  JSONObject json = new JSONObject(jsonStr);
                  String[] recepients = !StringUtil.isBlank(json.getString("to")) ? json.getString("to").split(",") : null;
                  String message = json.getString("message");
                  boolean privateGifts = json.getBoolean("private");
                  VirtualGiftData vgd = (VirtualGiftData)sid.referenceData;
                  Map<String, Integer> virtualGiftReceivedIdMap = contentBean.buyVirtualGiftForMultipleUsers(requestingUsername, Arrays.asList(recepients), vgd, VirtualGiftReceivedData.PurchaseLocationEnum.STORE.value(), privateGifts, message, false, false, accountEntrySourceData);
                  Map<Integer, UserData> recipientUserDataList = new HashMap();
                  ClientType deviceEnum = ClientType.fromValue(device);
                  String receiverStr = "";
                  Iterator i$ = virtualGiftReceivedIdMap.entrySet().iterator();

                  while(i$.hasNext()) {
                     Entry<String, Integer> entry = (Entry)i$.next();
                     String recipient = (String)entry.getKey();
                     UserData recipientUserData = userBean.loadUser(recipient, false, false);
                     if (recipientUserData != null) {
                        receiverStr = receiverStr + " @" + recipient;
                        recipientUserDataList.put(entry.getValue(), recipientUserData);
                     }
                  }

                  if (json.getBoolean("postToMiniblog") && virtualGiftReceivedIdMap.keySet().size() != 0) {
                     String msg;
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW422_ENABLED) && !StringUtil.isBlank(message)) {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGE_CUSTOM), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)), message);
                     } else if (virtualGiftReceivedIdMap.keySet().size() == 1) {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGE), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     } else {
                        msg = String.format(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_POST_MESSAGES), vgd.getName(), receiverStr, vgd.getGiftUrl(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                     }

                     contentBean.createMigboTextPostForUser(requestingUserId, msg, (String)null, (String)null, "1", deviceEnum, SSOEnums.View.fromFusionDeviceEnum(deviceEnum));
                  }

                  contentBean.onPurchaseVirtualGift(requestingUsername, recipientUserDataList, vgd, privateGifts, message, (String)null);
                  break;
               case AVATAR:
                  contentBean.buyAvatarItem(requestingUserId, requestingUserId, sid.referenceID, accountEntrySourceData);
                  break;
               case STICKER:
               case EMOTICON:
               case SUPER_EMOTICON:
                  contentBean.buyEmoticonPack(requestingUsername, sid.referenceID, accountEntrySourceData);
               case THEME:
               }

               return dataHolder;
            }
         }
      } catch (FusionRestException var24) {
         throw var24;
      } catch (Exception var25) {
         log.error("Failed to purchase store item.", var25);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var25.getMessage());
      }
   }

   @GET
   @Path("/category/{id}/items")
   @Produces({"application/json"})
   public DataHolder<ListDataWrapper<StoreItemData>> getStoreItemCategory(@QueryParam("requestingUserid") int requestingUserId, @PathParam("id") int id, @QueryParam("sortby") String sortby, @QueryParam("sortorder") String sortorder, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("featured") boolean featured) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String requestingUsername = userBean.getUsernameByUserid(requestingUserId, (Connection)null);
         if (requestingUsername == null) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find requesting user id ");
         } else {
            ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            ListDataWrapper<StoreItemData> sidw = contentBean.getStoreItemsByCategory(requestingUsername, id, sortby, sortorder, offset, limit, featured);
            return new DataHolder(sidw);
         }
      } catch (Exception var12) {
         log.error(String.format("Failed to get store item category id: %d", id), var12);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var12.getMessage());
      }
   }

   @GET
   @Path("/type/{id}/items")
   @Produces({"application/json"})
   public DataHolder<ListDataWrapper<StoreItemData>> getStoreItemType(@QueryParam("requestingUserid") int requestingUserId, @PathParam("id") int typeId, @QueryParam("sortby") String sortby, @QueryParam("sortorder") String sortorder, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("featured") boolean featured) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String requestingUsername = userBean.getUsernameByUserid(requestingUserId, (Connection)null);
         if (requestingUsername == null) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find requesting user id ");
         } else {
            ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            ListDataWrapper<StoreItemData> sidw = contentBean.getStoreItemsByType(requestingUsername, typeId, sortby, sortorder, offset, limit, featured);
            return new DataHolder(sidw);
         }
      } catch (Exception var12) {
         log.error(String.format("Failed to get store item type id: %d", typeId), var12);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var12.getMessage());
      }
   }

   @GET
   @Path("/item/{id}")
   @Produces({"application/json"})
   public DataHolder<StoreItemData> getStoreItem(@QueryParam("requestingUserid") int requestingUserId, @PathParam("id") int id) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String requestingUsername = userBean.getUsernameByUserid(requestingUserId, (Connection)null);
         if (requestingUsername == null) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find requesting user id ");
         } else {
            ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
            StoreItemData sid = contentBean.getStoreItem(requestingUsername, id);
            if (sid == null) {
               log.warn(String.format("Failed to retrieve store item id %d", id));
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to get Store Item.");
            } else {
               return new DataHolder(sid);
            }
         }
      } catch (FusionRestException var7) {
         throw var7;
      } catch (Exception var8) {
         log.error(String.format("Failed to retrieve store item id: %d", id), var8);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var8.getMessage());
      }
   }

   @GET
   @Path("/category/{id}")
   @Produces({"application/json"})
   public DataHolder<StoreCategoryData> getStoreCategory(@PathParam("id") int id, @QueryParam("requestingUserid") int requestingUserId, @QueryParam("sortorder") boolean sortorder) throws FusionRestException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         return new DataHolder(contentBean.getStoreCategory(requestingUserId, id, sortorder));
      } catch (Exception var5) {
         log.error(String.format("Failed to retrieve store category id: %d", id), var5);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var5.getMessage());
      }
   }

   @GET
   @Path("/categories/{id}")
   @Produces({"application/json"})
   public DataHolder<List<StoreCategoryData>> getStoreCategories(@PathParam("id") int id, @QueryParam("requestingUserid") int requestingUserId, @QueryParam("sortorder") boolean sortorder) throws FusionRestException {
      try {
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         List<StoreCategoryData> scd = contentBean.getStoreCategories(requestingUserId, id, sortorder);
         return new DataHolder(scd);
      } catch (Exception var6) {
         log.error(String.format("Failed to retrieve store category id: %d", id), var6);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var6.getMessage());
      }
   }

   @GET
   @Path("/search/items")
   @Produces({"application/json"})
   public DataHolder<ListDataWrapper<StoreItemData>> searchStoreItems(@QueryParam("requestingUserid") int requestingUserId, @QueryParam("query") String query, @QueryParam("type") int type, @QueryParam("categoryId") Integer categoryId, @QueryParam("minPrice") double minPrice, @QueryParam("maxPrice") double maxPrice, @QueryParam("sortby") String sortby, @QueryParam("sortorder") String sortorder, @QueryParam("offset") int offset, @QueryParam("limit") int limit, @QueryParam("featured") boolean featured) throws FusionRestException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String requestingUsername = userBean.getUsernameByUserid(requestingUserId, (Connection)null);
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         ListDataWrapper<StoreItemData> sidw = contentBean.searchStoreItems(requestingUsername, query, StoreItemData.TypeEnum.fromValue(type), categoryId, minPrice, maxPrice, sortby, sortorder, offset, limit, featured);
         return new DataHolder(sidw);
      } catch (Exception var18) {
         log.error(String.format("Failed to search store item id."));
         throw new FusionRestException(FusionRestException.RestException.ERROR, var18.getMessage());
      }
   }
}
