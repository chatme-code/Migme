package com.projectgoth.fusion.slice;

import Ice.LocalException;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.Outgoing;
import java.util.Map;

public final class _AuthenticationServiceDelM extends _ObjectDelM implements _AuthenticationServiceDel {
   public AuthenticationServiceResponseCodeEnum authenticate(Credential userCredential, String clientIP, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("authenticate", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(userCredential);
            __os.writeString(clientIP);
            __os.writePendingObjects();
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public byte[] availableCredentialTypes(int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("availableCredentialTypes", OperationMode.Normal, __ctx);

      byte[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            byte[] __ret = ByteArrayHelper.read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public AuthenticationServiceResponseCodeEnum checkCredential(Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("checkCredential", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(userCredential);
            __os.writePendingObjects();
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUserId(int userid, String password, byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("checkCredentialByUserId", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var9;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
            __os.writeString(password);
            __os.writeByte(passwordType);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var9 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var9;
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUsername(String username, String password, byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("checkCredentialByUsername", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var9;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(password);
            __os.writeByte(passwordType);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var9 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var9;
   }

   public AuthenticationServiceResponseCodeEnum createCredential(Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createCredential", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(userCredential);
            __os.writePendingObjects();
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public AuthenticationServiceResponseCodeEnum exists(int userid, byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("exists", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
            __os.writeByte(passwordType);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public Credential[] getAllCredentials(int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getAllCredentials", OperationMode.Normal, __ctx);

      Credential[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            Credential[] __ret = CredentialArrayHelper.read(__is);
            __is.readPendingObjects();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public Credential[] getAllCredentialsFromOldSource(int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getAllCredentialsFromOldSource", OperationMode.Normal, __ctx);

      Credential[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            Credential[] __ret = CredentialArrayHelper.read(__is);
            __is.readPendingObjects();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public AuthenticationServiceCredentialResponse getCredential(int userid, byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getCredential", OperationMode.Normal, __ctx);

      AuthenticationServiceCredentialResponse var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
            __os.writeByte(passwordType);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceCredentialResponseHolder __ret = new AuthenticationServiceCredentialResponseHolder();
            __is.readObject(__ret.getPatcher());
            __is.readPendingObjects();
            __is.endReadEncaps();
            var8 = __ret.value;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public Credential[] getCredentialsForTypes(int userid, byte[] passwordTypes, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getCredentialsForTypes", OperationMode.Normal, __ctx);

      Credential[] var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
            ByteArrayHelper.write(__os, passwordTypes);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            Credential[] __ret = CredentialArrayHelper.read(__is);
            __is.readPendingObjects();
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(String username, byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getLatestCredentialByUsernameAndPasswordType", OperationMode.Normal, __ctx);

      AuthenticationServiceCredentialResponse var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeByte(passwordType);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceCredentialResponseHolder __ret = new AuthenticationServiceCredentialResponseHolder();
            __is.readObject(__ret.getPatcher());
            __is.readPendingObjects();
            __is.endReadEncaps();
            var8 = __ret.value;
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public void migrateUserCredentials(int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("migrateUserCredentials", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(userid);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public AuthenticationServiceResponseCodeEnum removeCredential(Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("removeCredential", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(userCredential);
            __os.writePendingObjects();
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public AuthenticationServiceResponseCodeEnum updateCredential(Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("updateCredential", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(userCredential);
            __os.writePendingObjects();
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public AuthenticationServiceResponseCodeEnum updateFusionCredential(Credential userCredential, String oldPassword, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("updateFusionCredential", OperationMode.Normal, __ctx);

      AuthenticationServiceResponseCodeEnum var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(userCredential);
            __os.writeString(oldPassword);
            __os.writePendingObjects();
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            AuthenticationServiceResponseCodeEnum __ret = AuthenticationServiceResponseCodeEnum.__read(__is);
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public int userIDForFusionUsername(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("userIDForFusionUsername", OperationMode.Normal, __ctx);

      int var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var15) {
                  throw var15;
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }
}
