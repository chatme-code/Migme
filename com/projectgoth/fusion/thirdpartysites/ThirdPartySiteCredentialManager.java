package com.projectgoth.fusion.thirdpartysites;

import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.restapi.data.FacebookCredentialData;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.TwitterCredentialData;
import com.projectgoth.fusion.slice.AuthenticationServiceCredentialResponse;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.Credential;
import com.projectgoth.fusion.slice.FusionServerException;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ThirdPartySiteCredentialManager {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ThirdPartySiteCredentialManager.class));

   public static boolean updateCredential(int userId, byte siteId, String credentialsJson) throws FusionRestException {
      if (PasswordType.fromValue(siteId) == null) {
         log.info("Invalid third party site ID: " + siteId);
         return false;
      } else {
         Credential credential = null;
         if (siteId == PasswordType.TWITTER.value()) {
            try {
               TwitterCredentialData credentialData = new TwitterCredentialData();
               credentialData.fromJSONString(credentialsJson);
               String accessToken = credentialData.toJSONStringAccessToken();
               credential = new Credential(userId, credentialData.id, accessToken, siteId);
            } catch (JSONException var8) {
               log.error("Unable to convert TwitterCredentialData from JSON string :" + var8);
               return false;
            }
         } else {
            if (siteId != PasswordType.FACEBOOK_IM.value()) {
               log.info("Invalid third party site ID: " + siteId);
               return false;
            }

            try {
               FacebookCredentialData credentialData = new FacebookCredentialData();
               credentialData.fromJSONString(credentialsJson);
               credential = new Credential(userId, credentialData.id, credentialData.key, siteId);
            } catch (JSONException var7) {
               log.error("Unable to convert FacebookCredentialData from JSON string :" + var7);
               return false;
            }
         }

         try {
            AuthenticationServicePrx authenticationService = EJBIcePrxFinder.getAuthenticationServiceProxy();
            AuthenticationServiceCredentialResponse getResp = authenticationService.getCredential(credential.userID, credential.passwordType);
            AuthenticationServiceResponseCodeEnum createResp;
            if (getResp != null && getResp.code == AuthenticationServiceResponseCodeEnum.Success) {
               createResp = authenticationService.removeCredential(getResp.userCredential);
               if (createResp == null || createResp != AuthenticationServiceResponseCodeEnum.Success) {
                  log.error("Unable to remove credential for credential update. User ID [" + credential.userID + "], Response code: " + createResp);
                  return false;
               }
            }

            createResp = authenticationService.createCredential(credential);
            if (createResp != null && createResp == AuthenticationServiceResponseCodeEnum.Success) {
               return true;
            }

            log.error("Unable to update credentials using the authentication service. Response code: " + createResp);
            return false;
         } catch (FusionServerException var9) {
            log.error("Unable to update third party site credentials: " + var9);
            if (var9.errorCode == AuthenticationServiceResponseCodeEnum.CredentialAlreadyExists.value()) {
               throw new FusionRestException(107, "Account is already linked to another user");
            }
         } catch (Exception var10) {
            log.error("Unable to update third party site credentials: " + var10);
         }

         return false;
      }
   }

   public static boolean deleteCredential(int userId, byte siteId) {
      if (PasswordType.fromValue(siteId) == null) {
         log.info("Invalid third party site ID: " + siteId);
         return false;
      } else {
         try {
            AuthenticationServicePrx authenticationService = EJBIcePrxFinder.getAuthenticationServiceProxy();
            AuthenticationServiceCredentialResponse credential = authenticationService.getCredential(userId, siteId);
            if (credential != null && credential.userCredential != null) {
               AuthenticationServiceResponseCodeEnum resp = authenticationService.removeCredential(credential.userCredential);
               if (resp != null && resp == AuthenticationServiceResponseCodeEnum.Success) {
                  return true;
               } else {
                  log.error("Unable to delete third party site credentials using the authentication service. Response code: " + resp);
                  return false;
               }
            } else {
               return true;
            }
         } catch (Exception var5) {
            log.error("Unable to delete third party site credential for User ID [" + userId + "], Site ID: [" + siteId + "]. Exception: " + var5);
            return false;
         }
      }
   }

   public static String getCredentialsJsonStr(int userId) {
      JSONArray credenitalsListJson = new JSONArray();
      TwitterCredentialData twitterCredential = getTwitterCredential(userId);
      if (twitterCredential != null) {
         try {
            log.debug("User [" + userId + "] has a twitter credential: " + twitterCredential.toJSONString());
            JSONObject credentialJson = new JSONObject();
            credentialJson.put("type", PasswordType.TWITTER.value());
            credentialJson.put("credential", twitterCredential.toJSONObject());
            credenitalsListJson.put(credentialJson);
         } catch (JSONException var6) {
            log.error("Unable to parse twitter credential for User ID: " + userId);
         }
      } else {
         log.debug("User [" + userId + "] does not have a twitter credenital");
      }

      FacebookCredentialData facebookCredential = getFacebookCredential(userId);
      if (facebookCredential != null) {
         try {
            log.debug("User [" + userId + "] has a facebook credential: " + facebookCredential.toJSONString());
            JSONObject credentialJson = new JSONObject();
            credentialJson.put("type", PasswordType.FACEBOOK_IM.value());
            credentialJson.put("credential", facebookCredential.toJSONObject());
            credenitalsListJson.put(credentialJson);
         } catch (JSONException var5) {
            log.error("Unable to parse facebook credential for User ID: " + userId);
         }
      } else {
         log.debug("User [" + userId + "] does not have a facebook credenital");
      }

      return packageCredentialData(credenitalsListJson);
   }

   private static String packageCredentialData(JSONArray credenitalsList) {
      try {
         JSONObject credentials = new JSONObject();
         credentials.put("credentials", credenitalsList);
         JSONObject data = new JSONObject();
         return data.put("data", credentials).toString();
      } catch (JSONException var3) {
         log.error("Unable to package credential data into a JSON string: " + credenitalsList);
         return null;
      }
   }

   private static TwitterCredentialData getTwitterCredential(int userId) {
      try {
         AuthenticationServicePrx authenticationService = EJBIcePrxFinder.getAuthenticationServiceProxy();
         AuthenticationServiceCredentialResponse resp = authenticationService.getCredential(userId, PasswordType.TWITTER.value());
         if (resp != null && resp.userCredential != null) {
            TwitterCredentialData credential = new TwitterCredentialData();
            credential.fromJSONString(resp.userCredential.username, resp.userCredential.password);
            return credential;
         }
      } catch (Exception var4) {
         log.error("Unable to get twitter credentials: " + var4);
      }

      return null;
   }

   private static FacebookCredentialData getFacebookCredential(int userId) {
      try {
         AuthenticationServicePrx authenticationService = EJBIcePrxFinder.getAuthenticationServiceProxy();
         AuthenticationServiceCredentialResponse resp = authenticationService.getCredential(userId, PasswordType.FACEBOOK_IM.value());
         if (resp != null && resp.userCredential != null) {
            return new FacebookCredentialData(resp.userCredential.username, resp.userCredential.password);
         }
      } catch (Exception var3) {
         log.error("Unable to get facebook credentials: " + var3);
      }

      return null;
   }
}
