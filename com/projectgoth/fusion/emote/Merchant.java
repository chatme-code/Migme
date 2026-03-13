package com.projectgoth.fusion.emote;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.FusionException;
import java.sql.Connection;
import java.util.Map;
import org.apache.log4j.Logger;

public class Merchant extends EmoteCommand {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Merchant.class));

   public Merchant(EmoteCommandData emoteCommandData) {
      super(emoteCommandData);
   }

   protected EmoteCommand.ResultType execute(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] args = messageData.getArgs();
      if (args.length > 1 && "tag".equals(args[1]) && chatSource.getSessionI().getUserType() == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
         this.handleMerchantTagCommand(messageData, chatSource);
         return EmoteCommand.ResultType.HANDLED_AND_STOP;
      } else {
         return EmoteCommand.ResultType.NOTHANDLED;
      }
   }

   public EmoteCommandState createDefaultState(ChatSource.ChatType chatType) {
      return null;
   }

   private void handleMerchantTagCommand(MessageData messageData, ChatSource chatSource) throws FusionException {
      String[] participants = chatSource.getVisibleUsernamesInChat(false);

      try {
         Map<String, String> tags = MemCachedClientWrapper.getMultiString(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, participants);
         Account accountBean = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         String[] arr$ = participants;
         int len$ = participants.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String participant = arr$[i$];
            String merchant = (String)tags.get(participant);
            if (merchant == null) {
               boolean fromSlave = true;
               MerchantTagData merchantTagData = accountBean.getMerchantTagFromUsername((Connection)null, participant, fromSlave);
               if (merchantTagData == null) {
                  chatSource.getSessionI().sendMessageBackToUserAsEmote(messageData, participant + " _");
                  continue;
               }

               UserData merchantUserData = userBean.loadUserFromID(merchantTagData.merchantUserID);
               merchant = merchantUserData.username;
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, participant, merchant);
            }

            if (merchant.equals(chatSource.getSessionI().getUsername())) {
               chatSource.getSessionI().sendMessageBackToUserAsEmote(messageData, participant + " *");
            } else {
               chatSource.getSessionI().sendMessageBackToUserAsEmote(messageData, participant + " O");
            }
         }

      } catch (Exception var15) {
         log.error("Unable to retrive merchant tag information: " + var15.getMessage(), var15);
         throw new FusionException("Unable to retrieve tag information now. Please try again later.");
      }
   }
}
