package com.projectgoth.fusion.slice;

import Ice.Current;

public interface _AuthenticationServiceOperations {
   AuthenticationServiceResponseCodeEnum authenticate(Credential var1, String var2, Current var3) throws FusionException;

   AuthenticationServiceResponseCodeEnum exists(int var1, byte var2, Current var3) throws FusionException;

   AuthenticationServiceResponseCodeEnum createCredential(Credential var1, Current var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateCredential(Credential var1, Current var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential var1, String var2, Current var3) throws FusionException;

   AuthenticationServiceResponseCodeEnum removeCredential(Credential var1, Current var2) throws FusionException;

   AuthenticationServiceCredentialResponse getCredential(int var1, byte var2, Current var3) throws FusionException;

   byte[] availableCredentialTypes(int var1, Current var2) throws FusionException;

   Credential[] getCredentialsForTypes(int var1, byte[] var2, Current var3) throws FusionException;

   Credential[] getAllCredentials(int var1, Current var2) throws FusionException;

   Credential[] getAllCredentialsFromOldSource(int var1, Current var2) throws FusionException;

   void migrateUserCredentials(int var1, Current var2) throws FusionException;

   int userIDForFusionUsername(String var1, Current var2) throws FusionException;

   AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String var1, byte var2, Current var3);

   AuthenticationServiceResponseCodeEnum checkCredential(Credential var1, Current var2);

   AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int var1, String var2, byte var3, Current var4);

   AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String var1, String var2, byte var3, Current var4);
}
