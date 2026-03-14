package com.projectgoth.fusion.emailalert;

import Ice.LocalException;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.Date;

public class UnreadCountConnectionNotifier implements Runnable {
   String username;
   String password;
   UserPrx userProxy;
   Date dateRequestReceived;

   public UnreadCountConnectionNotifier(String username, String password, UserPrx userProxy) {
      this.username = username;
      this.password = password;
      this.userProxy = userProxy;
      this.dateRequestReceived = new Date();
   }

   public void run() {
      try {
         Date now = new Date();
         if (now.getTime() - this.dateRequestReceived.getTime() > 1200000L) {
            EmailAlert.discardedGatewayQueriesCounter.add();
            return;
         }

         int unreadEmailCount = EmailAlert.getUnreadEmailCountFromIMAP(this.username, this.password);
         EmailAlert.logger.debug("Notifying Gateway that unread email count for '" + this.username + "' is " + unreadEmailCount);
         this.userProxy.emailNotification(unreadEmailCount);
      } catch (LocalException var8) {
      } catch (Exception var9) {
      } finally {
         EmailAlert.processedGatewayQueriesCounter.add();
      }

   }
}
