package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.InputStream;
import Ice.MarshalException;
import Ice.Object;
import Ice.ObjectImpl;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.OutputStream;
import IceInternal.BasicStream;
import IceInternal.Incoming;
import java.util.Arrays;

public abstract class _AuthenticationServiceDisp extends ObjectImpl implements AuthenticationService {
   public static final String[] __ids = new String[]{"::Ice::Object", "::com::projectgoth::fusion::slice::AuthenticationService"};
   private static final String[] __all = new String[]{"authenticate", "availableCredentialTypes", "checkCredential", "checkCredentialByUserId", "checkCredentialByUsername", "createCredential", "exists", "getAllCredentials", "getAllCredentialsFromOldSource", "getCredential", "getCredentialsForTypes", "getLatestCredentialByUsernameAndPasswordType", "ice_id", "ice_ids", "ice_isA", "ice_ping", "migrateUserCredentials", "removeCredential", "updateCredential", "updateFusionCredential", "userIDForFusionUsername"};

   protected void ice_copyStateFrom(Object __obj) throws CloneNotSupportedException {
      throw new CloneNotSupportedException();
   }

   public boolean ice_isA(String s) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public boolean ice_isA(String s, Current __current) {
      return Arrays.binarySearch(__ids, s) >= 0;
   }

   public String[] ice_ids() {
      return __ids;
   }

   public String[] ice_ids(Current __current) {
      return __ids;
   }

   public String ice_id() {
      return __ids[1];
   }

   public String ice_id(Current __current) {
      return __ids[1];
   }

   public static String ice_staticId() {
      return __ids[1];
   }

   public final AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP) throws FusionException {
      return this.authenticate(userCredential, clientIP, (Current)null);
   }

   public final byte[] availableCredentialTypes(int userid) throws FusionException {
      return this.availableCredentialTypes(userid, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential) {
      return this.checkCredential(userCredential, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType) {
      return this.checkCredentialByUserId(userid, password, passwordType, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType) {
      return this.checkCredentialByUsername(username, password, passwordType, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential) throws FusionException {
      return this.createCredential(userCredential, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum exists(int userid, byte passwordType) throws FusionException {
      return this.exists(userid, passwordType, (Current)null);
   }

   public final Credential[] getAllCredentials(int userid) throws FusionException {
      return this.getAllCredentials(userid, (Current)null);
   }

   public final Credential[] getAllCredentialsFromOldSource(int userid) throws FusionException {
      return this.getAllCredentialsFromOldSource(userid, (Current)null);
   }

   public final AuthenticationServiceCredentialResponse getCredential(int userid, byte passwordType) throws FusionException {
      return this.getCredential(userid, passwordType, (Current)null);
   }

   public final Credential[] getCredentialsForTypes(int userid, byte[] passwordTypes) throws FusionException {
      return this.getCredentialsForTypes(userid, passwordTypes, (Current)null);
   }

   public final AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType) {
      return this.getLatestCredentialByUsernameAndPasswordType(username, passwordType, (Current)null);
   }

   public final void migrateUserCredentials(int userid) throws FusionException {
      this.migrateUserCredentials(userid, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential) throws FusionException {
      return this.removeCredential(userCredential, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential) throws FusionException {
      return this.updateCredential(userCredential, (Current)null);
   }

   public final AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword) throws FusionException {
      return this.updateFusionCredential(userCredential, oldPassword, (Current)null);
   }

   public final int userIDForFusionUsername(String username) throws FusionException {
      return this.userIDForFusionUsername(username, (Current)null);
   }

   public static DispatchStatus ___authenticate(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CredentialHolder userCredential = new CredentialHolder();
      __is.readObject(userCredential.getPatcher());
      String clientIP = __is.readString();
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceResponseCodeEnum __ret = __obj.authenticate(userCredential.value, clientIP, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___exists(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      byte passwordType = __is.readByte();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceResponseCodeEnum __ret = __obj.exists(userid, passwordType, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___createCredential(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CredentialHolder userCredential = new CredentialHolder();
      __is.readObject(userCredential.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceResponseCodeEnum __ret = __obj.createCredential(userCredential.value, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updateCredential(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CredentialHolder userCredential = new CredentialHolder();
      __is.readObject(userCredential.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceResponseCodeEnum __ret = __obj.updateCredential(userCredential.value, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___updateFusionCredential(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CredentialHolder userCredential = new CredentialHolder();
      __is.readObject(userCredential.getPatcher());
      String oldPassword = __is.readString();
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceResponseCodeEnum __ret = __obj.updateFusionCredential(userCredential.value, oldPassword, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___removeCredential(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CredentialHolder userCredential = new CredentialHolder();
      __is.readObject(userCredential.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceResponseCodeEnum __ret = __obj.removeCredential(userCredential.value, __current);
         __ret.__write(__os);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getCredential(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      byte passwordType = __is.readByte();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         AuthenticationServiceCredentialResponse __ret = __obj.getCredential(userid, passwordType, __current);
         __os.writeObject(__ret);
         __os.writePendingObjects();
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___availableCredentialTypes(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         byte[] __ret = __obj.availableCredentialTypes(userid, __current);
         ByteArrayHelper.write(__os, __ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getCredentialsForTypes(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      byte[] passwordTypes = ByteArrayHelper.read(__is);
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Credential[] __ret = __obj.getCredentialsForTypes(userid, passwordTypes, __current);
         CredentialArrayHelper.write(__os, __ret);
         __os.writePendingObjects();
         return DispatchStatus.DispatchOK;
      } catch (FusionException var8) {
         __os.writeUserException(var8);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getAllCredentials(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Credential[] __ret = __obj.getAllCredentials(userid, __current);
         CredentialArrayHelper.write(__os, __ret);
         __os.writePendingObjects();
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getAllCredentialsFromOldSource(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         Credential[] __ret = __obj.getAllCredentialsFromOldSource(userid, __current);
         CredentialArrayHelper.write(__os, __ret);
         __os.writePendingObjects();
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___migrateUserCredentials(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         __obj.migrateUserCredentials(userid, __current);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___userIDForFusionUsername(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();

      try {
         int __ret = __obj.userIDForFusionUsername(username, __current);
         __os.writeInt(__ret);
         return DispatchStatus.DispatchOK;
      } catch (FusionException var7) {
         __os.writeUserException(var7);
         return DispatchStatus.DispatchUserException;
      }
   }

   public static DispatchStatus ___getLatestCredentialByUsernameAndPasswordType(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      byte passwordType = __is.readByte();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      AuthenticationServiceCredentialResponse __ret = __obj.getLatestCredentialByUsernameAndPasswordType(username, passwordType, __current);
      __os.writeObject(__ret);
      __os.writePendingObjects();
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___checkCredential(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      CredentialHolder userCredential = new CredentialHolder();
      __is.readObject(userCredential.getPatcher());
      __is.readPendingObjects();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      AuthenticationServiceResponseCodeEnum __ret = __obj.checkCredential(userCredential.value, __current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___checkCredentialByUserId(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      int userid = __is.readInt();
      String password = __is.readString();
      byte passwordType = __is.readByte();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      AuthenticationServiceResponseCodeEnum __ret = __obj.checkCredentialByUserId(userid, password, passwordType, __current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public static DispatchStatus ___checkCredentialByUsername(AuthenticationService __obj, Incoming __inS, Current __current) {
      __checkMode(OperationMode.Normal, __current.mode);
      BasicStream __is = __inS.is();
      __is.startReadEncaps();
      String username = __is.readString();
      String password = __is.readString();
      byte passwordType = __is.readByte();
      __is.endReadEncaps();
      BasicStream __os = __inS.os();
      AuthenticationServiceResponseCodeEnum __ret = __obj.checkCredentialByUsername(username, password, passwordType, __current);
      __ret.__write(__os);
      return DispatchStatus.DispatchOK;
   }

   public DispatchStatus __dispatch(Incoming in, Current __current) {
      int pos = Arrays.binarySearch(__all, __current.operation);
      if (pos < 0) {
         throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
      } else {
         switch(pos) {
         case 0:
            return ___authenticate(this, in, __current);
         case 1:
            return ___availableCredentialTypes(this, in, __current);
         case 2:
            return ___checkCredential(this, in, __current);
         case 3:
            return ___checkCredentialByUserId(this, in, __current);
         case 4:
            return ___checkCredentialByUsername(this, in, __current);
         case 5:
            return ___createCredential(this, in, __current);
         case 6:
            return ___exists(this, in, __current);
         case 7:
            return ___getAllCredentials(this, in, __current);
         case 8:
            return ___getAllCredentialsFromOldSource(this, in, __current);
         case 9:
            return ___getCredential(this, in, __current);
         case 10:
            return ___getCredentialsForTypes(this, in, __current);
         case 11:
            return ___getLatestCredentialByUsernameAndPasswordType(this, in, __current);
         case 12:
            return ___ice_id(this, in, __current);
         case 13:
            return ___ice_ids(this, in, __current);
         case 14:
            return ___ice_isA(this, in, __current);
         case 15:
            return ___ice_ping(this, in, __current);
         case 16:
            return ___migrateUserCredentials(this, in, __current);
         case 17:
            return ___removeCredential(this, in, __current);
         case 18:
            return ___updateCredential(this, in, __current);
         case 19:
            return ___updateFusionCredential(this, in, __current);
         case 20:
            return ___userIDForFusionUsername(this, in, __current);
         default:
            assert false;

            throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
         }
      }
   }

   public void __write(BasicStream __os) {
      __os.writeTypeId(ice_staticId());
      __os.startWriteSlice();
      __os.endWriteSlice();
      super.__write(__os);
   }

   public void __read(BasicStream __is, boolean __rid) {
      if (__rid) {
         __is.readTypeId();
      }

      __is.startReadSlice();
      __is.endReadSlice();
      super.__read(__is, true);
   }

   public void __write(OutputStream __outS) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::AuthenticationService was not generated with stream support";
      throw ex;
   }

   public void __read(InputStream __inS, boolean __rid) {
      MarshalException ex = new MarshalException();
      ex.reason = "type com::projectgoth::fusion::slice::AuthenticationService was not generated with stream support";
      throw ex;
   }
}
