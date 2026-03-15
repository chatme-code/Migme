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

public final class SessionPrxHelper extends ObjectPrxHelperBase implements SessionPrx {
   public void chatroomJoined(ChatRoomPrx roomProxy, String name) {
      this.chatroomJoined(roomProxy, name, (Map)null, false);
   }

   public void chatroomJoined(ChatRoomPrx roomProxy, String name, Map<String, String> __ctx) {
      this.chatroomJoined(roomProxy, name, __ctx, true);
   }

   private void chatroomJoined(ChatRoomPrx roomProxy, String name, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.chatroomJoined(roomProxy, name, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void endSession() throws FusionException {
      this.endSession((Map)null, false);
   }

   public void endSession(Map<String, String> __ctx) throws FusionException {
      this.endSession(__ctx, true);
   }

   private void endSession(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("endSession");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.endSession(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void endSessionOneWay() {
      this.endSessionOneWay((Map)null, false);
   }

   public void endSessionOneWay(Map<String, String> __ctx) {
      this.endSessionOneWay(__ctx, true);
   }

   private void endSessionOneWay(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.endSessionOneWay(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public GroupChatPrx findGroupChatObject(String groupChatID) throws FusionException {
      return this.findGroupChatObject(groupChatID, (Map)null, false);
   }

   public GroupChatPrx findGroupChatObject(String groupChatID, Map<String, String> __ctx) throws FusionException {
      return this.findGroupChatObject(groupChatID, __ctx, true);
   }

   private GroupChatPrx findGroupChatObject(String groupChatID, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("findGroupChatObject");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.findGroupChatObject(groupChatID, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void friendInvitedByPhoneNumber() {
      this.friendInvitedByPhoneNumber((Map)null, false);
   }

   public void friendInvitedByPhoneNumber(Map<String, String> __ctx) {
      this.friendInvitedByPhoneNumber(__ctx, true);
   }

   private void friendInvitedByPhoneNumber(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.friendInvitedByPhoneNumber(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void friendInvitedByUsername() {
      this.friendInvitedByUsername((Map)null, false);
   }

   public void friendInvitedByUsername(Map<String, String> __ctx) {
      this.friendInvitedByUsername(__ctx, true);
   }

   private void friendInvitedByUsername(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.friendInvitedByUsername(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getChatListVersion() throws FusionException {
      return this.getChatListVersion((Map)null, false);
   }

   public int getChatListVersion(Map<String, String> __ctx) throws FusionException {
      return this.getChatListVersion(__ctx, true);
   }

   private int getChatListVersion(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getChatListVersion");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getChatListVersion(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public short getClientVersionIce() {
      return this.getClientVersionIce((Map)null, false);
   }

   public short getClientVersionIce(Map<String, String> __ctx) {
      return this.getClientVersionIce(__ctx, true);
   }

   private short getClientVersionIce(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getClientVersionIce");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getClientVersionIce(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public int getDeviceTypeAsInt() {
      return this.getDeviceTypeAsInt((Map)null, false);
   }

   public int getDeviceTypeAsInt(Map<String, String> __ctx) {
      return this.getDeviceTypeAsInt(__ctx, true);
   }

   private int getDeviceTypeAsInt(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getDeviceTypeAsInt");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getDeviceTypeAsInt(__ctx);
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
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getMessageSwitchboard(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getMobileDeviceIce() {
      return this.getMobileDeviceIce((Map)null, false);
   }

   public String getMobileDeviceIce(Map<String, String> __ctx) {
      return this.getMobileDeviceIce(__ctx, true);
   }

   private String getMobileDeviceIce(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getMobileDeviceIce");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getMobileDeviceIce(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getParentUsername() throws FusionException {
      return this.getParentUsername((Map)null, false);
   }

   public String getParentUsername(Map<String, String> __ctx) throws FusionException {
      return this.getParentUsername(__ctx, true);
   }

   private String getParentUsername(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getParentUsername");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getParentUsername(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getRemoteIPAddress() {
      return this.getRemoteIPAddress((Map)null, false);
   }

   public String getRemoteIPAddress(Map<String, String> __ctx) {
      return this.getRemoteIPAddress(__ctx, true);
   }

   private String getRemoteIPAddress(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getRemoteIPAddress");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getRemoteIPAddress(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getSessionID() {
      return this.getSessionID((Map)null, false);
   }

   public String getSessionID(Map<String, String> __ctx) {
      return this.getSessionID(__ctx, true);
   }

   private String getSessionID(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getSessionID");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getSessionID(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public SessionMetricsIce getSessionMetrics() {
      return this.getSessionMetrics((Map)null, false);
   }

   public SessionMetricsIce getSessionMetrics(Map<String, String> __ctx) {
      return this.getSessionMetrics(__ctx, true);
   }

   private SessionMetricsIce getSessionMetrics(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getSessionMetrics");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getSessionMetrics(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public String getUserAgentIce() {
      return this.getUserAgentIce((Map)null, false);
   }

   public String getUserAgentIce(Map<String, String> __ctx) {
      return this.getUserAgentIce(__ctx, true);
   }

   private String getUserAgentIce(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserAgentIce");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getUserAgentIce(__ctx);
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public UserPrx getUserProxy(String username) throws FusionException {
      return this.getUserProxy(username, (Map)null, false);
   }

   public UserPrx getUserProxy(String username, Map<String, String> __ctx) throws FusionException {
      return this.getUserProxy(username, __ctx, true);
   }

   private UserPrx getUserProxy(String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("getUserProxy");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.getUserProxy(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void groupChatJoined(String id) {
      this.groupChatJoined(id, (Map)null, false);
   }

   public void groupChatJoined(String id, Map<String, String> __ctx) {
      this.groupChatJoined(id, __ctx, true);
   }

   private void groupChatJoined(String id, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.groupChatJoined(id, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void groupChatJoinedMultiple(String id, int increment) {
      this.groupChatJoinedMultiple(id, increment, (Map)null, false);
   }

   public void groupChatJoinedMultiple(String id, int increment, Map<String, String> __ctx) {
      this.groupChatJoinedMultiple(id, increment, __ctx, true);
   }

   private void groupChatJoinedMultiple(String id, int increment, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.groupChatJoinedMultiple(id, increment, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted) {
      this.notifyUserJoinedChatRoomOneWay(chatroomname, username, isAdministrator, isMuted, (Map)null, false);
   }

   public void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted, Map<String, String> __ctx) {
      this.notifyUserJoinedChatRoomOneWay(chatroomname, username, isAdministrator, isMuted, __ctx, true);
   }

   private void notifyUserJoinedChatRoomOneWay(String chatroomname, String username, boolean isAdministrator, boolean isMuted, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.notifyUserJoinedChatRoomOneWay(chatroomname, username, isAdministrator, isMuted, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted) throws FusionException {
      this.notifyUserJoinedGroupChat(groupChatId, username, isMuted, (Map)null, false);
   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Map<String, String> __ctx) throws FusionException {
      this.notifyUserJoinedGroupChat(groupChatId, username, isMuted, __ctx, true);
   }

   private void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyUserJoinedGroupChat");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.notifyUserJoinedGroupChat(groupChatId, username, isMuted, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyUserLeftChatRoomOneWay(String chatroomname, String username) {
      this.notifyUserLeftChatRoomOneWay(chatroomname, username, (Map)null, false);
   }

   public void notifyUserLeftChatRoomOneWay(String chatroomname, String username, Map<String, String> __ctx) {
      this.notifyUserLeftChatRoomOneWay(chatroomname, username, __ctx, true);
   }

   private void notifyUserLeftChatRoomOneWay(String chatroomname, String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.notifyUserLeftChatRoomOneWay(chatroomname, username, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username) throws FusionException {
      this.notifyUserLeftGroupChat(groupChatId, username, (Map)null, false);
   }

   public void notifyUserLeftGroupChat(String groupChatId, String username, Map<String, String> __ctx) throws FusionException {
      this.notifyUserLeftGroupChat(groupChatId, username, __ctx, true);
   }

   private void notifyUserLeftGroupChat(String groupChatId, String username, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("notifyUserLeftGroupChat");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.notifyUserLeftGroupChat(groupChatId, username, __ctx);
            return;
         } catch (LocalExceptionWrapper var8) {
            this.__handleExceptionWrapper(__delBase, var8, (OutgoingAsync)null);
         } catch (LocalException var9) {
            __cnt = this.__handleException(__delBase, var9, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void photoUploaded() {
      this.photoUploaded((Map)null, false);
   }

   public void photoUploaded(Map<String, String> __ctx) {
      this.photoUploaded(__ctx, true);
   }

   private void photoUploaded(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.photoUploaded(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public boolean privateChattedWith(String username) {
      return this.privateChattedWith(username, (Map)null, false);
   }

   public boolean privateChattedWith(String username, Map<String, String> __ctx) {
      return this.privateChattedWith(username, __ctx, true);
   }

   private boolean privateChattedWith(String username, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("privateChattedWith");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            return __del.privateChattedWith(username, __ctx);
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void profileEdited() {
      this.profileEdited((Map)null, false);
   }

   public void profileEdited(Map<String, String> __ctx) {
      this.profileEdited(__ctx, true);
   }

   private void profileEdited(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.profileEdited(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putAlertMessage(String message, String title, short timeout) throws FusionException {
      this.putAlertMessage(message, title, timeout, (Map)null, false);
   }

   public void putAlertMessage(String message, String title, short timeout, Map<String, String> __ctx) throws FusionException {
      this.putAlertMessage(message, title, timeout, __ctx, true);
   }

   private void putAlertMessage(String message, String title, short timeout, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putAlertMessage");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.putAlertMessage(message, title, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putAlertMessageOneWay(String message, String title, short timeout) {
      this.putAlertMessageOneWay(message, title, timeout, (Map)null, false);
   }

   public void putAlertMessageOneWay(String message, String title, short timeout, Map<String, String> __ctx) {
      this.putAlertMessageOneWay(message, title, timeout, __ctx, true);
   }

   private void putAlertMessageOneWay(String message, String title, short timeout, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.putAlertMessageOneWay(message, title, timeout, __ctx);
            return;
         } catch (LocalExceptionWrapper var9) {
            this.__handleExceptionWrapper(__delBase, var9, (OutgoingAsync)null);
         } catch (LocalException var10) {
            __cnt = this.__handleException(__delBase, var10, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putMessage(MessageDataIce message) throws FusionException {
      this.putMessage(message, (Map)null, false);
   }

   public void putMessage(MessageDataIce message, Map<String, String> __ctx) throws FusionException {
      this.putMessage(message, __ctx, true);
   }

   private void putMessage(MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putMessage");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.putMessage(message, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putMessageOneWay(MessageDataIce message) {
      this.putMessageOneWay(message, (Map)null, false);
   }

   public void putMessageOneWay(MessageDataIce message, Map<String, String> __ctx) {
      this.putMessageOneWay(message, __ctx, true);
   }

   private void putMessageOneWay(MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.putMessageOneWay(message, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putSerializedPacket(byte[] packet) throws FusionException {
      this.putSerializedPacket(packet, (Map)null, false);
   }

   public void putSerializedPacket(byte[] packet, Map<String, String> __ctx) throws FusionException {
      this.putSerializedPacket(packet, __ctx, true);
   }

   private void putSerializedPacket(byte[] packet, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("putSerializedPacket");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.putSerializedPacket(packet, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void putSerializedPacketOneWay(byte[] packet) {
      this.putSerializedPacketOneWay(packet, (Map)null, false);
   }

   public void putSerializedPacketOneWay(byte[] packet, Map<String, String> __ctx) {
      this.putSerializedPacketOneWay(packet, __ctx, true);
   }

   private void putSerializedPacketOneWay(byte[] packet, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.putSerializedPacketOneWay(packet, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants) throws FusionException {
      this.sendGroupChatParticipantArrays(groupChatId, imType, participants, mutedParticipants, (Map)null, false);
   }

   public void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants, Map<String, String> __ctx) throws FusionException {
      this.sendGroupChatParticipantArrays(groupChatId, imType, participants, mutedParticipants, __ctx, true);
   }

   private void sendGroupChatParticipantArrays(String groupChatId, byte imType, String[] participants, String[] mutedParticipants, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendGroupChatParticipantArrays");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.sendGroupChatParticipantArrays(groupChatId, imType, participants, mutedParticipants, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants) throws FusionException {
      this.sendGroupChatParticipants(groupChatId, imType, participants, mutedParticipants, (Map)null, false);
   }

   public void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants, Map<String, String> __ctx) throws FusionException {
      this.sendGroupChatParticipants(groupChatId, imType, participants, mutedParticipants, __ctx, true);
   }

   private void sendGroupChatParticipants(String groupChatId, byte imType, String participants, String mutedParticipants, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendGroupChatParticipants");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.sendGroupChatParticipants(groupChatId, imType, participants, mutedParticipants, __ctx);
            return;
         } catch (LocalExceptionWrapper var10) {
            this.__handleExceptionWrapper(__delBase, var10, (OutgoingAsync)null);
         } catch (LocalException var11) {
            __cnt = this.__handleException(__delBase, var11, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendMessage(MessageDataIce message) throws FusionException {
      this.sendMessage(message, (Map)null, false);
   }

   public void sendMessage(MessageDataIce message, Map<String, String> __ctx) throws FusionException {
      this.sendMessage(message, __ctx, true);
   }

   private void sendMessage(MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendMessage");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.sendMessage(message, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message) throws FusionException {
      this.sendMessageBackToUserAsEmote(message, (Map)null, false);
   }

   public void sendMessageBackToUserAsEmote(MessageDataIce message, Map<String, String> __ctx) throws FusionException {
      this.sendMessageBackToUserAsEmote(message, __ctx, true);
   }

   private void sendMessageBackToUserAsEmote(MessageDataIce message, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("sendMessageBackToUserAsEmote");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.sendMessageBackToUserAsEmote(message, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setChatListVersion(int version) throws FusionException {
      this.setChatListVersion(version, (Map)null, false);
   }

   public void setChatListVersion(int version, Map<String, String> __ctx) throws FusionException {
      this.setChatListVersion(version, __ctx, true);
   }

   private void setChatListVersion(int version, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setChatListVersion");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.setChatListVersion(version, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setCurrentChatListGroupChatSubset(ChatListIce ccl) {
      this.setCurrentChatListGroupChatSubset(ccl, (Map)null, false);
   }

   public void setCurrentChatListGroupChatSubset(ChatListIce ccl, Map<String, String> __ctx) {
      this.setCurrentChatListGroupChatSubset(ccl, __ctx, true);
   }

   private void setCurrentChatListGroupChatSubset(ChatListIce ccl, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.setCurrentChatListGroupChatSubset(ccl, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setLanguage(String language) {
      this.setLanguage(language, (Map)null, false);
   }

   public void setLanguage(String language, Map<String, String> __ctx) {
      this.setLanguage(language, __ctx, true);
   }

   private void setLanguage(String language, Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.setLanguage(language, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void setPresence(int presence) throws FusionException {
      this.setPresence(presence, (Map)null, false);
   }

   public void setPresence(int presence, Map<String, String> __ctx) throws FusionException {
      this.setPresence(presence, __ctx, true);
   }

   private void setPresence(int presence, Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("setPresence");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.setPresence(presence, __ctx);
            return;
         } catch (LocalExceptionWrapper var7) {
            this.__handleExceptionWrapper(__delBase, var7, (OutgoingAsync)null);
         } catch (LocalException var8) {
            __cnt = this.__handleException(__delBase, var8, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void silentlyDropIncomingPackets() {
      this.silentlyDropIncomingPackets((Map)null, false);
   }

   public void silentlyDropIncomingPackets(Map<String, String> __ctx) {
      this.silentlyDropIncomingPackets(__ctx, true);
   }

   private void silentlyDropIncomingPackets(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.silentlyDropIncomingPackets(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void statusMessageSet() {
      this.statusMessageSet((Map)null, false);
   }

   public void statusMessageSet(Map<String, String> __ctx) {
      this.statusMessageSet(__ctx, true);
   }

   private void statusMessageSet(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.statusMessageSet(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void themeUpdated() {
      this.themeUpdated((Map)null, false);
   }

   public void themeUpdated(Map<String, String> __ctx) {
      this.themeUpdated(__ctx, true);
   }

   private void themeUpdated(Map<String, String> __ctx, boolean __explicitCtx) {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.themeUpdated(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public void touch() throws FusionException {
      this.touch((Map)null, false);
   }

   public void touch(Map<String, String> __ctx) throws FusionException {
      this.touch(__ctx, true);
   }

   private void touch(Map<String, String> __ctx, boolean __explicitCtx) throws FusionException {
      if (__explicitCtx && __ctx == null) {
         __ctx = _emptyContext;
      }

      int __cnt = 0;

      while(true) {
         _ObjectDel __delBase = null;

         try {
            this.__checkTwowayOnly("touch");
            __delBase = this.__getDelegate(false);
            _SessionDel __del = (_SessionDel)__delBase;
            __del.touch(__ctx);
            return;
         } catch (LocalExceptionWrapper var6) {
            this.__handleExceptionWrapper(__delBase, var6, (OutgoingAsync)null);
         } catch (LocalException var7) {
            __cnt = this.__handleException(__delBase, var7, (OutgoingAsync)null, __cnt);
         }
      }
   }

   public static SessionPrx checkedCast(ObjectPrx __obj) {
      SessionPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SessionPrx)__obj;
         } catch (ClassCastException var4) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::Session")) {
               SessionPrxHelper __h = new SessionPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SessionPrx)__d;
   }

   public static SessionPrx checkedCast(ObjectPrx __obj, Map<String, String> __ctx) {
      SessionPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SessionPrx)__obj;
         } catch (ClassCastException var5) {
            if (__obj.ice_isA("::com::projectgoth::fusion::slice::Session", __ctx)) {
               SessionPrxHelper __h = new SessionPrxHelper();
               __h.__copyFrom(__obj);
               __d = __h;
            }
         }
      }

      return (SessionPrx)__d;
   }

   public static SessionPrx checkedCast(ObjectPrx __obj, String __facet) {
      SessionPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::Session")) {
               SessionPrxHelper __h = new SessionPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var5) {
         }
      }

      return __d;
   }

   public static SessionPrx checkedCast(ObjectPrx __obj, String __facet, Map<String, String> __ctx) {
      SessionPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);

         try {
            if (__bb.ice_isA("::com::projectgoth::fusion::slice::Session", __ctx)) {
               SessionPrxHelper __h = new SessionPrxHelper();
               __h.__copyFrom(__bb);
               __d = __h;
            }
         } catch (FacetNotExistException var6) {
         }
      }

      return __d;
   }

   public static SessionPrx uncheckedCast(ObjectPrx __obj) {
      SessionPrx __d = null;
      if (__obj != null) {
         try {
            __d = (SessionPrx)__obj;
         } catch (ClassCastException var4) {
            SessionPrxHelper __h = new SessionPrxHelper();
            __h.__copyFrom(__obj);
            __d = __h;
         }
      }

      return (SessionPrx)__d;
   }

   public static SessionPrx uncheckedCast(ObjectPrx __obj, String __facet) {
      SessionPrx __d = null;
      if (__obj != null) {
         ObjectPrx __bb = __obj.ice_facet(__facet);
         SessionPrxHelper __h = new SessionPrxHelper();
         __h.__copyFrom(__bb);
         __d = __h;
      }

      return __d;
   }

   protected _ObjectDelM __createDelegateM() {
      return new _SessionDelM();
   }

   protected _ObjectDelD __createDelegateD() {
      return new _SessionDelD();
   }

   public static void __write(BasicStream __os, SessionPrx v) {
      __os.writeProxy(v);
   }

   public static SessionPrx __read(BasicStream __is) {
      ObjectPrx proxy = __is.readProxy();
      if (proxy != null) {
         SessionPrxHelper result = new SessionPrxHelper();
         result.__copyFrom(proxy);
         return result;
      } else {
         return null;
      }
   }
}
