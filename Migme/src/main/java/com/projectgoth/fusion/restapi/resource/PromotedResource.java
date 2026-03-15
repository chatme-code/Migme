package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.PromotedPostData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.util.List;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
@Path("/promoted")
public class PromotedResource {
   private static final Logger log = LoggerFactory.getLogger(ConfigUtils.getLoggerName(PromotedResource.class));

   @POST
   @Path("/post")
   @Consumes({"application/json"})
   public void updatePromotedPost(List<PromotedPostData> promotedPostDatas) throws FusionRestException {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.updatePromotedPost(promotedPostDatas);
      } catch (Exception var3) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unabled to update promoted post");
      }
   }

   @GET
   @Path("/post")
   @Produces({"application/json"})
   public List<PromotedPostData> getPromotedPost(@QueryParam("limit") int limit, @QueryParam("archived") boolean archived, @QueryParam("slot") int slot) throws FusionRestException {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         return misBean.getPromotedPost(limit, archived, slot);
      } catch (Exception var5) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Unabled to get promoted post");
      }
   }
}
