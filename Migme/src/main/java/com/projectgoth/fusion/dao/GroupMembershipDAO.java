package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.domain.UsernameAndMobileNumber;
import java.util.List;

public interface GroupMembershipDAO {
   List<String> getGroupMemberUsernamesForEmailNotification(int var1);

   List<UsernameAndMobileNumber> getGroupMemberUsernamesAndMobileNumbersForSMSNotification(int var1);

   List<UsernameAndMobileNumber> getGroupMemberUsernamesAndMobileNumbersForGroupEventSMSNotification(int var1);

   List<String> getGroupMemberUsernamesForEventNotification(int var1);

   List<String> getGroupMemberUsernamesForGroupEventNotification(int var1);

   List<String> getGroupMemberUsernamesForNewGroupUserPostNotificationViaEmail(int var1);

   List<String> getGroupMemberUsernamesForNewGroupUserPostNotificationViaEventSystem(int var1);
}
