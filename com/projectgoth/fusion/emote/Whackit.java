package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.slice.FusionException;
import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class Whackit extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Whackit.class));
   private static final SecureRandom RANDOM_GENERATOR = new SecureRandom();
   private static final String WHACKIT_VALUE_SEPARATOR = ";;;";
   private static final Pattern WHACKIT_VALUE_PATTERN = Pattern.compile("^([0-9]+)-([0-9]+):(.*)$");
   private static final String DEFAULT_WHACKIT_VALUES = StringUtil.join((Object[])(new String[]{"1-10:Useless - Is that all you got?", "11-20:Pathetic - You can do better than that!", "21-30:Weak - Whack it harder!", "31-40:Powerless - Are you tired already??", "41-50:Average - You know you want to go higher!", "51-60:Strong - Almost there! More!", "61-70:Mighty - You are doing great!", "71-80:Powerful - Well done! Beat the others!", "81-90:Awesome - Great job! Keep it up!", "91-99:Amazing - Wow! You're a power machine!", "100-100:Supreme - Woohoo! You are superhuman!"}), ";;;");

   public Whackit(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      int strength = RANDOM_GENERATOR.nextInt(100) + 1;
      String whackitString = SystemProperty.get("EmoteWhackitValues", DEFAULT_WHACKIT_VALUES);
      String[] values = whackitString.split(";;;");
      String whackitValue = null;

      for(int i = 0; i < values.length; ++i) {
         Matcher m = WHACKIT_VALUE_PATTERN.matcher(values[i]);
         if (m.matches()) {
            int start = Integer.parseInt(m.group(1));
            int end = Integer.parseInt(m.group(2));
            if (strength >= start && strength <= end) {
               whackitValue = m.group(3);
               break;
            }
         }
      }

      if (whackitValue != null) {
         messageData.messageText = String.format(this.emoteCommandData.getMessageText(), strength, whackitValue);
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToAllUsersInChat(messageData);
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      } else {
         log.error(String.format("Failed to handle /whackit emote: user=%s, strength=%d, values=%s", chatSource.getParentUsername(), strength, whackitString));
         return EmoteCommand.ResultType.NOTHANDLED;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
