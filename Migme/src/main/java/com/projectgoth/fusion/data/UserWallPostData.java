package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class UserWallPostData implements Serializable {
   public Integer id;
   public Integer userID;
   public Integer authorUserID;
   public Date dateCreated;
   public String comment;
   public Integer numComments;
   public Integer numLikes;
   public Integer numDislikes;
   public UserWallPostData.TypeEnum type;
   public UserWallPostData.StatusEnum status;

   public static enum StatusEnum {
      REMOVED(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserWallPostData.StatusEnum fromValue(int value) {
         UserWallPostData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserWallPostData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      NORMAL(1),
      STATUS_UPDATE(2);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static UserWallPostData.TypeEnum fromValue(int value) {
         UserWallPostData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            UserWallPostData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
