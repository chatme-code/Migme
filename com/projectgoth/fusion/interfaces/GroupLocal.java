package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import java.util.List;
import java.util.Set;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface GroupLocal extends EJBLocalObject {
   void giveGroupMemberModeratorRights(String var1, int var2, String var3) throws EJBExceptionWithErrorCause, EJBException;

   void removeGroupMemberModeratorRights(String var1, int var2, String var3) throws EJBExceptionWithErrorCause, EJBException;

   int getModeratorCount(int var1, boolean var2);

   Set getModeratorUserNames(int var1, boolean var2);

   List getGroupMembers(int var1) throws EJBException;
}
