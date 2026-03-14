package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _BotServiceAdminOperations {
   int ping(Current var1);

   BotServiceStats getStats(Current var1) throws FusionException;
}
