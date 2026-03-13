package com.projectgoth.fusion.rewardsystem.stateprocessors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.VGReceivedTrigger;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.log4j.Logger;

public class VGReceivedStateHandler extends RewardProgramStateHandler {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VGReceivedStateHandler.class));
   private static final Type hashMapType = (new TypeToken<Map<Integer, Integer>>() {
   }).getType();

   private Map<Integer, Integer> makeVgIdToCountMap(List<String> param) throws NumberFormatException {
      Map<Integer, Integer> vgIds = new HashMap();
      Iterator i$ = param.iterator();

      while(i$.hasNext()) {
         String vgId = (String)i$.next();
         Integer id = new Integer(vgId);
         vgIds.put(id, 1 + (vgIds.containsKey(id) ? (Integer)vgIds.get(id) : 0));
      }

      return vgIds;
   }

   private static int toIntOrDefault(Integer i, int defaultVal) {
      return i == null ? defaultVal : i;
   }

   public RewardProgramStateHandler.PerformReturn perform(RewardProgramData program, RewardProgramTrigger trigger, String curState) {
      if (!(trigger instanceof VGReceivedTrigger)) {
         return RewardProgramStateHandler.PerformReturn.NOTHING;
      } else {
         VGReceivedTrigger rt = (VGReceivedTrigger)trigger;
         if (rt.quantityDelta <= 0) {
            return RewardProgramStateHandler.PerformReturn.NOTHING;
         } else {
            List<String> giftParamList = program.getStringListParam("virtualGiftIDList");
            if (giftParamList != null && !giftParamList.isEmpty()) {
               Map expectedVgIdToCountMap;
               try {
                  expectedVgIdToCountMap = this.makeVgIdToCountMap(giftParamList);
               } catch (NumberFormatException var12) {
                  log.error("Program[" + program.id + "] has a non numeric VGID defined.[" + giftParamList + "]", var12);
                  return RewardProgramStateHandler.PerformReturn.NOTHING;
               }

               Integer receivedVirtualGiftID = rt.virtualGiftID;
               if (!expectedVgIdToCountMap.containsKey(receivedVirtualGiftID)) {
                  return RewardProgramStateHandler.PerformReturn.NOTHING;
               } else {
                  Gson gson = (new GsonBuilder()).create();
                  Object matchedGiftsCounters;
                  int newCountForMatchedVgId;
                  if (StringUtil.isBlank(curState)) {
                     matchedGiftsCounters = new HashMap(1);
                     newCountForMatchedVgId = rt.quantityDelta;
                  } else {
                     matchedGiftsCounters = (Map)gson.fromJson(curState, hashMapType);
                     newCountForMatchedVgId = toIntOrDefault((Integer)((Map)matchedGiftsCounters).get(receivedVirtualGiftID), 0) + rt.quantityDelta;
                  }

                  ((Map)matchedGiftsCounters).put(receivedVirtualGiftID, newCountForMatchedVgId);
                  VGReceivedStateHandler.MatchedVGIDsToCountMap consumedMatchedGiftsCounters = tryConsumeMatchedGiftsCounters(expectedVgIdToCountMap, (Map)matchedGiftsCounters);
                  return RewardProgramStateHandler.PerformReturn.saveState(consumedMatchedGiftsCounters.isConsumed(), gson.toJson(consumedMatchedGiftsCounters));
               }
            } else {
               return RewardProgramStateHandler.PerformReturn.NOTHING;
            }
         }
      }
   }

   private static VGReceivedStateHandler.MatchedVGIDsToCountMap tryConsumeMatchedGiftsCounters(Map<Integer, Integer> expectedVgIdToCountMap, Map<Integer, Integer> matchedGiftsCounters) {
      VGReceivedStateHandler.MatchedVGIDsToCountMap nextConsumedMatchedGiftsCounters = new VGReceivedStateHandler.MatchedVGIDsToCountMap(expectedVgIdToCountMap.size(), true);
      VGReceivedStateHandler.MatchedVGIDsToCountMap nextNonConsumedMatchedGiftsCounters = new VGReceivedStateHandler.MatchedVGIDsToCountMap(expectedVgIdToCountMap.size(), false);
      boolean foundNonConsumable = false;
      Iterator i$ = expectedVgIdToCountMap.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<Integer, Integer> expectedVgIdToCountEntry = (Entry)i$.next();
         Integer expectedVgId = (Integer)expectedVgIdToCountEntry.getKey();
         Integer matchedCount = (Integer)matchedGiftsCounters.get(expectedVgId);
         if (matchedCount != null) {
            nextNonConsumedMatchedGiftsCounters.put(expectedVgId, matchedCount);
            if (!foundNonConsumable) {
               Integer expectedCount = (Integer)expectedVgIdToCountEntry.getValue();
               Integer remainingMatches = matchedCount - expectedCount;
               if (remainingMatches >= 0) {
                  if (remainingMatches > 0) {
                     nextConsumedMatchedGiftsCounters.put(expectedVgId, remainingMatches);
                  }
               } else {
                  foundNonConsumable = true;
               }
            }
         } else {
            foundNonConsumable = true;
         }
      }

      if (foundNonConsumable) {
         return nextNonConsumedMatchedGiftsCounters;
      } else {
         return nextConsumedMatchedGiftsCounters;
      }
   }

   public String getStateKeySuffix() {
      return "vgrcvd";
   }

   private static class MatchedVGIDsToCountMap extends HashMap<Integer, Integer> {
      private final boolean consumed;

      public MatchedVGIDsToCountMap(boolean consumed) {
         this.consumed = consumed;
      }

      public MatchedVGIDsToCountMap(int capacity, boolean consumed) {
         super(capacity);
         this.consumed = consumed;
      }

      public boolean isConsumed() {
         return this.consumed;
      }
   }
}
