package com.projectgoth.fusion.botservice.message;

import com.projectgoth.fusion.botservice.bot.Bot;
import com.projectgoth.fusion.common.ConfigUtils;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executor;
import org.apache.log4j.Logger;

public class MessageQueue implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MessageQueue.class));
   private Bot bot;
   private Executor executor;
   private Queue<Message> messages = new LinkedList();

   public MessageQueue(Bot bot, Executor executor) {
      this.bot = bot;
      this.executor = executor;
   }

   public void run() {
      Message message;
      synchronized(this.messages) {
         message = (Message)this.messages.peek();
         if (message == null) {
            return;
         }
      }

      try {
         message.dispatch(this.bot);
      } catch (Exception var6) {
         String botName = "";
         if (this.bot != null) {
            botName = this.bot.getBotData() != null ? this.bot.getBotData().getDisplayName() : "";
         }

         log.warn("Unexpected exception while dispatching message for " + botName, var6);
      }

      synchronized(this.messages) {
         this.messages.poll();
         if (this.messages.size() > 0) {
            this.executor.execute(this);
         }

      }
   }

   public void queue(Message message) {
      synchronized(this.messages) {
         this.messages.add(message);
         if (this.messages.size() == 1) {
            this.executor.execute(this);
         }

      }
   }
}
