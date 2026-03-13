package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface ContactLocal extends EJBLocalObject {
   ContactData getContact(int var1) throws EJBException;

   ContactData getContact(String var1, String var2) throws EJBException;

   boolean isFriend(String var1, String var2) throws FusionEJBException;

   boolean isBlocking(String var1, String var2) throws FusionEJBException;

   boolean isFriend(int var1, int var2) throws FusionEJBException;

   ContactGroupData getGroup(int var1) throws EJBException;

   void assignDisplayPictureAndStatusMessageToContacts(Connection var1, Collection var2) throws SQLException;

   ContactData addFusionUserAsContact(int var1, ContactData var2, boolean var3) throws EJBException, FusionEJBException;

   Set getRecentFollowers(int var1) throws EJBException;

   void removeFusionUserFromContact(int var1, String var2, int var3, boolean var4) throws EJBException, FusionEJBException;

   ContactData addPendingFusionContact(int var1, ContactData var2) throws EJBException;

   ContactData acceptContactRequest(int var1, ContactData var2, boolean var3) throws EJBException;

   void makeReferrerAndReferreeFriends(int var1, String var2, String var3, int var4, String var5, String var6, String var7) throws EJBException;

   Set checkAndPopulateBCL(String var1, Connection var2) throws EJBException;

   void persistBroadcastList(String var1, Set var2, Connection var3) throws SQLException;

   ContactData addPhoneContact(int var1, ContactData var2) throws EJBException;

   void removeContact(int var1, String var2, int var3) throws EJBException;

   ContactData updateContactDetail(int var1, ContactData var2) throws EJBException;

   void unblockContact(String var1, String var2, boolean var3) throws EJBException;

   void blockContact(int var1, String var2, String var3) throws EJBException;

   void rejectContactRequest(int var1, String var2, String var3) throws EJBException;

   Set getContactList(String var1) throws EJBException;

   Set getPendingContacts(String var1) throws Exception;

   ContactGroupData addGroup(int var1, ContactGroupData var2, boolean var3) throws EJBException;

   void updateGroupDetail(int var1, ContactGroupData var2) throws EJBException;

   List getGroupList(String var1) throws EJBException;
}
