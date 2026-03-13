package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.ChatroomCategoryRefreshType;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.apache.log4j.Logger;

public class ChatroomCategoryData implements Serializable {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatroomCategoryData.class));
   public Integer id;
   public String name;
   public Integer maxLevel;
   public ChatroomCategoryData.StatusEnum status;
   public boolean itemsCanBeDeleted;
   public boolean initiallyCollapsed;
   public ChatroomCategoryRefreshType refreshMethod;
   public Integer orderIndex;
   public String refreshDisplayString;

   public ChatroomCategoryData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.name = rs.getString("name");
      this.itemsCanBeDeleted = rs.getBoolean("itemscanbedeleted");
      this.initiallyCollapsed = rs.getBoolean("initiallycollapsed");
      this.maxLevel = rs.getInt("maxmiglevel");
      this.orderIndex = rs.getInt("orderindex");
      this.refreshDisplayString = rs.getString("refreshdisplaystring");
      Integer intval = (Integer)rs.getObject("status");
      if (intval != null) {
         this.status = ChatroomCategoryData.StatusEnum.fromValue(intval);
      }

      intval = (Integer)rs.getObject("refreshmethod");
      if (intval != null) {
         this.refreshMethod = ChatroomCategoryRefreshType.fromValue(intval.byteValue());
      }

   }

   public byte intiallyCollapsedByteValue() {
      return (byte)(this.initiallyCollapsed ? 1 : 0);
   }

   public byte itemsCanBeDeletedByteValue() {
      return (byte)(this.itemsCanBeDeleted ? 1 : 0);
   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ChatroomCategoryData.StatusEnum fromValue(int value) {
         ChatroomCategoryData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatroomCategoryData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum SpecialCategoriesEnum {
      BOOKMARKED(1),
      RECENT(2),
      POPULAR(3),
      RECOMMENDED(8);

      private int value;

      private SpecialCategoriesEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ChatroomCategoryData.SpecialCategoriesEnum fromValue(int value) {
         ChatroomCategoryData.SpecialCategoriesEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatroomCategoryData.SpecialCategoriesEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
