package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ExternalDownloadLinkData implements Serializable {
   public int id;
   public String url;
   public int hitRate;
   public ExternalDownloadLinkData.StatusEnum status;
   public int startRange;
   public int endRange;
   public String version;

   public ExternalDownloadLinkData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.url = rs.getString("url");
      this.hitRate = rs.getInt("hitrate");
      this.status = ExternalDownloadLinkData.StatusEnum.fromValue(rs.getInt("status"));
      this.version = rs.getString("version");
   }

   public void setRange(int startRange) {
      this.startRange = startRange;
      this.endRange = startRange + this.hitRate;
   }

   public static enum StatusEnum {
      AVAILABLE(1),
      UNAVAILABLE(0);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ExternalDownloadLinkData.StatusEnum fromValue(int value) {
         ExternalDownloadLinkData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ExternalDownloadLinkData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
