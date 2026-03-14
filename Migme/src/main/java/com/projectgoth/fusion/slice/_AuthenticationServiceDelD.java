package com.projectgoth.fusion.slice;

import Ice.Current;
import Ice.DispatchStatus;
import Ice.IntHolder;
import Ice.Object;
import Ice.OperationMode;
import Ice.OperationNotExistException;
import Ice.SystemException;
import Ice.UserException;
import Ice._ObjectDelD;
import IceInternal.Direct;
import IceInternal.LocalExceptionWrapper;
import java.util.Map;

public final class _AuthenticationServiceDelD extends _ObjectDelD implements _AuthenticationServiceDel {
   public AuthenticationServiceResponseCodeEnum authenticate(final Credential userCredential, final String clientIP, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "authenticate", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.authenticate(userCredential, clientIP, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceResponseCodeEnum var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
         return __result.value;
      }
   }

   public byte[] availableCredentialTypes(final int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "availableCredentialTypes", OperationMode.Normal, __ctx);
      final ByteArrayHolder __result = new ByteArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.availableCredentialTypes(userid, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         byte[] var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredential(final Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "checkCredential", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.checkCredential(userCredential, __current);
               return DispatchStatus.DispatchOK;
            }
         };

         AuthenticationServiceResponseCodeEnum var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (SystemException var14) {
         throw var14;
      } catch (Throwable var15) {
         LocalExceptionWrapper.throwWrapper(var15);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUserId(final int userid, final String password, final byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "checkCredentialByUserId", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.checkCredentialByUserId(userid, password, passwordType, __current);
               return DispatchStatus.DispatchOK;
            }
         };

         AuthenticationServiceResponseCodeEnum var9;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var9 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var9;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum checkCredentialByUsername(final String username, final String password, final byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "checkCredentialByUsername", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.checkCredentialByUsername(username, password, passwordType, __current);
               return DispatchStatus.DispatchOK;
            }
         };

         AuthenticationServiceResponseCodeEnum var9;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var9 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var9;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum createCredential(final Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "createCredential", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.createCredential(userCredential, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceResponseCodeEnum var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum exists(final int userid, final byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "exists", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.exists(userid, passwordType, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceResponseCodeEnum var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
         return __result.value;
      }
   }

   public Credential[] getAllCredentials(final int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAllCredentials", OperationMode.Normal, __ctx);
      final CredentialArrayHolder __result = new CredentialArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getAllCredentials(userid, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         Credential[] var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public Credential[] getAllCredentialsFromOldSource(final int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getAllCredentialsFromOldSource", OperationMode.Normal, __ctx);
      final CredentialArrayHolder __result = new CredentialArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getAllCredentialsFromOldSource(userid, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         Credential[] var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceCredentialResponse getCredential(final int userid, final byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getCredential", OperationMode.Normal, __ctx);
      final AuthenticationServiceCredentialResponseHolder __result = new AuthenticationServiceCredentialResponseHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getCredential(userid, passwordType, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceCredentialResponse var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
         return __result.value;
      }
   }

   public Credential[] getCredentialsForTypes(final int userid, final byte[] passwordTypes, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "getCredentialsForTypes", OperationMode.Normal, __ctx);
      final CredentialArrayHolder __result = new CredentialArrayHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.getCredentialsForTypes(userid, passwordTypes, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         Credential[] var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
         return __result.value;
      }
   }

   public AuthenticationServiceCredentialResponse getLatestCredentialByUsernameAndPasswordType(final String username, final byte passwordType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      final Current __current = new Current();
      this.__initCurrent(__current, "getLatestCredentialByUsernameAndPasswordType", OperationMode.Normal, __ctx);
      final AuthenticationServiceCredentialResponseHolder __result = new AuthenticationServiceCredentialResponseHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var4) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               __result.value = __servant.getLatestCredentialByUsernameAndPasswordType(username, passwordType, __current);
               return DispatchStatus.DispatchOK;
            }
         };

         AuthenticationServiceCredentialResponse var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (SystemException var15) {
         throw var15;
      } catch (Throwable var16) {
         LocalExceptionWrapper.throwWrapper(var16);
         return __result.value;
      }
   }

   public void migrateUserCredentials(final int userid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "migrateUserCredentials", OperationMode.Normal, __ctx);
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __servant.migrateUserCredentials(userid, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;
         } finally {
            __direct.destroy();
         }
      } catch (FusionException var13) {
         throw var13;
      } catch (SystemException var14) {
         throw var14;
      } catch (Throwable var15) {
         LocalExceptionWrapper.throwWrapper(var15);
      }

   }

   public AuthenticationServiceResponseCodeEnum removeCredential(final Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "removeCredential", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.removeCredential(userCredential, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceResponseCodeEnum var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum updateCredential(final Credential userCredential, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "updateCredential", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.updateCredential(userCredential, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceResponseCodeEnum var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }

   public AuthenticationServiceResponseCodeEnum updateFusionCredential(final Credential userCredential, final String oldPassword, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "updateFusionCredential", OperationMode.Normal, __ctx);
      final AuthenticationServiceResponseCodeEnumHolder __result = new AuthenticationServiceResponseCodeEnumHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.updateFusionCredential(userCredential, oldPassword, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         AuthenticationServiceResponseCodeEnum var8;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var8 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var8;
      } catch (FusionException var16) {
         throw var16;
      } catch (SystemException var17) {
         throw var17;
      } catch (Throwable var18) {
         LocalExceptionWrapper.throwWrapper(var18);
         return __result.value;
      }
   }

   public int userIDForFusionUsername(final String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      final Current __current = new Current();
      this.__initCurrent(__current, "userIDForFusionUsername", OperationMode.Normal, __ctx);
      final IntHolder __result = new IntHolder();
      Direct __direct = null;

      try {
         __direct = new Direct(__current) {
            public DispatchStatus run(Object __obj) {
               AuthenticationService __servant = null;

               try {
                  __servant = (AuthenticationService)__obj;
               } catch (ClassCastException var5) {
                  throw new OperationNotExistException(__current.id, __current.facet, __current.operation);
               }

               try {
                  __result.value = __servant.userIDForFusionUsername(username, __current);
                  return DispatchStatus.DispatchOK;
               } catch (UserException var4) {
                  this.setUserException(var4);
                  return DispatchStatus.DispatchUserException;
               }
            }
         };

         int var7;
         try {
            DispatchStatus __status = __direct.servant().__collocDispatch(__direct);
            if (__status == DispatchStatus.DispatchUserException) {
               __direct.throwUserException();
            }

            assert __status == DispatchStatus.DispatchOK;

            var7 = __result.value;
         } finally {
            __direct.destroy();
         }

         return var7;
      } catch (FusionException var15) {
         throw var15;
      } catch (SystemException var16) {
         throw var16;
      } catch (Throwable var17) {
         LocalExceptionWrapper.throwWrapper(var17);
         return __result.value;
      }
   }
}
