package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.app.dao.DAOException;
import com.projectgoth.fusion.app.dao.DAOFactory;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedDistributedLock;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MemCachedKeyUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.CampaignData;
import com.projectgoth.fusion.data.CampaignParticipantData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/campaign")
public class CampaignResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CampaignResource.class));

   @GET
   @Path("/{campaignid}")
   @Produces({"application/json"})
   public DataHolder<CampaignData> getCampaignData(@PathParam("campaignid") int campaignid) throws FusionRestException {
      try {
         CampaignData campaignData = DAOFactory.getInstance().getCampaignDAO().getCampaignData(campaignid);
         return new DataHolder(campaignData);
      } catch (DAOException var3) {
         log.error("Exception occurred on getCampaignData", var3);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var3.getMessage());
      }
   }

   @GET
   @Path("/{campaignid}/participant")
   @Produces({"application/json"})
   public DataHolder<CampaignParticipantData> getCampaignParticipant(@QueryParam("requestingUserid") int userid, @PathParam("campaignid") int campaignid) throws FusionRestException {
      try {
         CampaignParticipantData campaignParticipantData = DAOFactory.getInstance().getCampaignDAO().getCampaignParticipantData(userid, campaignid);
         return new DataHolder(campaignParticipantData);
      } catch (DAOException var4) {
         log.error("Exception occurred on getCampaignParticipant", var4);
         throw new FusionRestException(FusionRestException.RestException.ERROR, var4.getMessage());
      }
   }

   private void checkRateLimit(String keyToRateLimit, String rateLimitStr) throws FusionRestException {
      String rateLimit = rateLimitStr;

      try {
         MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.CAMPAIGN.toString(), MemCachedKeyUtils.getFullKeyForKeySpace(MemCachedKeySpaces.RateLimitKeySpace.CAMPAIGN_RATE_LIMIT, String.format("%s:%s", this.getClass().getSimpleName(), keyToRateLimit)), rateLimit);
      } catch (MemCachedRateLimiter.LimitExceeded var5) {
         throw new FusionRestException(FusionRestException.RestException.RATE_LIMIT, "Sorry you only do " + var5.getPrettyMessage());
      } catch (MemCachedRateLimiter.FormatError var6) {
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR, "Internal Error");
      }
   }

   @POST
   @Path("/join")
   @Produces({"application/json"})
   public Response joinCampaign(DataHolder<CampaignParticipantData> dataholder) throws FusionRestException {
      if (dataholder == null) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid Campaign");
      } else {
         this.checkRateLimit("s:" + ((CampaignParticipantData)dataholder.data).getUserId(), SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CampaignSetting.DEFAULT_RATE_LIMIT));

         try {
            CampaignData campaignData = DAOFactory.getInstance().getCampaignDAO().getCampaignData(((CampaignParticipantData)dataholder.data).getCampaignId());
            if (campaignData != null && campaignData.isActive()) {
               CampaignParticipantData campaignParticipantData = DAOFactory.getInstance().getCampaignDAO().getCampaignParticipantData(((CampaignParticipantData)dataholder.data).getUserId(), ((CampaignParticipantData)dataholder.data).getCampaignId());
               if (campaignParticipantData != null) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, String.format("userid %s already join campaign id:", ((CampaignParticipantData)dataholder.data).getUserId(), ((CampaignParticipantData)dataholder.data).getCampaignId()));
               } else {
                  String lockKey = "campaign/" + ((CampaignParticipantData)dataholder.data).getCampaignId() + "/" + ((CampaignParticipantData)dataholder.data).getMobilePhone();

                  try {
                     MemCachedDistributedLock.getDistributedLock(lockKey);
                     CampaignParticipantData campaignParticipantData2 = DAOFactory.getInstance().getCampaignDAO().getCampaignParticipantDataByMobilePhone(((CampaignParticipantData)dataholder.data).getMobilePhone(), ((CampaignParticipantData)dataholder.data).getCampaignId());
                     if (campaignParticipantData2 != null) {
                        throw new FusionRestException(FusionRestException.RestException.DUPLICATE_MOBILE, "Duplicate Mobile");
                     }

                     DAOFactory.getInstance().getCampaignDAO().joinCampaign((CampaignParticipantData)dataholder.data);
                  } finally {
                     MemCachedDistributedLock.releaseDistributedLock(lockKey);
                  }

                  return Response.ok().entity(new DataHolder("ok")).build();
               }
            } else {
               throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid Campaign");
            }
         } catch (DAOException var11) {
            log.error("Exception occurred on joinCampaign", var11);
            throw new FusionRestException(FusionRestException.RestException.ERROR, var11.getMessage());
         }
      }
   }
}
