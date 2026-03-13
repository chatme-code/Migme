package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.bl1.BlueLabelResponseCodes;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BlueLabelVoucherData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.VoucherBatchData;
import com.projectgoth.fusion.data.VoucherData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.interfaces.Account;
import com.projectgoth.fusion.interfaces.AccountHome;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.interfaces.VoucherLocal;
import com.projectgoth.fusion.interfaces.VoucherLocalHome;
import com.projectgoth.fusion.merchant.VoucherIssuanceUtility;
import com.projectgoth.fusion.recommendation.collector.DataCollectorUtils;
import com.projectgoth.fusion.slice.BlueLabelOneVoucher;
import com.projectgoth.fusion.slice.BlueLabelServicePrx;
import com.projectgoth.fusion.slice.WebServiceResponse;
import com.sun.rowset.CachedRowSetImpl;
import java.io.IOException;
import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.sql.DataSource;
import org.apache.log4j.Logger;

public class VoucherBean implements SessionBean {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(VoucherBean.class));
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private SessionContext context;

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create Voucher EJB", var2);
         throw new CreateException("Unable to create Voucher EJB: " + var2.getMessage());
      }
   }

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public int createVoucherBatch(String username, String currency, String amountStr, int numVoucher, String notes, boolean initiallyInactive, AccountEntrySourceData accountEntrySourceData) throws EJBException, FusionEJBException {
      if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher.CREATE_BATCH_ENABLED)) {
         log.info(String.format("[DISABLED] Failed to create voucher batch for [%s] :: %s %s, %s vouchers", username, currency, amountStr, numVoucher));
         throw new FusionEJBException("Unable to create voucher batch. Please try again later.");
      } else {
         try {
            amountStr = amountStr.replaceAll(",", "").replaceAll(" ", "");

            try {
               if (!StringUtil.isValidMoneyFormat(amountStr)) {
                  log.info(String.format("[Invalid Amount] Failed to create voucher batch for [%s] :: %s %s, %s vouchers", username, currency, amountStr, numVoucher));
                  throw new FusionEJBException("Please enter a valid amount (e.g. 1000, 98.5, 897.94).");
               }
            } catch (IllegalArgumentException var86) {
               throw new FusionEJBException(var86.getMessage());
            }

            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.ENABLE_VOUCHER_ISSUANCE_CHECK)) {
               voucherIssuanceEntitlementCheck(username);
            }

            BigDecimal amount = new BigDecimal(amountStr);
            double minvalue = SystemProperty.getDouble("MinVoucherValue");
            Connection conn = this.dataSourceMaster.getConnection();

            int var29;
            try {
               PreparedStatement ps = conn.prepareStatement("select id from phonecall where username = ? and status = ?");

               try {
                  ps.setString(1, username);
                  ps.setInt(2, CallData.StatusEnum.IN_PROGRESS.value());
                  ResultSet rs = ps.executeQuery();

                  try {
                     if (rs.next()) {
                        throw new EJBException("You can't create vouchers when you have a call in progress");
                     }
                  } finally {
                     rs.close();
                  }
               } finally {
                  ps.close();
               }

               Account var93 = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
               AccountBalanceData userBalance = var93.getAccountBalance(username);
               double usercurrencyvalue = userBalance.currency.convert(minvalue);
               DecimalFormat df = new DecimalFormat("0.00");
               if (amount.doubleValue() < usercurrencyvalue) {
                  log.info("unable to create voucher batch, voucher value is less than minimum: amount [" + amount.doubleValue() + "] minimum [" + usercurrencyvalue + "]");
                  throw new EJBException("Each voucher must be at least " + df.format(userBalance.currency.convert(minvalue)) + " " + userBalance.currency.code);
               }

               double totalCostInVoucherCurrency = amount.doubleValue() * (double)numVoucher;
               MISLocal misEJB = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
               CurrencyData voucherCurrency = misEJB.getCurrency(currency);
               double totalCostInUserCurrency = userBalance.currency.convertFrom(amount.doubleValue() * (double)numVoucher, voucherCurrency);
               if (userBalance.balance < totalCostInUserCurrency) {
                  throw new EJBException("Total amount required exceeds account balance. You need to recharge first or create fewer vouchers");
               }

               double minBalanceAfterTransfer = userBalance.currency.convert(SystemProperty.getDouble("MinBalanceAfterTransfer"));
               if (userBalance.balance - minBalanceAfterTransfer < totalCostInUserCurrency) {
                  throw new EJBException("You must leave at least " + df.format(minBalanceAfterTransfer) + " " + userBalance.currency.code + " in your account");
               }

               PreparedStatement ps = conn.prepareStatement("insert into voucherbatch (username, datecreated, currency, amount, numvoucher, notes) values (?,?,?,?,?,?)", 1);

               int voucherBatchID;
               try {
                  ps.setString(1, username);
                  ps.setTimestamp(2, new Timestamp((new Date(System.currentTimeMillis())).getTime()));
                  ps.setString(3, currency);
                  ps.setBigDecimal(4, amount);
                  ps.setInt(5, numVoucher);
                  if (notes != null) {
                     ps.setString(6, notes);
                  }

                  ps.executeUpdate();
                  ResultSet rs = ps.getGeneratedKeys();

                  try {
                     if (!rs.next()) {
                        throw new EJBException("Failed to create Voucher Batch");
                     }

                     voucherBatchID = rs.getInt(1);
                  } finally {
                     rs.close();
                  }
               } finally {
                  ps.close();
               }

               SecureRandom random = new SecureRandom();
               PreparedStatement ps = conn.prepareStatement("insert into voucher (VoucherBatchID, Number, Status) values (?,?,?)");

               try {
                  for(int i = 0; i < numVoucher; ++i) {
                     do {
                        ps.setInt(1, voucherBatchID);
                        ps.setString(2, "" + (Math.abs(random.nextLong()) % 9000000000L + 1000000000L));
                        if (initiallyInactive) {
                           ps.setInt(3, VoucherData.StatusEnum.INACTIVE.value());
                        } else {
                           ps.setInt(3, VoucherData.StatusEnum.ACTIVE.value());
                        }
                     } while(ps.executeUpdate() != 1);
                  }
               } finally {
                  ps.close();
               }

               AccountEntryData accEntry = new AccountEntryData();
               accEntry.username = username;
               accEntry.type = AccountEntryData.TypeEnum.VOUCHERS_CREATED;
               accEntry.reference = String.valueOf(voucherBatchID);
               accEntry.description = "Voucher Creation - " + username + " created " + df.format(totalCostInVoucherCurrency) + " " + currency + " worth of vouchers";
               accEntry.amount = -totalCostInVoucherCurrency;
               accEntry.currency = currency;
               accEntry.tax = 0.0D;
               AccountLocal var98 = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
               var98.createAccountEntry((Connection)null, accEntry, accountEntrySourceData);
               var29 = voucherBatchID;
            } finally {
               conn.close();
            }

            return var29;
         } catch (Exception var91) {
            if (var91 instanceof FusionEJBException) {
               log.warn("Failed to create Voucher Batch. Exception:" + var91);
               throw (FusionEJBException)var91;
            } else if (var91 instanceof EJBException) {
               throw (EJBException)var91;
            } else if (var91 instanceof RuntimeException) {
               throw (RuntimeException)var91;
            } else {
               String errorID = DataCollectorUtils.newErrorID();
               log.error("[" + errorID + "]" + "Failed to create Voucher Batch.Exception:" + var91, var91);
               throw new EJBException("Failed to create Voucher Batch. ErrorID:[" + errorID + "]");
            }
         }
      }
   }

   private static void voucherIssuanceEntitlementCheck(String issuerUsername) throws FusionEJBException, CreateException, IOException {
      VoucherIssuanceUtility voucherUtility = VoucherIssuanceUtility.getInstance();
      if (voucherUtility.userTypeCheckEnabled()) {
         UserLocal user = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = user.loadUser(issuerUsername, false, false);
         UserData.TypeEnum userType = userData.type;
         if (!voucherUtility.userTypeAllowedToIssueVoucher(userType)) {
            throw new FusionEJBException(voucherUtility.getDisallowedUserTypeErrorMessage(userType));
         }
      }

      if (voucherUtility.migLevelCheckEnabled() && getMigLevel(issuerUsername) < voucherUtility.getMinMigLevel()) {
         throw new FusionEJBException(voucherUtility.getInsufficientMigLevelErrorMessage());
      }
   }

   private static int getMigLevel(String username) throws CreateException, EJBException {
      UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
      boolean skipCachedScore = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Issuance.SKIP_FETCH_CACHED_REPUTATION_SCORE);
      ReputationLevelData reputation = userBean.getReputationLevel(username, skipCachedScore);
      int senderReputationLevel = reputation != null && reputation.level != null ? reputation.level : 1;
      return senderReputationLevel;
   }

   public VoucherBatchData getVoucherBatch(int voucherBatchID) throws FusionEJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      VoucherBatchData var5;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("SELECT * FROM voucherbatch WHERE id = ?");
         ps.setInt(1, voucherBatchID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Voucher batch " + voucherBatchID + "does not exist.");
         }

         var5 = new VoucherBatchData(rs);
      } catch (SQLException var20) {
         log.error("Unable to retrieve voucher batch", var20);
         throw new FusionEJBException("Unable to retrieve voucher batch with id " + voucherBatchID);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var19) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var17) {
            conn = null;
         }

      }

      return var5;
   }

   public void cancelVoucherBatch(String username, int voucherBatchId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from voucherbatch where username = ? and id = ?");
         ps.setString(1, username);
         ps.setInt(2, voucherBatchId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("You can't cancel vouchers that were not created by you");
         }

         VoucherBatchData vbEntry = new VoucherBatchData(rs);
         ps = conn.prepareStatement("update voucher set status = ?, lastupdated = ? where VoucherBatchID = ? and status in (?, ?)");
         ps.setInt(1, VoucherData.StatusEnum.CANCELLED.value());
         ps.setTimestamp(2, new Timestamp((new Date(System.currentTimeMillis())).getTime()));
         ps.setInt(3, voucherBatchId);
         ps.setInt(4, VoucherData.StatusEnum.ACTIVE.value());
         ps.setInt(5, VoucherData.StatusEnum.INACTIVE.value());
         int numRowsUpdated = ps.executeUpdate();
         if (numRowsUpdated < 1) {
            throw new EJBException("The status of these vouchers are already cancelled");
         }

         ps = conn.prepareStatement("update voucherbatch set notes = ? where ID = ?");
         if (vbEntry.notes == null) {
            ps.setString(1, "CANCELLED BATCH ");
         } else {
            ps.setString(1, vbEntry.notes + " | CANCELLED BATCH ");
         }

         ps.setInt(2, vbEntry.id);
         if (ps.executeUpdate() < 1) {
            throw new EJBException("Could not update notes field");
         }

         double amountToRefund = (double)numRowsUpdated * vbEntry.amount;
         Account accEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         AccountEntryData oldEntry = accEJB.getAccountEntryFromReference(AccountEntryData.TypeEnum.VOUCHERS_CREATED, vbEntry.id.toString());
         if (oldEntry == null) {
            throw new EJBException("Could not retrieve original account entry for batch creation");
         }

         AccountEntryData accEntry = new AccountEntryData();
         DecimalFormat df = new DecimalFormat("0.00");
         accEntry.username = username;
         accEntry.type = AccountEntryData.TypeEnum.VOUCHERS_CANCELLED;
         accEntry.reference = String.valueOf(vbEntry.id);
         accEntry.description = "Voucher Batch Cancellation - " + username + " refunded for " + df.format(amountToRefund) + " " + vbEntry.currency + " worth of vouchers";
         accEntry.amount = amountToRefund;
         accEntry.fundedAmount = accEntry.amount * oldEntry.fundedAmount / oldEntry.amount;
         accEntry.currency = vbEntry.currency;
         accEntry.tax = 0.0D;
         accEJB.createAccountEntry((Connection)null, accEntry, accountEntrySourceData);
      } catch (Exception var29) {
         throw new EJBException("Failed to cancel Voucher Batch: " + var29.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var28) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var27) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var26) {
            conn = null;
         }

      }

   }

   public void cancelVoucher(String username, int voucherId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from voucher where id = ?");
         ps.setInt(1, voucherId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid Voucher ID");
         }

         VoucherData vEntry = new VoucherData(rs);
         ps = conn.prepareStatement("select * from voucherbatch where id = ?");
         ps.setInt(1, vEntry.voucherBatchID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Internal Error");
         }

         VoucherBatchData vbEntry = new VoucherBatchData(rs);
         if (!vbEntry.username.equalsIgnoreCase(username)) {
            throw new EJBException("You can't cancel a voucher that was not created by you");
         }

         ps = conn.prepareStatement("update voucher set status = ?, LastUpdated = ? where ID = ? and status in (?,?)");
         ps.setInt(1, VoucherData.StatusEnum.CANCELLED.value());
         ps.setTimestamp(2, new Timestamp((new Date(System.currentTimeMillis())).getTime()));
         ps.setInt(3, vEntry.id);
         ps.setInt(4, VoucherData.StatusEnum.ACTIVE.value());
         ps.setInt(5, VoucherData.StatusEnum.INACTIVE.value());
         int numRowsUpdated = ps.executeUpdate();
         if (numRowsUpdated < 1) {
            throw new EJBException("The voucher has already been cancelled");
         }

         double amountToRefund = vbEntry.amount;
         Account accEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         AccountEntryData oldEntry = accEJB.getAccountEntryFromReference(AccountEntryData.TypeEnum.VOUCHERS_CREATED, vbEntry.id.toString());
         if (oldEntry == null) {
            throw new EJBException("Could not retrieve original account entry for batch creation");
         }

         AccountEntryData accEntry = new AccountEntryData();
         DecimalFormat df = new DecimalFormat("0.00");
         accEntry.username = username;
         accEntry.type = AccountEntryData.TypeEnum.VOUCHERS_CANCELLED;
         accEntry.reference = String.valueOf(vbEntry.id);
         accEntry.description = "Voucher Cancellation - " + username + " refunded for " + df.format(amountToRefund) + " " + vbEntry.currency;
         accEntry.amount = amountToRefund;
         accEntry.fundedAmount = accEntry.amount * oldEntry.fundedAmount / oldEntry.amount;
         accEntry.currency = vbEntry.currency;
         accEntry.tax = 0.0D;
         accEJB.createAccountEntry((Connection)null, accEntry, accountEntrySourceData);
      } catch (Exception var30) {
         throw new EJBException("Failed to cancel Voucher: " + var30.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var28) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
            conn = null;
         }

      }

   }

   public BlueLabelVoucherData redeemBlueLabelVoucher(String username, String voucherId, String voucherValue) throws EJBException {
      try {
         BlueLabelServicePrx blueLabelServicePrx = EJBIcePrxFinder.getBlueLabelService();
         BlueLabelOneVoucher oneVoucher = new BlueLabelOneVoucher();
         oneVoucher.number = voucherId;
         oneVoucher.value = voucherValue;
         oneVoucher.amountRedeemed = voucherValue;
         WebServiceResponse authResponse = blueLabelServicePrx.authenticate(username);
         if (BlueLabelResponseCodes.fromValue(authResponse.responseCode) == BlueLabelResponseCodes.SUCCESS) {
            WebServiceResponse webResponse = blueLabelServicePrx.fullVoucherRedemption(username, authResponse.responseData, oneVoucher);
            System.out.println("BL CODE: " + webResponse.responseCode);
            System.out.println("BL DATA: " + webResponse.responseData);
            if (webResponse.responseCode == BlueLabelResponseCodes.SUCCESS.value()) {
               try {
                  AccountLocal accEJB = (AccountLocal)EJBHomeCache.getObject("AccountLocal", AccountLocalHome.class);
                  double amount = Double.valueOf(oneVoucher.amountRedeemed);
                  accEJB.sendRechargeCreditRewardProgramTrigger(username, amount, oneVoucher.currency, 0);
               } catch (Exception var11) {
                  log.warn("Unable to send recharge credit reward program trigger for user: " + username);
               }
            }

            return new BlueLabelVoucherData(webResponse.responseCode, webResponse.responseData);
         } else {
            return new BlueLabelVoucherData(authResponse.responseCode, authResponse.responseData);
         }
      } catch (Exception var12) {
         log.error("failed to redeem bl1 voucher", var12);
         throw new EJBException(var12.getMessage());
      }
   }

   public VoucherData redeemVoucher(String username, String number, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      VoucherData var14;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select Type, MobileVerified, if(timestampdiff(HOUR, LastFailedVoucherRecharge, now()) <= 24, FailedVoucherRecharges, 0) FailedVoucherRechargesInLast24Hours from user where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid username " + username);
         }

         boolean allowEmailVerifiedUser = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Mig33Voucher_Redemption.ALLOW_EMAIL_VERIFIED_USER);
         if (rs.getInt("MobileVerified") == 0 && rs.getInt("Type") != UserData.TypeEnum.MIG33_PREPAID_CARD.value()) {
            if (!allowEmailVerifiedUser) {
               throw new EJBExceptionWithErrorCause(ErrorCause.Mig33VoucherRedemptionErrorReasonType.USER_NOT_VERIFIED, new Object[0]);
            }

            UserLocal userLocal = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            boolean isEmailOrMobileVerified = userLocal.isUserEmailVerifiedWithTxSupport(username);
            if (!isEmailOrMobileVerified) {
               throw new EJBExceptionWithErrorCause(ErrorCause.Mig33VoucherRedemptionErrorReasonType.USER_NOT_VERIFIED, new Object[0]);
            }
         }

         int voucherStatus;
         if (rs.getInt("FailedVoucherRechargesInLast24Hours") > 0) {
            voucherStatus = SystemProperty.getInt("MaxVoucherFailsPerDay");
            if (rs.getInt("FailedVoucherRechargesInLast24Hours") >= voucherStatus) {
               throw new EJBException("Too many invalid numbers entered. Try again in 24 hours");
            }
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select voucher.*, voucherbatch.currency, voucherbatch.amount, user.status userstatus from voucherbatch, voucher, user where voucherbatch.id = voucher.voucherbatchid and voucherbatch.username = user.username and voucher.number = ?");
         ps.setString(1, number);
         rs = ps.executeQuery();
         if (!rs.next()) {
            VoucherLocal voucherEJB = (VoucherLocal)EJBHomeCache.getLocalObject("VoucherLocal", VoucherLocalHome.class);
            voucherEJB.voucherRechargeFailed(username);
            throw new EJBException("Voucher number does not exist");
         }

         if (rs.getInt("userstatus") != UserData.StatusEnum.ACTIVE.value()) {
            throw new EJBException("Voucher creator's account is suspended");
         }

         voucherStatus = rs.getInt("status");
         if (voucherStatus != VoucherData.StatusEnum.ACTIVE.value()) {
            if (voucherStatus == VoucherData.StatusEnum.CANCELLED.value()) {
               throw new EJBException("Voucher has been canceled");
            }

            if (voucherStatus == VoucherData.StatusEnum.REDEEMED.value()) {
               throw new EJBException("Voucher has already been used");
            }

            if (voucherStatus == VoucherData.StatusEnum.INACTIVE.value()) {
               throw new EJBException("Voucher number is not valid");
            }

            throw new EJBException("Voucher is no longer valid");
         }

         VoucherData voucherData = new VoucherData();
         voucherData.id = rs.getInt("id");
         voucherData.voucherBatchID = rs.getInt("voucherbatchid");
         voucherData.number = number;
         voucherData.lastUpdated = new Date();
         voucherData.status = VoucherData.StatusEnum.REDEEMED;
         voucherData.notes = "Redeemed by " + username;
         voucherData.amount = rs.getDouble("amount");
         voucherData.currency = rs.getString("currency");
         rs.close();
         ps.close();
         ps = conn.prepareStatement("select voucher.number from user usercreator, user userredeemer, voucherbatch, voucher, country where voucher.voucherbatchid=voucherbatch.id and voucherbatch.username=usercreator.username and voucher.number=? and userredeemer.username=? and usercreator.countryid=country.id and (country.allowusertransfertoothercountry=1 or (usercreator.countryid=userredeemer.countryid))");
         ps.setString(1, number);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("This voucher can not currently be used in your country. Please contact customer service for assistance");
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select voucher.number from user usercreator, user userredeemer, voucherbatch, voucher, country where voucher.voucherbatchid=voucherbatch.id and voucherbatch.username=usercreator.username and voucher.number=? and userredeemer.username=? and userredeemer.countryid=country.id and (country.allowusertransferfromothercountry=1 or (usercreator.countryid=userredeemer.countryid))");
         ps.setString(1, number);
         ps.setString(2, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("This voucher may not be redeemed in your country at this time. Please contact customer service for assistance");
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("update voucher set status = ?, lastupdated = ?, notes = ? where ID = ? and status = ?");
         ps.setInt(1, voucherData.status.value());
         ps.setTimestamp(2, new Timestamp(voucherData.lastUpdated.getTime()));
         ps.setString(3, voucherData.notes);
         ps.setInt(4, voucherData.id);
         ps.setInt(5, VoucherData.StatusEnum.ACTIVE.value());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to redeem voucher. The voucher cannot be found or is not active.");
         }

         Account accEJB = (Account)EJBHomeCache.getObject("ejb/Account", AccountHome.class);
         AccountEntryData oldEntry = accEJB.getAccountEntryFromReference(AccountEntryData.TypeEnum.VOUCHERS_CREATED, voucherData.voucherBatchID.toString());
         if (oldEntry == null) {
            throw new EJBException("Could not retrieve original account entry for batch creation");
         }

         AccountEntryData accEntry = new AccountEntryData();
         DecimalFormat df = new DecimalFormat("0.00");
         accEntry.username = username;
         accEntry.type = AccountEntryData.TypeEnum.VOUCHER_RECHARGE;
         accEntry.reference = String.valueOf(voucherData.id);
         accEntry.description = "Voucher/Prepaid Card Recharge of " + df.format(voucherData.amount) + " " + voucherData.currency;
         accEntry.amount = voucherData.amount;
         accEntry.fundedAmount = accEntry.amount * oldEntry.fundedAmount / oldEntry.amount;
         accEntry.currency = voucherData.currency;
         accEntry.tax = 0.0D;
         accEJB.createAccountEntry((Connection)null, accEntry, accountEntrySourceData);
         ps.close();
         ps = conn.prepareStatement("update user set FailedVoucherRecharges = 0 where username = ?");
         ps.setString(1, username);
         ps.executeUpdate();

         try {
            if (log.isDebugEnabled()) {
               log.debug("accountBean.sendRechargeCreditRewardProgramTrigger()");
            }

            accEJB.sendRechargeCreditRewardProgramTrigger(username, voucherData.amount, voucherData.currency, 0);
         } catch (Exception var31) {
            log.warn("Unable to send recharge credit reward program trigger for user: " + username);
         }

         var14 = voucherData;
      } catch (EJBException var32) {
         log.warn("Unable to redeem voucher(username=[" + username + "] voucher=[" + number + "].Exception:[" + var32 + "]", var32);
         throw var32;
      } catch (Exception var33) {
         log.error("Unhandled exception on redeeming voucher(username=[" + username + "] voucher=[" + number + "].Exception:[" + var33 + "]", var33);
         throw new EJBException(var33.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var30) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var29) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var28) {
            conn = null;
         }

      }

      return var14;
   }

   public void voucherRechargeFailed(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update user set FailedVoucherRecharges = FailedVoucherRecharges + 1, LastFailedVoucherRecharge = now() where username = ?");
         ps.setString(1, username);
         ps.executeUpdate();
      } catch (Exception var18) {
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var17) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var16) {
            conn = null;
         }

      }

   }

   public List<VoucherData> retrieveVouchers(String username, int batchid, int type) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      LinkedList voucherList = null;

      LinkedList var9;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select voucher.id, voucher.voucherbatchid, voucher.number, voucher.lastupdated, voucher.status, voucher.notes from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid =  voucherbatch.id ";
         if (batchid != 0) {
            sql = sql + " and voucher.voucherbatchid = ? ";
         }

         if (type != 0) {
            sql = sql + " and voucher.status = ? ";
         }

         sql = sql + "order by voucher.lastupdated desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         if (batchid != 0) {
            ps.setInt(2, batchid);
         }

         if (type != 0) {
            if (batchid != 0) {
               ps.setInt(3, type);
            } else {
               ps.setInt(2, type);
            }
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            voucherList = new LinkedList();

            do {
               voucherList.add(new VoucherData(rs));
            } while(rs.next());
         }

         var9 = voucherList;
      } catch (Exception var24) {
         throw new EJBException("Retrieve Vouchers failed: " + var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return var9;
   }

   public VoucherData searchForVoucher(String username, String vouchernumber) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      VoucherData vEntry = null;

      VoucherData var8;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select voucher.id, voucher.voucherbatchid, voucher.number, voucher.status, voucher.lastupdated, voucher.notes from voucherbatch, voucher where voucherbatch.username = ? and voucher.voucherbatchid = voucherbatch.id and voucher.number = ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setString(2, vouchernumber);
         rs = ps.executeQuery();
         if (rs.next()) {
            vEntry = new VoucherData(rs);
         }

         var8 = vEntry;
      } catch (Exception var23) {
         throw new EJBException("Search Voucher failed: " + var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var8;
   }

   public VoucherData getVoucher(String voucherNumber) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      VoucherData voucherData;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from voucher where number = ?");
         ps.setString(1, voucherNumber);
         rs = ps.executeQuery();
         if (rs.next()) {
            voucherData = new VoucherData(rs);
            rs.close();
            ps.close();
            ps = conn.prepareStatement("select * from voucherbatch where id = ?");
            ps.setInt(1, voucherData.voucherBatchID);
            rs = ps.executeQuery();
            if (rs.next()) {
               voucherData.amount = rs.getDouble("amount");
               voucherData.currency = rs.getString("currency");
               VoucherData var6 = voucherData;
               return var6;
            }
         }

         voucherData = null;
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return voucherData;
   }

   public List<VoucherBatchData> retrieveVoucherBatches(String username, int id) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      LinkedList voucherBatchList = null;

      LinkedList var25;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select vb.datecreated,vb.id, vb.currency, vb.amount, vb.numvoucher,\tvb.notes, vb.expirydate, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 1) as active, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 2) as cancelled, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 3) as redeemed, (select count(*) from voucher where voucher.voucherbatchid = vb.id and voucher.status = 4) as expired from voucherbatch vb\twhere vb.username = ? ";
         if (id != -1) {
            sql = sql + "and vb.id = ? ";
         }

         sql = sql + "order by vb.datecreated desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         if (id != -1) {
            ps.setInt(2, id);
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            voucherBatchList = new LinkedList();

            do {
               VoucherBatchData vbEntry = new VoucherBatchData(rs);
               vbEntry.num_active = rs.getInt("active");
               vbEntry.num_cancelled = rs.getInt("cancelled");
               vbEntry.num_redeemed = rs.getInt("redeemed");
               vbEntry.num_expired = rs.getInt("expired");
               voucherBatchList.add(vbEntry);
            } while(rs.next());
         }

         var25 = voucherBatchList;
      } catch (Exception var23) {
         throw new EJBException("Retrieve Voucher Batch failed: " + var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var25;
   }

   public VoucherData activateVoucher(String username) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      VoucherData voucherData;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select voucher.*, voucherbatch.currency, voucherbatch.amount from voucherbatch, voucher where voucherbatch.id = voucher.voucherbatchid and voucherbatch.username = ? and status = ? limit 1");
         ps.setString(1, username);
         ps.setInt(2, VoucherData.StatusEnum.INACTIVE.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            voucherData = new VoucherData(rs);
            voucherData.amount = rs.getDouble("amount");
            voucherData.currency = rs.getString("currency");
            rs.close();
            ps.close();
            ps = conn.prepareStatement("update voucher set status = ?, lastupdated = ? where id = ?");
            ps.setInt(1, VoucherData.StatusEnum.ACTIVE.value());
            ps.setTimestamp(2, new Timestamp((new Date(System.currentTimeMillis())).getTime()));
            ps.setInt(3, voucherData.id);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to activate voucher " + voucherData.number);
            }

            VoucherData var6 = voucherData;
            return var6;
         }

         voucherData = null;
      } catch (SQLException var24) {
         throw new EJBException(var24);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
            conn = null;
         }

      }

      return voucherData;
   }

   public CachedRowSetImpl affiliateOverview(String username) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      CachedRowSetImpl var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select sum(amount) AS 'Amount',Currency , sum(Number) As 'TotalVouchers', sum(activevouchers) AS 'ActiveVouchers', sum(activeamount) AS 'ActivetotalAmount', sum(cancelledvouchers) AS 'CancelledVouchers', sum(cancelledamount) AS 'CancelledtotalAmount', sum(redeemedvouchers) AS 'RedeemedVouchers', sum(redeemedamount) AS 'redeemedtotalAmount', sum(expiredvouchers) AS 'ExpiredVouchers', sum(expiredamount) AS 'ExpiredtotalAmount' from ( select (voucherbatch.amount * NumVoucher) AS Amount, voucherbatch.currency AS Currency, count(voucher.id) AS Number, count(case when voucher.status = 1 then 1 end) AS activevouchers, sum(case when voucher.status = 1 then voucherbatch.amount end) AS activeamount, count(case when voucher.status = 2 then 1 end) AS cancelledvouchers, sum(case when voucher.status = 2 then voucherbatch.amount end) AS cancelledamount, count(case when voucher.status = 3 then 1 end) AS redeemedvouchers, sum(case when voucher.status = 3 then voucherbatch.amount end) AS redeemedamount, count(case when voucher.status = 4 then 1 end) AS expiredvouchers, sum(case when voucher.status = 4 then voucherbatch.amount end) AS expiredamount from  voucherbatch, voucher where  voucherbatch.id = voucher.VoucherBatchID and voucherbatch.username = ? group by voucherbatch.id ) batch group by Currency";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         rs = ps.executeQuery();
         CachedRowSetImpl crs = new CachedRowSetImpl();
         crs.populate(rs);
         var7 = crs;
      } catch (Exception var22) {
         throw new EJBException("Affiliate Overview generation failed: " + var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }

   public CachedRowSetImpl recentActivities(String username, int limit) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      CachedRowSetImpl var8;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select v.id,v.voucherbatchid,v.number,v.lastupdated,v.status,v.notes from voucherbatch vb,voucher v where vb.id = v.voucherbatchid and vb.username = ? and v.lastupdated is not null order by v.lastupdated desc limit ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setInt(2, limit);
         rs = ps.executeQuery();
         CachedRowSetImpl crs = new CachedRowSetImpl();
         crs.populate(rs);
         var8 = crs;
      } catch (Exception var23) {
         throw new EJBException("Recent activity generation failed: " + var23.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var20) {
            conn = null;
         }

      }

      return var8;
   }

   public int recentRedeem(String username, int days) throws EJBException {
      Connection conn = null;
      ResultSet rs = null;
      PreparedStatement ps = null;

      int var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "select count(*) from voucher v,voucherbatch vb where v.voucherbatchid = vb.id and vb.username = ? and v.status = 3 ";
         if (days == 0) {
            sql = sql + "and v.lastupdated >= CURDATE() ";
         } else {
            sql = sql + "and v.lastupdated > subdate(CURRENT_DATE(), ?) ";
         }

         sql = sql + "order by v.lastupdated desc";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         if (days > 0) {
            ps.setInt(2, days);
         }

         rs = ps.executeQuery();
         rs.next();
         var7 = rs.getInt(1);
      } catch (Exception var22) {
         throw new EJBException("Recent activity generation failed: " + var22.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var21) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var19) {
            conn = null;
         }

      }

      return var7;
   }
}
