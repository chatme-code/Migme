package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _ConnectionWSOperations extends _ConnectionOperations {
   void accessed(Current var1);

   void addRemoteChildConnectionWS(String var1, ConnectionWSPrx var2, Current var3);

   void removeRemoteChildConnectionWS(String var1, ConnectionWSPrx var2, Current var3);
}
