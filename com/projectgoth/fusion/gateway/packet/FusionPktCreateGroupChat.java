package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataCreateGroupChat;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.AuthenticationServiceCredentialResponse;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.ObjectCachePrx;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.UserPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

public class FusionPktCreateGroupChat extends FusionPktDataCreateGroupChat {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktCreateGroupChat.class));
   private static final short FIELD_OTHER_PARTY_LIST = 4;
   private static final String WEB_PREFIX = "web:";
   private static final String COMMA = ",";
   private static final Pattern COMMA_SPLITTER = Pattern.compile(",");

   public FusionPktCreateGroupChat() {
   }

   public FusionPktCreateGroupChat(short transactionId) {
      super(transactionId);
   }

   public FusionPktCreateGroupChat(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktCreateGroupChat(FusionPacket packet) {
      super(packet);
   }

   public String[] getOtherPartyList() {
      String asString = this.getStringField((short)4);
      if (asString != null && asString.startsWith("web:")) {
         String remainder = asString.substring("web:".length());
         String[] tokens = COMMA_SPLITTER.split(remainder);
         return tokens;
      } else {
         return this.getInvitedUsernameList();
      }
   }

   public void setInvitedUsernameList(String usernames) {
      this.setField((short)4, usernames);
   }

   private FusionPacket[] createFusionGroupChat(ConnectionI connection) throws Exception {
      if (System.currentTimeMillis() - connection.getLastGroupChatCreated() < (long)connection.getGateway().getGeneralCoolDown()) {
         throw new Exception("You recently created a group chat. Please wait a short while before creating another one");
      } else {
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx == null) {
            throw new Exception("Unable to locate registry");
         } else {
            String privateChatter = this.getUsername();
            String invitedUser = this.getInvitedUsername();
            String[] otherPartyList = this.getOtherPartyList();
            String groupChatId;
            if (log.isDebugEnabled() && otherPartyList != null) {
               String logMe = "";
               String[] arr$ = otherPartyList;
               int len$ = otherPartyList.length;

               for(int i$ = 0; i$ < len$; ++i$) {
                  groupChatId = arr$[i$];
                  logMe = logMe + groupChatId + " ";
               }

               log.debug("otherPartyList=" + logMe);
            }

            boolean oldStyleInvalid = (privateChatter == null || invitedUser == null) && otherPartyList == null;
            if (oldStyleInvalid) {
               throw new Exception("Private chatter and initial invited user should be specified");
            } else {
               boolean newStyleInvalid = invitedUser != null && otherPartyList != null;
               if (newStyleInvalid) {
                  throw new Exception("Initial invited user and participant list cannot both be specified");
               } else {
                  String creator = connection.getUsername();
                  ObjectCachePrx objectCachePrx = registryPrx.getLowestLoadedObjectCache();
                  groupChatId = ConnectionI.newSessionID();
                  if (otherPartyList == null) {
                     ArrayList<String> converted = new ArrayList();
                     converted.add(invitedUser);
                     otherPartyList = new String[converted.size()];
                     converted.toArray(otherPartyList);
                  }

                  String privateChatterIce = privateChatter == null ? "\u0000" : privateChatter;
                  GroupChatPrx groupChatPrx = objectCachePrx.createGroupChatObject(groupChatId, creator, privateChatterIce, otherPartyList);
                  FusionPktGroupChat pkt = new FusionPktGroupChat(this.transactionId);
                  pkt.setGroupChatId(groupChatId);
                  pkt.setCreator(creator);
                  pkt.setIMType(ImType.FUSION.value());
                  connection.sendFusionPacket(pkt);
                  groupChatPrx.sendInitialMessages();
                  connection.groupChatCreated();
                  return null;
               }
            }
         }
      }
   }

   private FusionPacket[] createOtherIMConferenceChat(ConnectionI connection, ImType imType) throws Exception {
      String participant = this.getUsername();
      if (participant == null) {
         throw new Exception("No participant specified");
      } else {
         String invitedUser = this.getInvitedUsername();
         if (invitedUser == null) {
            throw new Exception("No invited user specified");
         } else {
            UserPrx userPrx = connection.getUserPrx();
            if (userPrx == null) {
               throw new Exception("You are no longer logged in");
            } else {
               String conferenceID = userPrx.otherIMInviteToConference(imType.value(), participant, invitedUser);
               int userID = connection.getUserID();
               byte passwordType = PasswordType.forIMEnum(imType).value();
               AuthenticationServiceCredentialResponse response = connection.findAuthenticationService().getCredential(userID, passwordType);
               if (response.code != AuthenticationServiceResponseCodeEnum.Success) {
                  throw new Exception("Unable to retrieve " + imType + " username. " + response.code);
               } else {
                  String creator = response.userCredential.username;
                  FusionPktGroupChat pkt = new FusionPktGroupChat(this.transactionId);
                  pkt.setGroupChatId(conferenceID);
                  pkt.setCreator(creator);
                  pkt.setIMType(imType.value());
                  return pkt.toArray();
               }
            }
         }
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         ImType imType = this.getImType();
         switch(imType != null ? imType : ImType.FUSION) {
         case FUSION:
            return this.createFusionGroupChat(connection);
         case MSN:
         case YAHOO:
            return this.createOtherIMConferenceChat(connection, imType);
         default:
            throw new Exception("Group chat is not supported for IM type " + imType);
         }
      } catch (ObjectNotFoundException var3) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create group chat - Failed to find lowest loaded object cache")).toArray();
      } catch (ObjectExistsException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create group chat - Group chat already exists in object cache")).toArray();
      } catch (FusionException var5) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create group chat - " + var5.message)).toArray();
      } catch (LocalException var6) {
         return (new FusionPktInternalServerError(this.transactionId, var6, "Failed to create group chat")).toArray();
      } catch (Exception var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to create group chat - " + var7.getMessage())).toArray();
      }
   }
}
