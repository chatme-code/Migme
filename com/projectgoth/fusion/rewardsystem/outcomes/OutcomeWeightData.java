package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.data.JSONSerializable;
import java.util.Iterator;
import org.json.JSONException;
import org.json.JSONObject;

public class OutcomeWeightData implements JSONSerializable<OutcomeWeightData> {
   private String outcome;
   private int weight;

   OutcomeWeightData() {
   }

   public OutcomeWeightData(String outcome, int weight) {
      this.outcome = outcome;
      this.weight = weight;
   }

   public String getOutcome() {
      return this.outcome;
   }

   public int getWeight() {
      return this.weight;
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject jsonObject = new JSONObject();
      jsonObject.put(this.outcome, this.weight);
      return jsonObject;
   }

   public OutcomeWeightData fromJSONObject(JSONObject jsonObject) throws JSONException {
      Iterator<?> iter = jsonObject.keys();
      if (iter.hasNext()) {
         String outcome = iter.next().toString();
         int weight = jsonObject.optInt(outcome, 0);
         this.outcome = outcome;
         this.weight = weight;
         return this;
      } else {
         return this;
      }
   }
}
