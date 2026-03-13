package com.projectgoth.fusion.slice;

import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _ConnectionWSDel extends _ConnectionDel {
   void accessed(Map<String, String> var1) throws LocalExceptionWrapper;

   void addRemoteChildConnectionWS(String var1, ConnectionWSPrx var2, Map<String, String> var3) throws LocalExceptionWrapper;

   void removeRemoteChildConnectionWS(String var1, ConnectionWSPrx var2, Map<String, String> var3) throws LocalExceptionWrapper;
}
