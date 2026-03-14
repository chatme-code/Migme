package com.projectgoth.fusion.externalfeed.source;

import com.projectgoth.fusion.common.SystemProperty;

public class ExchangeRateFeedDataSourceFactory {
   public static ExchangeRateFeedDataSource getDataSource() throws Exception {
      String dataSource = SystemProperty.get("ExchangeRateDataSource", "xe");
      ExchangeRateFeedDataSource source = getDataSource(ExchangeRateFeedDataSourceFactory.DataSourceType.fromName(dataSource));
      if (source == null) {
         throw new Exception("Invalid datasource specified in system property [" + dataSource + "]");
      } else {
         return source;
      }
   }

   public static ExchangeRateFeedDataSource getDataSource(ExchangeRateFeedDataSourceFactory.DataSourceType type) throws Exception {
      if (ExchangeRateFeedDataSourceFactory.DataSourceType.XE.equals(type)) {
         return new XEDataSource();
      } else {
         return ExchangeRateFeedDataSourceFactory.DataSourceType.OANDA.equals(type) ? new OandaDataSource() : null;
      }
   }

   public static enum DataSourceType {
      XE("xe"),
      OANDA("oanda");

      private String name;

      private DataSourceType(String s) {
         this.name = s;
      }

      public String getName() {
         return this.name;
      }

      public static ExchangeRateFeedDataSourceFactory.DataSourceType fromName(String s) {
         ExchangeRateFeedDataSourceFactory.DataSourceType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ExchangeRateFeedDataSourceFactory.DataSourceType t = arr$[i$];
            if (t.name.equals(s)) {
               return t;
            }
         }

         return null;
      }
   }
}
