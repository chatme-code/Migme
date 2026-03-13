package com.projectgoth.fusion.data;

import com.projectgoth.fusion.bl1.BlueLabelResponseCodes;
import java.io.Serializable;

public class BlueLabelVoucherData implements Serializable {
   public String responseData;
   public Double amount;
   public String currency;
   public String transactionId;
   public BlueLabelResponseCodes responseCode;

   public BlueLabelVoucherData(int responseCode, String responseData) {
      this.responseData = responseData;
      this.responseCode = BlueLabelResponseCodes.fromValue(responseCode);
      if (this.responseCode == BlueLabelResponseCodes.SUCCESS && this.responseData.length() > 0) {
         this.transactionId = responseData;
      }

   }
}
