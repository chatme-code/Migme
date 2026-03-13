package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.VirtualGiftData;
import java.util.LinkedList;
import java.util.UUID;

public class GiftSentToUserMessageData extends MessageData {
   public GiftSentToUserMessageData(String parentUsername, String recipientUsername, String giftMessage, VirtualGiftData gift) {
      this.contentType = MessageData.ContentTypeEnum.EMOTE;
      String returnMsg = parentUsername + " gives ";
      if (StringUtil.startsWithaVowel(gift.getName())) {
         returnMsg = returnMsg + "an ";
      } else {
         returnMsg = returnMsg + "a ";
      }

      returnMsg = returnMsg + gift.getName() + " " + gift.getHotKey() + " to " + recipientUsername + "!";
      if (giftMessage != null) {
         returnMsg = returnMsg + " -- " + giftMessage;
      }

      this.messageText = "<< " + returnMsg + " >>";
      this.emoticonKeys = new LinkedList();
      this.emoticonKeys.add(gift.getHotKey());
      this.guid = UUID.randomUUID().toString();
      this.messageTimestamp = System.currentTimeMillis();
   }
}
