package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.rewardsystem.outcomes.notification.EmailTemplateIDOutcomeData;
import com.projectgoth.fusion.rewardsystem.outcomes.notification.IMNotificationTemplateOutcomeData;
import org.json.JSONException;
import org.json.JSONObject;

public class RewardProgramOutcomeDataDeserializer {
   public static RewardProgramOutcomeData deserialize(JSONObject jsonObject) throws JSONException {
      int typeCode = jsonObject.getInt("_t");
      RewardProgramOutcomeData.TypeEnum typeEnum = RewardProgramOutcomeData.TypeEnum.fromCode(typeCode);
      if (typeEnum == null) {
         throw new UnsupportedOperationException("Unable to deserialize outcome data type [" + typeCode + "]");
      } else {
         switch(typeEnum) {
         case BASIC:
            return (new BasicRewardProgramOutcomeData()).fromJSONObject(jsonObject);
         case UNLOCKED_STORE_ITEMS:
            return (new UnlockedStoreItemsRewardProgramOutcomeData()).fromJSONObject(jsonObject);
         case EMAIL_TEMPLATE_ID:
            return EmailTemplateIDOutcomeData.create(jsonObject);
         case IMNOTIFICATION_TEMPLATE:
            return IMNotificationTemplateOutcomeData.create(jsonObject);
         default:
            throw new UnsupportedOperationException("Unable to deserialize outcome data type enum [" + typeEnum + "]");
         }
      }
   }
}
