package com.projectgoth.fusion.eventqueue.events;

import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.eventqueue.Event;

public class VirtualGiftSentEvent extends Event {
   public VirtualGiftSentEvent() {
      super(Enums.EventTypeEnum.VIRTUAL_GIFT_PURCHASED);
   }

   public VirtualGiftSentEvent(String senderUsername, String recipientUsername, String giftName, int virtualGiftReceivedId) {
      super(senderUsername, Enums.EventTypeEnum.VIRTUAL_GIFT_PURCHASED);
      this.putParameter("recipient", recipientUsername);
      this.putParameter("giftName", giftName);
      this.putParameter("virtualGiftReceivedID", Integer.toString(virtualGiftReceivedId));
   }
}
