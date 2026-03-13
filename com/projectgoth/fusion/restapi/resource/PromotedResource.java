package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.PromotedPostData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/promoted")
public class PromotedResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PromotedResource.class));

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
