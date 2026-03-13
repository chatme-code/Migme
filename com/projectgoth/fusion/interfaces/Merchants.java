package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.merchant.MerchantPointsLogData;
import java.rmi.RemoteException;
import java.util.List;
import javax.ejb.EJBObject;

public interface Merchants extends EJBObject {
   MerchantPointsLogData insertMerchantPoints(MerchantPointsLogData var1) throws RemoteException;

   List getMerchantsByCountry(int var1, int var2, int var3, int var4, boolean var5) throws RemoteException;

   List getMerchantsByCountry(int var1, String var2, int var3, int var4, boolean var5) throws RemoteException;
}
