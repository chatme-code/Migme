package com.projectgoth.fusion.common;

import com.google.gson.Gson;
import com.projectgoth.fusion.data.ThirdPartyApplicationData;

public class ThirdPartyAppHelper {
   public static final String THIRD_PARTY_APPLICATION_ID = "ThirdPartyApplicationID";
   public static Gson gson = new Gson();

   public static String getThirdPartyAppKey(int thirdPartyAppID) {
      return String.format("%s:%s", "ThirdPartyApplicationID", thirdPartyAppID);
   }

   public static ThirdPartyApplicationData fromJson(String jsonStr) {
      return (ThirdPartyApplicationData)gson.fromJson(jsonStr, ThirdPartyApplicationData.class);
   }

   public static String toJsonString(ThirdPartyApplicationData thirdPartyApplicationData) {
      return gson.toJson(thirdPartyApplicationData);
   }
}
