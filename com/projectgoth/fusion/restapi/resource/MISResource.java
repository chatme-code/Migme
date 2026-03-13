package com.projectgoth.fusion.restapi.resource;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.restapi.data.BooleanData;
import com.projectgoth.fusion.restapi.data.DataHolder;
import com.projectgoth.fusion.restapi.data.FusionRestException;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.ext.Provider;
import org.apache.log4j.Logger;

@Provider
@Path("/mis")
public class MISResource {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(MISResource.class));

   @POST
   @Path("/chatroom/announce")
   @Produces({"application/json"})
   public DataHolder<Integer[]> doAnnounceToChatrooms(@FormParam("chatrooms") @DefaultValue("") String chatrooms, @FormParam("message") String message, @FormParam("all") @DefaultValue("0") Integer sendToAll, @FormParam("waittime") @DefaultValue("1000") Integer waitTime) throws FusionRestException {
      if (StringUtil.isBlank(chatrooms) && sendToAll != 1) {
         throw new FusionRestException(101, "No chatroom names provided.");
      } else if (StringUtil.isBlank(message)) {
         throw new FusionRestException(101, "No message provided.");
      } else {
         String[] chatroomNames = null;
         Integer[] announcedChatrooms = null;

         try {
            MessageLocal msgLocal = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            if (sendToAll == 1) {
               announcedChatrooms = msgLocal.announceMessageToUserOwnedChatrooms(message, waitTime);
            } else {
               chatroomNames = chatrooms.split(";");
               announcedChatrooms = msgLocal.announceMessageToChatrooms(chatroomNames, message, waitTime);
            }
         } catch (Exception var8) {
            throw new FusionRestException(101, var8.getMessage());
         }

         return new DataHolder(announcedChatrooms);
      }
   }

   @POST
   @Path("/{username}/deletefilefromscrapbook")
   @Produces({"application/json"})
   public DataHolder<BooleanData> deleteFileFromScrapbook(@PathParam("username") String username, @QueryParam("id") int id) throws FusionRestException {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         misBean.deleteFileFromScrapbook(username, id);
         return new DataHolder(new BooleanData(true));
      } catch (Exception var4) {
         log.error(String.format("Error in deletefilefromscrapbook username:%s", username), var4);
         throw new FusionRestException(101, "Unable to deletefilefromscrapbook");
      }
   }
}
