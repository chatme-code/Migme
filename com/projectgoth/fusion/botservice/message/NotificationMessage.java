package com.projectgoth.fusion.botservice.message;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.data.BotData;

public class NotificationMessage extends Message {
   private String username;
   private BotData.BotCommandEnum notification;

   public NotificationMessage(String username, BotData.BotCommandEnum notification) {
      this.username = username;
      this.notification = notification;
   }

   public void dispatch(Bot bot) {
      switch(this.notification) {
      case JOIN:
         bot.onUserJoinChannel(this.username);
         break;
      case QUIT:
         bot.onUserLeaveChannel(this.username);
      }

   }
}
