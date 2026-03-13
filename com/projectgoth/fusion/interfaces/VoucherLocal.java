package com.projectgoth.fusion.interfaces;

import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BlueLabelVoucherData;
import com.projectgoth.fusion.data.VoucherBatchData;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.FusionEJBException;
import com.sun.rowset.CachedRowSetImpl;
import java.util.List;
import javax.ejb.EJBException;
import javax.ejb.EJBLocalObject;

public interface VoucherLocal extends EJBLocalObject {
   int createVoucherBatch(String var1, String var2, String var3, int var4, String var5, boolean var6, AccountEntrySourceData var7) throws EJBException, FusionEJBException;

   VoucherBatchData getVoucherBatch(int var1) throws FusionEJBException;

   void cancelVoucherBatch(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   void cancelVoucher(String var1, int var2, AccountEntrySourceData var3) throws EJBException;

   BlueLabelVoucherData redeemBlueLabelVoucher(String var1, String var2, String var3) throws EJBException;

   VoucherData redeemVoucher(String var1, String var2, AccountEntrySourceData var3) throws EJBException;

   void voucherRechargeFailed(String var1) throws EJBException;

   List retrieveVouchers(String var1, int var2, int var3) throws EJBException;

   VoucherData searchForVoucher(String var1, String var2) throws EJBException;

   VoucherData getVoucher(String var1) throws EJBException;

   List retrieveVoucherBatches(String var1, int var2) throws EJBException;

   VoucherData activateVoucher(String var1) throws EJBException;

   CachedRowSetImpl affiliateOverview(String var1) throws EJBException;

   CachedRowSetImpl recentActivities(String var1, int var2) throws EJBException;

   int recentRedeem(String var1, int var2) throws EJBException;
}
