package com.projectgoth.fusion.objectcache;

import Ice.ObjectNotExistException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.slice.MessageDataIce;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

public class ChatGroupParticipants extends ChatParticipants {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(ChatGroup.class));
   private ConcurrentHashMap<String, ChatGroupParticipant> participants = new ConcurrentHashMap();
   private String id;

   ChatGroupParticipants(String id) {
      this.id = id;
   }

   public int getParticipantLimit() {
      return SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.GroupChat.GROUP_CHAT_PARTICIPANT_LIMIT);
   }

   public void add(String username, ChatGroupParticipant participant) {
      this.participants.put(username, participant);
   }

   public ChatGroupParticipant get(String username) {
      return (ChatGroupParticipant)this.participants.get(username);
   }

   public boolean isParticipant(String username) {
      return this.participants.containsKey(username);
   }

   public String[] getUserNames() {
      ArrayList<String> usernames = new ArrayList();
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant p = (ChatGroupParticipant)i$.next();
         usernames.add(p.getUsername());
      }

      return (String[])usernames.toArray(new String[usernames.size()]);
   }

   public String getList(String separator) {
      StringBuffer sb = new StringBuffer();
      boolean firstParticipant = true;

      ChatGroupParticipant p;
      for(Iterator i$ = this.participants.values().iterator(); i$.hasNext(); sb.append(p.getUsername())) {
         p = (ChatGroupParticipant)i$.next();
         if (!firstParticipant) {
            sb.append(separator);
         } else {
            firstParticipant = false;
         }
      }

      return sb.toString();
   }

   public List<ChatGroupParticipant> getParticipants() {
      List<ChatGroupParticipant> currentParticipants = new ArrayList(this.participants.size());
      currentParticipants.addAll(this.participants.values());
      return currentParticipants;
   }

   public String[] getParticipants(String excludeUsername) {
      List<String> participantUsernames = new ArrayList();
      Iterator i$ = this.participants.keySet().iterator();

      while(i$.hasNext()) {
         String username = (String)i$.next();
         if (!username.equalsIgnoreCase(excludeUsername)) {
            participantUsernames.add(username);
         }
      }

      return (String[])participantUsernames.toArray(new String[participantUsernames.size()]);
   }

   public int[] getUserIDs() {
      List<Integer> participantUserIDs = new ArrayList();
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant p = (ChatGroupParticipant)i$.next();
         participantUserIDs.add(p.getUserID());
      }

      int[] results = new int[participantUserIDs.size()];

      for(int i = 0; i < participantUserIDs.size(); ++i) {
         results[i] = (Integer)participantUserIDs.get(i);
      }

      return results;
   }

   public int size() {
      return this.participants.size();
   }

   public boolean supportsBinaryMessage(String usernameToExclude) {
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant p = (ChatGroupParticipant)i$.next();

         try {
            if (!p.getUsername().equals(usernameToExclude) && p.supportsBinaryMessage()) {
               return true;
            }
         } catch (Exception var5) {
         }
      }

      return false;
   }

   public void putFileReceived(MessageDataIce messageIce) {
      List<ChatGroupParticipant> removeParticipants = new ArrayList();
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant participant = (ChatGroupParticipant)i$.next();
         if (!messageIce.source.equals(participant.getUsername())) {
            try {
               participant.putFileReceived(messageIce);
            } catch (Exception var6) {
               removeParticipants.add(participant);
            }
         }
      }

      this.remove((List)removeParticipants);
   }

   public ChatGroupParticipant remove(String username) {
      ChatGroupParticipant participant = (ChatGroupParticipant)this.participants.remove(username);
      if (participant == null) {
         return null;
      } else {
         try {
            participant.leavingGroupChat();
         } catch (Exception var4) {
         }

         return participant;
      }
   }

   public void removeAll() {
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant participant = (ChatGroupParticipant)i$.next();

         try {
            participant.leavingGroupChat();
         } catch (Exception var4) {
         }
      }

      this.participants.clear();
   }

   public void removeAllParticipants() {
      List<String> userNames = new LinkedList(this.participants.keySet());
      Iterator i$ = userNames.iterator();

      while(i$.hasNext()) {
         String userName = (String)i$.next();
         this.remove(userName);
      }

   }

   private void remove(List<ChatGroupParticipant> removeParticipants) {
      if (removeParticipants != null) {
         Iterator i$ = removeParticipants.iterator();

         while(i$.hasNext()) {
            ChatGroupParticipant p = (ChatGroupParticipant)i$.next();
            this.participants.remove(p.getUsername());
         }

      }
   }

   public void notifyUserJoined(ChatGroupParticipant user) {
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant participant = (ChatGroupParticipant)i$.next();
         if (!participant.getUsername().equals(user.getUsername())) {
            if (!participant.hasUserProxy()) {
               this.debugLogNonNotification(user, participant, "joined group chat", "participant is offline (and has been since group chat was created");
            } else {
               try {
                  participant.notifyUserJoinedGroupChat(this.id, user.getUsername());
               } catch (ObjectNotExistException var5) {
                  this.debugLogNonNotification(user, participant, "joined group chat", "participant is offline");
               }
            }
         }
      }

   }

   public void notifyUserLeft(ChatGroupParticipant user) {
      Iterator i$ = this.participants.values().iterator();

      while(i$.hasNext()) {
         ChatGroupParticipant participant = (ChatGroupParticipant)i$.next();
         if (!participant.getUsername().equals(user.getUsername())) {
            if (!participant.hasUserProxy()) {
               this.debugLogNonNotification(user, participant, "left group chat", "participant is offline (and has been since group chat was created");
            } else {
               try {
                  participant.notifyUserLeftGroupChat(this.id, user.getUsername());
               } catch (ObjectNotExistException var5) {
                  this.debugLogNonNotification(user, participant, "left group chat", "participant is offline");
               }
            }
         }
      }

   }

   private void debugLogNonNotification(ChatGroupParticipant user, ChatGroupParticipant participant, String event, String reason) {
      if (log.isDebugEnabled()) {
         log.debug("Not notifying participant " + participant.getUsername() + "that user " + user.getUsername() + " has " + event + " as " + reason);
      }
   }
}
