package com.projectgoth.fusion.slice;

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

public final class MessageLoggerPrxHelper extends ObjectPrxHelperBase implements MessageLoggerPrx {
   public void logMessage(int type, int sourceCountryID, String source, String destination, int numRecipients, String messageText) {
      this.logMessage(type, sourceCountryID, source, destination, numRecipients, messageText, (Map)null, false);
   }

   public void logMessage(int type, int sourceCountryID, String source, String destination, int numRecipients, String messageText, Map<String, String> __ctx) {
      this.logMessage(type, sourceCountryID, source, destination, numRecipients, messageText, __ctx, true);
   }

   private void logMessage(int type, int sourceCountryID, String source, String destination, int numRecipients, String messageText, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _MessageLoggerDel __del = (_MessageLoggerDel)__delBase;
            __del.logMessage(type, sourceCountryID, source, destination, numRecipients, messageText, __ctx);
            return;
         } catch (LocalExceptionWrapper var12) {
            this.__handleExceptionWrapper(__delBase, var12, (OutgoingAsync)null);
         } catch (LocalException var13) {
            __cnt = this.__handleException(__delBase, var13, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static MessageLoggerPrx checkedCast(ObjectPrx __obj) {
      MessageLoggerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageLoggerPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageLogger")) {
               MessageLoggerPrxHelper __h = new MessageLoggerPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageLoggerPrx)__d;
   }

   public static MessageLoggerPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      MessageLoggerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageLoggerPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::MessageLogger", __ctx)) {
               MessageLoggerPrxHelper __h = new MessageLoggerPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (MessageLoggerPrx)__d;
   }

   public static MessageLoggerPrx checkedCast(ObjectPrx __obj, String __facet) {
      MessageLoggerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageLogger")) {
               MessageLoggerPrxHelper __h = new MessageLoggerPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static MessageLoggerPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      MessageLoggerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::MessageLogger", __ctx)) {
               MessageLoggerPrxHelper __h = new MessageLoggerPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static MessageLoggerPrx uncheckedCast(ObjectPrx __obj) {
      MessageLoggerPrx __d = null;
      if (__obj != null) {
         try {
            __d = (MessageLoggerPrx)__obj;
         } catch (ClassCastException var4) {
            MessageLoggerPrxHelper __h = new MessageLoggerPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (MessageLoggerPrx)__d;
   }

   public static MessageLoggerPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      MessageLoggerPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         MessageLoggerPrxHelper __h = new MessageLoggerPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _MessageLoggerDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _MessageLoggerDelD();
   }

   public static void __write(BasicStream __os, MessageLoggerPrx v) {
      __os.writeProxy(v);
   }

   public static MessageLoggerPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         MessageLoggerPrxHelper result = new MessageLoggerPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
