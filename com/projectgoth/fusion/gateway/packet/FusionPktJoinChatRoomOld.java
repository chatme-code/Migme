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
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktJoinChatRoomOld extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktJoinChatRoomOld.class));

   public FusionPktJoinChatRoomOld() {
      super((short)703);
   }

   public FusionPktJoinChatRoomOld(short transactionId) {
      super((short)703, transactionId);
   }

   public FusionPktJoinChatRoomOld(FusionPacket packet) {
      super(packet);
   }

   public String getChatRoomName() {
      return this.getStringField((short)1);
   }

   public void setChatRoomName(String chatRoomName) {
      this.setField((short)1, chatRoomName);
   }

   private String concat(String[] stringArray, String seperator) {
      StringBuilder builder = new StringBuilder();
      if (stringArray != null) {
         Arrays.sort(stringArray);
         String[] arr$ = stringArray;
         int len$ = stringArray.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            String s = arr$[i$];
            builder.append(s).append(seperator);
         }
      }

      return builder.toString();
   }

   public boolean sessionRequired() {
      return true;
   }

   public String getChatRoomNameForRateLimit() {
      return this.getChatRoomName();
   }

   protected void preValidate(ConnectionI connection) throws FusionRequestException {
      if (connection.isBannedFromChatrooms()) {
         throw new FusionRequestException(FusionRequestException.ExceptionType.PREVALIDATION, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CHATROOM_BAN_ERROR_MESSAGE));
      }
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         String chatRoomName = this.getChatRoomName();
         chatRoomName = ChatRoomUtils.validateChatRoomNameForAccess(chatRoomName);
         UserPrx userPrx = connection.getUserPrx();
         if (userPrx == null) {
            throw new FusionException("You are not logged in");
         } else {
            UserDataIce userData;
            try {
               userData = userPrx.getUserData();
            } catch (ObjectNotExistException var19) {
               log.info("JoinChatRoom " + chatRoomName + " failed for user:" + connection.getUsername() + " because:" + var19.getMessage(), var19);
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
                  } catch (ObjectNotFoundException var18) {
                  }

                  if (chatRoomPrx == null) {
                     try {
                        ObjectCachePrx objectCachePrx = registryPrx.getLowestLoadedObjectCache();
                        chatRoomPrx = objectCachePrx.createChatRoomObject(chatRoomName);
                     } catch (ObjectNotFoundException var15) {
                        return (new FusionPktInternalServerError(this.transactionId, var15, "No ObjectCache found")).toArray();
                     } catch (ObjectExistsException var16) {
                        try {
                           chatRoomPrx = registryPrx.findChatRoomObject(chatRoomName);
                        } catch (ObjectNotFoundException var14) {
                           return (new FusionPktInternalServerError(this.transactionId, var16, "Unable to join chat room")).toArray();
                        }
                     } catch (FusionException var17) {
                        log.info("Attempt to join invalid chatroom : " + chatRoomName + " by user: " + userData.username);
                        throw var17;
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

                           packetsToReturn.add(new FusionPktChatRoomOld(this.transactionId, chatroomData));
                        } else {
                           packetsToReturn.add(new FusionPktOk(this.transactionId));
                        }
                     } else {
                        packetsToReturn.add(new FusionPktChatRoomOld(this.transactionId, new ChatRoomData(chatRoomPrx.getRoomData())));
                     }

                     if (connection.isMobileClientV2AndNewVersion()) {
                        FusionPktChatRoomParticipantsOld reply = new FusionPktChatRoomParticipantsOld(this.transactionId);
                        reply.setChatRoomName(chatRoomName);
                        String[] participantArray = chatRoomPrx.getParticipants(connection.getUsername());
                        String participants = this.concat(participantArray, ";");
                        if (participants != null && participants.length() > 0) {
                           reply.setParticipants(participants);
                        }

                        String administrators = this.concat(chatRoomPrx.getAdministrators(connection.getUsername()), ";");
                        if (administrators != null && administrators.length() > 0) {
                           reply.setAdministrators(administrators);
                        }

                        String mutedUsers = this.concat(connection.getUserPrx().getBlockListFromUsernames(participantArray), ";");
                        if (mutedUsers != null && mutedUsers.length() > 0) {
                           reply.setMutedParticipants(mutedUsers);
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
                           FusionPktServerQuestionOld questionPkt = new FusionPktServerQuestionOld(alertMessage);
                           connection.setServerQuestionOld(questionPkt);
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
      } catch (ChatRoomValidationException var20) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to join chat room - " + var20.getMessage())).toArray();
      } catch (CreateException var21) {
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (RemoteException var22) {
         return (new FusionPktOk(this.transactionId)).toArray();
      } catch (FusionException var23) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to join chat room - " + var23.message)).toArray();
      } catch (LocalException var24) {
         log.error("Unknown Ice.LocalException occurred", var24);
         return (new FusionPktInternalServerError(this.transactionId, var24, "Unable to join chat room")).toArray();
      } catch (Exception var25) {
         log.error("Caught Exception while trying to join chat room.", var25);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to join chat room - Internal Error")).toArray();
      }
   }
}
