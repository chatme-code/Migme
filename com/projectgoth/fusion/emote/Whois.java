package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserProfileData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserPrx;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Arrays;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class Whois extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Whois.class));

   public Whois(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length < 2) {
         throw new FusionException("Usage: /whois [username]");
      } else {
         String target = args[1];
         String source = chatSource.getParentUsername();
         String rateLimit = SystemProperty.get("WhoisRateLimitExpr", "1/5S");
         super.checkRateLimit(Whois.class, "s:" + messageData.source, rateLimit);
         messageData.messageText = String.format("** %s :", target);
         String[] chatrooms = null;
         User userBean = null;
         MIS misBean = null;
         boolean isSourceGlobalAdmin = false;
         boolean isTargetGlobalAdmin = false;
         boolean isUserFound = false;

         int targetPresence;
         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            misBean = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);

            try {
               int userId = userBean.getUserID(target, (Connection)null);
               if (userId > 0) {
                  UserData targetUserData = userBean.loadUser(target, false, false);
                  UserData sourceUserData = userBean.loadUser(chatSource.getParentUsername(), false, false);
                  UserProfileData profileData = userBean.getUserProfile(chatSource.getParentUsername(), target, false);
                  isSourceGlobalAdmin = sourceUserData.chatRoomAdmin;
                  isTargetGlobalAdmin = targetUserData.chatRoomAdmin;
                  String country = misBean.getCountry(targetUserData.countryID).name;
                  targetPresence = MemCacheOrEJB.getUserReputationLevel(target);
                  String gender = "Unknown";
                  if (null != profileData.gender) {
                     gender = profileData.gender == UserProfileData.GenderEnum.MALE ? "Male" : "Female";
                  }

                  String profileTemplate = " Gender: %s, migLevel: %d, Location: %s.";
                  messageData.messageText = messageData.messageText + String.format(profileTemplate, gender, targetPresence, country);
                  isUserFound = true;
               }
            } catch (FusionEJBException var28) {
               log.debug("unable to load user profile for " + target, var28);
            } catch (RemoteException var29) {
               log.debug("unable to load user profile for " + target, var29);
            }
         } catch (CreateException var30) {
            log.error("Unknown error while loading user profile", var30);
         }

         if (isUserFound) {
            String presenceText = isSourceGlobalAdmin ? " Status: offline." : "";
            String chatRoomText = "";

            try {
               UserPrx userPrx = this.getIcePrxFinder().getRegistry(false).findUserObject(target);
               if (null != userPrx) {
                  chatrooms = userPrx.getCurrentChatrooms();
                  boolean isWhoisSelf = target.equals(source);
                  java.util.List<String> broadcastList = Arrays.asList(userPrx.getBroadcastList());
                  targetPresence = userPrx.getOverallFusionPresence((String)null);
                  if (isSourceGlobalAdmin) {
                     presenceText = String.format(" Status: %s.", PresenceType.fromValue(targetPresence).toString().toLowerCase());
                  }

                  if ((isSourceGlobalAdmin || isWhoisSelf || broadcastList.contains(source) && PresenceType.OFFLINE.value() != targetPresence) && chatrooms.length > 0) {
                     if (!isSourceGlobalAdmin && isTargetGlobalAdmin) {
                        chatRoomText = String.format(" Chatting in : ***.");
                     } else {
                        chatRoomText = String.format(" Chatting in : %s.", StringUtil.join((Object[])chatrooms, ","));
                     }
                  }
               }
            } catch (Exception var31) {
               log.debug("unable to find target user " + args[1], var31);
            } finally {
               messageData.messageText = messageData.messageText + presenceText;
               messageData.messageText = messageData.messageText + chatRoomText;
            }
         } else {
            messageData.messageText = messageData.messageText + " Not Found.";
         }

         messageData.messageText = messageData.messageText + " **";
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
