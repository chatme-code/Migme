package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BotData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.ChatroomCategoryData;
import com.projectgoth.fusion.data.EmailTemplateData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MobilePrefixData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.projectgoth.fusion.fdl.enums.MessageType;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.ejb.EJBObject;

public interface Message extends EJBObject {
   MessageData saveSentMessage(MessageData var1) throws RemoteException;

   MessageData saveReceivedMessage(MessageData var1, Integer var2) throws RemoteException;

   MessageData sendSMS(MessageData var1, AccountEntrySourceData var2) throws RemoteException;

   void sendSystemSMS(SystemSMSData var1, AccountEntrySourceData var2) throws RemoteException;

   void sendSystemSMSNoTransaction(SystemSMSData var1, AccountEntrySourceData var2) throws RemoteException;

   void sendSystemSMS(SystemSMSData var1, long var2, AccountEntrySourceData var4) throws RemoteException;

   void systemSMSFailed(int var1, Integer var2, String var3) throws RemoteException;

   void smsFailed(int var1, Integer var2, String var3, AccountEntrySourceData var4) throws RemoteException;

   int getSystemSMSCount(SystemSMSData.SubTypeEnum var1, String var2) throws RemoteException;

   int getSystemSMSCount(SystemSMSData.SubTypeEnum var1, String var2, String var3) throws RemoteException;

   String cleanPhoneNumber(String var1) throws RemoteException;

   String cleanAndValidatePhoneNumber(String var1, boolean var2) throws RemoteException;

   Integer getIDDCode(String var1) throws RemoteException;

   MobilePrefixData getMobilePrefixData(int var1, int var2) throws RemoteException;

   int getMinimumMobileNumberLength(int var1) throws RemoteException;

   boolean isMobileNumber(String var1, boolean var2) throws RemoteException;

   void changePendingSystemSMSToSent(int var1, int var2, int var3, String var4, int var5, String var6, String var7, boolean var8) throws RemoteException;

   void changePendingMessageToSent(MessageType var1, int var2, int var3, Integer var4, Integer var5, String var6, String var7) throws RemoteException;

   MessageData getMessage(int var1) throws RemoteException;

   List getMessages(String var1, Integer var2, Integer var3, Date var4, Integer var5, Integer var6) throws RemoteException;

   SystemSMSData getSystemSMS(String var1, String var2) throws RemoteException;

   List getSystemSMS(String var1, Integer var2, Date var3, Integer var4) throws RemoteException;

   List getSMSGateways() throws RemoteException;

   ChatRoomData getSimpleChatRoomData(Integer var1, Connection var2) throws RemoteException;

   ChatRoomData getSimpleChatRoomData(String var1, Connection var2) throws RemoteException;

   List getLoginChatroomCategories() throws Exception, RemoteException;

   ChatroomCategoryData getChatroomCategory(Integer var1) throws Exception, RemoteException;

   Map getChatroomNamesPerCategory(boolean var1) throws Exception, RemoteException;

   String[] getChatroomNamesInCategory(int var1) throws Exception, RemoteException;

   String[] getChatroomNamesInCategory(int var1, boolean var2) throws Exception, RemoteException;

   ChatRoomData getChatRoom(String var1) throws RemoteException;

   void updateRoomExtraData(ChatRoomData var1) throws RemoteException;

   void updateRoomDetails(String var1, String var2, String var3, String var4) throws RemoteException;

   void updateRoomKickingRule(String var1, String var2, boolean var3) throws RemoteException;

   void updateRoomAdultOnlyFlag(String var1, String var2, boolean var3) throws RemoteException;

   void updateRoomKeywords(String var1, String var2, String var3, int var4) throws RemoteException;

   List getChatRooms(int var1, String var2) throws RemoteException;

   List getChatRooms(int var1, String var2, String var3, boolean var4, boolean var5) throws RemoteException;

   List getChatRoomsV2(int var1, String var2, String var3, boolean var4, boolean var5) throws RemoteException;

   List getFavouriteChatRooms(String var1) throws RemoteException;

   List getRecentChatRooms(String var1) throws RemoteException;

   int getRecentlyAccessedChatRoomCount() throws RemoteException;

   int getActiveGroupsCount() throws RemoteException;

   void createChatRoom(ChatRoomData var1, String var2) throws RemoteException;

   void chatRoomAccessed(int var1, String var2, Integer var3, Integer var4) throws RemoteException;

   void addFavouriteChatRoom(String var1, String var2) throws RemoteException;

   void removeFavouriteChatRoom(String var1, String var2) throws RemoteException;

   void logMessageStats(Date var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, int var10, int var11, int var12, int var13, int var14, int var15, int var16, int var17, int var18) throws RemoteException;

   void sendSystemEmail(String var1, String var2, String var3) throws RemoteException;

   void sendSystemEmail(String var1, String var2, String var3, UserEmailAddressData.UserEmailAddressTypeEnum var4) throws RemoteException;

   void sendEmailFromNoReply(String var1, String var2, String var3) throws RemoteException;

   String getUserEmailAddress(String var1) throws RemoteException;

   void sendEmail(String var1, String var2, String var3, String var4, String var5) throws RemoteException;

   void updateRoomDescriptions(String[] var1, String var2) throws RemoteException;

   String[] getGroupChatRooms(int var1) throws RemoteException;

   Integer[] announceMessageToChatrooms(String[] var1, String var2, int var3) throws RemoteException;

   Integer[] announceMessageToUserOwnedChatrooms(String var1, int var2) throws RemoteException;

   void updateRoomDescription(String var1, String var2) throws RemoteException;

   void sendChangeRoomOwnerEmail(String var1, String var2, String var3) throws RemoteException;

   void changeRoomOwner(String var1, String var2, String var3) throws RemoteException;

   void addRoomModerator(String var1, String var2, String var3) throws RemoteException;

   void resetRoomModerators(String var1) throws RemoteException;

   void removeRoomModerator(String var1, String var2, String var3) throws RemoteException;

   void banGroupMember(String var1, GroupData var2, String var3) throws RemoteException;

   void unbanGroupMember(String var1, GroupData var2, String var3) throws RemoteException;

   boolean isUserBlackListedInGroup(String var1, int var2) throws RemoteException;

   boolean isModeratorOfChatRoom(String var1, String var2) throws RemoteException;

   void addChatRoomEmoteLog(ChatRoomEmoteLogData var1) throws RemoteException;

   void banUserFromRoom(String var1, String var2, String var3) throws RemoteException;

   boolean updateChatroomBannedList(String var1, String var2) throws RemoteException;

   void unbanUserFromRoom(String var1, String var2, String var3) throws RemoteException;

   List getBots() throws RemoteException;

   BotData getBot(int var1) throws RemoteException;

   BotData getBotFromCommandName(String var1) throws RemoteException;

   EmailTemplateData getEmailTemplateData(int var1) throws RemoteException;

   void setChatRoomMaxSize(String var1, int var2) throws FusionEJBException, RemoteException;
}
