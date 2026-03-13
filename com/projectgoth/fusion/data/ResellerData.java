package com.projectgoth.fusion.data;

import java.io.Serializable;

public class ResellerData implements Serializable {
   public Integer id;
   public Integer countryID;
   public String state;
   public String city;
   public String name;
   public String address;
   public String phoneNumber;
   public String phoneNumberToDisplay;
   public String phoneNumber2;
   public String phoneNumber2ToDisplay;
   public ResellerData.StatusEnum status;

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

      public static ResellerData.StatusEnum fromValue(int value) {
         ResellerData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ResellerData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
