package com.projectgoth.fusion.slice;

public interface _AuthenticationServiceOperationsNC {
   AuthenticationServiceResponseCodeEnum authenticate(Credential var1, String var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum exists(int var1, byte var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum createCredential(Credential var1) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateCredential(Credential var1) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential var1, String var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum removeCredential(Credential var1) throws FusionException;

   AuthenticationServiceCredentialResponse getCredential(int var1, byte var2) throws FusionException;

   byte[] availableCredentialTypes(int var1) throws FusionException;

   Credential[] getCredentialsForTypes(int var1, byte[] var2) throws FusionException;

   Credential[] getAllCredentials(int var1) throws FusionException;

   Credential[] getAllCredentialsFromOldSource(int var1) throws FusionException;

   void migrateUserCredentials(int var1) throws FusionException;

   int userIDForFusionUsername(String var1) throws FusionException;

   AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String var1, byte var2);

   AuthenticationServiceResponseCodeEnum checkCredential(Credential var1);

   AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int var1, String var2, byte var3);

   AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String var1, String var2, byte var3);
}
