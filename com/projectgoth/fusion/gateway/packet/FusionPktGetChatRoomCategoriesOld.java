package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.LoginChatroomCategoryList;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class FusionPktGetChatRoomCategoriesOld extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetChatRoomCategoriesOld.class));

   public FusionPktGetChatRoomCategoriesOld() {
      super((short)713);
   }

   public FusionPktGetChatRoomCategoriesOld(short transactionId) {
      super((short)713, transactionId);
   }

   public FusionPktGetChatRoomCategoriesOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      String username = connection.getUsername();
      ArrayList packets = new ArrayList();

      try {
         List<ChatroomCategoryData> chatroomCategories = LoginChatroomCategoryList.get(username, connection.getUserID());
         Iterator i$ = chatroomCategories.iterator();

         while(i$.hasNext()) {
            ChatroomCategoryData category = (ChatroomCategoryData)i$.next();
            packets.add(new FusionPktChatRoomCategoryOld(this.transactionId, category));
         }

         packets.add(new FusionPktGetChatRoomCategoriesCompleteOld(this.transactionId));
      } catch (Exception var7) {
         log.error("Unable to get chatroom categories for user [" + username + "]: " + var7.getMessage());
      }

      return (FusionPacket[])packets.toArray(new FusionPacket[packets.size()]);
   }
}
