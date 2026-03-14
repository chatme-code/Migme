package com.projectgoth.fusion.rewardsystem.outcomes;

import com.projectgoth.fusion.data.JSONSerializable;
import java.util.Arrays;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class OutcomeWeightsDataList implements JSONSerializable<OutcomeWeightsDataList> {
   private OutcomeWeightData[] outcomeWeightList = new OutcomeWeightData[0];
   private int sumOfWeights = 0;

   public OutcomeWeightsDataList(OutcomeWeightData... data) {
      this.init(data);
   }

   private void init(OutcomeWeightData... data) {
      if (data != null && data.length > 0) {
         this.outcomeWeightList = data;
         OutcomeWeightData[] arr$ = this.outcomeWeightList;
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            OutcomeWeightData outcomeWeight = arr$[i$];
            this.sumOfWeights += outcomeWeight.getWeight();
         }
      } else {
         this.outcomeWeightList = new OutcomeWeightData[0];
         this.sumOfWeights = 0;
      }

   }

   public int count() {
      return this.outcomeWeightList.length;
   }

   public int sumOfWeights() {
      return this.sumOfWeights;
   }

   public OutcomeWeightData get(int i) {
      return this.outcomeWeightList[i];
   }

   public JSONObject toJSONObject() throws JSONException {
      JSONObject object = new JSONObject();
      JSONObject[] outcomeWeightDataJSON = new JSONObject[this.outcomeWeightList.length];

      for(int i = 0; i < this.outcomeWeightList.length; ++i) {
         outcomeWeightDataJSON[i] = this.outcomeWeightList[i].toJSONObject();
      }

      object.put("list", new JSONArray(Arrays.asList(outcomeWeightDataJSON)));
      return object;
   }

   public OutcomeWeightsDataList fromJSONObject(JSONObject jsonObject) throws JSONException {
      JSONArray jsonArray = jsonObject.getJSONArray("list");
      OutcomeWeightData[] outcomeWeightArr = new OutcomeWeightData[jsonArray.length()];

      for(int i = 0; i < jsonArray.length(); ++i) {
         outcomeWeightArr[i] = (new OutcomeWeightData()).fromJSONObject(jsonArray.getJSONObject(i));
      }

      this.init(outcomeWeightArr);
      return this;
   }

   public static OutcomeWeightsDataList create(JSONObject jsonObject) throws JSONException {
      return (new OutcomeWeightsDataList(new OutcomeWeightData[0])).fromJSONObject(jsonObject);
   }
}
