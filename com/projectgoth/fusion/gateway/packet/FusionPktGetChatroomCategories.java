package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetChatroomCategories;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.LoginChatroomCategoryList;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

public class FusionPktGetChatroomCategories extends FusionPktDataGetChatroomCategories {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetChatroomCategories.class));

   public FusionPktGetChatroomCategories(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetChatroomCategories(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      String username = connection.getUsername();
      ArrayList packets = new ArrayList();

      try {
         List<ChatroomCategoryData> chatroomCategories = LoginChatroomCategoryList.get(username, connection.getUserID());
         Iterator i$ = chatroomCategories.iterator();

         while(i$.hasNext()) {
            ChatroomCategoryData category = (ChatroomCategoryData)i$.next();
            packets.add(new FusionPktChatroomCategory(this.transactionId, category));
         }

         packets.add(new FusionPktGetChatroomCategoriesComplete(this.transactionId));
      } catch (Exception var7) {
         log.error("Unable to get chatroom categories for user [" + username + "]: " + var7.getMessage());
      }

      return (FusionPacket[])packets.toArray(new FusionPacket[packets.size()]);
   }
}
