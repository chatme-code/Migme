package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserPostData;
import java.util.Vector;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface WebLocal extends EJBLocalObject {
   Vector getUsersWhoViewed(String var1) throws EJBException;

   String cancelVoucherBatch(String var1, int var2, String var3, String var4, String var5, String var6) throws EJBException;

   Vector getHandsetVendors() throws EJBException;

   Vector getHandsetVendorPrefixes() throws EJBException;

   boolean processILoopAPICall(String var1, String var2, String var3, String var4, String var5, String var6, String var7) throws EJBException;

   boolean processOplayoAPICall(int var1, int var2, int var3, int var4, int var5) throws EJBException;

   Vector getCountriesWithMerchants() throws EJBException;

   boolean joinGroupWithoutValidation(UserData var1, int var2, int var3, String var4, String var5, String var6, String var7, boolean var8, boolean var9, boolean var10, boolean var11, boolean var12, boolean var13) throws EJBException;

   String inviteUserToGroup(String var1, String var2, int var3) throws EJBException;

   UserPostData createGroupUserPostTransaction(int var1, UserPostData var2) throws Exception;
}
