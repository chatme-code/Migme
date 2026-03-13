package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.data.ThirdPartyApplicationData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.eventqueue.EventQueue;
import com.projectgoth.fusion.eventqueue.events.GameEvent;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.sql.Connection;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/application")
public class ApplicationResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ApplicationResource.class));

   @POST
   @Path("/{applicationid}/gameevent/{userid}")
   @Produces({"application/json"})
   public Response sendApplicationEvent(@PathParam("applicationid") String applicationId, @PathParam("userid") String userIdStr, String gameEventJson) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, -1);
      if (userId == -1) {
         throw new FusionRestException(101, "Invalid User ID");
      } else {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            String username = userBean.getUsernameByUserid(userId, (Connection)null);
            if (SystemProperty.getBool("GameEventsToMigboEnabled", false)) {
               Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
               webBean.sendApplicationEvent(username, applicationId, gameEventJson);
            } else {
               EventQueue.enqueueSingleEvent(new GameEvent(username, applicationId, gameEventJson));
            }

            return Response.ok().entity(new DataHolder("ok")).build();
         } catch (Exception var8) {
            log.error("Unable to send application event: " + var8);
            throw new FusionRestException(101, "Internal system error: Unable to send application event");
         }
      }
   }

   @GET
   @Path("/thirdpartyapp_details/{thirdPartyAppId}")
   @Produces({"application/json"})
   public DataHolder<ThirdPartyApplicationData> getThirdPartyDetails(@PathParam("thirdPartyAppId") String thirdPartyAppIdStr) throws FusionRestException {
      int thirdPartyAppId = StringUtil.toIntOrDefault(thirdPartyAppIdStr, -1);
      if (thirdPartyAppId == -1) {
         throw new FusionRestException(101, "Invalid Third Party App ID");
      } else {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            ThirdPartyApplicationData thirdPartyApplicationData = userBean.getThirdPartyApplicationData(thirdPartyAppId, (Connection)null);
            return new DataHolder(thirdPartyApplicationData);
         } catch (Exception var5) {
            log.error("Unable to send application event: " + var5);
            throw new FusionRestException(101, "Internal system error: Unable to send application event");
         }
      }
   }
}
