package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.FusionException;

public class Lookout extends EmoteCommand {
   public Lookout(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length == 1) {
         throw new FusionException("Please specify who you like to lookout for");
      } else {
         String targetUsername = args[1];

         try {
            User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            userBean.createLookout(chatSource.getParentUsername(), targetUsername);
         } catch (Exception var7) {
            throw new FusionException(var7.getMessage());
         }

         messageData.messageText = "A lookout for " + targetUsername + " has been created. You will be notified via SMS when " + targetUsername + " logs in";
         this.emoteCommandData.updateMessageData(messageData);
         chatSource.sendMessageToSender(messageData);
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }
}
