package com.projectgoth.fusion.chatsync;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ConfigurableExecutor;
import com.projectgoth.fusion.common.LogFilter;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import org.apache.log4j.Logger;

public abstract class ChatSyncExecutor extends ConfigurableExecutor {
   private static final LogFilter log;

   protected ChatSyncExecutor() {
      super(SystemPropertyEntities.ChatSyncSettings.TPOOL_CORE_SIZE, SystemPropertyEntities.ChatSyncSettings.TPOOL_MAX_SIZE, SystemPropertyEntities.ChatSyncSettings.TPOOL_KEEP_ALIVE_SECONDS, SystemPropertyEntities.ChatSyncSettings.REFRESH_THREAD_POOL_PROPERTIES_ENABLED, SystemPropertyEntities.ChatSyncSettings.TPOOL_PROPS_REFRESH_INTERVAL_SECS);
   }

   static {
      log = new LogFilter(Logger.getLogger(ConfigUtils.getLoggerName(ChatSyncExecutor.class)), SystemPropertyEntities.ChatSyncSettings.LOG_EXCLUSION_FILTER);
   }
}
