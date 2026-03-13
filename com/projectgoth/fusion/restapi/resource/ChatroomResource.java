package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.MemCachedRateLimiter;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.Web;
import com.projectgoth.fusion.interfaces.WebHome;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.ChatroomSettingsMultiIDData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import java.sql.Connection;
import java.util.Hashtable;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.Consumes;
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
@Path("/chatroom")
public class ChatroomResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatroomResource.class));
   private static final Logger setMultiIDSettingsLog = Logger.getLogger("SetMultiIDSettingsLog");

   @GET
   @Path("/{chatroomname}/settings/multiid")
   @Produces({"application/json"})
   public DataHolder<ChatroomSettingsMultiIDData> getMultiIDSettings(@PathParam("chatroomname") String chatroomName) throws FusionRestException {
      if (StringUtil.isBlank(chatroomName)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid chatroom name " + chatroomName);
      } else {
         try {
            MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            String normalizedChatroomName = ChatRoomUtils.normalizeChatRoomName(chatroomName);
            ChatRoomData chatRoomData = messageEJB.getSimpleChatRoomData((String)normalizedChatroomName, (Connection)null);
            ChatroomSettingsMultiIDData data = new ChatroomSettingsMultiIDData();
            data.retrieveFromChatRoomData(chatRoomData);
            return new DataHolder(data);
         } catch (CreateException var6) {
            log.error(String.format("Unable to create ejb for message bean to get multi id settings for chatroom %s", chatroomName));
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var7) {
            log.error(String.format("Unable to get multi id settings for chatroom %s: %s", chatroomName, var7.getMessage()));
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }
      }
   }

   @POST
   @Path("/{chatroomname}/settings/multiid")
   @Consumes({"application/json"})
   @Produces({"application/json"})
   public Response setMultiIDSettings(@PathParam("chatroomname") String chatroomName, @QueryParam("requestingUserid") String strUserID, DataHolder<ChatroomSettingsMultiIDData> dataHolder) throws FusionRestException {
      ChatroomSettingsMultiIDData data = (ChatroomSettingsMultiIDData)dataHolder.data;
      if (data.minMigLevel != null && data.minMigLevel < 0) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid min mig level: " + data.minMigLevel);
      } else if (data.rateLimitByIP != null && !MemCachedRateLimiter.isValidHitPerDuration(data.rateLimitByIP)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid rate limit pattern: " + data.rateLimitByIP);
      } else {
         int userID = StringUtil.toIntOrDefault(strUserID, -1);
         if (userID == -1) {
            throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid requestingUserId specified: " + strUserID);
         } else {
            try {
               UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               UserData userData = userEJB.loadUserFromID(userID);
               if (userData == null) {
                  throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find user with ID '" + strUserID + "'");
               } else {
                  MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                  String normalizedChatroomName = ChatRoomUtils.normalizeChatRoomName(chatroomName);
                  ChatRoomData chatRoomData = messageEJB.getSimpleChatRoomData((String)normalizedChatroomName, (Connection)null);
                  if (chatRoomData == null) {
                     throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to find chatroom '" + chatroomName + "'");
                  } else {
                     boolean blockAccess = true;
                     String userLabel = "";
                     if (userData.chatRoomAdmin) {
                        userLabel = "GlobalAdmin";
                        blockAccess = false;
                     } else if (chatRoomData.userOwned) {
                        if (userData.username.equals(chatRoomData.creator)) {
                           userLabel = "ChatroomOwner";
                           blockAccess = false;
                        }

                        if (blockAccess && chatRoomData.belongsToGroup()) {
                           GroupData groupData = userEJB.getGroup(chatRoomData.groupID);
                           if (groupData == null) {
                              throw new FusionRestException(FusionRestException.RestException.ERROR, "Unable to retrieve group information for chatroom '" + chatroomName + "'");
                           }

                           if (userData.username.equals(groupData.createdBy)) {
                              userLabel = "GroupOwner";
                              blockAccess = false;
                           }
                        }

                        if (blockAccess && messageEJB.isModeratorOfChatRoom(userData.username, chatRoomData.name)) {
                           userLabel = "ChatroomModerator";
                           blockAccess = false;
                        }
                     }

                     if (blockAccess) {
                        throw new FusionRestException(FusionRestException.RestException.ERROR, "User '" + userData.username + "' does not have access to chatroom '" + chatroomName + "'");
                     } else {
                        data.updateToChatRoomData(chatRoomData);
                        messageEJB.updateRoomExtraData(chatRoomData);
                        StringBuilder sb = new StringBuilder();
                        sb.append(userData.username).append(",").append(userLabel).append(",").append(chatRoomData.name).append(",").append(chatRoomData.userOwned ? "UserOwned" : "Global").append(",").append(data.minMigLevel == null ? "None" : data.minMigLevel).append(",").append(StringUtil.isBlank(data.rateLimitByIP) ? "None" : data.rateLimitByIP);
                        setMultiIDSettingsLog.info(sb.toString());
                        return Response.ok().entity(new DataHolder("ok")).build();
                     }
                  }
               }
            } catch (CreateException var14) {
               log.error(String.format("Unable to create ejb for message bean to get multi id settings for chatroom %s", chatroomName));
               throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
            } catch (EJBException var15) {
               log.error(String.format("Unable to get multi id settings for chatroom %s: %s", chatroomName, var15.getMessage()));
               throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
            }
         }
      }
   }

   @POST
   @Path("/{chatroomname}/maxsize")
   @Produces({"application/json"})
   public DataHolder<BooleanData> setMaxSize(@PathParam("chatroomname") String chatRoomName, @QueryParam("maxsize") int maxSize) throws FusionRestException {
      if (log.isDebugEnabled()) {
         log.debug("ChatroomResource.setMaxSize chatroomname=" + chatRoomName + " maxsize=" + maxSize);
      }

      try {
         MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         String normalizedChatroomName = ChatRoomUtils.normalizeChatRoomName(chatRoomName);
         messageEJB.setChatRoomMaxSize(normalizedChatroomName, maxSize);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var5) {
         log.error("Unable to set chatroom maximum size for chatroom=" + chatRoomName + ": " + var5, var5);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      }
   }

   @POST
   @Path("/{chatroom}/deregisterchatroomice")
   @Produces({"application/json"})
   public DataHolder<BooleanData> deregisterChatroomIce(@PathParam("chatroom") String chatroom) throws FusionRestException {
      try {
         EJBIcePrxFinder.getRegistry().deregisterChatRoomObject(chatroom);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var3) {
         log.error(String.format("Error in deregisterchatroomice chatroom:%s", chatroom), var3);
         throw new FusionRestException(101, "Unable to deregisterchatroomice");
      }
   }

   @GET
   @Path("/{chatroomname}/getchatroom")
   @Produces({"application/json"})
   public DataHolder<Hashtable> getChatroom(@PathParam("chatroomname") String chatRoomName) throws FusionRestException {
      try {
         Web webBean = (Web)EJBHomeCache.getObject("ejb/Web", WebHome.class);
         return new DataHolder(webBean.getChatroom(chatRoomName));
      } catch (Exception var3) {
         log.error(String.format("Error in getChatroom chatRoomName:%s", chatRoomName), var3);
         throw new FusionRestException(101, "Unable to getChatroom");
      }
   }
}
