package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.EmoticonData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import java.sql.Connection;
import java.util.ArrayList;

public class StickerDeliveredMessageData {
   private MessageData stickerMessageData;
   private String msgToInstigator;
   private String msgToRecipients;

   public StickerDeliveredMessageData(String[] cmdArgs, MessageData messageData) throws EmoteCommandException {
      try {
         User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         String senderUsername = messageData.source;
         int userId = userEJB.getUserID(senderUsername, (Connection)null, true);
         ContentUtils.checkUserCanSendStickers(userEJB, userId);
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         String sanitizedStickerName = this.sanitizeStickerName(cmdArgs[1]);
         EmoticonData stickerData = contentEJB.getStickerDataByNameForUser(senderUsername, sanitizedStickerName);
         if (stickerData == null) {
            throw new EmoteCommandException(ErrorCause.EmoteCommandError.INVALID_STICKER_NAME, new Object[]{sanitizedStickerName});
         } else {
            messageData.emoticonKeys = new ArrayList();
            messageData.emoticonKeys.add(stickerData.hotKey);
            messageData.contentType = MessageData.ContentTypeEnum.EMOTE;
            messageData.emoteContentType = MessageData.EmoteContentTypeEnum.STICKERS;
            this.stickerMessageData = new MessageData(messageData.toIceObject());
            this.msgToInstigator = String.format("You sent a sticker '%s' %s", stickerData.alias, stickerData.hotKey);
            this.msgToRecipients = String.format("'%s' has sent you a sticker '%s' %s", senderUsername, stickerData.alias, stickerData.hotKey);
         }
      } catch (Exception var9) {
         throw new EmoteCommandException(var9);
      }
   }

   private String sanitizeStickerName(String stickerName) {
      return StringUtil.trimmedLowerCase(stickerName);
   }

   public MessageData getMessageData() {
      return this.stickerMessageData;
   }

   public String getMessageToInstigator() {
      return this.msgToInstigator;
   }

   public String getMessageToRecipients() {
      return this.msgToRecipients;
   }
}
