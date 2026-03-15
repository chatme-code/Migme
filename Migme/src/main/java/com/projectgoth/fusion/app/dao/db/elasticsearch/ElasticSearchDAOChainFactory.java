package com.projectgoth.fusion.app.dao.db.elasticsearch;

import com.projectgoth.fusion.app.dao.base.ChatRoomDAOChain;
import com.projectgoth.fusion.app.dao.base.DAOChainFactory;

public class ElasticSearchDAOChainFactory extends DAOChainFactory {
   public ChatRoomDAOChain createChatRoomDAOChain() {
      return new ElasticSearchChatRoomDAOChain();
   }
}
