package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.TieBase;

public class _AuthenticationServiceTie extends _AuthenticationServiceDisp implements TieBase {
   private _AuthenticationServiceOperations _ice_delegate;

   public _AuthenticationServiceTie() {
   }

   public _AuthenticationServiceTie(_AuthenticationServiceOperations delegate) {
      this._ice_delegate = delegate;
   }

   public Object ice_delegate() {
      return this._ice_delegate;
   }

   public void ice_delegate(Object delegate) {
      this._ice_delegate = (_AuthenticationServiceOperations)delegate;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         return !(rhs instanceof _AuthenticationServiceTie) ? false : this._ice_delegate.equals(((_AuthenticationServiceTie)rhs)._ice_delegate);
      }
   }

   public int hashCode() {
      return this._ice_delegate.hashCode();
   }

   public AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP, Current __current) throws FusionException {
      return this._ice_delegate.authenticate(userCredential, clientIP, __current);
   }

   public byte[] availableCredentialTypes(int userid, Current __current) throws FusionException {
      return this._ice_delegate.availableCredentialTypes(userid, __current);
   }

   public AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential, Current __current) {
      return this._ice_delegate.checkCredential(userCredential, __current);
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType, Current __current) {
      return this._ice_delegate.checkCredentialByUserId(userid, password, passwordType, __current);
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType, Current __current) {
      return this._ice_delegate.checkCredentialByUsername(username, password, passwordType, __current);
   }

   public AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential, Current __current) throws FusionException {
      return this._ice_delegate.createCredential(userCredential, __current);
   }

   public AuthenticationServiceResponseCodeEnum exists(int userid, byte passwordType, Current __current) throws FusionException {
      return this._ice_delegate.exists(userid, passwordType, __current);
   }

   public Credential[] getAllCredentials(int userid, Current __current) throws FusionException {
      return this._ice_delegate.getAllCredentials(userid, __current);
   }

   public Credential[] getAllCredentialsFromOldSource(int userid, Current __current) throws FusionException {
      return this._ice_delegate.getAllCredentialsFromOldSource(userid, __current);
   }

   public AuthenticationServiceCredentialResponse getCredential(int userid, byte passwordType, Current __current) throws FusionException {
      return this._ice_delegate.getCredential(userid, passwordType, __current);
   }

   public Credential[] getCredentialsForTypes(int userid, byte[] passwordTypes, Current __current) throws FusionException {
      return this._ice_delegate.getCredentialsForTypes(userid, passwordTypes, __current);
   }

   public AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType, Current __current) {
      return this._ice_delegate.getLatestCredentialByUsernameAndPasswordType(username, passwordType, __current);
   }

   public void migrateUserCredentials(int userid, Current __current) throws FusionException {
      this._ice_delegate.migrateUserCredentials(userid, __current);
   }

   public AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential, Current __current) throws FusionException {
      return this._ice_delegate.removeCredential(userCredential, __current);
   }

   public AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential, Current __current) throws FusionException {
      return this._ice_delegate.updateCredential(userCredential, __current);
   }

   public AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword, Current __current) throws FusionException {
      return this._ice_delegate.updateFusionCredential(userCredential, oldPassword, __current);
   }

   public int userIDForFusionUsername(String username, Current __current) throws FusionException {
      return this._ice_delegate.userIDForFusionUsername(username, __current);
   }
}
