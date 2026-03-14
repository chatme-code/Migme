package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface BlueLabelServicePrx extends ObjectPrx {
   WebServiceResponse registerAccount(String var1, String var2, int var3, String var4, int var5, String var6, String var7, String var8, String var9, String var10, String var11, String var12) throws FusionException;

   WebServiceResponse registerAccount(String var1, String var2, int var3, String var4, int var5, String var6, String var7, String var8, String var9, String var10, String var11, String var12, Map<String, String> var13) throws FusionException;

   WebServiceResponse fullVoucherRedemption(String var1, String var2, BlueLabelOneVoucher var3) throws FusionException;

   WebServiceResponse fullVoucherRedemption(String var1, String var2, BlueLabelOneVoucher var3, Map<String, String> var4) throws FusionException;

   WebServiceResponse getAccountStatus(String var1) throws FusionException;

   WebServiceResponse getAccountStatus(String var1, Map<String, String> var2) throws FusionException;

   WebServiceResponse authenticate(String var1) throws FusionException;

   WebServiceResponse authenticate(String var1, Map<String, String> var2) throws FusionException;
}
