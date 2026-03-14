package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.objectcache.ChatSourceSession;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class EmoteCommand {
   protected EmoteCommandData emoteCommandData;
   private IcePrxFinder icePrxFinder;
   private static final String MESSAGE_VARIABLE_BOUNDARY = ";;";
   private static final Pattern MESSAGE_VARIABLE_PATTERN = Pattern.compile(";;([A-Z_]+);;");
   private static Map<String, EmoteCommand.EmoteMessageVariableProcessor> variableProcessors = new HashMap<String, EmoteCommand.EmoteMessageVariableProcessor>() {
      {
         this.put("USER", new EmoteCommand.EmoteMessageVariableProcessor() {
            public String processVariable(String variableName, String targetUsername, String senderUsername) throws FusionException {
               return targetUsername != null && targetUsername.equals(senderUsername) ? "You" : senderUsername;
            }
         });
         this.put("USER_S", new EmoteCommand.EmoteMessageVariableProcessor() {
            public String processVariable(String variableName, String targetUsername, String senderUsername) throws FusionException {
               return targetUsername != null && targetUsername.equals(senderUsername) ? "Your" : senderUsername + "'s";
            }
         });
      }
   };
   private static EmoteCommand.EmoteMessageVariableProcessor defaultProcessor = new EmoteCommand.EmoteMessageVariableProcessor() {
      public String processVariable(String variableName, String targetUsername, String senderUsername) throws FusionException {
         return variableName;
      }
   };

   public EmoteCommandData getEmoteCommandData() {
      return this.emoteCommandData;
   }

   public void setEmoteCommandData(EmoteCommandData emoteCommandData) {
      this.emoteCommandData = emoteCommandData;
   }

   public EmoteCommand(EmoteCommandData emoteCommandData) {
      this.emoteCommandData = emoteCommandData;
   }

   public abstract EmoteCommandState createDefaultState(ChatSource.ChatType var1);

   protected boolean isHelpCommand(MessageData messageData, ChatSource chatSource) {
      String[] values = messageData.messageText.split("\\s", 2);
      return values.length < 2 || StringUtil.isBlank(values[1]) || "help".equalsIgnoreCase(values[1].trim());
   }

   protected String[] getHelpMessage(ChatSource chatSource) throws FusionException {
      return null;
   }

   protected boolean showHelpMessages(String[] helpMessages, MessageData messageData, ChatSource chatSource) throws FusionException {
      if (helpMessages != null && helpMessages.length != 0 && (helpMessages.length != 1 || !StringUtil.isBlank(helpMessages[0]))) {
         throw new FusionException(StringUtil.join((Object[])helpMessages, "\n"));
      } else {
         return false;
      }
   }

   protected boolean handleHelpCommand(MessageData messageData, ChatSource chatSource) throws FusionException {
      return this.isHelpCommand(messageData, chatSource) ? this.showHelpMessages(this.getHelpMessage(chatSource), messageData, chatSource) : false;
   }

   protected abstract EmoteCommand.ResultType execute(MessageData var1, ChatSource var2) throws FusionException;

   public EmoteCommand.ResultType execute(MessageData messageData, ChatSourceSession session, ChatRoomPrx chatRoomPrx) throws FusionException {
      return this.execute(messageData, ChatSource.createChatSourceForChatRoom(session, chatRoomPrx));
   }

   public EmoteCommand.ResultType execute(MessageData messageData, ChatSourceSession session, GroupChatPrx groupChatPrx) throws FusionException {
      return this.execute(messageData, ChatSource.createChatSourceForGroupChat(session, groupChatPrx));
   }

   public EmoteCommand.ResultType execute(MessageData messageData, ChatSourceSession session, String destinationUsername) throws FusionException {
      return this.execute(messageData, ChatSource.createChatSourceForPrivateChat(session, destinationUsername));
   }

   private static EmoteCommand.EmoteMessageVariableProcessor getVariableProcessor(String variableName) {
      EmoteCommand.EmoteMessageVariableProcessor p = (EmoteCommand.EmoteMessageVariableProcessor)variableProcessors.get(variableName);
      return p == null ? defaultProcessor : p;
   }

   public static String processMessageVariables(String messageText, String targetUsername, ChatSource chatSource) throws FusionException {
      return processMessageVariables(messageText, targetUsername, chatSource.getParentUsername());
   }

   public static String processMessageVariables(String messageText, String targetUsername, String senderUsername) throws FusionException {
      Matcher m = MESSAGE_VARIABLE_PATTERN.matcher(messageText);
      if (!m.find()) {
         return messageText;
      } else {
         StringBuffer sb = new StringBuffer();

         do {
            m.appendReplacement(sb, getVariableProcessor(m.group(1)).processVariable(m.group(1), targetUsername, senderUsername));
         } while(m.find());

         m.appendTail(sb);
         return sb.toString();
      }
   }

   protected void checkRateLimit(Class<? extends EmoteCommand> clazz, String keyToRateLimit, String rateLimitStr) throws FusionException {
      String rateLimit = StringUtil.isBlank(rateLimitStr) ? SystemProperty.get("DefaultEmoteRateLimitExpr", "1/5S") : rateLimitStr;

      try {
         MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.GENERIC_EMOTE.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.GENERIC_EMOTE_RATE_LIMIT, String.format("%s:%s", clazz.getSimpleName(), keyToRateLimit)), rateLimit);
      } catch (MemCachedRateLimiter.LimitExceeded var6) {
         throw new FusionException(String.format("Sorry, you can only do " + var6.getPrettyMessage(), this.getEmoteCommandData().getCommandName()));
      } catch (MemCachedRateLimiter.FormatError var7) {
         throw new FusionException("Internal Error");
      }
   }

   public EmoteCommand setIcePrxFinder(IcePrxFinder icePrxFinder) {
      this.icePrxFinder = icePrxFinder;
      return this;
   }

   public IcePrxFinder getIcePrxFinder() throws FusionException {
      if (null == this.icePrxFinder) {
         throw new FusionException("IcePrxFinder not initialized");
      } else {
         return this.icePrxFinder;
      }
   }

   public static boolean hasMessageVariables(String s) {
      return s.indexOf(";;") != -1;
   }

   public interface EmoteMessageVariableProcessor {
      String processVariable(String var1, String var2, String var3) throws FusionException;
   }

   public static enum ResultType {
      HANDLED_AND_STOP(1),
      HANDLED_AND_CONTINUE(2),
      NOTHANDLED(3);

      private int value;

      private ResultType(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static EmoteCommand.ResultType fromValue(int value) {
         EmoteCommand.ResultType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            EmoteCommand.ResultType e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
