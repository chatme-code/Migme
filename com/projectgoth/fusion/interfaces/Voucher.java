package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BlueLabelVoucherData;
import com.projectgoth.fusion.data.VoucherBatchData;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.sun.rowset.CachedRowSetImpl;
import java.rmi.RemoteException;
import java.util.List;
import javax.ejb.EJBObject;

public interface Voucher extends EJBObject {
   int createVoucherBatch(String var1, String var2, String var3, int var4, String var5, boolean var6, AccountEntrySourceData var7) throws FusionEJBException, RemoteException;

   VoucherBatchData getVoucherBatch(int var1) throws FusionEJBException, RemoteException;

   void cancelVoucherBatch(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   void cancelVoucher(String var1, int var2, AccountEntrySourceData var3) throws RemoteException;

   BlueLabelVoucherData redeemBlueLabelVoucher(String var1, String var2, String var3) throws RemoteException;

   VoucherData redeemVoucher(String var1, String var2, AccountEntrySourceData var3) throws RemoteException;

   void voucherRechargeFailed(String var1) throws RemoteException;

   List retrieveVouchers(String var1, int var2, int var3) throws RemoteException;

   VoucherData searchForVoucher(String var1, String var2) throws RemoteException;

   VoucherData getVoucher(String var1) throws RemoteException;

   List retrieveVoucherBatches(String var1, int var2) throws RemoteException;

   VoucherData activateVoucher(String var1) throws RemoteException;

   CachedRowSetImpl affiliateOverview(String var1) throws RemoteException;

   CachedRowSetImpl recentActivities(String var1, int var2) throws RemoteException;

   int recentRedeem(String var1, int var2) throws RemoteException;
}
