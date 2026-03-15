package com.projectgoth.fusion.rewardsystem.mmv2;

import com.mig33.rabbitmqclient.RabbitMQ;
import com.mig33.rabbitmqclient.settings.Settings;

public class MMv2RabbitMQ extends RabbitMQ {
   private MMv2RabbitMQ(Settings settings) {
      super(settings);
   }

   public static RabbitMQ getInstance() {
      return MMv2RabbitMQ.MMv2RabbitMQHolder.INSTANCE;
   }

   // $FF: synthetic method
   MMv2RabbitMQ(Settings x0, Object x1) {
      this(x0);
   }

   public static class MMv2RabbitMQHolder {
      private static final MMv2RabbitMQ INSTANCE = new MMv2RabbitMQ(new MMv2RMQClientSettings());
   }
}
