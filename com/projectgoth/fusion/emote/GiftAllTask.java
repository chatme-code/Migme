package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.data.VirtualGiftReceivedData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.objectcache.ChatObjectManagerRoom;
import com.projectgoth.fusion.objectcache.ChatSourceRoom;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.SessionPrx;
import java.rmi.RemoteException;
import java.util.Arrays;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class GiftAllTask implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GiftAllTask.class));
   private static final Logger metricslog = Logger.getLogger("GiftAllMetrics");
   private int giftId = -1;
   private String message = null;
   private MessageData messageData = null;
   private ChatSourceRoom chatRoom = null;
   private ChatObjectManagerRoom objectManager = null;
   private SessionPrx sessionProxy = null;

   public GiftAllTask(int giftId, String message, MessageData messageData, ChatSourceRoom chatRoom, ChatObjectManagerRoom objectManager, SessionPrx sessionPrx) {
      this.giftId = giftId;
      this.message = message;
      this.messageData = messageData;
      this.chatRoom = chatRoom;
      this.objectManager = objectManager;
      this.sessionProxy = sessionPrx;
   }

   public void run() {
      long executionBeginTime = System.currentTimeMillis();
      this.objectManager.getGiftAllSemaphore().acquireUninterruptibly();
      long acquisitionTime = System.currentTimeMillis();
      ChatSource chatSource = ChatSource.createChatSourceForChatRoom(this.sessionProxy, this.chatRoom);

      try {
         String userName = this.messageData.username;
         ChatRoomData chatRoomData = this.chatRoom.getNewRoomData();
         if (!this.chatRoom.isParticipant(userName)) {
            log.warn(userName + " left the chatroom " + chatRoomData.name + ". Dropping the gift all request");
            return;
         }

         java.util.List<String> participants = Arrays.asList(this.chatRoom.getParticipants(userName));
         if (participants.size() != 0) {
            Content contentBean = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
            VirtualGiftData gift = contentBean.getVirtualGift(this.giftId, (String)null, userName);
            double lowPriceThreshold = contentBean.isGiftLowPrice(gift, userName);
            String[] args = this.messageData.messageText.toLowerCase().split(" ");
            EmoteCommand emoteCommand = EmoteCommandFactory.getEmoteCommand(args[0].substring(1), ChatSource.ChatType.CHATROOM_CHAT, this.objectManager.getIcePrxFinder());
            Gift.giftAll(chatRoomData, this.message, this.chatRoom.getMaximumMessageLength(userName), lowPriceThreshold >= 0.0D, VirtualGiftReceivedData.PurchaseLocationEnum.CHATROOM_COMMAND, participants, gift, this.messageData, emoteCommand.emoteCommandData, chatSource, this.objectManager.getIcePrxFinder());
            long executionCompleteTime = System.currentTimeMillis();
            metricslog.info(String.format("chatRoom:%s, available semaphore permits: %d,  numParticipants:%d, semaphoreAcquisitionTime:%d, taskExecution:%d", chatRoomData.name, this.objectManager.getGiftAllSemaphore().availablePermits(), participants.size(), acquisitionTime - executionBeginTime, executionCompleteTime - executionBeginTime));
            return;
         }

         this.messageData.messageText = "There are no other users in the room.";
         chatSource.sendMessageToSender(this.messageData);
      } catch (FusionException var32) {
         log.error(var32, var32);
         this.messageData.messageText = var32.getMessage();

         try {
            chatSource.sendMessageToSender(this.messageData);
         } catch (Exception var31) {
         }

         return;
      } catch (CreateException var33) {
         log.error(var33, var33);
         this.messageData.messageText = "Unable to gift all, at the moment. Please try again later.";

         try {
            chatSource.sendMessageToSender(this.messageData);
         } catch (Exception var30) {
         }

         return;
      } catch (RemoteException var34) {
         log.error(var34, var34);
         this.messageData.messageText = "Unable to gift all, at the moment. Please try again later.";

         try {
            chatSource.sendMessageToSender(this.messageData);
         } catch (Exception var29) {
         }

         return;
      } catch (FusionEJBException var35) {
         log.error(var35, var35);
         this.messageData.messageText = var35.getMessage();

         try {
            chatSource.sendMessageToSender(this.messageData);
         } catch (Exception var28) {
         }

         return;
      } finally {
         this.objectManager.getGiftAllSemaphore().release();
      }

   }
}
