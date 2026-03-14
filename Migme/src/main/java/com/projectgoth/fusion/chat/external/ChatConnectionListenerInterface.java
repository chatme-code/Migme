package com.projectgoth.fusion.chat.external;

import com.projectgoth.fusion.fdl.enums.PresenceType;

public interface ChatConnectionListenerInterface {
   void onSignInSuccess(ChatConnectionInterface var1);

   void onSignInFailed(ChatConnectionInterface var1, String var2);

   void onDisconnected(ChatConnectionInterface var1, String var2);

   void onMessageReceived(ChatConnectionInterface var1, String var2, String var3, String var4);

   void onMessageFailed(ChatConnectionInterface var1, String var2, String var3, String var4, String var5);

   void onContactStatusChanged(ChatConnectionInterface var1, String var2, PresenceType var3);

   void onContactDetail(ChatConnectionInterface var1, String var2, String var3);

   void onContactRequest(ChatConnectionInterface var1, String var2, String var3);

   void onConferenceCreated(ChatConnectionInterface var1, String var2, String var3);

   void onUserJoinedConference(ChatConnectionInterface var1, String var2, String var3);

   void onUserLeftConference(ChatConnectionInterface var1, String var2, String var3);

   void onConferenceInvitationFailed(ChatConnectionInterface var1, String var2, String var3, String var4);
}
