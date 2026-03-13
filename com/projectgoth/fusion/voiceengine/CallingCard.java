package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VoucherData;
import java.io.IOException;
import java.rmi.RemoteException;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class CallingCard {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(CallingCard.class));
   protected static final int INPUT_TIME_SEC = 7;
   protected static final int OPT_TIME_SEC = 3;
   protected static final int VOUCHER_MIN_LEN = 4;
   protected static final int VOUCHER_MAX_LEN = 32;
   protected static final int PHONE_MIN_LEN = 5;
   protected static final int PHONE_MAX_LEN = 32;
   protected static final int RETRIES_MAX = 3;
   protected static final int RETRIES_HELP = 2;
   protected static final String DEF_ASTERISK_LANG = "mig33/en/allison";
   protected static final String DEF_ASTERISK_CURR = "";
   protected static final int RES_DONE = 1;
   protected static final int RES_CONTINUE = 0;
   protected static final int RES_ERROR = -1;
   protected static final int RES_TIMEOUT = -2;
   protected static final int RES_HANGUP = -2;
   protected LogicHelper helper = null;
   protected FastAGIWorker worker = null;
   protected FastAGICommand command = null;
   protected CallMakerI callMaker = null;
   protected ExtendedControl control = null;
   protected UserData userData = null;
   protected VoucherData voucherData = null;
   protected String callerId = null;
   protected String callerIdName = null;
   protected String callerDid = null;

   public CallingCard(LogicHelper helper, ExtendedControl control) {
      this.helper = helper;
      this.control = control;
      if (this.helper != null) {
         this.worker = helper.getWorker();
         this.command = helper.getCommand();
         this.callMaker = helper.getCallMaker();
      }

   }

   protected boolean isPhoneLenValid(String phone) {
      return phone != null && phone.length() >= 5 && phone.length() <= 32;
   }

   protected boolean isCallerIdLenValid(String phone) {
      return phone != null && phone.length() >= 5 && phone.length() <= 32;
   }

   protected boolean isVoucherLenValid(String pin) {
      return pin != null && pin.length() >= 4 && pin.length() <= 32;
   }

   public boolean processPlaceCall(CallRequest request) throws CreateException, RemoteException, IOException {
      boolean ok = false;
      if (this.userData != null && request != null && request.getDestination() != null) {
         boolean var13;
         try {
            CallData.TypeEnum callType = request.getCallData().type;
            boolean var22;
            switch(callType) {
            case MIDLET_CALL_THROUGH:
            case DIRECT_CALL_THROUGH:
               request = this.helper.initiateCallRequest(request);
               break;
            case MISSED_CALL_CALLBACK:
               request = this.helper.updateCallRequest(request);
               break;
            default:
               log.warn("Invalid call type " + callType.toString());
               var22 = false;
               return var22;
            }

            if (request == null || request.getDialCommand() == null || request.getDialCommand().length() < 1 || request.getLimitDuration() < 1L) {
               var22 = false;
               return var22;
            }

            log.info("Placing call to '" + request.getDestination() + "' by username '" + this.userData.username + "'");
            String dialCommand = request.getDialCommand();
            int dialDuration = 40;
            long limitDuration = request.getLimitDuration();
            long limitTimeoutWarning = request.getLimitTimeoutWarning();
            long limitTimeoutRepeat = request.getLimitTimeoutRepeat();
            this.helper.processBillingStart(this.userData, request.getDestination());
            int status = this.control.dialOptions(dialCommand, dialDuration, "goL(" + limitDuration + ":" + limitTimeoutWarning + ":" + limitTimeoutRepeat + ")");
            if (status != 5 && status != 0) {
               log.info("Call was NOT answered (code = 'unknown(" + status + ")')");
               ok = false;
            } else {
               log.info("Call was answered (code = 'answer(" + status + ")')");
               ok = true;
            }

            var13 = ok;
         } catch (CreateException var19) {
            throw var19;
         } catch (RemoteException var20) {
            return false;
         } finally {
            if (!ok) {
               log.info("Cancelling call and billing");
               if (null != request) {
                  this.helper.processBillingCancel(this.userData, request.getDestination());
               }
            } else {
               log.info("Ending the call and billing");
               if (null != request) {
                  this.helper.processBillingEnd(this.userData, request.getDestination());
               }
            }

         }

         return var13;
      } else {
         log.info("User is not registered or missing phone number");
         return false;
      }
   }

   public boolean playCongestion() {
      log.info("Playing congestion tone");
      this.control.playCongestion(1);
      return true;
   }

   public int handleAuthMenu() throws CreateException, RemoteException, IOException {
      int res = 0;
      String input = null;
      int count = 0;
      log.info("Handling menu 'authMenu'");
      if (this.userData != null) {
         log.info("User must NOT be registered to continue");
         return 0;
      } else {
         try {
            while(this.control.isConnected()) {
               input = this.control.readInput("ct0012", 32, 7);
               if (input != null && input.length() > 0) {
                  log.info("User entered input '" + input + "'");
                  if (this.isVoucherLenValid(input)) {
                     this.voucherData = this.helper.getVoucher(input);
                     if (this.voucherData != null && this.voucherData.status == VoucherData.StatusEnum.REDEEMED) {
                        log.info("Success valid voucher number '" + input + "'");
                        log.info("Loading user from voucher number (voucher already redeemed)");
                        this.userData = this.helper.loadUserFromVoucherNumber(this.voucherData.number);
                        if (this.userData != null) {
                           res = 1;
                           break;
                        }

                        log.info("Could not load user from voucher number");
                     } else {
                        if (this.voucherData != null && this.voucherData.status == VoucherData.StatusEnum.ACTIVE) {
                           log.info("Success valid voucher number '" + input + "'");
                           res = 1;
                           break;
                        }

                        log.info("Could not find voucher number '" + input + "'");
                     }
                  } else {
                     log.info("Voucher number '" + input + "' appears invalid");
                  }

                  log.info("User entered an unavailable option or invalid data");
                  this.control.playback("ct0007");
                  if (count < 2) {
                     this.control.playback("ct0024");
                  }
               } else {
                  log.info("User timed out when entering digit");
               }

               log.info("User has '" + (3 - count - 1) + "' retry before max retries");
               ++count;
               if (count >= 3) {
                  res = -2;
                  break;
               }

               if (count >= 2) {
                  this.control.playback("");
               }
            }

            if (res == 1 || res == 0) {
               if (this.voucherData != null && this.voucherData.status == VoucherData.StatusEnum.REDEEMED) {
                  log.info("Taking user to main menu (voucher already redeemed)");
               } else if (this.voucherData != null && this.voucherData.status == VoucherData.StatusEnum.ACTIVE) {
                  log.info("Taking user to registration (voucher not redeemed)");
                  int res = this.handleRegisterMenu();
                  if (res != 1 && res != 0) {
                     res = -1;
                  } else {
                     res = 1;
                  }
               } else {
                  log.info("Voucher number is not valid or user has exited");
               }
            }

            return this.control.isConnected() ? res : -2;
         } catch (CreateException var5) {
            throw var5;
         } catch (RemoteException var6) {
            return -1;
         }
      }
   }

   public int handleRegisterMenu() throws CreateException, RemoteException, IOException {
      int res = 0;
      String digit = null;
      int count = 0;
      log.info("Handling menu 'registerMenu'");
      if (this.userData == null && this.voucherData != null) {
         try {
            while(this.control.isConnected()) {
               digit = this.control.readInput("ct0022", 1, 3);
               if (digit != null && digit.length() > 0) {
                  log.info("User entered input '" + digit + "'");
                  UserData userDataNew;
                  if (digit.equals("1")) {
                     this.userData = this.helper.createUserFromVoucher(this.voucherData.number, this.callerDid);
                     if (this.userData != null) {
                        log.info("Created user from voucher number '" + this.voucherData.number + "'");
                        userDataNew = this.helper.updateUserMobilePhone(this.userData, this.callerId);
                        if (!this.isCallerIdLenValid(this.callerId)) {
                           log.info("Updated user mobile phone to '" + this.callerId + "' (PIN-less)");
                           log.info("Caller id is required to continue");
                           this.control.playback("ct0009");
                           res = 0;
                           break;
                        }

                        if (userDataNew != null) {
                           log.info("Success updating mobile phone to '" + this.callerId + "' (PIN-less)");
                           this.userData = userDataNew;
                           this.control.playback("ct0016");
                           res = 1;
                           break;
                        }

                        log.info("Failed updating mobile phone to '" + this.callerId + "' (PIN-less)");
                     } else {
                        log.info("Could not create user from voucher number '" + this.voucherData.number + "'");
                     }

                     this.control.playback("ct0015");
                     res = -1;
                     break;
                  }

                  if (digit.equals("2")) {
                     this.userData = this.helper.createUserFromVoucher(this.voucherData.number, this.callerDid);
                     if (this.userData != null) {
                        log.info("Created user from voucher number '" + this.voucherData.number + "'");
                        userDataNew = this.helper.updateUserMobilePhone(this.userData, "");
                        if (userDataNew != null) {
                           log.info("Success updating mobile phone to '' (PIN-full)");
                           this.userData = userDataNew;
                           this.control.playback("ct0017");
                           res = 1;
                           break;
                        }

                        log.info("Failed updating mobile phone to '' (PIN-full)");
                     } else {
                        log.info("Could not create user from voucher number '" + this.voucherData.number + "'");
                     }

                     this.control.playback("ct0015");
                     res = -1;
                     break;
                  }

                  log.info("User entered an unavailable option or invalid data");
                  this.control.playback("ct0023");
                  if (count < 2) {
                     this.control.playback("ct0024");
                  }
               } else {
                  log.info("User timed out when entering digit");
               }

               log.info("User has '" + (3 - count - 1) + "' retry before max retries");
               ++count;
               if (count >= 3) {
                  res = -2;
                  break;
               }

               if (count >= 2) {
                  this.control.playback("");
               }
            }

            return this.control.isConnected() ? res : -2;
         } catch (CreateException var5) {
            throw var5;
         } catch (RemoteException var6) {
            return -1;
         }
      } else {
         log.info("User must NOT be registered with a valid voucher number to continue");
         return 0;
      }
   }

   public int handleMainMenu(CallRequest request) throws CreateException, RemoteException, IOException {
      int res = 0;
      String input = null;
      int count = 0;
      log.info("Handling menu 'mainMenu'");
      if (this.userData == null) {
         log.info("User must be registered to continue");
         this.control.playback("ct0019");
         return 0;
      } else {
         try {
            if (request.callData.type != CallData.TypeEnum.MISSED_CALL_CALLBACK) {
               AccountBalanceData balanceData = this.helper.getBalance(this.userData);
               this.control.sayBalance(balanceData.balance, balanceData.currency == null ? "" : balanceData.currency.code);
            }

            while(this.control.isConnected()) {
               input = this.control.readInputExit("ct0011", "*", 32, 7);
               if (input != null && input.length() > 0) {
                  log.info("User entered input '" + input + "'");
                  int res;
                  if (input.equals("*")) {
                     if (request.callData.type == CallData.TypeEnum.MISSED_CALL_CALLBACK) {
                        res = 0;
                        count = 0;
                        continue;
                     }

                     res = this.handleAccountMenu();
                     if (res != 1 && res != 0) {
                        res = -1;
                        break;
                     }

                     res = 0;
                     count = 0;
                     continue;
                  }

                  if (this.isPhoneLenValid(input)) {
                     request.setDestination(this.helper.cleanPhoneNumber(input));
                     res = this.handleCallMenu(request);
                     if (res == 1) {
                        res = 1;
                        break;
                     }

                     if (res == 0) {
                        res = 0;
                        count = 0;
                        continue;
                     }

                     res = -1;
                     break;
                  }

                  log.info("User entered an unavailable option or invalid data");
                  this.control.playback("ct0026");
                  if (count < 2) {
                     this.control.playback("ct0024");
                  }
               } else {
                  log.info("User timed out when entering digit");
               }

               log.info("User has '" + (3 - count - 1) + "' retry before max retries");
               ++count;
               if (count >= 3) {
                  res = -2;
                  break;
               }

               if (count >= 2) {
                  this.control.playback("");
               }
            }

            return this.control.isConnected() ? res : -2;
         } catch (CreateException var6) {
            throw var6;
         } catch (RemoteException var7) {
            return -1;
         }
      }
   }

   public int handleAccountMenu() throws CreateException, RemoteException, IOException {
      int res = 0;
      String digit = null;
      int count = 0;
      log.info("Handling menu 'accountMenu'");

      try {
         while(this.control.isConnected()) {
            digit = this.control.readInput("ct0003", 1, 3);
            if (digit != null && digit.length() > 0) {
               log.info("User entered input '" + digit + "'");
               AccountBalanceData balanceData;
               int res;
               if (digit.equals("1")) {
                  if (this.userData == null) {
                     log.info("User must be registered to continue");
                     this.control.playback("ct0019");
                     res = 0;
                  } else {
                     res = this.handleRechargeMenu();
                     if (res == 1) {
                        balanceData = this.helper.getBalance(this.userData);
                        this.control.sayBalance(balanceData.balance, balanceData.currency == null ? "" : balanceData.currency.code);
                        res = 0;
                     } else if (res == 0) {
                        res = 0;
                     } else {
                        res = -1;
                     }
                  }
                  break;
               }

               if (digit.equals("2")) {
                  if (this.userData == null) {
                     log.info("User must be registered to continue");
                     this.control.playback("ct0019");
                     res = 0;
                  } else if (!this.isCallerIdLenValid(this.callerId)) {
                     log.info("Caller id is required to continue");
                     this.control.playback("ct0009");
                     res = 0;
                  } else {
                     res = this.handlePINToggleMenu();
                     if (res != 1 && res != 0) {
                        res = -1;
                     } else {
                        res = 0;
                     }
                  }
                  break;
               }

               if (digit.equals("3")) {
                  if (this.userData == null) {
                     log.info("User must be registered to continue");
                     this.control.playback("ct0019");
                     res = 0;
                  } else {
                     balanceData = this.helper.getBalance(this.userData);
                     this.control.sayBalance(balanceData.balance, balanceData.currency == null ? "" : balanceData.currency.code);
                     res = 0;
                  }
                  break;
               }

               if (digit.equals("4")) {
                  this.control.playback("ct0029");
                  res = 0;
                  break;
               }

               if (digit.equals("0")) {
                  res = 0;
                  break;
               }

               log.info("User entered an unavailable option or invalid data");
               this.control.playback("ct0023");
               if (count < 2) {
                  this.control.playback("ct0024");
               }
            } else {
               log.info("User timed out when entering digit");
            }

            log.info("User has '" + (3 - count - 1) + "' retry before max retries");
            ++count;
            if (count >= 3) {
               res = -2;
               break;
            }

            if (count >= 2) {
               this.control.playback("");
            }
         }

         return this.control.isConnected() ? res : -2;
      } catch (CreateException var5) {
         throw var5;
      } catch (RemoteException var6) {
         return -1;
      }
   }

   public int handleRechargeMenu() throws CreateException, RemoteException, IOException {
      int res = 0;
      String input = null;
      int count = 0;
      log.info("Handling menu 'rechargeMenu'");
      if (this.userData == null) {
         log.info("User must be registered to continue");
         this.control.playback("ct0019");
         return 0;
      } else {
         try {
            while(this.control.isConnected()) {
               input = this.control.readInputExit("ct0005", "0", 32, 7);
               if (input != null && input.length() > 0) {
                  log.info("User entered input '" + input + "'");
                  if (input.equals("0")) {
                     res = 0;
                     break;
                  }

                  if (this.isVoucherLenValid(input)) {
                     this.voucherData = this.helper.getVoucher(input);
                     if (this.voucherData != null) {
                        VoucherData voucherDataNew = this.helper.redeemVoucher(this.userData.username, input);
                        if (voucherDataNew != null) {
                           log.info("Redeemed voucher number '" + input + "'");
                           this.control.playback("ct0025");
                           res = 1;
                        } else {
                           log.info("Could not redeem voucher number '" + input + "'");
                           this.control.playback("ct0008");
                           res = 1;
                        }
                        break;
                     }

                     log.info("Could not find voucher number '" + input + "'");
                  } else {
                     log.info("Voucher number '" + input + "' appears invalid");
                  }

                  log.info("User entered an unavailable option or invalid data");
                  this.control.playback("ct0007");
                  if (count < 2) {
                     this.control.playback("ct0024");
                  }
               } else {
                  log.info("User timed out when entering digit");
               }

               log.info("User has '" + (3 - count - 1) + "' retry before max retries");
               ++count;
               if (count >= 3) {
                  res = -2;
                  break;
               }

               if (count >= 2) {
                  this.control.playback("");
               }
            }

            return this.control.isConnected() ? res : -2;
         } catch (CreateException var5) {
            throw var5;
         } catch (RemoteException var6) {
            return -1;
         }
      }
   }

   public int handlePINToggleMenu() throws CreateException, RemoteException, IOException {
      int res = 0;
      String digit = null;
      int count = 0;
      log.info("Handling menu 'pinToggleMenu'");
      if (this.userData == null) {
         log.info("User must be registered to continue");
         this.control.playback("ct0019");
         return 0;
      } else if (!this.isCallerIdLenValid(this.callerId)) {
         log.info("Caller id is required to continue");
         this.control.playback("ct0009");
         return 0;
      } else {
         try {
            int res;
            if (this.userData.mobilePhone != null && this.userData.mobilePhone.length() >= 1) {
               log.info("User has mobile phone, taking first route");

               while(this.control.isConnected()) {
                  digit = this.control.readInput("ct0010", 1, 3);
                  if (digit != null && digit.length() > 0) {
                     log.info("User entered input '" + digit + "'");
                     if (digit.equals("1")) {
                        res = this.handlePINToggleSubMenu();
                        if (res != 1 && res != 0) {
                           res = -1;
                        } else {
                           res = 0;
                        }
                        break;
                     }

                     if (digit.equals("2")) {
                        if (this.userData.type != UserData.TypeEnum.MIG33_PREPAID_CARD) {
                           log.info("User '" + this.userData.username + "' is not a prepaid card caller; can't unregister number");
                           this.control.playback("ct0038");
                           res = 0;
                        } else {
                           UserData userDataNew = this.helper.updateUserMobilePhone(this.userData, "");
                           if (userDataNew != null) {
                              log.info("Updated user mobile phone to '' (PIN-full)");
                              this.userData = userDataNew;
                              this.control.playback("ct0017");
                              res = 0;
                           } else {
                              log.info("Could not update user mobile phone to '' (PIN-full)");
                              this.control.playback("ct0015");
                              res = 0;
                           }
                        }
                        break;
                     }

                     if (digit.equals("0")) {
                        res = 0;
                        break;
                     }

                     log.info("User entered an unavailable option or invalid data");
                     this.control.playback("ct0023");
                     if (count < 2) {
                        this.control.playback("ct0024");
                     }
                  } else {
                     log.info("User timed out when entering digit");
                  }

                  log.info("User has '" + (3 - count - 1) + "' retry before max retries");
                  ++count;
                  if (count >= 3) {
                     res = -2;
                     break;
                  }

                  if (count >= 2) {
                     this.control.playback("");
                  }
               }

               return this.control.isConnected() ? res : -2;
            } else {
               log.info("User does not have mobile phone, taking second route");
               res = this.handlePINToggleSubMenu();
               if (res != 1 && res != 0) {
                  res = -1;
                  return res;
               } else {
                  int res = 0;
                  return res;
               }
            }
         } catch (CreateException var5) {
            throw var5;
         } catch (RemoteException var6) {
            return -1;
         }
      }
   }

   public int handlePINToggleSubMenu() throws CreateException, RemoteException, IOException {
      int res = 0;
      String digit = null;
      int count = 0;
      log.info("Handling menu 'pinToggleSubMenu'");
      if (this.userData == null) {
         log.info("User must be registered to continue");
         this.control.playback("ct0019");
         return 0;
      } else if (!this.isCallerIdLenValid(this.callerId)) {
         log.info("Caller id is required to continue");
         this.control.playback("ct0009");
         return 0;
      } else {
         try {
            if (this.userData.mobilePhone != null && this.userData.mobilePhone.length() >= 1) {
               this.control.playback("ct0004");
               this.control.sayDigits(this.callerId);
            } else {
               this.control.playback("ct0018");
               this.control.sayDigits(this.callerId);
            }

            while(this.control.isConnected()) {
               digit = this.control.readInput("ct0032", 1, 3);
               if (digit != null && digit.length() > 0) {
                  log.info("User entered input '" + digit + "'");
                  if (digit.equals("1")) {
                     UserData userDataNew = this.helper.updateUserMobilePhone(this.userData, this.callerId);
                     if (!this.isCallerIdLenValid(this.callerId)) {
                        log.info("Updated user mobile phone to '" + this.callerId + "' (PIN-less)");
                        log.info("Caller id is required to continue");
                        this.control.playback("ct0009");
                        res = 0;
                     } else if (userDataNew != null) {
                        log.info("Updated user mobile phone to '" + this.callerId + "' (PIN-less)");
                        this.userData = userDataNew;
                        this.control.playback("ct0016");
                        res = 0;
                     } else {
                        log.info("Could not update user mobile phone to '" + this.callerId + "' (PIN-less)");
                        this.control.playback("ct0015");
                        res = 0;
                     }
                     break;
                  }

                  if (digit.equals("0")) {
                     res = 0;
                     break;
                  }

                  log.info("User entered an unavailable option or invalid data");
                  this.control.playback("ct0023");
                  if (count < 2) {
                     this.control.playback("ct0024");
                  }
               } else {
                  log.info("User timed out when entering digit");
               }

               log.info("User has '" + (3 - count - 1) + "' retry before max retries");
               ++count;
               if (count >= 3) {
                  res = -2;
                  break;
               }

               if (count >= 2) {
                  this.control.playback("");
               }
            }

            return this.control.isConnected() ? res : -2;
         } catch (CreateException var5) {
            throw var5;
         } catch (RemoteException var6) {
            return -1;
         }
      }
   }

   public int handleCallMenu(CallRequest request) throws CreateException, RemoteException, IOException {
      int res = 0;
      String digit = null;
      int count = 0;
      log.info("Handling menu 'callMenu'");
      if (this.userData == null) {
         log.info("User must be registered to continue");
         this.control.playback("ct0019");
         return 0;
      } else {
         try {
            request = this.helper.evaluateCallRequest(request);
            if (request == null) {
               this.control.playback("ct0030");
               res = -1;
               return this.control.isConnected() ? res : -2;
            } else {
               log.info("User's call request: dialcommand='" + request.getDialCommand() + "', " + "limitdur='" + request.getLimitDuration() + "', " + "limitrate='" + request.getLimitRate() + "', " + "limitwarn='" + request.getLimitTimeoutWarning() + "', " + "limitrep='" + request.getLimitTimeoutRepeat() + "'");
               if (request.limitDuration < 1L) {
                  log.info("User's balance/limit is too low or call restricted (balance too low)");
                  this.control.playback("ct0031");
                  return -1;
               } else if (request.getCallData().type != CallData.TypeEnum.MIDLET_CALL_THROUGH && request.getCallData().type != CallData.TypeEnum.MISSED_CALL_CALLBACK) {
                  this.control.sayLimit(request.limitDuration / 1000L);

                  while(this.control.isConnected()) {
                     digit = this.control.readInput("ct0014", 1, 3);
                     if (digit != null && digit.length() > 0) {
                        log.info("User entered input '" + digit + "'");
                        if (digit.equals("0")) {
                           res = 0;
                           break;
                        }

                        if (digit.equals("1")) {
                           this.control.playback("ct0034");
                           if (this.processPlaceCall(request)) {
                              log.info("Success placing the call to destination '" + request.getDestination() + "'");
                              res = 1;
                           } else {
                              log.info("Failure placing the call to destination '" + request.getDestination() + "'");
                              this.control.playback("ct0030");
                              res = -1;
                           }
                           break;
                        }

                        log.info("User entered an unavailable option or invalid data");
                        this.control.playback("ct0023");
                        if (count < 2) {
                           this.control.playback("ct0024");
                        }
                     } else {
                        log.info("User timed out when entering digit");
                     }

                     log.info("User has '" + (3 - count - 1) + "' retry before max retries");
                     ++count;
                     if (count >= 3) {
                        res = -2;
                        break;
                     }

                     if (count >= 2) {
                        this.control.playback("");
                     }
                  }

                  return this.control.isConnected() ? res : -2;
               } else {
                  this.control.playback("ct0034");
                  if (this.processPlaceCall(request)) {
                     log.info("Success placing the call to destination '" + request.getDestination() + "'");
                     int res = 1;
                     return this.control.isConnected() ? res : -2;
                  } else {
                     log.info("Failure placing the call to destination '" + request.getDestination() + "'");
                     this.control.playback("ct0030");
                     res = -1;
                     return this.control.isConnected() ? res : -2;
                  }
               }
            }
         } catch (CreateException var6) {
            throw var6;
         } catch (RemoteException var7) {
            return -1;
         }
      }
   }

   public void execute() throws CreateException, RemoteException, IOException {
      boolean res = false;

      try {
         log.info("Starting calling card system");
         this.control.answer();
         this.control.wait(1);
         this.callerId = this.control.getAsteriskCallerId();
         this.callerIdName = this.control.getAsteriskCallerIdName();
         this.callerDid = this.control.getAsteriskCallerDid();
         if ("115409019".equals(this.callerDid)) {
            this.callerDid = "27115409019";
         }

         this.control.setAsteriskLanguage("mig33/en/allison");
         this.control.setAsteriskCurrency("");
         if (this.callerId == null || this.callerId.length() < 1 || this.callerId.equals("0")) {
            this.callerId = null;
         }

         if (this.callerDid == null || this.callerDid.length() < 1) {
            this.callerDid = null;
         }

         String oldCallerId = this.callerId;
         String oldCallerIdName = this.callerIdName;
         String oldCallerDid = this.callerDid;

         try {
            this.callerDid = this.helper.cleanPhoneNumber(this.callerDid);
            if (this.callerId != null && this.callerId.length() > 0) {
               this.callerId = this.helper.fixCallerCountryCode(this.callerId, this.callerDid);
               this.callerId = this.helper.fixValidateNumber(this.callerId);
            }

            this.callerIdName = this.callerId;
            this.control.setAsteriskCallerId(this.callerId == null ? "" : this.callerId);
            this.control.setAsteriskCallerIdName(this.callerIdName == null ? "" : this.callerIdName);
            this.control.setAsteriskCallerDid(this.callerDid == null ? "" : this.callerDid);
         } catch (CreateException var32) {
            throw var32;
         } catch (RemoteException var33) {
         } catch (IOException var34) {
         }

         String astCallerId = this.control.getAsteriskCallerId();
         String astCallerIdName = this.control.getAsteriskCallerIdName();
         String astCallerDid = this.control.getAsteriskCallerDid();
         log.info("CallerDID was '" + oldCallerDid + "', but is now fixed to '" + this.callerDid + "'");
         log.info("CallerID was '" + oldCallerId + "', but is now fixed to '" + this.callerId + "'");
         log.info("CallerIDName was '" + oldCallerIdName + "', but is now fixed to '" + this.callerIdName + "'");
         log.info("Asterisk CallerDID is set to '" + astCallerDid + "'");
         log.info("Asterisk CallerID is set to '" + astCallerId + "'");
         log.info("Asterisk CallerIDName is set to '" + astCallerIdName + "'");

         try {
            if (this.callerId != null && this.callerId.length() > 0) {
               log.info("User is calling from CID '" + this.callerId + "' on DID '" + this.callerDid + "'");
               this.userData = this.helper.loadUserFromMobilePhone(this.callerId);
               if (this.userData != null) {
                  log.info("Found username '" + this.userData.username + "' with that caller id");
               }
            }
         } catch (CreateException var29) {
            throw var29;
         } catch (RemoteException var30) {
         } catch (IOException var31) {
         }

         if (this.control.isConnected()) {
            int res = 0;
            if (this.userData != null) {
               boolean isAniCallback = "1".equals(this.command.getParameter("ani_callback"));
               CallRequest request;
               if (isAniCallback) {
                  log.info("Missed call callback from registered user '" + this.userData.username + "'");
                  request = this.helper.createMissedCallCallbackRequest(this.userData, this.callerId, this.command.getParameterAsInt("leg_a_carrier_id"));
                  request = this.helper.initiateCallRequest(request);
                  this.control.playback("ct0020");
                  res = this.handleMainMenu(request);
               } else {
                  request = null;

                  try {
                     request = this.helper.getPendingMidletRequest(this.userData, this.callerDid);
                  } catch (CreateException var26) {
                     throw var26;
                  } catch (RemoteException var27) {
                  } catch (IOException var28) {
                  }

                  if (request == null) {
                     log.info("Found registered user '" + this.userData.username + "'");
                     this.control.playback("ct0020");
                     request = this.helper.createDirectCallThroughRequest(this.userData, this.callerId, this.callerDid);
                     res = this.handleMainMenu(request);
                  } else {
                     log.info("Found midlet request for '" + this.userData.username + "'");
                     this.handleCallMenu(request);
                     res = 1;
                  }
               }
            } else {
               log.info("Could not find registered user");
               this.control.playback("ct0020");
            }

            if (res == 0) {
               res = false;
               res = this.handleAuthMenu();
               if (res != 1 && res != 0) {
                  this.control.playback("ct0006", 4);
               } else {
                  res = false;
                  if (this.userData != null) {
                     CallRequest request = this.helper.createDirectCallThroughRequest(this.userData, this.callerId, this.callerDid);
                     this.handleMainMenu(request);
                  }

                  this.control.playback("ct0006", 4);
               }
            } else {
               this.control.playback("ct0006", 4);
            }
         }
      } catch (CreateException var35) {
         log.warn("Error running the calling card system (ex1); " + var35.getMessage());
      } catch (RemoteException var36) {
         log.warn("Error running the calling card system (ex2); " + var36.getMessage());
      } catch (IOException var37) {
         log.warn("Error running the calling card system (ex3); " + var37.getMessage());
      } finally {
         this.control.wait(1);
         this.control.hangup();
         log.info("Completed the calling card system");
      }

   }
}
