package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.ContactData;
import com.projectgoth.fusion.data.ContactGroupData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBObject;

public interface Contact extends EJBObject {
   ContactData getContact(int var1) throws RemoteException;

   ContactData getContact(String var1, String var2) throws RemoteException;

   boolean isFriend(String var1, String var2) throws FusionEJBException, RemoteException;

   boolean isBlocking(String var1, String var2) throws FusionEJBException, RemoteException;

   boolean isFriend(int var1, int var2) throws FusionEJBException, RemoteException;

   ContactGroupData getGroup(int var1) throws RemoteException;

   void assignDisplayPictureAndStatusMessageToContacts(Connection var1, Collection var2) throws SQLException, RemoteException;

   ContactData addFusionUserAsContact(int var1, ContactData var2, boolean var3) throws FusionEJBException, RemoteException;

   Set getRecentFollowers(int var1) throws RemoteException;

   void removeFusionUserFromContact(int var1, String var2, int var3, boolean var4) throws FusionEJBException, RemoteException;

   ContactData addPendingFusionContact(int var1, ContactData var2) throws RemoteException;

   ContactData acceptContactRequest(int var1, ContactData var2, boolean var3) throws RemoteException;

   Set checkAndPopulateBCL(String var1, Connection var2) throws RemoteException;

   ContactData addIMContact(int var1, ContactData var2, boolean var3) throws RemoteException;

   ContactData addPhoneContact(int var1, ContactData var2) throws RemoteException;

   void removeContact(int var1, String var2, int var3) throws RemoteException;

   ContactData updateContactDetail(int var1, ContactData var2) throws RemoteException;

   void unblockContact(String var1, String var2, boolean var3) throws RemoteException;

   void blockContact(int var1, String var2, String var3) throws RemoteException;

   void rejectContactRequest(int var1, String var2, String var3) throws RemoteException;

   int getContactListVersion(int var1, Connection var2) throws RemoteException;

   Set getContactList(String var1) throws RemoteException;

   Set getPendingContacts(String var1) throws Exception, RemoteException;

   ContactGroupData addGroup(int var1, ContactGroupData var2, boolean var3) throws RemoteException;

   void removeGroup(int var1, String var2, int var3) throws RemoteException;

   void updateGroupDetail(int var1, ContactGroupData var2) throws RemoteException;

   List getGroupList(String var1) throws RemoteException;

   void moveContactToGroup(int var1, String var2, int var3, Integer var4) throws RemoteException;
}
