package com.projectgoth.fusion.slice;

import Ice.ObjectPrx;
import java.util.Map;

public interface AuthenticationServicePrx extends ObjectPrx {
   AuthenticationServiceResponseCodeEnum authenticate(Credential var1, String var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum authenticate(Credential var1, String var2, Map<String, String> var3) throws FusionException;

   AuthenticationServiceResponseCodeEnum exists(int var1, byte var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum exists(int var1, byte var2, Map<String, String> var3) throws FusionException;

   AuthenticationServiceResponseCodeEnum createCredential(Credential var1) throws FusionException;

   AuthenticationServiceResponseCodeEnum createCredential(Credential var1, Map<String, String> var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateCredential(Credential var1) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateCredential(Credential var1, Map<String, String> var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential var1, String var2) throws FusionException;

   AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential var1, String var2, Map<String, String> var3) throws FusionException;

   AuthenticationServiceResponseCodeEnum removeCredential(Credential var1) throws FusionException;

   AuthenticationServiceResponseCodeEnum removeCredential(Credential var1, Map<String, String> var2) throws FusionException;

   AuthenticationServiceCredentialResponse getCredential(int var1, byte var2) throws FusionException;

   AuthenticationServiceCredentialResponse getCredential(int var1, byte var2, Map<String, String> var3) throws FusionException;

   byte[] availableCredentialTypes(int var1) throws FusionException;

   byte[] availableCredentialTypes(int var1, Map<String, String> var2) throws FusionException;

   Credential[] getCredentialsForTypes(int var1, byte[] var2) throws FusionException;

   Credential[] getCredentialsForTypes(int var1, byte[] var2, Map<String, String> var3) throws FusionException;

   Credential[] getAllCredentials(int var1) throws FusionException;

   Credential[] getAllCredentials(int var1, Map<String, String> var2) throws FusionException;

   Credential[] getAllCredentialsFromOldSource(int var1) throws FusionException;

   Credential[] getAllCredentialsFromOldSource(int var1, Map<String, String> var2) throws FusionException;

   void migrateUserCredentials(int var1) throws FusionException;

   void migrateUserCredentials(int var1, Map<String, String> var2) throws FusionException;

   int userIDForFusionUsername(String var1) throws FusionException;

   int userIDForFusionUsername(String var1, Map<String, String> var2) throws FusionException;

   AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String var1, byte var2);

   AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String var1, byte var2, Map<String, String> var3);

   AuthenticationServiceResponseCodeEnum checkCredential(Credential var1);

   AuthenticationServiceResponseCodeEnum checkCredential(Credential var1, Map<String, String> var2);

   AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int var1, String var2, byte var3);

   AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int var1, String var2, byte var3, Map<String, String> var4);

   AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String var1, String var2, byte var3);

   AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String var1, String var2, byte var3, Map<String, String> var4);
}
