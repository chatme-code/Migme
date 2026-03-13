package com.projectgoth.fusion.gateway;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.RecommendationItem;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Message;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.recommendation.delivery.Enums;
import com.projectgoth.fusion.recommendation.delivery.RecommendationDeliveryUtils;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class ChatRoomList {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatRoomList.class));
   private static String[] defaultChatRoomNames = new String[0];
   private static String[] defaultGameChatRoomNames = new String[0];
   private static String[] defaultNewbiesChatRoomNames = new String[0];
   private static String[] chatRoomsToHide = new String[0];
   private static double hideProbability = 0.5D;
   private static int categoryPageSize = 5;
   private static int initialPageSize = 5;
   private static int pageSize = 5;
   private static int maxChatRoomsReturned = 100;
   private static Map<Integer, ChatRoomList.CacheData> cache = new HashMap();
   private static int cacheInterval = 10;
   private ChatRoomList.ArrayListBuffer newbiesChatRooms = new ChatRoomList.ArrayListBuffer();
   private ChatRoomList.ArrayListBuffer favouriteChatRooms = new ChatRoomList.ArrayListBuffer();
   private ChatRoomList.ArrayListBuffer recentChatRooms = new ChatRoomList.ArrayListBuffer();
   private ChatRoomList.ArrayListBuffer gameChatRooms = new ChatRoomList.ArrayListBuffer();
   private ChatRoomList.ArrayListBuffer chatRooms = new ChatRoomList.ArrayListBuffer();
   private boolean chatRoomsLoaded;
   private int countryID;
   private static Semaphore semaphore;
   private static long chatroomListLastUpdated;
   private static Map<Integer, List<String>> chatroomNamesPerCategory;
   private static long CHATROOM_LIST_CACHE_TIME;
   private Map<Integer, ChatRoomList.ArrayListBuffer> nonStandardChatroomsPerUser = new HashMap();

   public static void loadChatroomInChatroomCategories() {
      if (chatroomListLastUpdated == 0L) {
         semaphore.acquireUninterruptibly();
      } else if (!semaphore.tryAcquire()) {
         return;
      }

      try {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
            chatroomNamesPerCategory = DAOFactory.getInstance().getChatRoomDAO().getChatroomNamesPerCategory(true);
         } else {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            chatroomNamesPerCategory = messageEJB.getChatroomNamesPerCategory(true);
         }

         chatroomListLastUpdated = System.currentTimeMillis();
      } catch (Exception var4) {
         log.warn("Unable to load chatroom categories", var4);
      } finally {
         semaphore.release();
      }

   }

   public static List<String> getChatroomNamesInCategory(int chatroomCategoryId) throws Exception {
      if (System.currentTimeMillis() - chatroomListLastUpdated > CHATROOM_LIST_CACHE_TIME) {
         loadChatroomInChatroomCategories();
      }

      if (!chatroomNamesPerCategory.containsKey(chatroomCategoryId)) {
         log.error("Unable to retrieve chatroom name list for category [" + chatroomCategoryId + "]");
         throw new Exception("Unable to retrieve chatrooms.");
      } else {
         return (List)chatroomNamesPerCategory.get(chatroomCategoryId);
      }
   }

   public ChatRoomList.Page getPopularChatRooms(RegistryPrx registryPrx, int countryID, boolean refresh) throws CreateException, RemoteException {
      if (refresh || this.chatRooms.itemsLeft() == 0) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
            try {
               this.chatRooms.wrap(DAOFactory.getInstance().getChatRoomDAO().getChatRooms(countryID, (String)null));
            } catch (DAOException var5) {
               log.error(String.format("DAO: Failed to get Chatrooms from country:%s", countryID), var5);
               throw new RemoteException(var5.getMessage());
            }
         } else {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            this.chatRooms.wrap(messageEJB.getChatRooms(countryID, (String)null));
         }

         this.chatRooms.removeAll(this.favouriteChatRooms);
         this.chatRooms.removeAll(this.recentChatRooms);
         this.filterHiddenChatRooms(this.chatRooms);
      }

      return this.fillChatRoomSize(registryPrx, this.chatRooms.nextPage(categoryPageSize));
   }

   public ChatRoomList.Page getFavouriteChatRooms(RegistryPrx registryPrx, String username, boolean refresh) throws CreateException, RemoteException {
      if (refresh || this.favouriteChatRooms.itemsLeft() == 0) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
            try {
               this.favouriteChatRooms.wrap((new UserObject(username)).getFavouriteChatRooms());
            } catch (DAOException var5) {
               log.error(String.format("DAO: Failed to get FavouriteChatRooms from user:%s", username), var5);
               throw new RemoteException(var5.getMessage());
            }
         } else {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            this.favouriteChatRooms.wrap(messageEJB.getFavouriteChatRooms(username));
         }
      }

      return this.fillChatRoomSize(registryPrx, this.favouriteChatRooms.nextPage(categoryPageSize));
   }

   public ChatRoomList.Page getRecentChatRooms(RegistryPrx registryPrx, String username, boolean refresh) throws CreateException, RemoteException {
      if (refresh || this.recentChatRooms.size() == 0) {
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
            try {
               this.recentChatRooms.wrap((new UserObject(username)).getRecentChatRooms());
            } catch (DAOException var5) {
               log.error(String.format("DAO: Failed to get RecentChatRooms from user:%s", username), var5);
               throw new RemoteException(var5.getMessage());
            }
         } else {
            Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
            this.recentChatRooms.wrap(messageEJB.getRecentChatRooms(username));
         }
      }

      return this.fillChatRoomSize(registryPrx, this.recentChatRooms.nextPage(categoryPageSize));
   }

   public ChatRoomList.Page getRecommendedChatRooms(RegistryPrx registryPrx, int userId, boolean refresh) throws CreateException, RemoteException {
      if (refresh || this.chatRooms.itemsLeft() == 0) {
         List<ChatRoomData> recommendedChatRooms = new ArrayList();
         int maxRecommendedChatRooms = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MAX_RECOMMENDED_CHATROOMS);
         Iterator i$ = RecommendationDeliveryUtils.getRecommendation(Enums.RecommendationTypeEnum.CHATROOMS, Enums.RecommendationTargetEnum.INDIVIDUAL, userId, (String)null, maxRecommendedChatRooms, 0, false).getRecommendations().iterator();

         while(i$.hasNext()) {
            RecommendationItem item = (RecommendationItem)i$.next();
            ChatRoomData room = (ChatRoomData)Enums.RecommendationTypeEnum.CHATROOMS.getEvaluator().evaluate(item);
            if (null != room) {
               recommendedChatRooms.add(room);
            }
         }

         this.chatRooms.wrap(recommendedChatRooms);
         this.filterHiddenChatRooms(this.chatRooms);
      }

      return this.fillChatRoomSize(registryPrx, this.chatRooms.nextPage(categoryPageSize));
   }

   public ChatRoomList.Page getChatrooms(RegistryPrx registryPrx, boolean refresh, Integer categoryId) throws CreateException, RemoteException, Exception {
      if (!this.nonStandardChatroomsPerUser.containsKey(categoryId)) {
         this.nonStandardChatroomsPerUser.put(categoryId, new ChatRoomList.ArrayListBuffer());
      }

      if (refresh || ((ChatRoomList.ArrayListBuffer)this.nonStandardChatroomsPerUser.get(categoryId)).size() == 0) {
         List<String> chatroomNames = getChatroomNamesInCategory(categoryId);
         ChatRoomList.ArrayListBuffer chatrooms = (ChatRoomList.ArrayListBuffer)this.nonStandardChatroomsPerUser.get(categoryId);
         chatrooms.wrap(this.getChatRooms(registryPrx, (String[])chatroomNames.toArray(new String[chatroomNames.size()])));
         Collections.shuffle(chatrooms);
         this.nonStandardChatroomsPerUser.put(categoryId, chatrooms);
      }

      return this.fillChatRoomSize(registryPrx, ((ChatRoomList.ArrayListBuffer)this.nonStandardChatroomsPerUser.get(categoryId)).nextPage(categoryPageSize));
   }

   public ChatRoomList.Page getGameChatRooms(RegistryPrx registryPrx, boolean refresh) throws CreateException, RemoteException {
      if (!refresh && this.gameChatRooms.size() != 0) {
         return this.fillChatRoomSize(registryPrx, this.gameChatRooms.nextPage(categoryPageSize));
      } else {
         this.gameChatRooms.wrap(this.getChatRooms(registryPrx, defaultGameChatRoomNames));
         this.filterHiddenChatRooms(this.chatRooms);
         Collections.shuffle(this.gameChatRooms);
         return this.gameChatRooms.nextPage(categoryPageSize);
      }
   }

   public ChatRoomList.Page getNewbiesRooms(RegistryPrx registryPrx, boolean refresh) throws CreateException, RemoteException {
      log.debug("getNewbiesRooms() REFRESH [" + refresh + "] chatroomsize [" + this.newbiesChatRooms.size() + "]");
      if (!refresh && this.newbiesChatRooms.size() != 0) {
         return this.fillChatRoomSize(registryPrx, this.newbiesChatRooms.nextPage(categoryPageSize));
      } else {
         this.newbiesChatRooms.wrap(this.getChatRooms(registryPrx, defaultNewbiesChatRoomNames));
         Collections.shuffle(this.newbiesChatRooms);
         return this.newbiesChatRooms.nextPage(categoryPageSize);
      }
   }

   private void filterHiddenChatRooms(ChatRoomList.ArrayListBuffer chatrooms) {
      if (chatRoomsToHide.length != 0 && hideProbability != 0.0D) {
         Iterator li = chatrooms.iterator();

         while(true) {
            while(li.hasNext()) {
               ChatRoomData chatroomdata = (ChatRoomData)li.next();
               String chatroomname = chatroomdata.name.toLowerCase().replace(" ", "");
               double p = Math.random();

               for(int j = 0; j < chatRoomsToHide.length; ++j) {
                  if (chatroomname.indexOf(chatRoomsToHide[j].toLowerCase()) > 0) {
                     if (p <= hideProbability) {
                        li.remove();
                     }
                     break;
                  }
               }
            }

            return;
         }
      }
   }

   private ChatRoomList.Page fillChatRoomSize(RegistryPrx registryPrx, ChatRoomList.Page page) {
      this.fillChatRoomSize(registryPrx, (Collection)page.getPageList());
      return page;
   }

   private List<ChatRoomData> getChatRooms(int countryID, String searchString) throws CreateException, RemoteException {
      if (searchString == null && cacheInterval > 0) {
         boolean refreshCache = false;
         ChatRoomList.CacheData cacheData;
         synchronized(cache) {
            cacheData = (ChatRoomList.CacheData)cache.get(countryID);
            if (cacheData == null) {
               refreshCache = true;
            } else if (cacheData.expiry < System.currentTimeMillis()) {
               refreshCache = true;
               cacheData.expiry += (long)cacheInterval;
            }
         }

         if (refreshCache) {
            cacheData = new ChatRoomList.CacheData();
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
               try {
                  cacheData.chatRooms = DAOFactory.getInstance().getChatRoomDAO().getChatRooms(countryID, searchString);
               } catch (DAOException var9) {
                  log.error(String.format("Failed to get ChatRooms for country:%s, search:%s", countryID, searchString), var9);
                  throw new RemoteException(var9.getMessage());
               }
            } else {
               Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               cacheData.chatRooms = messageEJB.getChatRooms(countryID, searchString);
            }

            cacheData.expiry = System.currentTimeMillis() + (long)cacheInterval;
            synchronized(cache) {
               cache.put(countryID, cacheData);
            }
         }

         return cacheData.chatRooms;
      } else if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
         try {
            return DAOFactory.getInstance().getChatRoomDAO().getChatRooms(countryID, searchString);
         } catch (DAOException var11) {
            log.error(String.format("Failed to get ChatRooms for country:%s, search:%s", countryID, searchString), var11);
            throw new RemoteException(var11.getMessage());
         }
      } else {
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         return messageEJB.getChatRooms(countryID, searchString);
      }
   }

   private List<ChatRoomData> getChatRooms(RegistryPrx registryPrx, String[] chatRoomNames) throws RemoteException, CreateException {
      ChatRoomPrx[] chatRoomPrxs = new ChatRoomPrx[chatRoomNames.length];

      try {
         chatRoomPrxs = registryPrx.findChatRoomObjects(chatRoomNames);
      } catch (Exception var10) {
      }

      List<ChatRoomData> chatRoomList = new ArrayList();

      for(int i = 0; i < chatRoomNames.length; ++i) {
         ChatRoomPrx roomPrx = chatRoomPrxs[i];
         if (roomPrx == null) {
            ChatRoomData room = null;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
               try {
                  room = DAOFactory.getInstance().getChatRoomDAO().getChatRoom(chatRoomNames[i]);
               } catch (DAOException var9) {
                  throw new RemoteException(var9.getMessage());
               }
            } else {
               Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               room = messageEJB.getChatRoom(chatRoomNames[i]);
            }

            if (room != null) {
               chatRoomList.add(room);
            }
         } else {
            chatRoomList.add(new ChatRoomData(roomPrx.getRoomData()));
         }
      }

      return chatRoomList;
   }

   private void fillChatRoomSize(RegistryPrx registryPrx, Collection<ChatRoomData> chatRooms) {
      String[] chatRoomNames = new String[chatRooms.size()];
      int i = 0;

      ChatRoomData chatRoom;
      for(Iterator i$ = chatRooms.iterator(); i$.hasNext(); chatRoomNames[i++] = chatRoom.name) {
         chatRoom = (ChatRoomData)i$.next();
      }

      ChatRoomPrx[] chatRoomPrxs;
      try {
         chatRoomPrxs = registryPrx.findChatRoomObjects(chatRoomNames);
      } catch (Exception var8) {
         return;
      }

      i = 0;

      for(Iterator i$ = chatRooms.iterator(); i$.hasNext(); ++i) {
         ChatRoomData chatRoom = (ChatRoomData)i$.next();
         if (chatRoomPrxs[i] != null) {
            chatRoom.size = chatRoomPrxs[i].getNumParticipants();
         }
      }

   }

   public int pages() {
      return (int)Math.ceil((double)(this.chatRoomsLoaded ? this.chatRooms.size() : maxChatRoomsReturned) / (double)pageSize);
   }

   public List<ChatRoomData> getInitialList(RegistryPrx registryPrx, String username, String[] chatRoomNames) throws RemoteException, CreateException {
      List<String> nameList = new ArrayList();
      this.recentChatRooms.clear();
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DAOSettings.ENABLED_CHATROOM_DAO)) {
         try {
            this.recentChatRooms.addAll((new UserObject(username)).getRecentChatRooms());
         } catch (DAOException var7) {
            throw new RemoteException(var7.getMessage());
         }
      } else {
         Message messageEJB = (Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
         this.recentChatRooms.addAll(messageEJB.getRecentChatRooms(username));
      }

      int i;
      for(i = 0; i < this.recentChatRooms.size() && i < initialPageSize; ++i) {
         nameList.add(((ChatRoomData)this.recentChatRooms.get(i)).name.toLowerCase());
      }

      for(i = 0; i < defaultChatRoomNames.length && nameList.size() < initialPageSize; ++i) {
         String name = defaultChatRoomNames[i].toLowerCase();
         if (!nameList.contains(name)) {
            nameList.add(name);
         }
      }

      return this.getChatRooms(registryPrx, (String[])nameList.toArray(new String[nameList.size()]));
   }

   public List<ChatRoomData> getList(RegistryPrx registryPrx, int countryID, String searchString) throws RemoteException, CreateException {
      this.countryID = countryID;
      if (searchString == null) {
         this.chatRooms.clear();

         for(int i = initialPageSize; i < this.recentChatRooms.size(); ++i) {
            this.chatRooms.add(this.recentChatRooms.get(i));
         }

         this.chatRoomsLoaded = false;
      } else {
         this.chatRooms.clear();
         this.chatRooms.addAll(this.getChatRooms(countryID, searchString));
         this.chatRoomsLoaded = true;
      }

      return this.getPage(registryPrx, 1);
   }

   public List<ChatRoomData> getPage(RegistryPrx registryPrx, int page) throws RemoteException, CreateException {
      int start = (page - 1) * pageSize;
      if (!this.chatRoomsLoaded && (start > this.chatRooms.size() || start + pageSize > this.chatRooms.size())) {
         List<ChatRoomData> rooms = this.getChatRooms(this.countryID, (String)null);

         for(int i = 0; i < rooms.size() && this.chatRooms.size() < maxChatRoomsReturned; ++i) {
            if (!this.recentChatRooms.contains(rooms.get(i))) {
               this.chatRooms.add(rooms.get(i));
            }
         }

         this.chatRoomsLoaded = true;
      }

      int end = Math.min(start + pageSize, this.chatRooms.size());
      if (start >= end) {
         return Collections.EMPTY_LIST;
      } else {
         List<ChatRoomData> pageList = this.chatRooms.subList(start, end);
         this.fillChatRoomSize(registryPrx, (Collection)pageList);
         return pageList;
      }
   }

   static {
      try {
         defaultChatRoomNames = SystemProperty.getArray("DefaultChatRooms", defaultChatRoomNames);
         defaultGameChatRoomNames = SystemProperty.getArray("DefaultGameChatRooms", defaultGameChatRoomNames);
         defaultNewbiesChatRoomNames = SystemProperty.getArray("DefaultNewbiesChatRooms", defaultNewbiesChatRoomNames);
         initialPageSize = SystemProperty.getInt("ChatRoomInitialPageSize", initialPageSize);
         pageSize = SystemProperty.getInt("ChatRoomPageSize", pageSize);
         maxChatRoomsReturned = SystemProperty.getInt("MaxChatRoomsReturned", maxChatRoomsReturned);
         cacheInterval = SystemProperty.getInt("ChatRoomCacheInterval", cacheInterval) * 1000;
         categoryPageSize = SystemProperty.getInt("CategoryPageSize", categoryPageSize);
         chatRoomsToHide = SystemProperty.getArray("ChatRoomsToHide", chatRoomsToHide);
         hideProbability = SystemProperty.getDouble("ChatroomHideProbability", hideProbability);
      } catch (Exception var1) {
         log.warn("Failed to load chat room properties. Using default values");
      }

      semaphore = new Semaphore(1);
      chatroomNamesPerCategory = new ConcurrentHashMap();
      CHATROOM_LIST_CACHE_TIME = SystemProperty.getLong((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.CHATROOM_LIST_CACHE_TIME_IN_MILLIS);
   }

   public static class Page {
      private List<ChatRoomData> fullList;
      private List<ChatRoomData> pageList;
      private int itemsLeft;

      public List<ChatRoomData> getFullList() {
         return this.fullList;
      }

      public List<ChatRoomData> getPageList() {
         return this.pageList;
      }

      public int itemsLeft() {
         return this.itemsLeft;
      }
   }

   private static class ArrayListBuffer extends ArrayList<ChatRoomData> {
      private int index;

      private ArrayListBuffer() {
      }

      public void wrap(Collection<ChatRoomData> chatRoomList) {
         this.clear();
         this.addAll(chatRoomList);
         this.index = 0;
      }

      public int itemsLeft() {
         return Math.max(this.size() - this.index, 0);
      }

      public ChatRoomList.Page nextPage(int items) {
         int fromIndex = Math.min(this.index, this.size());
         int toIndex = Math.min(fromIndex + items, this.size());
         this.index = toIndex;
         ChatRoomList.Page page = new ChatRoomList.Page();
         page.fullList = this;
         page.pageList = fromIndex == toIndex ? Collections.EMPTY_LIST : this.subList(fromIndex, toIndex);
         page.itemsLeft = this.itemsLeft();
         return page;
      }

      // $FF: synthetic method
      ArrayListBuffer(Object x0) {
         this();
      }
   }

   private static class CacheData {
      public long expiry;
      public List<ChatRoomData> chatRooms;

      private CacheData() {
      }

      // $FF: synthetic method
      CacheData(Object x0) {
         this();
      }
   }
}
