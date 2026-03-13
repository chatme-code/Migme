package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.cache.GiftsReceivedCounter;
import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.cache.UserReferrerCache;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedDistributedLock;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MigboEnums;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.TemplateStringProcessor;
import com.projectgoth.fusion.common.jdbc.ConnectionCreator;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.AvatarItemData;
import com.projectgoth.fusion.data.ContentData;
import com.projectgoth.fusion.data.ContentPurchasedData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.EmoteData;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.ListDataWrapper;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.data.ReferenceStoreItemData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.RewardProgramCompletionData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.data.RewardProgramProcessorMappingData;
import com.projectgoth.fusion.data.RewardedBadgeData;
import com.projectgoth.fusion.data.RewardedGroupMembershipData;
import com.projectgoth.fusion.data.RewardedMigCreditData;
import com.projectgoth.fusion.data.RewardedReputationData;
import com.projectgoth.fusion.data.RewardedStoreItemData;
import com.projectgoth.fusion.data.SecurityQuestion;
import com.projectgoth.fusion.data.StoreCategoryData;
import com.projectgoth.fusion.data.StoreItemData;
import com.projectgoth.fusion.data.StoreItemInventoryData;
import com.projectgoth.fusion.data.StoreItemInventorySummaryData;
import com.projectgoth.fusion.data.StoreItemToUnlockData;
import com.projectgoth.fusion.data.StoreRatingSummaryData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.ThemeData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserReputationScoreAndLevelData;
import com.projectgoth.fusion.data.UserSettingData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.eventqueue.Event;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.UserDataUpdatedEvent;
import com.projectgoth.fusion.eventqueue.events.VirtualGiftSentEvent;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.ContentLocal;
import com.projectgoth.fusion.interfaces.ContentLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MerchantsLocal;
import com.projectgoth.fusion.interfaces.MerchantsLocalHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.WebLocal;
import com.projectgoth.fusion.interfaces.WebLocalHome;
import com.projectgoth.fusion.leaderboard.Leaderboard;
import com.projectgoth.fusion.merchant.MerchantPointsLogData;
import com.projectgoth.fusion.paintwars.ItemData;
import com.projectgoth.fusion.reputation.ReputationLevelScoreRanges;
import com.projectgoth.fusion.restapi.data.SSOEnums;
import com.projectgoth.fusion.restapi.util.MigboApiUtil;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.outcomes.BasicRewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.NotificationTemplateOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.RewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.UnlockedStoreItemsRewardProgramOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.notification.EmailTemplateIDOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.notification.IMNotificationTemplateOutcomeData;
import com.projectgoth.fusion.rewardsystem.triggers.AvatarItemPurchasedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MerchantTaggedUserMeetsRewardCriteriaTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserMeetsRewardCriteriaTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithBadgeTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithGroupMembershipTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithMigCreditTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithMigLevelTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithStoredItemTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReferredUserRewardedWithUnlockedStoredItemTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ReputationLevelIncreaseTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserMeetsRewardCriteriaTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedWithBadgeTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedWithReputationScoreTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedWithStoreItemTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.UserRewardedWithUnlockedStoreItemTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.VGReceivedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.VGSentTrigger;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ChatRoomPrxHelper;
import com.projectgoth.fusion.slice.EventSystemPrx;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.smsengine.SMSControl;
import com.projectgoth.fusion.userevent.domain.VirtualGoodType;
import com.projectgoth.leto.common.Money;
import com.projectgoth.leto.common.impl.notification.IMNotification;
import com.projectgoth.leto.common.impl.notification.MigAlertMessageNotification;
import com.projectgoth.leto.common.impl.notification.MiniblogTextNotification;
import com.projectgoth.leto.common.impl.notification.SMSNotification;
import com.projectgoth.leto.common.impl.notification.TemplatedEmailNotification;
import com.projectgoth.leto.common.impl.notification.TextEmailNotification;
import com.projectgoth.leto.common.impl.outcome.BadgeRewardDetail;
import com.projectgoth.leto.common.impl.outcome.CreditRewardDetail;
import com.projectgoth.leto.common.impl.outcome.GroupMembershipRewardDetail;
import com.projectgoth.leto.common.impl.outcome.MMv2Outcomes;
import com.projectgoth.leto.common.impl.outcome.MerchantPointsRewardDetail;
import com.projectgoth.leto.common.impl.outcome.ReputationRewardDetail;
import com.projectgoth.leto.common.impl.outcome.StoreItemRewardDetail;
import com.projectgoth.leto.common.impl.outcome.UnlockedStoreItemRewardDetail;
import com.projectgoth.leto.common.notification.Notification;
import com.projectgoth.leto.common.outcome.OutcomeDetail;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;
import java.util.Map.Entry;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ContentBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ContentBean.class));
   private int MAX_LENGTH_ACCOUNTENTRY_DESC = 128;
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private static Map<Integer, EmoticonData> emoticons;
   private static Map<String, Set<Integer>> emoticonHotkeys;
   private static Map<Integer, EmoticonPackData> emoticonPacks;
   private static Map<Integer, EmoticonPackData> stickerPacks;
   private static long emoticonsNextUpdate = -1L;
   private static long emoticonPacksNextUpdate = -1L;
   private static long emoticonHotkeysNextUpdate = -1L;
   private static long stickerPacksNextUpdate = -1L;
   private static Object emoticonsLock = new Object();
   private static Object emoticonPacksLock = new Object();
   private static Object emoticonHotkeysLock = new Object();
   private static Object stickerPacksLock = new Object();
   private static final DecimalFormat TWO_DECIMAL_POINT_FORMAT = new DecimalFormat("0.00");
   private static final double DEFAULT_GIFT_LOW_PRICE_THRESHOLD = 0.03D;
   private SessionContext context;
   private static final ArrayList<String> VALID_SORT_BY = new ArrayList(Arrays.asList("featured", "numsold", "datelisted", "name", "price"));
   private static final ArrayList<String> VALID_SORT_ORDER = new ArrayList(Arrays.asList("asc", "desc"));

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create Content EJB", var2);
         throw new CreateException("Unable to create Content EJB: " + var2.getMessage());
      }
   }

   private Map<Integer, EmoticonData> loadEmoticons() throws FusionEJBException {
      boolean flushEmoticonHeights = false;
      synchronized(emoticonsLock) {
         if (emoticons == null || emoticonsNextUpdate <= System.currentTimeMillis()) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            if (emoticons == null) {
               emoticons = new HashMap();
            }

            flushEmoticonHeights = true;

            try {
               conn = this.dataSourceSlave.getConnection();
               ps = conn.prepareStatement("select e.*, ehk.type hotkeytype, ehk.hotkey from emoticon e left outer join emoticonhotkey ehk on e.id = ehk.emoticonid order by e.id");
               rs = ps.executeQuery();

               while(rs.next()) {
                  EmoticonData emoticonData = new EmoticonData(rs);

                  do {
                     String hotKey = rs.getString("hotKey");
                     if (hotKey != null && hotKey.length() > 0) {
                        EmoticonData.HotKeyTypeEnum type = EmoticonData.HotKeyTypeEnum.fromValue(rs.getInt("hotkeytype"));
                        if (type == EmoticonData.HotKeyTypeEnum.PRIMARY) {
                           emoticonData.hotKey = hotKey;
                        } else {
                           emoticonData.alternateHotKeys.add(hotKey);
                        }
                     }
                  } while(rs.next() && emoticonData.id.equals((Integer)rs.getObject("id")));

                  rs.previous();
                  emoticons.put(emoticonData.id, emoticonData);
               }
            } catch (SQLException var25) {
               log.error("Exception occured in loadEmoticons: ", var25);
               throw new FusionEJBException("Unable to load emoticons: " + var25.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var24) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var23) {
                  ps = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var22) {
                  conn = null;
               }

            }

            emoticonsNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.CONTENT_EMOTICONS) * 1000L;
         }
      }

      if (flushEmoticonHeights) {
         ContentBean.SingletonHolder.getEmoticonHeightsLoader().invalidateCache();
      }

      return emoticons;
   }

   private Map<String, Set<Integer>> loadEmoticonHotkeys() throws FusionEJBException {
      synchronized(emoticonHotkeysLock) {
         if (emoticonHotkeys == null || emoticonHotkeysNextUpdate <= System.currentTimeMillis()) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            String hotkey = null;
            if (emoticonHotkeys == null) {
               emoticonHotkeys = new HashMap();
            }

            try {
               conn = this.dataSourceSlave.getConnection();
               ps = conn.prepareStatement("select * from emoticonhotkey order by hotkey, emoticonid");

               for(rs = ps.executeQuery(); rs.next(); ((Set)emoticonHotkeys.get(hotkey)).add(rs.getInt("emoticonid"))) {
                  hotkey = rs.getString("hotkey").toLowerCase();
                  if (emoticonHotkeys.get(hotkey) == null || !emoticonHotkeys.containsKey(hotkey)) {
                     emoticonHotkeys.put(hotkey, new TreeSet());
                  }
               }
            } catch (SQLException var23) {
               log.error("Exception occured in loadEmoticonHotkeys: ", var23);
               throw new FusionEJBException("Unable to load emoticon hotkeys: " + var23.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var22) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var21) {
                  ps = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var20) {
                  conn = null;
               }

            }

            emoticonHotkeysNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.CONTENT_EMOTICONHOTKEYS) * 1000L;
         }
      }

      return emoticonHotkeys;
   }

   private Map<Integer, EmoticonPackData> loadEmoticonPacks() throws FusionEJBException {
      synchronized(emoticonPacksLock) {
         if (emoticonPacks == null || emoticonPacksNextUpdate <= System.currentTimeMillis()) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            EmoticonPackData emoticonPackData = null;
            int emoticons = 0;
            if (emoticonPacks == null) {
               emoticonPacks = new HashMap();
            }

            try {
               conn = this.dataSourceSlave.getConnection();
               stmt = conn.prepareStatement("select ep.*, e.id as emoticonid, e.type as contenttype, s.catalogimage as thumbnailfile,s.catalogimage as catalogimage from emoticonpack ep join emoticon e on ep.id = e.emoticonpackid  left join storeitem s on (s.referenceid = ep.id and s.type = ?)  where ep.status = ?  and e.type in (1,2,3,4) order by ep.id");
               stmt.setInt(1, StoreItemData.TypeEnum.STICKER.value());
               stmt.setInt(2, EmoticonPackData.StatusEnum.ACTIVE.value());
               rs = stmt.executeQuery();

               while(rs.next()) {
                  emoticonPackData = this.readEmoticonPackFromResultSet(rs);
                  emoticonPacks.put(emoticonPackData.getId(), emoticonPackData);
                  emoticons += emoticonPackData.getEmoticonIDs().size();
                  rs.previous();
               }
            } catch (SQLException var24) {
               log.error("Exception occured in loadEmoticonPacks: ", var24);
               throw new FusionEJBException("Unable to load emoticon packs: " + var24.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var23) {
                  rs = null;
               }

               try {
                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (SQLException var22) {
                  stmt = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var21) {
                  conn = null;
               }

            }

            emoticonPacksNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.CONTENT_EMOTICONPACKS) * 1000L;
         }
      }

      return emoticonPacks;
   }

   private Map<Integer, EmoticonPackData> loadStickerPacks() {
      synchronized(stickerPacksLock) {
         if (stickerPacks == null || stickerPacksNextUpdate <= System.currentTimeMillis()) {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            EmoticonPackData emoticonPackData = null;
            int emoticons = 0;
            if (stickerPacks == null) {
               stickerPacks = new HashMap();
            }

            try {
               conn = this.dataSourceSlave.getConnection();
               boolean previewImage = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.LOAD_THUMBNAIL_STICKER_DIR_AS_PREVIEW);
               stmt = conn.prepareStatement("select ep.*, e.id as emoticonid , e.type as contenttype, " + (previewImage ? "s.previewimage" : "s.catalogimage") + " as thumbnailfile,s.catalogimage as catalogimage " + " from emoticonpack ep join emoticon e on ep.id = e.emoticonpackid" + " left join storeitem s on (s.referenceid = ep.id AND s.type = ?) " + " where ep.status = ? " + " and e.type = ?" + " order by ep.id");
               stmt.setInt(1, StoreItemData.TypeEnum.STICKER.value());
               stmt.setInt(2, EmoticonPackData.StatusEnum.ACTIVE.value());
               stmt.setInt(3, EmoticonData.TypeEnum.STICKER.value());
               rs = stmt.executeQuery();

               while(rs.next()) {
                  emoticonPackData = this.readEmoticonPackFromResultSet(rs);
                  stickerPacks.put(emoticonPackData.getId(), emoticonPackData);
                  emoticons += emoticonPackData.getEmoticonIDs().size();
                  rs.previous();
               }
            } catch (SQLException var24) {
               throw new EJBException("Unable to load sticker packs", var24);
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var23) {
                  rs = null;
               }

               try {
                  if (stmt != null) {
                     stmt.close();
                  }
               } catch (SQLException var22) {
                  stmt = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var21) {
                  conn = null;
               }

            }

            stickerPacksNextUpdate = System.currentTimeMillis() + SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.EJBCacheDuration.CONTENT_EMOTICONPACKS) * 1000L;
         }
      }

      return stickerPacks;
   }

   private SortedSet<Integer> loadEmoticonHeights() throws FusionEJBException {
      return (SortedSet)ContentBean.SingletonHolder.getEmoticonHeightsLoader().getValue();
   }

   private static SortedSet<Integer> fetchEmoticonHeightsFromDB(DataSource dataSource) throws SQLException {
      SortedSet<Integer> emoticonHeights = new TreeSet();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = dataSource.getConnection();
         ps = conn.prepareStatement("select distinct height from emoticon where type in (1,2,3,4) order by height");
         rs = ps.executeQuery();

         while(rs.next()) {
            emoticonHeights.add(rs.getInt(1));
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var17) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

      return emoticonHeights;
   }

   private List<Integer> getEmoticonPacksForUser(String username) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Object var23;
      try {
         List<Integer> emoticonIDs = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, username);
         if (emoticonIDs == null) {
            conn = this.dataSourceSlave.getConnection();
            String sql = "select emoticonpackid from emoticonpackowner where username = ? and status = 1 union select p.id from emoticonpack p, subscription s where p.serviceid = s.serviceid and p.type = ? and s.username = ? and s.status = ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setInt(2, EmoticonPackData.TypeEnum.PREMIUM_SUBSCRIPTION.value());
            ps.setString(3, username);
            ps.setInt(4, SubscriptionData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            emoticonIDs = new ArrayList();

            while(rs.next()) {
               ((List)emoticonIDs).add(rs.getInt(1));
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, username, emoticonIDs);
         }

         var23 = emoticonIDs;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return (List)var23;
   }

   public void invalidateLocalCachedItems() {
      synchronized(emoticonPacksLock) {
         emoticonPacksNextUpdate = -1L;
      }

      synchronized(stickerPacksLock) {
         stickerPacksNextUpdate = -1L;
      }

      synchronized(emoticonsLock) {
         emoticonsNextUpdate = -1L;
      }

      synchronized(emoticonHotkeysLock) {
         emoticonHotkeysNextUpdate = -1L;
      }

      ContentBean.SingletonHolder.getEmoticonHeightsLoader().invalidateCache();
      ContentBean.SingletonHolder.getDispatchableReferredUserRewardedTriggerTypes().invalidateCache();
      ContentBean.SingletonHolder.getDispatchableUserRewardedTriggerTypes().invalidateCache();
   }

   public int getOptimalEmoticonHeight(String username, int fontHeight) throws FusionEJBException, EJBException {
      Integer previous = 0;

      Integer height;
      for(Iterator i$ = this.loadEmoticonHeights().iterator(); i$.hasNext(); previous = height) {
         height = (Integer)i$.next();
         if (height > fontHeight) {
            return previous == 0 ? (Integer)this.loadEmoticonHeights().first() : previous;
         }
      }

      return previous;
   }

   public EmoticonData getEmoticon(int id) throws EJBException, FusionEJBException {
      return (EmoticonData)this.loadEmoticons().get(id);
   }

   private List<EmoticonData> getEmoticons(List<Integer> emoticonIDList) throws FusionEJBException, EJBException {
      List<EmoticonData> emoticonDatas = new ArrayList(emoticonIDList.size());
      Iterator i$ = emoticonIDList.iterator();

      while(i$.hasNext()) {
         Integer emoticonID = (Integer)i$.next();
         EmoticonData emoticonData = this.getEmoticon(emoticonID);
         if (emoticonData != null) {
            emoticonDatas.add(emoticonData);
         }
      }

      return emoticonDatas;
   }

   public EmoticonData getEmoticon(String hotKey, int height) throws FusionEJBException, EJBException {
      Set<Integer> emoticons = (Set)this.loadEmoticonHotkeys().get(hotKey.toLowerCase());
      if (emoticons == null) {
         return null;
      } else {
         EmoticonData optimalEmoticon = null;
         Iterator i$ = emoticons.iterator();

         while(true) {
            EmoticonData emoticonData;
            do {
               do {
                  if (!i$.hasNext()) {
                     return optimalEmoticon;
                  }

                  Integer emoticon = (Integer)i$.next();
                  emoticonData = this.getEmoticon(emoticon);
                  if (emoticonData.height == height) {
                     return emoticonData;
                  }
               } while(emoticonData.height >= height);
            } while(optimalEmoticon != null && emoticonData.height <= optimalEmoticon.height);

            optimalEmoticon = emoticonData;
         }
      }
   }

   private static int compareSize(EmoticonData emoticonData, int size) {
      return emoticonData.type == EmoticonData.TypeEnum.STICKER ? emoticonData.width - size : emoticonData.height - size;
   }

   private static int compareSize(EmoticonData emoticonData1, EmoticonData emoticonData2) {
      return emoticonData1.type == EmoticonData.TypeEnum.STICKER ? emoticonData1.width - emoticonData2.width : emoticonData1.height - emoticonData2.height;
   }

   public List<EmoticonData> getAllEmoticonDataByHotKey(String hotKey) throws FusionEJBException, EJBException {
      if (StringUtil.isBlank(hotKey)) {
         return null;
      } else {
         Set<Integer> emoticonsIDSet = (Set)this.loadEmoticonHotkeys().get(hotKey.toLowerCase());
         if (emoticonsIDSet != null && emoticonsIDSet.size() != 0) {
            List<EmoticonData> emoticonDataList = new ArrayList();
            Iterator<Integer> emoticonIDIterator = emoticonsIDSet.iterator();
            EmoticonData emoticonData = null;

            while(emoticonIDIterator.hasNext()) {
               Integer emoticonsID = (Integer)emoticonIDIterator.next();
               emoticonData = this.getEmoticon(emoticonsID);
               if (emoticonData != null) {
                  emoticonDataList.add(emoticonData);
               }
            }

            return emoticonDataList;
         } else {
            return null;
         }
      }
   }

   public EmoticonData getByHotKey(String hotKey, int emoticonHeight, int stickerWidth) throws FusionEJBException, EJBException {
      if (StringUtil.isBlank(hotKey)) {
         return null;
      } else {
         Set<Integer> emoticonsIDSet = (Set)this.loadEmoticonHotkeys().get(hotKey.toLowerCase());
         if (emoticonsIDSet != null && emoticonsIDSet.size() != 0) {
            Iterator<Integer> emoticonIDIterator = emoticonsIDSet.iterator();
            if (emoticonIDIterator.hasNext()) {
               Integer emoticonsID = (Integer)emoticonIDIterator.next();
               EmoticonData emoticonData = this.getEmoticon(emoticonsID);
               EmoticonData.TypeEnum detectedType = emoticonData.type;
               int requestedSize;
               if (detectedType == EmoticonData.TypeEnum.STICKER) {
                  requestedSize = stickerWidth;
               } else {
                  requestedSize = emoticonHeight;
               }

               int firstDiff = compareSize(emoticonData, requestedSize);
               if (firstDiff == 0) {
                  return emoticonData;
               } else {
                  boolean foundSmallerOrEqualSize = firstDiff <= 0;

                  while(emoticonIDIterator.hasNext()) {
                     Integer nextEmoticonID = (Integer)emoticonIDIterator.next();
                     EmoticonData nextEmoticonData = this.getEmoticon(nextEmoticonID);
                     if (nextEmoticonData.type == detectedType) {
                        int diffWithRequestedSize = compareSize(nextEmoticonData, requestedSize);
                        if (diffWithRequestedSize == 0) {
                           return nextEmoticonData;
                        }

                        if (diffWithRequestedSize < 0) {
                           if (!foundSmallerOrEqualSize) {
                              emoticonData = nextEmoticonData;
                              foundSmallerOrEqualSize = true;
                           } else if (compareSize(emoticonData, nextEmoticonData) < 0) {
                              emoticonData = nextEmoticonData;
                           }
                        } else if (!foundSmallerOrEqualSize && compareSize(emoticonData, nextEmoticonData) > 0) {
                           emoticonData = nextEmoticonData;
                        }
                     } else {
                        log.warn("Skipping emoticon/sticker ID [" + nextEmoticonID + "] whose type is [" + nextEmoticonData.type + "] while we are expecting type [" + detectedType + "]");
                     }
                  }

                  return emoticonData;
               }
            } else {
               return null;
            }
         } else {
            return null;
         }
      }
   }

   public List<EmoticonData> getEmoticons(String username) throws FusionEJBException, EJBException {
      List<Integer> emoticonPackIDs = this.getEmoticonPacksForUser(username);
      emoticonPackIDs.add(0, 1);
      List<EmoticonData> emoticons = new ArrayList();
      Iterator i$ = emoticonPackIDs.iterator();

      while(i$.hasNext()) {
         Integer emoticonPackID = (Integer)i$.next();
         if (this.loadEmoticonPacks().containsKey(emoticonPackID)) {
            emoticons.addAll(this.getEmoticons(((EmoticonPackData)this.loadEmoticonPacks().get(emoticonPackID)).getEmoticonIDs()));
         }
      }

      return emoticons;
   }

   public List<SecurityQuestion> getSecurityQeustions() throws FusionEJBException, EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from securityquestion where type = 1");
         List<SecurityQuestion> questions = new ArrayList();
         rs = ps.executeQuery();

         while(rs.next()) {
            SecurityQuestion question = new SecurityQuestion(rs);
            questions.add(question);
         }

         ArrayList var22 = questions;
         return var22;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public List<EmoticonData> getStickerDataListForUser(String username) throws EJBException {
      List<Integer> userEmoticonPackIDs = this.getEmoticonPacksForUser(username);
      String[] defaultFreeStickerPacks = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.DEFAULT_FREE_STICKER_PACKS);
      String[] arr$ = defaultFreeStickerPacks;
      int len$ = defaultFreeStickerPacks.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String packID = arr$[i$];
         userEmoticonPackIDs.add(Integer.valueOf(packID));
      }

      ArrayList stickers = new ArrayList();

      try {
         Map<Integer, EmoticonPackData> stickerPackMap = this.loadStickerPacks();
         Iterator i$ = userEmoticonPackIDs.iterator();

         while(true) {
            EmoticonPackData stickerPackData;
            do {
               do {
                  if (!i$.hasNext()) {
                     return stickers;
                  }

                  Integer emoticonPackID = (Integer)i$.next();
                  stickerPackData = (EmoticonPackData)stickerPackMap.get(emoticonPackID);
               } while(stickerPackData == null);
            } while(stickerPackData.getContentType() != EmoticonPackData.ContentTypeEnum.STICKER);

            Iterator i$ = stickerPackData.getEmoticonIDs().iterator();

            while(i$.hasNext()) {
               Integer emoticonID = (Integer)i$.next();
               EmoticonData emoticonData = this.getEmoticon(emoticonID);
               if (emoticonData.type == EmoticonData.TypeEnum.STICKER) {
                  stickers.add(emoticonData);
               }
            }
         }
      } catch (FusionEJBException var12) {
         throw new EJBException(var12);
      }
   }

   public EmoticonData getStickerDataByNameForUser(String sanitizedUsername, String sanitizedStickerName) {
      if (StringUtil.isBlank(sanitizedUsername)) {
         return null;
      } else if (StringUtil.isBlank(sanitizedStickerName)) {
         return null;
      } else {
         List<EmoticonData> stickerList = this.getStickerDataListForUser(sanitizedUsername);
         Iterator i$ = stickerList.iterator();

         EmoticonData sticker;
         do {
            if (!i$.hasNext()) {
               return null;
            }

            sticker = (EmoticonData)i$.next();
         } while(!sticker.alias.equalsIgnoreCase(sanitizedStickerName));

         return sticker;
      }
   }

   public List<Integer> getStickerPackIDListForUser(String username) throws EJBException {
      List<Integer> emoticonPackIDs = this.getEmoticonPacksForUser(username);
      String[] defaultFreeStickerPacks = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.DEFAULT_FREE_STICKER_PACKS);
      String[] arr$ = defaultFreeStickerPacks;
      int len$ = defaultFreeStickerPacks.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         String packID = arr$[i$];
         emoticonPackIDs.add(Integer.valueOf(packID));
      }

      Map<Integer, EmoticonPackData> stickerPacksMap = this.loadStickerPacks();
      List<Integer> stickerPackIDS = new ArrayList();
      Iterator i$ = emoticonPackIDs.iterator();

      while(i$.hasNext()) {
         Integer emoticonPackID = (Integer)i$.next();
         if (stickerPacksMap.containsKey(emoticonPackID)) {
            stickerPackIDS.add(emoticonPackID);
         }
      }

      return stickerPackIDS;
   }

   public List<EmoticonPackData> getStickerPackDataListForUser(String username) throws EJBException {
      List<Integer> stickerPackIDList = this.getStickerPackIDListForUser(username);
      List<EmoticonPackData> stickerPackDataList = new ArrayList();
      Iterator i$ = stickerPackIDList.iterator();

      while(i$.hasNext()) {
         Integer packID = (Integer)i$.next();
         EmoticonPackData packData = (EmoticonPackData)this.loadStickerPacks().get(packID);
         if (packData != null && packData.getContentType() == EmoticonPackData.ContentTypeEnum.STICKER) {
            stickerPackDataList.add(packData);
         }
      }

      return stickerPackDataList;
   }

   public int getEmoticonPackCountForUser(String username) {
      try {
         List<Integer> emoticonPackIds = this.getEmoticonPacksForUser(username);
         emoticonPackIds.add(0, 1);
         return emoticonPackIds.size();
      } catch (EJBException var3) {
         log.error("Unable to get emoticon packs for user: " + var3);
         return 2;
      }
   }

   public List<EmoticonData> getAllEmoticons() throws FusionEJBException, EJBException {
      List<EmoticonData> allEmoticons = new ArrayList();
      allEmoticons.addAll(this.loadEmoticons().values());
      return allEmoticons;
   }

   public List<EmoticonData> getEmoticonPack(int emoticonPackId) throws FusionEJBException, EJBException {
      EmoticonPackData emoticonPackData = (EmoticonPackData)this.loadEmoticonPacks().get(emoticonPackId);
      if (emoticonPackData != null) {
         List<Integer> emoticonIDs = emoticonPackData.getEmoticonIDs();
         return this.getEmoticons(emoticonIDs);
      } else {
         return Collections.emptyList();
      }
   }

   public List<EmoticonData> getStickerDataListForStickerPack(int stickerPackId) throws EJBException {
      try {
         EmoticonPackData packData = (EmoticonPackData)this.loadStickerPacks().get(stickerPackId);
         if (packData != null && packData.getContentType() == EmoticonPackData.ContentTypeEnum.STICKER) {
            List<Integer> stickerIdList = packData.getEmoticonIDs();
            Map<Integer, EmoticonData> emoticonsMap = this.loadEmoticons();
            List<EmoticonData> stickerDataList = new ArrayList();
            Iterator i$ = stickerIdList.iterator();

            while(i$.hasNext()) {
               Integer stickerId = (Integer)i$.next();
               EmoticonData stickerData = (EmoticonData)emoticonsMap.get(stickerId);
               if (stickerData.type == EmoticonData.TypeEnum.STICKER) {
                  stickerDataList.add(stickerData);
               }
            }

            return stickerDataList;
         } else {
            return Collections.emptyList();
         }
      } catch (FusionEJBException var9) {
         throw new EJBException("Exception caught:" + var9, var9);
      }
   }

   public EmoticonPackData getStickerPackData(int stickerPackId) throws EJBException {
      return (EmoticonPackData)this.loadStickerPacks().get(stickerPackId);
   }

   public List<EmoticonPackData> getStickerPackDataList(Collection<Integer> stickerPackIds) throws EJBException {
      List<EmoticonPackData> result = new ArrayList(stickerPackIds.size());
      Iterator i$ = stickerPackIds.iterator();

      while(i$.hasNext()) {
         Integer stickerPackId = (Integer)i$.next();
         EmoticonPackData emoticonPackData = this.getStickerPackData(stickerPackId);
         if (emoticonPackData != null && emoticonPackData.getContentType() == EmoticonPackData.ContentTypeEnum.STICKER) {
            result.add(emoticonPackData);
         }
      }

      return result;
   }

   public EmoticonData getStickerData(int stickerId) throws EJBException {
      try {
         EmoticonData stickerData = (EmoticonData)this.loadEmoticons().get(stickerId);
         return stickerData != null && stickerData.type == EmoticonData.TypeEnum.STICKER ? stickerData : null;
      } catch (FusionEJBException var3) {
         throw new EJBException("Exception caught:" + var3, var3);
      }
   }

   public List<ContentData> getTopWallpaper(int count) throws Exception {
      return this.getTopContent(ContentData.TypeEnum.WALLPAPER, count);
   }

   public List<ContentData> getTopRingtones(int count) throws Exception {
      return this.getTopContent(ContentData.TypeEnum.RINGTONE, count);
   }

   private List<ContentData> getTopContent(ContentData.TypeEnum contentType, int count) throws Exception {
      List<ContentData> content = new ArrayList();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select count(contentpurchased.id) as purchased, content.id, content.contentcategoryid, content.contentproviderid, contentprovider.name contentprovidername, content.type, content.name, content.artist, content.countryid, content.price, content.price/pricecurrency.exchangerate baseprice, content.currency, content.wholesalecost, content.wholesalecost/costcurrency.exchangerate basewholesalecost, content.wholesalecostcurrency, content.preview, content.previewwidth, content.previewheight, content.providerid, content.status, content.thumbnail from contentpurchased inner join content on content.id = contentpurchased.contentid and content.type = ? inner join currency pricecurrency on pricecurrency.code = content.currency inner join currency costcurrency on costcurrency.code = content.wholesalecostcurrency inner join contentprovider on contentprovider.id = content.contentproviderid where content.status = 1 group by content.id, content.name order by purchased desc limit ?");
         ps.setInt(1, contentType.value());
         ps.setInt(2, count);
         rs = ps.executeQuery();

         while(rs.next()) {
            ContentData data = new ContentData(rs);
            data.thumbnail = rs.getString("thumbnail");
            content.add(data);
         }

         ArrayList var24 = content;
         return var24;
      } catch (Exception var22) {
         throw new EJBException("Unable to load top contents: " + var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }
   }

   private EmoticonPackData readEmoticonPackFromResultSet(ResultSet rs) throws SQLException {
      EmoticonPackData emoticonPack = new EmoticonPackData();
      emoticonPack.setId(rs.getInt("id"));
      emoticonPack.setType(EmoticonPackData.TypeEnum.fromValue(rs.getInt("type")));
      emoticonPack.setName(rs.getString("name"));
      emoticonPack.setDescription(rs.getString("description"));
      emoticonPack.setPrice(rs.getDouble("price"));
      emoticonPack.setServiceID(rs.getInt("ServiceID"));
      emoticonPack.setGroupID(rs.getInt("GroupID"));
      emoticonPack.setServiceID(rs.getInt("ServiceID"));
      emoticonPack.setGroupVIPOnly(rs.getBoolean("GroupVIPOnly"));
      emoticonPack.setForSale(rs.getBoolean("ForSale"));
      emoticonPack.setSortOrder(rs.getInt("SortOrder"));
      emoticonPack.setStatus(EmoticonPackData.StatusEnum.fromValue(rs.getInt("status")));
      emoticonPack.setVersion(rs.getInt("version"));
      emoticonPack.setThumbnailFile(rs.getString("thumbnailfile"));
      emoticonPack.setCatalogImage(rs.getString("catalogimage"));
      int contentType = rs.getInt("contenttype");
      if (contentType == 5) {
         emoticonPack.setContentType(EmoticonPackData.ContentTypeEnum.STICKER);
      } else {
         emoticonPack.setContentType(EmoticonPackData.ContentTypeEnum.EMOTICON);
      }

      do {
         emoticonPack.addEmoticonID(rs.getInt("emoticonid"));
      } while(rs.next() && emoticonPack.getId().equals(rs.getInt("id")));

      return emoticonPack;
   }

   public List<EmoteData> getEmotes() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         List<EmoteData> emoteDataList = new ArrayList();
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from emote");
         rs = ps.executeQuery();

         while(rs.next()) {
            EmoteData emoteData = new EmoteData();
            emoteData.id = (Integer)rs.getObject("id");
            emoteData.command = rs.getString("command");
            emoteData.action = rs.getString("action");
            emoteData.actionWithTarget = rs.getString("actionWithTarget");
            emoteDataList.add(emoteData);
         }

         ArrayList var22 = emoteDataList;
         return var22;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }
   }

   public void updateEmoticonPackStatus(String username, int emoticonPackId, int status) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("UPDATE emoticonpackowner SET Status=? WHERE Username=? AND EmoticonPackID=?");
         ps.setInt(1, status);
         ps.setString(2, username);
         ps.setInt(3, emoticonPackId);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Internal Server Error (Unable to update emoticon pack)");
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, username);
         UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
         if (userPrx != null) {
            userPrx.emoticonPackActivated(emoticonPackId);
         }
      } catch (SQLException var19) {
         throw new EJBException(var19.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

   }

   public void buyEmoticonPack(String username, int emoticonPackId, AccountEntrySourceData accountEntrySourceData) throws FusionEJBException, EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.BUY_EMOTICONPACK, userBean.getUserAuthenticatedAccessControlParameter(username)) && SystemProperty.getBool("StoreItemPurchaseDisabledForUnauthenticatedUsers", false)) {
            throw new EJBException("You must be authenticated before you can purchase an emoticon pack.");
         }

         EmoticonPackData emoticonPack = (EmoticonPackData)this.loadEmoticonPacks().get(emoticonPackId);
         if (emoticonPack == null) {
            emoticonPack = (EmoticonPackData)this.loadStickerPacks().get(emoticonPackId);
         }

         if (emoticonPack == null || !emoticonPack.isForSale()) {
            throw new EJBException("Invalid emoticon pack");
         }

         String packName = emoticonPack.getName();
         double packPrice = emoticonPack.getPrice();
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from emoticonpackowner where username = ? and emoticonpackid = ?");
         ps.setString(1, username);
         ps.setInt(2, emoticonPackId);
         rs = ps.executeQuery();
         if (rs.next()) {
            throw new EJBException("You already own the " + packName + " emoticon pack");
         }

         rs.close();
         ps.close();
         conn.close();
         if (emoticonPack.getGroupID() > 0) {
            this.checkCanPurchaseGroupExclusiveItem(username, emoticonPack.getGroupID(), emoticonPack.isGroupVIPOnly(), emoticonPack.getName() + " emoticon pack");
         }

         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         if (emoticonPack.getType() == EmoticonPackData.TypeEnum.PREMIUM_SUBSCRIPTION) {
            accountBean.subscribeService(username, emoticonPack.getServiceID(), accountEntrySourceData);
            contentBean.incrementStoreItemSold(StoreItemData.TypeEnum.SUPER_EMOTICON, emoticonPackId, (Connection)null);
         } else {
            conn = this.dataSourceMaster.getConnection();
            if (!accountBean.userCanAffordCost(username, packPrice, CurrencyData.baseCurrency, conn)) {
               throw new EJBException("You do not have enough credit to purchase the emoticon pack");
            }

            ps = conn.prepareStatement("insert into emoticonpackowner (username, emoticonpackid) values (?, ?)");
            ps.setString(1, username);
            ps.setInt(2, emoticonPackId);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
               throw new EJBException("Internal Server Error (Unable to record purchase)");
            }

            AccountEntryData accountEntry = new AccountEntryData();
            accountEntry.username = username;
            accountEntry.type = AccountEntryData.TypeEnum.EMOTICON_PURCHASE;
            accountEntry.reference = (new Integer(emoticonPackId)).toString();
            accountEntry.description = "Purchase of the emoticon pack " + packName;
            accountEntry.currency = CurrencyData.baseCurrency;
            accountEntry.amount = -packPrice;
            accountEntry.tax = 0.0D;
            accountBean.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
            contentBean.incrementStoreItemSold(StoreItemData.TypeEnum.EMOTICON, emoticonPackId, conn);
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, username);
         UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(username);
         if (userPrx != null) {
            userPrx.emoticonPackActivated(emoticonPackId);
         }

         if (emoticonPack.getGroupID() == 0) {
            if (emoticonPack.getType() == EmoticonPackData.TypeEnum.PREMIUM_SUBSCRIPTION) {
               this.onPurchaseVirtualGoods(username, VirtualGoodType.PREMIUM_EMOTICON_PACK, emoticonPackId, emoticonPack.getName());
            } else {
               this.onPurchaseVirtualGoods(username, VirtualGoodType.EMOTICON_PACK, emoticonPackId, emoticonPack.getName());
            }
         }
      } catch (SQLException var31) {
         throw new EJBException(var31.getMessage());
      } catch (CreateException var32) {
         throw new EJBException(var32.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var28) {
            conn = null;
         }

      }

   }

   public void saveMobileContentItem(ContentData contentData, String categoryName, int categoryParentID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select ID from ContentCategory where Name=? and ParentContentCategoryID=?");
         ps.setString(1, categoryName);
         ps.setInt(2, categoryParentID);
         rs = ps.executeQuery();
         if (rs.next()) {
            contentData.contentCategoryID = rs.getInt(1);
         } else {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("insert into contentcategory (Name, ParentContentCategoryID) values (?, ?)", 1);
            ps.setString(1, categoryName);
            ps.setInt(2, categoryParentID);
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Unable to obtain the ID of the inserted category");
            }

            contentData.contentCategoryID = rs.getInt(1);
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select * from Content where ContentCategoryID=? and Type=? and ProviderID=? and Status=1");
         ps.setInt(1, contentData.contentCategoryID);
         ps.setInt(2, contentData.type.value());
         ps.setString(3, contentData.providerID);
         rs = ps.executeQuery();
         if (rs.next()) {
            if (!contentData.name.equals(rs.getString("Name")) || contentData.artist != null && !contentData.artist.equals(rs.getString("Artist")) || contentData.price != rs.getDouble("Price") || !contentData.currency.equals(rs.getString("Currency")) || !contentData.preview.equals(rs.getString("Preview"))) {
               int contentID = rs.getInt("ID");
               rs.close();
               ps.close();
               ps = conn.prepareStatement("update Content set Name=?, Artist=?, Price=?, Currency=?, Preview=? where ID=?");
               ps.setString(1, contentData.name);
               ps.setString(2, contentData.artist);
               ps.setDouble(3, contentData.price);
               ps.setString(4, contentData.currency);
               ps.setString(5, contentData.preview);
               ps.setInt(6, contentID);
            }
         } else {
            rs.close();
            ps.close();
            ps = conn.prepareStatement("insert into Content (ContentCategoryID, Type, Name, Artist, Price, Currency, Preview, ProviderID, Status) values (?,?,?,?,?,?,?,?,?)");
            ps.setInt(1, contentData.contentCategoryID);
            ps.setInt(2, contentData.type.value());
            ps.setString(3, contentData.name);
            ps.setString(4, contentData.artist);
            ps.setDouble(5, contentData.price);
            ps.setString(6, contentData.currency);
            ps.setString(7, contentData.preview);
            ps.setString(8, contentData.providerID);
            ps.setInt(9, ContentData.StatusEnum.AVAILABLE.value());
            ps.executeUpdate();
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

   }

   public String buyMobileContentItem(String username, int contentId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var33;
      try {
         conn = this.dataSourceMaster.getConnection();
         ContentData contentData = new ContentData(conn, contentId);
         if (contentData.status != ContentData.StatusEnum.AVAILABLE) {
            throw new EJBException("The content item is no longer available");
         }

         if (contentData.groupID > 0) {
            this.checkCanPurchaseGroupExclusiveItem(username, contentData.groupID, contentData.groupVIPOnly, contentData.name + " " + contentData.type.toString().toLowerCase());
         }

         ps = conn.prepareStatement("select id, refunded, numdownloads from contentpurchased where username = ? and contentid = ? and datecreated > date_sub(now(), interval 1 day) order by id desc limit 1");
         ps.setString(1, username);
         ps.setInt(2, contentId);
         rs = ps.executeQuery();
         int contentPurchasedId = 0;
         boolean contentAlreadyPurchased = false;
         if (rs.next() && !rs.getBoolean("refunded") && rs.getInt("numdownloads") < 3) {
            contentAlreadyPurchased = true;
            contentPurchasedId = rs.getInt("id");
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select mobilephone from user where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to load mobile phone number");
         }

         String mobilePhone = rs.getString(1);
         rs.close();
         ps.close();
         if (!contentAlreadyPurchased) {
            AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
            double balance = accountBean.getAccountBalance(username).getBaseBalance();
            if (balance < contentData.baseCurrencyPrice) {
               throw new EJBException("You do not have enough credit to purchase the item");
            }

            ps = conn.prepareStatement("insert into contentpurchased (username, datecreated, mobilephone, contentid, providercontentid) values (?, now(), ?, ?, ?)", 1);
            ps.setString(1, username);
            ps.setString(2, mobilePhone);
            ps.setInt(3, contentId);
            ps.setString(4, contentData.providerID);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
               throw new EJBException("Internal Server Error (Unable to record purchase)");
            }

            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Unable to obtain the ID of the new contentpurchased entry");
            }

            contentPurchasedId = rs.getInt(1);
            rs.close();
            ps.close();
            AccountEntryData accountEntry = new AccountEntryData();
            accountEntry.username = username;
            accountEntry.type = AccountEntryData.TypeEnum.CONTENT_ITEM_PURCHASE;
            accountEntry.reference = (new Integer(contentPurchasedId)).toString();
            accountEntry.description = "Purchase of mobile content " + contentData.type.toString().toLowerCase() + " " + contentData.name;
            accountEntry.currency = CurrencyData.baseCurrency;
            accountEntry.amount = -contentData.baseCurrencyPrice;
            accountEntry.tax = 0.0D;
            accountEntry.wholesaleCost = contentData.baseCurrencyWholesaleCost;
            accountBean.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
         }

         String downloadURL = null;
         if (contentData.contentProviderName.equalsIgnoreCase("iLoop")) {
            this.getDownloadURLFromiLoop(mobilePhone, contentData.providerID, contentData.orderURL);
         } else if (contentData.contentProviderName.equalsIgnoreCase("Oplayo")) {
            downloadURL = this.getDownloadURLFromOplayo(conn, mobilePhone, contentData.providerID, contentData.orderURL, contentPurchasedId);
         } else if (contentData.contentProviderName.equalsIgnoreCase("Elasitas")) {
            downloadURL = this.getDownloadURLFromElasitas(conn, mobilePhone, contentData.providerID, contentPurchasedId);
         }

         if (contentData.groupID == 0) {
            if (contentData.type == ContentData.TypeEnum.RINGTONE) {
               this.onPurchaseVirtualGoods(username, VirtualGoodType.RINGTONE, contentId, contentData.name);
            } else if (contentData.type == ContentData.TypeEnum.WALLPAPER) {
               this.onPurchaseVirtualGoods(username, VirtualGoodType.WALLPAPER, contentId, contentData.name);
            } else if (contentData.type == ContentData.TypeEnum.VIDEO) {
               this.onPurchaseVirtualGoods(username, VirtualGoodType.VIDEO, contentId, contentData.name);
            } else if (contentData.type == ContentData.TypeEnum.APPLICATION) {
               this.onPurchaseVirtualGoods(username, VirtualGoodType.GAME, contentId, contentData.name);
            }
         }

         var33 = downloadURL;
      } catch (Exception var30) {
         throw new EJBException(var30.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var28) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
            conn = null;
         }

      }

      return var33;
   }

   private void getDownloadURLFromiLoop(String mobilePhone, String providerID, String orderURL) throws Exception {
      try {
         URL url = new URL(orderURL + "&msisdn=" + mobilePhone + "&message=MIG1+" + providerID);
         System.out.println("iLOOP CONTENT: Content purchase request. Notifying iLoop: " + url.toString());
         HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
         httpConn.setDoOutput(true);
         httpConn.setUseCaches(false);
         if (httpConn.getResponseCode() != 200) {
            throw new IOException("HTTP " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
         } else {
            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String requestId = reader.readLine();
            if (requestId != null) {
               if (requestId.indexOf("RequestId=") >= 0) {
                  requestId.substring(9, requestId.length() - 1);
               } else {
                  throw new Exception("Unable to communicate with content provider (2)");
               }
            } else {
               throw new Exception("Unable to communicate with content provider (3)");
            }
         }
      } catch (IOException var8) {
         throw new Exception("Unable to communicate with content provider (4)");
      }
   }

   private String getDownloadURLFromOplayo(Connection conn, String mobilePhone, String providerID, String orderURL, int contentPurchasedId) throws Exception {
      String downloadURL = null;

      URL url;
      try {
         url = new URL(orderURL + "&uid=" + mobilePhone + "&cid=" + providerID + "&rputag=" + contentPurchasedId);
         System.out.println("OPLAYO CONTENT: Content purchase request. Notifying Oplayo: " + url.toString());
         HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
         httpConn.setDoOutput(true);
         httpConn.setUseCaches(false);
         if (httpConn.getResponseCode() != 200) {
            throw new IOException("HTTP " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
         }

         Document reply = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(httpConn.getInputStream());
         NodeList urlNodes = reply.getElementsByTagName("url");
         if (urlNodes == null || urlNodes.getLength() == 0) {
            System.out.println("OPLAYO ERROR: Could not find URL. XML: " + reply.toString());
            throw new Exception("Unable to communicate with content provider (1)");
         }

         Element urlElement = (Element)urlNodes.item(0);
         NodeList textURLList = urlElement.getChildNodes();
         downloadURL = textURLList.item(0).getNodeValue().trim();
         System.out.println("OPLAYO CONTENT: Download URL: " + downloadURL);
         if (downloadURL == null) {
            throw new Exception("Unable to communicate with content provider (2)");
         }
      } catch (IOException var24) {
         System.out.println("OPLAYO ERROR: IOException: " + var24.getMessage());
         throw new Exception("Unable to communicate with content provider (3)");
      }

      PreparedStatement ps = null;

      try {
         ps = conn.prepareStatement("update contentpurchased set downloadurl=? where id=?");
         ps.setString(1, downloadURL);
         ps.setInt(2, contentPurchasedId);
         if (ps.executeUpdate() != 1) {
            throw new Exception("Unable to communicate with content provider (4)");
         }
      } catch (SQLException var22) {
         System.out.println("OPLAYO ERROR: SQLException: " + var22.getMessage());
         throw new Exception("Unable to communicate with content provider (5)");
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            url = null;
         }

      }

      return downloadURL;
   }

   private String getDownloadURLFromElasitas(Connection conn, String mobilePhone, String providerID, int contentPurchasedId) throws Exception {
      String downloadURL = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         ps = conn.prepareStatement("select content.providerid from content, contentpurchased where contentpurchased.contentid=content.id and contentpurchased.id=?");
         ps.setInt(1, contentPurchasedId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new Exception("Unable to obtain download URL");
         }

         downloadURL = rs.getString(1);
      } catch (SQLException var20) {
         System.out.println("ELASITAS ERROR: SQLException: " + var20.getMessage());
         throw new Exception("Unable to obtain download URL.");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

      }

      return downloadURL;
   }

   public void processILoopAPICall(String providerTransactionId, String mobilePhone, String body, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      String providerContentId;
      if (body != null && body.length() != 0 && mobilePhone != null && mobilePhone.length() != 0) {
         System.out.println("iLOOP API Call: mobilePhone=" + mobilePhone + " body=" + body + " providerTransactionId=" + providerTransactionId);
         if (body.startsWith("http://")) {
            providerContentId = this.getLatestContentPurchasedWithNoDownloadURL(mobilePhone);
            if (providerContentId != null) {
               this.updateMobileContentDownloadURL(providerTransactionId, mobilePhone, providerContentId, body);
            }

         } else {
            char msgType = body.charAt(0);
            String providerContentId = body.substring(2, body.indexOf(32, 2));
            switch(msgType) {
            case '1':
               String downloadURL;
               try {
                  downloadURL = body.substring(body.indexOf("http://"));
               } catch (Exception var10) {
                  String error = "iLOOP CONTENT ERROR: Message type 1, unable to obtain URL. body=" + body + " mobilePhone=" + mobilePhone;
                  System.out.println(error);
                  throw new EJBException(error);
               }

               this.updateMobileContentDownloadURL(providerTransactionId, mobilePhone, providerContentId, downloadURL);
               break;
            case '2':
               this.refundMobileContentItem(mobilePhone, providerContentId, ContentPurchasedData.RefundReasonEnum.PROVIDER_ERROR, accountEntrySourceData);
               break;
            case '3':
               this.refundMobileContentItem(mobilePhone, providerContentId, ContentPurchasedData.RefundReasonEnum.HANDSET_INCOMPATIBLE, accountEntrySourceData);
               break;
            case '4':
               this.updateMobileContentNumDownloads(mobilePhone, providerContentId, 1);
               break;
            case '5':
               this.updateMobileContentNumDownloads(mobilePhone, providerContentId, 2);
               break;
            case '6':
               this.updateMobileContentNumDownloads(mobilePhone, providerContentId, 3);
            case '7':
               break;
            default:
               if (body.startsWith("http")) {
                  providerContentId = body.substring(body.lastIndexOf(47) + 1, body.length() - 1);
                  System.out.println("iLOOP CONTENT: WAP push instruction received. mobilePhone=" + mobilePhone + " body=" + body + " providerContentId=" + providerContentId);
                  this.updateMobileContentDownloadURL(providerTransactionId, mobilePhone, providerContentId, body);
                  return;
               }

               if (body.startsWith("MSG")) {
                  System.out.println("iLOOP CONTENT: 2nd part of two part message received. mobilePhone=" + mobilePhone + " body=" + body);
                  return;
               }

               String error = "iLOOP CONTENT ERROR: Unrecognized message received. mobilePhone=" + mobilePhone + " body=" + body;
               System.out.println(error);
               throw new EJBException(error);
            }

         }
      } else {
         providerContentId = "iLOOP CONTENT ERROR: body or mobilePhone missing. body=" + body + " mobilePhone=" + mobilePhone;
         System.out.println(providerContentId);
         throw new EJBException(providerContentId);
      }
   }

   private String getLatestContentPurchasedWithNoDownloadURL(String mobilePhone) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select providercontentid from contentpurchased where mobilephone=? and downloadurl is null order by id desc limit 1");
         ps.setString(1, mobilePhone);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = rs.getString(1);
      } catch (SQLException var26) {
         System.out.println("iLOOP CONTENT ERROR: getLatestContentPurchasedWithNoDownloadURL(" + mobilePhone + ") failed. Exception: " + var26.getMessage());
         return null;
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return var5;
   }

   private void updateMobileContentDownloadURL(String providerTransactionId, String mobilePhone, String providerContentId, String downloadURL) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update contentpurchased set downloadurl=?, providertransactionid=? where mobilephone=? and providercontentid=? and downloadurl is null");
         ps.setString(1, downloadURL);
         ps.setString(2, providerTransactionId);
         ps.setString(3, mobilePhone);
         ps.setString(4, providerContentId);
         int rowsUpdated = ps.executeUpdate();
         ps.close();
         if (rowsUpdated == 1) {
            System.out.println("iLOOP CONTENT NOTIFICATION: Set download URL. MobilePhone=" + mobilePhone + " providerContentId=" + providerContentId + " URL=" + downloadURL);
         } else {
            System.out.println("iLOOP CONTENT NOTIFICATION: Did not set download URL (URL already set). MobilePhone=" + mobilePhone + " providerContentId=" + providerContentId + " URL=" + downloadURL);
         }
      } catch (SQLException var26) {
         System.out.println("iLOOP CONTENT ERROR: Unable to update download URL. Exception: " + var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

   }

   private void updateMobileContentNumDownloads(String mobilePhone, String providerContentId, int numDownloads) {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update contentpurchased set numdownloads=? where mobilephone=? and providercontentid=? and numdownloads<?");
         ps.setInt(1, numDownloads);
         ps.setString(2, mobilePhone);
         ps.setString(3, providerContentId);
         ps.setInt(4, numDownloads);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated == 1) {
            System.out.println("iLOOP CONTENT NOTIFICATION: Updated numdownloads to " + numDownloads + ". MobilePhone=" + mobilePhone + " providerContentId=" + providerContentId);
         } else {
            System.out.println("iLOOP CONTENT NOTIFICATION: Did not update numdownloads to " + numDownloads + ". (row not found or numdownloads already >= " + numDownloads + "). MobilePhone=" + mobilePhone + " providerContentId=" + providerContentId);
         }
      } catch (SQLException var25) {
         System.out.println("iLOOP CONTENT ERROR: Unable to update download URL. Exception: " + var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

   }

   public void refundMobileContentItem(String mobilePhone, String providerContentId, ContentPurchasedData.RefundReasonEnum refundReason, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      log.info("iLOOP CONTENT: Refund requested for MobilePhone=" + mobilePhone + " providerContentId=" + providerContentId);

      try {
         conn = this.dataSourceMaster.getConnection();
         ContentPurchasedData contentPurchasedData = new ContentPurchasedData();
         ps = conn.prepareStatement("select * from contentpurchased where mobilephone = ? and providercontentid = ? order by id desc limit 1");
         ps.setString(1, mobilePhone);
         ps.setString(2, providerContentId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Purchase of content " + providerContentId + " by mobile phone " + mobilePhone + " not found");
         }

         contentPurchasedData.id = rs.getInt("id");
         contentPurchasedData.username = rs.getString("username");
         contentPurchasedData.dateCreated = rs.getTimestamp("datecreated");
         contentPurchasedData.mobilephone = rs.getString("mobilephone");
         contentPurchasedData.contentId = rs.getInt("contentid");
         contentPurchasedData.providerContentId = rs.getString("providercontentid");
         contentPurchasedData.providerTransactionId = rs.getString("providertransactionid");
         contentPurchasedData.downloadURL = rs.getString("downloadurl");
         contentPurchasedData.numDownloads = rs.getInt("numdownloads");
         contentPurchasedData.refunded = rs.getBoolean("refunded");
         contentPurchasedData.refundReason = ContentPurchasedData.RefundReasonEnum.fromValue(rs.getInt("refundreason"));
         rs.close();
         ps.close();
         if (contentPurchasedData.refunded) {
            throw new EJBException("Content purchase has already been refunded");
         }

         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData originalEntry = accountBean.getAccountEntryFromReference(AccountEntryData.TypeEnum.CONTENT_ITEM_PURCHASE, contentPurchasedData.id.toString());
         if (originalEntry == null || !originalEntry.username.equalsIgnoreCase(contentPurchasedData.username)) {
            throw new EJBException("Purchase of content " + providerContentId + " by mobile phone " + mobilePhone + " not found");
         }

         ps = conn.prepareStatement("update contentpurchased set refunded=1, refundreason=? where id=?");
         ps.setInt(1, refundReason.value());
         ps.setInt(2, contentPurchasedData.id);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Unable to update ContentPurchased for refund of content " + providerContentId + " by mobile phone " + mobilePhone);
         }

         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = contentPurchasedData.username;
         accountEntry.type = AccountEntryData.TypeEnum.CONTENT_ITEM_REFUND;
         accountEntry.reference = contentPurchasedData.id.toString();
         accountEntry.description = "Refund of mobile content purchase";
         accountEntry.currency = originalEntry.currency;
         accountEntry.amount = -originalEntry.amount;
         accountEntry.fundedAmount = -originalEntry.fundedAmount;
         accountEntry.tax = 0.0D;
         accountEntry.costOfGoodsSold = -originalEntry.costOfGoodsSold;
         accountEntry.costOfTrial = -originalEntry.costOfTrial;
         accountBean.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
      } catch (Exception var27) {
         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

   }

   public void refundMobileContentItemFromMIS(String username, int contentPurchasedId, String misUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ContentPurchasedData contentPurchasedData = new ContentPurchasedData();
         ps = conn.prepareStatement("select * from contentpurchased where id = ? and username = ?");
         ps.setInt(1, contentPurchasedId);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("ContentPurchased record not found");
         }

         contentPurchasedData.id = rs.getInt("id");
         contentPurchasedData.username = rs.getString("username");
         contentPurchasedData.dateCreated = rs.getTimestamp("datecreated");
         contentPurchasedData.mobilephone = rs.getString("mobilephone");
         contentPurchasedData.contentId = rs.getInt("contentid");
         contentPurchasedData.providerContentId = rs.getString("providercontentid");
         contentPurchasedData.providerTransactionId = rs.getString("providertransactionid");
         contentPurchasedData.downloadURL = rs.getString("downloadurl");
         contentPurchasedData.numDownloads = rs.getInt("numdownloads");
         contentPurchasedData.refunded = rs.getBoolean("refunded");
         contentPurchasedData.refundReason = ContentPurchasedData.RefundReasonEnum.fromValue(rs.getInt("refundreason"));
         rs.close();
         ps.close();
         if (contentPurchasedData.refunded) {
            throw new EJBException("Content purchase has already been refunded");
         }

         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData originalEntry = accountBean.getAccountEntryFromReference(AccountEntryData.TypeEnum.CONTENT_ITEM_PURCHASE, contentPurchasedData.id.toString());
         if (originalEntry == null || !originalEntry.username.equalsIgnoreCase(contentPurchasedData.username)) {
            throw new EJBException("Purchase of content was not found");
         }

         ps = conn.prepareStatement("update contentpurchased set refunded=1, refundreason=? where id=?");
         ps.setInt(1, ContentPurchasedData.RefundReasonEnum.MANUAL.value());
         ps.setInt(2, contentPurchasedData.id);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Unable to update ContentPurchased for refund of content");
         }

         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = contentPurchasedData.username;
         accountEntry.type = AccountEntryData.TypeEnum.CONTENT_ITEM_REFUND;
         accountEntry.reference = contentPurchasedData.id.toString();
         accountEntry.description = "Refund of mobile content purchase (by " + misUsername + ")";
         accountEntry.currency = originalEntry.currency;
         accountEntry.amount = -originalEntry.amount;
         accountEntry.fundedAmount = -originalEntry.fundedAmount;
         accountEntry.tax = 0.0D;
         accountEntry.costOfGoodsSold = -originalEntry.costOfGoodsSold;
         accountEntry.costOfTrial = -originalEntry.costOfTrial;
         accountBean.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
      } catch (Exception var27) {
         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

   }

   public void sendMobileContentDownloadURLInSMS(String username, int contentId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.MOBILE_CONTENT_DOWNLOAD, username)) {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            conn = this.dataSourceMaster.getConnection();
            MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            ps = conn.prepareStatement("select mobilephone, mobileverified from user where username = ? and status = 1");
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("User not found: " + username);
            }

            if (!rs.getBoolean("mobileverified")) {
               throw new EJBException("Account not authenticated");
            }

            String mobilePhone = rs.getString("mobilephone");
            if (!messageBean.isMobileNumber(mobilePhone, true)) {
               throw new EJBException("Invalid mobile number: " + mobilePhone);
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("select content.name, contentpurchased.downloadurl from content, contentpurchased where content.id=contentpurchased.contentid and contentpurchased.username=? and content.id=? order by contentpurchased.id desc limit 1");
            ps.setString(1, username);
            ps.setInt(2, contentId);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Content not found");
            }

            String contentName = rs.getString("name");
            String contentDownloadURL = rs.getString("downloadurl");
            rs.close();
            ps.close();
            int smsCount = messageBean.getSystemSMSCount(SystemSMSData.SubTypeEnum.MOBILE_CONTENT_DOWNLOAD, username);
            if (smsCount >= SystemProperty.getInt("MaxMobileContentDownloadSMSPerDay")) {
               throw new EJBException("You have already requested the download link in an SMS today. If you do not receive the SMS containing the link, please email contact@mig.me");
            }

            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.MOBILE_CONTENT_DOWNLOAD;
            systemSMSData.username = username;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = mobilePhone;
            systemSMSData.messageText = SystemProperty.get("MobileContentDownloadSMS").replaceAll("%1", contentName).replaceAll("%2", contentDownloadURL);
            messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
         } catch (CreateException var29) {
            throw new EJBException(var29.getMessage());
         } catch (SQLException var30) {
            throw new EJBException(var30.getMessage());
         } catch (NoSuchFieldException var31) {
            throw new EJBException(var31.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var28) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var27) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var26) {
               conn = null;
            }

         }

      }
   }

   private void onPurchaseVirtualGoods(String username, VirtualGoodType type, int itemId, String itemName) {
   }

   public void onPurchaseVirtualGift(String senderUsername, Map<Integer, UserData> recipientUserDataList, VirtualGiftData gift, boolean privateGift, String message, String chatroomName, boolean fromSenderInventory) {
      if (log.isDebugEnabled()) {
         log.debug("onPurchaseVirtualGift([" + senderUsername + "],recipient[" + recipientUserDataList + "])");
      }

      UserData buyerUserData = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         buyerUserData = userBean.loadUser(senderUsername, false, false);
      } catch (CreateException var29) {
         log.warn("Unable to load UserBean: " + var29.getMessage());
      }

      UserNotificationServicePrx unsProxy = null;
      ChatRoomPrx chatRoomPrx = null;
      EventSystemPrx eventSystem = null;
      if (recipientUserDataList != null && buyerUserData != null) {
         boolean isGiftShower = recipientUserDataList.size() > 1;
         List<Event> eventList = new ArrayList(recipientUserDataList.size());
         Iterator i$ = recipientUserDataList.entrySet().iterator();

         while(true) {
            Integer virtualGiftReceivedID;
            UserData recipientUserData;
            do {
               if (!i$.hasNext()) {
                  EventQueue.enqueueMultipleEvents(eventList);
                  if (isGiftShower) {
                     Integer[] giftShowerRecipients = (Integer[])((Integer[])recipientUserDataList.keySet().toArray(new Integer[recipientUserDataList.size()]));
                     Integer totalRecipients = recipientUserDataList.size();
                     int giftShowerEvents = Math.min(totalRecipients, SystemProperty.getInt((String)"GiftShowerEventsToSend", 5));

                     try {
                        if (eventSystem == null) {
                           eventSystem = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VIRTUALGIFT_ONEWAY_EVENT_TRIGGER) ? EJBIcePrxFinder.getOnewayEventSystemProxy() : EJBIcePrxFinder.getEventSystemProxy();
                        }

                        int index = false;
                        Random rand = new Random();

                        for(int i = 0; i < giftShowerEvents; ++i) {
                           int index = rand.nextInt(totalRecipients);
                           Integer virtualGiftReceivedId = giftShowerRecipients[index];
                           UserData recipientUserData = (UserData)recipientUserDataList.get(virtualGiftReceivedId);
                           log.debug("[" + senderUsername + "] [" + index + "] [" + recipientUserData.username + "] [" + gift.getName() + "][" + virtualGiftReceivedId + "] [" + totalRecipients + "]");
                           if (!privateGift) {
                              eventSystem.giftShowerEvent(senderUsername, recipientUserData.username, gift.getName(), virtualGiftReceivedId, totalRecipients);
                           }
                        }
                     } catch (Exception var30) {
                        log.error("Failed to send GiftShowerEvent for [" + senderUsername + "] gift[" + gift.getName() + "] totalRecipients[" + totalRecipients + "]", var30);
                     }

                     return;
                  }

                  return;
               }

               Entry<Integer, UserData> recipientUserDataListEntry = (Entry)i$.next();
               virtualGiftReceivedID = (Integer)recipientUserDataListEntry.getKey();
               recipientUserData = (UserData)recipientUserDataListEntry.getValue();
            } while(recipientUserData == null);

            if (!isGiftShower && SystemProperty.getBool("GiftEventEnabled", false)) {
               try {
                  if (eventSystem == null && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
                     eventSystem = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VIRTUALGIFT_ONEWAY_EVENT_TRIGGER) ? EJBIcePrxFinder.getOnewayEventSystemProxy() : EJBIcePrxFinder.getEventSystemProxy();
                  }

                  if (!privateGift) {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED)) {
                        eventSystem.virtualGift(senderUsername, recipientUserData.username, gift.getName(), virtualGiftReceivedID);
                     }

                     eventList.add(new VirtualGiftSentEvent(senderUsername, recipientUserData.username, gift.getName(), virtualGiftReceivedID));
                  }
               } catch (Exception var28) {
                  log.error("Failed to log virtual gift event for user [" + senderUsername + "]", var28);
               }
            }

            HashMap parameters = new HashMap();

            try {
               UserPrx userPrx = EJBIcePrxFinder.findUserPrx(recipientUserData.username);
               if (userPrx != null) {
                  MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                  String alertMessage = misBean.getInfoText(41);
                  if (alertMessage != null) {
                     if (privateGift) {
                        alertMessage = alertMessage.replaceAll("%private%", "private ");
                     } else {
                        alertMessage = alertMessage.replaceAll("%private%", "");
                     }

                     alertMessage = alertMessage.replaceAll("%senderusername%", senderUsername);
                     parameters.put("alertMessage", alertMessage);
                     parameters.put("alertURL", SystemProperty.get("VirtualGiftReceivedURL") + virtualGiftReceivedID);
                  }
               } else if (gift.getPrice() > 0.0D && recipientUserData.mobilePhone != null && recipientUserData.mobileVerified) {
                  boolean sendSMS = SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.VIRTUAL_GIFT_NOTIFICATION, recipientUserData.username) && decideSendSMS(recipientUserData.username);
                  if (sendSMS) {
                     try {
                        SystemSMSData systemSMSData = new SystemSMSData();
                        systemSMSData.username = recipientUserData.username;
                        systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
                        systemSMSData.subType = SystemSMSData.SubTypeEnum.VIRTUAL_GIFT_NOTIFICATION;
                        systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
                        systemSMSData.destination = recipientUserData.mobilePhone;
                        if (privateGift) {
                           systemSMSData.messageText = SystemProperty.get("VirtualGiftNotificationSMS").replaceAll("%private%", "private ");
                        } else {
                           systemSMSData.messageText = SystemProperty.get("VirtualGiftNotificationSMS").replaceAll("%private%", "");
                        }

                        systemSMSData.messageText = systemSMSData.messageText.replaceAll("%senderusername%", senderUsername);
                        MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                        messageBean.sendSystemSMS(systemSMSData, new AccountEntrySourceData(ContentBean.class));
                        log.info("Sending SMS to recipient of virtual gift [" + senderUsername + ", " + recipientUserData.username + ", " + gift.getName() + "]");
                     } catch (Exception var27) {
                        log.error("Unable to SMS to recipient of virtual gift [" + senderUsername + ", " + recipientUserData.username + ", " + gift.getName() + "]", var27);
                     }
                  } else if (log.isDebugEnabled()) {
                     log.debug("NOT Sending SMS to recipient of virtual gift [" + senderUsername + ", " + recipientUserData.username + ", " + gift.getName() + "]");
                  }
               }

               try {
                  if (unsProxy == null) {
                     unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  }

                  if (unsProxy != null) {
                     UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                     int senderUserId = userEJB.getUserID(senderUsername, (Connection)null);
                     parameters.put("virtualGiftId", gift.getId().toString());
                     parameters.put("senderUsername", senderUsername);
                     parameters.put("senderUserId", Integer.toString(senderUserId));
                     parameters.put("virtualGiftReceivedId", Integer.toString(virtualGiftReceivedID));
                     parameters.put("private", privateGift ? "1" : "0");
                     parameters.put("giftName", gift.getName());
                     parameters.put("location12x12GIF", gift.getLocation12x12GIF());
                     parameters.put("location12x12PNG", gift.getLocation12x12PNG());
                     parameters.put("location14x14GIF", gift.getLocation14x14GIF());
                     parameters.put("location14x14PNG", gift.getLocation14x14PNG());
                     parameters.put("location16x16GIF", gift.getLocation16x16GIF());
                     parameters.put("location16x16PNG", gift.getLocation16x16PNG());
                     parameters.put("location64x64PNG", gift.getLocation64x64PNG());
                     String key = gift.getId() + "/" + System.currentTimeMillis();
                     unsProxy.notifyFusionUser(new Message(key, recipientUserData.userID, recipientUserData.username, Enums.NotificationTypeEnum.VIRTUALGIFT_ALERT.getType(), System.currentTimeMillis(), parameters));
                  }
               } catch (Exception var26) {
                  log.error("Failed to push virtual gift notification for user [" + recipientUserData.username + "]", var26);
               }

               if (!privateGift && chatroomName != null) {
                  try {
                     if (chatRoomPrx == null) {
                        chatRoomPrx = EJBIcePrxFinder.findChatRoomPrx(chatroomName);
                     }

                     if (chatRoomPrx != null) {
                        String msg = "<< " + senderUsername + " gives ";
                        if (StringUtil.startsWithaVowel(gift.getName())) {
                           msg = msg + "an ";
                        } else {
                           msg = msg + "a ";
                        }

                        msg = msg + gift.getName() + " " + gift.getHotKey() + " to " + recipientUserData.username + "! >>";
                        String[] emoticonKeys = new String[]{gift.getHotKey()};
                        chatRoomPrx.putSystemMessage(msg, emoticonKeys);
                     }
                  } catch (Exception var25) {
                     log.error("Unable to send virtual gift notification to chatroom [" + senderUsername + ", " + recipientUserData.username + ", " + gift.getName() + ", " + chatroomName + "]", var25);
                  }
               }

               try {
                  VGSentTrigger vgSentTrigger = new VGSentTrigger(buyerUserData, recipientUserData);
                  vgSentTrigger.amountDelta = gift.getRoundedPrice();
                  vgSentTrigger.quantityDelta = 1;
                  vgSentTrigger.currency = gift.getCurrency();
                  vgSentTrigger.virtualGiftID = gift.getId();
                  vgSentTrigger.fromSenderInventory = fromSenderInventory;
                  if (log.isDebugEnabled()) {
                     log.debug("Sending VGSentTrigger[" + vgSentTrigger + "]");
                  }

                  RewardCentre.getInstance().sendTrigger(vgSentTrigger);
                  VGReceivedTrigger vgReceivedTrigger = new VGReceivedTrigger(recipientUserData, virtualGiftReceivedID);
                  vgReceivedTrigger.amountDelta = gift.getRoundedPrice();
                  vgReceivedTrigger.quantityDelta = 1;
                  vgReceivedTrigger.currency = gift.getCurrency();
                  vgReceivedTrigger.virtualGiftID = gift.getId();
                  vgReceivedTrigger.senderUserData = buyerUserData;
                  vgReceivedTrigger.virtualGiftName = gift.getName();
                  vgReceivedTrigger.fromSenderInventory = fromSenderInventory;
                  vgReceivedTrigger.message = StringUtil.truncateWithEllipsis(message, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_MESSAGE_LENGTH));
                  File file = new File(gift.getImageLocation(VirtualGiftData.ImageFormatType.PNG, (short)SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.VIRTUAL_GIFT_RESOLUTIONS_DEFAULT)));
                  vgReceivedTrigger.image = file != null ? file.getName() : null;
                  if (log.isDebugEnabled()) {
                     log.debug("Sending VGReceivedTrigger[" + vgReceivedTrigger + "]");
                  }

                  RewardCentre.getInstance().sendTrigger(vgReceivedTrigger);
               } catch (Exception var24) {
                  log.warn("Unable to notify reward system", var24);
               }

               try {
                  Leaderboard.sendVirtualGift(senderUsername, recipientUserData.username);
               } catch (Exception var23) {
                  log.error("Error in updating GiftSent and GiftReceived leaderboard.");
               }
            } catch (Exception var31) {
               log.error("Unable to notify recipient of a virtual gift [" + senderUsername + ", " + recipientUserData.username + ", " + gift.getName() + "]", var31);
            }
         }
      }
   }

   public void onPurchaseVirtualGift(String senderUsername, Map<Integer, UserData> recipientUserDataList, VirtualGiftData gift, boolean privateGift, String message, String chatroomName) {
      this.onPurchaseVirtualGift(senderUsername, recipientUserDataList, gift, privateGift, message, chatroomName, false);
   }

   public List<VirtualGiftData> getVirtualGifts(String username, int groupID, int limit) throws EJBException {
      List<VirtualGiftData> gifts = new ArrayList();
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.SortOrder, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status FROM virtualgift, user, currency currency_user, currency currency_gift WHERE user.Username=? and user.Currency=currency_user.Code and virtualgift.currency=currency_gift.Code and virtualgift.status=1 ";
         if (groupID > 0) {
            sql = sql + "and groupid=? ";
         } else {
            sql = sql + "and (groupid is null or groupid=0) ";
         }

         sql = sql + "order by virtualgift.SortOrder";
         if (limit > 0) {
            sql = sql + " limit " + limit;
         }

         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         if (groupID > 0) {
            ps.setInt(2, groupID);
         }

         rs = ps.executeQuery();

         while(rs.next()) {
            gifts.add(new VirtualGiftData(rs));
         }
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var20) {
            connSlave = null;
         }

      }

      return gifts;
   }

   public ArrayList<String> getVirtualGiftCategoryNames() throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList<String> categories = new ArrayList();
      byte VG_ROOT_CATEGORY = 1;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT Name FROM storecategory WHERE ParentStoreCategoryID=?";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, VG_ROOT_CATEGORY);
         rs = ps.executeQuery();

         while(rs.next()) {
            categories.add(rs.getString("Name"));
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }

      return categories;
   }

   public List<VirtualGiftData> getVirtualGiftForCategory(String username, String categoryName, int groupId, int limit) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      LinkedList gifts = new LinkedList();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.SortOrder, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status FROM virtualgift, user, currency currency_user, currency currency_gift, storeitem si, storecategory, storeitemcategory WHERE user.Username=? and user.Currency=currency_user.Code and virtualgift.currency=currency_gift.Code and virtualgift.id = si.referenceid and virtualgift.status=1 and si.status = 1 and si.forsale = 1 and si.type= 1 and si.id=storeitemcategory.StoreItemID and storeitemcategory.StoreCategoryID=storecategory.ID and storecategory.Name=?";
         if (groupId > 0) {
            sql = sql + " and virtualgift.groupid=?";
         } else {
            sql = sql + " and (virtualgift.groupid is null or virtualgift.groupid=0)";
         }

         sql = sql + " ORDER BY si.numsold DESC, si.datelisted DESC, virtualgift.sortorder ASC LIMIT ?";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         ps.setString(2, categoryName);
         ps.setInt(3, limit);
         rs = ps.executeQuery();

         while(rs.next()) {
            gifts.add(new VirtualGiftData(rs));
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return gifts;
   }

   private String getVirtualGiftsBaseSQL(int groupId, String type) {
      String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.SortOrder, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status" + (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.RETRIEVE_VIRTUAL_GIFT_STORE_ITEM_ID) ? ", si.id storeitemid " : "") + " FROM virtualgift, user, currency currency_user, currency currency_gift, storeitem si" + " WHERE user.Username= ?" + " and user.Currency=currency_user.Code" + " and virtualgift.currency=currency_gift.Code" + " and virtualgift.id = si.referenceid" + " and virtualgift.status=1" + " and si.status = 1" + " and si.forsale = 1" + " and si.type= 1" + " and si.migLevelMin <= ?";
      if (groupId > 0) {
         sql = sql + " and virtualgift.groupid=?";
      } else {
         sql = sql + " and (virtualgift.groupid is null or virtualgift.groupid=0)";
      }

      return sql;
   }

   private String getFeaturedVirtualGiftsSQL(int groupId, int limit) {
      String sql = this.getVirtualGiftsBaseSQL(groupId, "featured") + " and si.featured = 1" + " ORDER BY si.datelisted DESC, virtualgift.sortorder ASC";
      if (limit > 0) {
         sql = sql + " LIMIT " + limit;
      }

      return sql;
   }

   private String getPopularVirtualGiftsSQL(int groupId, int limit) {
      String sql = this.getVirtualGiftsBaseSQL(groupId, "popular") + " ORDER BY si.numsold DESC, si.featured DESC, si.datelisted DESC, virtualgift.sortorder ASC";
      if (limit > 0) {
         sql = sql + " LIMIT " + limit;
      }

      return sql;
   }

   private String getNewVirtualGiftsSQL(int groupId, int limit) {
      String sql = this.getVirtualGiftsBaseSQL(groupId, "new") + " ORDER BY si.datelisted DESC, virtualgift.sortorder ASC";
      if (limit > 0) {
         sql = sql + " LIMIT " + limit;
      }

      return sql;
   }

   private String searchVirtualGiftsSQL(int groupId, int limit) {
      String sql = this.getVirtualGiftsBaseSQL(groupId, "search") + " and virtualgift.Name like ?" + " ORDER BY si.numsold DESC, si.featured DESC, si.datelisted DESC, virtualgift.sortorder ASC";
      if (limit > 0) {
         sql = sql + " LIMIT " + limit;
      }

      return sql;
   }

   private List<VirtualGiftData> getCustomVirtualGifts(String username, String[] vgTypes, int groupId, int limit) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList<String> queries = new ArrayList();
      int paramPos;
      if (vgTypes.length == 1) {
         String type = vgTypes[0];
         if (type.equals("featured")) {
            queries.add(this.getFeaturedVirtualGiftsSQL(groupId, limit));
         } else if (type.equals("popular")) {
            queries.add(this.getPopularVirtualGiftsSQL(groupId, limit));
         } else if (type.equals("new")) {
            queries.add(this.getNewVirtualGiftsSQL(groupId, limit));
         } else if (type.equals("recent")) {
         }
      } else {
         int perTypeLimit = (int)Math.ceil((double)(limit / vgTypes.length));
         String[] arr$ = vgTypes;
         int len$ = vgTypes.length;

         for(paramPos = 0; paramPos < len$; ++paramPos) {
            String type = arr$[paramPos];
            type = type.toLowerCase();
            if (type.equals("featured")) {
               queries.add(this.getFeaturedVirtualGiftsSQL(groupId, type.equals(vgTypes[vgTypes.length - 1].toLowerCase()) ? limit : perTypeLimit));
            } else if (type.equals("popular")) {
               queries.add(this.getPopularVirtualGiftsSQL(groupId, type.equals(vgTypes[vgTypes.length - 1].toLowerCase()) ? limit : perTypeLimit));
            } else if (type.equals("new")) {
               queries.add(this.getNewVirtualGiftsSQL(groupId, type.equals(vgTypes[vgTypes.length - 1].toLowerCase()) ? limit : perTypeLimit));
            } else if (type.equals("recent")) {
            }
         }
      }

      if (queries.isEmpty()) {
         throw new EJBException("No valid virtualgift types passed in");
      } else {
         List<VirtualGiftData> gifts = new LinkedList();
         int migLevelMin = 0;

         try {
            migLevelMin = MemCacheOrEJB.getUserReputationLevel(username);
         } catch (Exception var30) {
            log.warn("Failed to restrict gift search by migLevel: " + var30.getMessage());
         }

         try {
            connSlave = this.dataSourceSlave.getConnection();
            String sql = "(" + StringUtil.join((Collection)queries, ") UNION (") + ") LIMIT " + limit;
            ps = connSlave.prepareStatement(sql);
            paramPos = 1;
            Iterator i$ = queries.iterator();

            while(i$.hasNext()) {
               String query = (String)i$.next();
               ps.setString(paramPos++, username);
               ps.setInt(paramPos++, migLevelMin);
               if (groupId > 0) {
                  ps.setInt(paramPos++, groupId);
               }
            }

            rs = ps.executeQuery();

            while(rs.next()) {
               gifts.add(new VirtualGiftData(rs));
            }
         } catch (SQLException var31) {
            throw new EJBException(var31.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var29) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var28) {
               ps = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var27) {
               connSlave = null;
            }

         }

         return gifts;
      }
   }

   public List<VirtualGiftData> getFeaturedPopularNewVirtualGifts(String username, int groupId, int limit) throws EJBException {
      List gifts = null;

      try {
         gifts = (List)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.VIRTUAL_GIFT_FEATURED_POPULAR_NEW, username);
      } catch (Exception var6) {
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.VIRTUAL_GIFT_FEATURED_POPULAR_NEW, username);
      }

      if (gifts != null) {
         return gifts;
      } else {
         String[] types = new String[]{"featured", "popular", "new"};
         gifts = this.getCustomVirtualGifts(username, types, groupId, limit);
         MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.VIRTUAL_GIFT_FEATURED_POPULAR_NEW, username, gifts);
         return gifts;
      }
   }

   public List<VirtualGiftData> getFeaturedVirtualGifts(String username, int groupId, int limit) throws EJBException {
      String[] types = new String[]{"featured"};
      return this.getCustomVirtualGifts(username, types, groupId, limit);
   }

   public List<VirtualGiftData> getPopularVirtualGifts(String username, int groupId, int limit) throws EJBException {
      String[] types = new String[]{"popular"};
      return this.getCustomVirtualGifts(username, types, groupId, limit);
   }

   public List<VirtualGiftData> getNewVirtualGifts(String username, int groupId, int limit) throws EJBException {
      String[] types = new String[]{"new"};
      return this.getCustomVirtualGifts(username, types, groupId, limit);
   }

   public List<VirtualGiftData> getRecentVirtualGifts(String username, int groupId, int limit) throws EJBException {
      String[] types = new String[]{"recent"};
      return this.getCustomVirtualGifts(username, types, groupId, limit);
   }

   public List<VirtualGiftData> searchVirtualGifts(String username, int groupId, String search, int limit, boolean useWildCards) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      List<VirtualGiftData> gifts = new LinkedList();
      int migLevelMin = 0;

      try {
         migLevelMin = MemCacheOrEJB.getUserReputationLevel(username);
      } catch (Exception var28) {
         log.warn("Failed to restrict gift search by migLevel: " + var28.getMessage());
      }

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = this.searchVirtualGiftsSQL(groupId, limit);
         ps = connSlave.prepareStatement(sql);
         int paramPos = 1;
         int var31 = paramPos + 1;
         ps.setString(paramPos, username);
         ps.setInt(var31++, migLevelMin);
         if (groupId > 0) {
            ps.setInt(var31++, groupId);
         }

         if (useWildCards) {
            search = "%" + search.replaceAll("[%]", "").replaceAll("\\*", "%") + "%";
         }

         ps.setString(var31++, search);
         rs = ps.executeQuery();

         while(rs.next()) {
            gifts.add(new VirtualGiftData(rs));
         }
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var25) {
            connSlave = null;
         }

      }

      return gifts;
   }

   public List<VirtualGiftData> getRecentGiftsReceivedBy(String receiverUsername, String viewerUsername, int groupId, int limit) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      LinkedList gifts = new LinkedList();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.SortOrder, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status, MAX(vgr.DateCreated) as vgrDateCreated FROM virtualgift, user, currency currency_user, currency currency_gift, storeitem si, virtualgiftreceived vgr WHERE user.Username=? and user.Currency=currency_user.Code and virtualgift.currency=currency_gift.Code and virtualgift.id = si.referenceid and virtualgift.status=1 and si.status = 1 and si.forsale = 1 and si.type= 1 and vgr.VirtualGiftID=virtualgift.id and vgr.Username=? and vgr.removed=0";
         boolean needPrivacy = !receiverUsername.equalsIgnoreCase(viewerUsername);
         if (needPrivacy) {
            sql = sql + " and (vgr.private=0 or (vgr.private=1 and vgr.sender=?))";
         }

         if (groupId > 0) {
            sql = sql + " and virtualgift.groupid=?";
         } else {
            sql = sql + " and (virtualgift.groupid is null or virtualgift.groupid=0)";
         }

         sql = sql + " GROUP by virtualgift.ID ORDER BY vgrDateCreated DESC LIMIT ?";
         ps = connSlave.prepareStatement(sql);
         int paramPos = 1;
         int var28 = paramPos + 1;
         ps.setString(paramPos, viewerUsername);
         ps.setString(var28++, receiverUsername);
         if (needPrivacy) {
            ps.setString(var28++, viewerUsername);
         }

         if (groupId > 0) {
            ps.setInt(var28++, groupId);
         }

         ps.setInt(var28++, limit);
         rs = ps.executeQuery();

         while(rs.next()) {
            gifts.add(new VirtualGiftData(rs));
         }
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var23) {
            connSlave = null;
         }

      }

      return gifts;
   }

   public List<VirtualGiftData> getRecentGiftsSentBy(String senderUsername, String viewerUsername, int groupId, int limit) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      LinkedList gifts = new LinkedList();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.SortOrder, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status FROM virtualgift, user, currency currency_user, currency currency_gift, storeitem si, virtualgiftreceived vgr WHERE user.Username=? and user.Currency=currency_user.Code and virtualgift.currency=currency_gift.Code and virtualgift.id = si.referenceid and virtualgift.status=1 and si.status = 1 and si.forsale = 1 and si.type= 1 and vgr.VirtualGiftID=virtualgift.id and vgr.Sender=? and vgr.removed=0";
         boolean needPrivacy = !senderUsername.equalsIgnoreCase(viewerUsername);
         if (needPrivacy) {
            sql = sql + " and (vgr.private=0 or (vgr.private=1 and vgr.Username=?))";
         }

         if (groupId > 0) {
            sql = sql + " and virtualgift.groupid=?";
         } else {
            sql = sql + " and (virtualgift.groupid is null or virtualgift.groupid=0)";
         }

         sql = sql + " GROUP by virtualgift.ID LIMIT ?";
         ps = connSlave.prepareStatement(sql);
         int paramPos = 1;
         int var28 = paramPos + 1;
         ps.setString(paramPos, viewerUsername);
         ps.setString(var28++, senderUsername);
         if (needPrivacy) {
            ps.setString(var28++, viewerUsername);
         }

         if (groupId > 0) {
            ps.setInt(var28++, groupId);
         }

         ps.setInt(var28++, limit);
         rs = ps.executeQuery();

         while(rs.next()) {
            gifts.add(new VirtualGiftData(rs));
         }
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var23) {
            connSlave = null;
         }

      }

      return gifts;
   }

   public VirtualGiftData getVirtualGiftDetails(VirtualGiftData gift) {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      VirtualGiftData var6;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT sirsummary.Average, sirsummary.Total, sirsummary.NumRatings, cat.ID, cat.Name, cat.ParentStoreCategoryID, cat.SortOrder FROM storeitem si, storeitemratingsummary sirsummary, storecategory cat, storeitemcategory sicat WHERE si.ID=sirsummary.StoreItemID and si.ID=sicat.StoreItemID and sicat.StoreCategoryID=cat.ID and si.type=1 and si.ReferenceID=? LIMIT 1";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, gift.getId());
         rs = ps.executeQuery();
         if (rs.next()) {
            gift.setStoreCategory(new StoreCategoryData(rs));
            gift.setStoreRatingSummary(new StoreRatingSummaryData(rs));
         }

         var6 = gift;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }

      return var6;
   }

   public VirtualGiftData getVirtualGiftByHotKey(String hotKey) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      VirtualGiftData var5;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("SELECT * FROM virtualgift WHERE HotKey=? LIMIT 0,1");
         ps.setString(1, hotKey);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = new VirtualGiftData(rs);
            return var5;
         }

         var5 = null;
      } catch (SQLException var23) {
         log.error("Unable to fetch virtual gift by hotkey", var23);
         throw new EJBException("Unable to fetch virtual gift by hotkey");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var20) {
            connSlave = null;
         }

      }

      return var5;
   }

   public VirtualGiftData getVirtualGift(Integer virtualGiftID, String virtualGiftName, String username) throws EJBException, FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      VirtualGiftData gift = null;

      VirtualGiftData var33;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.SortOrder, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status, virtualgift.GiftAllMessage, s.migLevelMin migLevelMin " + (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.RETRIEVE_VIRTUAL_GIFT_STORE_ITEM_ID) ? ", s.id storeitemid " : "") + "FROM virtualgift, user, currency currency_user, currency currency_gift, storeitem s " + "WHERE user.Username=? and user.Currency=currency_user.Code and " + "virtualgift.currency=currency_gift.Code and " + "s.type = 1 and " + "virtualgift.id = s.referenceid ";
         if (virtualGiftID != null) {
            sql = sql + "and virtualgift.ID=?";
         } else {
            sql = sql + "and virtualgift.Name=?";
         }

         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         if (virtualGiftID != null) {
            ps.setInt(2, virtualGiftID);
         } else {
            ps.setString(2, virtualGiftName);
         }

         rs = ps.executeQuery();
         UserLocal userEJB;
         if (!rs.next()) {
            userEJB = null;
            return userEJB;
         }

         gift = new VirtualGiftData(rs);
         userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         if (gift.getMigLevelMin() != -1) {
            int reputationLevel = MemCacheOrEJB.getUserReputationLevel(username);
            if (reputationLevel < gift.getMigLevelMin()) {
               throw new FusionEJBException("Sorry, " + gift.getName() + " is available only for users with migLevel " + gift.getMigLevelMin() + " and above");
            }
         }

         if (gift.getGroupID() > 0) {
            rs.close();
            rs = null;
            ps.close();
            ps = null;
            connSlave.close();
            connSlave = null;
            GroupMemberData groupMemberData = userEJB.getGroupMember(username, gift.getGroupID());
            if (groupMemberData == null || GroupMemberData.StatusEnum.ACTIVE.value() != groupMemberData.status.value() || !groupMemberData.vip && gift.isGroupVIPOnly()) {
               GroupData groupData = userEJB.getGroup(gift.getGroupID());
               String err = "You must be a ";
               if (gift.isGroupVIPOnly()) {
                  err = err + "VIP ";
               }

               err = err + "member of the " + groupData.name + " group to access the " + gift.getName() + " virtual gift";
               throw new FusionEJBException(err);
            }
         }

         var33 = gift;
      } catch (Exception var30) {
         throw new EJBException(var30.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var28) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var27) {
            connSlave = null;
         }

      }

      return var33;
   }

   private void checkCanPurchaseGroupExclusiveItem(String username, int groupID, boolean groupVIPOnly, String itemName) throws EJBException {
      if (groupID > 0) {
         GroupData groupData = null;
         GroupMemberData groupMemberData = null;

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            groupData = userBean.getGroup(groupID);
            groupMemberData = userBean.getGroupMember(username, groupID);
         } catch (Exception var8) {
            log.error("Exception in checkCanPurchaseGroupExclusiveItem()", var8);
            throw new EJBException("Unable to verify group membership");
         }

         if (groupMemberData == null || GroupMemberData.StatusEnum.ACTIVE.value() != groupMemberData.status.value() || !groupMemberData.vip && groupVIPOnly) {
            String err = "You must be a ";
            if (groupVIPOnly) {
               err = err + "VIP ";
            }

            err = err + "member of the " + groupData.name + " group to purchase the " + itemName;
            throw new EJBException(err);
         }
      }
   }

   public static boolean decideSendSMS(String recipientUsername) throws SQLException {
      int waitInterval = true;
      long waitIntervalLong = 900000L;
      long curTimeMillis = System.currentTimeMillis();
      long duration = 900000L - curTimeMillis % 900000L;
      if (duration <= 500L) {
         duration = 900000L;
      }

      return MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.SENDSMS.toString(), recipientUsername, 1L, duration);
   }

   public double isGiftLowPrice(VirtualGiftData gift, String buyerUsername) throws FusionEJBException {
      UserLocal userBean = null;

      try {
         userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
      } catch (CreateException var8) {
         throw new FusionEJBException(String.format("Unable to check whether gift '%s' is low price.", gift.getName()));
      }

      double lowPriceThresholdInUSD = SystemProperty.getDouble("GiftLowPriceThreholdInUSD", 0.03D);
      double lowPriceThreshold = userBean.getPriceInUserCurrency(lowPriceThresholdInUSD, "USD", buyerUsername);
      return gift.getPrice() <= lowPriceThreshold ? lowPriceThreshold : -1.0D;
   }

   public void buyVirtualGift(String buyerUsername, String recipientUsername, int virtualGiftID, int purchaseLocation, boolean privateGift, String message, String chatroomName, AccountEntrySourceData accountEntrySourceData) throws FusionEJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      message = StringUtil.stripHTML(message);
      VirtualGiftData gift = this.getVirtualGift(virtualGiftID, (String)null, buyerUsername);
      if (gift == null) {
         throw new FusionEJBException("Unknown gift");
      } else if (gift.getStatus() == VirtualGiftData.StatusEnum.INACTIVE) {
         throw new FusionEJBException("The " + gift.getName() + " gift is no longer available.");
      } else if (gift.getNumAvailable() != null && gift.getNumAvailable() > 1 && gift.getNumSold() >= gift.getNumAvailable()) {
         throw new FusionEJBException("The " + gift.getName() + " gift is sold out!");
      } else {
         double lowPriceThreshold = this.isGiftLowPrice(gift, buyerUsername);
         if (lowPriceThreshold >= 0.0D) {
            throw new FusionEJBException(String.format("Gifts priced %s %s and less are only available via /gift all. Please try another gift instead.", TWO_DECIMAL_POINT_FORMAT.format(lowPriceThreshold), gift.getCurrency()));
         } else {
            UserData recipientUserData = null;
            boolean var16 = true;

            int virtualGiftReceivedId;
            try {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.BUY_VIRTUALGIFT, userBean.getUserAuthenticatedAccessControlParameter(buyerUsername)) && SystemProperty.getBool("StoreItemPurchaseDisabledForUnauthenticatedUsers", false)) {
                  throw new FusionEJBException("You must be authenticated before you can purchase a virtual gift.");
               }

               recipientUserData = userBean.loadUser(recipientUsername, false, false);
               if (recipientUserData == null || recipientUserData.status == UserData.StatusEnum.INACTIVE) {
                  throw new FusionEJBException("Unknown user '" + recipientUsername + "'");
               }

               connMaster = this.dataSourceMaster.getConnection();
               if (gift.getPrice() == 0.0D) {
                  if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.FREE_VIRTUAL_GIFTS_ENABLED)) {
                     throw new FusionEJBException("This gift is not available at the moment.");
                  }

                  ps = connMaster.prepareStatement("select a.id from user u left outer join accountentry a on (u.username = a.username and a.type = ? and a.amount = 0) where u.username = ? and u.referredby = ?");
                  ps.setInt(1, AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE.value());
                  ps.setString(2, buyerUsername);
                  ps.setString(3, recipientUsername);
                  rs = ps.executeQuery();
                  if (!rs.next()) {
                     throw new FusionEJBException("You can only purchase free gifts to your referrer");
                  }

                  if (rs.getObject(1) != null) {
                     throw new FusionEJBException("You can only purchase one free gift to your referrer");
                  }

                  rs.close();
                  ps.close();
               }

               AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               if (!accountBean.userCanAffordCost(buyerUsername, gift.getPrice(), gift.getCurrency(), connMaster)) {
                  log.warn(buyerUsername + " doesn't have enough credit to purchase [" + gift.getName() + "] priced ]" + gift.getPrice() + " " + gift.getCurrency() + "]");
                  throw new FusionEJBException("You do not have enough credit to purchase the gift");
               }

               if (isMutualGiftingHappening(buyerUsername, recipientUserData.username)) {
                  throw new FusionEJBException("Unable to send gift at this moment. Please try again.");
               }

               MemCachedClientWrapper.add(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipientUserData.username, "1", 10000L);
               ps = connMaster.prepareStatement("insert into virtualgiftreceived (username, datecreated, purchaselocation, virtualgiftid, sender, private, message) values (?, now(), ?, ?, ?, ?, ?)", 1);
               ps.setString(1, recipientUsername);
               ps.setInt(2, purchaseLocation);
               ps.setInt(3, gift.getId());
               ps.setString(4, buyerUsername);
               ps.setInt(5, privateGift ? 1 : 0);
               ps.setString(6, message);
               int rowsUpdated = ps.executeUpdate();
               if (rowsUpdated != 1) {
                  throw new EJBException("Internal Server Error (Unable to record gift purchase)");
               }

               rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  throw new EJBException("Internal Server Error (Unable to record gift purchase)");
               }

               virtualGiftReceivedId = rs.getInt(1);
               AccountEntryData accountEntry = new AccountEntryData();
               accountEntry.reference = Integer.toString(virtualGiftReceivedId);
               accountEntry.username = buyerUsername;
               accountEntry.type = AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE;
               accountEntry.description = "Purchased the gift " + gift.getName() + " for " + recipientUsername;
               accountEntry.currency = gift.getCurrency();
               accountEntry.amount = -gift.getPrice();
               accountEntry.tax = 0.0D;
               accountBean.createAccountEntry(connMaster, accountEntry, accountEntrySourceData);
            } catch (SQLException var43) {
               throw new EJBException(var43.getMessage());
            } catch (CreateException var44) {
               throw new EJBException(var44.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var40) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var39) {
                  ps = null;
               }

               try {
                  if (connMaster != null) {
                     connMaster.close();
                  }
               } catch (SQLException var38) {
                  connMaster = null;
               }

               try {
                  MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipientUserData.username);
               } catch (Exception var37) {
                  log.info("Failed to remove memcached key for concurrent mutual gifting due to: " + var37.getMessage());
               }

            }

            if (!privateGift) {
               if (SystemProperty.getBool("UseRedisDataStore", true)) {
                  if (recipientUserData != null && recipientUserData.userID != null) {
                     GiftsReceivedCounter.incrementCacheCount(recipientUserData.userID);
                  } else {
                     log.error("Unable to increment gifts received counter because recipient User ID was null for username: " + recipientUsername);
                  }
               } else {
                  MemCachedClientWrapper.incr(MemCachedKeySpaces.CommonKeySpace.NUM_VIRTUAL_GIFTS_RECEIVED, recipientUsername);
               }
            }

            Map<Integer, UserData> recipientUserDataList = new HashMap();
            recipientUserDataList.put(virtualGiftReceivedId, recipientUserData);
            this.onPurchaseVirtualGift(buyerUsername, recipientUserDataList, gift, privateGift, message, chatroomName);

            try {
               ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
               contentBean.incrementStoreItemSold(StoreItemData.TypeEnum.VIRTUAL_GIFT, gift.getId(), (Connection)null);
            } catch (CreateException var41) {
               log.warn("Failed to incrementStoreItemSoldByNumber: " + var41.getMessage());
            } catch (SQLException var42) {
               log.warn("Failed to incrementStoreItemSoldByNumber: " + var42.getMessage());
            }

         }
      }
   }

   public Map<String, Integer> buyVirtualGiftForMultipleUsers(String buyerUsername, List<String> recipientUsernames, VirtualGiftData gift, int purchaseLocation, boolean privateGift, String message, boolean broadcastChatroomMessage, boolean isGiftAll, AccountEntrySourceData accountEntrySourceData) throws EJBException, FusionEJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      message = StringUtil.stripHTML(message);
      if (recipientUsernames != null && !recipientUsernames.isEmpty()) {
         if (gift == null) {
            throw new FusionEJBException("Unknown gift");
         } else if (gift.getStatus() == VirtualGiftData.StatusEnum.INACTIVE) {
            throw new FusionEJBException("The " + gift.getName() + " gift is no longer available.");
         } else if (gift.getNumAvailable() != null && gift.getNumAvailable() > 1 && gift.getNumSold() + recipientUsernames.size() >= gift.getNumAvailable()) {
            throw new FusionEJBException("The " + gift.getName() + " gift is sold out or not enough for all users!");
         } else {
            HashMap virtualGiftReceivedIdMap = new HashMap(recipientUsernames.size());

            try {
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.BUY_VIRTUALGIFT, userBean.getUserAuthenticatedAccessControlParameter(buyerUsername)) && SystemProperty.getBool("StoreItemPurchaseDisabledForUnauthenticatedUsers", false)) {
                  throw new FusionEJBException("You must be authenticated before you can purchase a virtual gift.");
               }

               connMaster = this.dataSourceMaster.getConnection();
               if (gift.getPrice() == 0.0D) {
                  if (recipientUsernames.size() != 1) {
                     throw new FusionEJBException("You can only purchase free gifts to your referrer");
                  }

                  ps = connMaster.prepareStatement("select a.id from user u left outer join accountentry a on (u.username = a.username and a.type = ? and a.amount = 0) where u.username = ? and u.referredby = ?");
                  ps.setInt(1, AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE.value());
                  ps.setString(2, buyerUsername);
                  ps.setString(3, (String)recipientUsernames.get(0));
                  rs = ps.executeQuery();
                  if (!rs.next()) {
                     throw new FusionEJBException("You can only purchase free gifts to your referrer");
                  }

                  if (rs.getObject(1) != null) {
                     throw new FusionEJBException("You can only purchase one free gift to your referrer");
                  }

                  rs.close();
                  ps.close();
               }

               AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               if (!accountBean.userCanAffordCost(buyerUsername, gift.getRoundedPrice() * (double)recipientUsernames.size(), gift.getCurrency(), connMaster)) {
                  log.warn(buyerUsername + " doesn't have enough credit to gift shower [" + gift.getName() + "] priced [" + gift.getPrice() + " " + gift.getCurrency() + " X " + recipientUsernames.size() + "]");
                  throw new FusionEJBException("You do not have enough credit to purchase the gift");
               }

               AccountEntryData accountEntry = new AccountEntryData();
               accountEntry.username = buyerUsername;
               accountEntry.type = AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE;
               ps = connMaster.prepareStatement("insert into virtualgiftreceived (username, datecreated, purchaselocation, virtualgiftid, sender, private, message) values (?, now(), ?, ?, ?, ?, ?)", 1);
               Iterator i$ = recipientUsernames.iterator();

               while(i$.hasNext()) {
                  String recipient = (String)i$.next();
                  userBean.getUserID(recipient, (Connection)null);
                  ps.setString(1, recipient);
                  ps.setInt(2, purchaseLocation);
                  ps.setInt(3, gift.getId());
                  ps.setString(4, buyerUsername);
                  ps.setInt(5, privateGift ? 1 : 0);
                  ps.setString(6, message);
                  if (!isMutualGiftingHappening(buyerUsername, recipient)) {
                     MemCachedClientWrapper.add(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipient, "1", 10000L);
                     int rowsUpdated = ps.executeUpdate();
                     if (rowsUpdated != 1) {
                        throw new EJBException("Internal Server Error (Unable to record gift purchase)");
                     }

                     rs = ps.getGeneratedKeys();
                     if (!rs.next()) {
                        throw new EJBException("Internal Server Error (Unable to record gift purchase)");
                     }

                     int virtualGiftReceivedId = rs.getInt(1);
                     virtualGiftReceivedIdMap.put(recipient, virtualGiftReceivedId);
                     rs.close();
                     accountEntry.currency = gift.getCurrency();
                     accountEntry.amount = -gift.getPrice();
                     accountEntry.fundedAmount = null;
                     accountEntry.tax = 0.0D;
                     accountEntry.reference = Integer.toString(virtualGiftReceivedId);
                     accountEntry.description = String.format("Purchased the gift %s for %s", gift.getName(), recipient);
                     accountBean.createAccountEntry(connMaster, accountEntry, accountEntrySourceData);

                     try {
                        if (!privateGift) {
                           if (SystemProperty.getBool("UseRedisDataStore", true)) {
                              try {
                                 int recipientUserId = userBean.getUserID(recipient, (Connection)null);
                                 GiftsReceivedCounter.incrementCacheCount(recipientUserId);
                              } catch (EJBException var39) {
                                 log.error("Unable to increment gifts received counter for username [" + recipient + "]: " + var39);
                              }
                           } else {
                              MemCachedClientWrapper.incr(MemCachedKeySpaces.CommonKeySpace.NUM_VIRTUAL_GIFTS_RECEIVED, recipient);
                           }
                        }

                        MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipient);
                     } catch (Exception var40) {
                        log.info("Failed to remove memcached key for concurrent mutual gifting due to: " + var40.getMessage());
                     }
                  }
               }

               ps.close();
            } catch (SQLException var41) {
               throw new EJBException(var41.getMessage());
            } catch (CreateException var42) {
               throw new EJBException(var42.getMessage());
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var38) {
                  rs = null;
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var37) {
                  ps = null;
               }

               try {
                  if (connMaster != null) {
                     connMaster.close();
                  }
               } catch (SQLException var36) {
                  connMaster = null;
               }

            }

            if (virtualGiftReceivedIdMap.isEmpty()) {
               throw new FusionEJBException("Unable to perform /gift all at the moment. Please try again.");
            } else {
               return virtualGiftReceivedIdMap;
            }
         }
      } else {
         throw new FusionEJBException("Please provide one or more recipient for this gift");
      }
   }

   public static boolean isMutualGiftingHappening(String buyerUsername, String recipientUsername) {
      try {
         Map<String, Object> data = MemCachedClientWrapper.getMulti(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, new String[]{buyerUsername + "/" + recipientUsername, recipientUsername + "/" + buyerUsername});
         if (data != null && !data.isEmpty()) {
            Iterator i$ = data.values().iterator();

            while(i$.hasNext()) {
               Object obj = i$.next();
               if (obj != null) {
                  log.warn("Detected mutual gifting for users " + buyerUsername + " and " + recipientUsername);
                  return true;
               }
            }
         }
      } catch (Exception var5) {
         log.warn("Unable to check for concurrent mutual gifting due to: " + var5.getMessage());
      }

      return false;
   }

   public boolean rateVirtualGift(int userID, String giftName, int rating) throws EJBException, FusionEJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("SELECT si.* FROM storeitem si, virtualgift vg WHERE vg.id = si.referenceid AND vg.Name=? AND si.Type=1");
         ps.setString(1, giftName);
         rs = ps.executeQuery();
         if (!rs.next()) {
            boolean var36 = false;
            return var36;
         }

         StoreItemData storeItem = new StoreItemData(rs);
         rs.close();
         ps.close();
         rs = null;
         ps = null;
         connSlave.close();
         connSlave = null;
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("INSERT INTO storeitemrating (storeitemid, userid, datecreated, rating) VALUES (?, ?, NOW(), ?) ON DUPLICATE KEY UPDATE rating = ?");
         ps.setInt(1, storeItem.id);
         ps.setInt(2, userID);
         ps.setInt(3, rating);
         ps.setInt(4, rating);
         int rowsUpdated = ps.executeUpdate();
         ps.close();
         if (rowsUpdated > 0) {
            int total = 0;
            int numRatings = 1;
            ps = connMaster.prepareStatement("SELECT SUM(rating) AS total, COUNT(*) AS numratings FROM storeitemrating WHERE storeitemid = ?");
            ps.setInt(1, storeItem.id);
            rs = ps.executeQuery();
            if (rs.next()) {
               total = rs.getInt("total");
               numRatings = rs.getInt("numratings");
            }

            rs.close();
            rs = null;
            float average = (float)total / (float)numRatings;
            ps = connMaster.prepareStatement("INSERT INTO storeitemratingsummary (storeitemid, average, total, numratings) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE average = ?, total = ?, numratings = ?");
            ps.setInt(1, storeItem.id);
            ps.setFloat(2, average);
            ps.setInt(3, total);
            ps.setInt(4, numRatings);
            ps.setFloat(5, average);
            ps.setInt(6, total);
            ps.setInt(7, numRatings);
            ps.executeUpdate();
            ps.close();
            ps = null;
         }

         connMaster.close();
         connMaster = null;
      } catch (SQLException var34) {
         throw new EJBException(var34.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var31) {
            connSlave = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var30) {
            connMaster = null;
         }

      }

      return true;
   }

   public void buyAvatarItem(int buyerUserID, int recipientUserID, int avatarItemID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection connMaster = null;
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select * from storeitem where type = ? and referenceid = ?");
         ps.setInt(1, StoreItemData.TypeEnum.AVATAR.value());
         ps.setInt(2, avatarItemID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid avatar item ID " + avatarItemID);
         }

         StoreItemData storeItem = new StoreItemData(rs);
         if (storeItem.status != StoreItemData.StatusEnum.ACTIVE) {
            throw new EJBException("The avatar item " + storeItem.name + " is no longer available");
         }

         if (storeItem.expiryDate != null && storeItem.expiryDate.getTime() <= System.currentTimeMillis()) {
            throw new EJBException("The avatar item " + storeItem.name + " is no longer available");
         }

         if (storeItem.numAvailable != null && storeItem.numAvailable == 0) {
            throw new EJBException("The avatar item " + storeItem.name + " is sold out");
         }

         if (storeItem.forSale == null || !storeItem.forSale) {
            throw new EJBException("The avatar item " + storeItem.name + " is not for sale");
         }

         rs.close();
         ps.close();
         ps = connSlave.prepareStatement("select * from avataruseritem where userid = ? and avataritemid = ?");
         ps.setInt(1, recipientUserID);
         ps.setInt(2, avatarItemID);
         rs = ps.executeQuery();
         if (rs.next()) {
            throw new EJBException("The recipient already owned the avatar item");
         }

         rs.close();
         ps.close();
         ps = connSlave.prepareStatement("select u.username, u.countryid from userid uid, user u where uid.username = u.username and uid.id = ?");
         ps.setInt(1, buyerUserID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid buyer ID " + buyerUserID);
         }

         String buyerUsername = rs.getString("username");
         Integer buyerCountryID = (Integer)rs.getObject("countryid");
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.BUY_AVATAR, userBean.getUserAuthenticatedAccessControlParameter(buyerUsername))) {
            throw new EJBException("You must be authenticated before you can purchase an avatar item.");
         }

         String recipientUsername;
         if (buyerUserID == recipientUserID) {
            recipientUsername = buyerUsername;
         } else {
            ps.setInt(1, recipientUserID);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException("Invalid recipient ID " + recipientUserID);
            }

            recipientUsername = rs.getString("username");
         }

         if (storeItem.migLevelMin != null && MemCacheOrEJB.getUserReputationLevel(recipientUsername) < storeItem.migLevelMin) {
            throw new EJBException("The recipient's mig level is not high enough");
         }

         rs.close();
         ps.close();
         connSlave.close();
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         if (!accountBean.userCanAffordCost(buyerUsername, storeItem.price, storeItem.currency, connMaster)) {
            throw new EJBException("You do not have enough credit to purchase the avatar item");
         }

         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("INSERT INTO avataruseritem (userid, avataritemid, used) SELECT ?, ?, 0 FROM DUAL WHERE NOT EXISTS (SELECT * FROM avataruseritem WHERE userid = ? and avataritemid = ?)", 1);
         ps.setInt(1, recipientUserID);
         ps.setInt(2, avatarItemID);
         ps.setInt(3, recipientUserID);
         ps.setInt(4, avatarItemID);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("You already own the avatar item");
         }

         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = buyerUsername;
         accountEntry.type = AccountEntryData.TypeEnum.AVATAR_PURCHASE;
         accountEntry.reference = Integer.toString(avatarItemID);
         accountEntry.description = "Purchased avatar item " + storeItem.name + " for " + recipientUsername;
         accountEntry.currency = storeItem.currency;
         accountEntry.amount = -storeItem.price;
         accountEntry.tax = 0.0D;
         accountBean.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);

         try {
            UserData buyerUserData = userBean.loadUserFromID(recipientUserID);
            AvatarItemPurchasedTrigger trigger = new AvatarItemPurchasedTrigger(buyerUserData);
            trigger.amountDelta = storeItem.price;
            trigger.currency = storeItem.currency;
            trigger.quantityDelta = 1;
            trigger.storeItemID = storeItem.id;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var38) {
            log.warn("Unable to notify reward system", var38);
         }

         ContentLocal contentBean = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
         contentBean.incrementStoreItemSold(StoreItemData.TypeEnum.AVATAR, avatarItemID, connMaster);
      } catch (SQLException var39) {
         throw new EJBException(var39.getMessage());
      } catch (Exception var40) {
         throw new EJBException(var40.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var37) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var36) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var35) {
            connSlave = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var34) {
            connMaster = null;
         }

      }

   }

   public void incrementStoreItemSold(StoreItemData.TypeEnum type, int referenceID, Connection masterConn) throws SQLException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (SystemProperty.getBool("IncrementStoreItemSoldEnabled", true)) {
         try {
            ch = new ConnectionHolder(this.dataSourceMaster, masterConn);
            ps = ch.getConnection().prepareStatement("UPDATE storeitem SET numsold = numsold + 1 WHERE type = ? AND referenceid = ? AND ((numavailable IS NOT NULL AND numsold < numavailable) OR numavailable IS NULL)");
            ps.setInt(1, type.value());
            ps.setInt(2, referenceID);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
               throw new EJBException("The store item is sold out!");
            }

            ps.close();
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var20) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var19) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var18) {
               ch = null;
            }

         }
      }

   }

   public void incrementStoreItemSoldByNumber(StoreItemData.TypeEnum type, int referenceID, int increment, Connection masterConn) throws SQLException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      if (SystemProperty.getBool("IncrementStoreItemSoldEnabled", true)) {
         try {
            ch = new ConnectionHolder(this.dataSourceMaster, masterConn);
            ps = ch.getConnection().prepareStatement("UPDATE storeitem SET numsold = numsold + ? WHERE type = ? AND referenceid = ? AND ((numavailable IS NOT NULL AND numsold + ? <= numavailable) OR numavailable IS NULL)");
            ps.setInt(1, increment);
            ps.setInt(2, type.value());
            ps.setInt(3, referenceID);
            ps.setInt(4, increment);
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
               throw new EJBException("The store item is sold out or not enough left for the amount requested!");
            }

            ps.close();
         } finally {
            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var21) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var20) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var19) {
               ch = null;
            }

         }
      }

   }

   public StoreItemData getStoreItem(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      StoreItemData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT * FROM storeitem WHERE id = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = new StoreItemData(rs);
            return var5;
         }

         var5 = null;
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var5;
   }

   public void resetCachedRewardPrograms() {
      RewardCentre.getInstance().resetCachedRewardPrograms();
   }

   public List<RewardProgramProcessorMappingData> getRewardProgramProcessorMapping() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      LinkedList list = new LinkedList();

      LinkedList var25;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from rewardprogramprocessormapping rpm, rewardprogramprocessors r where r.id = rpm.processorID and status = 1 order by programType,sequence");
         rs = ps.executeQuery();
         HashMap mappings = new HashMap();

         while(rs.next()) {
            RewardProgramData.TypeEnum programType = RewardProgramData.TypeEnum.fromValue(rs.getInt("programType"));
            RewardProgramProcessorMappingData data = (RewardProgramProcessorMappingData)mappings.get(programType);
            if (data == null) {
               data = new RewardProgramProcessorMappingData(programType);
               mappings.put(programType, data);
            }

            String className = rs.getString("className");
            data.addProcessorClass(className);
            if (log.isDebugEnabled()) {
               log.debug("Added [" + className + "] to [" + programType + "]");
            }
         }

         log.debug(" Found [" + mappings.size() + "] processors for all triggers");
         list.addAll(mappings.values());
         var25 = list;
      } catch (SQLException var23) {
         throw new EJBException("Unable to load reward program processor mapping: " + var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var25;
   }

   public List<RewardProgramData> getRewardPrograms() throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         Map<Integer, List<String>> idToOutcomeProcessorList = this.getActiveRewardProgramOutcomeProcessors(conn);
         ps = conn.prepareStatement("SELECT p.*, s.storeitemid, g.groupid, b.badgeid,usi.storeitemid as unlockedstoreitemid, usi.quantity as unlockedstoreitemquantity, merchpoints.points as merchrewardpoints, statehandler.rewardprogramstatehandlerclassname as rewardprogramstatehandlerclassname FROM rewardprogram p LEFT OUTER JOIN storeitemreward s ON (p.id = s.rewardprogramid) LEFT OUTER JOIN groupmembershipreward g ON (p.id = g.rewardprogramid) LEFT OUTER JOIN badgereward b ON (p.id = b.rewardprogramid) LEFT OUTER JOIN unlockedstoreitemreward usi ON (p.id = usi.rewardprogramid) LEFT OUTER JOIN merchantpointsreward merchpoints ON (p.id = merchpoints.rewardprogramid) LEFT OUTER JOIN rewardprogramstatehandler statehandler ON (p.id = statehandler.rewardprogramid) WHERE p.status = ? and startdate < now() and enddate > now()");
         ps.setInt(1, RewardProgramData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         List<RewardProgramData> programs = new LinkedList();

         for(RewardProgramData program = null; rs.next(); program.setParameters(this.getRewardProgramParameters(program.id))) {
            if (program == null || program.id != rs.getInt("id")) {
               program = new RewardProgramData(rs);
               programs.add(program);
            }

            Integer storeItemID = (Integer)rs.getObject("storeitemid");
            if (storeItemID != null && !program.storeItemRewards.contains(storeItemID)) {
               program.storeItemRewards.add(storeItemID);
            }

            Integer groupID = (Integer)rs.getObject("groupid");
            if (groupID != null && !program.groupMembershipRewards.contains(groupID)) {
               program.groupMembershipRewards.add(groupID);
            }

            Integer badgeID = (Integer)rs.getObject("badgeid");
            if (badgeID != null && !program.badgeRewards.contains(badgeID)) {
               program.badgeRewards.add(badgeID);
            }

            List<String> outcomeProcessorList = (List)idToOutcomeProcessorList.get(program.id);
            if (outcomeProcessorList != null) {
               program.setOutcomeProcessorClasses(outcomeProcessorList);
            }

            int unlockedStoreItemID = rs.getInt("unlockedstoreitemid");
            if (!rs.wasNull()) {
               int unlockedStoreItemQuantity = rs.getInt("unlockedstoreitemquantity");
               if (unlockedStoreItemQuantity > 0) {
                  program.addToStoreItemToUnlockRewards(unlockedStoreItemID, unlockedStoreItemQuantity);
               }
            }
         }

         LinkedList var29 = programs;
         return var29;
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage(), var27);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }
   }

   private Map<Integer, List<String>> getActiveRewardProgramOutcomeProcessors(Connection conn) throws SQLException {
      Map<Integer, List<String>> result = new HashMap();
      PreparedStatement ps = null;
      ResultSet rs = null;
      ps = conn.prepareStatement("select  pocp.rewardprogramid, pocp.classname  from  rewardprogram p join  rewardprogramoutcomeprocessors pocp on (p.id = pocp.rewardprogramid and p.status=? and pocp.enabled=1)  order by pocp.rewardprogramid asc,pocp.sequence asc");

      HashMap var18;
      try {
         ps.setInt(1, RewardProgramData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();

         try {
            String className;
            Object rewardProgramIDList;
            for(; rs.next(); ((List)rewardProgramIDList).add(className)) {
               Integer rewardProgramID = rs.getInt("rewardprogramid");
               className = rs.getString("classname");
               rewardProgramIDList = (List)result.get(rewardProgramID);
               if (rewardProgramIDList == null) {
                  rewardProgramIDList = new ArrayList();
                  result.put(rewardProgramID, rewardProgramIDList);
               }
            }

            var18 = result;
         } finally {
            rs.close();
         }
      } finally {
         ps.close();
      }

      return var18;
   }

   private Map<String, String> getRewardProgramParameters(int rewardProgramID) throws EJBException {
      HashMap<String, String> map = new HashMap();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashMap var24;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select ParamName, ParamValue from rewardprogramparameters where rewardprogramID = ?");
         ps.setInt(1, rewardProgramID);
         rs = ps.executeQuery();

         while(rs.next()) {
            String paramName = rs.getString("paramName");
            String paramValue = rs.getString("paramValue");
            if (!StringUtil.isBlank(paramName) && !StringUtil.isBlank(paramValue)) {
               map.put(paramName, paramValue);
            }
         }

         var24 = map;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var24;
   }

   /** @deprecated */
   public void giveRewards(int rewardProgramID, int userID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      this.giveRewards(rewardProgramID, userID, accountEntrySourceData, (List)null, (Map)null);
   }

   public void giveRewards(int rewardProgramID, int userID, AccountEntrySourceData accountEntrySourceData, List<RewardProgramOutcomeData> rewardProgramOutcomes, Map<String, String> templateData) throws EJBException {
      RewardProgramData programData = RewardCentre.getInstance().getRewardProgram(rewardProgramID);
      if (programData == null) {
         throw new EJBException("Invalid reward program ID " + rewardProgramID);
      } else {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUserFromID(userID);
            if (userData == null) {
               throw new EJBException("Invalid user ID " + userID);
            } else {
               log.debug("Begin invoking awardUser(...)");
               ContentLocal contentEJB = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
               RewardProgramCompletionData rewardCompletionData = contentEJB.awardUser(programData, userData, accountEntrySourceData, rewardProgramOutcomes, templateData);
               log.debug("End invoking awardUser(...)");
               log.debug("Begin sending reward program completion trigger");
               this.sendRewardProgramCompletionMechanicTrigger(rewardCompletionData);
               log.debug("End sending reward program completion trigger");
               log.debug("Begin invoking sendRewardProgramCompletionNotifications(...)");
               this.sendRewardProgramCompletionNotifications(rewardCompletionData, templateData);
               log.debug("End invoking sendRewardProgramCompletionNotifications(...)");
            }
         } catch (CreateException var11) {
            throw new EJBException("Create exception occurred.Exception:" + var11, var11);
         }
      }
   }

   private static boolean isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum type) {
      return ((Set)ContentBean.SingletonHolder.getDispatchableReferredUserRewardedTriggerTypes().getValue()).contains((short)type.value());
   }

   private static boolean isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum type) {
      return ((Set)ContentBean.SingletonHolder.getDispatchableUserRewardedTriggerTypes().getValue()).contains((short)type.value());
   }

   private static boolean isPassThroughEmailTemplate(int emailTemplateId) {
      return ((Set)ContentBean.SingletonHolder.getPassThroughEmailTemplateIds().getValue()).contains(emailTemplateId);
   }

   private void sendRewardProgramCompletionMechanicTrigger(RewardProgramCompletionData programCompletionData) {
      if (log.isDebugEnabled()) {
         log.debug("Sending mechanic trigger for reward completion event:" + programCompletionData);
      }

      try {
         this.sendRewardProgramCompletionMechanicTriggerForUser(programCompletionData);
      } catch (Exception var5) {
         log.error("sendRewardProgramCompletionMechanicTriggerForUser error.Exception:" + var5, var5);
      }

      try {
         this.sendRewardProgramCompletionMechanicTriggerForReferrer(programCompletionData);
      } catch (Exception var4) {
         log.error("sendRewardProgramCompletionMechanicTriggerForInviter error.Exception:" + var4, var4);
      }

      try {
         this.sendRewardProgramCompletionMechanicTriggerForMerchantTagger(programCompletionData);
      } catch (Exception var3) {
         log.error("sendRewardProgramCompletionMechanicTriggerForMerchantTagger error.Exception:" + var3, var3);
      }

   }

   private void sendRewardProgramCompletionMechanicTriggerForUser(RewardProgramCompletionData programCompletionData) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_TRIGGER_ON_USER_REWARDED)) {
         if (log.isDebugEnabled()) {
            log.debug("sendRewardProgramCompletionMechanicTriggerForUser(...):disabled.");
         }

      } else {
         this.sendUserRewardedWithReputationScoreTrigger(programCompletionData);
         this.sendUserMeetsRewardCriteriaTrigger(programCompletionData);
         this.sendUserRewardedWithBadgeTrigger(programCompletionData);
         this.sendUserRewardedWithStoreItemTrigger(programCompletionData);
         this.sendUserRewardedWithUnlockedStoreItemTrigger(programCompletionData);
      }
   }

   private void sendUserRewardedWithReputationScoreTrigger(RewardProgramCompletionData programCompletionData) {
      if (isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum.USER_REWARDED_WITH_REPUTATION_SCORE)) {
         Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
         Iterator i$ = programCompletionData.getRewardedReputations().iterator();

         while(i$.hasNext()) {
            RewardedReputationData rewardedReputation = (RewardedReputationData)i$.next();
            this.sendRewardTrigger(new UserRewardedWithReputationScoreTrigger(programCompletionData.getUserData(), programCompletionData.getRewardProgramData(), rewardedReputation.getNewScore() - rewardedReputation.getOldScore(), rewardedReputation.getSource(), currentTimestamp));
         }

      }
   }

   private void sendUserRewardedWithBadgeTrigger(RewardProgramCompletionData programCompletionData) {
      if (isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum.USER_REWARDED_WITH_BADGE)) {
         Iterator i$ = programCompletionData.getRewardedBadges().iterator();

         while(i$.hasNext()) {
            RewardedBadgeData rewardedBadgeData = (RewardedBadgeData)i$.next();
            this.sendRewardTrigger(new UserRewardedWithBadgeTrigger(programCompletionData.getUserData(), programCompletionData.getRewardProgramData(), rewardedBadgeData.getId(), programCompletionData.getRewardedTime()));
         }

      }
   }

   private void sendUserRewardedWithStoreItemTrigger(RewardProgramCompletionData programCompletionData) {
      if (isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum.USER_REWARDED_WITH_STOREITEM)) {
         Iterator i$ = programCompletionData.getRewardedStoreItems().iterator();

         while(i$.hasNext()) {
            RewardedStoreItemData rewardedStoreItemData = (RewardedStoreItemData)i$.next();
            this.sendRewardTrigger(new UserRewardedWithStoreItemTrigger(programCompletionData.getUserData(), programCompletionData.getRewardProgramData(), rewardedStoreItemData, programCompletionData.getRewardedTime()));
         }

      }
   }

   private void sendUserRewardedWithUnlockedStoreItemTrigger(RewardProgramCompletionData programCompletionData) {
      if (isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum.USER_REWARDED_WITH_UNLOCKED_STOREITEM)) {
         Iterator i$ = programCompletionData.getRewardedUnlockedStoreItems().iterator();

         while(i$.hasNext()) {
            RewardedUnlockedStoreItemData unlockedStoreItemData = (RewardedUnlockedStoreItemData)i$.next();
            this.sendRewardTrigger(new UserRewardedWithUnlockedStoreItemTrigger(programCompletionData.getUserData(), programCompletionData.getRewardProgramData(), unlockedStoreItemData, programCompletionData.getRewardedTime()));
         }

      }
   }

   private void sendRewardProgramCompletionMechanicTriggerForReferrer(RewardProgramCompletionData programCompletionData) throws CreateException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_TRIGGER_ON_REFFERED_USER_REWARDED)) {
         if (log.isDebugEnabled()) {
            log.debug("sendRewardProgramCompletionMechanicTriggerForInviter(...):disabled.");
         }

      } else {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData inviterUserData = userBean.getInviterForSignUp(programCompletionData.getUserData().userID);
         if (log.isDebugEnabled()) {
            log.debug("Signup inviter for userID:" + programCompletionData.getUserData().userID + " is userid:" + (inviterUserData != null ? inviterUserData.userID.toString() : "null"));
         }

         if (inviterUserData != null && UserReferrerCache.isWithinCapAllowed(inviterUserData, programCompletionData.getUserData(), programCompletionData.getRewardProgramData())) {
            if (programCompletionData.getRewardCount() > 0) {
               this.sendReferredUserRewardedMigCreditsTrigger(inviterUserData, programCompletionData);
               this.sendReferredUserRewardedBadgesTrigger(inviterUserData, programCompletionData);
               this.sendReferredUserRewardedMigLevelTrigger(inviterUserData, programCompletionData);
               this.sendReferredUserRewardedStoreItemsTrigger(inviterUserData, programCompletionData);
               this.sendReferredUserRewardedGroupMembershipTrigger(inviterUserData, programCompletionData);
               this.sendReferredUserRewardedUnlockedStoreItemData(inviterUserData, programCompletionData);
            }

            this.sendReferredUserMeetsRewardCriteriaTrigger(inviterUserData, programCompletionData);
         }

      }
   }

   private void sendRewardProgramCompletionMechanicTriggerForMerchantTagger(RewardProgramCompletionData programCompletionData) throws CreateException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.ENABLE_TRIGGER_FOR_MERCHANT_TAGGER_ON_TAGGED_USER_REWARDED)) {
         if (log.isDebugEnabled()) {
            log.debug("sendRewardProgramCompletionMechanicTriggerForMerchantTagger(...):disabled.");
         }

      } else {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         MerchantTagData merchantTagData = accountBean.getMerchantTagFromUsername((Connection)null, programCompletionData.getUserData().username, true);
         if (merchantTagData != null) {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData merchantUserData = userBean.loadUserFromID(merchantTagData.merchantUserID);
            this.sendMerchantTaggedUserMeetsRewardCriteriaTrigger(merchantUserData, programCompletionData);
         }

      }
   }

   private void sendReferredUserRewardedUnlockedStoreItemData(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_UNLOCKED_STOREITEM)) {
         if (!programCompletionData.getRewardedUnlockedStoreItems().isEmpty()) {
            UserData referredUserData = programCompletionData.getUserData();
            RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
            Iterator i$ = programCompletionData.getRewardedUnlockedStoreItems().iterator();

            while(i$.hasNext()) {
               RewardedUnlockedStoreItemData unlockedStoreItemData = (RewardedUnlockedStoreItemData)i$.next();
               this.sendRewardTrigger(new ReferredUserRewardedWithUnlockedStoredItemTrigger(inviterUserData, referredUserData, referredUserRewardProgram, unlockedStoreItemData));
            }
         }

      }
   }

   private void sendReferredUserRewardedMigCreditsTrigger(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_MIGCREDITS)) {
         if (!programCompletionData.getRewardedMigCredits().isEmpty()) {
            UserData referredUserData = programCompletionData.getUserData();
            RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
            Collection<RewardedMigCreditData> rewardedMigCredits = programCompletionData.getRewardedMigCredits();
            Iterator i$ = rewardedMigCredits.iterator();

            while(i$.hasNext()) {
               RewardedMigCreditData rewardedMigCredit = (RewardedMigCreditData)i$.next();
               this.sendRewardTrigger(new ReferredUserRewardedWithMigCreditTrigger(inviterUserData, referredUserData, referredUserRewardProgram, rewardedMigCredit));
            }
         }

      }
   }

   private void sendReferredUserRewardedBadgesTrigger(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_BADGES)) {
         if (!programCompletionData.getRewardedBadges().isEmpty()) {
            UserData referredUserData = programCompletionData.getUserData();
            RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
            Collection<RewardedBadgeData> rewardedBadges = programCompletionData.getRewardedBadges();
            Iterator i$ = rewardedBadges.iterator();

            while(i$.hasNext()) {
               RewardedBadgeData rewardedBadge = (RewardedBadgeData)i$.next();
               this.sendRewardTrigger(new ReferredUserRewardedWithBadgeTrigger(inviterUserData, referredUserData, referredUserRewardProgram, rewardedBadge));
            }
         }

      }
   }

   private void sendReferredUserRewardedMigLevelTrigger(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (!isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_MIGLEVEL)) {
         if (log.isDebugEnabled()) {
            log.debug("sendReferredUserRewardedMigLevelTrigger(...):Disabled sending ReferredUserRewardedWithMigLevelTrigger trigger");
         }

      } else {
         if (programCompletionData.getOldMigLevel() < programCompletionData.getNewMigLevel()) {
            if (log.isDebugEnabled()) {
               log.debug("sendReferredUserRewardedMigLevelTrigger(...):sending ReferredUserRewardedWithMigLevelTrigger");
            }

            UserData referredUserData = programCompletionData.getUserData();
            RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
            int userMigLevelStarts = programCompletionData.getOldMigLevel() + 1;
            int userMigLevelEnds = programCompletionData.getNewMigLevel();

            for(int level = userMigLevelStarts; level <= userMigLevelEnds; ++level) {
               this.sendRewardTrigger(new ReferredUserRewardedWithMigLevelTrigger(inviterUserData, referredUserData, referredUserRewardProgram, level));
            }
         } else if (log.isDebugEnabled()) {
            log.debug("sendReferredUserRewardedMigLevelTrigger(...):No miglevel change detected. Not sending trigger");
         }

      }
   }

   private void sendReferredUserRewardedStoreItemsTrigger(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_STOREITEM)) {
         if (!programCompletionData.getRewardedStoreItems().isEmpty()) {
            UserData referredUserData = programCompletionData.getUserData();
            RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
            Collection<RewardedStoreItemData> rewardedStoreItems = programCompletionData.getRewardedStoreItems();
            Iterator i$ = rewardedStoreItems.iterator();

            while(i$.hasNext()) {
               RewardedStoreItemData rewardedStoreItem = (RewardedStoreItemData)i$.next();
               this.sendRewardTrigger(new ReferredUserRewardedWithStoredItemTrigger(inviterUserData, referredUserData, referredUserRewardProgram, rewardedStoreItem));
            }
         }

      }
   }

   private void sendReferredUserRewardedGroupMembershipTrigger(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_REWARDED_WITH_GROUPMEMBERSHIP)) {
         if (!programCompletionData.getRewardedGroupMemberships().isEmpty()) {
            UserData referredUserData = programCompletionData.getUserData();
            RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
            Collection<RewardedGroupMembershipData> rewardedGrpMemberships = programCompletionData.getRewardedGroupMemberships();
            Iterator i$ = rewardedGrpMemberships.iterator();

            while(i$.hasNext()) {
               RewardedGroupMembershipData rewardedGrpMembership = (RewardedGroupMembershipData)i$.next();
               this.sendRewardTrigger(new ReferredUserRewardedWithGroupMembershipTrigger(inviterUserData, referredUserData, referredUserRewardProgram, rewardedGrpMembership));
            }
         }

      }
   }

   private void sendUserMeetsRewardCriteriaTrigger(RewardProgramCompletionData programCompletionData) {
      if (isDispatchableUserRewardedTrigger(RewardProgramData.TypeEnum.USER_MEETS_REWARD_CRITERIA)) {
         this.sendRewardTrigger(new UserMeetsRewardCriteriaTrigger(programCompletionData.getUserData(), programCompletionData.getRewardProgramData(), programCompletionData.getRewardCount() > 0, programCompletionData.getRewardedTime()));
      }
   }

   private void sendReferredUserMeetsRewardCriteriaTrigger(UserData inviterUserData, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.REFERRED_USER_MEETS_REWARD_CRITERIA)) {
         UserData referredUserData = programCompletionData.getUserData();
         RewardProgramData referredUserRewardProgram = programCompletionData.getRewardProgramData();
         this.sendRewardTrigger(new ReferredUserMeetsRewardCriteriaTrigger(inviterUserData, referredUserData, referredUserRewardProgram, programCompletionData.getRewardCount() > 0));
      }
   }

   private void sendMerchantTaggedUserMeetsRewardCriteriaTrigger(UserData merchantUserDataTagger, RewardProgramCompletionData programCompletionData) {
      if (isDispatchableReferredUserRewardedTrigger(RewardProgramData.TypeEnum.MERCHANT_TAGGED_USER_MEETS_REWARD_CRITERIA)) {
         UserData taggedUserData = programCompletionData.getUserData();
         RewardProgramData taggedUserRewardProgram = programCompletionData.getRewardProgramData();
         this.sendRewardTrigger(new MerchantTaggedUserMeetsRewardCriteriaTrigger(merchantUserDataTagger, taggedUserData, taggedUserRewardProgram, programCompletionData.getRewardCount() > 0));
      }
   }

   private void sendRewardTrigger(RewardProgramTrigger trigger) {
      if (trigger != null) {
         try {
            if (log.isDebugEnabled()) {
               log.debug("sendRewardTrigger(" + trigger + ")");
            }

            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var3) {
            log.error("Unable to submit trigger:[" + trigger + "].Exception:" + var3, var3);
         }
      } else {
         log.warn("sendRewardTrigger: ignoring null trigger.");
      }

   }

   public RewardProgramCompletionData awardUser(RewardProgramData programData, UserData userData, AccountEntrySourceData accountEntrySourceData, List<RewardProgramOutcomeData> rewardProgramOutcomes, Map<String, String> templateData) throws EJBException {
      try {
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.PARTICIPATE_IN_MARKETING_MECHANICS, userData) && SystemProperty.getBool("RewardProgramDisabledForUnauthenticatedUsers", false)) {
            throw new EJBException("Unable to give reward to [" + userData.username + "] user is non-authenticated. ProgramID [" + programData.id + "]");
         } else {
            ArrayList<RewardedReputationData> rewardedReputations = new ArrayList();
            ArrayList<RewardedMigCreditData> rewardedMigCredits = new ArrayList();
            ArrayList<RewardedStoreItemData> rewardedStoreItems = new ArrayList();
            ArrayList<RewardedBadgeData> rewardedBadges = new ArrayList();
            ArrayList<RewardedGroupMembershipData> rewardedGroupMemberships = new ArrayList();
            ArrayList<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems = new ArrayList();
            ArrayList<NotificationTemplateOutcomeData> notificationTemplates = new ArrayList();
            Timestamp rewardedTime = new Timestamp(System.currentTimeMillis());
            long completionID = this.logRewardProgramCompletion(rewardedTime, programData, userData);
            this.giveUnlockedStoreItemRewards(programData, userData, completionID, rewardedUnlockedStoreItems);
            this.giveStoreItemRewards(programData, userData, completionID, rewardedStoreItems);
            this.giveGroupMembershipRewards((RewardProgramData)programData, userData, completionID, accountEntrySourceData, rewardedGroupMemberships);
            this.giveReputationReward(programData, userData, completionID, rewardedReputations);
            this.giveMigCreditReward(programData, userData, completionID, accountEntrySourceData, rewardedMigCredits);
            this.giveBadgeRewards(programData, userData, completionID, rewardedBadges);
            this.giveRewardFromOutcomeList(programData, userData, completionID, rewardProgramOutcomes, accountEntrySourceData, rewardedMigCredits, rewardedReputations, rewardedUnlockedStoreItems, notificationTemplates);
            this.giveMerchantRewardPoints(programData, userData, completionID);
            return new RewardProgramCompletionData(userData, programData, completionID, rewardedTime, rewardedReputations, rewardedMigCredits, rewardedStoreItems, rewardedBadges, rewardedGroupMemberships, rewardedUnlockedStoreItems, notificationTemplates, accountEntrySourceData);
         }
      } catch (RuntimeException var16) {
         throw var16;
      } catch (Exception var17) {
         throw new EJBException("Unhandled Exception:" + var17, var17);
      }
   }

   private void sendRewardProgramCompletionNotifications(RewardProgramCompletionData rewardProgramCompletion, Map<String, String> templateData) {
      Collection<RewardedReputationData> rewardedReputations = rewardProgramCompletion.getRewardedReputations();
      UserData userData = rewardProgramCompletion.getUserData();
      RewardProgramData programData = rewardProgramCompletion.getRewardProgramData();
      long completionID = rewardProgramCompletion.getCompletionid();
      this.sendRewardedReputationTriggers(rewardProgramCompletion);
      this.sendIMNotification(programData, userData, completionID);
      this.sendSMSNotification(programData, userData, completionID);
      this.sendEmailNotification(programData, userData, completionID, templateData);
      this.sendNotificationToUser(programData, userData, completionID, rewardProgramCompletion.getNotificationTemplates(), templateData);
   }

   private void sendRewardedReputationTriggers(RewardProgramCompletionData rewardProgramCompletion) {
      long completionID = rewardProgramCompletion.getCompletionid();
      Collection<RewardedReputationData> rewardedReputations = rewardProgramCompletion.getRewardedReputations();
      RewardProgramData programData = rewardProgramCompletion.getRewardProgramData();
      UserData userData = rewardProgramCompletion.getUserData();
      if (rewardedReputations != null && rewardedReputations.size() > 0) {
         try {
            log.debug("sendRewardProgramCompletionNotification():begin invalidateCacheAndNotifyReputationScoreUpdated");
            UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            userEJB.invalidateCacheAndNotifyReputationScoreUpdated(userData.userID);
            log.debug("sendRewardProgramCompletionNotification():end invalidateCacheAndNotifyReputationScoreUpdated");
         } catch (CreateException var9) {
            log.error("Unable to invalidate cache and notify user proxy for rewardprogramid:[" + programData + "],completionid:[" + completionID + "],userid:[" + userData.userID + "].Exception:" + var9, var9);
         }

         int minOldLevel = rewardProgramCompletion.getOldMigLevel();
         int maxNewLevel = rewardProgramCompletion.getNewMigLevel();
         if (maxNewLevel > minOldLevel) {
            if ((Boolean)SystemPropertyEntities.Temp.Cache.se218Enabled.getValue()) {
               this.tryUpdateChatRoomSizes(rewardProgramCompletion.getUserData(), minOldLevel, maxNewLevel);
            }

            EventQueue.enqueueSingleEvent(new UserDataUpdatedEvent(userData.username, UserDataUpdatedEvent.TypeEnum.PROFILE));
            this.sendReputationLevelIncreaseNotification(completionID, programData, userData, minOldLevel, maxNewLevel);
         }
      } else if (log.isDebugEnabled()) {
         log.debug("No rewarded reputation score/miglevel for rewardprogramid:[" + programData.id + "],completionid:[" + completionID + "],userid:[" + userData.userID + "]");
      }

   }

   private void tryUpdateChatRoomSizes(UserData userData, int oldLevel, int newLevel) {
      try {
         ReputationLevelData oldMigLevelData = MemCacheOrEJB.getReputationLevelDataForLevel(oldLevel);
         ReputationLevelData newMigLevelData = MemCacheOrEJB.getReputationLevelDataForLevel(newLevel);
         if (newMigLevelData.chatRoomSize > oldMigLevelData.chatRoomSize) {
            log.info("Resizing chat room size owned by [" + userData.username + "] Level:[" + newMigLevelData.level + "] to:[" + newMigLevelData.chatRoomSize + "]");
            Connection masterConn = this.dataSourceMaster.getConnection();

            try {
               this.updateUserOwnedRoomSize(userData.username, newMigLevelData.chatRoomSize, masterConn);
            } finally {
               masterConn.close();
            }
         }
      } catch (Exception var12) {
         log.error("Unable to change chat room sizes for user [" + userData.username + "] upon level up from [" + oldLevel + "] to [" + newLevel + "]. Exception :" + var12, var12);
      }

   }

   private void sendReputationLevelIncreaseNotification(long completionID, RewardProgramData programData, UserData userData, int minOldLevel, int maxNewLevel) {
      Timestamp rewardedTime = new Timestamp(System.currentTimeMillis());

      for(int level = minOldLevel + 1; level <= maxNewLevel; ++level) {
         this.triggerMiglevelIncreaseNotification(userData, level);
         if (programData.type != RewardProgramData.TypeEnum.MIG_LEVEL) {
            try {
               ReputationLevelIncreaseTrigger trigger = new ReputationLevelIncreaseTrigger(userData, level, rewardedTime, programData.id, programData.type.getId());
               trigger.quantityDelta = 1;
               trigger.amountDelta = 0.0D;
               RewardCentre.getInstance().sendTrigger(trigger);
            } catch (Exception var11) {
               log.error("Unable to submit reputation level increase trigger for rewardprogramid:[" + programData + "],completionid:[" + completionID + "],userid:[" + userData.userID + "].Exception:" + var11, var11);
            }
         }
      }

   }

   private void giveReputationReward(RewardProgramData programData, UserData userData, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      this.giveReputationReward(programData.id, userData, programData.levelReward, programData.scoreReward, completionID, rewardedReputations);
   }

   private void giveReputationReward(Integer programID, UserData userData, Integer migLevelBoostReward, Integer scoreBoostReward, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      this.giveLevelReward(programID, userData, migLevelBoostReward, completionID, rewardedReputations);
      this.giveScoreReward(programID, scoreBoostReward, userData, completionID, rewardedReputations);
   }

   private void sendNotificationToUser(RewardProgramData programData, UserData userData, long completionID, Collection<NotificationTemplateOutcomeData> notificationTemplates, Map<String, String> templateData) {
      Iterator i$ = notificationTemplates.iterator();

      while(i$.hasNext()) {
         NotificationTemplateOutcomeData notificationTemplate = (NotificationTemplateOutcomeData)i$.next();
         switch(notificationTemplate.type) {
         case EMAIL_TEMPLATE_ID:
            EmailTemplateIDOutcomeData emailTemplateIDOutcome = (EmailTemplateIDOutcomeData)notificationTemplate;
            this.sendEmailNotification(programData, userData, completionID, emailTemplateIDOutcome, templateData);
            break;
         case IMNOTIFICATION_TEMPLATE:
            IMNotificationTemplateOutcomeData imnotificationTemplateOutcome = (IMNotificationTemplateOutcomeData)notificationTemplate;
            this.sendIMNotification(programData, userData, completionID, imnotificationTemplateOutcome, templateData);
            break;
         default:
            log.warn("sendNotificationToUser() : Unsupported outcome type type:" + notificationTemplate.type);
         }
      }

   }

   private static String renderContent(String template, Map<String, String> templateData) throws IOException {
      return TemplateStringProcessor.process(template, templateData);
   }

   private void sendIMNotification(RewardProgramData programData, UserData userData, long completionID, IMNotificationTemplateOutcomeData imnotificationTemplateOutcome, Map<String, String> templateData) {
      try {
         String contentTemplate = imnotificationTemplateOutcome.getContentTemplate();
         if (!StringUtil.isBlank(contentTemplate)) {
            String imNotificationContent = renderContent(contentTemplate, templateData);
            this.sendIMNotification(programData.id, userData.username, imNotificationContent);
         } else {
            log.warn("sendIMNotification():Can't send imnotification for reward program id:[" + programData.id + "] since template is blank.");
         }
      } catch (Exception var9) {
         log.warn("Unable to send im notification. Exception:" + var9, var9);
      }

   }

   private void sendEmailNotification(RewardProgramData programData, UserData userData, long completionID, EmailTemplateIDOutcomeData emailTemplateIDOutcome, Map<String, String> templateData) {
      String emailTemplateIDStr = emailTemplateIDOutcome.getContentTemplate();
      int emailTemplateID = StringUtil.toIntOrDefault(emailTemplateIDStr, -1);
      if (emailTemplateID != -1) {
         if (userData.allowToSendEmailViaMM(programData)) {
            this.sendTemplatizedEmailNotification(userData.userID, emailTemplateID, templateData);
         } else {
            log.info(String.format("user:%s does not want to receive email:%s", userData.userID, programData.type));
         }
      } else {
         log.warn("sendEmailNotification() unable to send email notification. EmailID:[" + emailTemplateIDStr + "] must be an integer ");
      }

   }

   private void sendEmailNotification(RewardProgramData programData, UserData userData, long completionID, Map<String, String> templateData) {
      if (programData.emailTemplateID != null) {
         this.sendTemplatizedEmailNotification(userData.userID, programData.emailTemplateID, templateData != null ? templateData : Collections.EMPTY_MAP);
      } else {
         this.sendStaticContentEmailNotification(programData, userData, completionID);
      }

   }

   private void giveRewardFromOutcomeList(RewardProgramData programData, UserData userData, long completionID, List<RewardProgramOutcomeData> rewardProgramOutcomes, AccountEntrySourceData accountEntrySourceData, Collection<RewardedMigCreditData> rewardedMigCredits, Collection<RewardedReputationData> rewardedReputations, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItemDatas, Collection<NotificationTemplateOutcomeData> notificationTemplates) throws Exception {
      if (rewardProgramOutcomes != null && !rewardProgramOutcomes.isEmpty()) {
         Iterator i$ = rewardProgramOutcomes.iterator();

         while(i$.hasNext()) {
            RewardProgramOutcomeData rewardProgramOutcomeData = (RewardProgramOutcomeData)i$.next();
            switch(rewardProgramOutcomeData.type) {
            case EMAIL_TEMPLATE_ID:
            case IMNOTIFICATION_TEMPLATE:
               notificationTemplates.add((NotificationTemplateOutcomeData)rewardProgramOutcomeData);
               break;
            case BASIC:
               this.giveRewardFromBasicRewardProgramOutcomeData(programData, userData, completionID, (BasicRewardProgramOutcomeData)rewardProgramOutcomeData, accountEntrySourceData, rewardedMigCredits, rewardedReputations);
               break;
            case UNLOCKED_STORE_ITEMS:
               this.giveUnlockedStoreItemRewards(completionID, rewardedUnlockedStoreItemDatas, ((UnlockedStoreItemsRewardProgramOutcomeData)rewardProgramOutcomeData).getUnlockedStoreItems(), userData);
               break;
            default:
               log.warn("No available processor for processing rewardProgramOutcomeData.type [" + rewardProgramOutcomeData.type + "]");
            }
         }
      }

   }

   private void giveRewardFromBasicRewardProgramOutcomeData(RewardProgramData programData, UserData userData, long completionID, BasicRewardProgramOutcomeData outcomeData, AccountEntrySourceData accountEntrySourceData, Collection<RewardedMigCreditData> rewardedMigCredits, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      String remarks = StringUtil.isBlank(outcomeData.accountEntryRemarks) ? this.getDefaultAccountEntryDescription(programData) : outcomeData.accountEntryRemarks;
      if (log.isDebugEnabled()) {
         log.debug("giveRewardFromBasicRewardProgramOutcomeData() programData.id:[" + programData.id + "] outcomeData.accountEntryRemarks:[" + outcomeData.accountEntryRemarks + "]" + " CompletionID:[" + completionID + "] userData.username:[" + userData.username + "]. Account entry remarks:[" + remarks + "]");
      }

      this.giveReputationReward(programData.id, userData, outcomeData.migLevelReward, outcomeData.scoreReward, completionID, rewardedReputations);
      this.giveMigCreditReward(accountEntrySourceData, completionID, userData.username, remarks, outcomeData.migCreditAmount, outcomeData.migCreditCurrency, rewardedMigCredits);
      double migCreditReward;
      String migCreditCcy;
      double baseExchRate;
      if (outcomeData.migCreditAmount != null) {
         migCreditReward = outcomeData.migCreditAmount;
         migCreditCcy = outcomeData.migCreditCurrency;
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CurrencyData rewardCurrencyData = misBean.getCurrency(outcomeData.migCreditCurrency);
         baseExchRate = rewardCurrencyData.exchangeRate;
      } else {
         migCreditReward = 0.0D;
         migCreditCcy = CurrencyData.baseCurrency;
         baseExchRate = 0.0D;
      }

      this.logExtraBasicOutcomeRewarded(completionID, outcomeData.scoreReward, outcomeData.migLevelReward, migCreditReward, migCreditCcy, baseExchRate);
   }

   public int getRewardProgramsCompletionCount(int rewardprogramid, int userid) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      byte var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select count(*) from rewardprogramcompleted where rewardprogramid = ? and userid = ?");
         ps.setInt(1, rewardprogramid);
         ps.setInt(2, userid);
         rs = ps.executeQuery();
         if (rs.next()) {
            int var26 = rs.getInt(1);
            return var26;
         }

         var6 = 0;
      } catch (Exception var24) {
         log.error("Exception caught while getting rewardprogram completion count for rewardprogramid[" + rewardprogramid + "] for userid[" + userid + "] : " + var24.getMessage(), var24);
         throw new EJBException(var24);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return var6;
   }

   private long logRewardProgramCompletion(Timestamp logTimestamp, RewardProgramData programData, UserData userData) throws SQLException {
      return this.logRewardProgramCompletion(logTimestamp, programData.id, userData.userID, programData.scoreReward, programData.levelReward, programData.migCreditReward, programData.migCreditRewardCurrency);
   }

   private long logRewardProgramCompletion(Timestamp logTimestamp, int rewardProgramId, int userId, Integer scoreReward, Integer levelReward, Double migCreditReward, String migCreditRewardCurrency) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      long var11;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("INSERT INTO rewardprogramcompleted (rewardprogramid, userid, datecreated, scorereward, levelreward, migcreditreward, migcreditrewardcurrency) VALUES (?,?,?,?,?,?,?)", 1);
         ps.setInt(1, rewardProgramId);
         ps.setInt(2, userId);
         ps.setTimestamp(3, logTimestamp);
         ps.setInt(4, scoreReward == null ? 0 : scoreReward);
         ps.setInt(5, levelReward == null ? 0 : levelReward);
         ps.setDouble(6, migCreditReward == null ? 0.0D : migCreditReward);
         ps.setString(7, migCreditRewardCurrency);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to record reward program completed");
         }

         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Unable to record reward program completed");
         }

         var11 = rs.getLong(1);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return var11;
   }

   private void logGroupMembershipRewarded(Collection<Integer> groupIds, UserData userData, long completionId) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("INSERT INTO groupmembershiprewarded (rewardprogramcompletedid, groupid) VALUES (?,?)");
         ps.setLong(1, completionId);
         Iterator i$ = groupIds.iterator();

         while(i$.hasNext()) {
            Integer groupID = (Integer)i$.next();
            ps.setInt(2, groupID);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to record group membership rewarded");
            }
         }
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

   }

   private void logStoreItemRewarded(List<StoreItemData> storeItems, UserData userData, long completionId) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("INSERT INTO storeitemrewarded (rewardprogramcompletedid, storeitemid) VALUES (?,?)");
         ps.setLong(1, completionId);
         Iterator i$ = storeItems.iterator();

         do {
            if (!i$.hasNext()) {
               ps.close();
               return;
            }

            StoreItemData storeItem = (StoreItemData)i$.next();
            ps.setInt(2, storeItem.id);
         } while(ps.executeUpdate() == 1);

         throw new EJBException("Unable to record store item rewarded");
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }
   }

   private void logBadgeRewarded(Collection<Integer> badgeIds, UserData userData, long completionId) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("INSERT INTO BadgeRewarded (rewardprogramcompletedid, badgeid) VALUES (?,?)");
         ps.setLong(1, completionId);
         Iterator i$ = badgeIds.iterator();

         while(i$.hasNext()) {
            Integer badgeID = (Integer)i$.next();
            ps.setInt(2, badgeID);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to record badge rewarded");
            }
         }
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

   }

   private void logUnlockedStoreItemRewards(List<Long> storeItemInventoryIDs, long completionID) throws SQLException {
      if (storeItemInventoryIDs != null && storeItemInventoryIDs.size() != 0) {
         Connection conn = this.dataSourceMaster.getConnection();

         try {
            String sql = "insert into unlockedstoreitemrewarded (rewardprogramcompletedid,StoreItemInventoryID) values(?,?)";
            PreparedStatement ps = conn.prepareStatement("insert into unlockedstoreitemrewarded (rewardprogramcompletedid,StoreItemInventoryID) values(?,?)");

            try {
               Iterator i$ = storeItemInventoryIDs.iterator();

               while(i$.hasNext()) {
                  Long storeItemInventoryID = (Long)i$.next();
                  ps.setLong(1, completionID);
                  ps.setLong(2, storeItemInventoryID);
                  ps.addBatch();
               }

               int[] returnedUpdates = ps.executeBatch();
               assertBatchUpdates(returnedUpdates, storeItemInventoryIDs.size());
            } finally {
               ps.close();
            }
         } finally {
            conn.close();
         }
      }
   }

   private void logExtraBasicOutcomeRewarded(long completionID, int scorereward, int levelreward, double migcreditreward, String migcreditrewardcurrency, double baseExchRate) throws SQLException, CreateException {
      Connection conn = this.dataSourceMaster.getConnection();

      try {
         String sql = "insert into extrabasicoutcomerewarded ( RewardProgramCompletedID, DateCreated, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, BaseExchRate ) values(?,?,?,?,?,?,?)";
         PreparedStatement ps = conn.prepareStatement("insert into extrabasicoutcomerewarded ( RewardProgramCompletedID, DateCreated, ScoreReward, LevelReward, MigCreditReward, MigCreditRewardCurrency, BaseExchRate ) values(?,?,?,?,?,?,?)");

         try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            ps.setLong(1, completionID);
            ps.setTimestamp(2, now);
            ps.setInt(3, scorereward);
            ps.setInt(4, levelreward);
            ps.setDouble(5, migcreditreward);
            ps.setString(6, migcreditrewardcurrency);
            ps.setDouble(7, baseExchRate);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to record store item rewarded");
            }
         } finally {
            ps.close();
         }
      } finally {
         conn.close();
      }

   }

   private static void assertBatchUpdates(int[] returnedUpdateCounts, int expectedReturnedArrayLength) {
      if (returnedUpdateCounts == null) {
         throw new EJBException("null returned update count array.Expected: " + expectedReturnedArrayLength);
      } else if (returnedUpdateCounts.length != expectedReturnedArrayLength) {
         throw new EJBException("returned update count array length is " + returnedUpdateCounts.length + ".Expected: " + expectedReturnedArrayLength);
      } else {
         for(int i = 0; i < returnedUpdateCounts.length; ++i) {
            int returnedUpdateCount = returnedUpdateCounts[i];
            if (returnedUpdateCount <= 0 && returnedUpdateCount != -2) {
               throw new EJBException("returned update count for statement #" + (i + 1) + " is " + returnedUpdateCount);
            }
         }

      }
   }

   private void populateStoreItemIdToUnlockMap(Map<Integer, Integer> storeItemIdToUnlockCountMap, List<StoreItemToUnlockData> storeItemToUnlockDataList) {
      Iterator i$ = storeItemToUnlockDataList.iterator();

      while(i$.hasNext()) {
         StoreItemToUnlockData storeItemToUnlockData = (StoreItemToUnlockData)i$.next();
         Integer quantity = (Integer)storeItemIdToUnlockCountMap.get(storeItemToUnlockData.getStoreitemID());
         storeItemIdToUnlockCountMap.put(storeItemToUnlockData.getStoreitemID(), (quantity == null ? 0 : quantity) + storeItemToUnlockData.getQuantity());
      }

   }

   private void populateRewardedUnlockedStoreItems(Map<Integer, Integer> storeItemIdToUnlockCountMap, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems) {
      Iterator i$ = storeItemIdToUnlockCountMap.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Integer, Integer> storeItemIdToUnlockCount = (Entry)i$.next();
         int storeItemId = (Integer)storeItemIdToUnlockCount.getKey();
         int quantity = (Integer)storeItemIdToUnlockCount.getValue();
         StoreItemData storeItemData = this.getStoreItem(storeItemId);
         rewardedUnlockedStoreItems.add(new RewardedUnlockedStoreItemData(storeItemData, quantity));
      }

   }

   private void giveUnlockedStoreItemRewards(RewardProgramData programData, UserData userData, long completionID, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems) throws SQLException {
      this.giveUnlockedStoreItemRewards(programData.getStoreItemToUnlockRewards(), programData.itemRewardType, userData, completionID, rewardedUnlockedStoreItems);
   }

   private void giveUnlockedStoreItemRewards(List<StoreItemToUnlockData> storeItemToUnlockDataList, RewardProgramData.ItemRewardType itemRewardType, UserData userData, long completionID, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems) throws SQLException {
      if (storeItemToUnlockDataList != null && storeItemToUnlockDataList.size() != 0) {
         Map<Integer, Integer> storeItemIdToUnlockCountMap = new HashMap();
         if (RewardProgramData.ItemRewardType.RANDOM.value() == itemRewardType.value()) {
            SecureRandom random = new SecureRandom();
            int rewardIndex = random.nextInt(storeItemToUnlockDataList.size());
            StoreItemToUnlockData itemToUnlockData = (StoreItemToUnlockData)storeItemToUnlockDataList.get(rewardIndex);
            storeItemIdToUnlockCountMap.put(itemToUnlockData.getStoreitemID(), itemToUnlockData.getQuantity());
         } else {
            this.populateStoreItemIdToUnlockMap(storeItemIdToUnlockCountMap, storeItemToUnlockDataList);
         }

         this.giveUnlockedStoreItemRewards(completionID, rewardedUnlockedStoreItems, storeItemIdToUnlockCountMap, userData);
      }
   }

   private void giveUnlockedStoreItemRewards(long completionID, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems, Map<Integer, Integer> storeItemIdToUnlockCountMap, UserData userData) throws SQLException {
      if (storeItemIdToUnlockCountMap != null && !storeItemIdToUnlockCountMap.isEmpty()) {
         List<Long> storeItemInventoryIDs = this.addStoreItemsInventory(storeItemIdToUnlockCountMap, userData.userID, StoreItemInventoryData.StoreItemInventoryLocationEnum.UNLOCK);
         this.logUnlockedStoreItemRewards(storeItemInventoryIDs, completionID);
         this.populateRewardedUnlockedStoreItems(storeItemIdToUnlockCountMap, rewardedUnlockedStoreItems);
      }

   }

   private void giveStoreItemRewards(RewardProgramData programData, UserData userData, long completionID, Collection<RewardedStoreItemData> rewardedStoreItems) throws SQLException {
      this.giveStoreItemRewards(programData.id, programData.name, programData.storeItemRewards, programData.itemRewardType, userData, completionID, rewardedStoreItems);
   }

   private void giveStoreItemRewards(int programId, String programName, List<Integer> storeItemRewards, RewardProgramData.ItemRewardType itemRewardType, UserData userData, long completionID, Collection<RewardedStoreItemData> rewardedStoreItems) throws SQLException {
      if (storeItemRewards != null && storeItemRewards.size() != 0) {
         List<StoreItemData> storeItems = new LinkedList();
         SecureRandom random = new SecureRandom();
         StoreItemData storeItemData;
         Iterator i$;
         if (RewardProgramData.ItemRewardType.RANDOM.value() == itemRewardType.value()) {
            if (storeItemRewards != null && !storeItemRewards.isEmpty()) {
               Integer itemId = random.nextInt(storeItemRewards.size());
               storeItemData = this.getStoreItem((Integer)storeItemRewards.get(itemId));
               if (storeItemData == null) {
                  throw new EJBException("Invalid store item ID " + itemId);
               }

               storeItems.add(storeItemData);
            }
         } else {
            i$ = storeItemRewards.iterator();

            while(i$.hasNext()) {
               Integer storeItemID = (Integer)i$.next();
               StoreItemData storeItemData = this.getStoreItem(storeItemID);
               if (storeItemData == null) {
                  throw new EJBException("Invalid store item ID " + storeItemID);
               }

               storeItems.add(storeItemData);
            }
         }

         this.logStoreItemRewarded(storeItems, userData, completionID);
         i$ = storeItems.iterator();

         while(i$.hasNext()) {
            storeItemData = (StoreItemData)i$.next();
            switch(storeItemData.type) {
            case VIRTUAL_GIFT:
               this.giveVirtualGiftReward(programId, programName, userData, storeItemData, completionID, rewardedStoreItems);
               break;
            case AVATAR:
               this.giveAvatarReward(programId, userData, storeItemData, completionID, rewardedStoreItems);
               break;
            case STICKER:
            case EMOTICON:
               this.giveStickerOrEmoticonPackReward(programId, userData, storeItemData, completionID, rewardedStoreItems);
            case SUPER_EMOTICON:
            case THEME:
            }
         }

      }
   }

   private void giveVirtualGiftReward(Integer programId, String programName, UserData userData, StoreItemData storeItemData, long completionID, Collection<RewardedStoreItemData> rewardedStoreItems) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("INSERT INTO virtualgiftreceived (username, datecreated, purchaselocation, virtualgiftid, sender, private, message) VALUES (?, now(), ?, ?, ?, 0, ?)");
         ps.setString(1, userData.username);
         ps.setInt(2, VirtualGiftReceivedData.PurchaseLocationEnum.MARKETING_REWARD.value());
         ps.setInt(3, storeItemData.referenceID);
         ps.setString(4, SystemProperty.get("VirtualGiftRewardUser", "migme"));
         ps.setString(5, "Congratulations for completing reward program \"" + programName + "\"");
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to reward virtual gift ID " + storeItemData.referenceID + " to " + userData.username + " for rewardProgramId:[" + programId + "]");
         }

         if (SystemProperty.getBool("UseRedisDataStore", true)) {
            if (userData != null && userData.userID != null) {
               GiftsReceivedCounter.incrementCacheCount(userData.userID);
            } else {
               log.error("Unable to increment gifts received counter because recipient User ID was null for username: " + userData.username);
            }
         } else {
            MemCachedClientWrapper.incr(MemCachedKeySpaces.CommonKeySpace.NUM_VIRTUAL_GIFTS_RECEIVED, userData.username);
         }

         rewardedStoreItems.add(new RewardedStoreItemData(storeItemData));
         log.info("giveVirtualGiftReward(rewardProgramId:[" + programId + "] Virtual gift ID " + storeItemData.referenceID + " rewarded to " + userData.username);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

   }

   private void giveAvatarReward(int programId, UserData userData, StoreItemData storeItemData, long completionID, Collection<RewardedStoreItemData> rewardedStoreItems) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("INSERT INTO avataruseritem (userid, avataritemid, used) SELECT ?, ?, 0 FROM DUAL WHERE NOT EXISTS (SELECT * FROM avataruseritem WHERE userid = ? and avataritemid = ?)");
         ps.setInt(1, userData.userID);
         ps.setInt(2, storeItemData.referenceID);
         ps.setInt(3, userData.userID);
         ps.setInt(4, storeItemData.referenceID);
         if (ps.executeUpdate() == 1) {
            rewardedStoreItems.add(new RewardedStoreItemData(storeItemData));
            log.info("giveAvatarReward(rewardProgramId:[" + programId + "] Avatar ID " + storeItemData.referenceID + " rewarded to " + userData.username);
         } else {
            log.info("giveAvatarReward(rewardProgramId:[" + programId + "] Avatar ID " + storeItemData.referenceID + " already owned by " + userData.username);
         }
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

   }

   private void giveStickerOrEmoticonPackReward(int programId, UserData userData, StoreItemData storeItemData, long completionID, Collection<RewardedStoreItemData> rewardedStoreItems) throws SQLException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      String insertEmoticonPackOwnerSQL;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.IGNORE_ON_ALREADY_OWNED_STICKER_OR_EMOTICON_PACK)) {
         insertEmoticonPackOwnerSQL = "INSERT INTO emoticonpackowner (username, emoticonpackid) SELECT ?, ? FROM DUAL WHERE NOT EXISTS (SELECT * FROM EMOTICONPACKOWNER WHERE username = ? and emoticonpackid= ?)";
      } else {
         insertEmoticonPackOwnerSQL = "INSERT INTO emoticonpackowner (username, emoticonpackid) VALUES (?, ?)";
      }

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement(insertEmoticonPackOwnerSQL);
         ps.setString(1, userData.username);
         ps.setInt(2, storeItemData.referenceID);
         ps.setString(3, userData.username);
         ps.setInt(4, storeItemData.referenceID);
         if (ps.executeUpdate() == 1) {
            rewardedStoreItems.add(new RewardedStoreItemData(storeItemData));
            log.info("giveStickerOrEmoticonPackReward(rewardProgramId:[" + programId + "] Emoticon pack ID " + storeItemData.referenceID + " rewarded to " + userData.username);
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.EMOTICON_PACKS_OWNED, userData.username);
            UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(userData.username);
            if (userPrx != null) {
               userPrx.emoticonPackActivated(storeItemData.referenceID);
            }

            return;
         }

         log.info("giveStickerOrEmoticonPackReward(rewardProgramId:[" + programId + "] Emoticon pack ID " + storeItemData.referenceID + " already owned by " + userData.username);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

   }

   private void giveGroupMembershipRewards(RewardProgramData programData, UserData userData, long completionID, AccountEntrySourceData accountEntrySourceData, Collection<RewardedGroupMembershipData> rewardedGroupMemberships) throws CreateException, EJBException, UnknownHostException, SQLException {
      this.giveGroupMembershipRewards((Collection)programData.groupMembershipRewards, userData, completionID, accountEntrySourceData, rewardedGroupMemberships);
   }

   private void giveGroupMembershipRewards(Collection<Integer> groupIds, UserData userData, long completionID, AccountEntrySourceData accountEntrySourceData, Collection<RewardedGroupMembershipData> rewardedGroupMemberships) throws CreateException, EJBException, UnknownHostException, SQLException {
      if (groupIds != null && groupIds.size() > 0) {
         this.logGroupMembershipRewarded(groupIds, userData, completionID);
         Iterator i$ = groupIds.iterator();

         while(i$.hasNext()) {
            Integer groupID = (Integer)i$.next();
            WebLocal webBean = (WebLocal)EJBHomeCache.getLocalObject("WebLocal", WebLocalHome.class);
            webBean.joinGroupWithoutValidation(userData, groupID, 0, accountEntrySourceData.ipAddress, accountEntrySourceData.sessionID, accountEntrySourceData.mobileDevice, accountEntrySourceData.userAgent, false, true, true, false, false, false);
            rewardedGroupMemberships.add(new RewardedGroupMembershipData(groupID));
            log.info("Membership to group ID " + groupID + " rewarded to " + userData.username);
         }
      }

   }

   private void giveBadgeRewards(RewardProgramData programData, UserData userData, long completionID, Collection<RewardedBadgeData> rewardedBadges) throws CreateException, SQLException, MigboApiUtil.MigboApiException {
      this.giveBadgeRewards(programData.id, programData.badgeRewards, userData, completionID, rewardedBadges);
   }

   private void giveBadgeRewards(int programId, Collection<Integer> badgeIds, UserData userData, long completionID, Collection<RewardedBadgeData> rewardedBadges) throws CreateException, SQLException, MigboApiUtil.MigboApiException {
      if (badgeIds != null && badgeIds.size() > 0) {
         this.logBadgeRewarded(badgeIds, userData, completionID);
         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         long curTime = System.currentTimeMillis();
         String pathPrefix = String.format("/user/%d/badge", userData.userID);
         Iterator i$ = badgeIds.iterator();

         while(i$.hasNext()) {
            Integer badgeID = (Integer)i$.next();
            if (apiUtil.postAndCheckOk(pathPrefix, String.format("{\"badgeId\":%d,\"unlockedTimestamp\":%d}", badgeID, curTime))) {
               log.info("programId [" + programId + "]: badge ID [" + badgeID + "] rewarded to " + userData.username);
               rewardedBadges.add(new RewardedBadgeData(badgeID));

               try {
                  UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  if (unsProxy != null) {
                     Map<String, String> parameters = new HashMap();
                     parameters.put("badgeId", Integer.toString(badgeID));
                     parameters.put("unlockedTimestamp", Long.toString(curTime));
                     String key = Integer.toString(badgeID);
                     unsProxy.notifyFusionUser(new Message(key, userData.userID, userData.username, Enums.NotificationTypeEnum.NEW_BADGE_ALERT.getType(), curTime, parameters));
                  } else {
                     log.error(String.format("Unable to find UserNotificationServicePrx to push badge alert notification for user [%s], badgeId %d", userData.username, badgeID));
                  }
               } catch (Exception var16) {
                  log.error("programId [" + programId + "]:Failed to push badge alert notification for user [" + userData.username + "]", var16);
               }
            } else {
               log.error("programId [" + programId + "]:Failed to reward badge ID [" + badgeID + "] to " + userData.username);
            }
         }
      }

   }

   private void lockGiveScoreReward(String username) {
      String lockID = "giveScoreReward/" + username;
      MemCachedDistributedLock.getDistributedLock(lockID);
   }

   private void releaseGiveScoreReward(String username) {
      String lockID = "giveScoreReward/" + username;
      MemCachedDistributedLock.releaseDistributedLock(lockID);
   }

   private void giveScoreReward(Integer programID, Integer scoreReward, UserData userData, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se218Enabled.getValue()) {
         this.giveScoreRewardUpdateScoreAndGetLevelChanges(programID, scoreReward, userData, completionID, rewardedReputations);
      } else {
         this.giveScoreRewardOld(programID, scoreReward, userData, completionID, rewardedReputations);
      }

   }

   /** @deprecated */
   @Deprecated
   private void giveScoreRewardOld(Integer programID, Integer scoreReward, UserData userData, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      if (scoreReward != null && scoreReward > 0) {
         Connection masterConn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;
         String username = userData.username;

         try {
            this.lockGiveScoreReward(username);
            masterConn = this.dataSourceMaster.getConnection();
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserReputationScoreAndLevelData currentScoreData = userBean.getReputationScoreAndLevel(userData.userID, (Connection)null);
            userBean.updateReputationScore(userData.userID, scoreReward, true);
            int newScore = currentScoreData.score + scoreReward;
            int oldLevel = currentScoreData.level;
            int oldScore = currentScoreData.score;
            int newLevel = oldLevel;

            try {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.MIGLEVEL_INCREASE_PROCESSING_ENABLED)) {
                  MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                  ReputationLevelData repuDataNext = MemCacheOrEJB.getReputationLevelDataForLevel(oldLevel + 1);
                  if (log.isDebugEnabled()) {
                     log.debug("current user score: " + oldScore + " score award increase: " + scoreReward + " current level: " + oldLevel);
                     log.debug("next repu level score: " + repuDataNext.score);
                     if (newScore >= repuDataNext.score) {
                        log.debug("miglevel increase detected for userID: " + userData.userID + " " + oldScore + "+" + scoreReward + " >= " + repuDataNext.score);
                     } else {
                        log.debug("miglevel increase not detected for userID: " + userData.userID + " " + oldScore + "+" + scoreReward + " < " + repuDataNext.score);
                     }
                  }

                  if (newScore >= repuDataNext.score) {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.LOG_SCORE_REWARDINGS_THAT_CAUSE_LEVEL_UPS)) {
                        log.info("ScoreRewarded (with level up) for UserID:[" + userData.userID + "]" + "ProgramID=[" + programID + "]," + "OldLevel=[" + oldLevel + "]," + "OldScore=[" + oldScore + "]," + "NewScore=[" + newScore + "]," + "MinScoreAchievedForLevelup=[" + repuDataNext.level + "]" + "ScoreReward=[" + scoreReward + "]");
                     }

                     newLevel = repuDataNext.level;
                     ReputationLevelData repuDataCurrent = MemCacheOrEJB.getReputationLevelDataForLevel(oldLevel);
                     if (repuDataNext.chatRoomSize > repuDataCurrent.chatRoomSize) {
                        this.updateUserOwnedRoomSize(userData.username, repuDataNext.chatRoomSize, masterConn);
                     }
                  } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.LOG_SCORE_REWARDINGS_THAT_DO_NOT_CAUSE_LEVEL_UPS)) {
                     log.info("ScoreRewarded (without level up) for UserID:[" + userData.userID + "]" + "ProgramID=[" + programID + "]," + "OldLevel=[" + oldLevel + "]," + "OldScore=[" + oldScore + "]," + "NewScore=[" + newScore + "]," + "ScoreReward=[" + scoreReward + "]");
                  }
               }
            } catch (Exception var35) {
               log.error("Score reward successful but unable to perform miglevel update for user [" + userData.username + "]." + var35, var35);
            }

            RewardedReputationData rewardedReputation = new RewardedReputationData(RewardedReputationData.ReputationRewardSourceEnum.GIVE_SCORE_REWARD, oldScore, oldLevel, newScore, newLevel);
            rewardedReputations.add(rewardedReputation);
         } catch (Exception var36) {
            log.error("Unable to create user bean to give score reward " + scoreReward + " for " + userData.username + "." + var36, var36);
            throw var36;
         } finally {
            this.releaseGiveScoreReward(username);

            try {
               if (rs != null) {
                  ((ResultSet)rs).close();
               }
            } catch (SQLException var34) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ((PreparedStatement)ps).close();
               }
            } catch (SQLException var33) {
               ps = null;
            }

            try {
               if (masterConn != null) {
                  masterConn.close();
               }
            } catch (SQLException var32) {
               masterConn = null;
            }

         }
      }

   }

   private void giveScoreRewardUpdateScoreAndGetLevelChanges(Integer programID, Integer scoreReward, UserData userData, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      if (scoreReward != null && scoreReward > 0) {
         String username = userData.username;

         try {
            this.lockGiveScoreReward(username);
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserReputationScoreAndLevelData repuScoreAndLvlAfterReward = userBean.updateReputationScoreAndGet(userData.userID, scoreReward, true);
            int scoreAfterReward = repuScoreAndLvlAfterReward.score;
            int levelAfterReward = repuScoreAndLvlAfterReward.level;
            int scoreBeforeReward = scoreAfterReward - scoreReward;
            ReputationLevelData levelDataAfterReward = MemCacheOrEJB.getReputationLevelDataForLevel(levelAfterReward);
            boolean userHasLevelUp = levelDataAfterReward.score > scoreBeforeReward;
            int levelBeforeReward;
            if (userHasLevelUp) {
               ReputationLevelScoreRanges.LevelScoreRangeEntry levelDataBeforeReward = ReputationLevelScoreRanges.getInstance().getLevelScoreRange(scoreBeforeReward, (ConnectionCreator)(new ConnectionCreator.FromDataSource(this.dataSourceSlave)));
               levelBeforeReward = levelDataBeforeReward == null ? 1 : levelDataBeforeReward.level;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.LOG_SCORE_REWARDINGS_THAT_CAUSE_LEVEL_UPS)) {
                  if ((Boolean)SystemPropertyEntities.Temp.Cache.se599FixEnabled.getValue()) {
                     log.info("ScoreRewarded (with level up) for UserID:[" + userData.userID + "]" + "ProgramID=[" + programID + "]," + "OldLevel=[" + levelBeforeReward + "]," + "OldScore=[" + scoreBeforeReward + "]," + "NewLevel=[" + repuScoreAndLvlAfterReward.level + "]," + "NewScore=[" + scoreAfterReward + "]," + "MinScoreForNewLevel=[" + levelDataAfterReward.score + "]" + "ScoreRewarded=[" + scoreReward + "]");
                  } else {
                     log.info("ScoreRewarded (with level up) for UserID:[" + userData.userID + "]" + "ProgramID=[" + programID + "]," + "OldLevel=[" + levelBeforeReward + "]," + "OldScore=[" + scoreBeforeReward + "]," + "NewLevel=[" + repuScoreAndLvlAfterReward.level + "]," + "NewScore=[" + scoreAfterReward + "]," + "MinScoreAchievedForLevelup=[" + repuScoreAndLvlAfterReward.score + "]" + "ScoreRewarded=[" + scoreReward + "]");
                  }
               }
            } else {
               levelBeforeReward = levelAfterReward;
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.LOG_SCORE_REWARDINGS_THAT_DO_NOT_CAUSE_LEVEL_UPS)) {
                  log.info("ScoreRewarded (without level up) for UserID:[" + userData.userID + "]" + "ProgramID=[" + programID + "]," + "level=[" + levelAfterReward + "]," + "OldScore=[" + scoreBeforeReward + "]," + "NewScore=[" + scoreAfterReward + "]," + "ScoreRewarded=[" + scoreReward + "]");
               }
            }

            RewardedReputationData rewardedReputation = new RewardedReputationData(RewardedReputationData.ReputationRewardSourceEnum.GIVE_SCORE_REWARD, scoreBeforeReward, levelBeforeReward, scoreAfterReward, levelAfterReward);
            rewardedReputations.add(rewardedReputation);
         } catch (Exception var21) {
            log.error("Failed to give score reward [" + scoreReward + "] for [" + username + "].Exception:" + var21, var21);
            throw var21;
         } finally {
            this.releaseGiveScoreReward(username);
         }
      }

   }

   private void giveMerchantRewardPoints(RewardProgramData programData, UserData userData, long completionID) {
      int merchantRewardPoints = programData.getMerchantRewardPoints();
      this.giveMerchantRewardPoints(programData.id, userData, merchantRewardPoints, completionID);
   }

   private void giveMerchantRewardPoints(int programId, UserData userData, int merchantRewardPoints, long completionID) {
      if (merchantRewardPoints != 0) {
         try {
            MerchantsLocal merchantsEJB = (MerchantsLocal)EJBHomeCache.getLocalObject("MerchantsLocal", MerchantsLocalHome.class);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            MerchantPointsLogData storedMerchantPointsLogData = merchantsEJB.insertMerchantPoints(new MerchantPointsLogData(MerchantPointsLogData.EntryTypeEnum.MECHANIC_REWARD, now, userData.userID, merchantRewardPoints));
            this.logMerchantPointsRewarded(now, completionID, storedMerchantPointsLogData.getId());
         } catch (Exception var9) {
            log.error("giveMerchantRewardPoints for programId:[" + programId + "] userId:[" + userData.userID + "] failed.Exception:" + var9, var9);
         }
      }

   }

   private void logMerchantPointsRewarded(Timestamp dateCreated, long completionID, long merchantpointslogID) throws SQLException {
      Connection conn = this.dataSourceMaster.getConnection();

      try {
         String sql = "insert into merchantpointsrewarded(dateCreated,rewardprogramcompletedid,merchantpointslogID) values(?,?,?)";
         PreparedStatement ps = conn.prepareStatement("insert into merchantpointsrewarded(dateCreated,rewardprogramcompletedid,merchantpointslogID) values(?,?,?)");

         try {
            ps.setTimestamp(1, dateCreated);
            ps.setLong(2, completionID);
            ps.setLong(3, merchantpointslogID);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to record  merchant points rewarded");
            }
         } finally {
            ps.close();
         }
      } finally {
         conn.close();
      }

   }

   private int getMaxMigLevel(Connection conn) throws SQLException {
      PreparedStatement ps = conn.prepareStatement("select max(level) maxlevel from reputationscoretolevel");

      int var4;
      try {
         ResultSet rs = ps.executeQuery();

         try {
            if (!rs.next()) {
               throw new EJBException("Unable to determine the max level");
            }

            var4 = rs.getInt("maxlevel");
         } finally {
            rs.close();
         }
      } finally {
         ps.close();
      }

      return var4;
   }

   private void giveLevelReward(int rewardProgramID, UserData userData, Integer levelReward, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      if ((Boolean)SystemPropertyEntities.Temp.Cache.se218Enabled.getValue()) {
         this.giveLevelRewardWithDeferredNotification(rewardProgramID, userData, levelReward, completionID, rewardedReputations);
      } else {
         this.giveLevelRewardOld(rewardProgramID, userData, levelReward, completionID, rewardedReputations);
      }

   }

   /** @deprecated */
   @Deprecated
   private void giveLevelRewardOld(int rewardProgramID, UserData userData, Integer levelReward, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      if (levelReward != null && levelReward > 0) {
         Connection connMaster = null;
         String username = userData.username;
         this.lockGiveScoreReward(username);

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            ReputationLevelData repuDataCurrent = userBean.getReputationLevelByUserid(userData.userID);
            connMaster = this.dataSourceMaster.getConnection();
            int maxLevel = this.getMaxMigLevel(connMaster);
            if (maxLevel == repuDataCurrent.level) {
               log.error("giveLevelReward for: user[" + userData.username + "]" + " for program:[" + rewardProgramID + "]" + " failed since user has already reached the max level:" + repuDataCurrent.level);
            } else {
               int diffToMaxLevel = maxLevel - repuDataCurrent.level;
               int allowedLevelUp = diffToMaxLevel > levelReward ? levelReward : diffToMaxLevel;
               int nextLevel = repuDataCurrent.level + allowedLevelUp;
               ReputationLevelData repuDataNext = MemCacheOrEJB.getReputationLevelDataForLevel(nextLevel);
               if (null == repuDataNext) {
                  throw new EJBException("Unable to determine an appropriate levelup for user" + userData.username + "for rewardprogram " + rewardProgramID + " nextLevel = " + nextLevel);
               }

               UserReputationScoreAndLevelData currentScoreData = userBean.getReputationScoreAndLevel(userData.userID, (Connection)null);
               Integer newScore = repuDataNext.score;
               Integer newChatRoomSize = repuDataNext.chatRoomSize;
               userBean.updateReputationScore(userData.userID, newScore, false);
               if (newChatRoomSize > repuDataCurrent.chatRoomSize) {
                  this.updateUserOwnedRoomSize(userData.username, newChatRoomSize, connMaster);
               }

               RewardedReputationData rewardedReputationData = new RewardedReputationData(RewardedReputationData.ReputationRewardSourceEnum.GIVE_LEVEL_REWARD, currentScoreData.score, currentScoreData.level, newScore, repuDataNext.level);
               rewardedReputations.add(rewardedReputationData);
               log.info("Reward Program :[" + rewardProgramID + "] (allowed) level up reward [" + allowedLevelUp + "] mig level(s) rewarded to " + userData.username + ". New Mig Level:[" + nextLevel + "]");
            }
         } catch (Exception var29) {
            log.error("giveLevelReward for:[" + userData.username + "]" + " for program:[" + rewardProgramID + "]" + " failed with exception:" + var29.getMessage(), var29);
            throw var29;
         } finally {
            this.releaseGiveScoreReward(username);

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var28) {
               connMaster = null;
            }

         }
      }

   }

   private void giveLevelRewardWithDeferredNotification(int rewardProgramID, UserData userData, Integer levelReward, long completionID, Collection<RewardedReputationData> rewardedReputations) throws Exception {
      if (levelReward != null && levelReward > 0) {
         String username = userData.username;
         this.lockGiveScoreReward(username);

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserReputationScoreAndLevelData repuScoreAndLevelCurrent = userBean.getReputationScoreAndLevel(false, userData.userID, true);
            Connection conn = this.dataSourceSlave.getConnection();

            int maxLevel;
            try {
               maxLevel = this.getMaxMigLevel(conn);
            } finally {
               conn.close();
            }

            if (maxLevel == repuScoreAndLevelCurrent.level) {
               log.error("giveLevelReward for: user[" + userData.username + "]" + " for program:[" + rewardProgramID + "]" + " failed since user has already reached the max level:" + repuScoreAndLevelCurrent.level);
            } else {
               int diffToMaxLevel = maxLevel - repuScoreAndLevelCurrent.level;
               int allowedLevelUp = diffToMaxLevel > levelReward ? levelReward : diffToMaxLevel;
               int nextLevel = repuScoreAndLevelCurrent.level + allowedLevelUp;
               ReputationLevelData repuDataNext = MemCacheOrEJB.getReputationLevelDataForLevel(nextLevel);
               if (null == repuDataNext) {
                  throw new EJBException("Unable to determine an appropriate levelup for user" + userData.username + "for rewardprogram " + rewardProgramID + " nextLevel = " + nextLevel);
               }

               Integer newScore = repuDataNext.score;
               userBean.updateReputationScore(userData.userID, newScore, false);
               RewardedReputationData rewardedReputationData = new RewardedReputationData(RewardedReputationData.ReputationRewardSourceEnum.GIVE_LEVEL_REWARD, repuScoreAndLevelCurrent.score, repuScoreAndLevelCurrent.level, newScore, repuDataNext.level);
               rewardedReputations.add(rewardedReputationData);
               log.info("Reward Program :[" + rewardProgramID + "] (allowed) level up reward [" + allowedLevelUp + "] mig level(s) rewarded to " + userData.username + ". New Mig Level:[" + nextLevel + "]");
            }
         } catch (Exception var27) {
            log.error("giveLevelReward for:[" + userData.username + "]" + " for program:[" + rewardProgramID + "]" + " failed with exception:" + var27, var27);
            throw var27;
         } finally {
            this.releaseGiveScoreReward(username);
         }
      }

   }

   private void triggerMiglevelIncreaseNotification(UserData userData, int nextLevel) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("triggering mig level increase notification for [" + userData.userID + "] for level [" + nextLevel + "]");
         }

         UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
         Map<String, String> parameters = new HashMap();
         parameters.put("miglevel", Integer.toString(nextLevel));
         unsProxy.notifyFusionUser(new Message(Integer.toString(nextLevel), userData.userID, userData.username, Enums.NotificationTypeEnum.MIGLEVEL_INCREASE_ALERT.getType(), System.currentTimeMillis(), parameters));
      } catch (Exception var5) {
         log.error("Unknown error while contacting UNS to generate miglevel increase alert:" + var5.getMessage());
      }

   }

   public void updateUserOwnedChatRoomSizes(String username, int chatRoomSize) {
      try {
         Connection masterConn = this.dataSourceMaster.getConnection();

         try {
            this.updateUserOwnedRoomSize(username, chatRoomSize, masterConn);
         } finally {
            masterConn.close();
         }

      } catch (SQLException var9) {
         throw new EJBException("Failed to update chat room sizes owned by user [" + username + "] to [" + chatRoomSize + "]");
      }
   }

   private void updateUserOwnedRoomSize(String username, int chatRoomSize, Connection connection) {
      boolean useOneWayProxy = (Boolean)SystemPropertyEntities.Temp.Cache.se639OneWayPrxEnabled.getValue();
      PreparedStatement psGetChatRooms = null;
      PreparedStatement psUpdateChatRoomSize = null;
      ResultSet rs = null;

      try {
         psGetChatRooms = connection.prepareStatement("select name from chatroom where creator = ? and type = 1 and userowned = 1");
         psUpdateChatRoomSize = connection.prepareStatement("update chatroom set maximumsize = ? where name = ?");
         psGetChatRooms.setString(1, username);
         rs = psGetChatRooms.executeQuery();

         while(rs.next()) {
            String chatRoomName = null;

            try {
               chatRoomName = rs.getString("name");
               psUpdateChatRoomSize.setInt(1, chatRoomSize);
               psUpdateChatRoomSize.setString(2, chatRoomName);
               psUpdateChatRoomSize.executeUpdate();
               ChatRoomUtils.invalidateChatRoomCache(chatRoomName);
               ChatRoomPrx chatRoomPrx = useOneWayProxy ? ChatRoomPrxHelper.uncheckedCast(EJBIcePrxFinder.findChatRoomPrx(chatRoomName).ice_oneway()) : EJBIcePrxFinder.findChatRoomPrx(chatRoomName);
               if (chatRoomPrx != null) {
                  if (log.isDebugEnabled()) {
                     String debugMsg = "Notifying a change in room [" + chatRoomName + "] maximum size to [" + chatRoomSize + "]";
                     log.debug(debugMsg + (useOneWayProxy ? " with one way proxy." : " with synchronous proxy"));
                  }

                  chatRoomPrx.setMaximumSize(chatRoomSize);
               }
            } catch (Exception var29) {
               log.error("failed to update chat room size for user [" + username + "], room [" + chatRoomName + "] new size [" + chatRoomSize + "]", var29);
            }
         }
      } catch (Exception var30) {
         log.error("failed to update chat room size for user [" + username + "]", var30);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var28) {
            rs = null;
         }

         try {
            if (psUpdateChatRoomSize != null) {
               psUpdateChatRoomSize.close();
            }
         } catch (SQLException var27) {
            psUpdateChatRoomSize = null;
         }

         try {
            if (psGetChatRooms != null) {
               psGetChatRooms.close();
            }
         } catch (SQLException var26) {
            psGetChatRooms = null;
         }

      }

   }

   private AccountEntryData giveMigCreditReward(RewardProgramData programData, UserData userData, long completionID, AccountEntrySourceData accountEntrySourceData, Collection<RewardedMigCreditData> rewardedMigCredits) throws CreateException {
      return programData.migCreditReward != null && programData.migCreditReward > 0.0D ? this.giveMigCreditReward(accountEntrySourceData, completionID, userData.username, this.getDefaultAccountEntryDescription(programData), programData.migCreditReward, programData.migCreditRewardCurrency, rewardedMigCredits) : null;
   }

   public String getDefaultAccountEntryDescription(RewardProgramData programData) {
      return "Completion of market program " + programData.name;
   }

   private AccountEntryData giveMigCreditReward(AccountEntrySourceData accountEntrySourceData, long completionID, String username, String remarks, Double migCreditReward, String migCreditRewardCurrency, Collection<RewardedMigCreditData> rewardedMigCredits) throws CreateException {
      if (migCreditReward != null && migCreditReward > 0.0D) {
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData accountEntryData = accountBean.giveMarketingReward(username, String.valueOf(completionID), StringUtil.truncateWithEllipsis(remarks, this.MAX_LENGTH_ACCOUNTENTRY_DESC), migCreditReward, migCreditRewardCurrency, accountEntrySourceData);
         rewardedMigCredits.add(new RewardedMigCreditData(migCreditReward, migCreditRewardCurrency));
         log.info(migCreditReward + " " + migCreditRewardCurrency + " mig credit rewarded to " + username);
         return accountEntryData;
      } else {
         return null;
      }
   }

   private void sendIMNotification(RewardProgramData programData, UserData userData, long completionID) {
      this.sendIMNotification(programData.id, userData.username, programData.imNotification);
   }

   private void sendIMNotification(int rewardprogramid, String username, String imNotification) {
      if (imNotification != null && !StringUtil.isBlank(imNotification)) {
         UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
         if (userPrx != null) {
            try {
               int imnotificationMaxLength = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.IM_NOTIFICATION_MAX_MESSAGE_LENGTH);
               String imMessage = StringUtil.truncateWithEllipsis(imNotification, imnotificationMaxLength);
               if (log.isDebugEnabled()) {
                  log.debug("Sending IMNotification to [" + username + "].Content:[" + imMessage + "]");
               }

               userPrx.putAlertMessage(imMessage, (String)null, (short)0);
               log.info("IM notification sent to " + username);
            } catch (Exception var7) {
               log.warn("Unable to send IM reward notification to [" + username + "] for reward program + [" + imNotification + "].Exception:" + var7, var7);
            }
         } else if (log.isDebugEnabled()) {
            log.debug("Unable to send IM reward notification to [" + username + "] for reward program + [" + imNotification + "]. User object proxy [" + username + "] not found.");
         }
      }

   }

   private void sendSMSNotification(RewardProgramData programData, UserData userData, long completionID) {
      this.sendSMSNotification(programData.id, programData.smsNotification, userData, completionID);
   }

   private void sendSMSNotification(int programDataId, String smsNotification, UserData userData, long completionID) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.MARKETING_REWARD_NOTIFICATION, userData.username)) {
         if (smsNotification != null && smsNotification.length() > 0 && userData.mobilePhone != null && userData.mobileVerified) {
            try {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.username = userData.username;
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.MARKETING_REWARD_NOTIFICATION;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = userData.mobilePhone;
               systemSMSData.messageText = smsNotification;
               MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               messageBean.sendSystemSMS(systemSMSData, new AccountEntrySourceData(ContentBean.class));
               log.info("SMS notification sent to " + userData.username);
            } catch (Exception var8) {
               log.warn("Unable to SMS reward notification to [" + userData.username + "] for reward program + [" + programDataId + "] Exception:[" + var8 + "]", var8);
            }
         }

      }
   }

   private void sendTemplatizedEmailNotification(int userId, Integer emailTemplateID, Map<String, String> templateData) {
      int userEmailAddressTypeCode = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DEFAULT_USER_EMAIL_ADDRESS_TYPE_FOR_EMAIL_NOTIFICATION);
      UserEmailAddressData.UserEmailAddressTypeEnum userEmailAddressTypeEnum;
      if (userEmailAddressTypeCode < 0) {
         userEmailAddressTypeEnum = null;
      } else {
         userEmailAddressTypeEnum = UserEmailAddressData.UserEmailAddressTypeEnum.fromValue(userEmailAddressTypeCode);
      }

      try {
         if (userEmailAddressTypeEnum != null) {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW519_EMAIL_NOTIFICATION_USER_SETTINGS_ENABLED) && !isPassThroughEmailTemplate(emailTemplateID)) {
               String username = userBean.getUsernameByUserid(userId, (Connection)null);
               UserSettingData.EmailSettingEnum emailAllSetting = userBean.getEmailNotificationSetting(username, UserSettingData.TypeEnum.EMAIL_ALL);
               if (UserSettingData.EmailSettingEnum.DISABLED == emailAllSetting) {
                  if (log.isDebugEnabled()) {
                     log.debug("User[" + userId + "] emailAllSetting is off, cancelling email sending");
                  }

                  return;
               }
            }

            UserEmailAddressData userEmailAddressData = userBean.getUserEmailAddressByType(userId, userEmailAddressTypeEnum);
            if (userEmailAddressData != null) {
               if (userEmailAddressData.verified) {
                  if (log.isDebugEnabled()) {
                     log.debug("Notifying userid[" + userId + "] emailtemplateID:[" + emailTemplateID + "]. Template Data:[" + templateData + "]");
                  }

                  EJBIcePrxFinder.getUserNotificationServiceProxy().sendTemplatizedEmailFromNoReply(userEmailAddressData.emailAddress, emailTemplateID, templateData);
               } else {
                  log.warn("Email address unverified. Unable to send templatized emails for user address type [" + userEmailAddressTypeEnum + "] userid [" + userId + "]");
               }
            } else {
               log.warn("Email address not found. Unable to send templatized emails for user address type [" + userEmailAddressTypeEnum + "] userid [" + userId + "]");
            }
         } else {
            log.error("Invalid default email address type configuration.Unable to send templatized emails for user address type [" + userEmailAddressTypeCode + "] userid [" + userId + "]");
         }
      } catch (Exception var10) {
         log.error("Error sending notification.Unable to send templatized emails for user address type [" + userEmailAddressTypeEnum + "] userid [" + userId + "]", var10);
      }

   }

   private void sendStaticContentEmailNotification(RewardProgramData programData, UserData userData, long completionID) {
      this.sendStaticContentEmailNotification(programData.id, "Reward notification", programData.emailNotification, userData);
   }

   private void sendStaticContentEmailNotification(int programId, String subject, String emailNotification, UserData userData) {
      if (emailNotification != null && emailNotification.length() > 0) {
         try {
            MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            UserEmailAddressData.UserEmailAddressTypeEnum userEmailAddressTypeEnum = null;
            int userEmailAddressTypeCode = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DEFAULT_USER_EMAIL_ADDRESS_TYPE_FOR_EMAIL_NOTIFICATION);
            if (userEmailAddressTypeCode < 0) {
               userEmailAddressTypeEnum = null;
            } else {
               userEmailAddressTypeEnum = UserEmailAddressData.UserEmailAddressTypeEnum.fromValue(userEmailAddressTypeCode);
            }

            messageBean.sendSystemEmail(userData.username, StringUtil.isBlank(subject) ? "Reward notification" : subject, emailNotification, userEmailAddressTypeEnum);
            log.info("ProgramID:[" + programId + "] Email notification sent to [" + userData.username + "] using userEmailAddressType:[" + userEmailAddressTypeEnum + "]");
         } catch (Exception var8) {
            log.warn("Unable to Email reward notification to [" + userData.username + "] for reward program + [" + programId + "]", var8);
         }
      }

   }

   public boolean buyPaidEmote(String username, PaidEmoteData paidEmoteData, int purchaseLocation, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var10;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.BUY_PAIDEMOTE, userBean.getUserAuthenticatedAccessControlParameter(username)) && SystemProperty.getBool("StoreItemPurchaseDisabledForUnauthenticatedUsers", false)) {
            throw new EJBException("You must be authenticated before you can use this emote.");
         }

         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         conn = this.dataSourceMaster.getConnection();
         if (accountBean.userCanAffordPaidEmote(username, paidEmoteData, conn)) {
            ps = conn.prepareStatement("insert into paidemotesent (username, datecreated, purchaselocation, emoteid) values (?, now(), ?, ?)", 1);
            ps.setString(1, username);
            ps.setInt(2, purchaseLocation);
            ps.setLong(3, paidEmoteData.getId());
            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated != 1) {
               throw new EJBException("Internal Server Error (Unable to record emote purchase)");
            }

            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Internal Server Error (Unable to record emote purchase)");
            }

            int paidEmoteSentID = rs.getInt(1);
            AccountEntryData accountEntry = new AccountEntryData();
            accountEntry.reference = Integer.toString(paidEmoteSentID);
            accountEntry.username = username;
            accountEntry.type = AccountEntryData.TypeEnum.EMOTE_PURCHASE;
            accountEntry.description = "Purchased the emote " + paidEmoteData.getDescription();
            accountEntry.currency = paidEmoteData.getCurrency();
            accountEntry.amount = -paidEmoteData.getPrice();
            accountEntry.tax = 0.0D;
            accountBean.createAccountEntry(conn, accountEntry, accountEntrySourceData);
            boolean var13 = true;
            return var13;
         }

         var10 = false;
      } catch (SQLException var32) {
         throw new EJBException(var32.getMessage());
      } catch (CreateException var33) {
         throw new EJBException(var33.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var29) {
            conn = null;
         }

      }

      return var10;
   }

   public Vector<ItemData> getPaintWarsSpecialItems() {
      Vector<ItemData> specialItems = (Vector)MemCachedClientWrapper.get(MemCachedKeySpaces.CommonKeySpace.PAINT_WARS_ITEMS, MemCachedKeySpaces.CommonKeySpace.PAINT_WARS_ITEMS.name());
      if (specialItems == null) {
         try {
            specialItems = new Vector();
            Connection conn = this.dataSourceSlave.getConnection();
            String sql = "SELECT * FROM paintwarsitem";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while(rs.next()) {
               try {
                  ItemData item = new ItemData();
                  item.setId(rs.getInt("ID"));
                  item.setName(rs.getString("Name"));
                  item.setDescription(rs.getString("Description"));
                  item.setCurrency(rs.getString("Currency"));
                  item.setPrice(rs.getDouble("Price"));
                  specialItems.add(item);
               } catch (SQLException var7) {
                  log.error(var7.getMessage());
               }
            }

            MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.PAINT_WARS_ITEMS, MemCachedKeySpaces.CommonKeySpace.PAINT_WARS_ITEMS.name(), specialItems);
         } catch (SQLException var8) {
            log.error(var8.getMessage());
         }
      }

      return specialItems;
   }

   public String createMigboTextPostForUser(int userID, String text, String rootPost, String parentPost, String originality, ClientType deviceEnum, SSOEnums.View ssoView) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.ENABLE_NEW_IMPLEMENTATION_FOR_CREATE_MINI_BLOG_TEXT)) {
         try {
            MigboEnums.PostApplicationEnum migboApplicationTypeEnum = MigboEnums.PostApplicationEnum.fromDeviceType(deviceEnum);
            if (migboApplicationTypeEnum == null) {
               migboApplicationTypeEnum = MigboEnums.PostApplicationEnum.fromSSOView(ssoView);
               if (migboApplicationTypeEnum == null) {
                  migboApplicationTypeEnum = MigboEnums.PostApplicationEnum.J2ME;
               }
            }

            log.info(String.format("creating migbo post,  userid %d, body '%s', root %s, parent %s, ori %s, device %s, view %s, app %s", userID, text, rootPost, parentPost, originality, deviceEnum, ssoView, migboApplicationTypeEnum));
            JSONObject result = this.createMigboTextPost(userID, text, rootPost, parentPost, originality, migboApplicationTypeEnum);
            if (result != null) {
               return result.toString();
            }
         } catch (Exception var10) {
            log.warn(String.format("Unable to create post for userid[%d] text[%s] :%s", userID, text, var10.getMessage()), var10);
            throw new EJBException(var10.getMessage());
         }

         return (new JSONObject()).toString();
      } else {
         return this.createMigboTextPostForUserOldImplementation(userID, text, rootPost, parentPost, originality, deviceEnum, ssoView);
      }
   }

   /** @deprecated */
   @Deprecated
   private String createMigboTextPostForUserOldImplementation(int userID, String text, String rootPost, String parentPost, String originality, ClientType deviceEnum, SSOEnums.View ssoView) throws EJBException {
      try {
         MigboApiUtil apiUtil = MigboApiUtil.getInstance();
         String pathPrefix = String.format("/user/%d/post", userID);
         MigboEnums.PostApplicationEnum migboApplicationTypeEnum = MigboEnums.PostApplicationEnum.fromDeviceType(deviceEnum);
         if (migboApplicationTypeEnum == null) {
            migboApplicationTypeEnum = MigboEnums.PostApplicationEnum.fromSSOView(ssoView);
            if (migboApplicationTypeEnum == null) {
               migboApplicationTypeEnum = MigboEnums.PostApplicationEnum.J2ME;
            }
         }

         log.info(String.format("creating migbo post,  userid %d, body '%s', root %s, parent %s, ori %s, device %s, view %s, app %s", userID, text, rootPost, parentPost, originality, deviceEnum, ssoView, migboApplicationTypeEnum));
         JSONObject postJson = new JSONObject();
         postJson.put("_version", "1");
         postJson.put("body", text.substring(0, Math.min(140, text.length())));
         postJson.put("application", Integer.toString(migboApplicationTypeEnum.value()));
         if (!StringUtil.isBlank(parentPost)) {
            postJson.put("parent_post", parentPost);
         }

         if (!StringUtil.isBlank(rootPost)) {
            postJson.put("root_post", rootPost);
         }

         if (!StringUtil.isBlank(originality)) {
            postJson.put("originality", originality);
         }

         JSONObject result = apiUtil.post(pathPrefix, postJson.toString());
         if (result != null) {
            return result.toString();
         }
      } catch (Exception var13) {
         log.warn(String.format("Unable to create post for userid[%d] text[%s] :%s", userID, text, var13.getMessage()), var13);
         throw new EJBException(var13.getMessage());
      }

      return (new JSONObject()).toString();
   }

   private JSONObject createMigboTextPost(int userID, String text, String rootPost, String parentPost, String originality, MigboEnums.PostApplicationEnum migboApplicationTypeEnum) throws JSONException, MigboApiUtil.MigboApiException {
      String pathPrefix = String.format("/user/%d/post", userID);
      MigboApiUtil apiUtil = MigboApiUtil.getInstance();
      JSONObject postJson = new JSONObject();
      postJson.put("_version", "1");
      postJson.put("body", text.substring(0, Math.min(SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.ContentService.MINIBLOG_TEXT_CONTENT_MAX_LENGTH), text.length())));
      postJson.put("application", Integer.toString(migboApplicationTypeEnum.value()));
      if (!StringUtil.isBlank(parentPost)) {
         postJson.put("parent_post", parentPost);
      }

      if (!StringUtil.isBlank(rootPost)) {
         postJson.put("root_post", rootPost);
      }

      if (!StringUtil.isBlank(originality)) {
         postJson.put("originality", originality);
      }

      return apiUtil.post(pathPrefix, postJson.toString());
   }

   public void enforceFreeGiftToReferrerRules(String buyerUsername, String recipientUsername) throws FusionEJBException, EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select a.id from user u left outer join accountentry a on (u.username = a.username and a.type = ? and a.amount = 0) where u.username = ? and u.referredby = ?");
         ps.setInt(1, AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE.value());
         ps.setString(2, buyerUsername);
         ps.setString(3, recipientUsername);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new FusionEJBException("You can only purchase free gifts to your referrer");
         }

         if (rs.getObject(1) != null) {
            throw new FusionEJBException("You can only purchase one free gift to your referrer");
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage(), var21);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }

   }

   public int recordVirtualGift(String buyerUsername, String recipient, VirtualGiftData gift, int purchaseLocation, boolean privateGift, String message) throws FusionEJBException, EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var34;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("insert into virtualgiftreceived (username, datecreated, purchaselocation, virtualgiftid, sender, private, message) values (?, now(), ?, ?, ?, ?, ?)", 1);
         ps.setString(1, recipient);
         ps.setInt(2, purchaseLocation);
         ps.setInt(3, gift.getId());
         ps.setString(4, buyerUsername);
         ps.setInt(5, privateGift ? 1 : 0);
         ps.setString(6, message);
         int rowsUpdated = ps.executeUpdate();
         if (rowsUpdated != 1) {
            throw new EJBException("Internal Server Error (Unable to record gift purchase)");
         }

         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Internal Server Error (Unable to record gift purchase)");
         }

         int virtualGiftReceivedId = rs.getInt(1);

         try {
            if (!privateGift) {
               if (SystemProperty.getBool("UseRedisDataStore", true)) {
                  try {
                     UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                     int recipientUserId = userBean.getUserID(recipient, (Connection)null);
                     GiftsReceivedCounter.incrementCacheCount(recipientUserId);
                  } catch (EJBException var30) {
                     log.error("Unable to increment gifts received counter for username [" + recipient + "]: " + var30);
                  }
               } else {
                  MemCachedClientWrapper.incr(MemCachedKeySpaces.CommonKeySpace.NUM_VIRTUAL_GIFTS_RECEIVED, recipient);
               }
            }

            MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipient);
         } catch (Exception var31) {
            log.info("Failed to remove memcached key for concurrent mutual gifting due to: " + var31.getMessage());
         }

         var34 = virtualGiftReceivedId;
      } catch (SQLException var32) {
         throw new EJBException(var32.getMessage(), var32);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var28) {
            ps = null;
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var27) {
            connMaster = null;
         }

      }

      return var34;
   }

   public void billVirtualGiftForMultipleUsers(String buyerUsername, VirtualGiftData gift, HashMap<String, Integer> recipientWithVGReceivedID, AccountEntrySourceData accountEntrySourceData) throws EJBException, FusionEJBException {
      Connection connMaster = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = buyerUsername;
         accountEntry.type = AccountEntryData.TypeEnum.VIRTUAL_GIFT_PURCHASE;
         Iterator i$ = recipientWithVGReceivedID.keySet().iterator();

         while(i$.hasNext()) {
            String recipient = (String)i$.next();
            Integer virtualGiftReceivedId = (Integer)recipientWithVGReceivedID.get(recipient);
            accountEntry.currency = gift.getCurrency();
            accountEntry.amount = -gift.getPrice();
            accountEntry.fundedAmount = null;
            accountEntry.tax = 0.0D;
            accountEntry.reference = Integer.toString(virtualGiftReceivedId);
            accountEntry.description = String.format("Purchased the gift %s for %s", gift.getName(), recipient);
            accountBean.createAccountEntry(connMaster, accountEntry, accountEntrySourceData);
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } catch (CreateException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var19) {
            connMaster = null;
         }

      }

   }

   private ReferenceStoreItemData getStoreItemReferenceData(StoreItemData.TypeEnum type, Integer referenceID, String username) throws FusionEJBException {
      switch(type) {
      case VIRTUAL_GIFT:
         return this.getVirtualGift(referenceID, username);
      case AVATAR:
         return this.getAvatarItem(referenceID);
      case STICKER:
         EmoticonPackData epd = (EmoticonPackData)this.loadStickerPacks().get(referenceID);
         List<Integer> ids = this.getStickerPackIDListForUser(username);
         if (epd != null) {
            epd.setOwned(ids.contains(epd.getId()));
         }

         return epd;
      case EMOTICON:
      case SUPER_EMOTICON:
         return (ReferenceStoreItemData)this.loadEmoticonPacks().get(referenceID);
      case THEME:
         return this.getThemeById(referenceID);
      default:
         return null;
      }
   }

   public StoreItemData getStoreItem(String username, int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      StoreItemData sid;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT si.*,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency,sc.parentstorecategoryid,sic.storecategoryid, g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member FROM storeitem si left join storeitemcategory sic on si.id= sic.storeitemid left join storecategory sc on sc.id = sic.storecategoryid  left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username=?,user u,currency fc,currency tc WHERE u.username=? AND si.id = ?  AND fc.code=si.currency AND tc.code=u.currency");
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            sid = new StoreItemData(rs);
            sid.groupName = rs.getString("GroupName");
            sid.isGroupMember = rs.getBoolean("Member");
            sid.storeCategoryID = (Integer)rs.getObject("StoreCategoryID");
            sid.parentStoreCategoryID = (Integer)rs.getObject("ParentStoreCategoryID");
            sid.localPrice = rs.getDouble("localPrice");
            sid.localCurrency = rs.getString("localCurrency");
            sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
            sid.roundLocalPrice();
            StoreItemData var7 = sid;
            return var7;
         }

         sid = null;
      } catch (EJBException var26) {
         throw var26;
      } catch (Exception var27) {
         log.error(var27.getMessage(), var27);
         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return sid;
   }

   public VirtualGiftData getVirtualGift(Integer virtualGiftID, String username) throws EJBException, FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      VirtualGiftData gift = null;

      VirtualGiftData var8;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT virtualgift.ID, virtualgift.Name, virtualgift.HotKey, virtualgift.Price / currency_gift.ExchangeRate * currency_user.ExchangeRate Price, currency_user.Code Currency, virtualgift.NumAvailable, virtualgift.NumSold, virtualgift.GroupID, virtualgift.GroupVIPOnly, virtualgift.SortOrder, virtualgift.Location12x12GIF, virtualgift.Location12x12PNG, virtualgift.Location14x14GIF, virtualgift.Location14x14PNG, virtualgift.Location16x16GIF, virtualgift.Location16x16PNG, virtualgift.Location64x64PNG, virtualgift.Status, virtualgift.GiftAllMessage FROM virtualgift, user, currency currency_user, currency currency_gift WHERE user.Username=? and user.Currency=currency_user.Code and virtualgift.currency=currency_gift.Code ";
         sql = sql + "and virtualgift.ID=?";
         ps = connSlave.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, virtualGiftID);
         rs = ps.executeQuery();
         if (rs.next()) {
            gift = new VirtualGiftData(rs);
            var8 = gift;
            return var8;
         }

         var8 = null;
      } catch (SQLException var26) {
         log.error(var26.getMessage(), var26);
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var23) {
            connSlave = null;
         }

      }

      return var8;
   }

   public AvatarItemData getAvatarItem(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AvatarItemData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT * FROM avataritem WHERE id = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = new AvatarItemData(rs);
      } catch (SQLException var23) {
         log.error(var23.getMessage(), var23);
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var5;
   }

   public ThemeData getThemeById(int id) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ThemeData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT * FROM theme WHERE id = ?");
         ps.setInt(1, id);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = new ThemeData(rs);
            return var5;
         }

         var5 = null;
      } catch (SQLException var23) {
         log.error(var23.getMessage(), var23);
         throw new EJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var5;
   }

   public VirtualGiftReceivedData getVirtualGiftReceived(String session_username, String username, int vgid) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      VirtualGiftReceivedData var9;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "SELECT vgr.id AS id,vgr.username,vgr.virtualgiftid AS giftid,vgr.datecreated AS datecreated,vgr.sender AS sender,vgr.message AS message,vgr.removed AS removed,vgr.private AS private,si.id AS storeitemid,si.name AS name,si.catalogimage AS location FROM virtualgiftreceived vgr,storeitem si WHERE vgr.username = ? AND si.type = 1 AND si.referenceid = vgr.virtualgiftid AND vgr.id = ?";
         if (!session_username.equals(username)) {
            sql = sql + " and vgr.private=0";
         }

         sql = sql + " order by vgr.datecreated desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, vgid);
         log.info(ps);
         rs = ps.executeQuery();
         VirtualGiftReceivedData vgrd;
         if (!rs.next()) {
            vgrd = null;
            return vgrd;
         }

         vgrd = new VirtualGiftReceivedData(rs);
         vgrd.setStoreItemID(rs.getInt("storeitemid"));
         vgrd.setName(rs.getString("name"));
         vgrd.setLocation(rs.getString("location"));
         vgrd.setStoreItemData(this.getStoreItem(username, vgrd.getStoreItemID()));
         var9 = vgrd;
      } catch (Exception var27) {
         throw new EJBException(var27.getMessage(), var27);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return var9;
   }

   public ListDataWrapper<VirtualGiftReceivedData> getVirtualGiftsReceived(String session_username, String username, int offset, int limit) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select vgr.id as id, vgr.username ,vgr.virtualgiftid as giftid, vgr.datecreated as datecreated, vgr.sender as sender, vgr.message as message, vgr.removed as removed, vgr.private as private, si.name as name, si.id as storeitemid, si.catalogimage as location from virtualgiftreceived vgr, storeitem si";
         String param = " WHERE vgr.username = ? and si.type = 1 and si.referenceid = vgr.virtualgiftid";
         if (!session_username.equals(username)) {
            param = param + " and vgr.private=0";
         }

         sql = sql + param;
         sql = sql + " ORDER BY vgr.datecreated desc";
         sql = sql + " LIMIT ?, ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, offset);
         ps.setInt(3, limit != 0 ? limit : 30);
         log.info(ps);
         rs = ps.executeQuery();
         ArrayList resultList = new ArrayList();

         while(rs.next()) {
            VirtualGiftReceivedData vgrd = new VirtualGiftReceivedData(rs);
            vgrd.setStoreItemID(rs.getInt("storeitemid"));
            vgrd.setName(rs.getString("name"));
            vgrd.setLocation(rs.getString("location"));
            resultList.add(vgrd);
         }

         String sqlCount = "select count(vgr.id)  from virtualgiftreceived vgr, storeitem si";
         sqlCount = sqlCount + param;
         ps = conn.prepareStatement(sqlCount);
         ps.setString(1, username);
         log.info(ps);
         rs = ps.executeQuery();
         ListDataWrapper<VirtualGiftReceivedData> sicdw = new ListDataWrapper(resultList);
         if (rs.next()) {
            sicdw.setTotalResults(rs.getInt(1));
         }

         ListDataWrapper var13 = sicdw;
         return var13;
      } catch (Exception var28) {
         throw new EJBException(var28.getMessage(), var28);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }
   }

   public ListDataWrapper<StoreItemData> searchStoreItems_old(String username, String query, StoreItemData.TypeEnum type, Integer categoryId, double minPrice, double maxPrice, String sortby, String sortorder, int offset, int limit, boolean featured) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ListDataWrapper var23;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select si.* ,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency, g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ? ,user u,currency fc,currency tc";
         String param = " WHERE u.username= ? ";
         param = param + " AND si.status=?";
         param = param + " AND si.type=?";
         param = param + " AND si.name LIKE ? ";
         if (categoryId != null) {
            param = param + " AND sc.id = ? ";
         }

         param = param + " AND fc.code=si.currency AND tc.code=u.currency";
         if (featured) {
            param = param + " AND si.featured=1";
         }

         if (minPrice != -1.0D) {
            param = param + String.format(" AND (si.price/fc.exchangerate)*tc.exchangerate >= %f", minPrice);
         }

         if (maxPrice != -1.0D) {
            param = param + String.format(" AND (si.price/fc.exchangerate)*tc.exchangerate <= %f", maxPrice);
         }

         sql = sql + param;
         sql = sql + String.format(" ORDER BY %s %s", !StringUtils.isEmpty(sortby) ? sortby : "date", !StringUtils.isEmpty(sortorder) ? sortorder : "desc");
         sql = sql + " LIMIT ?, ?";
         ps = conn.prepareStatement(sql);
         int ctr = 0;
         int ctr = ctr + 1;
         ps.setString(ctr, username);
         ++ctr;
         ps.setString(ctr, username);
         ++ctr;
         ps.setInt(ctr, StoreItemData.StatusEnum.ACTIVE.value());
         ++ctr;
         ps.setInt(ctr, type.value());
         ++ctr;
         ps.setString(ctr, "%" + query + "%");
         if (categoryId != null) {
            ++ctr;
            ps.setInt(ctr, categoryId);
         }

         ++ctr;
         ps.setInt(ctr, offset != 0 ? offset : 0);
         ++ctr;
         ps.setInt(ctr, limit != 0 ? limit : 30);
         rs = ps.executeQuery();
         ArrayList result = new ArrayList();

         while(rs.next()) {
            StoreItemData sid = new StoreItemData(rs);
            sid.groupName = rs.getString("GroupName");
            sid.isGroupMember = rs.getBoolean("Member");
            sid.localPrice = rs.getDouble("localPrice");
            sid.localCurrency = rs.getString("localCurrency");
            sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
            sid.roundLocalPrice();
            result.add(sid);
         }

         String sqlCount = "select count(si.id) from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ? ,user u,currency fc,currency tc";
         sqlCount = sqlCount + param;
         ps = conn.prepareStatement(sqlCount);
         ctr = 0;
         ctr = ctr + 1;
         ps.setString(ctr, username);
         ++ctr;
         ps.setString(ctr, username);
         ++ctr;
         ps.setInt(ctr, StoreItemData.StatusEnum.ACTIVE.value());
         ++ctr;
         ps.setInt(ctr, type.value());
         ++ctr;
         ps.setString(ctr, "%" + query + "%");
         if (categoryId != null) {
            ++ctr;
            ps.setInt(ctr, categoryId);
         }

         rs = ps.executeQuery();
         ListDataWrapper<StoreItemData> sicdw = new ListDataWrapper(result);
         if (rs.next()) {
            sicdw.setTotalResults(rs.getInt(1));
         }

         var23 = sicdw;
      } catch (Exception var38) {
         throw new FusionEJBException(var38.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var37) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var36) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var35) {
            conn = null;
         }

      }

      return var23;
   }

   public ListDataWrapper<StoreItemData> getStoreItemsByType_old(String username, int typeId, String sortby, String sortorder, int offset, int limit, boolean featured) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select si.*,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency,g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
         String param = " WHERE u.username= ?";
         param = param + " AND si.type = ?";
         param = param + " AND si.status = ?";
         param = param + " AND fc.code=si.currency AND tc.code=u.currency";
         if (featured) {
            param = param + " AND si.featured=1";
         }

         sql = sql + param;
         sql = sql + String.format(" ORDER BY %s %s", !StringUtils.isEmpty(sortby) ? sortby : "si.name", !StringUtils.isEmpty(sortorder) ? sortorder : "asc");
         sql = sql + " LIMIT ?, ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, typeId);
         ps.setInt(4, StoreItemData.StatusEnum.ACTIVE.value());
         ps.setInt(5, offset != 0 ? offset : 0);
         ps.setInt(6, limit != 0 ? limit : 30);
         rs = ps.executeQuery();
         ArrayList result = new ArrayList();

         while(rs.next()) {
            StoreItemData sid = new StoreItemData(rs);
            sid.groupName = rs.getString("GroupName");
            sid.isGroupMember = rs.getBoolean("Member");
            sid.localPrice = rs.getDouble("localPrice");
            sid.localCurrency = rs.getString("localCurrency");
            sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
            sid.roundLocalPrice();
            result.add(sid);
         }

         rs.close();
         ps.close();
         String sqlCount = "select count(si.id) from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
         sqlCount = sqlCount + param;
         ps = conn.prepareStatement(sqlCount);
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, typeId);
         ps.setInt(4, StoreItemData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         ListDataWrapper<StoreItemData> sicdw = new ListDataWrapper(result);
         if (rs.next()) {
            sicdw.setTotalResults(rs.getInt(1));
         }

         ListDataWrapper var16 = sicdw;
         return var16;
      } catch (Exception var31) {
         throw new FusionEJBException(var31.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var28) {
            conn = null;
         }

      }
   }

   public List<StoreCategoryData> getStoreCategories_old(int parentId, boolean sortorder) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "SELECT sc.*, count(si.id) as TotalItems FROM storecategory sc left join  storeitemcategory sic on sc.id=sic.storecategoryid left join storeitem si on sic.storeitemid = si.id";
         if (parentId == 0) {
            sql = sql + " WHERE parentstorecategoryid IS NULL ";
         } else {
            sql = sql + " WHERE parentstorecategoryid = ? ";
         }

         sql = sql + " AND si.status=?";
         sql = sql + " GROUP BY sc.id";
         if (sortorder) {
            sql = sql + " ORDER BY sc.sortorder, sc.name ASC ";
         } else {
            sql = sql + " ORDER BY sc.name ASC";
         }

         ps = conn.prepareStatement(sql);
         if (parentId != 0) {
            ps.setInt(1, parentId);
            ps.setInt(2, StoreItemData.StatusEnum.ACTIVE.value());
         } else {
            ps.setInt(1, StoreItemData.StatusEnum.ACTIVE.value());
         }

         log.info(ps);
         rs = ps.executeQuery();
         ArrayList result = new ArrayList();

         while(rs.next()) {
            StoreCategoryData scd = new StoreCategoryData(rs);
            scd.totalItems = rs.getInt("TotalItems");
            result.add(scd);
         }

         ArrayList var25 = result;
         return var25;
      } catch (Exception var23) {
         throw new FusionEJBException(var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }
   }

   public ListDataWrapper<StoreItemData> getStoreItemsByCategory_old(String username, int categoryId, String sortby, String sortorder, int offset, int limit, boolean featured) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select si.*,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency,g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
         String param = " WHERE u.username= ?";
         if (categoryId == 0) {
            param = param + " AND sc.id IS NULL ";
         } else {
            param = param + " AND sc.id = ? ";
         }

         param = param + " AND si.status =?";
         param = param + " AND fc.code=si.currency AND tc.code=u.currency";
         if (featured) {
            param = param + " AND si.featured=1";
         }

         sql = sql + param;
         sql = sql + String.format(" ORDER BY %s %s", !StringUtils.isEmpty(sortby) ? sortby : "si.name", !StringUtils.isEmpty(sortorder) ? sortorder : "asc");
         sql = sql + " LIMIT ?, ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, categoryId != 0 ? categoryId : 0);
         ps.setInt(4, StoreItemData.StatusEnum.ACTIVE.value());
         ps.setInt(5, offset != 0 ? offset : 0);
         ps.setInt(6, limit != 0 ? limit : 30);
         rs = ps.executeQuery();
         ArrayList result = new ArrayList();

         while(rs.next()) {
            StoreItemData sid = new StoreItemData(rs);
            sid.groupName = rs.getString("GroupName");
            sid.isGroupMember = rs.getBoolean("Member");
            sid.localPrice = rs.getDouble("localPrice");
            sid.localCurrency = rs.getString("localCurrency");
            sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
            sid.roundLocalPrice();
            result.add(sid);
         }

         String sqlCount = "select count(si.id) from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
         sqlCount = sqlCount + param;
         ps = conn.prepareStatement(sqlCount);
         ps.setString(1, username);
         ps.setString(2, username);
         ps.setInt(3, categoryId != 0 ? categoryId : 0);
         ps.setInt(4, StoreItemData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         ListDataWrapper<StoreItemData> sicdw = new ListDataWrapper(result);
         if (rs.next()) {
            sicdw.setTotalResults(rs.getInt(1));
         }

         ListDataWrapper var16 = sicdw;
         return var16;
      } catch (Exception var31) {
         throw new FusionEJBException(var31.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var28) {
            conn = null;
         }

      }
   }

   public ListDataWrapper<StoreItemData> searchStoreItems(String username, String query, StoreItemData.TypeEnum type, Integer categoryId, double minPrice, double maxPrice, String sortby, String sortorder, int offset, int limit, boolean featured) throws FusionEJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Store.STORE_OLD_ENABLED)) {
         return this.searchStoreItems_old(username, query, type, categoryId, minPrice, maxPrice, sortby, sortorder, offset, limit, featured);
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ReputationLevelData rld = userBean.getReputationLevel(username);
            conn = this.dataSourceSlave.getConnection();
            String sql = "select si.* ,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency, g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ? ,user u,currency fc,currency tc";
            String param = " WHERE u.username= ? AND si.miglevelmin <=?";
            param = param + " AND si.status=?";
            param = param + " AND si.type=?";
            param = param + " AND si.name LIKE ? ";
            if (categoryId != null) {
               param = param + " AND sc.id = ? ";
            }

            param = param + " AND fc.code=si.currency AND tc.code=u.currency";
            if (featured) {
               param = param + " AND si.featured=1";
            }

            if (minPrice != -1.0D) {
               param = param + String.format(" AND (si.price/fc.exchangerate)*tc.exchangerate >= %f", minPrice);
            }

            if (maxPrice != -1.0D) {
               param = param + String.format(" AND (si.price/fc.exchangerate)*tc.exchangerate <= %f", maxPrice);
            }

            sql = sql + param;
            sql = sql + String.format(" ORDER BY %s", this.getSortString(!StringUtils.isEmpty(sortby) ? sortby : "date", !StringUtils.isEmpty(sortorder) ? sortorder : "desc"));
            sql = sql + " LIMIT ?, ?";
            ps = conn.prepareStatement(sql);
            int ctr = 0;
            int ctr = ctr + 1;
            ps.setString(ctr, username);
            ++ctr;
            ps.setString(ctr, username);
            ++ctr;
            ps.setInt(ctr, rld.level);
            ++ctr;
            ps.setInt(ctr, StoreItemData.StatusEnum.ACTIVE.value());
            ++ctr;
            ps.setInt(ctr, type.value());
            ++ctr;
            ps.setString(ctr, "%" + query + "%");
            if (categoryId != null) {
               ++ctr;
               ps.setInt(ctr, categoryId);
            }

            ++ctr;
            ps.setInt(ctr, offset);
            ++ctr;
            ps.setInt(ctr, limit != 0 ? limit : 30);
            rs = ps.executeQuery();
            ArrayList result = new ArrayList();

            while(rs.next()) {
               StoreItemData sid = new StoreItemData(rs);
               sid.groupName = rs.getString("GroupName");
               sid.isGroupMember = rs.getBoolean("Member");
               sid.localPrice = rs.getDouble("localPrice");
               sid.localCurrency = rs.getString("localCurrency");
               sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
               sid.roundLocalPrice();
               result.add(sid);
            }

            String sqlCount = "select count(si.id) from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ? ,user u,currency fc,currency tc";
            sqlCount = sqlCount + param;
            ps = conn.prepareStatement(sqlCount);
            ctr = 0;
            ctr = ctr + 1;
            ps.setString(ctr, username);
            ++ctr;
            ps.setString(ctr, username);
            ++ctr;
            ps.setInt(ctr, rld.level);
            ++ctr;
            ps.setInt(ctr, StoreItemData.StatusEnum.ACTIVE.value());
            ++ctr;
            ps.setInt(ctr, type.value());
            ++ctr;
            ps.setString(ctr, "%" + query + "%");
            if (categoryId != null) {
               ++ctr;
               ps.setInt(ctr, categoryId);
            }

            rs = ps.executeQuery();
            ListDataWrapper<StoreItemData> sicdw = new ListDataWrapper(result);
            if (rs.next()) {
               sicdw.setTotalResults(rs.getInt(1));
            }

            ListDataWrapper var25 = sicdw;
            return var25;
         } catch (Exception var40) {
            throw new FusionEJBException(var40.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var39) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var38) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var37) {
               conn = null;
            }

         }
      }
   }

   private String getSortString(String sortby, String sortorder) throws Exception {
      String[] sortbyArr = sortby.split(",");
      String[] sortOrderArr = sortorder.split(",");
      if (sortbyArr.length != sortOrderArr.length) {
         throw new IllegalArgumentException("sort by number is not equal to sort order");
      } else {
         String sortbyString = "";

         for(int i = 0; i < sortbyArr.length; ++i) {
            if (i != 0) {
               sortbyString = sortbyString + ",";
            }

            if (!VALID_SORT_ORDER.contains(sortOrderArr[i])) {
               throw new IllegalArgumentException("Unsupported sort order " + sortOrderArr[i]);
            }

            if (!VALID_SORT_BY.contains(sortbyArr[i])) {
               throw new IllegalArgumentException("Unsupported sort by" + sortbyArr[i]);
            }

            sortbyString = sortbyString + sortbyArr[i] + " " + sortOrderArr[i];
         }

         return sortbyString;
      }
   }

   public ListDataWrapper<StoreItemData> getStoreItemsByType(String username, int typeId, String sortby, String sortorder, int offset, int limit, boolean featured) throws FusionEJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Store.STORE_OLD_ENABLED)) {
         return this.getStoreItemsByType_old(username, typeId, sortby, sortorder, offset, limit, featured);
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ReputationLevelData rld = userBean.getReputationLevel(username);
            conn = this.dataSourceSlave.getConnection();
            String sql = "select si.*,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency,g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
            String param = " WHERE u.username= ?  AND si.miglevelmin <=?";
            param = param + " AND si.type = ?";
            param = param + " AND si.status = ?";
            param = param + " AND fc.code=si.currency AND tc.code=u.currency";
            if (featured) {
               param = param + " AND si.featured=1";
            }

            sql = sql + param;
            sql = sql + String.format(" ORDER BY %s", this.getSortString(!StringUtils.isEmpty(sortby) ? sortby : "date", !StringUtils.isEmpty(sortorder) ? sortorder : "desc"));
            sql = sql + " LIMIT ?, ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setInt(3, rld.level);
            ps.setInt(4, typeId);
            ps.setInt(5, StoreItemData.StatusEnum.ACTIVE.value());
            ps.setInt(6, offset);
            ps.setInt(7, limit != 0 ? limit : 30);
            rs = ps.executeQuery();
            ArrayList result = new ArrayList();

            while(rs.next()) {
               StoreItemData sid = new StoreItemData(rs);
               sid.groupName = rs.getString("GroupName");
               sid.isGroupMember = rs.getBoolean("Member");
               sid.localPrice = rs.getDouble("localPrice");
               sid.localCurrency = rs.getString("localCurrency");
               sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
               sid.roundLocalPrice();
               result.add(sid);
            }

            rs.close();
            ps.close();
            String sqlCount = "select count(si.id) from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
            sqlCount = sqlCount + param;
            ps = conn.prepareStatement(sqlCount);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setInt(3, rld.level);
            ps.setInt(4, typeId);
            ps.setInt(5, StoreItemData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            ListDataWrapper<StoreItemData> sicdw = new ListDataWrapper(result);
            if (rs.next()) {
               sicdw.setTotalResults(rs.getInt(1));
            }

            ListDataWrapper var18 = sicdw;
            return var18;
         } catch (Exception var33) {
            throw new FusionEJBException(var33.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var32) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var31) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var30) {
               conn = null;
            }

         }
      }
   }

   public List<StoreCategoryData> getStoreCategories(int userId, int parentId, boolean sortorder) throws FusionEJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Store.STORE_OLD_ENABLED)) {
         return this.getStoreCategories_old(parentId, sortorder);
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         ArrayList var28;
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ReputationLevelData rld = userBean.getReputationLevelByUserid(userId);
            conn = this.dataSourceSlave.getConnection();
            String sql = "SELECT sc.*, count(si.id) as TotalItems FROM storecategory sc left join  storeitemcategory sic on sc.id=sic.storecategoryid left join storeitem si on sic.storeitemid = si.id";
            sql = sql + " WHERE si.miglevelmin <=?";
            if (parentId == 0) {
               sql = sql + " AND parentstorecategoryid IS NULL ";
            } else {
               sql = sql + " AND parentstorecategoryid = ? ";
            }

            sql = sql + " AND si.status=?";
            sql = sql + " GROUP BY sc.id";
            if (sortorder) {
               sql = sql + " ORDER BY sc.sortorder, sc.name ASC ";
            } else {
               sql = sql + " ORDER BY sc.name ASC";
            }

            ps = conn.prepareStatement(sql);
            ps.setInt(1, rld.level);
            if (parentId != 0) {
               ps.setInt(2, parentId);
               ps.setInt(3, StoreItemData.StatusEnum.ACTIVE.value());
            } else {
               ps.setInt(2, StoreItemData.StatusEnum.ACTIVE.value());
            }

            log.info(ps);
            rs = ps.executeQuery();
            ArrayList result = new ArrayList();

            while(rs.next()) {
               StoreCategoryData scd = new StoreCategoryData(rs);
               scd.totalItems = rs.getInt("TotalItems");
               result.add(scd);
            }

            var28 = result;
         } catch (Exception var26) {
            throw new FusionEJBException(var26.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var25) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var24) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var23) {
               conn = null;
            }

         }

         return var28;
      }
   }

   public StoreCategoryData getStoreCategory(int userId, int id, boolean sortorder) throws FusionEJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      StoreCategoryData var8;
      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "SELECT sc.*, count(sic.storeitemid) as TotalItems FROM storecategory sc left join  storeitemcategory sic on sc.id=sic.storecategoryid left join storeitem si on sic.storeitemid = si.id";
         if (id == 0) {
            sql = sql + " WHERE sc.id IS NULL ";
         } else {
            sql = sql + " WHERE sc.id = ? ";
         }

         sql = sql + " GROUP BY sc.id";
         if (sortorder) {
            sql = sql + " ORDER BY sc.sortorder, sc.name ASC ";
         } else {
            sql = sql + " ORDER BY sc.name ASC";
         }

         ps = conn.prepareStatement(sql);
         if (id != 0) {
            ps.setInt(1, id);
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            var8 = new StoreCategoryData(rs);
            return var8;
         }

         var8 = null;
      } catch (Exception var26) {
         throw new FusionEJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return var8;
   }

   public ListDataWrapper<StoreItemData> getStoreItemsByCategory(String username, int categoryId, String sortby, String sortorder, int offset, int limit, boolean featured) throws FusionEJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Store.STORE_OLD_ENABLED)) {
         return this.getStoreItemsByCategory_old(username, categoryId, sortby, sortorder, offset, limit, featured);
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         ListDataWrapper var18;
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ReputationLevelData rld = userBean.getReputationLevel(username);
            conn = this.dataSourceSlave.getConnection();
            String sql = "select si.*,(si.price/fc.exchangerate)*tc.exchangerate localPrice, u.currency localCurrency,g.name GroupName,IF(gm.username IS NULL, 'False', 'True') Member from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
            String param = " WHERE u.username= ?  AND si.miglevelmin <=?";
            if (categoryId == 0) {
               param = param + " AND sc.id IS NULL ";
            } else {
               param = param + " AND sc.id = ? ";
            }

            param = param + " AND si.status =?";
            param = param + " AND fc.code=si.currency AND tc.code=u.currency";
            if (featured) {
               param = param + " AND si.featured=1";
            }

            sql = sql + param;
            sql = sql + String.format(" ORDER BY %s", this.getSortString(!StringUtils.isEmpty(sortby) ? sortby : "si.name", !StringUtils.isEmpty(sortorder) ? sortorder : "asc"));
            sql = sql + " LIMIT ?, ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setInt(3, rld.level);
            ps.setInt(4, categoryId != 0 ? categoryId : 0);
            ps.setInt(5, StoreItemData.StatusEnum.ACTIVE.value());
            ps.setInt(6, offset);
            ps.setInt(7, limit != 0 ? limit : 30);
            rs = ps.executeQuery();
            ArrayList result = new ArrayList();

            while(rs.next()) {
               StoreItemData sid = new StoreItemData(rs);
               sid.groupName = rs.getString("GroupName");
               sid.isGroupMember = rs.getBoolean("Member");
               sid.localPrice = rs.getDouble("localPrice");
               sid.localCurrency = rs.getString("localCurrency");
               sid.referenceData = this.getStoreItemReferenceData(sid.type, sid.referenceID, username);
               sid.roundLocalPrice();
               result.add(sid);
            }

            String sqlCount = "select count(si.id) from storeitem si left join storeitemcategory sic on sic.StoreItemID= si.id left join storecategory sc on sc.id  = sic.storecategoryid left join groups g on g.id = si.groupid left join groupmember gm on gm.groupid=g.id and gm.username= ?,user u,currency fc,currency tc";
            sqlCount = sqlCount + param;
            ps = conn.prepareStatement(sqlCount);
            ps.setString(1, username);
            ps.setString(2, username);
            ps.setInt(3, rld.level);
            ps.setInt(4, categoryId != 0 ? categoryId : 0);
            ps.setInt(5, StoreItemData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            ListDataWrapper<StoreItemData> sicdw = new ListDataWrapper(result);
            if (rs.next()) {
               sicdw.setTotalResults(rs.getInt(1));
            }

            var18 = sicdw;
         } catch (Exception var33) {
            throw new FusionEJBException(var33.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var32) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var31) {
               ps = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var30) {
               conn = null;
            }

         }

         return var18;
      }
   }

   public List<Long> addStoreItemsInventory(Map<Integer, Integer> storeItemIdToUnlockCount, int userid, StoreItemInventoryData.StoreItemInventoryLocationEnum location) {
      Connection conn = null;
      ResultSet rs = null;
      ResultSet generatedKeys = null;
      PreparedStatement ps = null;
      ArrayList ids = new ArrayList();

      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "insert into storeiteminventory (storeitemid,userid,location,datecreated) values(?,?,?,?)";
         ps = conn.prepareStatement(sql, 1);
         Iterator i$ = storeItemIdToUnlockCount.entrySet().iterator();

         while(i$.hasNext()) {
            Entry<Integer, Integer> set = (Entry)i$.next();
            Integer storeItemId = (Integer)set.getKey();
            Integer count = (Integer)set.getValue();

            for(int i = 0; i < count; ++i) {
               ps.setInt(1, storeItemId);
               ps.setInt(2, userid);
               ps.setInt(3, location != null ? location.value() : StoreItemInventoryData.StoreItemInventoryLocationEnum.UNLOCK.value());
               ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
               ps.addBatch();
               log.debug(ps);
            }

            ps.executeBatch();
            generatedKeys = ps.getGeneratedKeys();

            while(generatedKeys.next()) {
               ids.add(generatedKeys.getLong(1));
            }
         }

         ArrayList var31 = ids;
         return var31;
      } catch (Exception var29) {
         throw new EJBException(var29.getMessage(), var29);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var28) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var27) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var26) {
            conn = null;
         }

      }
   }

   public long addStoreItemInventory(int storeItemId, int userid, StoreItemInventoryData.StoreItemInventoryLocationEnum location) {
      Connection conn = null;
      ResultSet rs = null;
      ResultSet generatedKeys = null;
      PreparedStatement ps = null;

      long var9;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "insert into storeiteminventory (storeitemid,userid,location,datecreated) values(?,?,?,?)";
         ps = conn.prepareStatement(sql, 1);
         ps.setInt(1, storeItemId);
         ps.setInt(2, userid);
         ps.setInt(3, location != null ? location.value() : StoreItemInventoryData.StoreItemInventoryLocationEnum.UNLOCK.value());
         ps.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
         log.debug(ps);
         ps.executeUpdate();
         generatedKeys = ps.getGeneratedKeys();
         if (!generatedKeys.next()) {
            throw new SQLException("Creating item failed.");
         }

         var9 = generatedKeys.getLong(1);
      } catch (Exception var25) {
         throw new EJBException(var25.getMessage(), var25);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

      return var9;
   }

   public void addStoreItemInventoryReceived(long storeItemInventoryId, int receiveruserid, Integer referenceID, StoreItemData.TypeEnum referenceType) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "insert into storeiteminventoryreceived (storeiteminventoryid,receiveruserid,referenceID,referenceType,datecreated) values(?,?,?,?,?)";
         ps = conn.prepareStatement(sql);
         ps.setLong(1, storeItemInventoryId);
         ps.setInt(2, receiveruserid);
         ps.setObject(3, referenceID);
         ps.setInt(4, referenceType.value());
         ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
         log.debug(ps);
         ps.executeUpdate();
      } catch (Exception var24) {
         var24.printStackTrace();
         throw new EJBException(var24.getMessage(), var24);
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

   }

   public StoreItemInventorySummaryData getStoreItemInventory(int userid, int storeItemid) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      StoreItemInventorySummaryData siisd;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userBean.getUsernameByUserid(userid, (Connection)null);
         String sql;
         if (username == null) {
            sql = null;
            return sql;
         }

         conn = this.dataSourceSlave.getConnection();
         sql = "select  sii.*,count(si.id) as count from storeiteminventory sii left join storeiteminventoryreceived siir on siir.storeiteminventoryid = sii.id,storeitem si where sii.userid = ? and si.id= sii.storeitemid and siir.id is null and sii.storeitemid=?";
         sql = sql + " group by sii.storeitemid";
         sql = sql + " order by sii.datecreated desc";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, userid);
         ps.setInt(2, storeItemid);
         log.debug(ps);
         rs = ps.executeQuery();
         if (rs.next()) {
            siisd = new StoreItemInventorySummaryData(rs);
            siisd.setStoreItemData(this.getStoreItem(username, siisd.getStoreItemID()));
            StoreItemInventorySummaryData var10 = siisd;
            return var10;
         }

         siisd = null;
      } catch (Exception var31) {
         throw new EJBException(var31.getMessage(), var31);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var28) {
            conn = null;
         }

      }

      return siisd;
   }

   private List<StoreItemInventoryData> getStoreItemsInventory(int userid, int storeItemid) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceSlave.getConnection();
         String sql = "select  sii.* from storeiteminventory sii left join storeiteminventoryreceived siir on siir.storeiteminventoryid = sii.id,storeitem si where sii.userid = ? and si.id= sii.storeitemid and siir.id is null and sii.storeitemid=? order by sii.datecreated desc";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, userid);
         ps.setInt(2, storeItemid);
         log.debug(ps);
         rs = ps.executeQuery();
         ArrayList resultList = new ArrayList();

         while(rs.next()) {
            StoreItemInventoryData siid = new StoreItemInventoryData(rs);
            resultList.add(siid);
         }

         ArrayList var25 = resultList;
         return var25;
      } catch (Exception var23) {
         throw new EJBException(var23.getMessage(), var23);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }
   }

   public List<VirtualGiftData> getInventoryVirtualGifts(int userid) {
      List<VirtualGiftData> result = new ArrayList();
      List<StoreItemInventorySummaryData> siisd = this.getStoreItemsInventoryByType(userid, StoreItemData.TypeEnum.VIRTUAL_GIFT);
      Iterator i$ = siisd.iterator();

      while(i$.hasNext()) {
         StoreItemInventorySummaryData storeItemInventorySummaryData = (StoreItemInventorySummaryData)i$.next();
         result.add((VirtualGiftData)storeItemInventorySummaryData.getStoreItemData().referenceData);
      }

      return result;
   }

   public List<StoreItemInventorySummaryData> getStoreItemsInventoryByType(int userid, StoreItemData.TypeEnum type) {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         String username = userBean.getUsernameByUserid(userid, (Connection)null);
         String sql;
         if (username == null) {
            sql = null;
            return sql;
         } else {
            conn = this.dataSourceSlave.getConnection();
            sql = "select  sii.*,count(storeitemid) as count from storeiteminventory sii left join storeiteminventoryreceived siir on siir.storeiteminventoryid = sii.id,storeitem si where sii.userid = ? and si.id= sii.storeitemid and siir.id is null";
            if (type != null) {
               sql = sql + " and si.type=?";
            }

            sql = sql + " group by sii.storeitemid order by sii.datecreated desc";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userid);
            if (type != null) {
               ps.setInt(2, type.value());
            }

            log.debug(ps);
            rs = ps.executeQuery();
            ArrayList resultList = new ArrayList();

            while(rs.next()) {
               StoreItemInventorySummaryData siisd = new StoreItemInventorySummaryData(rs);
               siisd.setStoreItemData(this.getStoreItem(username, siisd.getStoreItemID()));
               resultList.add(siisd);
            }

            ArrayList var30 = resultList;
            return var30;
         }
      } catch (Exception var28) {
         throw new EJBException(var28.getMessage(), var28);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }
   }

   public Map<String, Integer> giveVirtualGiftForMultipleUsers(String buyerUsername, List<String> recipientUsernames, StoreItemData sid, int purchaseLocation, boolean privateGift, String message) throws EJBException, FusionEJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      message = StringUtil.stripHTML(message);
      if (recipientUsernames != null && !recipientUsernames.isEmpty()) {
         HashMap virtualGiftReceivedIdMap = new HashMap(recipientUsernames.size());

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            VirtualGiftData gift = (VirtualGiftData)sid.referenceData;
            List<StoreItemInventoryData> siids = this.getStoreItemsInventory(userBean.getUserID(buyerUsername, (Connection)null), sid.id);
            if (recipientUsernames.size() > siids.size()) {
               throw new EJBException("You don’t have any unlocked gift " + gift.getName() + ".");
            }

            connMaster = this.dataSourceMaster.getConnection();
            ps = connMaster.prepareStatement("insert into virtualgiftreceived (username, datecreated, purchaselocation, virtualgiftid, sender, private, message) values (?, now(), ?, ?, ?, ?, ?)", 1);

            for(int i = 0; i < recipientUsernames.size(); ++i) {
               String recipient = (String)recipientUsernames.get(i);
               userBean.getUserID(recipient, (Connection)null);
               ps.setString(1, recipient);
               ps.setInt(2, purchaseLocation);
               ps.setInt(3, gift.getId());
               ps.setString(4, buyerUsername);
               ps.setInt(5, privateGift ? 1 : 0);
               ps.setString(6, message);
               if (!isMutualGiftingHappening(buyerUsername, recipient)) {
                  MemCachedClientWrapper.add(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipient, "1", 10000L);
                  int rowsUpdated = ps.executeUpdate();
                  if (rowsUpdated != 1) {
                     throw new EJBException("Internal Server Error (Unable to record gift received)");
                  }

                  rs = ps.getGeneratedKeys();
                  if (!rs.next()) {
                     throw new EJBException("Internal Server Error (Unable to record gift received)");
                  }

                  int virtualGiftReceivedId = rs.getInt(1);
                  virtualGiftReceivedIdMap.put(recipient, virtualGiftReceivedId);
                  this.addStoreItemInventoryReceived(((StoreItemInventoryData)siids.get(i)).getID(), userBean.getUserID(recipient, (Connection)null), virtualGiftReceivedId, StoreItemData.TypeEnum.VIRTUAL_GIFT);
                  rs.close();

                  try {
                     if (!privateGift) {
                        if (SystemProperty.getBool("UseRedisDataStore", true)) {
                           try {
                              int recipientUserId = userBean.getUserID(recipient, (Connection)null);
                              GiftsReceivedCounter.incrementCacheCount(recipientUserId);
                           } catch (EJBException var36) {
                              log.error("Unable to increment gifts received counter for username [" + recipient + "]: " + var36);
                           }
                        } else {
                           MemCachedClientWrapper.incr(MemCachedKeySpaces.CommonKeySpace.NUM_VIRTUAL_GIFTS_RECEIVED, recipient);
                        }
                     }

                     MemCachedClientWrapper.delete(MemCachedKeySpaces.RateLimitKeySpace.VIRTUAL_GIFT_RATE_LIMIT, buyerUsername + "/" + recipient);
                  } catch (Exception var37) {
                     log.info("Failed to remove memcached key for concurrent mutual gifting due to: " + var37.getMessage());
                  }
               }
            }

            ps.close();
         } catch (SQLException var38) {
            throw new EJBException(var38.getMessage());
         } catch (CreateException var39) {
            throw new EJBException(var39.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var35) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var34) {
               ps = null;
            }

            try {
               if (connMaster != null) {
                  connMaster.close();
               }
            } catch (SQLException var33) {
               connMaster = null;
            }

         }

         if (virtualGiftReceivedIdMap.isEmpty()) {
            throw new FusionEJBException("Unable to perform operation at the moment. Please try again.");
         } else {
            return virtualGiftReceivedIdMap;
         }
      } else {
         throw new FusionEJBException("Please provide one or more recipient for this gift");
      }
   }

   private void giveReward(int rewardProgramId, String rewardProgramName, UserData userData, Collection<RewardedMigCreditData> rewardedMigCredits, long completionId, AccountEntrySourceData accountEntrySourceData, CreditRewardDetail creditRewardDetail) throws CreateException, SQLException {
      Money creditReward = creditRewardDetail.getCreditReward();
      if (creditReward != null && !creditReward.equals(Money.ZERO)) {
         String username = userData.username;
         String remarks = StringUtil.isBlank(creditRewardDetail.getRemarks()) ? "Completion of program " + rewardProgramName : creditRewardDetail.getRemarks();
         double migCreditAmount = creditReward.getAmount().doubleValue();
         String migCreditCcy = creditReward.getCurrency();
         this.giveMigCreditReward(accountEntrySourceData, completionId, username, remarks, migCreditAmount, migCreditCcy, rewardedMigCredits);
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CurrencyData rewardCurrencyData = misBean.getCurrency(migCreditCcy);
         double baseExchRate = rewardCurrencyData.exchangeRate;
         this.logExtraBasicOutcomeRewarded(completionId, 0, 0, migCreditAmount, migCreditCcy, baseExchRate);
      }
   }

   private void giveReward(int rewardProgramId, UserData userData, Collection<RewardedReputationData> rewardedReputations, long completionId, ReputationRewardDetail reputationRewardDetail) throws Exception {
      this.giveReputationReward(rewardProgramId, userData, reputationRewardDetail.getLevelReward(), reputationRewardDetail.getScoreReward(), completionId, rewardedReputations);
      this.logExtraBasicOutcomeRewarded(completionId, reputationRewardDetail.getScoreReward(), reputationRewardDetail.getLevelReward(), Money.ZERO.getAmount().doubleValue(), Money.ZERO.getCurrency(), 0.0D);
   }

   private void giveReward(int rewardProgramId, UserData userData, long completionId, MerchantPointsRewardDetail merchantPointRewardDetail) {
      this.giveMerchantRewardPoints(rewardProgramId, userData, merchantPointRewardDetail.getPoints(), completionId);
   }

   private void giveReward(int rewardProgramId, UserData userData, Collection<RewardedBadgeData> rewardedBadges, long completionId, BadgeRewardDetail badgeReward) throws CreateException, SQLException, MigboApiUtil.MigboApiException {
      this.giveBadgeRewards(rewardProgramId, badgeReward.getBadgeIds(), userData, completionId, rewardedBadges);
   }

   private void giveReward(int rewardProgramId, UserData userData, Collection<RewardedGroupMembershipData> rewardedGroupMemberships, long completionId, AccountEntrySourceData accountEntrySourceData, GroupMembershipRewardDetail groupMembershipReward) throws EJBException, UnknownHostException, CreateException, SQLException {
      this.giveGroupMembershipRewards((Collection)groupMembershipReward.getGroupIds(), userData, completionId, accountEntrySourceData, rewardedGroupMemberships);
   }

   private void giveReward(int rewardProgramId, UserData userData, Collection<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems, long completionId, UnlockedStoreItemRewardDetail unlockedStoreItemRewardDetail) throws SQLException {
      ArrayList<StoreItemToUnlockData> storeItemToUnlockList = new ArrayList();
      Iterator i$ = unlockedStoreItemRewardDetail.getStoreItemIdToUnlockCounts().entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Integer, Integer> storeItemToUnlockEntry = (Entry)i$.next();
         storeItemToUnlockList.add(new StoreItemToUnlockData((Integer)storeItemToUnlockEntry.getKey(), (Integer)storeItemToUnlockEntry.getValue()));
      }

      this.giveUnlockedStoreItemRewards(storeItemToUnlockList, RewardProgramData.ItemRewardType.fromValue(unlockedStoreItemRewardDetail.getItemRewardMethodType().typeId), userData, completionId, rewardedUnlockedStoreItems);
   }

   private void giveReward(int rewardProgramId, String rewardProgramName, UserData userData, Collection<RewardedStoreItemData> rewardedStoreItems, long completionId, StoreItemRewardDetail storeItemRewardDetail) throws SQLException {
      this.giveStoreItemRewards(rewardProgramId, rewardProgramName, storeItemRewardDetail.getStoreItemIds(), RewardProgramData.ItemRewardType.fromValue(storeItemRewardDetail.getItemRewardMethodType().typeId), userData, completionId, rewardedStoreItems);
   }

   public void giveRewards(MMv2Outcomes outcomes, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      try {
         int rewardProgramID = Integer.parseInt(outcomes.getRuleId());
         int userID = Integer.parseInt(outcomes.getSubjectId());
         RewardProgramData programData = RewardCentre.getInstance().getRewardProgram(rewardProgramID);
         if (programData == null) {
            throw new EJBException("Invalid reward program ID " + rewardProgramID);
         } else {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData userData = userBean.loadUserFromID(userID);
            if (userData == null) {
               throw new EJBException("Invalid user ID " + userID);
            } else {
               log.debug("begin awardUser(...)");
               ContentLocal contentEJB = (ContentLocal)EJBHomeCache.getLocalObject("ContentLocal", ContentLocalHome.class);
               RewardProgramCompletionData rewardCompletionData = contentEJB.awardUser(programData, userData, accountEntrySourceData, outcomes.getOutcomeDetails());
               log.debug("end awardUser(...)");
               log.debug("begin dispatch reward program completion trigger");
               this.sendRewardProgramCompletionMechanicTrigger(rewardCompletionData);
               log.debug("end dispatch reward program completion trigger");
               log.debug("begin dispatch reputation rewarded trigger");
               this.sendRewardedReputationTriggers(rewardCompletionData);
               log.debug("end dispatch reputation rewarded trigger");
               this.sendRewardCompletionUserNotification(rewardCompletionData, outcomes.getNotifications());
            }
         }
      } catch (CreateException var10) {
         throw new EJBException("Create exception occurred.Exception:" + var10, var10);
      }
   }

   private void sendRewardCompletionUserNotification(RewardProgramCompletionData rewardCompletionData, List<Notification> notifications) {
      RewardProgramData rewardProgramData = rewardCompletionData.getRewardProgramData();
      UserData userData = rewardCompletionData.getUserData();
      long completionId = rewardCompletionData.getCompletionid();
      Iterator i$ = notifications.iterator();

      while(i$.hasNext()) {
         Notification notification = (Notification)i$.next();

         try {
            this.sendRewardCompletionUserNotification(completionId, rewardProgramData, userData, notification);
         } catch (Exception var10) {
            log.warn("Failed to send notification for program:[" + rewardProgramData.id + "].UserId:[" + userData.userID + "].Notification:[" + StringUtil.truncateWithEllipsis(notification.toString(), 250) + "].Exception:" + var10, var10);
         }
      }

   }

   private void sendRewardCompletionUserNotification(long completionId, RewardProgramData rewardProgramData, UserData userData, Notification notification) throws JSONException, MigboApiUtil.MigboApiException {
      switch(notification.getNotificationType()) {
      case 1:
         this.sendRewardCompletionUserNotification(rewardProgramData.id, userData.username, (IMNotification)notification);
         break;
      case 2:
         this.sendRewardCompletionUserNotification(completionId, rewardProgramData.id, userData, (MigAlertMessageNotification)notification);
         break;
      case 3:
         this.sendRewardCompletionUserNotification(completionId, rewardProgramData.id, userData, (MiniblogTextNotification)notification);
         break;
      case 4:
         this.sendRewardCompletionUserNotification(completionId, rewardProgramData.id, userData, (SMSNotification)notification);
         break;
      case 5:
         this.sendRewardCompletionUserNotification(rewardProgramData.id, userData, (TemplatedEmailNotification)notification);
         break;
      case 6:
         this.sendRewardCompletionUserNotification(rewardProgramData.id, userData, (TextEmailNotification)notification);
         break;
      default:
         log.error("Unsupported notification type:[" + notification.getNotificationType() + "]. Type:" + notification.getClass().getName() + "]");
      }

   }

   private void sendRewardCompletionUserNotification(int rewardprogramid, String username, IMNotification notification) {
      this.sendIMNotification(rewardprogramid, username, notification.getContent());
   }

   private void sendRewardCompletionUserNotification(long completionID, int rewardprogramid, UserData userData, SMSNotification notification) {
      this.sendSMSNotification(rewardprogramid, notification.getContent(), userData, completionID);
   }

   private void sendRewardCompletionUserNotification(int rewardprogramid, UserData userData, TextEmailNotification notification) {
      this.sendStaticContentEmailNotification(rewardprogramid, notification.getSubject(), notification.getContent(), userData);
   }

   private void sendRewardCompletionUserNotification(int rewardprogramid, UserData userData, TemplatedEmailNotification notification) {
      this.sendTemplatizedEmailNotification(userData.userID, (int)notification.getTemplateId(), notification.getTemplateData());
   }

   private void sendRewardCompletionUserNotification(long completionId, int rewardprogramid, UserData userData, MigAlertMessageNotification notification) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("triggering MigAlertMessageNotification for [" + userData.userID + "] Notification:[" + notification + "]");
         }

         UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
         Map<String, String> parameters = new HashMap(notification.getParameters());
         String msgKey = StringUtil.isBlank(notification.getMsgKey()) ? String.valueOf(completionId) : notification.getMsgKey();
         String fqdnMsgKey = StringUtil.isBlank(notification.getMsgSubKey()) ? msgKey : msgKey + "[" + notification.getMsgSubKey() + "]";
         unsProxy.notifyFusionUser(new Message(fqdnMsgKey, userData.userID, userData.username, notification.getAlertType() == null ? Enums.NotificationTypeEnum.SYS_ALERT.getType() : notification.getAlertType(), System.currentTimeMillis(), parameters));
      } catch (Exception var10) {
         log.error("Unknown error while contacting UNS to generate MigAlertMessageNotification alert.Exception:" + var10, var10);
      }

   }

   private void sendRewardCompletionUserNotification(long completionID, int rewardprogramid, UserData userData, MiniblogTextNotification notification) throws JSONException, MigboApiUtil.MigboApiException {
      String actionLogMessage = "reward completion notification on miniblog for rewardProgram:[" + rewardprogramid + "] completionID:[" + completionID + "] userID:[" + userData.userID + "].";

      try {
         JSONObject result = this.createMigboTextPost(userData.userID, notification.getContent(), (String)null, (String)null, String.valueOf(MigboEnums.MigboPostOriginalityEnum.ORIGINAL.getType()), MigboEnums.PostApplicationEnum.SYSTEM);
         log.info("Posting " + actionLogMessage + " Result:[" + result + "]");
      } catch (Exception var8) {
         log.error("Failed posting " + actionLogMessage + ".Exception:[" + var8 + "]", var8);
      }

   }

   public RewardProgramCompletionData awardUser(RewardProgramData programData, UserData userData, AccountEntrySourceData accountEntrySourceData, List<OutcomeDetail> outcomeDetails) throws EJBException {
      try {
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.PARTICIPATE_IN_MARKETING_MECHANICS, userData) && SystemProperty.getBool("RewardProgramDisabledForUnauthenticatedUsers", false)) {
            throw new EJBException("Unable to give reward to [" + userData.username + "] user is non-authenticated. ProgramID [" + programData.id + "]");
         } else {
            ArrayList<RewardedReputationData> rewardedReputations = new ArrayList();
            ArrayList<RewardedMigCreditData> rewardedMigCredits = new ArrayList();
            ArrayList<RewardedStoreItemData> rewardedStoreItems = new ArrayList();
            ArrayList<RewardedBadgeData> rewardedBadges = new ArrayList();
            ArrayList<RewardedGroupMembershipData> rewardedGroupMemberships = new ArrayList();
            ArrayList<RewardedUnlockedStoreItemData> rewardedUnlockedStoreItems = new ArrayList();
            ArrayList<NotificationTemplateOutcomeData> notificationTemplates = new ArrayList();
            Timestamp rewardedTime = new Timestamp(System.currentTimeMillis());
            long completionID = this.logRewardProgramCompletion(rewardedTime, programData.id, userData.userID, 0, 0, 0.0D, Money.ZERO.getCurrency());
            Iterator i$ = outcomeDetails.iterator();

            while(i$.hasNext()) {
               OutcomeDetail outcomeDetail = (OutcomeDetail)i$.next();
               switch(outcomeDetail.getOutcomeDetailType()) {
               case 1:
                  this.giveReward(programData.id, userData, rewardedBadges, completionID, (BadgeRewardDetail)((BadgeRewardDetail)outcomeDetail));
                  break;
               case 2:
                  this.giveReward(programData.id, programData.name, userData, rewardedMigCredits, completionID, accountEntrySourceData, (CreditRewardDetail)outcomeDetail);
                  break;
               case 3:
                  this.giveReward(programData.id, userData, rewardedReputations, completionID, (ReputationRewardDetail)((ReputationRewardDetail)outcomeDetail));
                  break;
               case 4:
                  this.giveReward(programData.id, programData.name, userData, rewardedStoreItems, completionID, (StoreItemRewardDetail)outcomeDetail);
                  break;
               case 5:
                  this.giveReward(programData.id, userData, rewardedUnlockedStoreItems, completionID, (UnlockedStoreItemRewardDetail)((UnlockedStoreItemRewardDetail)outcomeDetail));
                  break;
               case 6:
                  this.giveReward(programData.id, userData, completionID, (MerchantPointsRewardDetail)outcomeDetail);
                  break;
               case 7:
                  this.giveReward(programData.id, userData, rewardedGroupMemberships, completionID, accountEntrySourceData, (GroupMembershipRewardDetail)outcomeDetail);
                  break;
               default:
                  log.error("Unsupported outcomedetail type:[" + outcomeDetail.getOutcomeDetailType() + "]. Type:" + outcomeDetail.getClass().getName() + "]");
               }
            }

            return new RewardProgramCompletionData(userData, programData, completionID, rewardedTime, rewardedReputations, rewardedMigCredits, rewardedStoreItems, rewardedBadges, rewardedGroupMemberships, rewardedUnlockedStoreItems, notificationTemplates, accountEntrySourceData);
         }
      } catch (EJBException var17) {
         throw var17;
      } catch (Exception var18) {
         throw new EJBException("Failed to awardUser[" + userData.userID + "] programId:[" + programData + "].Exception:" + var18, var18);
      }
   }

   public static class SingletonHolder {
      private static final LazyLoader<SortedSet<Integer>> EMOTICON_HEIGHTS_LOADER = new LazyLoader<SortedSet<Integer>>("EMOTICON_HEIGHTS_LOADER", Long.MAX_VALUE) {
         protected SortedSet<Integer> fetchValue() throws SQLException {
            DataSource dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
            return ContentBean.fetchEmoticonHeightsFromDB(dataSourceSlave);
         }
      };
      private static final LazyLoader<Set<Short>> DISPATCHABLE_REFERRED_USER_REWARDED_TRIGGER_TYPES_LOADER = new LazyLoader<Set<Short>>("DISPATCHABLE_REFERRED_USER_REWARDED_TRIGGER_TYPES_LOADER", 60000L) {
         protected Set<Short> fetchValue() throws SQLException {
            Set<Short> set = new HashSet();
            short[] arr$ = SystemProperty.getShortArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DISPATCHABLE_REFERRED_USER_REWARDED_TRIGGER_TYPES);
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Short triggerType = arr$[i$];
               set.add(triggerType);
            }

            return set;
         }
      };
      private static final LazyLoader<Set<Short>> DISPATCHABLE_USER_REWARDED_TRIGGER_TYPES_LOADER = new LazyLoader<Set<Short>>("DISPATCHABLE_USER_REWARDED_TRIGGER_TYPES_LOADER", 60000L) {
         protected Set<Short> fetchValue() throws SQLException {
            Set<Short> set = new HashSet();
            short[] arr$ = SystemProperty.getShortArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MechanicsEngineSettings.DISPATCHABLE_USER_REWARDED_TRIGGER_TYPES);
            int len$ = arr$.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               Short triggerType = arr$[i$];
               set.add(triggerType);
            }

            return set;
         }
      };
      private static final LazyLoader<Set<Integer>> PASS_THROUGH_EMAIL_TEMPLATE_IDS = new LazyLoader<Set<Integer>>("PASS_THROUGH_EMAIL_TEMPLATE_IDS", 60000L) {
         protected Set<Integer> fetchValue() throws Exception {
            int[] passThruEmailTemplateIds = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Email.PASS_THROUGH_EMAIL_TEMPLATE_IDS);
            if (passThruEmailTemplateIds != null && passThruEmailTemplateIds.length > 0) {
               HashSet<Integer> passThruEmailTemplateIdSet = new HashSet();
               int[] arr$ = passThruEmailTemplateIds;
               int len$ = passThruEmailTemplateIds.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  int templateId = arr$[i$];
                  passThruEmailTemplateIdSet.add(templateId);
               }

               return Collections.unmodifiableSet(passThruEmailTemplateIdSet);
            } else {
               return Collections.EMPTY_SET;
            }
         }
      };

      public static LazyLoader<SortedSet<Integer>> getEmoticonHeightsLoader() {
         return EMOTICON_HEIGHTS_LOADER;
      }

      public static LazyLoader<Set<Short>> getDispatchableReferredUserRewardedTriggerTypes() {
         return DISPATCHABLE_REFERRED_USER_REWARDED_TRIGGER_TYPES_LOADER;
      }

      public static LazyLoader<Set<Short>> getDispatchableUserRewardedTriggerTypes() {
         return DISPATCHABLE_USER_REWARDED_TRIGGER_TYPES_LOADER;
      }

      public static LazyLoader<Set<Integer>> getPassThroughEmailTemplateIds() {
         return PASS_THROUGH_EMAIL_TEMPLATE_IDS;
      }
   }
}
