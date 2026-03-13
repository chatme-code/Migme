package com.projectgoth.fusion.app.dao.db.fusiondb;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.ChatRoomDAOChain;
import com.projectgoth.fusion.app.dao.data.UserObject;
import com.projectgoth.fusion.cache.RecentChatRoomList;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ChatroomCategoryData;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

public class FusionDbChatRoomDAOChain extends ChatRoomDAOChain {
   private static final Logger log = Logger.getLogger(FusionDbChatRoomDAOChain.class);
   private static final SecureRandom secureRandom = new SecureRandom();

   public String[] getGroupChatRooms(int groupId) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      String[] chatroomArr;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select name from chatroom where groupid = ? and status = ?");
         ps.setInt(1, groupId);
         ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         LinkedList chatrooms = new LinkedList();

         while(rs.next()) {
            chatrooms.add(rs.getString("name"));
         }

         rs.close();
         chatroomArr = (String[])chatrooms.toArray(new String[chatrooms.size()]);
         String[] var7 = chatroomArr;
         return var7;
      } catch (SQLException var12) {
         log.error(String.format("Failed to get GroupChatRooms for groupid:%s", groupId), var12);
         chatroomArr = super.getGroupChatRooms(groupId);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return chatroomArr;
   }

   public Map<Integer, List<String>> getChatroomNamesPerCategory(boolean activeChatroomsAndCategoriesOnly) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Map var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         String sql = "SELECT c2crl.chatroomcategorylistid, c.name FROM chatroom c     ,chatroomcategorylist crl \t   ,chatroomtochatroomcategorylist c2crl WHERE c.id = c2crl.chatroomid AND crl.id = c2crl.chatroomcategorylistid ";
         if (activeChatroomsAndCategoriesOnly) {
            sql = sql + "AND crl.status = ? AND c.status = ? ";
         }

         sql = sql + "ORDER BY c2crl.orderIndex DESC";
         ps = conn.prepareStatement(sql);
         if (activeChatroomsAndCategoriesOnly) {
            ps.setInt(1, ChatroomCategoryData.StatusEnum.ACTIVE.value());
            ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         }

         rs = ps.executeQuery();

         int categoryID;
         HashMap chatroomNamesPerCategory;
         for(chatroomNamesPerCategory = new HashMap(); rs.next(); ((List)chatroomNamesPerCategory.get(categoryID)).add(rs.getString("name"))) {
            categoryID = rs.getInt("chatroomcategorylistid");
            if (!chatroomNamesPerCategory.containsKey(categoryID)) {
               chatroomNamesPerCategory.put(categoryID, new ArrayList());
            }
         }

         if (log.isDebugEnabled()) {
            log.debug(String.format("DAO: Successfully get ChatroomNamesPerCategory from fusion databse with activeChatroomsAndCategoriesOnly:%s", activeChatroomsAndCategoriesOnly));
         }

         HashMap var15 = chatroomNamesPerCategory;
         return var15;
      } catch (SQLException var12) {
         log.error(String.format("Failed to get ChatroomNamesPerCategory for activeChatroomsAndCategoriesOnly:%s", activeChatroomsAndCategoriesOnly), var12);
         var6 = super.getChatroomNamesPerCategory(activeChatroomsAndCategoriesOnly);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public List<ChatRoomData> getChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.ENABLED_REFINED_SQL_FOR_SEARCHING_CHATROOM) ? this.getChatRoomsV2(countryID, search, language, includeAdultOnly, searchKeywords) : this.getChatRoomsV1(countryID, search, language, includeAdultOnly, searchKeywords);
   }

   private List<ChatRoomData> getChatRoomsV1(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      LinkedList var45;
      try {
         int maxChatRoomsReturned = 33;

         try {
            maxChatRoomsReturned = SystemProperty.getInt("MaxChatRoomsReturned");
         } catch (NoSuchFieldException var37) {
            log.error("Failed to get MaxChatRoomsReturned from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var37);
         }

         List<ChatRoomData> chatRooms = new LinkedList();
         List<ChatRoomData> adultRooms = new LinkedList();
         conn = DBUtils.getFusionReadConnection();
         boolean performingSearch = StringUtils.hasLength(search);
         if (performingSearch) {
            String searchLike = search.trim().replaceAll("[\\*%]", "");
            int minChatRoomSearchLength = SystemProperty.getInt((String)"MinChatRoomSearchLength", 0);
            if (searchLike.length() < minChatRoomSearchLength) {
               throw new DAOException("Search string must have at least " + minChatRoomSearchLength + " characters");
            }

            searchLike = searchLike.replaceAll("_", "\\\\_") + "%";
            String sql = "(select chatroom.*, null as keywords, 1 as sortorder from chatroom where name = ? and status = 1) union (select chatroom.*, null as keywords, 2 as sortorder from chatroom where name like ? and status = 1 ";
            if (!includeAdultOnly) {
               sql = sql + "and chatroom.adultonly=0 ";
            }

            sql = sql + "order by datelastaccessed desc limit ?) ";
            if (searchKeywords) {
               sql = sql + "union (select chatroom.*, group_concat(keyword.keyword) as keywords, 2 as sortorder from chatroom left join chatroomkeyword on chatroom.id=chatroomkeyword.chatroomid left join keyword on chatroomkeyword.keywordid=keyword.id where keyword.keyword = ? and chatroom.status = 1 ";
               if (!includeAdultOnly) {
                  sql = sql + "and chatroom.adultonly=0 ";
               }

               sql = sql + "group by id, name, description, type, creator, chatroomcategoryid, primarycountryid, secondarycountryid, locationid, groupid, adultonly, maximumsize, userowned, allowkicking, allowuserkeywords, allowBots, language, datecreated, datelastaccessed, status order by datelastaccessed desc limit ?) ";
            }

            sql = sql + "order by sortorder, datelastaccessed desc limit ?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, search);
            ps.setString(2, searchLike);
            ps.setInt(3, maxChatRoomsReturned);
            if (searchKeywords) {
               ps.setString(4, search);
               ps.setInt(5, maxChatRoomsReturned);
               ps.setInt(6, maxChatRoomsReturned);
            } else {
               ps.setInt(4, maxChatRoomsReturned);
            }

            rs = ps.executeQuery();
            boolean languageSearch = performingSearch && StringUtils.hasLength(language);
            List<ChatRoomData> languageMatchRooms = null;
            if (languageSearch) {
               languageMatchRooms = new LinkedList();
            }

            ChatRoomData exactMatch = null;

            while(true) {
               if (!rs.next()) {
                  if (languageSearch) {
                     chatRooms.addAll(0, languageMatchRooms);
                  }

                  if (exactMatch != null) {
                     chatRooms.add(0, exactMatch);
                  }
                  break;
               }

               ChatRoomData room = new ChatRoomData(rs);
               if (room.name.equalsIgnoreCase(search)) {
                  exactMatch = room;
               } else if (room.adultOnly != null && !room.adultOnly) {
                  if (languageSearch && room.language != null && room.language.equals(language)) {
                     languageMatchRooms.add(room);
                  } else {
                     chatRooms.add(room);
                  }
               } else {
                  adultRooms.add(room);
               }
            }
         } else {
            ps = conn.prepareStatement("select chatroom.*, null as keywords from chatroom where status = ? order by datelastaccessed desc limit ?");
            ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
            ps.setInt(2, maxChatRoomsReturned);
            rs = ps.executeQuery();

            label347:
            while(true) {
               while(true) {
                  if (!rs.next()) {
                     break label347;
                  }

                  ChatRoomData room = new ChatRoomData(rs);
                  if (room.adultOnly != null && !room.adultOnly) {
                     chatRooms.add(room);
                  } else {
                     adultRooms.add(room);
                  }
               }
            }
         }

         if (adultRooms.size() > 0) {
            int chatRoomInitialPageSize = 33;

            try {
               chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomInitialPageSize");
            } catch (NoSuchFieldException var36) {
               log.error("Failed to get ChatRoomInitialPageSize from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var36);
            }

            byte chatRoomPageSize = 33;

            try {
               chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomPageSize");
            } catch (NoSuchFieldException var35) {
               log.error("Failed to get ChatRoomPageSize from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var35);
            }

            byte chatRoomCleanPages = 33;

            try {
               chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomCleanPages");
            } catch (NoSuchFieldException var34) {
               log.error("Failed to get ChatRoomCleanPages from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var34);
            }

            int cleanRoomsToShow = chatRoomInitialPageSize + chatRoomPageSize * chatRoomCleanPages;
            double adultRoomsPerPage = (double)adultRooms.size() / ((double)(chatRooms.size() - cleanRoomsToShow) / (double)chatRoomPageSize);
            double roundingRatio = adultRoomsPerPage - (double)((int)adultRoomsPerPage);

            for(int i = cleanRoomsToShow; i < chatRooms.size() && adultRooms.size() > 0; i += chatRoomPageSize) {
               double noOfRooms = secureRandom.nextDouble() > roundingRatio ? Math.floor(adultRoomsPerPage) : Math.ceil(adultRoomsPerPage);

               for(int j = 0; (double)j < noOfRooms && adultRooms.size() > 0; ++j) {
                  chatRooms.add(i, adultRooms.remove(0));
               }
            }

            chatRooms.addAll(adultRooms);
         }

         var45 = chatRooms;
      } catch (DAOException var38) {
         log.error(String.format("Failed to get ChatRooms for countryID:%s, search:%s, language:%s, includeAdultOnly:%s, searchKeywords:%s", countryID, search, language, includeAdultOnly, search));
         throw var38;
      } catch (SQLException var39) {
         log.error(String.format("Failed to get ChatRooms for countryID:%s, search:%s, language:%s, includeAdultOnly:%s, searchKeywords:%s", countryID, search, language, includeAdultOnly, search), var39);
         List var10 = super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
         return var10;
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var45;
   }

   private List<ChatRoomData> getChatRoomsV2(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Statement stmt = null;

      List var11;
      try {
         int maxChatRoomsReturned = 33;

         try {
            maxChatRoomsReturned = SystemProperty.getInt("MaxChatRoomsReturned");
         } catch (NoSuchFieldException var48) {
            log.error("Failed to get MaxChatRoomsReturned from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var48);
         }

         List<ChatRoomData> chatRooms = new LinkedList();
         List<ChatRoomData> adultRooms = new LinkedList();
         conn = DBUtils.getFusionReadConnection();
         boolean performingSearch = StringUtils.hasLength(search);
         if (!performingSearch) {
            ps = conn.prepareStatement("select chatroom.*, null as keywords from chatroom where status = ? order by datelastaccessed desc limit ?");
            ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
            ps.setInt(2, maxChatRoomsReturned);
            rs = ps.executeQuery();

            while(rs.next()) {
               ChatRoomData room = new ChatRoomData(rs);
               if (room.adultOnly) {
                  adultRooms.add(room);
               } else {
                  chatRooms.add(room);
               }
            }
         } else {
            String searchLike = search.trim().replaceAll("[\\*%]", "");
            int minChatRoomSearchLength = SystemProperty.getInt((String)"MinChatRoomSearchLength", 0);
            if (searchLike.length() < minChatRoomSearchLength) {
               throw new DAOException("Search string must have at least " + minChatRoomSearchLength + " characters");
            }

            searchLike = searchLike.replaceAll("_", "\\\\_") + "%";
            String retrieveChatroomID = "(select chatroom.id, chatroom.adultonly, datelastaccessed from chatroom where name like ? and status = 1 order by datelastaccessed desc limit ?) ";
            if (searchKeywords) {
               retrieveChatroomID = retrieveChatroomID + " union all ";
               retrieveChatroomID = retrieveChatroomID + "(select chatroom.id, chatroom.adultonly, datelastaccessed from chatroom join chatroomkeyword on chatroom.id=chatroomkeyword.chatroomid join keyword on chatroomkeyword.keywordid=keyword.id where keyword.keyword = ? and chatroom.status = 1 order by datelastaccessed desc limit ?) order by datelastaccessed desc";
            }

            ps = conn.prepareStatement(retrieveChatroomID);
            ps.setString(1, searchLike);
            ps.setInt(2, maxChatRoomsReturned);
            if (searchKeywords) {
               ps.setString(3, search);
               ps.setInt(4, maxChatRoomsReturned);
            }

            rs = ps.executeQuery();
            Set<Integer> chatroomIDSet = new LinkedHashSet();
            int chatroomIdCount = 0;

            while(rs.next() && chatroomIdCount < maxChatRoomsReturned) {
               if (includeAdultOnly || rs.getInt(2) != 1) {
                  chatroomIDSet.add(rs.getInt(1));
                  ++chatroomIdCount;
               }
            }

            if (chatroomIDSet.isEmpty()) {
               List var60 = Collections.emptyList();
               return var60;
            }

            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var47) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var46) {
               ps = null;
            }

            String retrieveChatroomInfo = "select chatroom.*, group_concat(keyword.keyword) as keywords from chatroom left join chatroomkeyword on chatroom.id=chatroomkeyword.chatroomid left join keyword on chatroomkeyword.keywordid=keyword.id where chatroom.id in (%s) group by chatroom.id order by field(chatroom.id, %s);";
            StringBuilder sb = new StringBuilder();
            Iterator i$ = chatroomIDSet.iterator();

            while(i$.hasNext()) {
               Integer id = (Integer)i$.next();
               sb.append(id + ",");
            }

            String chatroomIDList = sb.substring(0, sb.length() - 1);
            retrieveChatroomInfo = String.format(retrieveChatroomInfo, chatroomIDList, chatroomIDList);
            stmt = conn.createStatement();
            rs = stmt.executeQuery(retrieveChatroomInfo);
            boolean languageSearch = performingSearch && StringUtils.hasLength(language);
            List<ChatRoomData> languageMatchRooms = null;
            if (languageSearch) {
               languageMatchRooms = new LinkedList();
            }

            ChatRoomData exactMatch = null;

            while(true) {
               if (!rs.next()) {
                  if (languageSearch) {
                     chatRooms.addAll(0, languageMatchRooms);
                  }

                  if (exactMatch != null) {
                     chatRooms.add(0, exactMatch);
                  }
                  break;
               }

               ChatRoomData room = new ChatRoomData(rs);
               if (room.name.equalsIgnoreCase(search)) {
                  exactMatch = room;
               } else if (room.adultOnly != null && !room.adultOnly) {
                  if (languageSearch && room.language != null && room.language.equals(language)) {
                     languageMatchRooms.add(room);
                  } else {
                     chatRooms.add(room);
                  }
               } else {
                  adultRooms.add(room);
               }
            }
         }

         if (adultRooms.size() > 0) {
            int chatRoomInitialPageSize = 33;

            try {
               chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomInitialPageSize");
            } catch (NoSuchFieldException var45) {
               log.error("Failed to get ChatRoomInitialPageSize from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var45);
            }

            byte chatRoomPageSize = 33;

            try {
               chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomPageSize");
            } catch (NoSuchFieldException var44) {
               log.error("Failed to get ChatRoomPageSize from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var44);
            }

            byte chatRoomCleanPages = 33;

            try {
               chatRoomInitialPageSize = SystemProperty.getInt("ChatRoomCleanPages");
            } catch (NoSuchFieldException var43) {
               log.error("Failed to get ChatRoomCleanPages from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var43);
            }

            int cleanRoomsToShow = chatRoomInitialPageSize + chatRoomPageSize * chatRoomCleanPages;
            double adultRoomsPerPage = (double)adultRooms.size() / ((double)(chatRooms.size() - cleanRoomsToShow) / (double)chatRoomPageSize);
            double roundingRatio = adultRoomsPerPage - (double)((int)adultRoomsPerPage);

            for(int i = cleanRoomsToShow; i < chatRooms.size() && adultRooms.size() > 0; i += chatRoomPageSize) {
               double noOfRooms = secureRandom.nextDouble() > roundingRatio ? Math.floor(adultRoomsPerPage) : Math.ceil(adultRoomsPerPage);

               for(int j = 0; (double)j < noOfRooms && adultRooms.size() > 0; ++j) {
                  chatRooms.add(i, adultRooms.remove(0));
               }
            }

            chatRooms.addAll(adultRooms);
         }

         LinkedList var56 = chatRooms;
         return var56;
      } catch (DAOException var49) {
         log.error(String.format("Failed to get ChatRooms for countryID:%s, search:%s, language:%s, includeAdultOnly:%s, searchKeywords:%s", countryID, search, language, includeAdultOnly, search));
         throw var49;
      } catch (SQLException var50) {
         log.error(String.format("Failed to get ChatRooms for countryID:%s, search:%s, language:%s, includeAdultOnly:%s, searchKeywords:%s", countryID, search, language, includeAdultOnly, search), var50);
         var11 = super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);

         try {
            if (stmt != null) {
               stmt.close();
            }
         } catch (SQLException var42) {
            stmt = null;
            log.warn("Failed to close statement", var42);
         }

      }

      return var11;
   }

   public List<ChatRoomData> getFavouriteChatRooms(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select c.* from chatroom c, chatroombookmark b where c.name = b.chatroomname and b.username = ? order by b.datecreated desc limit ?");
         ps.setString(1, user.getUsername());
         int maxChatRoomBookmarks = 33;

         try {
            maxChatRoomBookmarks = SystemProperty.getInt("MaxChatRoomBookmarks");
         } catch (NoSuchFieldException var13) {
            log.error("Failed to get MaxChatRoomBookmarks from System Properties will use 33 instead, should not reach here! MUST add the default value into system table", var13);
         }

         ps.setInt(2, maxChatRoomBookmarks);
         rs = ps.executeQuery();
         LinkedList chatRooms = new LinkedList();

         while(rs.next()) {
            chatRooms.add(new ChatRoomData(rs));
         }

         LinkedList var7 = chatRooms;
         return var7;
      } catch (SQLException var14) {
         log.error(String.format("Failed to get FavouriteChatRooms for user:%s", user), var14);
         var6 = super.getFavouriteChatRooms(user);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public List<String> getAllRecentChatRooms(UserObject user) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      List var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select chatroomnames from recentchatrooms where username = ?");
         ps.setString(1, user.getUsername());
         rs = ps.executeQuery();
         List allRecentChatRooms = RecentChatRoomList.newRecentChatRoomList();

         label62:
         while(true) {
            if (rs.next()) {
               String[] rooms = StringUtil.asArray(rs.getString("chatroomnames"));
               String[] arr$ = rooms;
               int len$ = rooms.length;
               int i$ = 0;

               while(true) {
                  if (i$ >= len$) {
                     continue label62;
                  }

                  String room = arr$[i$];
                  allRecentChatRooms.add(room);
                  ++i$;
               }
            }

            var6 = allRecentChatRooms;
            return var6;
         }
      } catch (SQLException var15) {
         log.error(String.format("Failed to get AllRecentChatRooms for user:%s", user), var15);
         var6 = super.getAllRecentChatRooms(user);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public List<ChatRoomData> getRecentChatRooms(List<String> allRecentChatRooms) throws DAOException {
      if (allRecentChatRooms.isEmpty()) {
         return Collections.emptyList();
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         List var6;
         try {
            conn = DBUtils.getFusionReadConnection();
            ps = conn.prepareStatement("select * from chatroom where status = ? and name in (" + RecentChatRoomList.asString(allRecentChatRooms) + ")");
            ps.setInt(1, ChatRoomData.StatusEnum.ACTIVE.value());
            rs = ps.executeQuery();
            HashMap chatRoomMap = new HashMap();

            while(rs.next()) {
               ChatRoomData chatRoomData = new ChatRoomData(rs);
               chatRoomMap.put(chatRoomData.name.toLowerCase(), chatRoomData);
            }

            List<ChatRoomData> chatRoomList = new LinkedList();
            Iterator i$ = allRecentChatRooms.iterator();

            while(i$.hasNext()) {
               String recentChatRoom = (String)i$.next();
               ChatRoomData chatRoomData = (ChatRoomData)chatRoomMap.get(recentChatRoom.toLowerCase());
               if (chatRoomData != null) {
                  chatRoomList.add(chatRoomData);
               }
            }

            LinkedList var18 = chatRoomList;
            return var18;
         } catch (SQLException var14) {
            log.error(String.format("Failed to get RecentChatRooms for chatrooms:%s", allRecentChatRooms), var14);
            var6 = super.getRecentChatRooms(allRecentChatRooms);
         } finally {
            DBUtils.closeResource(rs, ps, conn, log);
         }

         return var6;
      }
   }

   public ChatRoomData getSimpleChatRoomData(String normalizedChatRoomName) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ChatRoomData var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select * from chatroom where name = ? and status = ?");
         ps.setString(1, normalizedChatRoomName);
         ps.setInt(2, ChatRoomData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         ChatRoomData room;
         if (rs.next() && normalizedChatRoomName.equalsIgnoreCase(rs.getString("name"))) {
            room = new ChatRoomData(rs);

            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var14) {
               rs = null;
               log.warn("Failed to close ResultSet", var14);
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var13) {
               ps = null;
               log.warn("Failed to close PreparedStatement", var13);
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.MULTI_ID_CONTROL_ENABLED)) {
               ps = conn.prepareStatement("select * from chatroomextradata where chatroomid = ?");
               ps.setInt(1, room.id);
               rs = ps.executeQuery();
               room.updateExtraData(rs);
            }

            var6 = room;
            return var6;
         }

         room = null;
         return room;
      } catch (SQLException var15) {
         log.error(String.format("Failed to get SimpleChatRoomData for chatrooms:%s", normalizedChatRoomName), var15);
         var6 = super.getSimpleChatRoomData(normalizedChatRoomName);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public Map<String, String> getChatRoomTheme(int themeID) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Map var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select attributekey,attributevalue from chatroomtheme, chatroomthemeattribute where chatroomtheme.id=? and chatroomtheme.status=? and chatroomtheme.id=chatroomthemeattribute.chatroomthemeid");
         ps.setInt(1, themeID);
         ps.setInt(2, 1);
         rs = ps.executeQuery();
         HashMap theme = new HashMap();

         while(rs.next()) {
            theme.put(rs.getString("attributekey"), rs.getString("attributevalue"));
         }

         HashMap var13 = theme;
         return var13;
      } catch (SQLException var11) {
         log.error(String.format("Failed to get ChatRoomTheme for themeID:%s", themeID), var11);
         var6 = super.getChatRoomTheme(themeID);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public Set<String> getChatRoomModerators(ChatRoomData chatroom) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Set var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select username from chatroommoderator where chatroomid=?");
         ps.setInt(1, chatroom.id);
         rs = ps.executeQuery();
         HashSet moderators = new HashSet();

         while(rs.next()) {
            moderators.add(rs.getString("username"));
         }

         HashSet var13 = moderators;
         return var13;
      } catch (SQLException var11) {
         log.error(String.format("Failed to get ChatRoomModerators for chat room:%s", chatroom.id), var11);
         var6 = super.getChatRoomModerators(chatroom);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }

   public Set<String> getChatRoomBannedUsers(ChatRoomData chatroom) throws DAOException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Set var6;
      try {
         conn = DBUtils.getFusionReadConnection();
         ps = conn.prepareStatement("select username from chatroombanneduser where chatroomid=?");
         ps.setInt(1, chatroom.id);
         rs = ps.executeQuery();
         HashSet bannedUsers = new HashSet();

         while(rs.next()) {
            bannedUsers.add(rs.getString("username"));
         }

         HashSet var13 = bannedUsers;
         return var13;
      } catch (SQLException var11) {
         log.error(String.format("Failed to get ChatRoomBannedUsers for chat room:%s", chatroom.id), var11);
         var6 = super.getChatRoomBannedUsers(chatroom);
      } finally {
         DBUtils.closeResource(rs, ps, conn, log);
      }

      return var6;
   }
}
