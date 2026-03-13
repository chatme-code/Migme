package com.projectgoth.fusion.userevent.system;

import Ice.Communicator;
import Ice.Current;
import Ice.ObjectPrx;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.RequestAndRateLongCounter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.dao.BroadcastListDAO;
import com.projectgoth.fusion.dao.DisplayPictureAndStatusMessageDAO;
import com.projectgoth.fusion.dao.GroupAnnouncementDAO;
import com.projectgoth.fusion.dao.GroupDAO;
import com.projectgoth.fusion.dao.GroupMembershipDAO;
import com.projectgoth.fusion.dao.UserPostDAO;
import com.projectgoth.fusion.dao.UserProfileStatusDAO;
import com.projectgoth.fusion.data.GroupAnnouncementData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.UserPostData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.slice.AddingFriendUserEventIce;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.CreatedChatroomUserEventIce;
import com.projectgoth.fusion.slice.EventPrivacySettingIce;
import com.projectgoth.fusion.slice.EventStorePrx;
import com.projectgoth.fusion.slice.EventStorePrxHelper;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GenericApplicationUserEventIce;
import com.projectgoth.fusion.slice.GiftShowerUserEventIce;
import com.projectgoth.fusion.slice.GroupAnnouncementUserEventIce;
import com.projectgoth.fusion.slice.GroupDonationUserEventIce;
import com.projectgoth.fusion.slice.GroupJoinedUserEventIce;
import com.projectgoth.fusion.slice.GroupUserEventIce;
import com.projectgoth.fusion.slice.GroupUserPostUserEventIce;
import com.projectgoth.fusion.slice.PhotoUploadedUserEventIce;
import com.projectgoth.fusion.slice.ProfileUpdatedUserEventIce;
import com.projectgoth.fusion.slice.PurchasedVirtualGoodsUserEventIce;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.ShortTextStatusUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.slice.UserWallPostUserEventIce;
import com.projectgoth.fusion.slice.VirtualGiftUserEventIce;
import com.projectgoth.fusion.slice._EventSystemDisp;
import com.projectgoth.fusion.userevent.domain.EventPrivacySetting;
import com.projectgoth.fusion.userevent.domain.UserEventType;
import com.projectgoth.fusion.userevent.system.domain.UsernameAndUserEvent;
import com.projectgoth.fusion.userevent.system.domain.UsernameAndUserEvents;
import com.projectgoth.fusion.userevent.system.loadbalancing.ConsistentHash;
import com.projectgoth.fusion.userevent.system.loadbalancing.FNVHashFunction;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.util.StringUtils;

public class EventSystemI extends _EventSystemDisp implements InitializingBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventSystemI.class));
   private boolean slimVersion = true;
   public static final String KEY_VALUE_SEPERATOR = "**";
   private int virtualNodes = 200;
   private int storeQueueLimit = 10000;
   private int maxDistributionThreadPoolSize = 20;
   private int maxStoreThreadPoolSize = 20;
   private List<String> eventStores;
   private BroadcastListDAO broadcastListDAO;
   private DisplayPictureAndStatusMessageDAO displayPictureAndStatusMessageDAO;
   private UserProfileStatusDAO userProfileStatusDAO;
   private GroupDAO groupDAO;
   private GroupAnnouncementDAO groupAnnouncementDAO;
   private UserPostDAO userPostDAO;
   private GroupMembershipDAO groupMembershipDAO;
   private RegistryPrx registryProxy;
   private RequestAndRateLongCounter setProfileStatusCounter;
   private RequestAndRateLongCounter madePhotoPublicCounter;
   private RequestAndRateLongCounter createdPublicChatrooomStatusCounter;
   private RequestAndRateLongCounter addedFriendCounter;
   private RequestAndRateLongCounter updatedProfileCounter;
   private RequestAndRateLongCounter purchasedVirtualGoodsCounter;
   private RequestAndRateLongCounter virtualGiftCounter;
   private RequestAndRateLongCounter giftShowerEventCounter;
   private RequestAndRateLongCounter userWallPostCounter;
   private RequestAndRateLongCounter genericApplicationEventCounter;
   private RequestAndRateLongCounter groupDonationCounter;
   private RequestAndRateLongCounter groupJoinedCounter;
   private RequestAndRateLongCounter groupAnnouncementCounter;
   private RequestAndRateLongCounter groupUserPostCounter;
   private RequestAndRateLongCounter totalCounter;
   private RequestAndRateLongCounter droppedEventCounter;
   private RequestAndRateLongCounter streamedEventsCounter;
   private RequestAndRateLongCounter distributedEventsCounter;
   private ConsistentHash<String> consistentHash;
   private Map<String, EventStorePrx> eventStoreProxies;
   private Map<String, ExecutorService> eventStoreExecutors;
   private ExecutorService distributionService;
   private BufferedWriter userEventStatsLogger;
   private BufferedWriter generatorEventStatsLogger;
   private CacheManager cacheManager;
   private SelfPopulatingCache groupCache;
   private SelfPopulatingCache groupAnnouncementCache;
   private SelfPopulatingCache groupTopicForUserPostCache;

   public EventSystemI() throws IOException {
      this.distributionService = (ThreadPoolExecutor)Executors.newFixedThreadPool(this.maxDistributionThreadPoolSize);
      FileInputStream fis = new FileInputStream((new File(System.getProperty("config.dir", "/usr/fusion/etc/") + "ehcache.evt.xml")).getAbsolutePath());

      try {
         this.cacheManager = new CacheManager(fis);
      } finally {
         fis.close();
      }

   }

   public void afterPropertiesSet() throws Exception {
      this.addedFriendCounter = new RequestAndRateLongCounter(5);
      this.createdPublicChatrooomStatusCounter = new RequestAndRateLongCounter(5);
      this.madePhotoPublicCounter = new RequestAndRateLongCounter(5);
      this.purchasedVirtualGoodsCounter = new RequestAndRateLongCounter(5);
      this.setProfileStatusCounter = new RequestAndRateLongCounter(5);
      this.updatedProfileCounter = new RequestAndRateLongCounter(5);
      this.virtualGiftCounter = new RequestAndRateLongCounter(5);
      this.giftShowerEventCounter = new RequestAndRateLongCounter(5);
      this.userWallPostCounter = new RequestAndRateLongCounter(5);
      this.groupAnnouncementCounter = new RequestAndRateLongCounter(5);
      this.groupDonationCounter = new RequestAndRateLongCounter(5);
      this.groupJoinedCounter = new RequestAndRateLongCounter(5);
      this.groupUserPostCounter = new RequestAndRateLongCounter(5);
      this.genericApplicationEventCounter = new RequestAndRateLongCounter(5);
      this.totalCounter = new RequestAndRateLongCounter(1);
      this.droppedEventCounter = new RequestAndRateLongCounter(1);
      this.streamedEventsCounter = new RequestAndRateLongCounter(1);
      this.distributedEventsCounter = new RequestAndRateLongCounter(1);
      this.consistentHash = new ConsistentHash(new FNVHashFunction(), this.virtualNodes, this.eventStores);
      if (this.slimVersion) {
         this.userEventStatsLogger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("userevents.log", true), "UTF-8"));
         this.generatorEventStatsLogger = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("generatorevents.log", true), "UTF-8"));
      }

      this.groupCache = new SelfPopulatingCache(this.cacheManager.getCache("groupCache"), new GroupCacheEntryFactory(this.groupDAO));
      this.groupAnnouncementCache = new SelfPopulatingCache(this.cacheManager.getCache("groupAnnouncementCache"), new GroupAnnouncementCacheEntryFactory(this.groupAnnouncementDAO));
      this.groupTopicForUserPostCache = new SelfPopulatingCache(this.cacheManager.getCache("groupTopicForUserPostCache"), new GroupTopicForUserPostCacheEntryFactory(this.userPostDAO));
   }

   public void setMaxDistributionThreadPoolSize(int maxDistributionThreadPoolSize) {
      this.maxDistributionThreadPoolSize = maxDistributionThreadPoolSize;
   }

   public void setMaxStoreThreadPoolSize(int maxStoreThreadPoolSize) {
      this.maxStoreThreadPoolSize = maxStoreThreadPoolSize;
   }

   public void setVirtualNodes(int virtualNodes) {
      this.virtualNodes = virtualNodes;
   }

   public void setStoreQueueLimit(int storeQueueLimit) {
      this.storeQueueLimit = storeQueueLimit;
   }

   @Required
   public void setEventStores(List<String> eventStores) {
      this.eventStores = eventStores;
   }

   @Required
   public void setBroadcastListDAO(BroadcastListDAO broadcastDAO) {
      this.broadcastListDAO = broadcastDAO;
   }

   public DisplayPictureAndStatusMessageDAO getDisplayPictureAndStatusMessageDAO() {
      return this.displayPictureAndStatusMessageDAO;
   }

   @Required
   public void setDisplayPictureAndStatusMessageDAO(DisplayPictureAndStatusMessageDAO displayPictureAndStatusMessageDAO) {
      this.displayPictureAndStatusMessageDAO = displayPictureAndStatusMessageDAO;
   }

   @Required
   public void setUserProfileStatusDAO(UserProfileStatusDAO userProfileStatusDAO) {
      this.userProfileStatusDAO = userProfileStatusDAO;
   }

   @Required
   public void setGroupDAO(GroupDAO groupDAO) {
      this.groupDAO = groupDAO;
   }

   @Required
   public void setGroupAnnouncementDAO(GroupAnnouncementDAO groupAnnouncementDAO) {
      this.groupAnnouncementDAO = groupAnnouncementDAO;
   }

   @Required
   public void setUserPostDAO(UserPostDAO userPostDAO) {
      this.userPostDAO = userPostDAO;
   }

   @Required
   public void setGroupMembershipDAO(GroupMembershipDAO groupMembershipDAO) {
      this.groupMembershipDAO = groupMembershipDAO;
   }

   public SelfPopulatingCache getGroupCache() {
      return this.groupCache;
   }

   public SelfPopulatingCache getGroupAnnouncementCache() {
      return this.groupAnnouncementCache;
   }

   public SelfPopulatingCache getGroupTopicForUserPostCache() {
      return this.groupTopicForUserPostCache;
   }

   public RegistryPrx getRegistryProxy() {
      return this.registryProxy;
   }

   public void setRegistryProxy(RegistryPrx registryProxy) {
      this.registryProxy = registryProxy;
   }

   public void setSlimVersion(boolean distributeEvents) {
      this.slimVersion = distributeEvents;
   }

   private EventStorePrx proxyForProxyString(String proxyString) {
      return (EventStorePrx)this.eventStoreProxies.get(proxyString);
   }

   private Set<String> fusionBroadcastListForUsername(String username) {
      Set<String> broadcastList = this.broadcastListDAO.getBroadcastListForUser(username);
      return broadcastList;
   }

   private Set<String> fusionBroadcastListForGroup(int groupId) {
      return this.broadcastListDAO.getBroadcastListForGroup(groupId);
   }

   private Set<String> fusionBroadcastListForTopic(int userPostId) {
      return new HashSet(this.groupMembershipDAO.getGroupMemberUsernamesForNewGroupUserPostNotificationViaEventSystem(userPostId));
   }

   private boolean profileIsPrivate(String username) {
      UserProfileData.StatusEnum status = this.userProfileStatusDAO.getUserProfileStatus(username);
      return status == UserProfileData.StatusEnum.PRIVATE;
   }

   private boolean isEventBeingGenerated(UserEventIce userEvent) {
      if (this.profileIsPrivate(userEvent.generatingUsername)) {
         if (log.isDebugEnabled()) {
            log.debug("not generating events for private user [" + userEvent.generatingUsername + "]");
         }

         return false;
      } else {
         EventPrivacySettingIce mask = null;

         try {
            mask = this.getPublishingPrivacyMask(userEvent.generatingUsername);
            if (!EventPrivacySetting.fromEventPrivacySettingIce(mask).applyMask(userEvent)) {
               if (log.isDebugEnabled()) {
                  log.debug("publishing mask [" + mask + "] for user [" + userEvent.generatingUsername + "] prohibits this event [" + userEvent + "]");
               }

               return false;
            } else {
               return true;
            }
         } catch (FusionException var4) {
            log.error("failed to get publishing mask for user [" + userEvent.generatingUsername + "]", var4);
            return false;
         }
      }
   }

   private Set<String> defaultDistributionList(UserEventIce userEvent) {
      return this.fusionBroadcastListForUsername(userEvent.generatingUsername);
   }

   private void processGeneratorEvent(UserEventIce userEvent) {
      UsernameAndUserEvent generatorEvent = new UsernameAndUserEvent(userEvent.generatingUsername, userEvent);
      if (!this.slimVersion) {
         this.storeGeneratorEvent(generatorEvent);
      } else {
         this.logEvent(generatorEvent);
      }

   }

   private void storeAndDistributeEvents(Set<String> distributionList, UserEventIce userEvent) {
      Iterator i$ = distributionList.iterator();

      while(i$.hasNext()) {
         String username = (String)i$.next();
         if (!this.slimVersion) {
            String proxyString = (String)this.consistentHash.get(username);
            this.storeEvent(new UsernameAndUserEvent(username, userEvent), proxyString);
            this.distributeEvent(new UsernameAndUserEvent(username, (UserEventIce)userEvent.clone()), proxyString);
         } else {
            this.logEvent(new UsernameAndUserEvent(username, userEvent));
         }

         if (log.isDebugEnabled()) {
            log.debug("Done processing event for user [" + username + "]");
         }
      }

   }

   private void defaultEventProcess(UserEventIce userEvent) {
      if (this.isEventBeingGenerated(userEvent)) {
         this.processGeneratorEvent(userEvent);
         this.storeAndDistributeEvents(this.defaultDistributionList(userEvent), userEvent);
      }
   }

   private void storeGeneratorEvent(UsernameAndUserEvent event) {
      String proxyString = (String)this.consistentHash.get(event.getUsername());
      if (log.isDebugEnabled()) {
         log.debug("Submitting generator event for generator [" + event.getUserEvent().generatingUsername + "] and user [" + event.getUsername() + "] to proxy [" + proxyString + "]");
      }

      StoreGeneratorTask task = new StoreGeneratorTask(event, this.proxyForProxyString(proxyString));
      ((ExecutorService)this.eventStoreExecutors.get(proxyString)).execute(task);
   }

   private void storeEvent(UsernameAndUserEvent event, String proxyString) {
      StoreTask task = new StoreTask(event, this.proxyForProxyString(proxyString));
      if (log.isDebugEnabled()) {
         log.debug("Submitting event [" + event.getUserEvent() + "] and user [" + event.getUsername() + "] to proxy [" + proxyString + "]");
      }

      if (((ThreadPoolExecutor)this.eventStoreExecutors.get(proxyString)).getQueue().size() > this.storeQueueLimit) {
         this.droppedEventCounter.add();
         if (log.isDebugEnabled()) {
            log.debug("dropping event since we have exceeded the queue limit [" + this.storeQueueLimit + "]");
         }
      } else {
         ((ExecutorService)this.eventStoreExecutors.get(proxyString)).execute(task);
      }

   }

   private void logEvent(UsernameAndUserEvent event) {
      StringBuilder builder = new StringBuilder(System.currentTimeMillis() + ",");
      builder.append(event.getUsername()).append(",").append(event.getUserEvent()).append(",").append(event.getUserEvent().timestamp);
      if (StringUtils.hasLength(event.getUserEvent().generatingUsername)) {
         builder.append(",").append(event.getUserEvent().generatingUsername);
      }

      builder.append("\n");

      try {
         if (log.isDebugEnabled()) {
            log.debug("logging event to disk for user " + event.getUsername() + "");
         }

         if (StringUtils.hasLength(event.getUserEvent().generatingUsername)) {
            this.userEventStatsLogger.write(builder.toString());
         } else {
            this.generatorEventStatsLogger.write(builder.toString());
         }
      } catch (IOException var4) {
         log.error("failed to write", var4);
      }

   }

   private void distributeEvent(UsernameAndUserEvent event, String proxyString) {
      if (log.isDebugEnabled()) {
         log.debug("Submitting event [" + event.getUserEvent() + "] to distribution service");
      }

      DistributionTask task = new DistributionTask(event, this, this.proxyForProxyString(proxyString));
      this.distributionService.execute(task);
   }

   public void streamEventsToLoggingInUser(String username, ConnectionPrx connectionProxy, Current __current) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("going to stream for user [" + username + "]");
      }

      if (!this.slimVersion) {
         EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
         if (proxy == null) {
            log.error("Failed to find an EventStore proxy for user [" + username + "], not streaming anything");
         } else {
            if (log.isDebugEnabled()) {
               log.debug("found EventStore proxy [" + proxy + "] for username [" + username + "]");
            }

            UserEventIce[] events = proxy.getUserEventsForUser(username);
            if (log.isDebugEnabled()) {
               log.debug("got " + (events == null ? 0 : events.length) + " events for user [" + username + "]");
            }

            if (events != null && events.length > 0) {
               StreamTask streamTask = new StreamTask(new UsernameAndUserEvents(username, events), connectionProxy, this);
               this.distributionService.execute(streamTask);
            }

            if (log.isDebugEnabled()) {
               log.debug("sent stream for user [" + username + "]");
            }

         }
      }
   }

   public UserEventIce[] getUserEventsForUser(String username, Current __current) throws FusionException {
      EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
      UserEventIce[] events = proxy.getUserEventsForUser(username);
      UserEventIce[] arr$ = events;
      int len$ = events.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         UserEventIce event = arr$[i$];
         this.assignRuntimeValues(event);
      }

      return events;
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Current __current) throws FusionException {
      EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
      UserEventIce[] events = proxy.getUserEventsGeneratedByUser(username);
      UserEventIce[] arr$ = events;
      int len$ = events.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         UserEventIce event = arr$[i$];
         this.assignRuntimeValues(event);
      }

      return events;
   }

   public void updateAllowList(String username, String[] watchers, Current __current) throws FusionException {
      throw new FusionException("Not implemented");
   }

   public void assignRuntimeValues(UserEventIce event) {
      try {
         if (event instanceof GroupUserEventIce) {
            int groupId = ((GroupUserEventIce)event).groupId;
            Element element = this.getGroupCache().get(groupId);
            if (element != null) {
               ((GroupUserEventIce)event).groupName = ((GroupData)element.getObjectValue()).name;
               if (log.isDebugEnabled()) {
                  log.debug("set group name to [" + ((GroupUserEventIce)event).groupName + "]");
               }
            } else {
               log.error("failed to find group [" + groupId + "] details from group cache!");
            }

            int userPostId;
            if (event instanceof GroupAnnouncementUserEventIce) {
               userPostId = ((GroupAnnouncementUserEventIce)event).groupAnnouncementId;
               element = this.getGroupAnnouncementCache().get(userPostId);
               if (element != null) {
                  ((GroupAnnouncementUserEventIce)event).groupAnnouncementTitle = ((GroupAnnouncementData)element.getObjectValue()).title;
                  if (log.isDebugEnabled()) {
                     log.debug("set group announcement title to [" + ((GroupAnnouncementUserEventIce)event).groupAnnouncementTitle + "]");
                  }
               } else {
                  log.warn("failed to find announcement [" + userPostId + "] details from group announcement cache!");
               }
            }

            if (event instanceof GroupUserPostUserEventIce) {
               userPostId = ((GroupUserPostUserEventIce)event).userPostId;
               element = this.getGroupTopicForUserPostCache().get(userPostId);
               if (element != null) {
                  ((GroupUserPostUserEventIce)event).topicText = ((UserPostData)element.getObjectValue()).body;
                  ((GroupUserPostUserEventIce)event).topicId = ((UserPostData)element.getObjectValue()).id;
                  if (((GroupUserPostUserEventIce)event).topicText.length() > 35) {
                     ((GroupUserPostUserEventIce)event).topicText = ((GroupUserPostUserEventIce)event).topicText.substring(0, 34) + "...";
                  }

                  if (log.isDebugEnabled()) {
                     log.debug("set group topic text to [" + ((GroupUserPostUserEventIce)event).topicText + "]");
                  }
               } else {
                  log.warn("failed to find UserPostData [" + userPostId + "] details from GroupTopicForUserPostCache!");
               }
            }
         }
      } catch (Exception var5) {
         log.error("failed to populate event [" + event + "]", var5);
      }

   }

   public void addedFriend(String username, String friend, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.ADDING_FRIEND.toString() + "] event for user [" + username + "] and friend [" + friend + "]");
         }

         AddingFriendUserEventIce userEvent = new AddingFriendUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.friend1 = friend;
         this.defaultEventProcess(userEvent);
      } finally {
         this.addedFriendCounter.add();
         this.totalCounter.add();
      }

   }

   public void createdPublicChatroom(String username, String chatroomName, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.CREATE_PUBLIC_CHATROOM.toString() + "] event for user [" + username + "] and chatroom [" + chatroomName + "]");
         }

         CreatedChatroomUserEventIce event = new CreatedChatroomUserEventIce();
         event.generatingUsername = username;
         event.timestamp = System.currentTimeMillis();
         event.chatroom = chatroomName;
         this.defaultEventProcess(event);
      } finally {
         this.createdPublicChatrooomStatusCounter.add();
         this.totalCounter.add();
      }

   }

   public void madePhotoPublic(String username, int scrapbookid, String title, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.PHOTO_UPLOAD_WITH_TITLE.toString() + "] event for user [" + username + "] and scrapbookid [" + scrapbookid + "] and title [" + title + "]");
         }

         PhotoUploadedUserEventIce userEvent = new PhotoUploadedUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.scrapbookid = scrapbookid;
         userEvent.title = title;
         this.defaultEventProcess(userEvent);
      } finally {
         this.madePhotoPublicCounter.add();
         this.totalCounter.add();
      }

   }

   public void purchasedVirtualGoods(String username, byte itemType, int itemid, String itemName, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.PURCHASED_GOODS.toString() + "] event for user [" + username + "] and itemid [" + itemid + "] and itemname [" + itemName + "]");
         }

         PurchasedVirtualGoodsUserEventIce userEvent = new PurchasedVirtualGoodsUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.itemId = itemid;
         userEvent.itemName = itemName;
         userEvent.itemType = itemType;
         this.defaultEventProcess(userEvent);
      } finally {
         this.purchasedVirtualGoodsCounter.add();
         this.totalCounter.add();
      }

   }

   public void setProfileStatus(String username, String status, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.SHORT_TEXT_STATUS.toString() + "] event for user [" + username + "] and status [" + status + "]");
         }

         ShortTextStatusUserEventIce userEvent = new ShortTextStatusUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.status = StringUtil.stripHTML(status);
         this.defaultEventProcess(userEvent);
      } finally {
         this.setProfileStatusCounter.add();
         this.totalCounter.add();
      }

   }

   public void updatedProfile(String username, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.UPDATING_PROFILE.toString() + "] event for user [" + username + "]");
         }

         ProfileUpdatedUserEventIce userEvent = new ProfileUpdatedUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         this.defaultEventProcess(userEvent);
      } finally {
         this.updatedProfileCounter.add();
         this.totalCounter.add();
      }

   }

   public void virtualGift(String username, String recipient, String giftName, int virtualGiftReceivedId, Current __current) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.VIRTUAL_GIFT.toString() + "] event for user [" + username + "]");
         }

         VirtualGiftUserEventIce userEvent = new VirtualGiftUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.recipient = recipient;
         userEvent.giftName = giftName;
         userEvent.virtualGiftReceivedId = virtualGiftReceivedId;
         if (this.isEventBeingGenerated(userEvent)) {
            Set<String> distributionList = this.defaultDistributionList(userEvent);
            distributionList.addAll(this.fusionBroadcastListForUsername(recipient));
            distributionList.remove(username);
            if (log.isDebugEnabled()) {
               log.debug("distribution list for virtual gift event, generator [" + userEvent.generatingUsername + "] and recipient [" + recipient + "]");
               Iterator i$ = distributionList.iterator();

               while(i$.hasNext()) {
                  String watcher = (String)i$.next();
                  log.debug(watcher);
               }
            }

            this.storeAndDistributeEvents(distributionList, userEvent);
            return;
         }
      } catch (Exception var14) {
         log.error("Unable to trigger virtualGift event", var14);
         return;
      } finally {
         this.virtualGiftCounter.add();
         this.totalCounter.add();
      }

   }

   public void giftShowerEvent(String username, String recipient, String giftName, int virtualGiftReceivedId, int totalRecipients, Current __current) {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.GIFT_SHOWER_EVENT.toString() + "] event for user [" + username + "]");
         }

         GiftShowerUserEventIce userEvent = new GiftShowerUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.recipient = recipient;
         userEvent.giftName = giftName;
         userEvent.totalRecipients = totalRecipients;
         userEvent.virtualGiftReceivedId = virtualGiftReceivedId;
         if (!this.isEventBeingGenerated(userEvent)) {
            return;
         }

         Set<String> distributionList = this.defaultDistributionList(userEvent);
         distributionList.addAll(this.fusionBroadcastListForUsername(recipient));
         distributionList.remove(username);
         log.debug("Distributing [" + UserEventType.GIFT_SHOWER_EVENT.toString() + "] from [" + recipient + "] to " + distributionList.toString());
         this.storeAndDistributeEvents(distributionList, userEvent);
      } catch (Exception var13) {
         log.error("Unable to trigger giftShowerEvent event", var13);
      } finally {
         this.giftShowerEventCounter.add();
         this.totalCounter.add();
         log.debug("Total shower events: " + this.giftShowerEventCounter.getTotalRequests());
      }

   }

   public void userWallPost(String username, String wallOwnerUsername, String postContent, int userWallPostId, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.USER_WALL_POST.toString() + "] event for user [" + username + "]");
         }

         UserWallPostUserEventIce userEvent = new UserWallPostUserEventIce();
         userEvent.generatingUsername = username;
         userEvent.timestamp = System.currentTimeMillis();
         userEvent.wallOwnerUsername = wallOwnerUsername;
         userEvent.postPrefix = postContent.substring(0, 10) + "...";
         userEvent.userWallPostId = userWallPostId;
         if (this.isEventBeingGenerated(userEvent)) {
            Set<String> distributionList = this.fusionBroadcastListForUsername(wallOwnerUsername);
            distributionList.add(wallOwnerUsername);
            distributionList.remove(username);
            if (log.isDebugEnabled()) {
               log.debug("distribution list for user wall post event, generator [" + userEvent.generatingUsername + "] and wall owner [" + userEvent.wallOwnerUsername + "]");
               Iterator i$ = distributionList.iterator();

               while(i$.hasNext()) {
                  String watcher = (String)i$.next();
                  log.debug(watcher);
               }
            }

            this.storeAndDistributeEvents(distributionList, userEvent);
            return;
         }
      } finally {
         this.userWallPostCounter.add();
         this.totalCounter.add();
      }

   }

   public void groupAnnouncement(String username, int groupId, int groupAnnoucementId, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.GROUP_ANNOUNCEMENT.toString() + "] event for user [" + username + "]");
         }

         GroupAnnouncementUserEventIce groupEvent = new GroupAnnouncementUserEventIce();
         groupEvent.generatingUsername = username;
         groupEvent.timestamp = System.currentTimeMillis();
         groupEvent.groupId = groupId;
         groupEvent.groupAnnouncementId = groupAnnoucementId;
         Set<String> distributionList = this.fusionBroadcastListForGroup(groupId);
         if (log.isDebugEnabled()) {
            log.debug("distribution list for group announcement event, group [" + groupEvent.groupId + "]");
            Iterator i$ = distributionList.iterator();

            while(i$.hasNext()) {
               String watcher = (String)i$.next();
               log.debug(watcher);
            }
         }

         this.storeAndDistributeEvents(distributionList, groupEvent);
      } finally {
         this.groupAnnouncementCounter.add();
         this.totalCounter.add();
      }

   }

   public void groupDonation(String username, int groupId, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.GROUP_DONATION.toString() + "] event for user [" + username + "]");
         }

         GroupDonationUserEventIce groupEvent = new GroupDonationUserEventIce();
         groupEvent.generatingUsername = username;
         groupEvent.timestamp = System.currentTimeMillis();
         groupEvent.groupId = groupId;
         this.defaultEventProcess(groupEvent);
      } finally {
         this.groupDonationCounter.add();
         this.totalCounter.add();
      }

   }

   public void groupJoined(String username, int groupId, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.GROUP_JOINED.toString() + "] event for user [" + username + "]");
         }

         GroupJoinedUserEventIce groupEvent = new GroupJoinedUserEventIce();
         groupEvent.generatingUsername = username;
         groupEvent.timestamp = System.currentTimeMillis();
         groupEvent.groupId = groupId;
         this.defaultEventProcess(groupEvent);
      } finally {
         this.groupJoinedCounter.add();
         this.totalCounter.add();
      }

   }

   public void madeGroupUserPost(String username, int userPostId, int groupId, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.GROUP_USER_POST.toString() + "] event for user [" + username + "]");
         }

         GroupUserPostUserEventIce groupEvent = new GroupUserPostUserEventIce();
         groupEvent.generatingUsername = username;
         groupEvent.timestamp = System.currentTimeMillis();
         groupEvent.groupId = groupId;
         groupEvent.userPostId = userPostId;
         Set<String> distributionList = this.fusionBroadcastListForTopic(userPostId);
         if (log.isDebugEnabled()) {
            log.debug("distribution list for group userpost event, userpost [" + groupEvent.userPostId + "] group [" + groupEvent.groupId + "]");
            Iterator i$ = distributionList.iterator();

            while(i$.hasNext()) {
               String watcher = (String)i$.next();
               log.debug(watcher);
            }
         }

         this.storeAndDistributeEvents(distributionList, groupEvent);
      } finally {
         this.groupUserPostCounter.add();
         this.totalCounter.add();
      }

   }

   public void genericApplicationEvent(String username, String appID, String text, Map<String, String> customDeviceURLs, Current __current) throws FusionException {
      try {
         if (log.isDebugEnabled()) {
            log.debug("Creating [" + UserEventType.GENERIC_APP_EVENT.toString() + "] event for user [" + username + "]");
         }

         if (!StringUtil.isBlank(username) && !StringUtil.isBlank(text) && customDeviceURLs != null) {
            if (text.length() > 200) {
               text = text.substring(0, 200);
            }

            GenericApplicationUserEventIce userEvent = new GenericApplicationUserEventIce();
            userEvent.generatingUsername = username;
            userEvent.timestamp = System.currentTimeMillis();
            userEvent.text = StringUtil.stripHTML(text);
            userEvent.urls = customDeviceURLs;
            this.defaultEventProcess(userEvent);
            return;
         }

         log.error("genericApplicationEvent called with username (" + username + ") text(" + text + ") customDeviceURLs(" + customDeviceURLs + ")");
      } finally {
         this.genericApplicationEventCounter.add();
         this.totalCounter.add();
      }

   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Current __current) throws FusionException {
      EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
      return proxy.getPublishingPrivacyMask(username);
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Current __current) throws FusionException {
      EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
      return proxy.getReceivingPrivacyMask(username);
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
      proxy.setPublishingPrivacyMask(username, mask);
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Current __current) throws FusionException {
      EventStorePrx proxy = this.proxyForProxyString((String)this.consistentHash.get(username));
      proxy.setReceivingPrivacyMask(username, mask);
   }

   public void deleteUserEvents(String username, Current __current) throws FusionException {
      String proxyString = (String)this.consistentHash.get(username);
      EventStorePrx eventStoreProxy = this.proxyForProxyString(proxyString);
      eventStoreProxy.deleteUserEvents(username);
   }

   private EventStorePrx narrowEventStoreProxy(Communicator communicator, String name) {
      try {
         ObjectPrx basePrx = communicator.stringToProxy(name);
         if (basePrx == null) {
            log.error("failed to create proxy from string [" + name + "]");
            return null;
         } else {
            EventStorePrx proxy = EventStorePrxHelper.checkedCast(basePrx);
            if (proxy == null) {
               log.error("failed to cast EventStore proxy from base proxy from string [" + name + "]");
               return null;
            } else {
               proxy.ice_ping();
               return proxy;
            }
         }
      } catch (Exception var5) {
         log.warn("failed to contact proxy [" + name + "]", var5);
         return null;
      }
   }

   public void createProxies(Communicator communicator) {
      log.info("Creating proxies...");

      try {
         this.eventStoreProxies = new HashMap(this.eventStores.size());
         this.eventStoreExecutors = new HashMap(this.eventStores.size());
         Iterator i$ = this.eventStores.iterator();

         while(i$.hasNext()) {
            String eventStoreProxyString = (String)i$.next();
            if (log.isDebugEnabled()) {
               log.debug("Considering proxy string [" + eventStoreProxyString + "]");
            }

            EventStorePrx proxy = this.narrowEventStoreProxy(communicator, eventStoreProxyString);

            while(proxy == null) {
               proxy = this.narrowEventStoreProxy(communicator, eventStoreProxyString);
               log.warn("Waiting for proxy [" + eventStoreProxyString + "] to become available, EventSystem is not creating events until the proxy is up!");
            }

            this.eventStoreProxies.put(eventStoreProxyString, proxy);
            this.eventStoreExecutors.put(eventStoreProxyString, Executors.newFixedThreadPool(this.maxStoreThreadPoolSize));
            log.info("Added proxy [" + proxy + "]");
         }
      } catch (Exception var5) {
         log.error("Failed to create proxies", var5);
      }

      log.info("Done creating proxies...");
   }

   public void shutdown() {
      log.info("Shutting down " + this.getClass().getCanonicalName());
      if (this.slimVersion) {
         try {
            this.userEventStatsLogger.close();
            this.generatorEventStatsLogger.close();
         } catch (IOException var2) {
            log.error("failed to close stats logger", var2);
         }
      }

   }

   public RequestAndRateLongCounter getSetProfileStatusCounter() {
      return this.setProfileStatusCounter;
   }

   public RequestAndRateLongCounter getMadePhotoPublicCounter() {
      return this.madePhotoPublicCounter;
   }

   public RequestAndRateLongCounter getCreatedPublicChatrooomStatusCounter() {
      return this.createdPublicChatrooomStatusCounter;
   }

   public RequestAndRateLongCounter getAddedFriendCounter() {
      return this.addedFriendCounter;
   }

   public RequestAndRateLongCounter getUpdatedProfileCounter() {
      return this.updatedProfileCounter;
   }

   public RequestAndRateLongCounter getPurchasedVirtualGoodsCounter() {
      return this.purchasedVirtualGoodsCounter;
   }

   public RequestAndRateLongCounter getVirtualGiftCounter() {
      return this.virtualGiftCounter;
   }

   public RequestAndRateLongCounter getUserWallPostCounter() {
      return this.userWallPostCounter;
   }

   public RequestAndRateLongCounter getTotalCounter() {
      return this.totalCounter;
   }

   public RequestAndRateLongCounter getDroppedEventCounter() {
      return this.droppedEventCounter;
   }

   public RequestAndRateLongCounter getStreamedEventsCounter() {
      return this.streamedEventsCounter;
   }

   public RequestAndRateLongCounter getDistributedEventsCounter() {
      return this.distributedEventsCounter;
   }

   public RequestAndRateLongCounter getGenericApplicationEventCounter() {
      return this.genericApplicationEventCounter;
   }

   public RequestAndRateLongCounter getGiftShowerEventCounter() {
      return this.giftShowerEventCounter;
   }
}
