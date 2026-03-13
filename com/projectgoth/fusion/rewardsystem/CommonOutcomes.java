package com.projectgoth.fusion.rewardsystem;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.rewardsystem.outcomes.UserRewardOutcome;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.leto.common.impl.outcome.MMv2Outcomes;
import com.projectgoth.leto.common.outcome.OutcomeMarshaller;
import com.projectgoth.leto.common.outcome.Outcomes;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.json.JSONException;
import org.json.JSONObject;

public class CommonOutcomes {
   public static Outcomes deserialize(String jsonData) throws CommonOutcomes.InvalidJSONException {
      try {
         JSONObject rewardOutcomeJSON = new JSONObject(jsonData);
         int outcomeType = rewardOutcomeJSON.optInt("type", 1);
         switch(outcomeType) {
         case 1:
            return (new UserRewardOutcome()).fromJSONObject(rewardOutcomeJSON);
         case 2:
            return OutcomeMarshaller.getInstance().unmarshalToOutcomes(jsonData);
         default:
            throw new CommonOutcomes.InvalidJSONException("Unsupported outcome type:[" + outcomeType + "].");
         }
      } catch (JsonParseException var3) {
         throw new CommonOutcomes.InvalidJSONException(var3);
      } catch (JsonMappingException var4) {
         throw new CommonOutcomes.InvalidJSONException(var4);
      } catch (JSONException var5) {
         throw new CommonOutcomes.InvalidJSONException(var5);
      } catch (IOException var6) {
         throw new CommonOutcomes.InvalidJSONException(var6);
      }
   }

   public static void processOutcomes(MMv2Outcomes outcomes, AccountEntrySourceData accountEntrySource) throws CreateException, RemoteException {
      Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
      contentEJB.giveRewards(outcomes, new AccountEntrySourceData(RewardDispatcher.class));
   }

   public static void processOutcomes(int programId, UserRewardOutcome outcomes, AccountEntrySourceData accountEntrySource) throws CreateException, RemoteException {
      Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
      contentEJB.giveRewards(programId, outcomes.getUserid(), accountEntrySource, outcomes.getOutcomeDataList(), outcomes.getTemplateData());
   }

   public static class InvalidJSONException extends FusionException {
      public InvalidJSONException(Exception ex) {
         super(ex.toString());
         this.initCause(ex);
      }

      public InvalidJSONException(String msg) {
         super(msg);
      }
   }
}
