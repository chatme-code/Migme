package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.MigboEnums;
import com.projectgoth.fusion.common.MiniblogDailyDigestHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.metrics.MetricsEnums;
import com.projectgoth.fusion.common.metrics.MetricsLogger;
import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.interfaces.Contact;
import com.projectgoth.fusion.interfaces.ContactHome;
import com.projectgoth.fusion.interfaces.ContactLocal;
import com.projectgoth.fusion.interfaces.ContactLocalHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.EventFollowingRequestData;
import com.projectgoth.fusion.restapi.data.EventNewPostData;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.GroupActivityRewardProgramTriggerData;
import com.projectgoth.fusion.restapi.data.ThirdPartyAppActivityRewardProgramTriggerData;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.GroupActivityTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MigboCampaignTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MigboFollowedByEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MigboFollowingEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MigboPostCreatedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MigboPostEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.MutuallyFollowingEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RelationshipEventTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.RewardProgramTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ThirdPartyAppInternalInvitationTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ThirdPartyAppStartEventTrigger;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.ejb.CreateException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Provider
@Path("/event")
public class EventResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(EventResource.class));
   private UserNotificationServicePrx unsProxy = null;

   public void setUserNotificationServicePrx(UserNotificationServicePrx proxy) {
      this.unsProxy = proxy;
   }

   public UserNotificationServicePrx getUserNotificationServicePrx() {
      if (this.unsProxy == null) {
         this.unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
      } else {
         try {
            this.unsProxy.ice_ping();
         } catch (Exception var2) {
            this.unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
         }
      }

      return this.unsProxy;
   }

   @POST
   @Path("/newpost/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response createNewPostEvent(@PathParam("userid") String useridStr, @QueryParam("fireRewardProgramTrigger") boolean fireRewardProgramTrigger, @QueryParam("shareToThirdParty") String shareToThirdParty, EventNewPostData data) throws FusionRestException {
      log.info(String.format("received new migbo post event, userid=%s, fullPostId=%s, parentFullPostid=%s, originality=%s application=%s type=%s hashtags=%s links=%s mentions=%s fireRewardProgramTrigger=%b shareToThirdParty=%s", useridStr, data.fullPostid, data.parentFullPostid, data.originality, data.application, data.type, data.hashtags, data.links, data.mentions, fireRewardProgramTrigger, shareToThirdParty));
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Invalid userid '%s' specified", useridStr));
         throw new FusionRestException(-1, String.format("Invalid userid '%s' specified", useridStr));
      } else {
         MigboEnums.MigboPostOriginalityEnum originality = MigboEnums.MigboPostOriginalityEnum.fromType(StringUtil.toIntOrDefault(data.originality, -1));
         if (originality == null) {
            log.error(String.format("Invalid orinality '%s' specified", data.originality));
            throw new FusionRestException(-1, String.format("Invalid originality '%s' specified", data.originality));
         } else {
            MigboEnums.PostApplicationEnum application = MigboEnums.PostApplicationEnum.fromValue(StringUtil.toIntOrDefault(data.application, 0));
            if (application == null) {
               application = MigboEnums.PostApplicationEnum.WEB;
            }

            MigboEnums.MigboPostTypeEnum type = MigboEnums.MigboPostTypeEnum.fromValue(StringUtil.toIntOrDefault(data.type, 0));
            if (type == null) {
               type = MigboEnums.MigboPostTypeEnum.TEXT;
            }

            List<String> hashtags = data.hashtags == null ? new LinkedList() : data.hashtags;
            List<Map<String, String>> links = data.links == null ? new LinkedList() : data.links;
            User userBean = null;

            try {
               userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            } catch (CreateException var21) {
               log.error(String.format("Unable to initialize UserBean for the new post created event, userid %s, full post id %s", useridStr, data.fullPostid), var21);
               throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
            }

            String key;
            if (fireRewardProgramTrigger) {
               try {
                  if (application != MigboEnums.PostApplicationEnum.SYSTEM) {
                     UserData authorUserData = userBean.loadUserFromID(userid);
                     MigboPostCreatedTrigger trigger = new MigboPostCreatedTrigger(authorUserData);
                     trigger.amountDelta = 0.0D;
                     trigger.quantityDelta = 1;
                     trigger.postOriginality = originality;
                     trigger.postType = type;
                     trigger.application = application;
                     trigger.hashtags = (List)hashtags;
                     trigger.links = (List)links;
                     trigger.parentPostID = data.parentFullPostid;
                     trigger.shareToThirdParty = EnumSet.noneOf(Enums.ThirdPartyEnum.class);
                     if (!StringUtil.isBlank(shareToThirdParty)) {
                        String[] shareToThirdPartyArr = shareToThirdParty.split(",");
                        String[] arr$ = shareToThirdPartyArr;
                        int len$ = shareToThirdPartyArr.length;

                        for(int i$ = 0; i$ < len$; ++i$) {
                           key = arr$[i$];
                           trigger.shareToThirdParty.add(Enums.ThirdPartyEnum.valueOf(key));
                        }
                     }

                     RewardCentre.getInstance().sendTrigger(trigger);
                     if (originality == MigboEnums.MigboPostOriginalityEnum.REPLY || originality == MigboEnums.MigboPostOriginalityEnum.RESHARE) {
                        int originalAuthorUserid = StringUtil.toIntOrDefault(data.parentFullPostid.split("-")[0], -1);
                        UserData originalAuthorUserData = userBean.loadUserFromID(originalAuthorUserid);
                        MigboPostEventTrigger.PostEventTypeEnum eventType = originality == MigboEnums.MigboPostOriginalityEnum.REPLY ? MigboPostEventTrigger.PostEventTypeEnum.REPLIED_TO : MigboPostEventTrigger.PostEventTypeEnum.RESHARED;
                        if (originalAuthorUserid == -1) {
                           log.warn("Unable to determine original post author userid [" + data.parentFullPostid + "]");
                        } else {
                           MigboPostEventTrigger postEventTrigger = new MigboPostEventTrigger(originalAuthorUserData, data.parentFullPostid, eventType);
                           postEventTrigger.amountDelta = 0.0D;
                           postEventTrigger.quantityDelta = 1;
                           postEventTrigger.shareToThirdParty = trigger.shareToThirdParty;
                           RewardCentre.getInstance().sendTrigger(postEventTrigger);
                        }
                     }
                  }
               } catch (Exception var22) {
                  log.error(String.format("Unable to notify RewardCentre about the new post created event, userid %s, full post id %s", useridStr, data.fullPostid), var22);
                  throw new FusionRestException(101, String.format("Internal error: unable to notify the new post created event"));
               }
            }

            if (originality == MigboEnums.MigboPostOriginalityEnum.REPLY) {
               try {
                  int originalAuthorUserid = StringUtil.toIntOrDefault(data.parentFullPostid.split("-")[0], -1);
                  if (originalAuthorUserid == -1) {
                     throw new Exception(String.format("Unable to determine original author's userid from parentFullPostid [%s]", data.parentFullPostid));
                  }

                  if (userid != originalAuthorUserid) {
                     String originalAuthorUsername = userBean.getUsernameByUserid(originalAuthorUserid, (Connection)null);
                     UserNotificationServicePrx unsProxy = this.getUserNotificationServicePrx();
                     if (unsProxy == null) {
                        log.error(String.format("Unable to find UserNotificationServicePrx to generate REPLY_TO_MIGBO_POST_ALERT for userid [%d] postid [%s]", userid, data.fullPostid));
                        throw new FusionRestException(101, String.format("Internal error: unable to notify the new post created event"));
                     }

                     long replyTimestamp = StringUtil.toLongOrDefault(data.timestamp, System.currentTimeMillis());
                     Map<String, String> parameters = new HashMap();
                     parameters.put("replierUserid", Integer.toString(userid));
                     parameters.put("parentFullPostid", data.parentFullPostid);
                     parameters.put("fullPostid", data.fullPostid);
                     key = data.fullPostid;
                     unsProxy.notifyFusionUser(new Message(key, originalAuthorUserid, originalAuthorUsername, Enums.NotificationTypeEnum.REPLY_TO_MIGBO_POST_ALERT.getType(), replyTimestamp, parameters));
                     log.debug(String.format("Successfully created notification for user [%s][%d] that post [%s] has been replied by user [%s] with post[%s]", originalAuthorUsername, originalAuthorUserid, data.parentFullPostid, useridStr, data.fullPostid));
                  }
               } catch (Exception var20) {
                  log.error(String.format("Unexpected exception while generating REPLY_TO_MIGBO_POST_ALERT for userid [%d] postid [%s] : [%s]", userid, data.fullPostid, var20.getMessage()), var20);
                  throw new FusionRestException(101, String.format("Internal error: unable to notify the new post created event"));
               }
            }

            this.notifyMentionedUsers(data, userBean, userid);

            try {
               MetricsLogger.log(MetricsEnums.FusionRestMetrics.MIGBO_NEW_POST_APPLICATION, data.fullPostid, data.application);
               MetricsLogger.log(MetricsEnums.FusionRestMetrics.MIGBO_NEW_POST_ORIGINALITY, data.fullPostid, data.originality);
               MetricsLogger.log(MetricsEnums.FusionRestMetrics.MIGBO_NEW_POST_TYPE, data.fullPostid, data.type);
            } catch (Exception var19) {
               log.error("Exception caught while logging to metrics logger: " + var19.getMessage());
            }

            return Response.ok().entity(new DataHolder("ok")).build();
         }
      }
   }

   private void notifyMentionedUsers(EventNewPostData data, User userBean, int userid) throws FusionRestException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE523_ALERT_USER_MENTIONED_IN_MIGBO_POST_ENABLED)) {
         if (null != data.mentions && !data.mentions.isEmpty()) {
            try {
               UserNotificationServicePrx unsProxy = this.getUserNotificationServicePrx();
               if (unsProxy == null) {
                  log.error(String.format("Unable to find UserNotificationServicePrx to generate MENTIONED_IN_MIGBO_POST_ALERT for userid [%d] postid [%s]", userid, data.fullPostid));
                  throw new FusionRestException(101, String.format("Internal error: unable to notify the new post created event"));
               }

               Iterator i$ = data.mentions.iterator();

               while(i$.hasNext()) {
                  Integer mentionedUserId = (Integer)i$.next();
                  if (null != mentionedUserId) {
                     String userName = userBean.getUsernameByUserid(mentionedUserId, (Connection)null);
                     Map<String, String> parameters = new HashMap();
                     parameters.put("fullPostid", data.fullPostid);
                     String key = data.fullPostid;
                     unsProxy.notifyFusionUser(new Message(key, mentionedUserId, userName, Enums.NotificationTypeEnum.MENTIONED_IN_MIGBO_POST_ALERT.getType(), StringUtil.toLongOrDefault(data.timestamp, System.currentTimeMillis()), parameters));
                     if (log.isDebugEnabled()) {
                        log.debug(String.format("Successfully created notification for user [%s][%d] who has been mentioned by user [%d] in post[%s]", mentionedUserId, userName, userid, data.fullPostid));
                     }
                  }
               }
            } catch (Exception var10) {
               log.error(String.format("Unexpected exception while generating MENTIONED_IN_MIGBO_POST_ALERT for userid [%d] postid [%s] : [%s]", userid, data.fullPostid, var10.getMessage()), var10);
               throw new FusionRestException(101, String.format("Internal error: unable to notify the new post created event"));
            }
         }

      }
   }

   @POST
   @Path("/followingrequest/{userid}/{followerUserid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response createFollowingRequestEvent(@PathParam("userid") String useridStr, @PathParam("followerUserid") String followerUseridStr, EventFollowingRequestData data) throws FusionRestException {
      log.info(String.format("recieved following request event, timestamp=%s", data.timestamp));
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         log.error(String.format("Invalid userid '%s' specified", useridStr));
         throw new FusionRestException(-1, String.format("Invalid userid '%s' specified", useridStr));
      } else {
         int followerUserid = StringUtil.toIntOrDefault(followerUseridStr, -1);
         if (followerUserid == -1) {
            log.error(String.format("Invalid follower userid '%s' specified", followerUseridStr));
            throw new FusionRestException(-1, String.format("Invalid follower userid '%s' specified", followerUseridStr));
         } else {
            try {
               UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
               if (unsProxy != null) {
                  UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  String username = userBean.getUsernameByUserid(userid, (Connection)null);
                  long requestedTimestamp = StringUtil.toLongOrDefault(data.timestamp, -1L);
                  if (requestedTimestamp == -1L) {
                     log.warn(String.format("Invalid following request timestamp '%s', using current system time.", data.timestamp));
                     requestedTimestamp = System.currentTimeMillis();
                  }

                  Map<String, String> parameters = new HashMap();
                  parameters.put("requestedTimestamp", data.timestamp);
                  String key = Integer.toString(followerUserid);
                  unsProxy.notifyFusionUser(new Message(key, userid, username, Enums.NotificationTypeEnum.FOLLOWING_REQUEST.getType(), requestedTimestamp, parameters));
                  return Response.ok().entity(new DataHolder("ok")).build();
               }

               log.error(String.format("Unable to find UserNotificationServicePrx to push following request alert notification for user [%d], followerUserId %d", userid, followerUserid));
            } catch (Exception var13) {
               log.error(String.format("Unable to notify UserNotificationService about the new following request event due to exception, userid %s, followerUserid %s", useridStr, followerUseridStr), var13);
            }

            throw new FusionRestException(101, String.format("Internal error: unable to notify the new following request event"));
         }
      }
   }

   @POST
   @Path("/followingevent/{userid}/{otherUserid}")
   @Produces({"application/json"})
   public Response handleFollowingEvent(@PathParam("userid") String useridStr, @PathParam("otherUserid") String otherUseridStr, @QueryParam("eventType") String eventTypeStr, @QueryParam("showAlert") String showAlertStr, @QueryParam("isAutoFollow") String isAutoFollow) throws FusionRestException {
      log.info(String.format("received following event, userid=%s otherUserid=%s eventType=%s", useridStr, otherUseridStr, eventTypeStr));
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.HANDLE_FOLLOWING_EVENTS_FROM_MIGBO_ENABLED)) {
         throw new FusionRestException(FusionRestException.RestException.SERVICE_DISABLED);
      } else {
         int userid = StringUtil.toIntOrDefault(useridStr, -1);
         if (userid == -1) {
            log.error(String.format("Invalid userid '%s' specified", useridStr));
            throw new FusionRestException(-1, String.format("Invalid userid '%s' specified", useridStr));
         } else {
            int otherUserid = StringUtil.toIntOrDefault(otherUseridStr, -1);
            if (otherUserid == -1) {
               log.error(String.format("Invalid other userid '%s' specified", otherUseridStr));
               throw new FusionRestException(-1, String.format("Invalid other userid '%s' specified", otherUseridStr));
            } else if (userid == otherUserid) {
               log.error(String.format("Invalid userid '%s' and other userid '%s' specified - must not be the same", useridStr, otherUseridStr));
               throw new FusionRestException(-1, String.format("Invalid userid '%s' and other userid '%s' specified - must not be the same", useridStr, otherUseridStr));
            } else {
               MigboEnums.MigboFollowingEventTypeEnum type = MigboEnums.MigboFollowingEventTypeEnum.fromType(StringUtil.toIntOrDefault(eventTypeStr, -1));
               if (type == null) {
                  log.error(String.format("Invalid following event type '%s' specified", eventTypeStr));
                  throw new FusionRestException(-1, String.format("Invalid following event type '%s' specified", eventTypeStr));
               } else {
                  try {
                     UserNotificationServicePrx unsProxy = this.getUserNotificationServicePrx();
                     if (unsProxy != null) {
                        UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                        ContactLocal contactBean = (ContactLocal)EJBHomeCache.getLocalObject("ContactLocal", ContactLocalHome.class);
                        String username = userBean.getUsernameByUserid(userid, (Connection)null);
                        String otherUsername = userBean.getUsernameByUserid(otherUserid, (Connection)null);
                        if (StringUtil.isBlank(username)) {
                           log.error(String.format("Invalid userid '%s' specified - user not found", useridStr));
                           throw new FusionRestException(-1, String.format("Invalid userid '%s' specified - user not found", useridStr));
                        }

                        if (StringUtil.isBlank(otherUsername)) {
                           log.error(String.format("Invalid other userid '%s' specified - user not found", otherUseridStr));
                           throw new FusionRestException(-1, String.format("Invalid other userid '%s' specified - user not found", otherUseridStr));
                        }

                        long eventTimestamp = System.currentTimeMillis();
                        ContactData userContactData;
                        if (type == MigboEnums.MigboFollowingEventTypeEnum.MUTUAL_FOLLOWING) {
                           userContactData = contactBean.getContact(username, otherUsername);
                           ContactData otherContactData = contactBean.getContact(otherUsername, username);
                           if (StringUtil.isBlank(userContactData.username)) {
                              userContactData.username = username;
                              userContactData.fusionUsername = otherUsername;
                              contactBean.addFusionUserAsContact(userid, userContactData, false);
                           }

                           if (StringUtil.isBlank(otherContactData.username)) {
                              otherContactData.username = otherUsername;
                              otherContactData.fusionUsername = username;
                              contactBean.addFusionUserAsContact(otherUserid, otherContactData, false);
                           }

                           Map<String, String> parametersForUserBeingFollowedAlert = new HashMap();
                           parametersForUserBeingFollowedAlert.put("otherUsername", otherUsername);
                           String key = Integer.toString(otherUserid);
                           unsProxy.notifyFusionUser(new Message(key, userid, username, Enums.NotificationTypeEnum.MUTUAL_FOLLOWING_ALERT.getType(), eventTimestamp, parametersForUserBeingFollowedAlert));
                           Map<String, String> parametersForFollowerAlert = new HashMap();
                           parametersForFollowerAlert.put("otherUsername", username);
                           String key2 = Integer.toString(userid);
                           unsProxy.notifyFusionUser(new Message(key2, otherUserid, otherUsername, Enums.NotificationTypeEnum.MUTUAL_FOLLOWING_ALERT.getType(), eventTimestamp, parametersForFollowerAlert));
                           unsProxy.clearNotificationsForUser(userid, Enums.NotificationTypeEnum.NEW_FOLLOWER_ALERT.getType(), new String[]{Integer.toString(otherUserid)});
                           unsProxy.clearNotificationsForUser(otherUserid, Enums.NotificationTypeEnum.NEW_FOLLOWER_ALERT.getType(), new String[]{Integer.toString(userid)});
                        } else if (type == MigboEnums.MigboFollowingEventTypeEnum.NEW_FOLLOWING) {
                           if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.ADD_CONTACT_ON_NEW_FOLLOWING_EVENT_ENABLED)) {
                              userContactData = contactBean.getContact(username, otherUsername);
                              if (StringUtil.isBlank(userContactData.username)) {
                                 userContactData.username = username;
                                 userContactData.fusionUsername = otherUsername;
                                 contactBean.addFusionUserAsContact(userid, userContactData, false);
                              }
                           }

                           if (StringUtil.toBooleanOrDefault(showAlertStr, true)) {
                              Map<String, String> parametersForFollowerAlert = new HashMap();
                              parametersForFollowerAlert.put("otherUsername", username);
                              String key = Integer.toString(userid);
                              unsProxy.notifyFusionUser(new Message(key, otherUserid, otherUsername, Enums.NotificationTypeEnum.NEW_FOLLOWER_ALERT.getType(), eventTimestamp, parametersForFollowerAlert));
                           }

                           MiniblogDailyDigestHelper.addRecipientForRecentlyFollowedDailyDigest(userid, otherUserid, System.currentTimeMillis());
                        } else if (type == MigboEnums.MigboFollowingEventTypeEnum.REMOVE_FOLLOWING) {
                           if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.REMOVE_CONTACT_ON_REMOVE_FOLLOWING_EVENT_ENABLED)) {
                              userContactData = contactBean.getContact(username, otherUsername);
                              if (userContactData.id != null) {
                                 contactBean.removeFusionUserFromContact(userid, username, userContactData.id, false);
                              }
                           }
                        } else {
                           log.warn("Unsupported migbo following event type: " + type);
                        }

                        boolean isAutoFollowBool = StringUtil.toBooleanOrDefault(isAutoFollow, false);
                        UserLocal userEJB;
                        UserData followBackerUserData;
                        UserData followedUserData;
                        if (type == MigboEnums.MigboFollowingEventTypeEnum.NEW_FOLLOWING) {
                           userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                           followBackerUserData = userEJB.loadUserFromID(userid);
                           followedUserData = userEJB.loadUserFromID(otherUserid);
                           sendRewardTrigger(new MigboFollowingEventTrigger(followBackerUserData, followedUserData, RelationshipEventTrigger.RelationshipEventTypeEnum.NEW, isAutoFollowBool));
                           sendRewardTrigger(new MigboFollowedByEventTrigger(followedUserData, followBackerUserData, RelationshipEventTrigger.RelationshipEventTypeEnum.NEW, isAutoFollowBool));
                        } else if (type == MigboEnums.MigboFollowingEventTypeEnum.MUTUAL_FOLLOWING) {
                           userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                           followBackerUserData = userEJB.loadUserFromID(userid);
                           followedUserData = userEJB.loadUserFromID(otherUserid);
                           sendRewardTrigger(new MigboFollowingEventTrigger(followBackerUserData, followedUserData, RelationshipEventTrigger.RelationshipEventTypeEnum.MUTUALLY_FOLLOWING, isAutoFollowBool));
                           sendRewardTrigger(new MigboFollowedByEventTrigger(followedUserData, followBackerUserData, RelationshipEventTrigger.RelationshipEventTypeEnum.MUTUALLY_FOLLOWING, isAutoFollowBool));
                           sendRewardTrigger(new MutuallyFollowingEventTrigger(followBackerUserData, followedUserData, true));
                           sendRewardTrigger(new MutuallyFollowingEventTrigger(followedUserData, followBackerUserData, false));
                        }

                        return Response.ok().entity(new DataHolder("ok")).build();
                     }

                     log.error(String.format("Unable to find UserNotificationServicePrx to push following request alert notification for user [%d], followerUserId %d", userid, otherUserid));
                  } catch (Exception var22) {
                     log.error(String.format("Unable to to handle the following event, userid %s, followerUserid %s, eventType %s", useridStr, otherUseridStr, eventTypeStr), var22);
                  }

                  throw new FusionRestException(101, String.format("Internal error: unable to handle the following event"));
               }
            }
         }
      }
   }

   private static void sendRewardTrigger(RewardProgramTrigger trigger) {
      try {
         RewardCentre.getInstance().sendTrigger(trigger);
      } catch (Exception var2) {
         log.error("Unexpected exception while sending event triggers " + var2, var2);
      }

   }

   @POST
   @Path("/groupinvite/{userid}/{action}/{groupid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response createGroupInvitationEventResponse(@PathParam("userid") String useridStr, @PathParam("groupid") String groupidStr, @PathParam("action") String actionStr) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      int groupid = StringUtil.toIntOrDefault(groupidStr, -1);
      if (!actionStr.equals("accept") && !actionStr.equals("reject")) {
         throw new FusionRestException(-1, String.format("Invalid action '%s'", actionStr));
      } else if (userid == -1) {
         throw new FusionRestException(-1, String.format("Invalid userid '%s'", useridStr));
      } else if (groupid == -1 && (!groupidStr.equals("@all") || !actionStr.equals("reject"))) {
         throw new FusionRestException(-1, String.format("Invalid groupid '%s'", groupidStr));
      } else {
         try {
            Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
            User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            String username = userBean.getUsernameByUserid(userid, (Connection)null);
            if (StringUtil.isBlank(username)) {
               throw new FusionRestException(101, String.format("User with id '%d' not found", userid));
            }

            if (actionStr.equals("accept")) {
               boolean smsNotification = false;
               boolean emailNotification = false;
               boolean eventNotification = true;
               boolean smsGroupEventNotification = false;
               boolean emailThreadUpdateNotification = false;
               boolean eventThreadUpdateNotification = false;
               int locationID = 0;
               String ipAddress = "127.0.0.1";

               try {
                  ipAddress = InetAddress.getLocalHost().toString();
               } catch (Exception var30) {
               }

               String sessionID = "None";
               String mobileDevice = "FusionRest";
               String userAgent = "FusionRest";
               webBean.joinGroup(username, groupid, locationID, ipAddress, sessionID, mobileDevice, userAgent, smsNotification, emailNotification, eventNotification, smsGroupEventNotification, emailThreadUpdateNotification, eventThreadUpdateNotification);
               log.debug(String.format("Accepted group invitation for user [%s][%d] group [%d]", username, userid, groupid));
            } else if (groupidStr.equals("@all")) {
               webBean.declineAllGroupInvitations(username);
               log.debug(String.format("Declined all group invitations for user [%s][%d]", username, userid));
            } else {
               webBean.declineGroupInvitation(username, groupid);
               log.debug(String.format("Declined group invitation for user [%s][%d] group [%d]", username, userid, groupid));
            }
         } catch (CreateException var31) {
            log.error(String.format("Unable to process friend invitation response due to EJB CreateException '%s'", var31.getMessage()), var31);
            throw new FusionRestException(101, String.format("Internal system error: unable to respond to the friend invite"));
         } catch (RemoteException var32) {
            log.error(String.format("Unable to process friend invitation response due to EJB RemoteException '%s'", var32.getMessage()), var32);
            throw new FusionRestException(101, String.format("Internal system error: unable to respond to the friend invite"));
         } finally {
            try {
               UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
               if (unsProxy != null) {
                  if (groupidStr.equals("@all")) {
                     unsProxy.clearAllNotificationsByTypeForUser(userid, Enums.NotificationTypeEnum.GROUP_INVITE.getType());
                  } else {
                     unsProxy.clearNotificationsForUser(userid, Enums.NotificationTypeEnum.GROUP_INVITE.getType(), new String[]{userid + "/" + groupid});
                  }
               }
            } catch (Exception var29) {
               log.warn("Unable to remove GROUP_INVITE notification for userid [" + userid + "] from group [" + groupidStr + "]", var29);
            }

         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/friendinvite/{userid}/{action}/{requestorUserid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response createFriendInvitationEventResponse(@PathParam("userid") String useridStr, @PathParam("requestorUserid") String requestorUseridStr, @PathParam("action") String actionStr) throws FusionRestException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Contacts.HANDLE_FRIENDINVITE_EVENTS_FROM_MIGBO_ENABLED)) {
         throw new FusionRestException(FusionRestException.RestException.SERVICE_DISABLED);
      } else {
         log.info(String.format("received friend invitation response %s %s %s", useridStr, requestorUseridStr, actionStr));
         int userid = StringUtil.toIntOrDefault(useridStr, -1);
         int requestorUserid = StringUtil.toIntOrDefault(requestorUseridStr, -1);
         if (userid == -1) {
            throw new FusionRestException(-1, String.format("Invalid userid '%s'", useridStr));
         } else if (requestorUserid == -1) {
            throw new FusionRestException(-1, String.format("Invalid requestorUserid '%s'", requestorUseridStr));
         } else if (!actionStr.equals("accept") && !actionStr.equals("reject") && !actionStr.equals("rejectAndBlock")) {
            throw new FusionRestException(-1, String.format("Invalid action '%s'", actionStr));
         } else {
            String requestorUsername = "";

            try {
               Contact contactBean = (Contact)EJBHomeCache.getObject("ejb/Contact", ContactHome.class);
               User userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
               String username = userBean.getUsernameByUserid(userid, (Connection)null);
               requestorUsername = userBean.getUsernameByUserid(requestorUserid, (Connection)null);
               if (StringUtil.isBlank(username)) {
                  throw new FusionRestException(101, String.format("User with id '%d' not found", userid));
               }

               if (StringUtil.isBlank(requestorUsername)) {
                  throw new FusionRestException(101, String.format("User with id '%d' not found", requestorUserid));
               }

               if (actionStr.equals("accept")) {
                  ContactData contactData = new ContactData();
                  contactData.username = username;
                  contactData.fusionUsername = requestorUsername;
                  contactData.contactGroupId = null;
                  contactData.displayOnPhone = true;
                  contactData.shareMobilePhone = false;
                  contactBean.acceptContactRequest(userid, contactData, false);
               } else {
                  contactBean.rejectContactRequest(userid, username, requestorUsername);
                  if (actionStr.equals("rejectAndBlock")) {
                     contactBean.blockContact(userid, username, requestorUsername);
                  }
               }
            } catch (CreateException var20) {
               log.error(String.format("Unable to process friend invitation response due to EJB CreateException '%s'", var20.getMessage()), var20);
               throw new FusionRestException(101, String.format("Internal system error: unable to respond to the friend invite"));
            } catch (RemoteException var21) {
               log.error(String.format("Unable to process friend invitation response due to EJB RemoteException '%s'", var21.getMessage()), var21);
               throw new FusionRestException(101, String.format("Internal system error: unable to respond to the friend invite"));
            } finally {
               try {
                  UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                  if (!StringUtil.isBlank(requestorUsername) && unsProxy != null) {
                     unsProxy.clearNotificationsForUser(userid, Enums.NotificationTypeEnum.FRIEND_INVITE.getType(), new String[]{requestorUsername});
                  }
               } catch (Exception var19) {
                  log.warn("Unable to remove FRIEND_INVITE notification for [" + userid + "] from [" + requestorUsername + "]", var19);
               }

            }

            return Response.ok().entity(new DataHolder("ok")).build();
         }
      }
   }

   @POST
   @Path("/alertbatch")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response processAlertBatch(String data) throws FusionRestException {
      UserNotificationServicePrx unsProxy = this.getUserNotificationServicePrx();
      if (unsProxy != null) {
         try {
            JSONArray jsonArray = new JSONArray(data);
            String rateLimit = SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.FusionRestRateLimit.EVENT_ALERTBATCH);

            for(int i = 0; i < jsonArray.length(); ++i) {
               JSONObject jsonObj = jsonArray.getJSONObject(i);
               Message msg = new Message();
               msg.key = jsonObj.getString("key");
               msg.toUserId = jsonObj.getInt("toUserId");
               msg.toUsername = jsonObj.getString("toUsername");
               msg.notificationType = jsonObj.getInt("notificationType");
               msg.dateCreated = jsonObj.getLong("dateCreated");
               msg.parameters = new HashMap();
               JSONObject parametersJsonObj = jsonObj.getJSONObject("parameters");
               JSONArray parameterKeys = parametersJsonObj.names();

               String rateLimitKey;
               for(int j = 0; j < parameterKeys.length(); ++j) {
                  rateLimitKey = parameterKeys.getString(j);
                  String parameterValue = parametersJsonObj.getString(rateLimitKey);
                  msg.parameters.put(rateLimitKey, parameterValue);
               }

               try {
                  String parentFullPostid = (String)msg.parameters.get("parentFullPostid");
                  if (!StringUtil.isBlank(parentFullPostid)) {
                     rateLimitKey = parentFullPostid + ':' + msg.toUserId;
                     MemCachedRateLimiter.hit(MemCachedRateLimiter.NameSpace.FUSIONREST_ALERTBATCH, rateLimitKey, rateLimit);
                  }

                  unsProxy.notifyFusionUser(msg);
               } catch (MemCachedRateLimiter.LimitExceeded var13) {
                  log.warn(String.format("Rate limit exceeded in /event/alertbatch when sending alert of type %d to user %s key %s : %s", msg.notificationType, msg.toUsername, msg.key, var13.getMessage()));
               } catch (MemCachedRateLimiter.FormatError var14) {
                  log.error(String.format("Format Error in /event/alertbatch : %s", var14.getMessage()));
               }
            }
         } catch (JSONException var15) {
            log.error("Unable to parse JSON data for alert batch processing: " + var15);
         } catch (Exception var16) {
            log.error("Unable to process alert batch: " + var16);
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      } else {
         log.error("Unable to find UserNotificationServicePrx to for alert batch processing");
         throw new FusionRestException(101, String.format("Internal error: Unable to process alert batch"));
      }
   }

   @POST
   @Path("/rewardprogram/group/topic/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitGroupTopicCreatedRewardProgramTrigger(@PathParam("userid") String useridStr, GroupActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         String message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the group topic created reward program trigger, userid %s", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            GroupActivityTrigger trigger = new GroupActivityTrigger(GroupActivityTrigger.ActivityTypeEnum.TOPIC_CREATED, authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.groupID = data.groupID;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the group topic created reward program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the group topic created reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/rewardprogram/group/topic_comment/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitGroupTopicCommentRewardProgramTrigger(@PathParam("userid") String useridStr, GroupActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         String message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the group topic comment reward program trigger, userid %ss", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            GroupActivityTrigger trigger = new GroupActivityTrigger(GroupActivityTrigger.ActivityTypeEnum.TOPIC_COMMENTED, authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.groupID = data.groupID;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the group topic comment program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the group topic comment reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/rewardprogram/group/wallpost/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitGroupWallPostCreatedRewardProgramTrigger(@PathParam("userid") String useridStr, GroupActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         String message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the group topic comment reward program trigger, userid %ss", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            GroupActivityTrigger trigger = new GroupActivityTrigger(GroupActivityTrigger.ActivityTypeEnum.WALLPOST_CREATED, authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.groupID = data.groupID;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the group topic comment program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the group topic comment reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/rewardprogram/group/wallpost_comment/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitGroupWallPostCommentRewardProgramTrigger(@PathParam("userid") String useridStr, GroupActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      if (userid == -1) {
         String message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the group wallpost comment reward program trigger, userid %ss", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            GroupActivityTrigger trigger = new GroupActivityTrigger(GroupActivityTrigger.ActivityTypeEnum.WALLPOST_COMMENTED, authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.groupID = data.groupID;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the group wallpost comment program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the group wallpost comment reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/rewardprogram/thirdpartyapp/start_event/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitThirdPartyAppStartEventRewardProgramTrigger(@PathParam("userid") String useridStr, ThirdPartyAppActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      String message;
      if (userid == -1) {
         message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else if (StringUtil.isBlank(data.applicationName)) {
         message = "applicationName cannot be empty";
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the third party app start event reward program trigger, userid %ss", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            ThirdPartyAppStartEventTrigger trigger = new ThirdPartyAppStartEventTrigger(authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.applicationName = data.applicationName;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the third party app start event program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the third party app start event reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/rewardprogram/thirdpartyapp/internal_invite_sent/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitThirdPartyAppInternalInviteSentEventRewardProgramTrigger(@PathParam("userid") String useridStr, ThirdPartyAppActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      String message;
      if (userid == -1) {
         message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else if (StringUtil.isBlank(data.applicationName)) {
         message = "applicationName cannot be empty";
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the third party app start event reward program trigger, userid %ss", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            ThirdPartyAppInternalInvitationTrigger trigger = new ThirdPartyAppInternalInvitationTrigger(ThirdPartyAppInternalInvitationTrigger.StateEnum.SENT, authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.applicationName = data.applicationName;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the third party app start event program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the third party app start event reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/rewardprogram/thirdpartyapp/internal_invite_accepted/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitThirdPartyAppInternalInviteAcceptedEventRewardProgramTrigger(@PathParam("userid") String useridStr, ThirdPartyAppActivityRewardProgramTriggerData data) throws FusionRestException {
      int userid = StringUtil.toIntOrDefault(useridStr, -1);
      String message;
      if (userid == -1) {
         message = String.format("Invalid userid '%s' specified", useridStr);
         log.error(message);
         throw new FusionRestException(-1, message);
      } else if (StringUtil.isBlank(data.applicationName)) {
         message = "applicationName cannot be empty";
         log.error(message);
         throw new FusionRestException(-1, message);
      } else {
         User userBean = null;

         try {
            userBean = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (CreateException var8) {
            log.error(String.format("Unable to notify RewardCentre about the third party app start event reward program trigger, userid %ss", useridStr), var8);
            throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
         }

         try {
            UserData authorUserData = userBean.loadUserFromID(userid);
            ThirdPartyAppInternalInvitationTrigger trigger = new ThirdPartyAppInternalInvitationTrigger(ThirdPartyAppInternalInvitationTrigger.StateEnum.ACCEPTED, authorUserData);
            trigger.amountDelta = 0.0D;
            trigger.quantityDelta = 1;
            trigger.applicationName = data.applicationName;
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var7) {
            log.error(String.format("Unable to notify RewardCentre about the third party app start event program trigger, userid %s", useridStr), var7);
            throw new FusionRestException(101, String.format("Internal error: unable to notify RewardCentre about the third party app start event reward program trigger"));
         }

         return Response.ok().entity(new DataHolder("ok")).build();
      }
   }

   @POST
   @Path("/migbo_campaign/{campaignid}/{userid}")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response submitMigboCampaignEvent(@PathParam("campaignid") String campaignidStr, @PathParam("userid") String useridStr, @QueryParam("type") String typeStr, String jsonBody) throws FusionRestException {
      int campaignId = StringUtil.toIntOrDefault(campaignidStr, 0);
      if (campaignId < 1) {
         throw new FusionRestException(101, String.format("Invalid campaignid provided [%s]", campaignidStr));
      } else {
         int userid = StringUtil.toIntOrDefault(useridStr, 0);
         if (userid < 1) {
            throw new FusionRestException(101, String.format("Invalid userid provided [%s]", useridStr));
         } else {
            MigboCampaignTrigger.EventTypeEnum type = MigboCampaignTrigger.EventTypeEnum.fromString(typeStr);
            if (type == null) {
               throw new FusionRestException(101, String.format("Invalid type provided [%s]", typeStr));
            } else {
               JSONObject jsonObj;
               try {
                  jsonObj = new JSONObject(jsonBody);
               } catch (Exception var20) {
                  log.error(String.format("JSONException caught while parsing jsonBody %s", var20.getMessage()), var20);
                  throw new FusionRestException(101, "Invalid JSON body provided : " + jsonBody);
               }

               MigboCampaignTrigger trigger = null;
               UserLocal userBean = null;

               try {
                  userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               } catch (CreateException var19) {
                  log.error("Unable to initialize UserBean in submitMigboCampaignEvent()", var19);
                  throw new FusionRestException(101, String.format("Internal error: unable to initialize UserBean"));
               }

               long eventTimestamp = StringUtil.toLongOrDefault(jsonObj.optString("timestamp"), System.currentTimeMillis());
               String entityType = jsonObj.optString("entityType");
               String entityId = jsonObj.optString("entityID");
               int tagValue = jsonObj.optInt("tagValue");
               UserData userData = userBean.loadUserFromID(userid);
               if (userData == null) {
                  throw new FusionRestException(101, String.format("Invalid userid provided", useridStr));
               } else {
                  switch(type) {
                  case REGISTRATION:
                     trigger = MigboCampaignTrigger.getCampaignRegistrationTrigger(userData, campaignId, eventTimestamp);
                     break;
                  case TAG_CREATED:
                     trigger = MigboCampaignTrigger.getTagCreatedTrigger(userData, campaignId, eventTimestamp, entityType, entityId, tagValue);
                     break;
                  default:
                     throw new FusionRestException(101, String.format("Unsupported eventType : %s", typeStr));
                  }

                  try {
                     RewardCentre.getInstance().sendTrigger(trigger);
                  } catch (Exception var18) {
                     log.error(String.format("Unknown exception while submitting trigger %s", var18.getMessage(), var18));
                     throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
                  }

                  return Response.ok().entity(new DataHolder("ok")).build();
               }
            }
         }
      }
   }
}
