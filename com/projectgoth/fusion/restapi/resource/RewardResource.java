package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.RedisQueue;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.log4j.Log4JUtils;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.RewardProgramData;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.rewardsystem.CommonOutcomes;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.outcomes.UserRewardOutcome;
import com.projectgoth.leto.common.impl.outcome.MMv2Outcomes;
import com.projectgoth.leto.common.outcome.Outcomes;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/reward")
public class RewardResource {
   private static final Logger log = Log4JUtils.getLogger(RewardResource.class);

   @POST
   @Path("/outcomes/{programId}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public void dispatch(@PathParam("programId") int programId, @QueryParam("useAsync") boolean useAsync, String outcomeJsonString) throws FusionRestException {
      String errorCode;
      try {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RewardDispatcherSettings.ENABLE_REST_INTERFACE)) {
            throw new FusionRestException(FusionRestException.RestException.DISABLED_FEATURE);
         } else {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.RewardDispatcherSettings.ENABLE_LOGGING_RECEIVED_OUTCOMES_FROM_REST_INTERFACE)) {
               log.info("dispatch programId:[" + programId + "] useAsync:[" + useAsync + "] data:[" + outcomeJsonString + "]");
            }

            RewardProgramData rewardProgram = RewardCentre.getInstance().getRewardProgram(programId);
            if (rewardProgram == null) {
               throw new FusionRestException(FusionRestException.RestException.UNKNOWN_REWARD_PROGRAM_ID);
            } else {
               Outcomes outcomes = CommonOutcomes.deserialize(outcomeJsonString);
               if (useAsync) {
                  RedisQueue redisQueue = RedisQueue.getInstance();

                  try {
                     RewardCentre.getInstance().dispatchRewardData(redisQueue, programId, outcomeJsonString);
                  } finally {
                     try {
                        redisQueue.disconnect();
                     } catch (Exception var16) {
                        log.warn("Failed to disconnect redisQueue.Exception:" + var16, var16);
                     }

                  }
               } else {
                  switch(outcomes.getOutcomeType()) {
                  case 1:
                     CommonOutcomes.processOutcomes(programId, (UserRewardOutcome)outcomes, new AccountEntrySourceData(RewardResource.class));
                     break;
                  case 2:
                     CommonOutcomes.processOutcomes((MMv2Outcomes)outcomes, new AccountEntrySourceData(RewardResource.class));
                     break;
                  default:
                     throw new CommonOutcomes.InvalidJSONException("Unsupported outcome type:[" + outcomes.getOutcomeType() + "].");
                  }
               }

            }
         }
      } catch (FusionRestException var18) {
         throw var18;
      } catch (CommonOutcomes.InvalidJSONException var19) {
         errorCode = "E-" + UUID.randomUUID().toString();
         log.warn("Invalid payload (" + errorCode + "). Exception:[" + var19 + "].Data:[" + outcomeJsonString + "]", var19);
         throw new FusionRestException(FusionRestException.RestException.INVALID_PAYLOAD_DATA.getCode(), "Invalid payload (" + errorCode + ")");
      } catch (Exception var20) {
         errorCode = "E-" + UUID.randomUUID().toString();
         String errorMessage = "Internal error (" + errorCode + "). Exception:[" + var20 + "].Data:[" + outcomeJsonString + "]";
         log.error(errorMessage, var20);
         throw new FusionRestException(FusionRestException.RestException.ERROR.getCode(), "Internal error (" + errorCode + ")");
      }
   }
}
