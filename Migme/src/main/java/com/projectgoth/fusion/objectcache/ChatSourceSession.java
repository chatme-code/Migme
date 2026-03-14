package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.common.IcePrxFinder;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.fdl.enums.ClientType;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.ConnectionPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;
import com.projectgoth.fusion.slice.UserPrx;

public interface ChatSourceSession {
   void sendMessageBackToUserAsEmote(MessageData var1, String var2) throws FusionException;

   void putMessage(MessageDataIce var1) throws FusionException;

   void logEmoteData(ChatRoomEmoteLogData var1);

   String getSessionID();

   ClientType getDeviceType();

   short getClientVersion();

   int getUserID();

   String getUsername();

   String getRemoteAddress();

   String getMobileDevice();

   String getUserAgent();

   ConnectionPrx getConnectionProxy();

   UserPrx findUserPrx(String var1) throws FusionException;

   SessionPrx findSessionPrx(String var1);

   ChatRoomPrx findChatRoomPrx(String var1) throws FusionException;

   GroupChatPrx findGroupChatPrx(String var1) throws FusionException;

   IcePrxFinder getIcePrxFinder();

   UserDataIce getUserDataIce();

   double getBalance();

   UserData.TypeEnum getUserType();
}
