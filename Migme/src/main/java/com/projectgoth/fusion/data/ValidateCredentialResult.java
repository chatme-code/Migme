package com.projectgoth.fusion.data;

public class ValidateCredentialResult {
   public boolean valid;
   public String reason;

   public ValidateCredentialResult(boolean valid, String reason) {
      this.valid = valid;
      this.reason = reason;
   }

   public ValidateCredentialResult() {
   }
}
