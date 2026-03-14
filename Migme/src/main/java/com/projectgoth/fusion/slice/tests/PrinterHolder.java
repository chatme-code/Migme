package com.projectgoth.fusion.slice.tests;

import Ice.Object;
import IceInternal.Ex;

public final class PrinterHolder {
   public Printer value;

   public PrinterHolder() {
   }

   public PrinterHolder(Printer value) {
      this.value = value;
   }

   public PrinterHolder.Patcher getPatcher() {
      return new PrinterHolder.Patcher();
   }

   public class Patcher implements IceInternal.Patcher {
      public void patch(Object v) {
         try {
            PrinterHolder.this.value = (Printer)v;
         } catch (ClassCastException var3) {
            Ex.throwUOE(this.type(), v.ice_id());
         }

      }

      public String type() {
         return "::com::projectgoth::fusion::slice::tests::Printer";
      }
   }
}
