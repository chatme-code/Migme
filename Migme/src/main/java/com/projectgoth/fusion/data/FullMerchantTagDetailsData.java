package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FullMerchantTagDetailsData extends BasicMerchantTagDetailsData implements Serializable {
   public UserProfileLabelsData labels;
   public String country;
   public String gender;
   public String aboutMe;
   public int migLevel;
   public String migBotImage;

   public FullMerchantTagDetailsData(ResultSet rs, ReputationLevelData reputationLevelData, UserProfileLabelsData userProfileLabels) throws SQLException {
      super(rs);
      this.country = rs.getString("country");
      if (userProfileLabels != null) {
         this.labels = userProfileLabels;
      }

      Integer intval = rs.getInt("profileStatus");
      if (intval == UserProfileData.StatusEnum.PUBLIC.value() || intval == UserProfileData.StatusEnum.CONTACTS_ONLY.value() && rs.getBoolean("isContact")) {
         this.gender = rs.getString("gender");
         this.aboutMe = rs.getString("aboutMe");
      }

      if (reputationLevelData != null) {
         this.migLevel = reputationLevelData.level;
         this.migBotImage = reputationLevelData.image;
      }

   }
}
