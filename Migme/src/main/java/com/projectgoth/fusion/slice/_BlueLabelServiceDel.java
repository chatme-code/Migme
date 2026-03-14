package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _BlueLabelServiceDel extends _ObjectDel {
   WebServiceResponse registerAccount(String var1, String var2, int var3, String var4, int var5, String var6, String var7, String var8, String var9, String var10, String var11, String var12, Map<String, String> var13) throws LocalExceptionWrapper, FusionException;

   WebServiceResponse fullVoucherRedemption(String var1, String var2, BlueLabelOneVoucher var3, Map<String, String> var4) throws LocalExceptionWrapper, FusionException;

   WebServiceResponse getAccountStatus(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   WebServiceResponse authenticate(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;
}
