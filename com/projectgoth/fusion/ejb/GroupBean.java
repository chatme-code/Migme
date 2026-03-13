package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.GroupMemberData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.slice.ChatRoomPrx;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class GroupBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(GroupBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private DataSource userRegistrationMaster;
   private DataSource userRegistrationSlave;
   private SessionContext context;

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void setSessionContext(SessionContext newContext) throws EJBException, RemoteException {
      this.context = newContext;
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         this.userRegistrationMaster = LookupUtil.getRegistrationMasterDataSource();
         this.userRegistrationSlave = LookupUtil.getRegistrationSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Throwable var3) {
         CreateException crex = new CreateException("Unable to create User EJB: " + var3.getMessage());
         crex.initCause(var3);
         throw crex;
      }
   }

   public void giveGroupMemberModeratorRights(String groupCreatorUserName, int groupID, String groupMemberUserName) throws EJBExceptionWithErrorCause, EJBException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         GroupData groupData = userEJB.getGroup(groupID);
         if (groupData == null) {
            throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INVALID_GROUP, new Object[]{"group"});
         } else if (!groupData.createdBy.equalsIgnoreCase(groupCreatorUserName)) {
            throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INCORRECT_GROUP_CREATOR, new Object[]{groupCreatorUserName, groupData.name});
         } else {
            UserData groupMemberUserData = userEJB.loadUser(groupMemberUserName, false, false);
            if (groupMemberUserData == null) {
               throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INVALID_USER, new Object[]{"group member " + groupMemberUserName});
            } else {
               ReputationLevelData groupMemberReputation = userEJB.getReputationLevelByUserid(groupMemberUserData.userID, true);
               if (groupMemberReputation == null) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.FAIL_LOADING_REPUTATION_DATA, new Object[]{" group member " + groupMemberUserName});
               } else {
                  int moderatorMinUserLevel = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Group.MODERATOR_MIN_USER_LEVEL);
                  if (groupMemberReputation.level < moderatorMinUserLevel) {
                     throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INCORRECT_TARGET_USER_LEVEL, new Object[]{groupMemberUserName, moderatorMinUserLevel});
                  } else {
                     UserData groupCreatorUserData = userEJB.loadUser(groupCreatorUserName, false, false);
                     if (groupCreatorUserData == null) {
                        throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INVALID_USER, new Object[]{"group creator " + groupCreatorUserData});
                     } else {
                        ReputationLevelData groupCreatorReputation = userEJB.getReputationLevelByUserid(groupCreatorUserData.userID, true);
                        if (groupCreatorReputation == null) {
                           throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.FAIL_LOADING_REPUTATION_DATA, new Object[]{" group creator " + groupCreatorUserName});
                        } else {
                           int maxNumModerator = groupCreatorReputation.numGroupModerators;
                           if (this.retrieveModeratorCount(groupID, false) >= maxNumModerator) {
                              throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.MAX_MODERATOR_COUNT_REACHED, new Object[]{groupCreatorUserName, maxNumModerator, groupData.name});
                           } else {
                              this.updateGroupMemberModeratorStatus(groupData, groupMemberUserName, true);
                           }
                        }
                     }
                  }
               }
            }
         }
      } catch (EJBException var12) {
         throw var12;
      } catch (SQLException var13) {
         throw new EJBExceptionWithErrorCause(var13, ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{"Database access error."});
      } catch (Exception var14) {
         throw new EJBExceptionWithErrorCause(var14, ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{"Failed to give moderator rights." + var14.getMessage()});
      }
   }

   private void updateGroupMemberModeratorStatus(GroupData groupData, String groupMemberUserName, boolean promote) throws SQLException, CreateException {
      Connection conn = this.dataSourceMaster.getConnection();

      try {
         PreparedStatement ps = conn.prepareStatement(" UPDATE groupmember, groups  SET groupmember.type = ?  WHERE groupmember.groupid = groups.id  AND groupmember.status = ?  AND groupmember.username = ?  AND groups.id = ?  AND groups.createdby = ?");

         try {
            ps.setInt(1, promote ? GroupMemberData.TypeEnum.MODERATOR.value() : GroupMemberData.TypeEnum.REGULAR.value());
            ps.setInt(2, GroupMemberData.StatusEnum.ACTIVE.value());
            ps.setString(3, groupMemberUserName);
            ps.setInt(4, groupData.id);
            ps.setString(5, groupData.createdBy);
            if (ps.executeUpdate() != 1) {
               throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.MANAGE_RIGHTS_ERROR, new Object[]{promote ? "Failed to grant moderator rights to '" + groupMemberUserName + "' on group '" + groupData.name + "'" : "Failed to revoke moderator rights from '" + groupMemberUserName + "' on group '" + groupData.name + "'"});
            }
         } finally {
            ps.close();
         }
      } finally {
         conn.close();
      }

      try {
         this.updateChatroomParticipantModeratorStatus(groupData.id, groupMemberUserName, promote);
      } catch (Exception var16) {
         log.error("Unable to update chatroom participant moderator status for group " + groupData.id + " member:" + groupMemberUserName + " promote:" + promote + ".", var16);
      }

   }

   public void removeGroupMemberModeratorRights(String groupCreatorUserName, int groupID, String groupMemberUserName) throws EJBExceptionWithErrorCause, EJBException {
      try {
         UserLocal userEJB = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         GroupData groupData = userEJB.getGroup(groupID);
         if (groupData == null) {
            throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INVALID_GROUP, new Object[]{"group"});
         } else if (!groupData.createdBy.equalsIgnoreCase(groupCreatorUserName)) {
            throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INCORRECT_GROUP_CREATOR, new Object[]{groupCreatorUserName, groupData.name});
         } else {
            UserData groupMemberUserData = userEJB.loadUser(groupMemberUserName, false, false);
            if (groupMemberUserData == null) {
               throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INVALID_USER, new Object[]{"group member " + groupMemberUserName});
            } else {
               UserData groupCreatorUserData = userEJB.loadUser(groupCreatorUserName, false, false);
               if (groupCreatorUserData == null) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INVALID_USER, new Object[]{"group creator " + groupCreatorUserData});
               } else if (groupCreatorUserData.userID.equals(groupMemberUserData.userID)) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.SELF_DEMOTE, new Object[0]);
               } else {
                  this.updateGroupMemberModeratorStatus(groupData, groupMemberUserName, false);
               }
            }
         }
      } catch (EJBException var8) {
         throw var8;
      } catch (SQLException var9) {
         throw new EJBExceptionWithErrorCause(var9, ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{"Database access error."});
      } catch (Exception var10) {
         throw new EJBExceptionWithErrorCause(var10, ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{"Failed to give moderator rights." + var10.getMessage()});
      }
   }

   public int getModeratorCount(int groupId, boolean fromMasterDB) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VIEW_GROUP_MODERATORS_ENABLED)) {
         return 0;
      } else {
         try {
            return this.retrieveModeratorCount(groupId, fromMasterDB);
         } catch (SQLException var4) {
            throw new EJBExceptionWithErrorCause(var4, ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{var4.getMessage()});
         }
      }
   }

   private int retrieveModeratorCount(int groupId, boolean fromMasterDB) throws SQLException {
      Connection conn = fromMasterDB ? this.dataSourceMaster.getConnection() : this.dataSourceSlave.getConnection();

      int var6;
      try {
         PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) as modCount FROM groupmember  WHERE groupid = ?  AND status = ?  AND type = ?");

         try {
            ps.setInt(1, groupId);
            ps.setInt(2, GroupMemberData.StatusEnum.ACTIVE.value());
            ps.setInt(3, GroupMemberData.TypeEnum.MODERATOR.value());
            ResultSet rs = ps.executeQuery();

            try {
               if (!rs.next()) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{"Failed to retrieve result for groupid " + groupId});
               }

               var6 = rs.getInt("modCount");
            } finally {
               rs.close();
            }
         } finally {
            ps.close();
         }
      } finally {
         conn.close();
      }

      return var6;
   }

   public Set<String> getModeratorUserNames(int groupId, boolean fromMasterDB) {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.VIEW_GROUP_MODERATORS_ENABLED)) {
         return new HashSet(0);
      } else {
         TreeSet result = new TreeSet();

         try {
            Connection conn = fromMasterDB ? this.dataSourceMaster.getConnection() : this.dataSourceSlave.getConnection();

            try {
               PreparedStatement ps = conn.prepareStatement("SELECT gm.username as username FROM groupmember gm WHERE gm.groupid = ?  AND gm.status = ?  AND gm.type = ?");

               try {
                  ps.setInt(1, groupId);
                  ps.setInt(2, GroupMemberData.StatusEnum.ACTIVE.value());
                  ps.setInt(3, GroupMemberData.TypeEnum.MODERATOR.value());
                  ResultSet rs = ps.executeQuery();

                  try {
                     while(rs.next()) {
                        result.add(rs.getString("username"));
                     }
                  } finally {
                     rs.close();
                  }
               } finally {
                  ps.close();
               }
            } finally {
               conn.close();
            }

            return result;
         } catch (SQLException var28) {
            throw new EJBExceptionWithErrorCause(var28, ErrorCause.GroupErrorType.INTERNAL_ERROR, new Object[]{var28.getMessage()});
         }
      }
   }

   private int updateChatroomParticipantModeratorStatus(int groupid, String username, boolean promote) throws CreateException {
      MessageLocal messageEJB = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
      String[] chatrooms = messageEJB.getGroupChatRooms(groupid);
      ChatRoomPrx[] chatRoomProxies = null;
      if (chatrooms != null && chatrooms.length > 0) {
         chatRoomProxies = EJBIcePrxFinder.findChatRoomProxies(chatrooms);
      }

      if (log.isDebugEnabled()) {
         if (chatRoomProxies != null) {
            log.debug("For group:" + groupid + " ,user[" + username + "] is in " + chatRoomProxies.length + " chatrooms");
         } else {
            log.debug("For group:" + groupid + " ,user[" + username + "] :active chatroomproxies is null");
         }
      }

      int chatRoomCount = 0;
      if (chatRoomProxies != null) {
         ChatRoomPrx[] arr$ = chatRoomProxies;
         int len$ = chatRoomProxies.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            ChatRoomPrx chatroomPrx = arr$[i$];
            if (chatroomPrx != null) {
               try {
                  chatroomPrx.updateGroupModeratorStatus(username, promote);
                  ++chatRoomCount;
               } catch (Exception var13) {
                  log.error("Unable to update group moderator status for username " + username + " under group " + groupid + " promote:" + promote + ".", var13);
               }
            }
         }
      }

      return chatRoomCount;
   }

   public List<Integer> getGroupMembers(int groupId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         List<Integer> members = new ArrayList();
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT userid.id FROM userid JOIN groupmember USING(username) WHERE groupmember.groupid=? AND status=1");
         ps.setInt(1, groupId);
         rs = ps.executeQuery();

         while(rs.next()) {
            int userId = rs.getInt("id");
            members.add(userId);
         }

         ArrayList var23 = members;
         return var23;
      } catch (SQLException var21) {
         throw new EJBException("SQLException", var21);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }
   }
}
