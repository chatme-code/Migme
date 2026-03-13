package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.data.ChatRoomEmoteLogData;
import com.projectgoth.fusion.data.GroupData;
import com.projectgoth.fusion.data.MessageData;
import com.projectgoth.fusion.data.MobilePrefixData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import java.sql.Connection;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface MessageLocal extends EJBLocalObject {
   MessageData sendSMS(MessageData var1, AccountEntrySourceData var2) throws EJBException;

   void sendSystemSMS(SystemSMSData var1, AccountEntrySourceData var2) throws EJBException;

   void sendSystemSMSNoTransaction(SystemSMSData var1, AccountEntrySourceData var2) throws EJBException;

   void sendSystemSMS(SystemSMSData var1, long var2, AccountEntrySourceData var4) throws EJBException;

   int getSystemSMSCount(SystemSMSData.SubTypeEnum var1, String var2) throws EJBException;

   int getSystemSMSCount(SystemSMSData.SubTypeEnum var1, String var2, String var3) throws EJBException;

   String cleanPhoneNumber(String var1) throws EJBException;

   String cleanAndValidatePhoneNumber(String var1, boolean var2) throws EJBException;

   Integer getIDDCode(String var1) throws EJBException;

   MobilePrefixData getMobilePrefixData(int var1, int var2) throws EJBException;

   int getMinimumMobileNumberLength(int var1) throws EJBException;

   boolean isMobileNumber(String var1, boolean var2) throws EJBException;

   MessageData getMessage(int var1) throws EJBException;

   SystemSMSData getSystemSMS(String var1, String var2) throws EJBException;

   ChatRoomData getSimpleChatRoomData(Integer var1, Connection var2) throws EJBException;

   ChatRoomData getSimpleChatRoomData(String var1, Connection var2) throws EJBException;

   void updateRoomExtraData(ChatRoomData var1) throws EJBException;

   void updateRoomDetails(String var1, String var2, String var3, String var4) throws EJBException;

   void updateRoomKickingRule(String var1, String var2, boolean var3) throws EJBException;

   void updateRoomAdultOnlyFlag(String var1, String var2, boolean var3) throws EJBException;

   void updateRoomKeywords(String var1, String var2, String var3, int var4) throws EJBException;

   List getChatRooms(int var1, String var2) throws EJBException;

   List getChatRooms(int var1, String var2, String var3, boolean var4, boolean var5) throws EJBException;

   List getChatRoomsV2(int var1, String var2, String var3, boolean var4, boolean var5) throws EJBException;

   void createChatRoom(ChatRoomData var1, String var2) throws EJBException;

   void sendSystemEmail(String var1, String var2, String var3) throws EJBException;

   void sendSystemEmail(String var1, String var2, String var3, UserEmailAddressData.UserEmailAddressTypeEnum var4) throws EJBException;

   void sendEmailFromNoReply(String var1, String var2, String var3) throws EJBException;

   String getUserEmailAddress(String var1) throws EJBException;

   void sendEmail(String var1, String var2, String var3, String var4, String var5);

   String[] getGroupChatRooms(int var1) throws EJBException;

   Integer[] announceMessageToChatrooms(String[] var1, String var2, int var3) throws EJBException;

   Integer[] announceMessageToUserOwnedChatrooms(String var1, int var2) throws EJBException;

   void sendChangeRoomOwnerEmail(String var1, String var2, String var3) throws EJBException;

   void changeRoomOwner(String var1, String var2, String var3) throws EJBException;

   void addRoomModerator(String var1, String var2, String var3) throws EJBException;

   void resetRoomModerators(String var1) throws EJBException;

   void removeRoomModerator(String var1, String var2, String var3) throws EJBException;

   void banGroupMember(String var1, GroupData var2, String var3) throws EJBException;

   void unbanGroupMember(String var1, GroupData var2, String var3) throws EJBException;

   boolean isUserBlackListedInGroup(String var1, int var2) throws EJBException;

   boolean isModeratorOfChatRoom(String var1, String var2);

   void addChatRoomEmoteLog(ChatRoomEmoteLogData var1) throws EJBException;

   void banUserFromRoom(String var1, String var2, String var3) throws EJBException;

   boolean updateChatroomBannedList(String var1, String var2) throws EJBException;

   void unbanUserFromRoom(String var1, String var2, String var3) throws EJBException;

   void setChatRoomMaxSize(String var1, int var2) throws FusionEJBException;
}
