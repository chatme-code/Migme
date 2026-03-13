package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.ejb.EJBIcePrxFinder;
import com.projectgoth.fusion.interfaces.Group;
import com.projectgoth.fusion.interfaces.GroupHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import com.projectgoth.fusion.restapi.data.GroupMembersListData;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.util.List;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/group")
public class GroupResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupResource.class));

   @POST
   @Path("/{groupid}/moderator/{username}")
   @Produces({"application/json"})
   public DataHolder<BooleanData> giveGroupAdminRights(@PathParam("groupid") int groupid, @PathParam("username") String username) throws FusionRestException {
      return this.updateModeratorStatus(groupid, username, true);
   }

   @DELETE
   @Path("/{groupid}/moderator/{username}")
   @Produces({"application/json"})
   public DataHolder<BooleanData> removeGroupAdminRights(@PathParam("groupid") int groupid, @PathParam("username") String username) throws FusionRestException {
      return this.updateModeratorStatus(groupid, username, false);
   }

   private DataHolder<BooleanData> updateModeratorStatus(int groupid, String username, boolean promote) throws FusionRestException {
      if (StringUtil.isBlank(username)) {
         throw new FusionRestException(FusionRestException.RestException.ERROR, "Invalid username" + username);
      } else {
         try {
            UserPrx userPrx = EJBIcePrxFinder.findUserPrx(username);
            if (userPrx == null) {
               return new DataHolder(new BooleanData(true));
            } else {
               MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               String[] chatrooms = messageEJB.getGroupChatRooms(groupid);
               ChatRoomPrx[] chatRoomProxies = null;
               if (chatrooms != null && chatrooms.length > 0) {
                  chatRoomProxies = EJBIcePrxFinder.findChatRoomProxies(chatrooms);
               }

               if (log.isDebugEnabled()) {
                  if (chatRoomProxies != null) {
                     log.debug("For group:" + groupid + " ,user[" + username + "] is in " + chatRoomProxies.length + " chatrooms");
                  } else {
                     log.debug("For group:" + groupid + " ,user[" + username + "] :active chatroomproxies is null");
                  }
               }

               if (chatRoomProxies != null) {
                  ChatRoomPrx[] arr$ = chatRoomProxies;
                  int len$ = chatRoomProxies.length;

                  for(int i$ = 0; i$ < len$; ++i$) {
                     ChatRoomPrx chatroomPrx = arr$[i$];
                     if (chatroomPrx != null) {
                        chatroomPrx.updateGroupModeratorStatus(username, promote);
                     }
                  }
               }

               return new DataHolder(new BooleanData(true));
            }
         } catch (CreateException var12) {
            log.error("Unable to create ejb for user or message bean to change moderator status user:" + username + " to " + promote);
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (EJBException var13) {
            log.error("Unable to change moderator status of user " + username + " to " + promote + ":" + var13.getMessage());
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         } catch (Exception var14) {
            log.error("Exception in changing moderator status for user " + username + " to " + promote + ":" + var14.getMessage());
            throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
         }
      }
   }

   @GET
   @Path("/{groupid}/members")
   @Produces({"application/json"})
   public DataHolder<GroupMembersListData> getGroupMembers(@PathParam("groupid") int groupId) throws FusionRestException {
      try {
         Group groupEJB = (Group)EJBHomeCache.getObject("ejb/Group", GroupHome.class);
         List<Integer> members = groupEJB.getGroupMembers(groupId);
         return new DataHolder(new GroupMembersListData(members));
      } catch (CreateException var4) {
         log.error("Create Exception", var4);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (EJBException var5) {
         log.error(var5.getMessage());
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      } catch (Exception var6) {
         log.error("Unhandled Exception", var6);
         throw new FusionRestException(FusionRestException.RestException.INTERNAL_SERVER_ERROR);
      }
   }
}
