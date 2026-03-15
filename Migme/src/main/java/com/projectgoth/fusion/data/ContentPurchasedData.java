package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class ContentPurchasedData implements Serializable {
   public Integer id;
   public String username;
   public Date dateCreated;
   public String mobilephone;
   public Integer contentId;
   public String providerContentId;
   public String providerTransactionId;
   public String downloadURL;
   public Integer numDownloads;
   public Boolean refunded;
   public ContentPurchasedData.RefundReasonEnum refundReason;

   public static enum RefundReasonEnum {
      PROVIDER_ERROR(1),
      HANDSET_INCOMPATIBLE(2),
      CONTENT_NOT_DOWNLOADED(3),
      MANUAL(99);

      private int value;

      private RefundReasonEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static ContentPurchasedData.RefundReasonEnum fromValue(int value) {
         ContentPurchasedData.RefundReasonEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ContentPurchasedData.RefundReasonEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
