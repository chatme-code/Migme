package com.projectgoth.fusion.restapi.data;

import com.projectgoth.fusion.data.ChatRoomData;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(
   name = "user"
)
public class ChatroomSettingsMultiIDData {
   public Integer minMigLevel;
   public String rateLimitByIP;

   public void retrieveFromChatRoomData(ChatRoomData data) {
      this.minMigLevel = data.minMigLevel;
      this.rateLimitByIP = data.rateLimitByIp;
   }

   public void updateToChatRoomData(ChatRoomData data) {
      data.minMigLevel = this.minMigLevel;
      data.rateLimitByIp = this.rateLimitByIP;
   }
}
