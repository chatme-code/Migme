package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ShortTextStatusUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.userevent.EventTextTranslator;
import com.projectgoth.fusion.userevent.domain.UserEvent;
import org.springframework.util.StringUtils;

public class FusionPktUserEvent extends FusionPacket {
   public FusionPktUserEvent() {
      super((short)422);
   }

   public FusionPktUserEvent(EventTextTranslator translator, ClientType deviceType, String receivingUsername, UserEventIce event) {
      super((short)422);
      this.setSecondsAgo((int)((System.currentTimeMillis() - event.timestamp) / 1000L));
      this.setEventType(UserEvent.getEventType(event).value());
      if (StringUtils.hasLength(event.generatingUserDisplayPicture)) {
         this.setGeneratingUserProfileImageGUID(event.generatingUserDisplayPicture);
      }

      if (event instanceof ShortTextStatusUserEventIce) {
         ((ShortTextStatusUserEventIce)event).status = StringUtil.stripHTML(((ShortTextStatusUserEventIce)event).status);
      }

      this.setText(translator.translate(event, deviceType, receivingUsername));
      if (deviceType == ClientType.AJAX2) {
         this.setTimestamp(event.timestamp);
      }

   }

   public FusionPktUserEvent(FusionPacket packet) {
      super(packet);
   }

   public void setSecondsAgo(int secondsAgo) {
      this.setField((short)1, secondsAgo);
   }

   public void setEventType(byte type) {
      this.setField((short)2, type);
   }

   public void setGeneratingUserProfileImageGUID(String guid) {
      this.setField((short)3, guid);
   }

   public void setText(String text) {
      this.setField((short)4, text);
   }

   public void setTimestamp(Long timeStamp) {
      this.setField((short)5, timeStamp);
   }
}
