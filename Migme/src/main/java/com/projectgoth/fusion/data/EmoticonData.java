package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmoticonData implements Serializable {
   public Integer id;
   public Integer emoticonPackID;
   public EmoticonData.TypeEnum type;
   public String alias;
   public Integer width;
   public Integer height;
   public String location;
   public String locationPNG;
   public String hotKey;
   public List<String> alternateHotKeys;

   public EmoticonData() {
   }

   public EmoticonData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.emoticonPackID = (Integer)rs.getObject("emoticonPackID");
      this.alias = rs.getString("alias");
      this.width = (Integer)rs.getObject("width");
      this.height = (Integer)rs.getObject("height");
      this.location = rs.getString("location");
      this.locationPNG = rs.getString("locationPNG");
      this.alternateHotKeys = new ArrayList();
      Integer intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = EmoticonData.TypeEnum.fromValue(intVal);
      }

   }

   public String toString() {
      return this.id.toString();
   }

   public static enum HotKeyTypeEnum {
      PRIMARY(1),
      ALTERNATE(2);

      private int value;

      private HotKeyTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static EmoticonData.HotKeyTypeEnum fromValue(int value) {
         EmoticonData.HotKeyTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            EmoticonData.HotKeyTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      IMAGE(1),
      VIBRATION(2),
      ANIMATION(3),
      AUDIO(4),
      STICKER(5);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static EmoticonData.TypeEnum fromValue(int value) {
         EmoticonData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            EmoticonData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
