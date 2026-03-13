package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.ExceptionHelper;
import com.projectgoth.fusion.common.PasswordUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.metrics.MetricsEnums;
import com.projectgoth.fusion.common.metrics.MetricsLogger;
import com.projectgoth.fusion.data.ValidateCredentialResult;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

@Provider
@Path("/test")
public class TestResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(TestResource.class));

   @GET
   @Path("/ping")
   @Produces({"application/json"})
   public DataHolder<String> getPingTest() {
      MetricsLogger.log(MetricsEnums.FusionRestMetrics.TEST_PING, "", 1);
      return new DataHolder("OK");
   }

   @GET
   @Path("/password")
   @Produces({"application/json"})
   public DataHolder<ValidateCredentialResult> validatePassword(@QueryParam("username") String username, @QueryParam("password") String password) {
      return new DataHolder(PasswordUtils.validatePassword(username, password));
   }

   @POST
   @Path("/email/{username}")
   public DataHolder<String> sendTestMig33Email(@PathParam("username") String username, String jsonData) throws FusionRestException {
      try {
         JSONObject json = new JSONObject(jsonData);
         String senderPassword = json.getString("password");
         String to = json.getString("to");
         String subject = json.getString("subject");
         String content = json.getString("content");
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         messageBean.sendEmail(username, senderPassword, to, subject, content);
         return new DataHolder("OK");
      } catch (CreateException var9) {
         return new DataHolder(ExceptionHelper.getRootMessage(var9));
      } catch (EJBException var10) {
         return new DataHolder(ExceptionHelper.setErrorMessage(var10.getMessage()));
      } catch (JSONException var11) {
         return new DataHolder(ExceptionHelper.setErrorMessage(var11.getMessage()));
      }
   }

   @POST
   @Path("/alerts/{userid}/{type}")
   @Produces({"application/json"})
   public DataHolder<String> createTestAlert(@PathParam("userid") String useridStr, @PathParam("type") String typeStr, String jsondata) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Invalid userid '%s' specified", useridStr));
         throw new FusionRestException(-1, String.format("Invalid userid '%s' specified", useridStr));
      } else {
         int type = StringUtil.toIntOrDefault(typeStr, -1);
         Enums.NotificationTypeEnum notfnType = Enums.NotificationTypeEnum.fromType(type);
         if (notfnType == null) {
            log.error(String.format("Invalid type '%s' specified", typeStr));
            throw new FusionRestException(-1, String.format("Invalid type '%s' specified", typeStr));
         } else {
            try {
               User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               String username = userBean.getUsernameByUserid(userid, (Connection)null);
               if (StringUtil.isBlank(username)) {
                  throw new FusionRestException(101, String.format("User with id '%d' not found", userid));
               } else {
                  UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  String key = "test-" + System.currentTimeMillis();
                  Map<String, String> parameters = new HashMap();
                  JSONObject jobj = new JSONObject(jsondata);
                  Iterator iter = jobj.keys();

                  while(iter.hasNext()) {
                     String paramkey = (String)iter.next();
                     String value = jobj.getString(paramkey);
                     parameters.put(paramkey, value);
                  }

                  Message m = new Message(key, userid, username, notfnType.getType(), System.currentTimeMillis(), parameters);
                  unsProxy.notifyFusionUser(m);
                  return new DataHolder("OK");
               }
            } catch (Exception var16) {
               log.error(String.format("Exception caught %s", var16.getMessage()), var16);
               return new DataHolder("ERROR");
            }
         }
      }
   }
}
