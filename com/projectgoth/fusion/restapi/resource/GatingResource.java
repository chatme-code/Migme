package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.UserAccessData;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.restapi.enums.MigboAccessMemberTypeEnum;
import javax.ejb.CreateException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/gating")
public class GatingResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GatingResource.class));

   @GET
   @Path("/{userid}/{functionid}")
   @Produces({"application/json"})
   public DataHolder<UserAccessData> isUserAllowedMigboAccess(@PathParam("userid") String userIdStr, @PathParam("functionid") String functionIdStr) throws FusionRestException {
      int userId = StringUtil.toIntOrDefault(userIdStr, -1);
      if (userId == -1) {
         log.error(String.format("Failed to check guard due to invalid userid '%s'", userIdStr));
         throw new FusionRestException(101, "Internal error while checking access credentials");
      } else {
         int functionId = StringUtil.toIntOrDefault(functionIdStr, -1);
         if (functionId == -1) {
            log.error(String.format("Failed to check guard due to invalid functionid '%s'", functionIdStr));
            throw new FusionRestException(101, "Internal error while checking access credentials");
         } else {
            GuardCapabilityEnum guardCapability = GuardCapabilityEnum.fromValue(functionId);
            if (guardCapability == null) {
               log.error(String.format("Failed to check guard due to invalid functionid '%s'", functionIdStr));
               throw new FusionRestException(101, "Internal error while checking access credentials");
            } else {
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)(new SystemPropertyEntities.GuardsetEnabled(guardCapability)))) {
                  UserLocal userBean = null;

                  try {
                     userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  } catch (CreateException var10) {
                     log.error("Unable to create user bean: " + var10.getMessage());
                     throw new FusionRestException(101, "Internal error while checking access credentials");
                  }

                  UserAccessData data;
                  try {
                     if (userBean.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.WHITELIST.value(), functionId)) {
                        log.debug("User [" + userId + "] was granted access because user was in whitelist");
                        data = new UserAccessData(true);
                        return new DataHolder(data);
                     }
                  } catch (FusionEJBException var9) {
                     log.error("Unable to check guard: " + var9.getMessage());
                     throw new FusionRestException(101, "Internal error while checking access credentials");
                  }

                  try {
                     if (userBean.isUserInMigboAccessList(userId, MigboAccessMemberTypeEnum.BLACKLIST.value(), functionId)) {
                        log.debug("User [" + userId + "] was denied access because user was in blacklist");
                        data = new UserAccessData(false);
                        return new DataHolder(data);
                     }
                  } catch (FusionEJBException var12) {
                     log.error("Unable to check guard: " + var12.getMessage());
                     throw new FusionRestException(101, "Internal error while checking access credentials");
                  }

                  try {
                     Boolean allowedAccess = userBean.isUserLevelAllowedMigboAccess(userId, functionId);
                     UserAccessData data = new UserAccessData(allowedAccess);
                     return new DataHolder(data);
                  } catch (FusionEJBException var11) {
                     log.error("Unable to check guard: " + var11.getMessage());
                  }
               }

               UserAccessData data = new UserAccessData(true);
               return new DataHolder(data);
            }
         }
      }
   }

   @GET
   @Path("/authenticatedaccesscontrol/{userid}/{authentiatedaccesscontroltype}")
   @Produces({"application/json"})
   public DataHolder<UserAccessData> isUserAllowedAuthenticatedAccess(@PathParam("userid") String userIdOrUsernameStr, @PathParam("authentiatedaccesscontroltype") String authenticatedAccessControlTypeStr, @QueryParam("useUsername") String useUsernameStr) throws FusionRestException {
      boolean useUsername = StringUtil.toBooleanOrDefault(useUsernameStr, false);
      int userId = -1;
      String username = null;
      if (useUsername) {
         username = userIdOrUsernameStr;
      } else {
         userId = StringUtil.toIntOrDefault(userIdOrUsernameStr, -1);
         if (userId == -1) {
            log.error(String.format("Failed to check authenticated access control due to invalid userid '%s'", userIdOrUsernameStr));
            throw new FusionRestException(101, "Internal error while checking access");
         }
      }

      AuthenticatedAccessControlTypeEnum type = null;

      try {
         type = AuthenticatedAccessControlTypeEnum.valueOf(authenticatedAccessControlTypeStr);
      } catch (IllegalArgumentException var9) {
         log.error(String.format("Failed to check authenticated access control of %s due to invalid type '%s'", userIdOrUsernameStr, authenticatedAccessControlTypeStr));
         throw new FusionRestException(101, "Internal error while checking access");
      }

      boolean allowed = useUsername ? AuthenticatedAccessControl.hasAccessByUsernameLocal(type, username) : AuthenticatedAccessControl.hasAccessByUseridLocal(type, userId);
      return new DataHolder(new UserAccessData(allowed));
   }
}
