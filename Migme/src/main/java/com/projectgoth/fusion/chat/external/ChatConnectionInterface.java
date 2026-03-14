package com.projectgoth.fusion.chat.external;

import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import java.util.List;

public interface ChatConnectionInterface {
   String getUsername();

   ImType getImType();

   void signIn(String var1, String var2) throws Exception;

   void signOut();

   boolean isSignedIn();

   boolean isConnected();

   void sendMessage(String var1, String var2) throws Exception;

   void addContact(String var1) throws Exception;

   void removeContact(String var1) throws Exception;

   void setAvatar(String var1) throws Exception;

   void setStatus(PresenceType var1, String var2) throws Exception;

   String inviteToConference(String var1, String var2) throws Exception;

   void leaveConference(String var1) throws Exception;

   List<String> getConferenceParticipants(String var1) throws Exception;
}
