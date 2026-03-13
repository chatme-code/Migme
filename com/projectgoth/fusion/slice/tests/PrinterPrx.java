package com.projectgoth.fusion.slice.tests;

import Ice.ObjectPrx;
import java.util.Map;

public interface PrinterPrx extends ObjectPrx {
   void printString(String var1);

   void printString(String var1, Map<String, String> var2);

   void circular(String var1, int var2);

   void circular(String var1, int var2, Map<String, String> var3);
}
