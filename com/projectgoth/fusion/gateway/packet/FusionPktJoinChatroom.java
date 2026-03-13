package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import Ice.ObjectNotExistException;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.common.ChatRoomUtils;
import com.projectgoth.fusion.common.ChatRoomValidationException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AlertMessageData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.enums.AlertContentType;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.fdl.packets.FusionPktDataAlert;
import com.projectgoth.fusion.fdl.packets.FusionPktDataJoinChatroom;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.exceptions.FusionRequestException;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ObjectCachePrx;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.ObjectNotFoundException;
import com.projectgoth.fusion.slice.RegistryPrx;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktJoinChatroom extends FusionPktDataJoinChatroom {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktJoinChatroom.class));

   public FusionPktJoinChatroom() {
   }

   public FusionPktJoinChatroom(short transactionId) {
      super(transactionId);
   }

   public FusionPktJoinChatroom(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktJoinChatroom(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatroomName();
   }

   protected void preValidate(ConnectionI connection) throws FusionRequestException {
      if (connection.isBannedFromChatrooms()) {
         throw new FusionRequestException(FusionRequestException.ExceptionType.PREVALIDATION, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BAN_ERROR_MESSAGE));
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatroomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new FusionException("You are not logged in");
         } else {
            UserDataIce userData;
            try {
               userData = userPrx.getUserData();
            } catch (ObjectNotExistException var18) {
               log.info("JoinChatRoom " + chatRoomName + " failed for user:" + connection.getUsername() + " because:" + var18.getMessage(), var18);
               throw new FusionException("You are not logged in");
            }

            if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.ENTER_CHATROOM, new UserData(userData))) {
               throw new FusionException("You must authenticate your account");
            } else {
               RegistryPrx registryPrx = connection.findRegistry();
               if (registryPrx == null) {
                  throw new Exception("Unable to locate registry");
               } else {
                  ChatRoomPrx chatRoomPrx = null;

                  try {
                     chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
                  } catch (ObjectNotFoundException var17) {
                  }

                  if (chatRoomPrx == null) {
                     try {
                        ObjectCachePrx objectCachePrx = registryPrx.getLowestLoadedObjectCache();
                        chatRoomPrx = objectCachePrx.createChatRoomObject(chatRoomName);
                     } catch (ObjectNotFoundException var14) {
                        return (new FusionPktInternalServerError(this.transactionId, var14, "No ObjectCache found")).toArray();
                     } catch (ObjectExistsException var15) {
                        try {
                           chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
                        } catch (ObjectNotFoundException var13) {
                           return (new FusionPktInternalServerError(this.transactionId, var15, "Unable to join chat room")).toArray();
                        }
                     } catch (FusionException var16) {
                        log.info("Attempt to join invalid chatroom : " + chatRoomName + " by user: " + userData.username);
                        throw var16;
                     }
                  }

                  SessionPrx sessionPrx = connection.getSessionPrx();
                  if (sessionPrx == null) {
                     throw new Exception("You are no longer logged in");
                  } else {
                     if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Chatroom.PARTICIPANT_CACHE_DATA, true)) {
                        chatRoomPrx.addParticipant(userPrx, userData, sessionPrx, connection.getSessionID(), connection.getRemoteAddress(), connection.getMobileDevice(), connection.getUserAgent(), connection.getClientVersion(), connection.getDeviceType().value());
                     } else {
                        chatRoomPrx.addParticipantOld(userPrx, userData, sessionPrx, connection.getSessionID(), connection.getRemoteAddress(), connection.getMobileDevice(), connection.getUserAgent());
                     }

                     sessionPrx.chatroomJoined(chatRoomPrx, chatRoomName);
                     List<FusionPacket> packetsToReturn = new ArrayList();
                     if (!connection.isAjax() && !connection.isMobileClientV2()) {
                        if (connection.isMidletVersionAndAbove(440)) {
                           ChatRoomData chatroomData = new ChatRoomData(chatRoomPrx.getRoomData());
                           if (chatroomData.themeID != null && chatRoomPrx.getTheme() != null) {
                              packetsToReturn.add(new FusionPktChatRoomTheme(this.transactionId, chatroomData.themeID, chatRoomPrx.getTheme()));
                           }

                           packetsToReturn.add(new FusionPktChatroom(this.transactionId, chatroomData));
                        } else {
                           packetsToReturn.add(new FusionPktOk(this.transactionId));
                        }
                     } else {
                        packetsToReturn.add(new FusionPktChatroom(this.transactionId, new ChatRoomData(chatRoomPrx.getRoomData())));
                     }

                     if (connection.isMobileClientV2AndNewVersion()) {
                        FusionPktChatroomParticipants reply = new FusionPktChatroomParticipants(this.transactionId);
                        reply.setChatroomName(chatRoomName);
                        String[] participantArray = chatRoomPrx.getParticipants(connection.getUsername());
                        if (participantArray != null && participantArray.length > 0) {
                           reply.setParticipantList(participantArray);
                        }

                        String[] administrators = chatRoomPrx.getAdministrators(connection.getUsername());
                        if (administrators != null && administrators.length > 0) {
                           reply.setAdministratorList(administrators);
                        }

                        String[] mutedUsers = connection.getUserPrx().getBlockListFromUsernames(participantArray);
                        if (mutedUsers != null && mutedUsers.length > 0) {
                           reply.setMutedList(mutedUsers);
                        }

                        packetsToReturn.add(reply);
                     }

                     User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                     AlertMessageData alertMessage;
                     if (connection.getDeviceType() == ClientType.MIDP1) {
                        alertMessage = userEJB.getLatestAlertMessage(connection.getClientVersion(), AlertMessageData.TypeEnum.CHAT_ROOM, userData.countryID, new Date(userData.lastLoginDate), AlertContentType.TEXT, ClientType.MIDP2.value());
                     } else {
                        alertMessage = userEJB.getLatestAlertMessage(connection.getClientVersion(), AlertMessageData.TypeEnum.CHAT_ROOM, userData.countryID, new Date(userData.lastLoginDate), (AlertContentType)null, connection.getDeviceType().value());
                     }

                     if (alertMessage != null) {
                        if (alertMessage.contentType == AlertContentType.URL_WITH_CONFIRMATION) {
                           FusionPktServerQuestion questionPkt = new FusionPktServerQuestion(alertMessage);
                           connection.setServerQuestion(questionPkt);
                           packetsToReturn.add(questionPkt);
                        } else {
                           FusionPktAlert alertPkt = new FusionPktAlert();
                           alertPkt.setAlertType(FusionPktDataAlert.AlertType.INFORMATION);
                           alertPkt.setContentType(AlertContentType.fromValue(alertMessage.contentType.value()));
                           alertPkt.setContent(alertMessage.content);
                           packetsToReturn.add(alertPkt);
                        }
                     }

                     return (FusionPacket[])packetsToReturn.toArray(new FusionPacket[packetsToReturn.size()]);
                  }
               }
            }
         }
      } catch (ChatRoomValidationException var19) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to join chat room - " + var19.getMessage())).toArray();
      } catch (CreateException var20) {
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (RemoteException var21) {
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (FusionException var22) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to join chat room - " + var22.message)).toArray();
      } catch (LocalException var23) {
         log.error("Unknown Ice.LocalException occurred", var23);
         return (new FusionPktInternalServerError(this.transactionId, var23, "Unable to join chat room")).toArray();
      } catch (Exception var24) {
         log.error("Caught Exception while trying to join chat room.", var24);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to join chat room - Internal Error")).toArray();
      }
   }
}
