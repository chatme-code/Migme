package com.projectgoth.fusion.objectcache;

import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.MessageDataIce;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserDataIce;

public interface ChatSourceUser {
   String getUsername();

   void putMessage(MessageDataIce var1) throws FusionException;

   UserDataIce getUserData();

   SessionPrx getSessionPrx(String var1);
}
