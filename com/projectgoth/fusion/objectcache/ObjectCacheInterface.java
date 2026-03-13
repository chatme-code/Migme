package com.projectgoth.fusion.objectcache;

import Ice.Object;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import com.projectgoth.fusion.slice.UserPrx;

public interface ObjectCacheInterface extends Object {
   void getStats(ObjectCacheStats var1);

   String[] getUsernames();

   int getUserCount();

   void setLoadWeightage(int var1);

   int getLoadWeightage();

   void purgeUserObject(String var1);

   UserPrx createUserObjectNonAsync(String var1) throws FusionException;

   ChatRoomPrx createChatRoomObject(String var1) throws FusionException;

   GroupChatPrx createGroupChatObject(String var1, String var2, String var3, String[] var4) throws FusionException;
}
