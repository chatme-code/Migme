package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.ChatRoomDataIce;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.sql.Connection;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class Unfollow extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Follow.class));
   private static final String USAGE_STR = "Usage: /unfollow or /uf [username]";

   public Unfollow(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length != 2) {
         throw new FusionException("Usage: /unfollow or /uf [username]");
      } else {
         String rateLimitPerUser = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.UNFOLLOW_RATE_LIMIT);
         super.checkRateLimit(Unfollow.class, "s:" + messageData.source, rateLimitPerUser);
         if (!EmoteCommandUtils.clientMeetsMinVersion(chatSource.getSessionI().getClientVersion(), chatSource.getSessionI().getDeviceType())) {
            throw new FusionException("/unfollow is not supported on this client device and version");
         } else {
            String usernameToUnfollow = args[1];

            int groupId;
            try {
               User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               groupId = userBean.getUserID(messageData.source, (Connection)null);
               Contact contactBean = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               ContactData contactData = contactBean.getContact(messageData.source, usernameToUnfollow);
               if (contactData == null || contactData.id == null) {
                  throw new FusionException("You are not following user " + usernameToUnfollow);
               }

               contactBean.removeFusionUserFromContact(groupId, messageData.source, contactData.id, true);
               messageData.messageText = "You have unfollowed " + usernameToUnfollow;
               chatSource.sendMessageToSender(messageData);
            } catch (CreateException var10) {
               log.error(var10.getMessage());
               throw new FusionException("Unable to unfollow user [" + usernameToUnfollow + "]. Please try again later.");
            } catch (RemoteException var11) {
               log.error(var11.getMessage());
               throw new FusionException("Unable to unfollow user [" + usernameToUnfollow + "]. Please try again later.");
            } catch (EJBException var12) {
               log.error(var12.getMessage());
               throw new FusionException("Unable to unfollow user [" + usernameToUnfollow + "]. Please try again later.");
            } catch (FusionEJBException var13) {
               log.error(var13.getMessage());
               throw new FusionException(var13.getMessage());
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.LOG_ALL_EMOTES_ENABLED)) {
               int chatroomId = -1;
               groupId = -1;
               if (chatSource.getChatType() == ChatSource.ChatType.CHATROOM_CHAT) {
                  ChatRoomPrx chatRoomPrx = chatSource.castToLocalChatRoomChatSource().getChatRoomPrx();
                  ChatRoomDataIce roomData = chatRoomPrx.getRoomData();
                  chatroomId = roomData.id;
                  groupId = roomData.groupID;
               }

               ChatRoomEmoteLogData logData = new ChatRoomEmoteLogData(messageData.source, usernameToUnfollow, args[0], chatroomId, groupId, -1, (String)null);
               chatSource.getSessionI().logEmoteData(logData);
            }

            return EmoteCommand.ResultType.HANDLED_AND_STOP;
         }
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
