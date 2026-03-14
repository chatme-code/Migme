package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ThemeData extends ReferenceStoreItemData implements Serializable {
   public Integer id;
   public String name;
   public String description;
   public String location;
   public ThemeData.StatusEnum status;

   public ThemeData() {
   }

   public ThemeData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.name = rs.getString("name");
      this.description = rs.getString("description");
      this.location = rs.getString("location");
      Integer intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = ThemeData.StatusEnum.fromValue(intVal);
      }

   }

   public static enum StatusEnum {
      AVAILABLE(1),
      NOT_AVAILABLE(0);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ThemeData.StatusEnum fromValue(int value) {
         ThemeData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ThemeData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
