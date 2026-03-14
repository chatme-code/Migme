package com.projectgoth.fusion.clientsession;

import com.projectgoth.fusion.common.StringUtil;

public class SSOCaptchaState {
   public String captchaId;
   public String username;
   public String loginChallenge;
   public int passwordHash;
   private static final String DELIMITER = "##";

   public SSOCaptchaState() {
   }

   public SSOCaptchaState(String captchaId, String username, String loginChallenge, int passwordHash) {
      this.captchaId = captchaId;
      this.username = username;
      this.loginChallenge = loginChallenge;
      this.passwordHash = passwordHash;
   }

   public static SSOCaptchaState fromString(String encodedString) {
      String[] values = encodedString.split("##");
      if (values.length != 4) {
         return null;
      } else {
         int passwordHash = Integer.parseInt(values[3]);
         return new SSOCaptchaState(values[0], values[1], values[2], passwordHash);
      }
   }

   public String toString() {
      return StringUtil.join((Object[])(new String[]{this.captchaId, this.username, this.loginChallenge, "" + this.passwordHash}), "##");
   }
}
