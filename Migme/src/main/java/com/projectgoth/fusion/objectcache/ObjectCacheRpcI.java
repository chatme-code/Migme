package com.projectgoth.fusion.objectcache;

import Ice.Current;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.ice.IceAmdInvoker;
import com.projectgoth.fusion.slice.AMD_ObjectCache_createUserObject;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.GroupChatPrx;
import com.projectgoth.fusion.slice.MessageSwitchboardPrx;
import com.projectgoth.fusion.slice.ObjectCacheStats;
import com.projectgoth.fusion.slice.ObjectExistsException;
import com.projectgoth.fusion.slice.SessionPrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice._ObjectCacheDisp;
import java.util.concurrent.ScheduledExecutorService;

public class ObjectCacheRpcI extends _ObjectCacheDisp implements ObjectCacheInterface {
   private final ChatObjectManager manager;
   private static LazyLoader<Boolean> createUserObjectAmdEnabled;

   public ObjectCacheRpcI(ObjectCacheContext ctx) {
      this.manager = new ChatObjectManager(ctx, this.ice_id());
   }

   public UserPrx createUserObjectNonAsync(String username, Current __current) throws FusionException {
      return this.manager.createUser(username);
   }

   public void createUserObject_async(final AMD_ObjectCache_createUserObject cb, final String username, Current __current) throws FusionException {
      IceAmdInvoker ivk = new ObjectCacheIceAmdInvoker() {
         UserPrx result;

         public boolean isAMDEnabled() {
            return (Boolean)ObjectCacheRpcI.createUserObjectAmdEnabled.getValue() && super.isAMDEnabled();
         }

         public void payload() throws Exception {
            this.result = ObjectCacheRpcI.this.createUserObjectNonAsync(username);
         }

         public void ice_response() {
            cb.ice_response(this.result);
         }

         public void ice_exception(Exception e) {
            cb.ice_exception(e);
         }

         public String getLogContext() {
            return "ObjectCache.createUserObject, usernameD=" + username;
         }
      };
      ivk.invoke();
   }

   public ChatRoomPrx createChatRoomObject(String name, Current __current) throws FusionException {
      return this.manager.createRoom(name);
   }

   public GroupChatPrx createGroupChatObject(String id, String creator, String privateChatParticipantIce, String[] otherPartyList, Current __current) throws FusionException, ObjectExistsException {
      return this.manager.createGroupChatObject(id, creator, privateChatParticipantIce, otherPartyList);
   }

   public void sendAlertMessageToAllUsers(String message, String title, short timeout, Current __current) throws FusionException {
      this.manager.sendAlertMessageToAllUsers(message, title, timeout);
   }

   public void purgeGroupChatObject(String id, Current __current) {
      this.manager.purgeGroupChat(id);
   }

   public void purgeUserObject(String username, Current __current) {
      this.manager.purgeUser(username);
   }

   public GroupChatPrx[] getAllGroupChats(Current __current) throws FusionException {
      return this.manager.getGroupChats();
   }

   public ScheduledExecutorService getDistributionService() {
      return this.manager.getDistributionService();
   }

   public int getDistributionServiceQueueSize() {
      return this.manager.getDistributionServiceQueueSize();
   }

   public void setLoadWeightage(int weightage) {
      this.manager.setLoadWeightage(weightage);
   }

   public int getLoadWeightage() {
      return this.manager.getLoadWeightage();
   }

   public UserPrx findUserPrx(String username) throws FusionException {
      return this.manager.findUserPrx(username);
   }

   public GroupChatPrx findGroupChatPrx(String groupChatID) throws FusionException {
      return this.manager.findGroupChatPrx(groupChatID);
   }

   public ChatRoomPrx findChatRoomPrx(String name) throws FusionException {
      return this.manager.findChatRoomPrx(name);
   }

   public SessionPrx findSessionPrx(String name) throws FusionException {
      return this.manager.makeSessionPrx(name);
   }

   public MessageSwitchboardPrx getMessageSwitchboard(Current __current) throws FusionException {
      return this.manager.getMessageSwitchboardPrx();
   }

   public void getStats(ObjectCacheStats stats) {
      this.manager.getStats(stats);
   }

   public String[] getUsernames() {
      return this.manager.getUsernames();
   }

   public int getUserCount() {
      return this.manager.getUserCount();
   }

   static {
      createUserObjectAmdEnabled = new LazyLoader<Boolean>("CREATEUSEROBJECT_AMD_ENABLED", ObjectCacheIceAmdInvoker.AMD_ENABLED_REFRESH_INTERVAL) {
         protected Boolean fetchValue() throws Exception {
            return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.IceAsyncSettings.CREATEUSEROBJECT_AMD_ENABLED);
         }
      };
   }
}
