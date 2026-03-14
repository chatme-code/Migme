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

public final class EventStorePrxHelper extends ObjectPrxHelperBase implements EventStorePrx {
   public void deleteUserEvents(String username) throws FusionException {
      this.deleteUserEvents(username, (Map)null, false);
   }

   public void deleteUserEvents(String username, Map<String, String> __ctx) throws FusionException {
      this.deleteUserEvents(username, __ctx, true);
   }

   private void deleteUserEvents(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("deleteUserEvents");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            __del.deleteUserEvents(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username) throws FusionException {
      return this.getPublishingPrivacyMask(username, (Map)null, false);
   }

   public EventPrivacySettingIce getPublishingPrivacyMask(String username, Map<String, String> __ctx) throws FusionException {
      return this.getPublishingPrivacyMask(username, __ctx, true);
   }

   private EventPrivacySettingIce getPublishingPrivacyMask(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getPublishingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            return __del.getPublishingPrivacyMask(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username) throws FusionException {
      return this.getReceivingPrivacyMask(username, (Map)null, false);
   }

   public EventPrivacySettingIce getReceivingPrivacyMask(String username, Map<String, String> __ctx) throws FusionException {
      return this.getReceivingPrivacyMask(username, __ctx, true);
   }

   private EventPrivacySettingIce getReceivingPrivacyMask(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getReceivingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            return __del.getReceivingPrivacyMask(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserEventIce[] getUserEventsForUser(String username) throws FusionException {
      return this.getUserEventsForUser(username, (Map)null, false);
   }

   public UserEventIce[] getUserEventsForUser(String username, Map<String, String> __ctx) throws FusionException {
      return this.getUserEventsForUser(username, __ctx, true);
   }

   private UserEventIce[] getUserEventsForUser(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserEventsForUser");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            return __del.getUserEventsForUser(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username) throws FusionException {
      return this.getUserEventsGeneratedByUser(username, (Map)null, false);
   }

   public UserEventIce[] getUserEventsGeneratedByUser(String username, Map<String, String> __ctx) throws FusionException {
      return this.getUserEventsGeneratedByUser(username, __ctx, true);
   }

   private UserEventIce[] getUserEventsGeneratedByUser(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserEventsGeneratedByUser");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            return __del.getUserEventsGeneratedByUser(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask) throws FusionException {
      this.setPublishingPrivacyMask(username, mask, (Map)null, false);
   }

   public void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx) throws FusionException {
      this.setPublishingPrivacyMask(username, mask, __ctx, true);
   }

   private void setPublishingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setPublishingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            __del.setPublishingPrivacyMask(username, mask, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask) throws FusionException {
      this.setReceivingPrivacyMask(username, mask, (Map)null, false);
   }

   public void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx) throws FusionException {
      this.setReceivingPrivacyMask(username, mask, __ctx, true);
   }

   private void setReceivingPrivacyMask(String username, EventPrivacySettingIce mask, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setReceivingPrivacyMask");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            __del.setReceivingPrivacyMask(username, mask, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void storeGeneratorEvent(String username, UserEventIce event) throws FusionException {
      this.storeGeneratorEvent(username, event, (Map)null, false);
   }

   public void storeGeneratorEvent(String username, UserEventIce event, Map<String, String> __ctx) throws FusionException {
      this.storeGeneratorEvent(username, event, __ctx, true);
   }

   private void storeGeneratorEvent(String username, UserEventIce event, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("storeGeneratorEvent");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            __del.storeGeneratorEvent(username, event, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void storeUserEvent(String username, UserEventIce event) throws FusionException {
      this.storeUserEvent(username, event, (Map)null, false);
   }

   public void storeUserEvent(String username, UserEventIce event, Map<String, String> __ctx) throws FusionException {
      this.storeUserEvent(username, event, __ctx, true);
   }

   private void storeUserEvent(String username, UserEventIce event, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("storeUserEvent");
            __delBase = this.__getDelegate(false);
            _EventStoreDel __del = (_EventStoreDel)__delBase;
            __del.storeUserEvent(username, event, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static EventStorePrx checkedCast(ObjectPrx __obj) {
      EventStorePrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStorePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventStore")) {
               EventStorePrxHelper __h = new EventStorePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventStorePrx)__d;
   }

   public static EventStorePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      EventStorePrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStorePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::EventStore", __ctx)) {
               EventStorePrxHelper __h = new EventStorePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (EventStorePrx)__d;
   }

   public static EventStorePrx checkedCast(ObjectPrx __obj, String __facet) {
      EventStorePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventStore")) {
               EventStorePrxHelper __h = new EventStorePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static EventStorePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      EventStorePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::EventStore", __ctx)) {
               EventStorePrxHelper __h = new EventStorePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static EventStorePrx uncheckedCast(ObjectPrx __obj) {
      EventStorePrx __d = null;
      if (__obj != null) {
         try {
            __d = (EventStorePrx)__obj;
         } catch (ClassCastException var4) {
            EventStorePrxHelper __h = new EventStorePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (EventStorePrx)__d;
   }

   public static EventStorePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      EventStorePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         EventStorePrxHelper __h = new EventStorePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _EventStoreDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _EventStoreDelD();
   }

   public static void __write(BasicStream __os, EventStorePrx v) {
      __os.writeProxy(v);
   }

   public static EventStorePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         EventStorePrxHelper result = new EventStorePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
