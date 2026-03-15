package com.projectgoth.fusion.datagrid;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.objectcache.ChatUserState;
import com.projectgoth.fusion.slice.FusionException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.Lock;

public abstract class DataGrid {
   public abstract void prepare();

   public abstract ExecutorService getExecutorService(String var1);

   public ExecutorService getDefaultExecutorService() {
      return this.getExecutorService(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.DataGridSettings.DEFAULT_EXECUTOR_SERVICE));
   }

   public abstract Lock getLock(Object var1);

   public abstract Map<String, ChatUserState> getUsersMap() throws FusionException;

   public abstract <K, V> void configMap(String var1, int var2);

   public abstract Map<String, Integer> getStringIntMap(String var1);

   public abstract void destroyLock(Lock var1);

   public abstract void destroyMap(String var1);

   public abstract String getStats();
}
