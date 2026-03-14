package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class ScrapbookData implements Serializable {
   public Integer id;
   public String username;
   public String fileID;
   public Date dateCreated;
   public String receivedFrom;
   public String description;
   public ScrapbookData.StatusEnum status;
   public FileData file;

   public static enum StatusEnum {
      INACTIVE(0),
      PRIVATE(1),
      PUBLIC(2),
      CONTACTS_ONLY(3),
      REPORTED(4);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ScrapbookData.StatusEnum fromValue(int value) {
         ScrapbookData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ScrapbookData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
