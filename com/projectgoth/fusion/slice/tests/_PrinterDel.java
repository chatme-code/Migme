package com.projectgoth.fusion.slice.tests;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _PrinterDel extends _ObjectDel {
   void printString(String var1, Map<String, String> var2) throws LocalExceptionWrapper;

   void circular(String var1, int var2, Map<String, String> var3) throws LocalExceptionWrapper;
}
