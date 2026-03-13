package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public enum AuthenticationServiceResponseCodeEnum implements Serializable {
   Failed,
   Success,
   UnknownUsername,
   UnknownCredential,
   InvalidCredential,
   CredentialsExpired,
   AuthenticationRateExceeded,
   InvalidRequestingIP,
   CredentialAlreadyExists,
   InternalError,
   UnknownError;

   public static final int _Failed = 0;
   public static final int _Success = 1;
   public static final int _UnknownUsername = 2;
   public static final int _UnknownCredential = 3;
   public static final int _InvalidCredential = 4;
   public static final int _CredentialsExpired = 5;
   public static final int _AuthenticationRateExceeded = 6;
   public static final int _InvalidRequestingIP = 7;
   public static final int _CredentialAlreadyExists = 8;
   public static final int _InternalError = 9;
   public static final int _UnknownError = 10;

   public static AuthenticationServiceResponseCodeEnum convert(int val) {
      assert val >= 0 && val < 11;

      return values()[val];
   }

   public static AuthenticationServiceResponseCodeEnum convert(String val) {
      try {
         return valueOf(val);
      } catch (IllegalArgumentException var2) {
         return null;
      }
   }

   public int value() {
      return this.ordinal();
   }

   public void __write(BasicStream __os) {
      __os.writeByte((byte)this.value());
   }

   public static AuthenticationServiceResponseCodeEnum __read(BasicStream __is) {
      int __v = __is.readByte(11);
      return convert(__v);
   }
}
