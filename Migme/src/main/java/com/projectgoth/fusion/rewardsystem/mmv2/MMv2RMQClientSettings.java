package com.projectgoth.fusion.rewardsystem.mmv2;

import com.mig33.rabbitmqclient.settings.Settings;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;

public class MMv2RMQClientSettings extends Settings {
   public int getWaitChannelReturnTimeoutMillis() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.WAIT_CHANNEL_RETURN_TIMEOUT_MILLIS);
   }

   public String getHost() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.HOST_ADDR);
   }

   public int getPort() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.HOST_PORT);
   }

   public int getMaxPendingAsyncCloseConnTask() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.MAX_PENDING_ASYNC_CLOSE_CONN_TASK);
   }

   public String getVirtualHost() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.VIRTUAL_HOST);
   }

   public String getUsername() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.USERNAME);
   }

   public String getPassword() {
      return SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.PASSWORD);
   }

   public int getConnectTimeoutMillis() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.CONNECT_TIMEOUT_MILLIS);
   }

   public int getRequestedHeartBeatSecs() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.REQUESTED_HEARTBEAT_SECS);
   }
}
