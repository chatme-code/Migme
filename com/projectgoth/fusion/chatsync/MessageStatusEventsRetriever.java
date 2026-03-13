package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageStatusEventIce;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

public class MessageStatusEventsRetriever extends MessageStatusEvents {
   private static final LogFilter log;
   private final String parentUsername;
   private final ConnectionPrx parentConnection;
   private final short requestTxnId;

   public MessageStatusEventsRetriever(ChatDefinition chatKey, Long startTime, Long endTime, Integer maxResults, String parentUsername, ConnectionPrx parentCxn, short requestTxnId) throws FusionException {
      super(chatKey, startTime, endTime, maxResults, parentUsername);
      this.parentUsername = parentUsername;
      this.parentConnection = parentCxn;
      this.requestTxnId = requestTxnId;
   }

   public MessageStatusEventsRetriever(ChatDefinition chatKey, String[] messageGuids, long[] messageTimestamps, Integer maxResults, String parentUsername, ConnectionPrx parentCxn, short requestTxnId) throws FusionException {
      super(chatKey, messageTimestamps, messageGuids, maxResults, parentUsername);
      this.parentUsername = parentUsername;
      this.parentConnection = parentCxn;
      this.requestTxnId = requestTxnId;
   }

   public void retrieve(ChatSyncStore[] stores) throws FusionException {
      if (log.isDebugEnabled()) {
         log.debug("Retrieving message status events for user=" + this.parentUsername);
      }

      super.retrieve(stores);
      List<MessageStatusEventIce> eventsIce = new ArrayList();
      MessageStatusEventPersistable[] arr$ = this.events;
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         MessageStatusEvent mse = arr$[i$];
         eventsIce.add(mse.toIceObject());
      }

      MessageStatusEventIce[] arr = (MessageStatusEventIce[])eventsIce.toArray(new MessageStatusEventIce[eventsIce.size()]);
      if (log.isDebugEnabled()) {
         log.debug("Pushing " + arr.length + " retrieved message status events to user=" + this.parentUsername);
      }

      this.parentConnection.putMessageStatusEvents(arr, this.requestTxnId);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(MessageStatusEventsRetriever.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
