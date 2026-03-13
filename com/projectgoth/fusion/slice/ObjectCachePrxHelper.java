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

public final class ObjectCachePrxHelper extends ObjectPrxHelperBase implements ObjectCachePrx {
   public ChatRoomPrx createChatRoomObject(String name) throws FusionException, ObjectExistsException {
      return this.createChatRoomObject(name, (Map)null, false);
   }

   public ChatRoomPrx createChatRoomObject(String name, Map<String, String> __ctx) throws FusionException, ObjectExistsException {
      return this.createChatRoomObject(name, __ctx, true);
   }

   private ChatRoomPrx createChatRoomObject(String name, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException, ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("createChatRoomObject");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            return __del.createChatRoomObject(name, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public GroupChatPrx createGroupChatObject(String id, String creator, String privateChatPartner, String[] otherPartyList) throws FusionException, ObjectExistsException {
      return this.createGroupChatObject(id, creator, privateChatPartner, otherPartyList, (Map)null, false);
   }

   public GroupChatPrx createGroupChatObject(String id, String creator, String privateChatPartner, String[] otherPartyList, Map<String, String> __ctx) throws FusionException, ObjectExistsException {
      return this.createGroupChatObject(id, creator, privateChatPartner, otherPartyList, __ctx, true);
   }

   private GroupChatPrx createGroupChatObject(String id, String creator, String privateChatPartner, String[] otherPartyList, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException, ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("createGroupChatObject");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            return __del.createGroupChatObject(id, creator, privateChatPartner, otherPartyList, __ctx);
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserPrx createUserObject(String username) throws FusionException, ObjectExistsException {
      return this.createUserObject(username, (Map)null, false);
   }

   public UserPrx createUserObject(String username, Map<String, String> __ctx) throws FusionException, ObjectExistsException {
      return this.createUserObject(username, __ctx, true);
   }

   private UserPrx createUserObject(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException, ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("createUserObject");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            return __del.createUserObject(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserPrx createUserObjectNonAsync(String username) throws FusionException, ObjectExistsException {
      return this.createUserObjectNonAsync(username, (Map)null, false);
   }

   public UserPrx createUserObjectNonAsync(String username, Map<String, String> __ctx) throws FusionException, ObjectExistsException {
      return this.createUserObjectNonAsync(username, __ctx, true);
   }

   private UserPrx createUserObjectNonAsync(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException, ObjectExistsException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("createUserObjectNonAsync");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            return __del.createUserObjectNonAsync(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public GroupChatPrx[] getAllGroupChats() throws FusionException {
      return this.getAllGroupChats((Map)null, false);
   }

   public GroupChatPrx[] getAllGroupChats(Map<String, String> __ctx) throws FusionException {
      return this.getAllGroupChats(__ctx, true);
   }

   private GroupChatPrx[] getAllGroupChats(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getAllGroupChats");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            return __del.getAllGroupChats(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public MessageSwitchboardPrx getMessageSwitchboard() throws FusionException {
      return this.getMessageSwitchboard((Map)null, false);
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> __ctx) throws FusionException {
      return this.getMessageSwitchboard(__ctx, true);
   }

   private MessageSwitchboardPrx getMessageSwitchboard(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getMessageSwitchboard");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            return __del.getMessageSwitchboard(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean getMessageSwitchboard_async(AMI_ObjectCache_getMessageSwitchboard __cb) {
      return this.getMessageSwitchboard_async(__cb, (Map)null, false);
   }

   public boolean getMessageSwitchboard_async(AMI_ObjectCache_getMessageSwitchboard __cb, Map<String, String> __ctx) {
      return this.getMessageSwitchboard_async(__cb, __ctx, true);
   }

   private boolean getMessageSwitchboard_async(AMI_ObjectCache_getMessageSwitchboard __cb, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      return __cb.__invoke(this, __cb, __ctx);
   }

   public void purgeGroupChatObject(String id) {
      this.purgeGroupChatObject(id, (Map)null, false);
   }

   public void purgeGroupChatObject(String id, Map<String, String> __ctx) {
      this.purgeGroupChatObject(id, __ctx, true);
   }

   private void purgeGroupChatObject(String id, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            __del.purgeGroupChatObject(id, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void purgeUserObject(String username) {
      this.purgeUserObject(username, (Map)null, false);
   }

   public void purgeUserObject(String username, Map<String, String> __ctx) {
      this.purgeUserObject(username, __ctx, true);
   }

   private void purgeUserObject(String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            __del.purgeUserObject(username, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout) throws FusionException {
      this.sendAlertMessageToAllUsers(message, title, timeout, (Map)null, false);
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout, Map<String, String> __ctx) throws FusionException {
      this.sendAlertMessageToAllUsers(message, title, timeout, __ctx, true);
   }

   private void sendAlertMessageToAllUsers(String message, String title, short timeout, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendAlertMessageToAllUsers");
            __delBase = this.__getDelegate(false);
            _ObjectCacheDel __del = (_ObjectCacheDel)__delBase;
            __del.sendAlertMessageToAllUsers(message, title, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static ObjectCachePrx checkedCast(ObjectPrx __obj) {
      ObjectCachePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCachePrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ObjectCache")) {
               ObjectCachePrxHelper __h = new ObjectCachePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ObjectCachePrx)__d;
   }

   public static ObjectCachePrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      ObjectCachePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCachePrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::ObjectCache", __ctx)) {
               ObjectCachePrxHelper __h = new ObjectCachePrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (ObjectCachePrx)__d;
   }

   public static ObjectCachePrx checkedCast(ObjectPrx __obj, String __facet) {
      ObjectCachePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ObjectCache")) {
               ObjectCachePrxHelper __h = new ObjectCachePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static ObjectCachePrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      ObjectCachePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::ObjectCache", __ctx)) {
               ObjectCachePrxHelper __h = new ObjectCachePrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static ObjectCachePrx uncheckedCast(ObjectPrx __obj) {
      ObjectCachePrx __d = null;
      if (__obj != null) {
         try {
            __d = (ObjectCachePrx)__obj;
         } catch (ClassCastException var4) {
            ObjectCachePrxHelper __h = new ObjectCachePrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (ObjectCachePrx)__d;
   }

   public static ObjectCachePrx uncheckedCast(ObjectPrx __obj, String __facet) {
      ObjectCachePrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         ObjectCachePrxHelper __h = new ObjectCachePrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _ObjectCacheDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _ObjectCacheDelD();
   }

   public static void __write(BasicStream __os, ObjectCachePrx v) {
      __os.writeProxy(v);
   }

   public static ObjectCachePrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         ObjectCachePrxHelper result = new ObjectCachePrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
