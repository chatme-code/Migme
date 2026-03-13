package com.projectgoth.fusion.rewardsystem.mmv2;

import com.mig33.rabbitmqclient.RabbitMQ;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.leto.common.event.EventEnvelope;
import com.projectgoth.leto.common.event.EventMarshaller;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP.BasicProperties;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.Logger;

public class MMv2TriggerSender {
   public static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MMv2TriggerSender.class));
   private static final String CURRENT_EVENT_ENVELOP_VERSION = "1.0";
   private ThreadPoolExecutor executor;
   private final LazyLoader<Set<RewardProgramData.TypeEnum>> whitelistedTriggerTypesLoader;
   private static volatile MMv2TriggerSender overrideInstance;

   protected MMv2TriggerSender() {
      this.executor = new ThreadPoolExecutor(1, 5, 300000L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
      this.whitelistedTriggerTypesLoader = new LazyLoader<Set<RewardProgramData.TypeEnum>>("MMv2TriggerSender.WhitelistedTypes", 60000L) {
         protected Set<RewardProgramData.TypeEnum> fetchValue() throws Exception {
            int[] triggerTypeWhitelist = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2.TRIGGER_TYPE_WHITELIST);
            if (triggerTypeWhitelist != null && triggerTypeWhitelist.length > 0) {
               Set<RewardProgramData.TypeEnum> list = new HashSet();
               int[] arr$ = triggerTypeWhitelist;
               int len$ = triggerTypeWhitelist.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  int triggerType = arr$[i$];
                  RewardProgramData.TypeEnum type = RewardProgramData.TypeEnum.fromValue(triggerType);
                  if (type != null) {
                     list.add(type);
                  }
               }

               return list;
            } else {
               return Collections.EMPTY_SET;
            }
         }
      };
   }

   public static MMv2TriggerSender getInstance() {
      return overrideInstance == null ? MMv2TriggerSender.MMv2TriggerSenderSingletonHolder.INSTANCE : overrideInstance;
   }

   protected static void overrideInstance(MMv2TriggerSender instance) {
      overrideInstance = instance;
   }

   public static void resetInstanceToDefault() {
      overrideInstance((MMv2TriggerSender)null);
   }

   public final void send(RewardProgramTrigger trigger) throws Exception {
      if (this.mustSendToRMQ(trigger)) {
         this.sendTriggerToRMQ(trigger);
      } else if (log.isDebugEnabled()) {
         log.debug("Not sending trigger [" + trigger + "] to RMQ");
      }

   }

   private boolean mustSendToRMQ(RewardProgramTrigger trigger) {
      if (trigger == null) {
         return false;
      } else {
         boolean enableOutgoingTrigger = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2.ENABLE_OUTBOUND_TRIGGERS);
         if (!enableOutgoingTrigger) {
            return false;
         } else {
            boolean sendWhiteListedTypesOnly = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2.SEND_WHITELIST_TRIGGER_TYPES_ONLY);
            if (sendWhiteListedTypesOnly) {
               if (trigger.programType == null) {
                  return false;
               } else {
                  Set<RewardProgramData.TypeEnum> whitelist = (Set)this.whitelistedTriggerTypesLoader.getValue();
                  return whitelist != null && whitelist.contains(trigger.programType);
               }
            } else {
               return true;
            }
         }
      }
   }

   private void sendTriggerToRMQ(RewardProgramTrigger trigger) throws Exception {
      MMv2TriggerSender.RoutingSpec routingSpec = getRoutingSpec(trigger);
      if (log.isDebugEnabled()) {
         log.debug("sending trigger [" + trigger + "] with routing spec [" + routingSpec + "]");
      }

      if (routingSpec.isUsesSynchronousDispatch()) {
         this.asyncDispatch(routingSpec, trigger);
      } else {
         this.dispatch(routingSpec, trigger);
      }

   }

   private void asyncDispatch(MMv2TriggerSender.RoutingSpec routingSpec, RewardProgramTrigger trigger) {
      if (log.isDebugEnabled()) {
         log.debug("asyncDispatch [" + trigger + "] with routing spec [" + routingSpec + "]");
      }

      int queueSize = this.executor.getQueue().size();
      int maxQueueSize = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.MAX_DISPATCH_TASK_SIZE);
      if (queueSize <= maxQueueSize) {
         this.executor.submit(new MMv2TriggerSender.DispatchEventEnvelop(routingSpec, trigger));
      } else {
         log.warn("DispatchEventEnvelop queue too busy (" + queueSize + "/" + maxQueueSize + "). Dropping Trigger: " + trigger.toString());
      }

   }

   private void dispatch(MMv2TriggerSender.RoutingSpec routingSpec, RewardProgramTrigger trigger) throws Exception {
      if (log.isDebugEnabled()) {
         log.debug("dispatching [" + trigger + "] with routing spec [" + routingSpec + "]");
      }

      EventEnvelope eventEnvelop = toEventEnvelope(trigger);
      byte[] eventEnvelopBytes = EventMarshaller.getInstance().marshalToBytes(eventEnvelop);
      this.send(routingSpec, eventEnvelopBytes);
      if (log.isDebugEnabled()) {
         log.debug("sent [" + trigger + "] with routing spec [" + routingSpec + "]");
      }

   }

   protected void send(MMv2TriggerSender.RoutingSpec routingSpec, byte[] eventEnvelopBytes) throws Exception {
      String exchange = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ.EXCHANGE_NAME);
      BasicProperties messageProperties;
      if (routingSpec.isUsesPersistentDelivery()) {
         messageProperties = MessageProperties.PERSISTENT_TEXT_PLAIN;
      } else {
         messageProperties = MessageProperties.TEXT_PLAIN;
      }

      RabbitMQ rmq = MMv2RabbitMQ.getInstance();
      if (log.isDebugEnabled()) {
         log.debug("Sending " + new String(eventEnvelopBytes) + "to exchange [" + exchange + "] with spec [" + routingSpec + "]");
      }

      rmq.send(exchange, routingSpec.getRoutingKey(), messageProperties, eventEnvelopBytes);
   }

   private static EventEnvelope toEventEnvelope(RewardProgramTrigger trigger) {
      EventEnvelope eventEnvelop = new EventEnvelope();
      eventEnvelop.setId(UUID.randomUUID().toString());
      eventEnvelop.setMetadataVersion("1.0");
      eventEnvelop.setPayload(trigger);
      if (trigger != null && trigger.userData != null && trigger.userData.userID != null) {
         eventEnvelop.setSenderid(trigger.userData.userID.toString());
      }

      eventEnvelop.setSenttime(new Timestamp(System.currentTimeMillis()));
      eventEnvelop.setType(trigger.getEventType());
      return eventEnvelop;
   }

   public static MMv2TriggerSender.RoutingSpec getRoutingSpec(RewardProgramTrigger trigger) {
      int eventTypeID = trigger.programType.getId();
      String routingKey = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ_TriggerRouting.ROUTING_KEY.forEventTypeID(eventTypeID));
      boolean usesPersistentDelivery = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ_TriggerRouting.PERSISTENT_MODE.forEventTypeID(eventTypeID));
      boolean useSynchDispatch = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.MarketingMechanicsV2_RMQ_TriggerRouting.SYNC_DISPATCH.forEventTypeID(eventTypeID));
      return new MMv2TriggerSender.RoutingSpec(usesPersistentDelivery, routingKey, useSynchDispatch);
   }

   private class DispatchEventEnvelop implements Callable<Object> {
      private final MMv2TriggerSender.RoutingSpec routingSpec;
      private final RewardProgramTrigger trigger;

      public DispatchEventEnvelop(MMv2TriggerSender.RoutingSpec routingSpec, RewardProgramTrigger trigger) {
         this.routingSpec = routingSpec;
         this.trigger = trigger;
      }

      public Object call() throws Exception {
         MMv2TriggerSender.this.dispatch(this.routingSpec, this.trigger);
         return null;
      }
   }

   public static final class RoutingSpec {
      private final boolean usesPersistentDelivery;
      private final String routingKey;
      private final boolean usesSynchronousDispatch;

      public RoutingSpec(boolean persistentDelivery, String routingKey, boolean useSynchronousDispatch) {
         this.usesPersistentDelivery = persistentDelivery;
         this.routingKey = routingKey;
         this.usesSynchronousDispatch = useSynchronousDispatch;
      }

      public boolean isUsesPersistentDelivery() {
         return this.usesPersistentDelivery;
      }

      public String getRoutingKey() {
         return this.routingKey;
      }

      public boolean isUsesSynchronousDispatch() {
         return this.usesSynchronousDispatch;
      }

      public String toString() {
         return "RoutingSpec [usesPersistentDelivery=" + this.usesPersistentDelivery + ", routingKey=" + this.routingKey + ", usesSynchronousDispatch=" + this.usesSynchronousDispatch + "]";
      }
   }

   private static class MMv2TriggerSenderSingletonHolder {
      private static final MMv2TriggerSender INSTANCE = new MMv2TriggerSender();
   }
}
