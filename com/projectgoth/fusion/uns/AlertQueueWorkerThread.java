package com.projectgoth.fusion.uns;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.uns.domain.AlertNote;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.log4j.Logger;

public class AlertQueueWorkerThread extends Thread {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AlertQueueWorkerThread.class));
   private BlockingQueue<AlertNote> queue;
   private RegistryPrx registryProxy;
   private boolean dryRun;
   private AtomicLong alertsSent;

   public AlertQueueWorkerThread(BlockingQueue<AlertNote> queue, RegistryPrx registryProxy, boolean dryRun, AtomicLong alertsSent) {
      this.queue = queue;
      this.registryProxy = registryProxy;
      this.dryRun = dryRun;
      this.alertsSent = alertsSent;
   }

   public void run() {
      while(true) {
         try {
            AlertNote note = (AlertNote)this.queue.take();
            String[] usernames = note.getUsersArray();
            UserPrx[] userProxies = this.registryProxy.findUserObjects(usernames);
            if (log.isDebugEnabled()) {
               log.debug("sending alert [" + note.getText() + "] to [" + StringUtil.asString((Object[])userProxies) + "]");
            }

            if (!this.dryRun) {
               UserPrx[] arr$ = userProxies;
               int len$ = userProxies.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  UserPrx userProxy = arr$[i$];
                  userProxy.putAlertMessage(note.getText(), (String)null, (short)0);
                  this.alertsSent.incrementAndGet();
               }
            }
         } catch (Exception var8) {
            log.error("failed to send alert ", var8);
         }
      }
   }
}
