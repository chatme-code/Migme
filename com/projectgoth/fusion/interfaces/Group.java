package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBObject;

public interface Group extends EJBObject {
   void giveGroupMemberModeratorRights(String var1, int var2, String var3) throws EJBExceptionWithErrorCause, RemoteException;

   void removeGroupMemberModeratorRights(String var1, int var2, String var3) throws EJBExceptionWithErrorCause, RemoteException;

   int getModeratorCount(int var1, boolean var2) throws RemoteException;

   Set getModeratorUserNames(int var1, boolean var2) throws RemoteException;

   List getGroupMembers(int var1) throws RemoteException;
}
