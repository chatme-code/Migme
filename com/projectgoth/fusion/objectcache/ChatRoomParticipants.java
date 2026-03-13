package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.FusionExceptionWithErrorCauseCode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class ChatRoomParticipants extends ChatParticipants {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatRoomParticipants.class));
   private final Map<String, ChatRoomParticipant> participants = new ConcurrentHashMap();
   private final String chatRoomName;

   public ChatRoomParticipants(String chatRoomName) {
      this.chatRoomName = chatRoomName;
   }

   private String getRoomName() {
      return this.chatRoomName;
   }

   public ChatRoomParticipant get(String username) {
      return (ChatRoomParticipant)this.participants.get(username);
   }

   public Set<String> getAllNames() {
      return this.participants.keySet();
   }

   public Collection<ChatRoomParticipant> getAll() {
      return this.participants.values();
   }

   public ChatRoomParticipant remove(String username) {
      return (ChatRoomParticipant)this.participants.remove(username);
   }

   public boolean isParticipant(String username) {
      return this.participants.containsKey(username);
   }

   public ChatRoomParticipant verifyYouAreParticipant(String username) throws FusionException {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(username);
      if (participant == null) {
         throw new FusionException("You are not in the " + this.getRoomName() + " chat room");
      } else {
         return participant;
      }
   }

   public ChatRoomParticipant verifyIsParticipant(String username) throws FusionException {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(username);
      if (participant == null) {
         throw new FusionException(username + " is no longer in the chat room " + this.getRoomName());
      } else {
         return participant;
      }
   }

   public ChatRoomParticipant verifyYouAreParticipant(String username, ErrorCause errorCause) throws FusionExceptionWithErrorCauseCode {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(username);
      if (participant == null) {
         throw new FusionExceptionWithErrorCauseCode("You are not in the " + this.getRoomName() + " chat room", errorCause.getCode());
      } else {
         return participant;
      }
   }

   public ChatRoomParticipant verifyIsParticipant(String username, ErrorCause error) throws FusionExceptionWithErrorCauseCode {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(username);
      if (participant == null) {
         throw new FusionExceptionWithErrorCauseCode(username + " is no longer in the chat room " + this.getRoomName() + " chat room", error.getCode());
      } else {
         return participant;
      }
   }

   public ChatRoomParticipant verifyHasNotLeftParticipant(String username) throws FusionException {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(username);
      if (participant == null) {
         throw new FusionException(username + " has left the chat");
      } else {
         return participant;
      }
   }

   public String[] getAdministrators(String requestingUsername) {
      List<String> list = new ArrayList();
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
         if (!participant.getUsername().equals(requestingUsername) && participant.hasAdminOrModeratorRights() && !participant.isHiddenAdmin()) {
            list.add(participant.getUsername());
         }
      }

      return (String[])list.toArray(new String[list.size()]);
   }

   public int size() {
      return this.participants.size();
   }

   public boolean isEmpty() {
      return this.participants.isEmpty();
   }

   public boolean isAdministrator(String requestingUsername) {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(requestingUsername);
      if (participant == null) {
         return false;
      } else {
         return participant.hasAdminOrModeratorRights() && !participant.isHiddenAdmin();
      }
   }

   public String[] getAllParticipants(String requestingUsername) {
      List<String> list = new ArrayList();
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
         if (!participant.getUsername().equals(requestingUsername)) {
            list.add(participant.getUsername());
         }
      }

      return (String[])list.toArray(new String[list.size()]);
   }

   public String[] getAllParticipantsExceptHiddenAdmins(String requestingUsername) {
      List<String> list = new ArrayList();
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
         if (!participant.getUsername().equals(requestingUsername) && !participant.isHiddenAdmin()) {
            list.add(participant.getUsername());
         }
      }

      return (String[])list.toArray(new String[list.size()]);
   }

   public HashMap<String, List<String>> getIpToUserMap() {
      HashMap<String, List<String>> ipToUsernames = new HashMap();
      Iterator i$ = this.participants.entrySet().iterator();

      while(i$.hasNext()) {
         Entry<String, ChatRoomParticipant> entry = (Entry)i$.next();
         String username = (String)entry.getKey();
         ChatRoomParticipant participant = (ChatRoomParticipant)entry.getValue();
         String ip = participant.getIPAddress();
         if (!ipToUsernames.containsKey(ip)) {
            List<String> list = new ArrayList();
            list.add(username);
            ipToUsernames.put(ip, list);
         } else {
            ((List)ipToUsernames.get(ip)).add(username);
         }
      }

      return ipToUsernames;
   }

   public ChatRoomParticipant add(ChatRoomParticipant participant) {
      return (ChatRoomParticipant)this.participants.put(participant.getUsername(), participant);
   }

   public void updateLastTimeMessageSent(String username) {
      ChatRoomParticipant participant = (ChatRoomParticipant)this.participants.get(username);
      if (participant != null) {
         participant.updateLastTimeMessageSent();
      }

   }

   public void notifyUserLeftChatRoom(String username) {
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
         if (!participant.getUsername().equals(username)) {
            participant.notifyUserLeftChatRoom_async(username);
         }
      }

   }

   public void notifyUserJoinedChatRoom(boolean isBanned, String username) {
      boolean isAdministrator = this.isAdministrator(username);
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatRoomParticipant participant = (ChatRoomParticipant)i$.next();
         if (!participant.getUsername().equals(username)) {
            participant.notifyUserJoinedChatRoom_async(username, isAdministrator, isBanned);
         }
      }

   }
}
