package com.projectgoth.fusion.slice.tests;

import Ice.FacetNotExistException;
import Ice.LocalException;
import Ice.ObjectPrx;
import Ice.ObjectPrxHelperBase;
import Ice._ObjectDel;
import Ice._ObjectDelD;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.OutgoingAsync;
import java.util.Map;

public final class PrinterPrxHelper extends ObjectPrxHelperBase implements PrinterPrx {
   public void circular(String s, int level) {
      this.circular(s, level, (Map)null, false);
   }

   public void circular(String s, int level, Map<String, String> __ctx) {
      this.circular(s, level, __ctx, true);
   }

   private void circular(String s, int level, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _PrinterDel __del = (_PrinterDel)__delBase;
            __del.circular(s, level, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void printString(String s) {
      this.printString(s, (Map)null, false);
   }

   public void printString(String s, Map<String, String> __ctx) {
      this.printString(s, __ctx, true);
   }

   private void printString(String s, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _PrinterDel __del = (_PrinterDel)__delBase;
            __del.printString(s, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static PrinterPrx checkedCast(ObjectPrx __obj) {
      PrinterPrx __d = null;
      if (__obj != null) {
         try {
            __d = (PrinterPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::tests::Printer")) {
               PrinterPrxHelper __h = new PrinterPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (PrinterPrx)__d;
   }

   public static PrinterPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      PrinterPrx __d = null;
      if (__obj != null) {
         try {
            __d = (PrinterPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::tests::Printer", __ctx)) {
               PrinterPrxHelper __h = new PrinterPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (PrinterPrx)__d;
   }

   public static PrinterPrx checkedCast(ObjectPrx __obj, String __facet) {
      PrinterPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::tests::Printer")) {
               PrinterPrxHelper __h = new PrinterPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static PrinterPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      PrinterPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::tests::Printer", __ctx)) {
               PrinterPrxHelper __h = new PrinterPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static PrinterPrx uncheckedCast(ObjectPrx __obj) {
      PrinterPrx __d = null;
      if (__obj != null) {
         try {
            __d = (PrinterPrx)__obj;
         } catch (ClassCastException var4) {
            PrinterPrxHelper __h = new PrinterPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (PrinterPrx)__d;
   }

   public static PrinterPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      PrinterPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         PrinterPrxHelper __h = new PrinterPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _PrinterDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _PrinterDelD();
   }

   public static void __write(BasicStream __os, PrinterPrx v) {
      __os.writeProxy(v);
   }

   public static PrinterPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         PrinterPrxHelper result = new PrinterPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
