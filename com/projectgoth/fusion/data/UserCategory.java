package com.projectgoth.fusion.data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserCategory {
   public int id;
   public String name;
   public UserCategory.UserCategoryTypeEnum type;

   public UserCategory(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.name = rs.getString("name");
      this.type = UserCategory.UserCategoryTypeEnum.fromValue(rs.getInt("type"));
   }

   public static enum UserCategoryTypeEnum {
      PUBLIC(1),
      AD_GROUPS(2);

      private int value;

      private UserCategoryTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserCategory.UserCategoryTypeEnum fromValue(int value) {
         UserCategory.UserCategoryTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserCategory.UserCategoryTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
