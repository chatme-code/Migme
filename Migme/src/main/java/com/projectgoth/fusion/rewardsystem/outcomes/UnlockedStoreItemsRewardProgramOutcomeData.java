package com.projectgoth.fusion.rewardsystem.outcomes;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class UnlockedStoreItemsRewardProgramOutcomeData extends RewardProgramOutcomeData {
   private static final String CURRENT_DATA_FORMAT_VERSION = "1.0";
   private static final String FIELD_UNLOCKED_STORE_ITEMS = "ulstoreitems";
   private Map<Integer, Integer> storeItemIDToCountsMap;

   public UnlockedStoreItemsRewardProgramOutcomeData(Map<Integer, Integer> storeItemIDToCountsMap) {
      this();
      this.storeItemIDToCountsMap = storeItemIDToCountsMap;
   }

   public UnlockedStoreItemsRewardProgramOutcomeData() {
      super(RewardProgramOutcomeData.TypeEnum.UNLOCKED_STORE_ITEMS);
   }

   protected String currentDataFormatVersion() {
      return "1.0";
   }

   protected void serializeToJSONObject(JSONObject jsonObject) throws JSONException {
      if (this.storeItemIDToCountsMap != null && !this.storeItemIDToCountsMap.isEmpty()) {
         jsonObject.put("ulstoreitems", new JSONObject(this.storeItemIDToCountsMap));
      }

   }

   protected void deserializeFromJSONObject(JSONObject jsonObject) throws JSONException {
      JSONObject storeItemIDToCountsMapJSON = (JSONObject)jsonObject.opt("ulstoreitems");
      Map<Integer, Integer> storeItemIDToCountsMap = new HashMap(jsonObject.length());
      Iterator keyIterator = storeItemIDToCountsMapJSON.keys();

      while(keyIterator.hasNext()) {
         String keyStr = (String)keyIterator.next();
         int value = storeItemIDToCountsMapJSON.getInt(keyStr);
         if (value != -1) {
            storeItemIDToCountsMap.put(Integer.parseInt(keyStr), value);
         }
      }

      this.storeItemIDToCountsMap = storeItemIDToCountsMap;
   }

   public Map<Integer, Integer> getUnlockedStoreItems() {
      return this.storeItemIDToCountsMap;
   }

   public boolean requiresTemplateData() {
      return false;
   }
}
