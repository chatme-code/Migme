package com.projectgoth.fusion.search;

import com.projectgoth.fusion.data.ChatRoomData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.util.StringUtils;

public class ChatRoomsIndex extends BaseIndex {
   public static void updateChatRoom(int chatRoomID, String chatRoomName, JSONObject fieldsToUpdate) throws Exception {
      JSONObject defaultFields = new JSONObject();
      defaultFields.put(ChatRoomsIndex.Field.NAME.toString(), chatRoomName);
      updateDocument(ElasticSearch.IndexType.CHAT_ROOMS, ElasticSearch.DocumentType.CHAT_ROOM, chatRoomID, fieldsToUpdate, defaultFields);
   }

   public static void indexNewChatRoom(int chatRoomID, String chatRoomName, boolean adultOnly, Collection<String> tags) throws Exception {
      JSONObject fieldsToUpdate = new JSONObject();
      fieldsToUpdate.put(ChatRoomsIndex.Field.ADULT_ONLY.toString(), adultOnly);
      if (tags != null) {
         fieldsToUpdate.put(ChatRoomsIndex.Field.TAGS.toString(), new JSONArray(tags));
      }

      updateChatRoom(chatRoomID, chatRoomName, fieldsToUpdate);
   }

   public static void chatRoomAccessed(int chatRoomID, String chatRoomName, Integer primaryCountryID, Integer secondaryCountryID) throws Exception {
      JSONObject fieldsToUpdate = new JSONObject();
      if (primaryCountryID != null) {
         fieldsToUpdate.put(ChatRoomsIndex.Field.PRIMARY_COUNTRY_ID.toString(), primaryCountryID);
      }

      if (secondaryCountryID != null) {
         fieldsToUpdate.put(ChatRoomsIndex.Field.SECONDARY_COUNTRY_ID.toString(), secondaryCountryID);
      }

      fieldsToUpdate.put(ChatRoomsIndex.Field.DATE_LAST_ACCESSED.toString(), System.currentTimeMillis());
      updateChatRoom(chatRoomID, chatRoomName, fieldsToUpdate);
   }

   public static void updateChatRoomTags(int chatRoomID, String chatRoomName, Collection<String> tags) throws Exception {
      JSONObject fieldsToUpdate = new JSONObject();
      fieldsToUpdate.put(ChatRoomsIndex.Field.TAGS.toString(), new JSONArray(tags));
      updateChatRoom(chatRoomID, chatRoomName, fieldsToUpdate);
   }

   public static void updateChatRoomAdultOnlyFlag(int chatRoomID, String chatRoomName, boolean adultOnly) throws Exception {
      JSONObject fieldsToUpdate = new JSONObject();
      fieldsToUpdate.put(ChatRoomsIndex.Field.ADULT_ONLY.toString(), adultOnly);
      updateChatRoom(chatRoomID, chatRoomName, fieldsToUpdate);
   }

   public static List<ChatRoomData> searchChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords, int maxChatRoomsReturned) throws Exception {
      String query = "{\"size\":" + maxChatRoomsReturned + ",";
      if (StringUtils.hasLength(search)) {
         query = query + "\"query\":{\"bool\":{\"should\":[{\"field\":{\"name\":\"" + search + "*\"}}";
         if (searchKeywords) {
            query = query + ",{\"field\":{\"tags\":\"" + search + "*\"}}";
         }

         if (!includeAdultOnly) {
            query = query + ",\"must\":{\"field\":{\"adult_only\":false}}";
         }

         query = query + "]}}}";
      } else {
         query = query + "\"sort\":[{\"date_last_accessed\":{\"reverse\":true}},\"_score\"],\"query\":{\"match_all\":{}}}";
      }

      ElasticSearch.IndexType[] indexTypes = new ElasticSearch.IndexType[]{ElasticSearch.IndexType.CHAT_ROOMS};
      ElasticSearch.DocumentType[] documentTypes = new ElasticSearch.DocumentType[]{ElasticSearch.DocumentType.CHAT_ROOM};
      JSONObject result = ElasticSearch.search(indexTypes, documentTypes, new JSONObject(query));
      JSONArray hits = result.getJSONObject("hits").getJSONArray("hits");
      if (hits.length() == 0) {
         return null;
      } else {
         List<ChatRoomData> rooms = new ArrayList(hits.length());

         for(int i = 0; i < hits.length(); ++i) {
            ChatRoomData room = new ChatRoomData();
            room.id = hits.getJSONObject(i).getInt("_id");
            room.name = hits.getJSONObject(i).getJSONObject("_source").getString("name");
            rooms.add(room);
         }

         return rooms;
      }
   }

   public static enum Field {
      ID("id"),
      NAME("name"),
      ADULT_ONLY("adult_only"),
      TAGS("tags"),
      PRIMARY_COUNTRY_ID("primary_country_id"),
      SECONDARY_COUNTRY_ID("secondary_country_id"),
      DATE_LAST_ACCESSED("date_last_accessed");

      private String value;

      private Field(String value) {
         this.value = value;
      }

      public String toString() {
         return this.value;
      }
   }
}
