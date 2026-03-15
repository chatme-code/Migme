package com.projectgoth.fusion.app.dao.db.elasticsearch;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.base.ChatRoomDAOChain;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.search.ChatRoomsIndex;
import java.util.List;

public class ElasticSearchChatRoomDAOChain extends ChatRoomDAOChain {
   public List<ChatRoomData> getChatRooms(int countryID, String search, String language, boolean includeAdultOnly, boolean searchKeywords) throws DAOException {
      if (!SystemProperty.getBool("DisableElasticSearch", false) && !SystemProperty.getBool("DisableElasticSearchQueries", false)) {
         return super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
      } else {
         try {
            return ChatRoomsIndex.searchChatRooms(countryID, search, language, includeAdultOnly, searchKeywords, SystemProperty.getInt("MaxChatRoomsReturned"));
         } catch (Exception var7) {
            return super.getChatRooms(countryID, search, language, includeAdultOnly, searchKeywords);
         }
      }
   }
}
