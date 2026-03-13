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

public final class _BlueLabelServiceDelM extends _ObjectDelM implements _BlueLabelServiceDel {
   public WebServiceResponse authenticate(String username, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("authenticate", OperationMode.Normal, __ctx);

      WebServiceResponse var7;
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
            WebServiceResponse __ret = new WebServiceResponse();
            __ret.__read(__is);
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

   public WebServiceResponse fullVoucherRedemption(String migUsername, String userTicket, BlueLabelOneVoucher voucher, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("fullVoucherRedemption", OperationMode.Normal, __ctx);

      WebServiceResponse var9;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(migUsername);
            __os.writeString(userTicket);
            voucher.__write(__os);
         } catch (LocalException var19) {
            __og.abort(var19);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var17) {
                  throw var17;
               } catch (UserException var18) {
                  throw new UnknownUserException(var18.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            WebServiceResponse __ret = new WebServiceResponse();
            __ret.__read(__is);
            __is.endReadEncaps();
            var9 = __ret;
         } catch (LocalException var20) {
            throw new LocalExceptionWrapper(var20, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var9;
   }

   public WebServiceResponse getAccountStatus(String liveid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("getAccountStatus", OperationMode.Normal, __ctx);

      WebServiceResponse var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(liveid);
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
            WebServiceResponse __ret = new WebServiceResponse();
            __ret.__read(__is);
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

   public WebServiceResponse registerAccount(String username, String password, int countryCode, String mobileNumber, int secretQuestionCode, String secretQuestionAnswer, String firstName, String lastName, String nickName, String dateOfBirth, String sex, String emailAddress, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("registerAccount", OperationMode.Normal, __ctx);

      WebServiceResponse var18;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeString(password);
            __os.writeInt(countryCode);
            __os.writeString(mobileNumber);
            __os.writeInt(secretQuestionCode);
            __os.writeString(secretQuestionAnswer);
            __os.writeString(firstName);
            __os.writeString(lastName);
            __os.writeString(nickName);
            __os.writeString(dateOfBirth);
            __os.writeString(sex);
            __os.writeString(emailAddress);
         } catch (LocalException var28) {
            __og.abort(var28);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var26) {
                  throw var26;
               } catch (UserException var27) {
                  throw new UnknownUserException(var27.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            WebServiceResponse __ret = new WebServiceResponse();
            __ret.__read(__is);
            __is.endReadEncaps();
            var18 = __ret;
         } catch (LocalException var29) {
            throw new LocalExceptionWrapper(var29, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var18;
   }
}
