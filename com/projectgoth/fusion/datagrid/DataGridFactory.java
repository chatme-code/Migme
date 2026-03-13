package com.projectgoth.fusion.datagrid;

import com.projectgoth.fusion.common.ConfigUtils;
import org.apache.log4j.Logger;

public class DataGridFactory {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(DataGridFactory.class));

   public static DataGridFactory getInstance() {
      return DataGridFactory.SingletonHolder.INSTANCE;
   }

   public DataGrid getGrid() {
      return HazelcastDataGrid.getInstance();
   }

   private static class SingletonHolder {
      public static final DataGridFactory INSTANCE = new DataGridFactory();
   }
}
