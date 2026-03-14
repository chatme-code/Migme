package com.projectgoth.fusion.slice;

import Ice._ObjectDel;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public interface _AuthenticationServiceDel extends _ObjectDel {
   AuthenticationServiceResponseCodeEnum authenticate(Credential var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceResponseCodeEnum exists(int var1, byte var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceResponseCodeEnum createCredential(Credential var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceResponseCodeEnum updateCredential(Credential var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential var1, String var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceResponseCodeEnum removeCredential(Credential var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceCredentialResponse getCredential(int var1, byte var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   byte[] availableCredentialTypes(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   Credential[] getCredentialsForTypes(int var1, byte[] var2, Map<String, String> var3) throws LocalExceptionWrapper, FusionException;

   Credential[] getAllCredentials(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   Credential[] getAllCredentialsFromOldSource(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   void migrateUserCredentials(int var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   int userIDForFusionUsername(String var1, Map<String, String> var2) throws LocalExceptionWrapper, FusionException;

   AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String var1, byte var2, Map<String, String> var3) throws LocalExceptionWrapper;

   AuthenticationServiceResponseCodeEnum checkCredential(Credential var1, Map<String, String> var2) throws LocalExceptionWrapper;

   AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int var1, String var2, byte var3, Map<String, String> var4) throws LocalExceptionWrapper;

   AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String var1, String var2, byte var3, Map<String, String> var4) throws LocalExceptionWrapper;
}
