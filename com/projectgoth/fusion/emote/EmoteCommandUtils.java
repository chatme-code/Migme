package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.cache.MemCacheOrEJB;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.UsernameUtils;
import com.projectgoth.fusion.common.UsernameValidationException;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MessageDestinationData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.gateway.packet.FusionPktMidletTab;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.slice.FusionException;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import org.apache.log4j.Logger;

public class EmoteCommandUtils {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EmoteCommandUtils.class));
   private static final Pattern TWO_USERNAME_COMMAND_PATTERN = Pattern.compile("([a-z0-9._-]+)\\s+([a-z0-9._-]+)", 2);
   private static final Pattern TWO_USERNAME_SINGLE_COMMAND_PATTERN = Pattern.compile("([a-z0-9._-]+)", 2);
   private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();
   private static final String VALUE_SEPARATOR = ";;;";

   public static EmoteCommandUtils.TwoUsernameCommand parseTwoUsernameCommand(String command, MessageData messageData, ChatSource chatSource, boolean allowSameUser) throws FusionException {
      String[] params = messageData.messageText.split("\\s+", 2);
      String username1 = null;
      String username2 = null;
      if (params.length == 2) {
         Matcher m = TWO_USERNAME_COMMAND_PATTERN.matcher(params[1]);
         if (m.matches()) {
            username1 = m.group(1).toLowerCase();
            username2 = m.group(2).toLowerCase();
         } else {
            m = TWO_USERNAME_SINGLE_COMMAND_PATTERN.matcher(params[1]);
            if (m.matches()) {
               username1 = messageData.source.toLowerCase();
               username2 = m.group(1).toLowerCase();
            }
         }
      }

      if (username1 == null && chatSource.getChatType() == ChatSource.ChatType.PRIVATE_CHAT) {
         username1 = messageData.source.toLowerCase();
         username2 = ((MessageDestinationData)messageData.messageDestinations.get(0)).destination.toLowerCase();
      }

      if (username1 == null) {
         throw new FusionException(String.format("Usage: /%s [username1] [username2]", command));
      } else if (!allowSameUser && username1.equals(username2)) {
         throw new FusionException(String.format("Usage: /%s [username1] [username2]", command));
      } else {
         String[] arr$ = new String[]{username1, username2};
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String username = arr$[i$];
            if (!chatSource.isUserVisibleInChat(username)) {
               throw new FusionException(username + " is not in the chat");
            }
         }

         return new EmoteCommandUtils.TwoUsernameCommand(username1, username2);
      }
   }

   public static String createRandomTextCandidateString(String[] candidates) {
      return StringUtil.join((Object[])candidates, ";;;");
   }

   public static String getRandomTextBySystemProperty(String propertyName, String defaultTexts) {
      return getRandomTextBySystemProperty(propertyName, defaultTexts, ";;;");
   }

   public static String getRandomTextBySystemProperty(String propertyName, String defaultTexts, String separator) {
      return getRandomText(SystemProperty.get(propertyName, defaultTexts), separator);
   }

   public static String getRandomText(String texts) {
      return getRandomText(texts, ";;;");
   }

   public static String getRandomText(String texts, String separator) {
      return texts != null && separator != null ? getRandomText(texts.split(separator)) : null;
   }

   public static String getRandomText(String[] texts) {
      return texts.length == 0 ? null : texts[RANDOM_GENERATOR.nextInt(texts.length)];
   }

   public static EmoteCommand.ResultType createNewTab(String commandName, String urlSuffix, MessageData messageData, ChatSource chatSource, EmoteCommandData emoteCommandData) throws FusionException {
      ClientType deviceType = chatSource.getSessionI().getDeviceType();
      short clientVersion = chatSource.getSessionI().getClientVersion();
      int minClientVersion = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.NEW_TAB_CLIENT_VERSION_MIN);
      if (clientVersion >= minClientVersion && ClientType.canShowMidletTabs(deviceType)) {
         String webServerUrl = "";

         try {
            webServerUrl = SystemProperty.get("WebServerURL");
         } catch (NoSuchFieldException var10) {
            throw new FusionException(var10.getMessage());
         }

         FusionPktMidletTab pktMidletTab = new FusionPktMidletTab();
         pktMidletTab.setURL(webServerUrl + urlSuffix + "&v=midlet");
         pktMidletTab.setFocus((byte)1);
         chatSource.getSessionI().getConnectionProxy().putSerializedPacket(pktMidletTab.toSerializedBytes());
      } else {
         messageData.messageText = "The /" + commandName + " command is not supported on this client.";
         emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
      }

      return EmoteCommand.ResultType.HANDLED_AND_STOP;
   }

   public static String getSanitizedUsername(String username) throws EmoteCommandException {
      try {
         UsernameUtils.validateUsernameLength(username);
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         UserData userData = userBean.loadUserByUsernameOrAlias(UsernameUtils.normalizeUsername(username), false, false);
         if (userData == null) {
            throw new EmoteCommandException(ErrorCause.EmoteCommandError.INVALID_USER, new Object[]{StringUtil.truncateWithEllipsis(username, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.MAX_USERNAME_LENGTH_DISPLAY))});
         } else {
            return userData.username;
         }
      } catch (UsernameValidationException var3) {
         throw new EmoteCommandException(ErrorCause.EmoteCommandError.INVALID_USER, new Object[]{StringUtil.isBlank(username) ? "" : StringUtil.truncateWithEllipsis(username, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Emote.MAX_USERNAME_LENGTH_DISPLAY))});
      } catch (CreateException var4) {
         log.error("Create Exception", var4);
         throw new EmoteCommandException(var4, ErrorCause.EmoteCommandError.INTERNAL_ERROR, new Object[0]);
      } catch (RemoteException var5) {
         log.error("Remote Exception", var5);
         throw new EmoteCommandException(var5, ErrorCause.EmoteCommandError.INTERNAL_ERROR, new Object[0]);
      }
   }

   public static boolean clientMeetsMinVersion(int clientVersion, ClientType deviceType) {
      try {
         Short minClientVersion = MemCacheOrEJB.getMinimumClientVersionForAccess(deviceType.value(), GuardCapabilityEnum.EMOTES_MIN_VERSION.value());
         if (minClientVersion != null && clientVersion >= minClientVersion) {
            return true;
         }
      } catch (EJBException var3) {
         log.warn(var3);
      } catch (RemoteException var4) {
         log.warn(var4);
      } catch (CreateException var5) {
         log.warn(var5);
      } catch (FusionEJBException var6) {
         log.warn(var6);
      }

      return false;
   }

   public static class TwoUsernameCommand {
      public String username1;
      public String username2;

      public TwoUsernameCommand(String username1, String username2) {
         this.username1 = username1;
         this.username2 = username2;
      }

      public String toString() {
         return String.format("[TwoUsernameCommand: %s %s]", this.username1, this.username2);
      }
   }
}
