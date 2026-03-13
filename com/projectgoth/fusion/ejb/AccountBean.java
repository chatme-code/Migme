package com.projectgoth.fusion.ejb;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlParameter;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.common.AsymmetricCryptUtils;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.CreditCardUtils;
import com.projectgoth.fusion.common.DateTimeUtils;
import com.projectgoth.fusion.common.EJBExceptionWithErrorCause;
import com.projectgoth.fusion.common.Enums;
import com.projectgoth.fusion.common.ErrorCause;
import com.projectgoth.fusion.common.LazyLoader;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedKeySpaces;
import com.projectgoth.fusion.common.MessageBundle;
import com.projectgoth.fusion.common.Numerics;
import com.projectgoth.fusion.common.PaymentUtils;
import com.projectgoth.fusion.common.SimpleXMLParser;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.common.UsernameUtils;
import com.projectgoth.fusion.credittransfer.CreditTransferUtils;
import com.projectgoth.fusion.data.AccountBalanceData;
import com.projectgoth.fusion.data.AccountEntryData;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.data.BankTransferIntentData;
import com.projectgoth.fusion.data.BankTransferReceivedData;
import com.projectgoth.fusion.data.BasicMerchantTagDetailsData;
import com.projectgoth.fusion.data.CallData;
import com.projectgoth.fusion.data.CashReceiptData;
import com.projectgoth.fusion.data.CountryData;
import com.projectgoth.fusion.data.CreditCardPaymentData;
import com.projectgoth.fusion.data.CreditTransferData;
import com.projectgoth.fusion.data.CurrencyData;
import com.projectgoth.fusion.data.DiscountTierData;
import com.projectgoth.fusion.data.MerchantDetailsData;
import com.projectgoth.fusion.data.MerchantTagData;
import com.projectgoth.fusion.data.MerchantTagUserData;
import com.projectgoth.fusion.data.MoneyTransferData;
import com.projectgoth.fusion.data.PaidEmoteData;
import com.projectgoth.fusion.data.PotData;
import com.projectgoth.fusion.data.PotStakeData;
import com.projectgoth.fusion.data.ReputationLevelData;
import com.projectgoth.fusion.data.ServiceData;
import com.projectgoth.fusion.data.SubscriptionData;
import com.projectgoth.fusion.data.SystemSMSData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.data.UserEmailAddressData;
import com.projectgoth.fusion.data.UserPotEligibilityData;
import com.projectgoth.fusion.data.UserReferralData;
import com.projectgoth.fusion.data.pot.GameSpenderData;
import com.projectgoth.fusion.data.pot.GameWinnerData;
import com.projectgoth.fusion.data.pot.PayoutData;
import com.projectgoth.fusion.ejb.util.LookupUtil;
import com.projectgoth.fusion.interfaces.AccountLocal;
import com.projectgoth.fusion.interfaces.AccountLocalHome;
import com.projectgoth.fusion.interfaces.MISLocal;
import com.projectgoth.fusion.interfaces.MISLocalHome;
import com.projectgoth.fusion.interfaces.MessageHome;
import com.projectgoth.fusion.interfaces.MessageLocal;
import com.projectgoth.fusion.interfaces.MessageLocalHome;
import com.projectgoth.fusion.interfaces.UserLocal;
import com.projectgoth.fusion.interfaces.UserLocalHome;
import com.projectgoth.fusion.merchant.MerchantCenter;
import com.projectgoth.fusion.payment.PaymentData;
import com.projectgoth.fusion.payment.PaymentDataFactory;
import com.projectgoth.fusion.payment.PaymentException;
import com.projectgoth.fusion.payment.PaymentMetaDetails;
import com.projectgoth.fusion.payment.PaymentSummaryData;
import com.projectgoth.fusion.payment.VendorVoucherData;
import com.projectgoth.fusion.payment.creditcard.CreditCardData;
import com.projectgoth.fusion.payment.creditcard.CreditCardPaymentUserAndCountryInfo;
import com.projectgoth.fusion.restapi.enums.GuardCapabilityEnum;
import com.projectgoth.fusion.rewardsystem.RewardCentre;
import com.projectgoth.fusion.rewardsystem.triggers.BotGameSpendingTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.BotGameWonTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.CreditRechargeTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.GameItemPurchasedTrigger;
import com.projectgoth.fusion.rewardsystem.triggers.ThirdPartyAppPurchaseTrigger;
import com.projectgoth.fusion.slice.AuthenticationServicePrx;
import com.projectgoth.fusion.slice.AuthenticationServiceResponseCodeEnum;
import com.projectgoth.fusion.slice.EmailUserNotification;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.Message;
import com.projectgoth.fusion.slice.UserNotificationServicePrx;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.smsengine.SMSControl;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.client.ClientProtocolException;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AccountBean implements SessionBean {
   private static final long serialVersionUID = -9012983848874004156L;
   private static final String URL_ENCODING = "UTF-8";
   private static final int COUNTRY_ID_USA = 231;
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(AccountBean.class));
   private static final AccountBean.BannedCreditSendersLoader bannedCreditSenders = new AccountBean.BannedCreditSendersLoader();
   public static final String BANNED_SENDER_USER_ERROR = "Transfer from this account is not allowed";
   private DataSource dataSourceMaster;
   private DataSource dataSourceSlave;
   private SessionContext context;

   public void setSessionContext(SessionContext newContext) throws EJBException {
      this.context = newContext;
   }

   public void ejbRemove() throws EJBException, RemoteException {
   }

   public void ejbActivate() throws EJBException, RemoteException {
   }

   public void ejbPassivate() throws EJBException, RemoteException {
   }

   public void ejbCreate() throws CreateException {
      try {
         this.dataSourceMaster = LookupUtil.getFusionMasterDataSource();
         this.dataSourceSlave = LookupUtil.getFusionSlaveDataSource();
         SystemProperty.ejbInit(this.dataSourceSlave);
      } catch (Exception var2) {
         log.error("Unable to create Account EJB", var2);
         throw new CreateException("Unable to create Account EJB: " + var2.getMessage());
      }
   }

   private double calculateTaxComponent(Connection conn, String username, double amount) throws SQLException {
      PreparedStatement ps = null;
      ResultSet rs = null;

      double var9;
      try {
         ps = conn.prepareStatement("select tax from country inner join user on (country.id = user.countryid) where username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         double taxRate = rs.next() ? rs.getDouble("tax") : 0.0D;
         var9 = amount * taxRate / (1.0D + taxRate);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

      }

      return var9;
   }

   private void applyDiscountTier(Connection conn, String username, DiscountTierData discountTierData, AccountEntryData accountEntryData) throws SQLException {
      PreparedStatement ps = null;

      try {
         ps = conn.prepareStatement("insert into applieddiscount (username, datecreated, discounttierid, accountentryid, amount, currency, exchangerate) values (?,?,?,?,?,?,?)");
         ps.setString(1, username);
         ps.setTimestamp(2, new Timestamp(accountEntryData.dateCreated.getTime()));
         ps.setObject(3, discountTierData.id);
         ps.setObject(4, accountEntryData.id);
         ps.setObject(5, discountTierData.discountAmount);
         ps.setString(6, discountTierData.currency);
         ps.setObject(7, accountEntryData.exchangeRate);
         if (ps.executeUpdate() != 1) {
            throw new SQLException("Unable to log discount");
         }
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var12) {
            ps = null;
         }

      }

   }

   public AccountEntryData createAccountEntry(Connection conn, AccountEntryData accountEntryData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      boolean isRestrictedForAll = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CREATE_ACCOUNT_ENTRY_RESTRICTED_ALL);
      boolean isRestrictedForNonTopMerchants = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CREATE_ACCOUNT_ENTRY_RESTRICTION_NONTOPMERCHANT);
      List restrictedTypes;
      UserLocal userBean;
      if (isRestrictedForAll || isRestrictedForNonTopMerchants) {
         restrictedTypes = Arrays.asList(SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.CREATED_ACCOUNT_ENTRY_RESTRICTED_TYPES));
         boolean isRestrictedType = restrictedTypes.contains(Integer.toString(accountEntryData.type.value()));
         if (isRestrictedType) {
            if (isRestrictedForAll) {
               throw new EJBException("This feature is currently disabled. Please try again later.");
            }

            if (accountEntryData.amount < 0.0D) {
               try {
                  userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                  UserData transferrerUserData = userBean.loadUserByUsernameOrAlias(accountEntryData.username, false, false);
                  if (transferrerUserData.type != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                     throw new EJBException("This feature is currently disabled. Please try again later.");
                  }
               } catch (CreateException var33) {
                  throw new EJBException("This feature is currently disabled. Please try again later.", var33);
               }
            }
         }
      }

      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountEntryData var45;
      try {
         ch = new ConnectionHolder(this.dataSourceMaster, conn);
         accountEntryData.dateCreated = new Date(System.currentTimeMillis());
         accountEntryData.username = accountEntryData.username.trim().toLowerCase();
         AccountBalanceData userBalance = this.getAccountBalance(accountEntryData.username);
         CurrencyData userCurrency = userBalance.currency;
         if (!accountEntryData.currency.equals(userCurrency.code)) {
            MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            CurrencyData transactionCurrency = misBean.getCurrency(accountEntryData.currency);
            accountEntryData.amount = userCurrency.convertFrom(accountEntryData.amount, transactionCurrency);
            accountEntryData.currency = userCurrency.code;
            if (accountEntryData.wholesaleCost != null) {
               accountEntryData.wholesaleCost = userCurrency.convertFrom(accountEntryData.wholesaleCost, transactionCurrency);
            }

            if (accountEntryData.fundedAmount != null) {
               accountEntryData.fundedAmount = userCurrency.convertFrom(accountEntryData.fundedAmount, transactionCurrency);
            }
         }

         accountEntryData.exchangeRate = userCurrency.exchangeRate;
         accountEntryData.amount = Numerics.floor(accountEntryData.amount, 2);
         if (accountEntryData.fundedAmount == null) {
            if (accountEntryData.amount < 0.0D) {
               accountEntryData.fundedAmount = Math.min(0.0D, userBalance.balance - userBalance.fundedBalance + accountEntryData.amount);
               accountEntryData.fundedAmount = Math.max(accountEntryData.amount, accountEntryData.fundedAmount);
            } else {
               accountEntryData.fundedAmount = 0.0D;
            }
         }

         accountEntryData.fundedAmount = Numerics.floor(accountEntryData.fundedAmount, 2);
         if (Math.signum(accountEntryData.amount) != Math.signum(accountEntryData.fundedAmount) && accountEntryData.fundedAmount != 0.0D) {
            throw new EJBException(String.format("We are unable to process your request at the moment. Please try again later. [%s]", AccountBean.AccountTransactionErrorEnum.AMOUNT_AND_FUNDED_AMOUNT_HAVE_DIFFERENT_SIGNS.code));
         }

         if (accountEntryData.amount >= 0.0D && accountEntryData.fundedAmount > accountEntryData.amount) {
            log.error("Funded Amount (" + accountEntryData.fundedAmount + ") exceeds Amount (" + accountEntryData.amount + ")");
            throw new EJBException(String.format("We are unable to process your request at the moment. Please try again later. [%s]", AccountBean.AccountTransactionErrorEnum.FUNDED_AMOUNT_GREATER_THAN_TRANSACTION_AMOUNT.code));
         }

         if (accountEntryData.amount <= 0.0D && accountEntryData.fundedAmount < accountEntryData.amount) {
            log.error("Funded Amount (" + accountEntryData.fundedAmount + ") exceeds Amount (" + accountEntryData.amount + ")");
            throw new EJBException(String.format("We are unable to process your request at the moment. Please try again later. [%s]", AccountBean.AccountTransactionErrorEnum.FUNDED_AMOUNT_LESS_THAN_TRANSACTION_AMOUNT.code));
         }

         this.splitWholesaleCost(accountEntryData, userBalance);
         if (accountEntryData.amount != 0.0D || accountEntryData.fundedAmount != 0.0D) {
            ps = ch.getConnection().prepareStatement("update user set balance = balance + ?, fundedbalance = fundedbalance + ? where username = ?");
            ps.setObject(1, accountEntryData.amount);
            ps.setObject(2, accountEntryData.fundedAmount);
            ps.setString(3, accountEntryData.username);
            if (ps.executeUpdate() != 1) {
               throw new EJBException(accountEntryData.username + " is not a valid user");
            }

            ps.close();
         }

         ps = ch.getConnection().prepareStatement("insert into accountentry (username, datecreated, type, reference, description, currency, exchangerate, amount, fundedamount, tax, costofgoodssold, costoftrial) values (?,?,?,?,?,?,?,?,?,?,?,?)", 1);
         ps.setString(1, accountEntryData.username);
         ps.setTimestamp(2, new Timestamp(accountEntryData.dateCreated.getTime()));
         ps.setObject(3, accountEntryData.type == null ? null : accountEntryData.type.value());
         ps.setString(4, accountEntryData.reference);
         ps.setString(5, StringUtil.truncateWithEllipsis(accountEntryData.description, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountEntry.MAX_DESC_LENGTH)));
         ps.setString(6, accountEntryData.currency);
         ps.setDouble(7, accountEntryData.exchangeRate);
         ps.setObject(8, accountEntryData.amount);
         ps.setObject(9, accountEntryData.fundedAmount);
         ps.setObject(10, accountEntryData.tax);
         ps.setObject(11, accountEntryData.costOfGoodsSold);
         ps.setObject(12, accountEntryData.costOfTrial);
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException(String.format("We are unable to process your request at the moment. Please try again later. [%s]", AccountBean.AccountTransactionErrorEnum.CREATE_ACCOUNTENTRY_FAILED.code));
         }

         accountEntryData.id = rs.getLong(1);
         rs.close();
         ps.close();
         BasicMerchantTagDetailsData merchantTagData = this.getMerchantTagFromUsername(ch.getConnection(), accountEntryData.username, false);
         if (merchantTagData != null) {
            accountEntrySourceData.merchantUserID = merchantTagData.merchantUserID;
         }

         ps = ch.getConnection().prepareStatement("insert into accountentrysource (accountentryid, ipaddress, sessionid, mobiledevice, useragent, imei, merchantuserid) values (?,?,?,?,?,?,?)");
         ps.setObject(1, accountEntryData.id);
         ps.setString(2, accountEntrySourceData.ipAddress);
         ps.setString(3, accountEntrySourceData.sessionID);
         ps.setString(4, accountEntrySourceData.mobileDevice);
         ps.setString(5, accountEntrySourceData.userAgent);
         ps.setString(6, accountEntrySourceData.imei);
         ps.setObject(7, accountEntrySourceData.merchantUserID);
         if (ps.executeUpdate() != 1) {
            throw new EJBException(String.format("We are unable to process your request at the moment. Please try again later. [%s]", AccountBean.AccountTransactionErrorEnum.CREATE_ACCOUNTENTRY_SOURCE_FAILED.code));
         }

         ps.close();
         userBalance = this.getAccountBalance(accountEntryData.username);
         if (accountEntryData.amount < 0.0D) {
            if ((userBalance.balance < 0.0D || userBalance.fundedBalance < 0.0D) && accountEntryData.type != AccountEntryData.TypeEnum.CALL_CHARGE) {
               log.warn("Insuffcient credit for " + accountEntryData.type + ": " + accountEntryData.username + ", amount = " + userCurrency.formatWithCode(accountEntryData.amount) + ", funded amount = " + userCurrency.formatWithCode(accountEntryData.fundedAmount));
               if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE503_EXTEND_EXCEPTION_ON_INSUFFICIENT_CREDIT_ENABLED)) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.INSUFFICIENT_CREDIT, new Object[0]);
               }

               throw new EJBException("Insufficient credit.");
            }

            this.sendLowBalanceAlertIfRequired(ch.getConnection(), accountEntryData.type, accountEntryData.username, userBalance.getBaseBalance(), accountEntrySourceData);
         }

         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.ACCOUNT_BALANCE, accountEntryData.username);

         try {
            UserPrx userPrx = EJBIcePrxFinder.findOnewayUserPrx(accountEntryData.username);
            if (userPrx != null) {
               userPrx.accountBalanceChanged(userBalance.balance, userBalance.fundedBalance, userBalance.currency.toIceObject());
            }
         } catch (Exception var32) {
            log.warn("Unable to notify account balance change for " + accountEntryData.username, var32);
         }

         var45 = accountEntryData;
      } catch (CreateException var34) {
         throw new EJBException(var34.getMessage(), var34);
      } catch (SQLException var35) {
         throw new EJBException(var35.getMessage(), var35);
      } catch (EJBException var36) {
         throw var36;
      } catch (Exception var37) {
         throw new EJBException(var37.getMessage(), var37);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            userBean = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var29) {
            restrictedTypes = null;
         }

      }

      return var45;
   }

   private void splitWholesaleCost(AccountEntryData accountEntryData, AccountBalanceData userBalance) {
      if (accountEntryData.wholesaleCost != null && accountEntryData.wholesaleCost != 0.0D) {
         if (accountEntryData.amount == 0.0D) {
            if (userBalance == null) {
               userBalance = this.getAccountBalance(accountEntryData.username);
            }

            if (!(userBalance.balance <= 0.0D) && !(userBalance.fundedBalance <= 0.0D)) {
               accountEntryData.costOfGoodsSold = accountEntryData.wholesaleCost * userBalance.fundedBalance / userBalance.balance;
               accountEntryData.costOfTrial = accountEntryData.wholesaleCost - accountEntryData.costOfGoodsSold;
            } else {
               accountEntryData.costOfGoodsSold = 0.0D;
               accountEntryData.costOfTrial = accountEntryData.wholesaleCost;
            }
         } else {
            accountEntryData.costOfGoodsSold = accountEntryData.wholesaleCost * accountEntryData.fundedAmount / accountEntryData.amount;
            accountEntryData.costOfTrial = accountEntryData.wholesaleCost - accountEntryData.costOfGoodsSold;
         }
      } else {
         if (accountEntryData.costOfGoodsSold == null) {
            accountEntryData.costOfGoodsSold = 0.0D;
         }

         if (accountEntryData.costOfTrial == null) {
            accountEntryData.costOfTrial = 0.0D;
         }
      }

      accountEntryData.costOfGoodsSold = Numerics.round(accountEntryData.costOfGoodsSold, 4);
      accountEntryData.costOfTrial = Numerics.round(accountEntryData.costOfTrial, 4);
   }

   private void sendLowBalanceAlertIfRequired(Connection conn, AccountEntryData.TypeEnum type, String username, double balanceInBaseCurrency, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.LOW_BALANCE_ALERT, username)) {
         PreparedStatement ps = null;
         ResultSet rs = null;
         if (type != null) {
            if (type == AccountEntryData.TypeEnum.CALL_CHARGE) {
               try {
                  if (balanceInBaseCurrency > SystemProperty.getDouble("LowBalanceAlertThreshold")) {
                     return;
                  }

                  ps = conn.prepareStatement("select user.mobilephone from accountentry, user where accountentry.username = user.username and accountentry.username=? and accountentry.type in (1,2,11,14,15,21,25,40) and amount > 0 limit 1");
                  ps.setString(1, username);
                  rs = ps.executeQuery();
                  if (rs.next()) {
                     String mobilePhone = rs.getString("mobilephone");
                     if (mobilePhone == null) {
                        return;
                     }

                     MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
                     if (!messageBean.isMobileNumber(mobilePhone, true)) {
                        return;
                     }

                     SystemSMSData systemSMSData = new SystemSMSData();
                     systemSMSData.username = username;
                     systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
                     systemSMSData.subType = SystemSMSData.SubTypeEnum.LOW_BALANCE_ALERT;
                     systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
                     systemSMSData.destination = mobilePhone;
                     systemSMSData.messageText = SystemProperty.get("LowBalanceAlertSMS");
                     messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
                  }

                  return;
               } catch (CreateException var40) {
                  System.err.println("CreateException occurred during sendLowBalanceAlertIfRequired(): " + var40.getMessage());
                  return;
               } catch (SQLException var41) {
                  System.err.println("SQLException occurred during sendLowBalanceAlertIfRequired(): " + var41.getMessage());
                  return;
               } catch (EJBException var42) {
                  System.err.println("EJBException occurred during sendLowBalanceAlertIfRequired(): " + var42.getMessage());
                  return;
               } catch (Exception var43) {
                  System.err.println("Exception occurred during sendLowBalanceAlertIfRequired(): " + var43.getMessage());
               } finally {
                  try {
                     if (rs != null) {
                        rs.close();
                     }
                  } catch (SQLException var39) {
                     rs = null;
                  }

                  try {
                     if (ps != null) {
                        ps.close();
                     }
                  } catch (SQLException var38) {
                     ps = null;
                  }

               }

            }
         }
      }
   }

   public AccountEntryData refundAccountEntry(long accountEntryID, String reference, String description, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = this.getAccountEntry(accountEntryID);
      AccountEntryData refundEntry = new AccountEntryData();
      refundEntry.type = AccountEntryData.TypeEnum.REFUND;
      refundEntry.username = accountEntry.username;
      refundEntry.reference = reference;
      refundEntry.description = description;
      refundEntry.currency = accountEntry.currency;
      refundEntry.amount = -accountEntry.amount;
      refundEntry.fundedAmount = -accountEntry.fundedAmount;
      refundEntry.tax = 0.0D;
      refundEntry.costOfGoodsSold = -accountEntry.costOfGoodsSold;
      refundEntry.costOfTrial = -accountEntry.costOfTrial;
      return this.createAccountEntry((Connection)null, refundEntry, accountEntrySourceData);
   }

   public AccountBalanceData getAccountBalance(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountBalanceData var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select user.balance, user.fundedbalance, currency.* from user, currency where user.currency = currency.code and username = ?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to determine the currency code for user " + username);
         }

         AccountBalanceData balanceData = new AccountBalanceData();
         balanceData.currency = new CurrencyData(rs);
         balanceData.balance = rs.getDouble("balance");
         balanceData.fundedBalance = rs.getDouble("fundedBalance");
         var6 = balanceData;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var6;
   }

   public AccountEntryData getAccountEntry(long accountEntryID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountEntryData var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from accountentry where ID = ?");
         ps.setLong(1, accountEntryID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var6 = null;
            return var6;
         }

         var6 = new AccountEntryData(rs);
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage(), var24);
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

      return var6;
   }

   public AccountEntryData getAccountEntryFromSlave(long accountEntryID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountEntryData var6;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement("select * from accountentry where ID = ?");
         ps.setLong(1, accountEntryID);
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = new AccountEntryData(rs);
            return var6;
         }

         var6 = null;
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

      return var6;
   }

   protected boolean userTransferReversedAlready(long accountEntryID) {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var6;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select * from accountentry where Type=? and Reference=?");
         ps.setInt(1, AccountEntryData.TypeEnum.REFUND.value());
         ps.setString(2, Long.toString(accountEntryID));
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = true;
            return var6;
         }

         var6 = false;
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage(), var24);
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
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var21) {
            connMaster = null;
         }

      }

      return var6;
   }

   public AccountEntryData getAccountEntryFromReference(AccountEntryData.TypeEnum type, String reference) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountEntryData var6;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from accountentry where type = ? and reference = ?");
         ps.setInt(1, type.value());
         ps.setString(2, reference);
         rs = ps.executeQuery();
         if (rs.next()) {
            var6 = new AccountEntryData(rs);
            return var6;
         }

         var6 = null;
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

      return var6;
   }

   public List<AccountEntryData> getAccountEntries(String username, boolean desc) throws EJBException {
      return new LinkedList();
   }

   public MoneyTransferData getMoneyTransferEntry(int moneyTransferID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MoneyTransferData var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from moneytransfer where id = ?");
         ps.setInt(1, moneyTransferID);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = new MoneyTransferData(rs);
            return var5;
         }

         var5 = null;
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

      return var5;
   }

   public List<MoneyTransferData> getMoneyTransferEntries(String username) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from moneytransfer where username = ? order by DateCreated desc");
         ps.setString(1, username);
         rs = ps.executeQuery();
         LinkedList moneyEntryList = new LinkedList();

         while(rs.next()) {
            moneyEntryList.add(new MoneyTransferData(rs));
         }

         LinkedList var6 = moneyEntryList;
         return var6;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }
   }

   public AccountEntryData giveActivationCredit(String username, int countryID, String reference, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misBean.getCountry(countryID);
         if (countryData.activationCredit != null && !(countryData.activationCredit <= 0.0D)) {
            AccountEntryData accountEntry = new AccountEntryData();
            accountEntry.username = username;
            accountEntry.type = AccountEntryData.TypeEnum.ACTIVATION_CREDIT;
            accountEntry.reference = reference;
            accountEntry.description = "Free credit for account activation";
            accountEntry.currency = CurrencyData.baseCurrency;
            accountEntry.amount = countryData.activationCredit;
            accountEntry.tax = 0.0D;
            return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
         } else {
            return null;
         }
      } catch (CreateException var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   public AccountEntryData creditReferrer(UserReferralData userReferralData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      AccountEntryData accountEntry = null;

      AccountEntryData var42;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData referredUserData = userBean.loadUser(userReferralData.referredUsername, false, false);
         if (referredUserData == null) {
            throw new EJBException("Unable to load referred user " + userReferralData.referredUsername + " for referral ID " + userReferralData.id);
         }

         UserData referrerUserData = userBean.loadUser(userReferralData.username, false, false);
         if (referrerUserData == null) {
            throw new EJBException("Unable to load referrer " + userReferralData.username + " for referral ID " + userReferralData.id);
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select count(*) from activation where (username = ? or mobilephone = ?) and datecreated <= ?");
         ps.setString(1, referredUserData.username);
         ps.setString(2, referredUserData.mobilePhone);
         ps.setTimestamp(3, new Timestamp(userReferralData.dateCreated.getTime()));
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to determine if referral ID " + userReferralData.id + " is first activation");
         }

         StringBuffer description;
         if (rs.getInt(1) > 1) {
            description = null;
            return description;
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("update userreferral set paid = 1 where ID = ? and paid = 0");
         ps.setInt(1, userReferralData.id);
         if (ps.executeUpdate() == 1 && userReferralData.amount > 0.0D) {
            this.createSweepstakesEntry(conn, userReferralData.id, referrerUserData.username, referrerUserData.countryID);
            accountEntry = new AccountEntryData();
            description = new StringBuffer("Bonus credit for referring ");
            description.append(referredUserData.username);
            accountEntry.username = referrerUserData.username;
            accountEntry.type = AccountEntryData.TypeEnum.REFERRAL_CREDIT;
            accountEntry.reference = userReferralData.id.toString();
            accountEntry.description = description.length() > 128 ? description.substring(0, 128).toString() : description.toString();
            accountEntry.currency = CurrencyData.baseCurrency;
            accountEntry.amount = userReferralData.amount;
            accountEntry.tax = 0.0D;
            accountEntry = this.createAccountEntry(conn, accountEntry, accountEntrySourceData);
            if (!SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.USER_REFERRAL_ACTIVATION, userReferralData.username)) {
               AccountEntryData var43 = accountEntry;
               return var43;
            }

            MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            int notificationMeans = SystemProperty.getInt((String)"UserReferralActivationNotificationMeans", 1);
            DecimalFormat df = new DecimalFormat("0.00");
            String messageText = SystemProperty.get("ReferralCreditSMS").replaceAll("%1", referredUserData.username).replaceAll("%2", referredUserData.mobilePhone).replaceAll("%3", df.format(userReferralData.amount));
            if ((notificationMeans & 1) == 1 && !StringUtil.isBlank(referrerUserData.mobilePhone) && messageBean.isMobileNumber(referrerUserData.mobilePhone, true)) {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.USER_REFERRAL_ACTIVATION;
               systemSMSData.username = referrerUserData.username;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = referrerUserData.mobilePhone;
               systemSMSData.messageText = messageText;
               messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
            }

            if ((notificationMeans & 2) == 2) {
               String emailSubject = "User Referral Activated";
               messageBean.sendSystemEmail(referrerUserData.username, emailSubject, messageText);
               if (referrerUserData.emailActivated && !StringUtil.isBlank(referrerUserData.emailAddress)) {
                  messageBean.sendEmailFromNoReply(referrerUserData.emailAddress, emailSubject, messageText);
               }
            }
         }

         var42 = accountEntry;
      } catch (CreateException var38) {
         throw new EJBException(var38.getMessage());
      } catch (SQLException var39) {
         throw new EJBException(var39.getMessage());
      } catch (NoSuchFieldException var40) {
         throw new EJBException(var40.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var37) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var36) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var35) {
            conn = null;
         }

      }

      return var42;
   }

   private void createSweepstakesEntry(Connection conn, int referralID, String referrerUsername, int referrerCountryID) throws EJBException {
      try {
         if (SystemProperty.getBool("GenerateSweepstakesCode")) {
            if (referrerCountryID != 231) {
               String code = this.createSweepstakesCode(conn, referralID, referrerUsername);
               MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               messageBean.sendSystemEmail(referrerUsername, SystemProperty.get("SweepstakesCodeEmailSubject"), SystemProperty.get("SweepstakesCodeEmailBody").replaceAll("%1", code));
            }
         }
      } catch (CreateException var7) {
         throw new EJBException(var7.getMessage());
      } catch (NoSuchFieldException var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   private String createSweepstakesCode(Connection conn, int referralID, String referrerUsername) {
      PreparedStatement ps = null;
      ResultSet rs = null;
      String sweepstakesCode = "";

      try {
         SecureRandom random = new SecureRandom();
         ps = conn.prepareStatement("insert into sweepstakescode (userreferralID, datecreated, username, code) values (?,?,?,?)", 1);

         do {
            ps.setInt(1, referralID);
            ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
            ps.setString(3, referrerUsername);
            sweepstakesCode = "" + (Math.abs(random.nextLong()) % 9000000000L + 1000000000L);
            ps.setString(4, sweepstakesCode);
         } while(ps.executeUpdate() != 1);

         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Failed to save sweepstakes code for userreferralID '" + referralID + "'");
         }

         int id = rs.getInt(1);
         log.info("Saved sweepstakes code for referrerName '" + referrerUsername + "' [id=" + id + ", userreferralID=" + referralID + ", code=" + sweepstakesCode + "]");
      } catch (SQLException var20) {
         throw new EJBException("Failed to save sweepstakes code for userreferralID '" + referralID + "'" + var20.getMessage());
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

      }

      return sweepstakesCode;
   }

   public CreditTransferData transferPartnerCredit(int partnerId, String mobilePhone, double amount, String transactionId, AccountEntryData.TypeEnum type, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      DecimalFormat df = new DecimalFormat("0.00");

      CreditTransferData var21;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData toUserData = userBean.loadUserFromMobilePhone(mobilePhone);
         if (toUserData == null) {
            throw new EJBException("Unknown mobilephone number");
         }

         amount = Numerics.floor(amount, 2);
         if (amount < 0.01D) {
            throw new EJBException("Transfer amount is too low");
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from ussdpartneruser where userid = ? and ussdpartnerid = ?");
         ps.setInt(1, toUserData.userID);
         ps.setInt(2, partnerId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Mobile number " + mobilePhone + " doesn't belong to partner: " + partnerId);
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select user.username, user.currency, user.balance, user.countryid, userid.id userid from user, userid, ussdpartner where user.username = userid.username and ussdpartner.userid = userid.id and ussdpartner.id = ? ");
         ps.setInt(1, partnerId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unknown partner " + partnerId);
         }

         String currency = rs.getString("currency");
         double balance = rs.getDouble("balance");
         String fromUserName = rs.getString("username");
         if (balance < amount) {
            throw new EJBException("Insufficient credit");
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select * from accountentry where type = ? and username in (?, ?) and amount < 0 and substring_index(description, '[', -1) = ?");
         ps.setInt(1, type.value());
         ps.setString(2, fromUserName);
         ps.setString(3, toUserData.username);
         ps.setString(4, transactionId + "]");
         rs = ps.executeQuery();
         if (rs.next()) {
            log.warn(transactionId + " belongs to accountentry[" + rs.getInt("id") + "] and description: " + rs.getString("description"));
            throw new EJBException("A transaction with transaction id [" + transactionId + "] already exists");
         }

         rs.close();
         ps.close();
         AccountEntryData fromEntry = new AccountEntryData();
         fromEntry.username = fromUserName;
         fromEntry.type = type;
         fromEntry.reference = "";
         fromEntry.description = "Purchase/redeem " + df.format(amount) + " " + currency + " from " + mobilePhone + " [" + transactionId + "]";
         fromEntry.currency = currency;
         fromEntry.amount = -amount;
         fromEntry.tax = 0.0D;
         fromEntry = this.createAccountEntry(conn, fromEntry, accountEntrySourceData);
         if (fromEntry.amount > -0.01D) {
            throw new EJBException("Transfer amount is too low");
         }

         AccountEntryData toEntry = new AccountEntryData();
         toEntry.username = toUserData.username;
         toEntry.type = type;
         toEntry.reference = fromEntry.id.toString();
         toEntry.description = df.format(amount) + " " + currency + " purchased/redeemed through " + mobilePhone + " [" + transactionId + "]";
         toEntry.currency = currency;
         toEntry.amount = amount;
         toEntry.fundedAmount = -fromEntry.fundedAmount;
         toEntry.tax = 0.0D;
         toEntry = this.createAccountEntry(conn, toEntry, new AccountEntrySourceData(AccountBean.class));
         ps = conn.prepareStatement("update accountentry set reference = ? where id = ?");
         ps.setString(1, toEntry.id.toString());
         ps.setLong(2, fromEntry.id);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to update transferer's account entry");
         }

         ps.close();
         AccountBalanceData accountBalanceData = this.getAccountBalance(fromUserName);
         balance = accountBalanceData.balance;
         if (balance < 0.0D) {
            throw new EJBException("Insufficient credit");
         }

         var21 = new CreditTransferData(fromEntry, accountBalanceData);
      } catch (SQLException var37) {
         throw new EJBException(var37.getMessage());
      } catch (CreateException var38) {
         throw new EJBException(var38.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var36) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var35) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var34) {
            conn = null;
         }

      }

      return var21;
   }

   public AccountEntryData refundCreditTransferFee(long transferAccountEntryID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CreditTransferFee.REVERSE_ENABLED)) {
         boolean feeCreditedToTagger = false;
         AccountEntryData transferChargeAccountEntry = this.getAccountEntryFromReference(AccountEntryData.TypeEnum.TRANSFER_CREDIT_FEE, String.valueOf(transferAccountEntryID));
         if (transferChargeAccountEntry == null) {
            transferChargeAccountEntry = this.getAccountEntryFromReference(AccountEntryData.TypeEnum.TRANSFER_CREDIT_FEE_TO_TAGGER, String.valueOf(transferAccountEntryID));
            feeCreditedToTagger = true;
         }

         if (transferChargeAccountEntry != null) {
            String description = String.format("Reversal of transfer credit fee. (Transfer to %s)", transferChargeAccountEntry.description.substring(transferChargeAccountEntry.description.lastIndexOf(" ") + 1));
            if (feeCreditedToTagger) {
               AccountEntryData taggerFee = this.getAccountEntryFromReference(AccountEntryData.TypeEnum.TRANSFER_CREDIT_FEE_TO_TAGGER, String.valueOf(transferChargeAccountEntry.id));
               if (taggerFee != null) {
                  this.refundAccountEntry(taggerFee.id, taggerFee.id.toString(), description, accountEntrySourceData);
               }
            }

            return this.refundAccountEntry(transferChargeAccountEntry.id, transferChargeAccountEntry.id.toString(), description, accountEntrySourceData);
         }
      }

      return null;
   }

   public boolean isChargeableWithCreditTransferFee(int userID) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var27;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT COUNT(*) ctr_capability FROM guardcapability gc, guardsetcapability gsc, guardsetmember gsm, guardset gs WHERE gc.id = gsc.guardcapabilityid AND gsm.guardsetid = gsc.guardsetid AND gsm.guardsetid = gs.id AND gc.id = ? AND gsm.memberid = ?";
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, GuardCapabilityEnum.CHARGEABLE_WITH_CREDIT_TRANSFER_FEE.value());
         ps.setInt(2, userID);
         rs = ps.executeQuery();
         if (rs.next()) {
            int value = rs.getInt(1);
            if (value > 0) {
               boolean var7 = true;
               return var7;
            }
         }

         var27 = false;
      } catch (SQLException var25) {
         log.error("SQLException occurred in isChargeableWithTransferCreditFee: " + var25);
         throw new FusionEJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var27;
   }

   public AccountEntryData chargeCreditTransferFee(String sender, String receipient, AccountEntryData creditTransferAccountEntryData, Connection conn) throws EJBExceptionWithErrorCause, EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CreditTransferFee.ENABLED)) {
         ConnectionHolder ch = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            ch = new ConnectionHolder(this.dataSourceMaster, conn);
            ps = ch.getConnection().prepareStatement("SELECT ui.id, u.username, u.type, c.minbalanceaftertransfer, u.balance FROM user u, country c, userid ui WHERE u.countryID = c.id AND u.username IN (?,?) AND ui.username = u.username");
            ps.setString(1, sender);
            ps.setString(2, receipient);
            HashMap<String, HashMap<String, Object>> transferUserData = new HashMap();
            rs = ps.executeQuery();

            while(rs.next()) {
               HashMap<String, Object> userData = new HashMap();
               userData.put("type", UserData.TypeEnum.fromValue(rs.getInt("type")));
               userData.put("minBalanceAfterTransfer", rs.getDouble("minbalanceaftertransfer"));
               userData.put("balance", rs.getDouble("balance"));
               userData.put("userID", rs.getInt("id"));
               transferUserData.put(rs.getString("username"), userData);
            }

            UserData.TypeEnum receipientType = (UserData.TypeEnum)((HashMap)transferUserData.get(receipient)).get("type");
            CreditTransferData.CreditTransferFeeEnum transferType = CreditTransferData.CreditTransferFeeEnum.fromUserType((UserData.TypeEnum)((HashMap)transferUserData.get(sender)).get("type"), receipientType);
            String senderUserID;
            if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CreditTransferFeeTypeSettings.ENABLED.forTransferFee(transferType))) {
               senderUserID = null;
               return senderUserID;
            } else {
               if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CreditTransferFee.ENABLED_TO_PUBLIC)) {
                  senderUserID = String.valueOf(((HashMap)transferUserData.get(sender)).get("userID"));
                  if (!this.isChargeableWithCreditTransferFee(Integer.valueOf(senderUserID))) {
                     Object var12 = null;
                     return (AccountEntryData)var12;
                  }
               }

               if (this.convertCurrency(Math.abs(creditTransferAccountEntryData.amount), creditTransferAccountEntryData.currency, "USD") < SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CreditTransferFeeTypeSettings.MINIMUM_TRANSFER_AMOUNT_REQUIRED_IN_USD.forTransferFee(transferType))) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.TRANSER_AMOUNT_TOO_LOW, new Object[0]);
               } else {
                  double transferFeePercentage = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.CreditTransferFeeTypeSettings.FEE_PERCENTAGE.forTransferFee(transferType));
                  double transferFee = Numerics.floor(Math.abs(creditTransferAccountEntryData.amount) * transferFeePercentage / 100.0D, 2);
                  double minTransferBalanceAfterTransferInUserCurrency = (Double)((HashMap)transferUserData.get(sender)).get("minBalanceAfterTransfer");
                  double senderBalance = (Double)((HashMap)transferUserData.get(sender)).get("balance");
                  if (senderBalance - transferFee < minTransferBalanceAfterTransferInUserCurrency) {
                     double maxAllowedTransferAmount = Numerics.floor((senderBalance + Math.abs(creditTransferAccountEntryData.amount) - minTransferBalanceAfterTransferInUserCurrency) / (1.0D + transferFeePercentage / 100.0D), 2);
                     throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.BELOW_MIN_BALANCE, new Object[]{String.format("You do not have sufficient balance to make the transfer to %s. The maximum possible credits you can transfer is %s %s.", receipient, creditTransferAccountEntryData.currency, maxAllowedTransferAmount)});
                  } else {
                     boolean creditFeeToMig33 = transferType == CreditTransferData.CreditTransferFeeEnum.NON_TOP_MERCHANT_TO_NON_TOP_MERCHANT || transferType == CreditTransferData.CreditTransferFeeEnum.NON_TOP_MERCHANT_TO_TOP_MERCHANT;
                     MerchantTagData merchantTagData = null;
                     AccountEntryData transferFeeData;
                     if (!creditFeeToMig33) {
                        merchantTagData = this.getMerchantTagFromUsername((Connection)null, receipient, false);
                        if (merchantTagData == null || merchantTagData.merchantUserID == Integer.valueOf(String.valueOf(((HashMap)transferUserData.get(sender)).get("userID")))) {
                           transferFeeData = null;
                           return transferFeeData;
                        }
                     }

                     transferFeeData = new AccountEntryData();
                     transferFeeData.username = sender.toLowerCase();
                     if (creditFeeToMig33) {
                        transferFeeData.type = AccountEntryData.TypeEnum.TRANSFER_CREDIT_FEE;
                     } else {
                        transferFeeData.type = AccountEntryData.TypeEnum.TRANSFER_CREDIT_FEE_TO_TAGGER;
                     }

                     String description = "Transfer fee deducted for %s [%s].";
                     if (transferType == CreditTransferData.CreditTransferFeeEnum.NON_TOP_MERCHANT_TO_NON_TOP_MERCHANT) {
                        description = String.format(description, "blue ID credit transfer", receipient);
                     } else if (transferType == CreditTransferData.CreditTransferFeeEnum.NON_TOP_MERCHANT_TO_TOP_MERCHANT) {
                        description = String.format(description, "transfer to a merchant", receipient);
                     } else if (transferType == CreditTransferData.CreditTransferFeeEnum.TOP_MERCHANT_TO_NON_TOP_MERCHANT) {
                        description = String.format(description, "transfer to a user not tagged by you", receipient);
                     } else {
                        description = String.format(description, "transfer to a merchant not tagged by you", receipient);
                     }

                     transferFeeData.description = description;
                     transferFeeData.reference = String.valueOf(creditTransferAccountEntryData.id);
                     transferFeeData.currency = creditTransferAccountEntryData.currency;
                     transferFeeData.amount = -transferFee;
                     transferFeeData.tax = 0.0D;
                     transferFeeData = this.createAccountEntry(conn, transferFeeData, new AccountEntrySourceData(AccountBean.class));
                     if (!creditFeeToMig33) {
                        UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
                        UserData tagger = userBean.loadUserFromID(merchantTagData.merchantUserID);
                        double transferFeeInTaggersBaseCurrency = this.convertCurrency(transferFee, creditTransferAccountEntryData.currency, tagger.currency);
                        this.convertCurrency(transferFeeData.fundedAmount, creditTransferAccountEntryData.currency, tagger.currency);
                        AccountEntryData taggerTransferFeeData = new AccountEntryData();
                        taggerTransferFeeData.username = tagger.username.toLowerCase();
                        taggerTransferFeeData.type = AccountEntryData.TypeEnum.TRANSFER_CREDIT_FEE_TO_TAGGER;
                        taggerTransferFeeData.reference = String.valueOf(transferFeeData.id);
                        if (receipientType == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                           taggerTransferFeeData.description = "Transfer fee charges received from your tagged Merchant, " + receipient;
                        } else {
                           taggerTransferFeeData.description = "Transfer fee charges received from your tagged user, " + receipient;
                        }

                        taggerTransferFeeData.currency = tagger.currency;
                        taggerTransferFeeData.amount = transferFeeInTaggersBaseCurrency;
                        taggerTransferFeeData.tax = 0.0D;
                        this.createAccountEntry(conn, taggerTransferFeeData, new AccountEntrySourceData(AccountBean.class));
                     }

                     AccountEntryData var62 = transferFeeData;
                     return var62;
                  }
               }
            }
         } catch (CreateException var55) {
            throw new EJBException(var55.getMessage(), var55);
         } catch (SQLException var56) {
            throw new EJBException(var56.getMessage(), var56);
         } catch (Exception var57) {
            throw new EJBException(var57.getMessage(), var57);
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var54) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var53) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var52) {
               ch = null;
            }

         }
      } else {
         return null;
      }
   }

   private static Integer getInteger(ResultSet rs, String columnLabel) throws SQLException {
      int intVal = rs.getInt(columnLabel);
      return rs.wasNull() ? null : new Integer(intVal);
   }

   private static boolean areEquals(Integer intObj, int intVal) {
      return intObj != null && intObj == intVal;
   }

   public CreditTransferData transferCredit(String fromUsername, String toUsername, double amount, boolean overrideCountryAllowTransferSetting, String pin, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         fromUsername = UsernameUtils.normalizeUsername(fromUsername);
         toUsername = UsernameUtils.normalizeUsername(toUsername);
         if (fromUsername != null && toUsername != null) {
            if (fromUsername.length() != 0 && toUsername.length() != 0) {
               if (fromUsername.equalsIgnoreCase(toUsername)) {
                  throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.SELF_TRANSFER_CREDIT_DISALLOWED, new Object[0]);
               } else {
                  amount = Numerics.floor(amount, 2);
                  if (amount < 0.01D) {
                     throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.TRANSER_AMOUNT_TOO_LOW, new Object[0]);
                  } else {
                     conn = this.dataSourceMaster.getConnection();
                     ps = conn.prepareStatement("select user.type, user.mobileverified, uea.verified emailverified, user.currency, user.balance, currency.exchangerate, user.countryid, country.minbalanceaftertransfer, country.allowusertransfertoothercountry, country.allowusertransferwithincountry, country.name countryname, userid.id userid, uv.verified accountVerified from user, country, currency, userid LEFT OUTER JOIN useremailaddress uea ON userid.id = uea.userid and uea.type = ? LEFT OUTER JOIN userverified uv ON uv.userid = userid.id where user.countryid = country.id and user.currency = currency.code and user.username = userid.username and user.username = ?");
                     ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
                     ps.setString(2, fromUsername);
                     rs = ps.executeQuery();
                     if (!rs.next()) {
                        throw new EJBException("Unable to determine the currency code for user " + fromUsername);
                     } else {
                        String currency = rs.getString("currency");
                        double exchangeRate = rs.getDouble("exchangerate");
                        double balance = rs.getDouble("balance");
                        double minBalanceAfterTransferPerCountryAtSystemBaseCurrency = rs.getDouble("minbalanceaftertransfer");
                        double minBalanceAfterTransferPerCountryAtUsersCurrency = minBalanceAfterTransferPerCountryAtSystemBaseCurrency * exchangeRate;
                        int senderCountryID = rs.getInt("countryid");
                        String senderCountryName = rs.getString("countryname");
                        UserData.TypeEnum senderUserType = UserData.TypeEnum.fromValue(rs.getInt("type"));
                        Integer senderAccountVerifiedCode = getInteger(rs, "accountVerified");
                        UserData.AccountVerifiedEnum senderAccountVerified = senderAccountVerifiedCode != null ? UserData.AccountVerifiedEnum.fromValue(senderAccountVerifiedCode) : null;
                        boolean allowUserTransferToOtherCountry = rs.getBoolean("allowusertransfertoothercountry");
                        boolean allowUserTransferWithInCountry = rs.getBoolean("allowusertransferwithincountry");
                        int senderUserID = rs.getInt("userid");
                        this.transferCreditDoEntitlementChecks(fromUsername, toUsername, senderUserType, senderAccountVerified, senderUserID);
                        if (balance < amount) {
                           throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.INSUFFICIENT_CREDIT, new Object[0]);
                        } else {
                           double requiredMinBalanceAfterTransferAtUsersCurrency = minBalanceAfterTransferPerCountryAtUsersCurrency;
                           if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.ENABLE_MIN_BALANCE_CALC_ON_TRANSFER)) {
                              Date now = new Date(System.currentTimeMillis());
                              int numDaysToLookBack = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.MIN_BALANCE_MAX_LOOK_BACK_DAYS);
                              double nonTransferableAmount = this.getNonTransferableFundsAtSystemBaseCurrency(fromUsername, now, numDaysToLookBack, conn, false);
                              if (minBalanceAfterTransferPerCountryAtSystemBaseCurrency < nonTransferableAmount) {
                                 requiredMinBalanceAfterTransferAtUsersCurrency = nonTransferableAmount * exchangeRate;
                              }
                           }

                           if (balance - amount < requiredMinBalanceAfterTransferAtUsersCurrency) {
                              throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.BELOW_MIN_BALANCE, new Object[]{String.format("migCredit Transfer stopped. You cannot transfer below your current minimum balance of %s %s", Numerics.toTwoDecMoneyDigit(requiredMinBalanceAfterTransferAtUsersCurrency), currency)});
                           } else if ((pin == null || pin.length() == 0) && (senderUserType == UserData.TypeEnum.MIG33_MERCHANT && SystemProperty.getBool("EnforceMerchantPIN", false) || senderUserType == UserData.TypeEnum.MIG33_TOP_MERCHANT && SystemProperty.getBool("EnforceSuperMerchantPIN", true))) {
                              throw new EJBException("You must create a merchant PIN before you can transfer credit");
                           } else {
                              boolean fromUserMobileVerified = areEquals(getInteger(rs, "mobileverified"), 1);
                              boolean fromUserEmailVerified = areEquals(getInteger(rs, "emailverified"), 1);
                              if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.TRANSFER_CREDIT_OUT, new AuthenticatedAccessControlParameter(fromUsername, fromUserMobileVerified, fromUserEmailVerified))) {
                                 throw new EJBException("You must authenticate your account before you can transfer credit");
                              } else {
                                 rs.close();
                                 ps.close();
                                 ps = conn.prepareStatement("select id from phonecall where username = ? and status = ?");
                                 ps.setString(1, fromUsername);
                                 ps.setInt(2, CallData.StatusEnum.IN_PROGRESS.value());
                                 rs = ps.executeQuery();
                                 if (rs.next()) {
                                    throw new EJBException("You can't transfer credit when you have a call in progress");
                                 } else {
                                    rs.close();
                                    ps.close();
                                    ps = conn.prepareStatement("select user.type, user.mobileverified, uea.verified emailverified, user.merchantcreated, country.id, country.name, country.allowusertransferfromothercountry, userid.id userid from user, country, userid LEFT OUTER JOIN useremailaddress uea ON userid.id = uea.userid and uea.type = ? where userid.username=user.username and user.countryid=country.id and user.username = ?");
                                    ps.setInt(1, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
                                    ps.setString(2, toUsername);
                                    rs = ps.executeQuery();
                                    if (!rs.next()) {
                                       throw new EJBException("Invalid user " + toUsername);
                                    } else {
                                       boolean isMerchantCreated = fromUsername.equals(rs.getString("merchantcreated"));
                                       boolean toUserMobileVerified = areEquals(getInteger(rs, "mobileverified"), 1);
                                       boolean toUserEmailVerified = areEquals(getInteger(rs, "emailverified"), 1);
                                       if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.RECEIVE_CREDIT_TRANSFER, new AuthenticatedAccessControlParameter(toUsername, toUserMobileVerified || isMerchantCreated, toUserEmailVerified || isMerchantCreated))) {
                                          throw new EJBException("You can't transfer credit to an account that is not authenticated");
                                       } else {
                                          int receiverCountryID = rs.getInt("id");
                                          String receiverCountryName = rs.getString("name");
                                          UserData.TypeEnum receiverUserType = UserData.TypeEnum.fromValue(rs.getInt("type"));
                                          boolean allowUserTransferFromOtherCountry = rs.getBoolean("allowusertransferfromothercountry");
                                          int receiverUserID = rs.getInt("userid");
                                          if (senderUserID == receiverUserID) {
                                             throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.SELF_TRANSFER_CREDIT_DISALLOWED, new Object[0]);
                                          } else if (!overrideCountryAllowTransferSetting && senderCountryID == receiverCountryID && !allowUserTransferWithInCountry) {
                                             throw new EJBException("Local credit transfer within " + senderCountryName + " is not available at this time. Please contact migme (contact@mig.me) for assistance");
                                          } else if (!overrideCountryAllowTransferSetting && !allowUserTransferToOtherCountry && senderCountryID != receiverCountryID) {
                                             throw new EJBException("International transfer from " + senderCountryName + " is not available at this time. Please contact migme (contact@mig.me) for assistance");
                                          } else if (!overrideCountryAllowTransferSetting && !allowUserTransferFromOtherCountry && senderCountryID != receiverCountryID) {
                                             throw new EJBException("International transfer to " + receiverCountryName + " is not available at this time. Please contact migme (contact@mig.me) for assistance");
                                          } else {
                                             AuthenticationServicePrx authenticationServicePrx = EJBIcePrxFinder.getAuthenticationServiceProxy();

                                             try {
                                                if (authenticationServicePrx.exists(senderUserID, PasswordType.MERCHANT_PIN_AUTH.value()) == AuthenticationServiceResponseCodeEnum.Success) {
                                                   if (!StringUtils.hasLength(pin)) {
                                                      throw new EJBException("Invalid PIN");
                                                   }

                                                   String actualPIN = authenticationServicePrx.getCredential(senderUserID, PasswordType.MERCHANT_PIN_AUTH.value()).userCredential.password;
                                                   if (!actualPIN.equalsIgnoreCase(pin)) {
                                                      throw new EJBException("Invalid PIN");
                                                   }
                                                }
                                             } catch (FusionException var72) {
                                                throw new EJBException("Unable to verify PIN");
                                             }

                                             MerchantTagData merchantTagData = null;
                                             boolean tagMerchant = false;
                                             if (senderUserType == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                                                merchantTagData = this.getMerchantTagFromUsername(conn, toUsername, false);
                                                if (receiverUserType == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                                                   if (!SystemProperty.getBool("OfflineTopMerchantTagEnabled", true)) {
                                                      double nettTransfer = this.getNETTTransfer(conn, fromUsername, toUsername, (Date)null, SystemProperty.getInt((String)"MerchantMerchantTagInterval", 43200));
                                                      double minRequirementUSD = SystemProperty.getDouble("MinMerchantMerchantTagAmountUSD", 100.0D);
                                                      double minRequirement = this.convertCurrency(minRequirementUSD, "USD", CurrencyData.baseCurrency);
                                                      tagMerchant = amount / exchangeRate + nettTransfer >= minRequirement;
                                                   }
                                                } else {
                                                   List<String> countryIDs = Arrays.asList(SystemProperty.getArray("receiverCountryIDsExemptedFromSameCountryTagRule", new String[0]));
                                                   if (senderCountryID == receiverCountryID || countryIDs.contains(Integer.toString(receiverCountryID))) {
                                                      double minTagAmountRequired = 0.0D;

                                                      try {
                                                         Map<String, Object> minRequired = this.getMinTopMerchantToNonTopMerchantTagAmount(receiverCountryID, currency);
                                                         minTagAmountRequired = (Double)minRequired.get("amount");
                                                      } catch (Exception var71) {
                                                         log.error("Unable to retrieve minimum required tag amount: [" + fromUsername + "] to [" + toUsername + "] with amount [" + amount + "]:: " + var71.getMessage());
                                                         throw new Exception("We are unable to process your request at the moment. Please try again at a later time.");
                                                      }

                                                      if (amount >= minTagAmountRequired) {
                                                         if (merchantTagData == null) {
                                                            tagMerchant = true;
                                                         } else if (merchantTagData.merchantUserID == senderUserID) {
                                                            tagMerchant = true;
                                                         } else {
                                                            try {
                                                               if (merchantTagData.currency == null) {
                                                                  AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                                                                  MerchantTagData tagData = accountBean.fixNullAccountentryInMerchantTag(conn, merchantTagData);
                                                                  merchantTagData.amount = tagData.amount;
                                                                  merchantTagData.currency = tagData.currency;
                                                               }
                                                            } catch (Exception var70) {
                                                               log.error("MERCHANT TAG:: Unable to determine amount for tag to be transfered for merchant:" + fromUsername + " to user:" + toUsername);
                                                            }
                                                         }
                                                      }
                                                   }
                                                }
                                             }

                                             AccountEntryData fromEntry = new AccountEntryData();
                                             fromEntry.username = fromUsername.toLowerCase();
                                             fromEntry.type = AccountEntryData.TypeEnum.USER_TO_USER_TRANSFER;
                                             fromEntry.reference = "";
                                             fromEntry.description = "Transfer " + Numerics.toTwoDecMoneyDigit(amount) + " " + currency + " to " + toUsername;
                                             fromEntry.currency = currency;
                                             fromEntry.amount = -amount;
                                             fromEntry.tax = 0.0D;
                                             fromEntry = this.createAccountEntry(conn, fromEntry, accountEntrySourceData);
                                             if (fromEntry.amount > -0.01D) {
                                                throw new EJBException("Transfer amount is too low");
                                             } else {
                                                AccountEntryData toEntry = new AccountEntryData();
                                                toEntry.username = toUsername.toLowerCase();
                                                toEntry.type = AccountEntryData.TypeEnum.USER_TO_USER_TRANSFER;
                                                toEntry.reference = fromEntry.id.toString();
                                                toEntry.description = Numerics.toTwoDecMoneyDigit(amount) + " " + currency + " transferred from " + fromUsername;
                                                toEntry.currency = currency;
                                                toEntry.amount = amount;
                                                toEntry.fundedAmount = -fromEntry.fundedAmount;
                                                toEntry.tax = 0.0D;
                                                toEntry = this.createAccountEntry(conn, toEntry, new AccountEntrySourceData(AccountBean.class));
                                                ps.close();
                                                ps = conn.prepareStatement("update accountentry set reference = ? where id = ?");
                                                ps.setString(1, toEntry.id.toString());
                                                ps.setLong(2, fromEntry.id);
                                                if (ps.executeUpdate() == 1) {
                                                   ps.close();
                                                   AccountBalanceData accountBalanceData = this.getAccountBalance(fromUsername);
                                                   balance = accountBalanceData.balance;
                                                   if (balance < 0.0D) {
                                                      throw new EJBException("Insufficient credit");
                                                   } else if (balance < minBalanceAfterTransferPerCountryAtUsersCurrency) {
                                                      throw new EJBException("You must leave at least " + Numerics.toTwoDecMoneyDigit(minBalanceAfterTransferPerCountryAtUsersCurrency) + " " + currency + " in your account. For more help, email contact@mig.me");
                                                   } else {
                                                      Enums.MerchantTagStatEnum stat = Enums.MerchantTagStatEnum.FAILED;
                                                      if (tagMerchant) {
                                                         if (this.tagMerchant(conn, receiverUserID, toUsername, senderUserID, fromUsername, merchantTagData, (Date)null, fromEntry.id, MerchantTagData.StatusEnum.ACTIVE.value())) {
                                                            if (merchantTagData == null) {
                                                               stat = Enums.MerchantTagStatEnum.CREATED;
                                                            } else {
                                                               stat = Enums.MerchantTagStatEnum.TRANSFERED;
                                                            }
                                                         }

                                                         try {
                                                            this.incrementMerchantTagStat(conn, stat);
                                                         } catch (EJBException var69) {
                                                            log.debug("MERCHANT TAG:: exception in incrementing merchant tag stat: " + var69.getMessage());
                                                            throw var69;
                                                         }
                                                      } else if (senderUserType == UserData.TypeEnum.MIG33_TOP_MERCHANT && receiverUserType != UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                                                         this.incrementMerchantTagStat(conn, stat);
                                                      }

                                                      this.chargeCreditTransferFee(fromUsername, toUsername, fromEntry, conn);
                                                      if (SystemProperty.getBool("OfflineTopMerchantTagEnabled", true) && senderUserType == UserData.TypeEnum.MIG33_TOP_MERCHANT && receiverUserType == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
                                                         MerchantCenter.getInstance().createTagEntry(fromEntry.id);
                                                      }

                                                      try {
                                                         UserNotificationServicePrx unsProxy = EJBIcePrxFinder.getUserNotificationServiceProxy();
                                                         Map<String, String> parameters = new HashMap();
                                                         parameters.put("amount", toEntry.amount.toString());
                                                         parameters.put("currency", toEntry.currency);
                                                         parameters.put("senderUserId", Integer.toString(senderUserID));
                                                         String key = String.format("%d", toEntry.id);
                                                         Message m = new Message(key, receiverUserID, toUsername, Enums.NotificationTypeEnum.INCOMING_CREDIT_TRANSFER_ALERT.getType(), System.currentTimeMillis(), parameters);
                                                         unsProxy.notifyFusionUser(m);
                                                      } catch (Exception var68) {
                                                         log.error("Unknown error while contacting UNS to generate credit transfer alert:" + var68.getMessage());
                                                      }

                                                      CreditTransferData var94 = new CreditTransferData(fromEntry, accountBalanceData);
                                                      return var94;
                                                   }
                                                } else {
                                                   throw new EJBException("Failed to update transferer's account entry");
                                                }
                                             }
                                          }
                                       }
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               }
            } else {
               throw new EJBExceptionWithErrorCause(ErrorCause.InvalidUserName.VALUE_IS_BLANK, new Object[0]);
            }
         } else {
            throw new EJBExceptionWithErrorCause(ErrorCause.InvalidUserName.VALUE_NOT_SPECIFIED, new Object[0]);
         }
      } catch (EJBExceptionWithErrorCause var73) {
         throw var73;
      } catch (SQLException var74) {
         throw new EJBException(var74.getMessage(), var74);
      } catch (EJBException var75) {
         throw var75;
      } catch (Exception var76) {
         throw new EJBException(var76.getMessage(), var76);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var67) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var66) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var65) {
            conn = null;
         }

      }
   }

   private void transferCreditDoEntitlementChecks(String fromUsername, String toUsername, UserData.TypeEnum senderUserType, UserData.AccountVerifiedEnum senderAccountVerified, int senderUserID) throws CreateException, EJBException, EJBExceptionWithErrorCause {
      String fromUsernameLower = fromUsername.toLowerCase();
      Iterator i$ = bannedCreditSenders.fetchValue().iterator();

      String passedReason;
      do {
         if (!i$.hasNext()) {
            CreditTransferUtils creditTransferUtils = CreditTransferUtils.getInstance();
            if (creditTransferUtils.migLevelCheckEnabled()) {
               if (creditTransferUtils.applyMigLevelCheckForVerifiedAccountStatusValue(senderAccountVerified)) {
                  if (creditTransferUtils.userTypeRequiresMigLevelCheck(senderUserType)) {
                     if (!creditTransferUtils.isUserIdExemptedFromMigLevelCheck(senderUserID)) {
                        int senderReputationLevel = getMigLevel(senderUserID);
                        if (senderReputationLevel < SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.MIN_MIG_LEVEL)) {
                           log.info("transferCreditDoEntitlementChecks:Insufficient miglevel for credit transfer: user[" + fromUsername + "] level [" + senderReputationLevel + "] attempted to transfer to [" + toUsername + "]");
                           throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.INSUFFICIENT_MIG_LEVEL, new Object[]{SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.INSUFFICIENT_MIG_LEVEL_ERROR_MESSAGE)});
                        }

                        passedReason = "MigLevel(" + senderReputationLevel + ")";
                     } else {
                        passedReason = "WhiteListed";
                     }
                  } else {
                     passedReason = "UserType(" + senderUserType + ")";
                  }
               } else {
                  passedReason = "AccountVerifiedStatus(" + senderAccountVerified + ")";
               }

               if (passedReason != null && SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.LOG_SENDERS_PASSING_TRANSFER_ENTITLEMENT_CHECKS)) {
                  log.info("transferCreditDoEntitlementChecks:Passed Sender [" + fromUsername + "] " + "Reason [" + passedReason + "]");
               }
            }

            return;
         }

         passedReason = (String)i$.next();
      } while(passedReason == null || !fromUsernameLower.contains(passedReason.toLowerCase()));

      log.warn("Attempt to transfer credit out of banned sender account: fromUsername=" + fromUsername + " toUsername=" + toUsername + " matched pattern=" + passedReason);
      throw new EJBExceptionWithErrorCause(ErrorCause.TransferCreditErrorReasonType.BANNED_SENDER, new Object[]{"Transfer from this account is not allowed"});
   }

   private static int getMigLevel(int userID) throws CreateException, EJBException {
      UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
      boolean skipCachedScore = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction_CreditTransfer.SKIP_FETCH_CACHED_REPUTATION_SCORE);
      ReputationLevelData reputation = userBean.getReputationLevelByUserid(userID, skipCachedScore);
      int senderReputationLevel = reputation != null && reputation.level != null ? reputation.level : 1;
      return senderReputationLevel;
   }

   public Map<String, Object> getMinTopMerchantToNonTopMerchantTagAmount(int countryId) throws Exception {
      return this.getMinTopMerchantToNonTopMerchantTagAmount(countryId, (String)null);
   }

   public Map<String, Object> getMinTopMerchantToNonTopMerchantTagAmount(int countryId, String currency) throws Exception {
      PreparedStatement ps = null;
      ResultSet rs = null;
      Connection conn = null;
      HashMap minimumRequirement = new HashMap();

      HashMap var11;
      try {
         minimumRequirement.put("validity", MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         Double minimum = SystemProperty.getDouble("MinMerchantUserTagAmountUSD", 0.1D);
         conn = this.dataSourceSlave.getConnection();
         String reqCurrency = "USD";
         String query = "SELECT IFNULL(minNonTopMerchantTagAmount, 0) amount, currency FROM country WHERE id = ?";
         ps = conn.prepareStatement(query);
         ps.setInt(1, countryId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.error("Unable to get merchant tag amount :: countryId [" + countryId + "] currency [" + currency + "]");
            throw new Exception("Unable to get merchant tag amount.");
         }

         if (rs.getDouble("amount") > 0.0D && SystemProperty.getBool("NonTopMerchantTagMinimumAmountOverrideEnabled", false)) {
            minimum = rs.getDouble("amount");
            reqCurrency = rs.getString("currency");
         }

         if (currency == null) {
            currency = rs.getString("currency");
         }

         rs.close();
         ps.close();
         query = "SELECT (?/fromcurrency.exchangerate)*tocurrency.exchangerate amount, tocurrency.code currency FROM currency fromcurrency, currency tocurrency WHERE fromcurrency.code = ? AND tocurrency.code = ?";
         ps = conn.prepareStatement(query);
         ps.setDouble(1, minimum);
         ps.setString(2, reqCurrency);
         ps.setString(3, currency);
         rs = ps.executeQuery();
         if (!rs.next()) {
            log.error("Unable to get merchant tag amount :: countryId [" + countryId + "] currency [" + currency + "]");
            throw new Exception("Unable to get merchant tag amount.");
         }

         Double minNonTopMerchantTagAmount = Math.ceil(rs.getDouble("amount") * 100.0D) / 100.0D;
         minimumRequirement.put("amount", minNonTopMerchantTagAmount);
         minimumRequirement.put("currency", rs.getString("currency"));
         var11 = minimumRequirement;
      } catch (Exception var26) {
         throw new Exception(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return var11;
   }

   public double getNETTTransfer(Connection conn, String fromUsername, String toUsername, Date timeOfAction, int minutes) throws SQLException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         long timeOfActionMillis = 0L;
         if (timeOfAction != null) {
            timeOfActionMillis = timeOfAction.getTime();
         } else {
            timeOfActionMillis = System.currentTimeMillis();
         }

         Calendar beginCalendar = Calendar.getInstance();
         beginCalendar.setTimeInMillis(timeOfActionMillis);
         beginCalendar.add(12, -minutes);
         Timestamp fromCreateDate = new Timestamp(beginCalendar.getTimeInMillis());
         Timestamp toCreateDate = new Timestamp(timeOfActionMillis);
         ch = new ConnectionHolder(this.dataSourceSlave, conn);
         ps = ch.getConnection().prepareStatement(" SELECT -sum(amount/exchangerate)  FROM accountentry WHERE type = ? and datecreated between ? and ? and username = ? and amount < 0 and substring_index(description, ' ', -1)  = ? UNION SELECT sum(amount/exchangerate) FROM accountentry WHERE type = ? and datecreated between ? and ? and username = ? and amount < 0 and substring_index(description, ' ', -1)  = ?");
         int accountEntryType = AccountEntryData.TypeEnum.USER_TO_USER_TRANSFER.value();
         ps.setInt(1, accountEntryType);
         ps.setTimestamp(2, fromCreateDate);
         ps.setTimestamp(3, toCreateDate);
         ps.setString(4, fromUsername);
         ps.setString(5, toUsername);
         ps.setInt(6, accountEntryType);
         ps.setTimestamp(7, fromCreateDate);
         ps.setTimestamp(8, toCreateDate);
         ps.setString(9, toUsername);
         ps.setString(10, fromUsername);
         BigDecimal nett = BigDecimal.ZERO;
         rs = ps.executeQuery();

         while(rs.next()) {
            BigDecimal sum = rs.getBigDecimal(1);
            if (sum != null) {
               nett = nett.add(sum);
            }
         }

         double var32 = nett.doubleValue();
         return var32;
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
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var28) {
            ch = null;
         }

      }
   }

   public BasicMerchantTagDetailsData getMerchantTagFromUsername(Connection conn, String username, boolean fromSlave) throws EJBException {
      return SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.USE_NESTED_QUERY_FOR_MERCHANT_TAG_RETRIEVAL) ? this.getMerchantTagFromUsername_NestedQuery(conn, username, fromSlave) : this.getMerchantTagFromUsername_NonNestedQuery(conn, username, fromSlave);
   }

   /** @deprecated */
   @Deprecated
   private BasicMerchantTagDetailsData getMerchantTagFromUsername_NestedQuery(Connection conn, String username, boolean fromSlave) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BasicMerchantTagDetailsData var10;
      try {
         Calendar topMerchantCutoff = Calendar.getInstance();
         Calendar nonTopmerchantCutoff = Calendar.getInstance();
         topMerchantCutoff.add(12, -MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         nonTopmerchantCutoff.add(12, -MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         if (fromSlave) {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
         } else {
            ch = new ConnectionHolder(this.dataSourceMaster, conn);
         }

         String sql = "SELECT mt.*        , u.username merchantusername        , u.type merchantusertype        , ttag.username username        , ttag.type usertype        , ABS(a.amount) amount        , a.currency        , CASE WHEN ttag.type = ? THEN DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE)          ELSE DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE) END expiry        , 1 AS status        , u.displayPicture        , mt.accountentryid FROM user u      , merchanttag mt LEFT JOIN accountentry a ON mt.accountentryid = a.id      , userid ui      , ( SELECT MAX(mt.id) id, u.username, u.type          FROM userid ui, merchanttag mt, user u          WHERE ui.id = mt.userid          AND ui.username = u.username          AND ui.username = ?          AND mt.status = ?) ttag WHERE u.username = ui.username AND ui.id = mt.merchantuserid AND mt.id = ttag.id AND ((ttag.type < ? AND mt.lastsalesdate >= ?)      OR (ttag.type = ? AND mt.lastsalesdate >= ?))";
         ps = ch.getConnection().prepareStatement(sql);
         ps.setInt(1, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         ps.setInt(2, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         ps.setInt(3, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         ps.setString(4, username);
         ps.setInt(5, MerchantTagData.StatusEnum.ACTIVE.value());
         ps.setInt(6, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         ps.setTimestamp(7, new Timestamp(nonTopmerchantCutoff.getTimeInMillis()));
         ps.setInt(8, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         ps.setTimestamp(9, new Timestamp(topMerchantCutoff.getTimeInMillis()));
         rs = ps.executeQuery();
         if (rs.next()) {
            var10 = new BasicMerchantTagDetailsData(rs);
            return var10;
         }

         var10 = null;
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var25) {
            ch = null;
         }

      }

      return var10;
   }

   private BasicMerchantTagDetailsData getMerchantTagFromUsername_NonNestedQuery(Connection conn, String username, boolean fromSlave) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BasicMerchantTagDetailsData var11;
      try {
         Calendar topMerchantCutoff = Calendar.getInstance();
         Calendar nonTopmerchantCutoff = Calendar.getInstance();
         topMerchantCutoff.add(12, -MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         nonTopmerchantCutoff.add(12, -MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         if (fromSlave) {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
         } else {
            ch = new ConnectionHolder(this.dataSourceMaster, conn);
         }

         Integer lastActiveMerchantTagID = this.getLatestActiveMerchantTagID(ch, username);
         String sql;
         if (lastActiveMerchantTagID == null) {
            sql = null;
            return sql;
         }

         sql = " select mt.*,  merchantuser.username merchantusername, merchantuser.type merchantusertype, taggeduser.username username, taggeduser.type usertype, ABS(a.amount) amount,  a.currency, CASE WHEN taggeduser.type = ? THEN DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE)          ELSE DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE) END expiry,  1 AS status,  merchantuser.displayPicture,  mt.accountentryid  FROM merchanttag mt  JOIN userid merchantuserid ON (mt.id = ? AND mt.merchantuserid = merchantuserid.id)  JOIN userid taggeduserid ON (mt.userid = taggeduserid.id)  JOIN user merchantuser ON (merchantuser.username=merchantuserid.username)  JOIN user taggeduser ON (taggeduser.username=taggeduserid.username)  LEFT JOIN accountentry a ON (mt.accountentryid = a.id)  WHERE ((taggeduser.type < ? AND mt.lastsalesdate >= ?)  OR (taggeduser.type=? AND mt.lastsalesdate >=?))";
         ps = ch.getConnection().prepareStatement(" select mt.*,  merchantuser.username merchantusername, merchantuser.type merchantusertype, taggeduser.username username, taggeduser.type usertype, ABS(a.amount) amount,  a.currency, CASE WHEN taggeduser.type = ? THEN DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE)          ELSE DATE_ADD(mt.lastsalesdate, INTERVAL ? MINUTE) END expiry,  1 AS status,  merchantuser.displayPicture,  mt.accountentryid  FROM merchanttag mt  JOIN userid merchantuserid ON (mt.id = ? AND mt.merchantuserid = merchantuserid.id)  JOIN userid taggeduserid ON (mt.userid = taggeduserid.id)  JOIN user merchantuser ON (merchantuser.username=merchantuserid.username)  JOIN user taggeduser ON (taggeduser.username=taggeduserid.username)  LEFT JOIN accountentry a ON (mt.accountentryid = a.id)  WHERE ((taggeduser.type < ? AND mt.lastsalesdate >= ?)  OR (taggeduser.type=? AND mt.lastsalesdate >=?))");
         ps.setInt(1, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         ps.setInt(2, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         ps.setInt(3, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         ps.setInt(4, lastActiveMerchantTagID);
         ps.setInt(5, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         ps.setTimestamp(6, new Timestamp(nonTopmerchantCutoff.getTimeInMillis()));
         ps.setInt(7, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
         ps.setTimestamp(8, new Timestamp(topMerchantCutoff.getTimeInMillis()));
         rs = ps.executeQuery();
         if (rs.next()) {
            var11 = new BasicMerchantTagDetailsData(rs);
            return var11;
         }

         var11 = null;
      } catch (SQLException var32) {
         throw new EJBException(var32.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var29) {
            ch = null;
         }

      }

      return var11;
   }

   private Integer getLatestActiveMerchantTagID(ConnectionHolder connHolder, String taggedUsername) throws SQLException {
      String sql = "SELECT  max(mt.id) lastid  FROM merchanttag mt JOIN userid uid on (mt.userid=uid.id and uid.username=?)  where mt.status=?";
      PreparedStatement ps = connHolder.getConnection().prepareStatement("SELECT  max(mt.id) lastid  FROM merchanttag mt JOIN userid uid on (mt.userid=uid.id and uid.username=?)  where mt.status=?");

      Object lastid;
      try {
         ps.setString(1, taggedUsername);
         ps.setInt(2, MerchantTagData.StatusEnum.ACTIVE.value());
         ResultSet rs = ps.executeQuery();

         try {
            if (rs.next()) {
               lastid = rs.getInt("lastid");
               Integer var7;
               if (rs.wasNull()) {
                  var7 = null;
                  return var7;
               }

               var7 = Integer.valueOf((int)lastid);
               return var7;
            }

            lastid = null;
         } finally {
            rs.close();
         }
      } finally {
         ps.close();
      }

      return (Integer)lastid;
   }

   public MerchantTagData getInactiveMerchantTagBetweenUsers(Connection conn, int userid, int merchantUserid, boolean fromSlave) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      MerchantTagData var11;
      try {
         Calendar topMerchantTagCutoff = Calendar.getInstance();
         Calendar nonTopMerchantTagCutoff = Calendar.getInstance();
         topMerchantTagCutoff.add(12, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         nonTopMerchantTagCutoff.add(12, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         if (fromSlave) {
            ch = new ConnectionHolder(this.dataSourceSlave, conn);
         } else {
            ch = new ConnectionHolder(this.dataSourceMaster, conn);
         }

         String sql = "SELECT mt.*        , u.username merchantusername        , u.type merchantusertype        , ttag.username username        , ttag.type usertype FROM user u      , merchanttag mt      , userid ui      , ( SELECT MAX(mt.id) id, u.username, u.type          FROM merchanttag mt, userid ui, user u          WHERE mt.userid = ui.id          AND u.username = ui.username          AND mt.userid = ?          AND mt.merchantuserid = ?          AND mt.status = ?) ttag WHERE u.username = ui.username AND ui.id = mt.merchantuserid AND mt.id = ttag.id";
         ps = ch.getConnection().prepareStatement(sql);
         ps.setInt(1, userid);
         ps.setInt(2, merchantUserid);
         ps.setInt(3, MerchantTagData.StatusEnum.INACTIVE.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            var11 = null;
            return var11;
         }

         var11 = new MerchantTagData(rs);
      } catch (SQLException var29) {
         throw new EJBException(var29.getMessage());
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
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var26) {
            ch = null;
         }

      }

      return var11;
   }

   public void tagMerchantPending(Connection conn, int userID, String username, int merchantUserID, String merchantUsername) throws EJBException {
      try {
         this.tagMerchant(conn, userID, username, merchantUserID, merchantUsername, this.getMerchantTagFromUsername(conn, username, false), (Date)null, (Long)null, MerchantTagData.StatusEnum.PENDING.value());
      } catch (Exception var7) {
         throw new EJBException(var7.getMessage());
      }
   }

   public boolean tagMerchant(Connection conn, int userID, String username, int merchantUserID, String merchantUsername) throws EJBException {
      try {
         return this.tagMerchant(conn, userID, username, merchantUserID, merchantUsername, this.getMerchantTagFromUsername(conn, username, false), (Date)null, (Long)null, MerchantTagData.StatusEnum.ACTIVE.value());
      } catch (Exception var7) {
         throw new EJBException(var7.getMessage());
      }
   }

   public boolean tagMerchant(Connection conn, int userID, String username, int merchantUserID, String merchantUsername, MerchantTagData exisitingMerchantTagData, Date lastSaleDate, Long accountentryID, Integer status) throws Exception {
      ConnectionHolder ch = null;
      ResultSet rs = null;
      PreparedStatement ps = null;
      if (!SystemProperty.getBool("MerchantTaggingEnabled", true)) {
         log.warn("MERCHANT TAG:: merchant tagging is disabled");
         return false;
      } else {
         boolean tagged = false;
         boolean tagMerchant = true;

         try {
            Timestamp now = null;
            if (lastSaleDate != null) {
               now = new Timestamp(lastSaleDate.getTime());
            } else {
               now = new Timestamp(System.currentTimeMillis());
            }

            if (status == null) {
               status = 0;
            }

            ch = new ConnectionHolder(this.dataSourceMaster, conn);
            String query;
            if (exisitingMerchantTagData != null && merchantUserID == exisitingMerchantTagData.merchantUserID) {
               tagMerchant = false;
               query = "UPDATE merchanttag SET lastsalesdate = ?";
               if (accountentryID != null) {
                  query = query + ", accountentryid = ? ";
               }

               query = query + " WHERE id = ?";
               ps = ch.getConnection().prepareStatement(query);
               ps.setTimestamp(1, now);
               if (accountentryID != null) {
                  ps.setLong(2, accountentryID);
                  ps.setInt(3, exisitingMerchantTagData.id);
               } else {
                  ps.setInt(2, exisitingMerchantTagData.id);
               }

               if (ps.executeUpdate() != 1) {
                  log.warn("EXCEPTION: Unable to update merchant tag for user: " + userID + " to merchant: " + merchantUserID);
                  throw new SQLException("Unable to update merchant tag");
               }

               ps.close();
               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, username, merchantUsername);
            } else {
               ps = ch.getConnection().prepareStatement("UPDATE merchanttag SET status = 0 WHERE userID = ?");
               ps.setInt(1, userID);
               if (ps.executeUpdate() < 1) {
                  log.warn("No merchanttag invalidated for userid" + userID);
               }

               ps.close();
               ch.close();
               MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, username);
            }

            if (tagMerchant) {
               ch = new ConnectionHolder(this.dataSourceMaster, conn);
               query = "insert into merchanttag (userid, merchantuserid, datecreated, lastsalesdate, status, accountentryid) values (?,?,?,?,?,?)";
               ps = ch.getConnection().prepareStatement(query, 1);
               ps.setInt(1, userID);
               ps.setInt(2, merchantUserID);
               ps.setTimestamp(3, now);
               ps.setTimestamp(4, now);
               ps.setInt(5, status);
               ps.setObject(6, accountentryID);
               ps.executeUpdate();
               rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  log.info("EXCEPTION: Unable to tag merchant: " + merchantUserID + " to user: " + userID);
                  throw new Exception("Unable to tag merchant");
               }

               MemCachedClientWrapper.set(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, username, merchantUsername);
               MemCachedClientWrapper.incr(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG_COUNT, "" + merchantUserID);
               tagged = true;
            }
         } catch (SQLException var29) {
            throw new Exception(var29.getMessage());
         } catch (Exception var30) {
            throw new Exception(var30.getMessage());
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var28) {
               ps = null;
            }

            try {
               if (ch != null) {
                  ch.close();
               }
            } catch (SQLException var27) {
               ch = null;
            }

         }

         return tagged;
      }
   }

   public void untagMerchant(Connection conn, String username) throws EJBException {
      MerchantTagData merchantTagData = this.getMerchantTagFromUsername(conn, username, false);
      if (merchantTagData != null) {
         this.untagMerchant(conn, merchantTagData, username);
      }

   }

   public void untagMerchant(Connection conn, MerchantTagData merchantTagData, String userName) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;

      try {
         ch = new ConnectionHolder(this.dataSourceMaster, conn);
         ps = ch.getConnection().prepareStatement("update merchanttag set status = ? where id = ?");
         ps.setInt(1, MerchantTagData.StatusEnum.INACTIVE.value());
         ps.setInt(2, merchantTagData.id);
         ps.executeUpdate();
         MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, userName);
      } catch (SQLException var19) {
         throw new EJBException(var19.getMessage());
      } catch (Exception var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var18) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var17) {
            ch = null;
         }

      }

   }

   public void untagMerchant(Connection conn, List<MerchantTagUserData> tagData) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      StringBuilder inClause = new StringBuilder();
      boolean firstValue = true;

      int queryParameterIndex;
      for(queryParameterIndex = 0; queryParameterIndex < tagData.size(); ++queryParameterIndex) {
         inClause.append('?');
         if (firstValue) {
            firstValue = false;
         }

         if (queryParameterIndex < tagData.size() - 1) {
            inClause.append(',');
         }
      }

      try {
         queryParameterIndex = 1;
         ch = new ConnectionHolder(this.dataSourceMaster, conn);
         ps = ch.getConnection().prepareStatement("UPDATE merchanttag SET status = ? WHERE id IN (" + inClause.toString() + ")");
         ps.setInt(1, MerchantTagData.StatusEnum.INACTIVE.value());
         Iterator i$ = tagData.iterator();

         MerchantTagUserData tag;
         while(i$.hasNext()) {
            tag = (MerchantTagUserData)i$.next();
            ++queryParameterIndex;
            ps.setInt(queryParameterIndex, tag.id);
         }

         ps.executeUpdate();
         i$ = tagData.iterator();

         while(i$.hasNext()) {
            tag = (MerchantTagUserData)i$.next();
            MemCachedClientWrapper.delete(MemCachedKeySpaces.CommonKeySpace.MERCHANT_TAG, tag.userName);
         }
      } catch (Exception var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var20) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var19) {
            ch = null;
         }

      }

   }

   public void incrementMerchantTagStat(Connection conn, Enums.MerchantTagStatEnum stat) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;

      try {
         ch = new ConnectionHolder(this.dataSourceMaster, conn);
         ps = ch.getConnection().prepareStatement("INSERT INTO merchanttagstat(date, " + stat + ")" + "VALUES(NOW(), 1) " + "ON DUPLICATE KEY UPDATE " + stat + " = " + stat + "+1");
         ps.executeUpdate();
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var15) {
            ch = null;
         }

      }

   }

   public MerchantTagData fixNullAccountentryInMerchantTag(Connection conn, MerchantTagData merchantTag) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         Date lastSalesDate = DateTimeUtils.getDateWithoutTimeFromISO8601DateString(merchantTag.lastSalesDate);
         int minCreditTransferAccontEntryID = SystemProperty.getInt("MinimumCreditTransferAccountentryID", 716668286);
         ch = new ConnectionHolder(this.dataSourceMaster, conn);
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData merchantUserData = userBean.loadUserFromID(merchantTag.merchantUserID);
         UserData taggedUserData = userBean.loadUserFromID(merchantTag.userID);
         String merchantUser = merchantUserData.username;
         String taggedUser = taggedUserData.username;
         String sql = "SELECT   currency        , ABS(amount) AS amount        , id FROM accountentry a WHERE id >= ? AND type = ? AND DATE(datecreated) = ? AND username = ? AND substring_index(description, ' ', -1) = ? AND amount < 0 ORDER BY id DESC LIMIT 1";
         ps = ch.getConnection().prepareStatement(sql);
         ps.setInt(1, minCreditTransferAccontEntryID);
         ps.setInt(2, AccountEntryData.TypeEnum.USER_TO_USER_TRANSFER.value());
         ps.setTimestamp(3, new Timestamp(lastSalesDate.getTime()));
         ps.setString(4, merchantUser);
         ps.setString(5, taggedUser);
         rs = ps.executeQuery();
         if (log.isDebugEnabled()) {
            log.debug("Retrieving credit transfer accountentry record from merchant [" + merchantUser + "] to tagged user [" + taggedUser + "] with ID greater than [" + minCreditTransferAccontEntryID + "] and lastSalesDate [" + lastSalesDate + "]");
         }

         Long accountentryID = 0L;
         if (rs.next()) {
            merchantTag.amount = rs.getDouble("amount");
            merchantTag.currency = rs.getString("currency");
            accountentryID = rs.getLong("id");
         }

         rs.close();
         ps.close();
         if (accountentryID > 0L) {
            String query = "UPDATE merchanttag SET accountentryid = ? WHERE id = ?";
            ps = ch.getConnection().prepareStatement(query);
            ps.setLong(1, accountentryID);
            ps.setInt(2, merchantTag.id);
            if (ps.executeUpdate() == 0) {
               log.warn("MERCHANT TAG: No tag updated for id=" + merchantTag.id + "; accountentryid=" + accountentryID);
            }

            ps.close();
         } else {
            log.warn("Unable to retrieve credit transfer accountentry record from merchant [" + merchantUser + "] to tagged user [" + taggedUser + "] with ID greater than [" + SystemProperty.getInt("MinimumCreditTransferAccountentryID", 716668286) + "] and lastSalesDate [" + lastSalesDate + "]");
         }
      } catch (SQLException var32) {
         log.error("SQL Exception caught in fixNullAccountentryInMerchantTag :" + var32.getMessage(), var32);
         throw new EJBException(var32.getMessage(), var32);
      } catch (CreateException var33) {
         log.error("EJB CreateException caught in fixNullAccountentryInMerchantTag :" + var33.getMessage(), var33);
         throw new EJBException(var33.getMessage(), var33);
      } catch (Exception var34) {
         log.error("Unknown Exception caught in fixNullAccountentryInMerchantTag :" + var34.getMessage(), var34);
         throw new EJBException(var34.getMessage(), var34);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var29) {
            ch = null;
         }

      }

      return merchantTag;
   }

   public void activatePendingMerchantTag(Connection conn, String username) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;

      try {
         ch = new ConnectionHolder(this.dataSourceMaster, conn);
         ps = ch.getConnection().prepareStatement("UPDATE merchanttag mt, userid ui SET mt.status = ? WHERE mt.userID = ui.id AND mt.status = ? AND ui.username = ?");
         ps.setInt(1, MerchantTagData.StatusEnum.ACTIVE.value());
         ps.setInt(2, MerchantTagData.StatusEnum.PENDING.value());
         ps.setString(3, username);
         ps.executeUpdate();
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var15) {
            ch = null;
         }

      }

   }

   public void reverseTransferCredit(long accEntryID, String MISUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.ENABLE_REVERSAL_TRANSFER_BIGINT_AE_ID)) {
         this.reverseTransferCreditInt64(accEntryID, MISUsername, accountEntrySourceData);
      } else {
         this.reverseTransferCreditInt32(accEntryID, MISUsername, accountEntrySourceData);
      }

   }

   private void reverseTransferCreditInt64(long accEntryID, String MISUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry1 = this.getAccountEntry(accEntryID);
      if (accountEntry1 == null) {
         throw new EJBException("AccountEntryID:[" + accEntryID + "] does not exist");
      } else {
         this.validateAccountEntryIDForTransferReversal(accountEntry1);
         AccountEntryData accountEntry2 = this.getReferencedAccountEntryData(accountEntry1);
         if (accountEntry2 == null) {
            throw new EJBException("AccountEntryID:[" + accountEntry1.id + "] pointing to a non existing accountEntry id = [" + accountEntry1.reference + "]");
         } else if (this.userTransferReversedAlready(accEntryID)) {
            throw new EJBException("The user transfer has been reversed already");
         } else {
            String reverseEntryDescription = "";
            if (accountEntry1.amount < 0.0D) {
               reverseEntryDescription = "Reversal of user transfer from " + accountEntry1.username + " to " + accountEntry2.username + " (Reversed By " + MISUsername + ")";
            } else {
               reverseEntryDescription = "Reversal of user transfer from " + accountEntry2.username + " to " + accountEntry1.username + " (Reversed By " + MISUsername + ")";
            }

            this.refundAccountEntry(accountEntry1.id, String.valueOf(accountEntry1.id), reverseEntryDescription, accountEntrySourceData);
            this.refundAccountEntry(accountEntry2.id, String.valueOf(accountEntry2.id), reverseEntryDescription, accountEntrySourceData);
            this.refundCreditTransferFee(accountEntry1.id, accountEntrySourceData);
         }
      }
   }

   private void validateAccountEntryIDForTransferReversal(AccountEntryData accountEntry) {
      if (accountEntry == null) {
         throw new EJBException("accountEntry cannot be null");
      } else if (StringUtil.isBlank(accountEntry.reference)) {
         throw new EJBException("AccountEntryID:[" + accountEntry.id + "] has blank reference");
      }
   }

   private AccountEntryData getReferencedAccountEntryData(AccountEntryData accountEntry) {
      long secondLegAeID;
      try {
         secondLegAeID = Long.parseLong(accountEntry.reference.trim());
      } catch (NumberFormatException var5) {
         throw new EJBException("Unable to get referenced accountentry from account entry id:[" + accountEntry.id + "]. Unable to parse account entry ref:[" + accountEntry.reference + "]", var5);
      }

      return this.getAccountEntry(secondLegAeID);
   }

   /** @deprecated */
   private void reverseTransferCreditInt32(long accEntryID, String MISUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry1 = this.getAccountEntry(accEntryID);
      AccountEntryData accountEntry2 = this.getAccountEntry((long)Integer.valueOf(accountEntry1.reference));
      if (this.userTransferReversedAlready(accEntryID)) {
         throw new EJBException("The user transfer has been reversed already");
      } else {
         String reverseEntryDescription = "";
         if (accountEntry1.amount < 0.0D) {
            reverseEntryDescription = "Reversal of user transfer from " + accountEntry1.username + " to " + accountEntry2.username + " (Reversed By " + MISUsername + ")";
         } else {
            reverseEntryDescription = "Reversal of user transfer from " + accountEntry2.username + " to " + accountEntry1.username + " (Reversed By " + MISUsername + ")";
         }

         this.refundAccountEntry(accountEntry1.id, String.valueOf(accountEntry1.id), reverseEntryDescription, accountEntrySourceData);
         this.refundAccountEntry(accountEntry2.id, String.valueOf(accountEntry2.id), reverseEntryDescription, accountEntrySourceData);
         this.refundCreditTransferFee(accountEntry1.id, accountEntrySourceData);
      }
   }

   public void creditAndNotifyUser(String username, double amountSent, double amountCredit, String cashReceiptID, AccountEntrySourceData accountEntrySourceData, boolean sendNotification) throws Exception {
      UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
      UserData user = userBean.loadUser(username, false, false);
      if (user == null) {
         throw new Exception("Unknown user [" + username + "]");
      } else {
         AccountEntryData accountEntry = this.creditUserFromCashReceipt(username, amountSent, amountCredit, cashReceiptID, accountEntrySourceData);
         if (sendNotification) {
            if (!user.mobileVerified && !user.emailVerified) {
               log.warn("User [" + username + "] has been credited [" + CurrencyData.baseCurrency + " " + amountCredit + "] but is neither email verified nor mobile verified.");
            } else {
               if (user.emailVerified) {
                  this.sendCreditNotificationViaEmail(accountEntry, user.emailAddress);
               }

               if (user.mobileVerified) {
                  this.sendCreditNotificationViaSMS(accountEntry, user.mobilePhone, accountEntrySourceData);
               }
            }
         }

      }
   }

   private AccountEntryData creditUserFromCashReceipt(String username, double amountSent, double amountCredit, String cashReceiptID, AccountEntrySourceData accountEntrySourceData) throws Exception {
      if (amountCredit <= 0.0D) {
         throw new EJBException("Unable to credit user: Credit amount must be more than zero.");
      } else {
         log.info("Crediting [" + CurrencyData.baseCurrency + " " + amountCredit + "] to user [" + username + "]");

         try {
            DecimalFormat df = new DecimalFormat("0");
            String accEntryDescription = "";

            try {
               accEntryDescription = "TT of " + df.format(amountSent) + CurrencyData.baseCurrency + " to purchase " + df.format(amountCredit) + CurrencyData.baseCurrency + " credit";
            } catch (NumberFormatException var13) {
            }

            AccountEntryData creditEntry = new AccountEntryData();
            creditEntry.username = username;
            creditEntry.type = AccountEntryData.TypeEnum.TELEGRAPHIC_TRANSFER;
            creditEntry.reference = cashReceiptID;
            creditEntry.description = accEntryDescription;
            creditEntry.currency = CurrencyData.baseCurrency;
            creditEntry.amount = amountCredit;
            creditEntry.fundedAmount = amountCredit;
            creditEntry.tax = 0.0D;
            creditEntry = this.createAccountEntry((Connection)null, creditEntry, accountEntrySourceData);

            try {
               if (amountSent > 0.0D) {
                  this.sendRechargeCreditRewardProgramTrigger(creditEntry.username, creditEntry.fundedAmount, creditEntry.currency, 0);
               }
            } catch (Exception var12) {
               log.warn("Unable to send recharge credit reward program trigger for user: " + creditEntry.username);
            }

            return creditEntry;
         } catch (Exception var14) {
            log.error("Unable to credit [" + username + "] with [" + CurrencyData.baseCurrency + " " + amountCredit + "] amount of credits: " + var14.getMessage());
            throw new Exception("Unable to give credits.");
         }
      }
   }

   private void sendCreditNotificationViaSMS(AccountEntryData accountEntry, String mobilePhone, AccountEntrySourceData accountEntrySourceData) {
      if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.TT_NOTIFICATION, accountEntry.username)) {
         try {
            DecimalFormat df = new DecimalFormat("0.00");
            MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.username = accountEntry.username;
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.TT_NOTIFICATION;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = mobilePhone;
            systemSMSData.messageText = MessageBundle.getMessage("resource.Credit_Notification_Messages", "SMS_CONTENT", accountEntry.currency, df.format(accountEntry.amount));
            log.info("Sending credit notification SMS to " + mobilePhone + ": " + systemSMSData.messageText);
            messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
         } catch (Exception var7) {
            log.error("Unable to send credit notification via SMS to [" + accountEntry.username + "] [" + accountEntry.currency + " " + accountEntry.amount + "] ::" + var7.getMessage());
         }

      }
   }

   private void sendCreditNotificationViaEmail(AccountEntryData accountEntry, String emailAddress) {
      try {
         DecimalFormat df = new DecimalFormat("0.00");
         MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
         String subject = MessageBundle.getMessage("resource.Credit_Notification_Messages", "EMAIL_SUBJECT");
         String content = MessageBundle.getMessage("resource.Credit_Notification_Messages", "EMAIL_CONTENT", accountEntry.username, accountEntry.currency, df.format(accountEntry.amount));
         log.info("Sending credit notification email to [" + emailAddress + "] Subject: " + subject);
         log.info("Sending credit notification email to [" + emailAddress + "] Content: " + content);
         messageBean.sendEmailFromNoReply(emailAddress, subject, content);
      } catch (Exception var7) {
         log.error("Unable to send credit notification via Email to [" + accountEntry.username + "] [" + accountEntry.currency + " " + accountEntry.amount + "] ::" + var7.getMessage());
      }

   }

   public void reverseTTCredit(long accountEntryID, String MISUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select reference from accountentry where id = ?");
         ps.setLong(1, Long.valueOf(accountEntryID));
         rs = ps.executeQuery();
         rs.next();
         int cashReceiptID = rs.getInt(1);
         ps = conn.prepareStatement("update cashreceipt set status = 3 where id = ?");
         ps.setInt(1, cashReceiptID);
         ps.executeUpdate();
         this.refundAccountEntry(accountEntryID, String.valueOf(accountEntryID), "Reversal of incorrect TT credit (Reversed By " + MISUsername + ")", accountEntrySourceData);
      } catch (SQLException var23) {
         throw new EJBException("Failed to Reverse TT. Database Error: " + var23.getMessage());
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

   }

   public AccountEntryData chargeUserForChatRoomKick(String username, String reference, String description, double amount, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.CHATROOM_KICK_CHARGE;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = CurrencyData.baseCurrency;
      accountEntry.amount = -amount;
      accountEntry.tax = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData chargeUserForSMS(String username, String reference, String description, double amount, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.SMS_CHARGE;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = CurrencyData.baseCurrency;
      accountEntry.amount = -amount;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData refundUserForSMS(int messageDestinationID, String reference, String description, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      Long accountEntryID;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select id from accountentry where type = ? and reference = ?");
         ps.setInt(1, AccountEntryData.TypeEnum.SMS_CHARGE.value());
         ps.setString(2, String.valueOf(messageDestinationID));
         rs = ps.executeQuery();
         if (rs.next()) {
            accountEntryID = rs.getLong("id");
            AccountEntryData var9 = this.refundAccountEntry(accountEntryID, reference, description, accountEntrySourceData);
            return var9;
         }

         accountEntryID = null;
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

      return accountEntryID;
   }

   public AccountEntryData chargeUserForSystemSMS(String username, String reference, String description, double amount, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.SYSTEM_SMS_CHARGE;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = CurrencyData.baseCurrency;
      accountEntry.amount = -amount;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public void updateWholesaleSMSCost(String reference, double wholesaleCost) throws EJBException {
      this.updateWholesaleCost(AccountEntryData.TypeEnum.SMS_CHARGE, reference, wholesaleCost);
   }

   public void updateWholesaleSystemSMSCost(String reference, double wholesaleCost) throws EJBException {
      this.updateWholesaleCost(AccountEntryData.TypeEnum.SYSTEM_SMS_CHARGE, reference, wholesaleCost);
   }

   private AccountEntryData updateWholesaleCost(AccountEntryData.TypeEnum type, String reference, double wholesaleCost) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      AccountEntryData var8;
      try {
         AccountEntryData accountEntryData = this.getAccountEntryFromReference(type, reference);
         if (accountEntryData != null) {
            accountEntryData.wholesaleCost = wholesaleCost * accountEntryData.exchangeRate;
            this.splitWholesaleCost(accountEntryData, (AccountBalanceData)null);
            conn = this.dataSourceMaster.getConnection();
            ps = conn.prepareStatement("update accountentry set costofgoodssold = ?, costoftrial = ?  where id = ?");
            ps.setDouble(1, accountEntryData.costOfGoodsSold);
            ps.setDouble(2, accountEntryData.costOfTrial);
            ps.setLong(3, accountEntryData.id);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Invalid System SMS ID " + reference);
            }
         }

         var8 = accountEntryData;
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return var8;
   }

   public AccountEntryData chargeUserForCall(String username, String reference, String description, double amount, double wholesaleCost, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.CALL_CHARGE;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = CurrencyData.baseCurrency;
      accountEntry.amount = -amount;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = wholesaleCost;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public void refundUserForCall(long accountEntryID, String MISUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      this.refundAccountEntry(accountEntryID, String.valueOf(accountEntryID), "Refund for incorrect call charge (Refunded By " + MISUsername + ")", accountEntrySourceData);
   }

   public void reverseExpiredCredit(long accountEntryID, String MISUsername, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      this.refundAccountEntry(accountEntryID, String.valueOf(accountEntryID), "Refund of Expired Credit (Refunded By " + MISUsername + ")", accountEntrySourceData);
   }

   public MoneyTransferData moneyTransferTopup(MoneyTransferData moneyTransferData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         if (moneyTransferData.username != null && moneyTransferData.username.length() != 0) {
            if (moneyTransferData.receiptNumber != null && moneyTransferData.receiptNumber.length() >= 3) {
               if (moneyTransferData.amount != null && !(moneyTransferData.amount <= 0.0D)) {
                  if (moneyTransferData.type == null) {
                     throw new EJBException("Type must be specified");
                  } else {
                     conn = this.dataSourceMaster.getConnection();
                     ps = conn.prepareStatement("insert into moneytransfer (username, datecreated, receiptnumber, fullname, amount, type) values (?,?,?,?,?,?)", 1);
                     ps.setString(1, moneyTransferData.username);
                     ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                     ps.setString(3, moneyTransferData.receiptNumber);
                     ps.setString(4, moneyTransferData.fullName);
                     ps.setDouble(5, moneyTransferData.amount);
                     ps.setInt(6, moneyTransferData.type.value());
                     if (ps.executeUpdate() != 1) {
                        throw new EJBException("Failed to save money transfer record");
                     } else {
                        rs = ps.getGeneratedKeys();
                        if (rs.next()) {
                           moneyTransferData.id = rs.getInt(1);
                           MoneyTransferData var5 = moneyTransferData;
                           return var5;
                        } else {
                           throw new EJBException("Failed to save money transfer record");
                        }
                     }
                  }
               } else {
                  throw new EJBException("Amount must be specified");
               }
            } else {
               throw new EJBException("Invalid receipt number");
            }
         } else {
            throw new EJBException("Username was not specified");
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
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
   }

   public CreditCardPaymentData creditCardPayment(CreditCardPaymentData paymentData, AccountEntrySourceData accountEntrySourceData) throws FusionEJBException, EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      CreditCardPaymentData protectedPayment = null;

      try {
         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments_CREDITCARD.NON_HML_CC_PAYMENT_ENABLED)) {
            throw new FusionEJBException("Please use our web client to recharge via credit card payments.");
         } else {
            Locale errorMessageLocale = Locale.ENGLISH;
            boolean isCreditCardPaymentAutoApproveEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_PAYMENT_ENABLED);
            Crypter crypter = new Crypter(CreditCardUtils.CRYPTER_KEY_LOCATION);
            PublicKey publicKey = AsymmetricCryptUtils.loadPublicKey(CreditCardUtils.PUBLIC_KEY_LOCATION);
            paymentData.dateCreated = new Date();
            paymentData.status = CreditCardPaymentData.StatusEnum.AWAITING_APPROVAL;
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_FIRST_NAME_LAST_NAME_CHECK_ENABLED) && (StringUtil.isBlank(paymentData.firstName) || StringUtil.isBlank(paymentData.lastName))) {
               throw new FusionEJBException("Please provide the first name and last name.");
            } else if (paymentData.source == null || paymentData.ipAddress == null) {
               throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.UNKNOWN_SOURCE.message(errorMessageLocale));
            } else if (paymentData.cardType == null) {
               throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CARD_TYPE.message(errorMessageLocale));
            } else if (paymentData.amount != null && !(paymentData.amount < 0.01D)) {
               if (!this.isValidCreditCardExpiryDate(paymentData.cardExpiryDate)) {
                  throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CARD_EXPIRY.message(errorMessageLocale, paymentData.cardExpiryDate));
               } else if (!this.isValidCreditCardNumber(paymentData.cardNumber)) {
                  throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CREDIT_CARD_NUMBER.message(errorMessageLocale));
               } else if (paymentData.cardHolder != null && paymentData.cardHolder.length() >= 3) {
                  if (paymentData.cardType == CreditCardPaymentData.CardTypeEnum.AMEX) {
                     if (paymentData.cardVerificationNumber == null || paymentData.cardVerificationNumber.length() != 4) {
                        throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_AMEX_CVV.message(errorMessageLocale));
                     }
                  } else if (paymentData.cardVerificationNumber == null || paymentData.cardVerificationNumber.length() != 3) {
                     throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CVV.message(errorMessageLocale));
                  }

                  conn = this.dataSourceMaster.getConnection();
                  ps = conn.prepareStatement("select (DATE_SUB(NOW(), INTERVAL ? HOUR) < u.dateregistered) isNewUser, u.mobilephone, u.mobileverified, uea.verified emailverified, u.type, c.* from country c, user u, userid LEFT OUTER JOIN useremailaddress uea ON userid.id = uea.userid and uea.type = ? where u.username=userid.username and u.countryid = c.id and u.username = ?");
                  ps.setInt(1, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.NEW_USER_THRESHOLD_HOURS));
                  ps.setInt(2, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
                  ps.setString(3, paymentData.username);
                  rs = ps.executeQuery();
                  if (!rs.next()) {
                     throw new FusionEJBException("Unable to find user data");
                  } else {
                     boolean isNewUser = rs.getBoolean("isNewUser");
                     boolean userMobileVerified = rs.getBoolean("mobileverified");
                     boolean userEmailVerified = rs.getBoolean("emailverified");
                     if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.MAKE_CREDIT_CARD_PAYMENT, new AuthenticatedAccessControlParameter(paymentData.username, userMobileVerified, userEmailVerified))) {
                        throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.UNAUTHENTICATED_USER.message(errorMessageLocale));
                     } else {
                        String mobilePhone = rs.getString("mobilephone");
                        boolean isMerchant = rs.getInt("type") == UserData.TypeEnum.MIG33_MERCHANT.value() || rs.getInt("type") == UserData.TypeEnum.MIG33_TOP_MERCHANT.value();
                        MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                        CurrencyData paymentCurrencyData = misBean.getCurrency(paymentData.currency);
                        if (paymentCurrencyData == null) {
                           throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CURRENCY.message(errorMessageLocale, paymentData.currency));
                        } else {
                           String creditCardPaymentCurrency = SystemProperty.get("CreditCardPaymentCurrency");
                           CurrencyData dbSettingCurrencyData = misBean.getCurrency(creditCardPaymentCurrency);
                           if (dbSettingCurrencyData == null) {
                              throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CURRENCY.message(errorMessageLocale, creditCardPaymentCurrency));
                           } else {
                              paymentData.exchangeRate = paymentCurrencyData.exchangeRate;
                              CountryData countryData = new CountryData(rs);
                              if (countryData.allowCreditCard == CountryData.AllowCreditCardEnum.NOT_ALLOW) {
                                 throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.CC_PAYMENT_UNAVAILABLE.message(errorMessageLocale));
                              } else if (countryData.allowCreditCard == CountryData.AllowCreditCardEnum.ALLOW_IF_BIN_CHECK && paymentData.cardType == CreditCardPaymentData.CardTypeEnum.AMEX) {
                                 throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.AMEX_UNSUPPORTED.message(errorMessageLocale));
                              } else {
                                 double amountInBaseCurrency = paymentCurrencyData.convertToBaseCurrency(paymentData.amount);
                                 boolean whitelistedUsernameAndCardNumber = false;
                                 boolean isOnCreditCardWhiteList = this.isOnCreditCardWhiteList(conn, paymentData.username, (String)null);
                                 log.info("User " + paymentData.username + " is in credit card whitelist: " + isOnCreditCardWhiteList);
                                 double maxAmountInDbCurrency;
                                 if (countryData.allowCreditCard == CountryData.AllowCreditCardEnum.ALLOW_IF_BIN_CHECK) {
                                    maxAmountInDbCurrency = SystemProperty.getDouble("MaxCreditCardPaymentAmount");
                                 } else {
                                    double[] amounts;
                                    if (isMerchant && isOnCreditCardWhiteList) {
                                       amounts = SystemProperty.getDoubleArray("CreditCardTrustedPaymentAmounts");
                                       maxAmountInDbCurrency = amounts[amounts.length - 1];
                                       whitelistedUsernameAndCardNumber = true;
                                    } else {
                                       amounts = SystemProperty.getDoubleArray("CreditCardPaymentAmounts");
                                       maxAmountInDbCurrency = amounts[amounts.length - 1];
                                    }
                                 }

                                 double maxAmountInBaseCurrency = dbSettingCurrencyData.convertToBaseCurrency(maxAmountInDbCurrency);
                                 if (isOnCreditCardWhiteList) {
                                    if (isCreditCardPaymentAutoApproveEnabled && !whitelistedUsernameAndCardNumber) {
                                       paymentData.allowAutoApprove = false;
                                       log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] is using different cc");
                                    }

                                    if (amountInBaseCurrency > maxAmountInBaseCurrency && isMerchant) {
                                       throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.DIFFERENT_CC_USED.message(errorMessageLocale));
                                    }
                                 }

                                 protectedPayment = CreditCardPaymentData.protectedPayment(paymentData, publicKey, crypter);
                                 protectedPayment = this.validatePastCreditCardTransactions(conn, protectedPayment, countryData, amountInBaseCurrency, maxAmountInBaseCurrency, maxAmountInDbCurrency, isNewUser);
                                 paymentData.status = protectedPayment.status;
                                 paymentData.allowAutoApprove = protectedPayment.allowAutoApprove && paymentData.allowAutoApprove;
                                 String gcMerchantUserID = isMerchant ? SystemProperty.get("CreditCardMerchantIDForMerchant") : SystemProperty.get("CreditCardMerchantIDForUser");
                                 String sql;
                                 if (isCreditCardPaymentAutoApproveEnabled && paymentData.allowAutoApprove) {
                                    if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_CC_COUNTRY_CHECK)) {
                                       sql = this.getCreditCardCountryIso(paymentData.cardNumber, gcMerchantUserID);
                                       if (paymentData.allowAutoApprove && !countryData.isoCountryCode.equals(sql)) {
                                          paymentData.allowAutoApprove = false;
                                          protectedPayment.allowAutoApprove = false;
                                          log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] country(" + countryData.isoCountryCode + ") != cc country (" + sql + ")");
                                       }
                                    }

                                    if (paymentData.allowAutoApprove) {
                                       double maxAmountAllowedforAutoApproveUSD = SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_MAXIMUM_AUTO_APPROVE_AMOUNT_USD);
                                       if (maxAmountAllowedforAutoApproveUSD > 0.0D) {
                                          double amountInUSD = paymentData.amount;
                                          if (!paymentData.currency.toUpperCase().equals("USD")) {
                                             amountInUSD = this.convertCurrency(paymentData.amount, paymentData.currency, "USD");
                                          }

                                          if (amountInUSD > maxAmountAllowedforAutoApproveUSD) {
                                             paymentData.allowAutoApprove = false;
                                             protectedPayment.allowAutoApprove = false;
                                             log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] (USD " + amountInUSD + ") amount more than auto-approve limit which is " + maxAmountAllowedforAutoApproveUSD);
                                          }
                                       }
                                    }

                                    int[] autoApprovalRiskyCountries = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_RISKY_COUNTRIES);
                                    if (paymentData.allowAutoApprove) {
                                       for(int i = 0; i < autoApprovalRiskyCountries.length; ++i) {
                                          if (countryData.id == autoApprovalRiskyCountries[i]) {
                                             paymentData.allowAutoApprove = false;
                                             protectedPayment.allowAutoApprove = false;
                                             log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] user country(" + countryData.isoCountryCode + ") is listed under risky countries");
                                             break;
                                          }
                                       }
                                    }
                                 }

                                 if (log.isDebugEnabled()) {
                                    log.debug("checkPastCreditCardTransactions results, status [" + protectedPayment.status.toString() + "] responseCode [" + protectedPayment.responseCode + "]");
                                 }

                                 sql = "";
                                 if (isCreditCardPaymentAutoApproveEnabled) {
                                    sql = "INSERT INTO creditcardpayment (username, datecreated, source, ipaddress, cardtype, cardnumber, encryptedcardnumber, checknumber, cardholder, cardexpirydate, cardverificationnumber, amount, currency, exchangerate, status, details) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                 } else {
                                    sql = "INSERT INTO creditcardpayment (username, datecreated, source, ipaddress, cardtype, cardnumber, encryptedcardnumber, checknumber, cardholder, cardexpirydate, cardverificationnumber, amount, currency, exchangerate, status, details) SELECT ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? FROM user WHERE username = ? and username not in (select username from creditcardpayment where username = ? and status = ?)";
                                 }

                                 ps = conn.prepareStatement(sql, 1);
                                 ps.setString(1, protectedPayment.username);
                                 ps.setTimestamp(2, new Timestamp(protectedPayment.dateCreated.getTime()));
                                 ps.setInt(3, protectedPayment.source.value());
                                 ps.setString(4, protectedPayment.ipAddress);
                                 ps.setInt(5, protectedPayment.cardType.value());
                                 char[] ccnArray = protectedPayment.cardNumber.toCharArray();
                                 CreditCardUtils.maskCreditCardNumber(ccnArray);
                                 ps.setString(6, protectedPayment.cardNumber);
                                 ps.setString(7, protectedPayment.encryptedCardNumber);
                                 ps.setString(8, protectedPayment.checkNumber);
                                 ps.setString(9, protectedPayment.cardHolder);
                                 ps.setString(10, protectedPayment.cardExpiryDate);
                                 ps.setString(11, (String)null);
                                 ps.setDouble(12, protectedPayment.amount);
                                 ps.setString(13, protectedPayment.currency);
                                 ps.setDouble(14, protectedPayment.exchangeRate);
                                 ps.setInt(15, protectedPayment.status.value());
                                 ps.setBytes(16, protectedPayment.serializeExtraFieldsToJSON().toString().getBytes());
                                 if (!isCreditCardPaymentAutoApproveEnabled) {
                                    ps.setString(17, protectedPayment.username);
                                    ps.setString(18, protectedPayment.username);
                                    ps.setInt(19, CreditCardPaymentData.StatusEnum.AWAITING_APPROVAL.value());
                                 }

                                 ps.executeUpdate();
                                 rs = ps.getGeneratedKeys();
                                 if (!rs.next()) {
                                    if (isCreditCardPaymentAutoApproveEnabled) {
                                       throw new EJBException(CreditCardPaymentData.ErrorEnum.DEFAULT.message(errorMessageLocale));
                                    } else {
                                       throw new EJBException(CreditCardPaymentData.ErrorEnum.HAS_PENDING_CC_PAYMENT.message(errorMessageLocale));
                                    }
                                 } else {
                                    protectedPayment.id = paymentData.id = rs.getInt(1);
                                    if (paymentData.status != CreditCardPaymentData.StatusEnum.DECLINED) {
                                       paymentData = this.makeCreditCardTransactionGlobalCollect(paymentData, countryData, mobilePhone, gcMerchantUserID);
                                       protectedPayment.providerTransactionId = paymentData.providerTransactionId;
                                       protectedPayment.responseCode = paymentData.responseCode;
                                       protectedPayment.status = paymentData.status;
                                       if (log.isDebugEnabled()) {
                                          log.debug("ACH results, status [" + protectedPayment.status.toString() + "], responseCode [" + protectedPayment.responseCode + "], providerTransactionId [" + protectedPayment.providerTransactionId + "]");
                                       }
                                    }

                                    paymentData.scrubSensitiveInfo();

                                    try {
                                       InitialContext ctx = new InitialContext();
                                       AccountLocalHome localHome = (AccountLocalHome)ctx.lookup("AccountLocal");
                                       AccountLocal localEJB = localHome.create();
                                       protectedPayment = localEJB.updateCreditCardPaymentStatus(protectedPayment, accountEntrySourceData);
                                       if (log.isDebugEnabled()) {
                                          log.debug("updateCreditCardPaymentStatus results, status [" + protectedPayment.status.toString() + "], discountAmount [" + protectedPayment.discountAmount + "], percentageDiscount [" + protectedPayment.percentageDiscount + "]");
                                       }
                                    } catch (Exception var56) {
                                       log.error("Unable to update credit card payment status. ID = " + protectedPayment.id, var56);
                                    }

                                    if (protectedPayment.status == CreditCardPaymentData.StatusEnum.DECLINED) {
                                       if (protectedPayment.error != null) {
                                          protectedPayment.responseCode = protectedPayment.error.message(errorMessageLocale);
                                       } else {
                                          protectedPayment.responseCode = CreditCardPaymentData.ErrorEnum.DECLINED.message(errorMessageLocale);
                                       }

                                       return protectedPayment;
                                    } else {
                                       if (isCreditCardPaymentAutoApproveEnabled && protectedPayment.allowAutoApprove) {
                                          protectedPayment = this.approveCreditCardPayment(protectedPayment.id, accountEntrySourceData);
                                       }

                                       try {
                                          EmailUserNotification note = new EmailUserNotification();
                                          String amountString = (new DecimalFormat("0.00 ")).format(paymentData.amount) + " " + paymentData.currency;
                                          note.emailAddress = SystemProperty.get("CreditCardPaymentNotificationEmail");
                                          note.subject = "[CREDIT CARD PAYMENT] User: " + paymentData.username + " Amount: " + amountString;
                                          if (protectedPayment.status == CreditCardPaymentData.StatusEnum.APPROVED) {
                                             note.message = "A credit card payment from the user " + paymentData.username + "  is authorised and credited.\n\n";
                                          } else {
                                             note.message = "A credit card payment from the user " + paymentData.username + " is now awaiting authorisation.\n\n";
                                          }

                                          note.message = note.message + "Amount: " + amountString + "\n";
                                          note.message = note.message + "migme ID: " + paymentData.id + "\n";
                                          note.message = note.message + "Provider ID: " + paymentData.providerTransactionId + "\n";
                                          log.info("Sending email to: " + note.emailAddress);
                                          log.info("Subject: " + note.message);
                                          log.info("Body: " + note.message);
                                          EJBIcePrxFinder.getUserNotificationServiceProxy().notifyUserViaEmail(note);
                                       } catch (Exception var55) {
                                       }

                                       return protectedPayment;
                                    }
                                 }
                              }
                           }
                        }
                     }
                  }
               } else {
                  throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_CARD_HOLDER.message(errorMessageLocale));
               }
            } else {
               throw new FusionEJBException(CreditCardPaymentData.ErrorEnum.INVALID_AMOUNT.message(errorMessageLocale));
            }
         }
      } catch (CreateException var57) {
         log.error("Unable to process credit card payment", var57);
         throw new EJBException(var57.getMessage());
      } catch (SQLException var58) {
         log.error("Unable to process credit card payment", var58);
         throw new EJBException(var58.getMessage());
      } catch (KeyczarException var59) {
         log.error("Failed to encrypt ", var59);
         throw new EJBException(var59.getMessage());
      } catch (FusionEJBException var60) {
         log.error("Failed to make credit card payment: ", var60);
         throw var60;
      } catch (Exception var61) {
         log.error("Failed to process credit card ", var61);
         throw new EJBException(var61.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var54) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var53) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var52) {
            conn = null;
         }

      }
   }

   public CreditCardPaymentUserAndCountryInfo getUserAndCountryInfoForCreditCardPayment(Connection masterConn, int userID) throws FusionEJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CreditCardPaymentUserAndCountryInfo var6;
      try {
         ch = new ConnectionHolder(this.dataSourceMaster, masterConn);
         ps = ch.getConnection().prepareStatement("SELECT   ui.id userid       , u.username        , (DATE_SUB(NOW(), INTERVAL ? HOUR) < u.dateregistered) isnewuser       , u.mobilephone       , u.mobileverified       , uea.verified emailverified       , u.type       , c.* FROM   country c      , user u      , userid ui LEFT OUTER JOIN useremailaddress uea ON ui.id = uea.userid AND uea.type = ? WHERE u.username = ui.username AND u.countryid = c.id AND ui.id = ?");
         ps.setInt(1, SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.NEW_USER_THRESHOLD_HOURS));
         ps.setInt(2, UserEmailAddressData.UserEmailAddressTypeEnum.PRIMARY.value);
         ps.setInt(3, userID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new FusionEJBException("Unable to find user data");
         }

         var6 = new CreditCardPaymentUserAndCountryInfo(rs);
      } catch (SQLException var21) {
         log.warn("Error in retrieving user and country info for cc payment:: " + var21.getMessage());
         throw new FusionEJBException("An error occurred in your request. Please try again later.", var21);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var18) {
            ch = null;
         }

      }

      return var6;
   }

   public boolean isOnCreditCardWhiteList(Connection masterConn, String username, String checkNumber) throws SQLException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var7;
      try {
         ch = new ConnectionHolder(this.dataSourceMaster, masterConn);
         if (username == null) {
            ps = ch.getConnection().prepareStatement("select * from creditcardwhitelist where checkNumber = ?");
            ps.setString(1, checkNumber);
         } else if (checkNumber == null) {
            ps = ch.getConnection().prepareStatement("select * from creditcardwhitelist where username = ?");
            ps.setString(1, username);
         } else {
            ps = ch.getConnection().prepareStatement("select * from creditcardwhitelist where username = ? and checkNumber = ?");
            ps.setString(1, username);
            ps.setString(2, checkNumber);
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            var7 = true;
            return var7;
         }

         rs.close();
         ps.close();
         if (username == null) {
            ps = ch.getConnection().prepareStatement("select sum(amount/exchangerate) from creditcardpayment where checkNumber = ? and datecreated >= '2008-01-01' and datecreated < date_sub(now(), interval 3 month) and status = ?");
            ps.setString(1, checkNumber);
            ps.setInt(2, CreditCardPaymentData.StatusEnum.APPROVED.value());
         } else if (checkNumber == null) {
            ps = ch.getConnection().prepareStatement("select sum(amount/exchangerate) from creditcardpayment where username = ? and datecreated >= '2008-01-01' and datecreated < date_sub(now(), interval 3 month) and status = ?");
            ps.setString(1, username);
            ps.setInt(2, CreditCardPaymentData.StatusEnum.APPROVED.value());
         } else {
            label321: {
               ps = ch.getConnection().prepareStatement("select username from creditcardpayment where checkNumber = ? and datecreated >= '2008-01-01' and status = ? order by datecreated limit 1");
               ps.setString(1, checkNumber);
               ps.setInt(2, CreditCardPaymentData.StatusEnum.APPROVED.value());
               rs = ps.executeQuery();
               if (rs.next() && username.equals(rs.getString("username"))) {
                  rs.close();
                  ps.close();
                  ps = ch.getConnection().prepareStatement("select checkNumber from creditcardpayment where username = ? and datecreated >= '2008-01-01' and datecreated < date_sub(now(), interval 3 month) and status = ? order by datecreated desc limit 1");
                  ps.setString(1, username);
                  ps.setInt(2, CreditCardPaymentData.StatusEnum.APPROVED.value());
                  rs = ps.executeQuery();
                  if (rs.next() && checkNumber.equals(rs.getString("checkNumber"))) {
                     rs.close();
                     ps.close();
                     ps = ch.getConnection().prepareStatement("select sum(amount/exchangerate) from creditcardpayment where username = ? and checkNumber = ? and datecreated >= '2008-01-01' and datecreated < date_sub(now(), interval 3 month) and status = ?");
                     ps.setString(1, username);
                     ps.setString(2, checkNumber);
                     ps.setInt(3, CreditCardPaymentData.StatusEnum.APPROVED.value());
                     break label321;
                  }

                  var7 = false;
                  return var7;
               }

               var7 = false;
               return var7;
            }
         }

         rs = ps.executeQuery();
         var7 = rs.next() && rs.getInt(1) >= 50;
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
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var27) {
            ch = null;
         }

      }

      return var7;
   }

   private CreditCardPaymentData validatePastCreditCardTransactions(Connection conn, CreditCardPaymentData paymentData, CountryData countryData, double amountInBaseCurrency, double maxAmountInBaseCurrency, double maxAmountInDbCurrency, boolean isNewUser) throws EJBException, SQLException, KeyczarException, NoSuchFieldException, Exception {
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         Locale errorMessageLocale = Locale.ENGLISH;
         boolean autoApproveCreditCardEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_PAYMENT_ENABLED);
         ps = conn.prepareStatement("select username, checknumber from creditcardpayment where (username = ? or checknumber = ?) and status = ? limit 1");
         ps.setString(1, paymentData.username);
         ps.setString(2, paymentData.checkNumber);
         ps.setInt(3, CreditCardPaymentData.StatusEnum.CHARGE_BACK.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            if (paymentData.username.equalsIgnoreCase(rs.getString("username"))) {
               paymentData.decline("Chargeback on user");
               if (log.isDebugEnabled()) {
                  log.debug("chargeback found on the user [" + paymentData.username + "]");
               }
            } else {
               paymentData.decline("Chargeback on card");
               if (log.isDebugEnabled()) {
                  log.debug("chargeback found on the card");
               }
            }
         }

         if (paymentData.status != CreditCardPaymentData.StatusEnum.DECLINED) {
            int maxCreditCardTransactionsPerDay = SystemProperty.getInt("MaxCreditCardTransactionsPerDay");
            int maxCreditCardTransactionsPerWeek = SystemProperty.getInt("MaxCreditCardTransactionsPerWeek");
            int maxCreditCardTransactionsPerMonth = SystemProperty.getInt("MaxCreditCardTransactionsPerMonth");
            int maxCreditCardUsersPer48Hours = SystemProperty.getInt("MaxCreditCardUsersPer48Hours");
            int maxCreditCardUsersPerWeek = SystemProperty.getInt("MaxCreditCardUsersPerWeek");
            int maxCreditCardUsersPerMonth = SystemProperty.getInt("MaxCreditCardUsersPerMonth");
            int maxCreditCardFailsPerCardDetailPerDay = SystemProperty.getInt("MaxCreditCardFailsPerCardDetailPerDay");
            int maxCreditCardFailsPerUserPerDay = SystemProperty.getInt("MaxCreditCardFailsPerUserPerDay");
            int maxCreditCardFailsPerCardPer48Hours = SystemProperty.getInt("MaxCreditCardFailsPerCardPer48Hours");
            ps = conn.prepareStatement("SELECT SUM( IF(status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR), (amount/exchangerate), 0) ) totalAmountApproved24Hrs, SUM( status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ) totalApproved24Hrs, SUM( IF(status = 2 AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR), (amount/exchangerate), 0) ) totalAmountDeclined24Hrs, SUM( status = 2 AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ) totalDeclined24Hrs, SUM( IF(status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 48 HOUR), (amount/exchangerate), 0) ) totalAmountApproved48Hrs, SUM( status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 7 DAY) ) totalApproved7Days, SUM( status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 30 DAY) ) totalApproved30Days, SUM( status = 0 ) totalPending FROM creditcardpayment WHERE username = ? AND datecreated > DATE_SUB(now(), INTERVAL 30 DAY)");
            ps.setString(1, paymentData.username);
            rs = ps.executeQuery();
            if (rs.next()) {
               if (rs.getInt("totalPending") > 0) {
                  if (!autoApproveCreditCardEnabled) {
                     throw new EJBException(CreditCardPaymentData.ErrorEnum.HAS_PENDING_CC_PAYMENT.message(errorMessageLocale));
                  }

                  paymentData.allowAutoApprove = false;
                  log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] has pending credit card payment.");
               }

               if (rs.getInt("totalApproved24Hrs") >= maxCreditCardTransactionsPerDay) {
                  paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_DAILY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED);
               } else if (rs.getInt("totalApproved7Days") >= maxCreditCardTransactionsPerWeek) {
                  paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_WEEKLY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED);
               } else if (rs.getInt("totalApproved30Days") >= maxCreditCardTransactionsPerMonth) {
                  paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_MONTHLY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED);
               } else {
                  if (rs.getInt("totalDeclined24Hrs") >= maxCreditCardFailsPerUserPerDay) {
                     String message = "";
                     if (autoApproveCreditCardEnabled) {
                        message = CreditCardPaymentData.ErrorEnum.USER_DAILY_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED.message(errorMessageLocale);
                     } else {
                        message = "We regret we are unable to complete your payment at this time. Your satisfaction is very important to us. Please email merchant@mig.me and we will assist you in completing your payment. Alternatively, you may like to try another payment option available to you such as ";
                        if (countryData.allowBankTransfer > 0 && countryData.allowWesternUnion > 0) {
                           message = message + "Local Bank Deposit, or Western Union.";
                        } else if (countryData.allowBankTransfer > 0) {
                           message = message + "Local Bank Deposit.";
                        } else if (countryData.allowWesternUnion > 0) {
                           message = message + "Western Union.";
                        } else {
                           message = message + "PayPal.";
                        }
                     }

                     throw new EJBException(message);
                  }

                  if (rs.getDouble("totalAmountApproved24Hrs") + amountInBaseCurrency > maxAmountInBaseCurrency) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_DAILY_RECHARGE_AMOUNT_EXCEEDED, String.valueOf(maxAmountInDbCurrency));
                  } else {
                     double amountInUSD = this.convertCurrency(amountInBaseCurrency + rs.getDouble("totalAmountApproved48Hrs"), CurrencyData.baseCurrency, "USD");
                     if (autoApproveCreditCardEnabled && isNewUser && amountInUSD >= SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.MAX_NEW_USER_CC_PURCHASE_USD)) {
                        paymentData.allowAutoApprove = false;
                        log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] has registered in the past 48 hours and exceeds recharge cc threshold");
                     }
                  }
               }
            }

            if (paymentData.status != CreditCardPaymentData.StatusEnum.DECLINED && StringUtil.isBlank(paymentData.checkNumber)) {
               ps = conn.prepareStatement("SELECT SUM( IF(status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR), (amount/exchangerate), 0) ) totalAmountApproved24Hrs, SUM( status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ) totalApproved24Hrs, SUM( status = 2 AND cardtype = ? AND cardexpirydate = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ) totalCardDetailDeclined24Hrs, COUNT( DISTINCT CASE WHEN status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 48 HOUR) AND username != ? THEN username ELSE NULL END ) totalApprovedUsers48Hrs, SUM( status = 2 AND datecreated >= DATE_SUB(NOW(), INTERVAL 48 HOUR) ) totalDeclined48Hrs, SUM( status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 7 DAY) ) totalApproved7Days, COUNT( DISTINCT CASE WHEN status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 7 DAY) AND username != ? THEN username ELSE NULL END ) totalApprovedUsers7Days, SUM( status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 30 DAY) ) totalApproved30Days, COUNT( DISTINCT CASE WHEN status = 1 AND datecreated >= DATE_SUB(NOW(), INTERVAL 30 DAY) AND username != ? THEN username ELSE NULL END ) totalApprovedUsers30Days, SUM( status = 0 ) totalPending FROM creditcardpayment WHERE checknumber = ? AND datecreated > DATE_SUB(now(), INTERVAL 30 DAY)");
               ps.setInt(1, paymentData.cardType.value());
               ps.setString(2, paymentData.cardExpiryDate);
               ps.setString(3, paymentData.username);
               ps.setString(4, paymentData.username);
               ps.setString(5, paymentData.username);
               ps.setString(6, paymentData.checkNumber);
               rs = ps.executeQuery();
               if (rs.next()) {
                  if (rs.getInt("totalPending") > 0) {
                     if (!autoApproveCreditCardEnabled) {
                        throw new EJBException(CreditCardPaymentData.ErrorEnum.HAS_PENDING_CC_PAYMENT.message(errorMessageLocale));
                     }

                     paymentData.allowAutoApprove = false;
                     log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] credit card has pending transaction.");
                  }

                  if (rs.getInt("totalApproved24Hrs") >= maxCreditCardTransactionsPerDay) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.CARD_DAILY_APPROVED_USAGE_EXCEEDED);
                  } else if (rs.getInt("totalApprovedUsers48Hrs") >= maxCreditCardUsersPer48Hours) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.CARD_48_HRS_APPROVED_USAGE_EXCEEDED, String.valueOf(maxCreditCardUsersPer48Hours));
                  } else if (rs.getInt("totalApproved7Days") >= maxCreditCardTransactionsPerWeek) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.CARD_WEEKLY_APPROVED_USAGE_THRESHOLD_EXCEEDED);
                  } else if (rs.getInt("totalApprovedUsers7Days") >= maxCreditCardUsersPerWeek) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.WEEKLY_NUMBER_OF_CARD_USER_THRESHOLD_EXCEEDED, String.valueOf(maxCreditCardUsersPerWeek));
                  } else if (rs.getInt("totalApproved30Days") >= maxCreditCardTransactionsPerMonth) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.CARD_MONTHLY_APPROVED_USAGE_THRESHOLD_EXCEEDED);
                  } else if (rs.getInt("totalApprovedUsers30Days") >= maxCreditCardUsersPerMonth) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.MONTHLY_NUMBER_OF_CARD_USER_THRESHOLD_EXCEEDED, String.valueOf(maxCreditCardUsersPerMonth));
                  } else if (rs.getInt("totalCardDetailDeclined24Hrs") >= maxCreditCardFailsPerCardDetailPerDay) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.CARD_DETAILS_DAILY_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED, String.valueOf(maxCreditCardFailsPerCardDetailPerDay));
                  } else if (rs.getInt("totalDeclined48Hrs") >= maxCreditCardFailsPerCardPer48Hours) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.CARD_48_HRS_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED, String.valueOf(maxCreditCardFailsPerCardPer48Hours));
                  } else if (rs.getDouble("totalAmountApproved24Hrs") + amountInBaseCurrency > maxAmountInBaseCurrency) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_DAILY_RECHARGE_AMOUNT_EXCEEDED, String.valueOf(maxAmountInDbCurrency));
                  }
               }
            }
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var35) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var34) {
            ps = null;
         }

      }

      return paymentData;
   }

   public CreditCardPaymentData validatePastCreditCardHMLTransactions(CreditCardPaymentData paymentData, CountryData countryData, double amountInBaseCurrency, double maxAmountInBaseCurrency, double maxAmountInDbCurrency, boolean isNewUser) throws EJBException, SQLException, NoSuchFieldException, Exception {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         Locale errorMessageLocale = Locale.ENGLISH;
         conn = this.dataSourceSlave.getConnection();
         boolean autoApproveCreditCardEnabled = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_APPROVE_CREDIT_CARD_PAYMENT_ENABLED);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.SE546_ENABLED)) {
            ps = conn.prepareStatement("SELECT ui.username FROM payments p, userid ui WHERE p.userid = ui.id AND ui.username = ?AND status = ? AND p.type = ? LIMIT 1");
            ps.setString(1, paymentData.username);
            ps.setInt(2, CreditCardPaymentData.StatusEnum.CHARGE_BACK.value());
            ps.setInt(3, PaymentData.TypeEnum.CREDIT_CARD.value());
         } else {
            ps = conn.prepareStatement("SELECT ui.username FROM payments p, userid ui WHERE p.userid = ui.id AND ui.username = ?AND status = ? LIMIT 1");
            ps.setString(1, paymentData.username);
            ps.setInt(2, CreditCardPaymentData.StatusEnum.CHARGE_BACK.value());
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            if (paymentData.username.equalsIgnoreCase(rs.getString("username"))) {
               paymentData.decline("Chargeback on user");
               if (log.isDebugEnabled()) {
                  log.debug("chargeback found on the user [" + paymentData.username + "]");
               }
            } else {
               paymentData.decline("Chargeback on card");
               if (log.isDebugEnabled()) {
                  log.debug("chargeback found on the card");
               }
            }
         }

         if (paymentData.status != CreditCardPaymentData.StatusEnum.DECLINED) {
            int maxCreditCardTransactionsPerDay = SystemProperty.getInt("MaxCreditCardTransactionsPerDay");
            int maxCreditCardTransactionsPerWeek = SystemProperty.getInt("MaxCreditCardTransactionsPerWeek");
            int maxCreditCardTransactionsPerMonth = SystemProperty.getInt("MaxCreditCardTransactionsPerMonth");
            int maxCreditCardFailsPerUserPerDay = SystemProperty.getInt("MaxCreditCardFailsPerUserPerDay");
            ps = conn.prepareStatement("SELECT IFNULL(SUM( IF(status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR), (amount/exchangeRateUSD), 0)), 0) totalAmountApproved24Hrs,        IFNULL(SUM( status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ), 0) totalApproved24Hrs,        IFNULL(SUM( IF(status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR), (amount/exchangeRateUSD), 0) ), 0) totalAmountDeclined24Hrs,        IFNULL(SUM( status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ), 0) totalDeclined24Hrs,        IFNULL(SUM( IF(status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 48 HOUR), (amount/exchangeRateUSD), 0) ), 0) totalAmountApproved48Hrs,        IFNULL(SUM( status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 7 DAY) ), 0) totalApproved7Days,        IFNULL(SUM( status = ? AND datecreated >= DATE_SUB(NOW(), INTERVAL 30 DAY) ), 0) totalApproved30Days,        IFNULL(SUM( status = ? ), 0) totalPending FROM payments p, userid ui WHERE p.userid = ui.id AND type = ? AND username = ? AND datecreated > DATE_SUB(now(), INTERVAL 30 DAY)");
            ps.setInt(1, PaymentData.StatusEnum.APPROVED.value());
            ps.setInt(2, PaymentData.StatusEnum.APPROVED.value());
            ps.setInt(3, PaymentData.StatusEnum.REJECTED.value());
            ps.setInt(4, PaymentData.StatusEnum.REJECTED.value());
            ps.setInt(5, PaymentData.StatusEnum.APPROVED.value());
            ps.setInt(6, PaymentData.StatusEnum.APPROVED.value());
            ps.setInt(7, PaymentData.StatusEnum.APPROVED.value());
            ps.setInt(8, PaymentData.StatusEnum.PENDING.value());
            ps.setInt(9, PaymentData.TypeEnum.CREDIT_CARD.value());
            ps.setString(10, paymentData.username);
            rs = ps.executeQuery();
            if (rs.next()) {
               if (rs.getInt("totalPending") > 0) {
                  if (!autoApproveCreditCardEnabled) {
                     throw new EJBException(CreditCardPaymentData.ErrorEnum.HAS_PENDING_CC_PAYMENT.message(errorMessageLocale));
                  }

                  paymentData.allowAutoApprove = false;
                  log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] has pending credit card payment.");
               }

               if (rs.getInt("totalApproved24Hrs") >= maxCreditCardTransactionsPerDay) {
                  paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_DAILY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED);
               } else if (rs.getInt("totalApproved7Days") >= maxCreditCardTransactionsPerWeek) {
                  paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_WEEKLY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED);
               } else if (rs.getInt("totalApproved30Days") >= maxCreditCardTransactionsPerMonth) {
                  paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_MONTHLY_APPROVED_TRANSACTIONS_LIMIT_EXCEEDED);
               } else {
                  if (rs.getInt("totalDeclined24Hrs") >= maxCreditCardFailsPerUserPerDay) {
                     String message = "";
                     if (autoApproveCreditCardEnabled) {
                        message = CreditCardPaymentData.ErrorEnum.USER_DAILY_DECLINED_TRANSACTIONS_LIMIT_EXCEEDED.message(errorMessageLocale);
                     } else {
                        message = "We regret we are unable to complete your payment at this time. Your satisfaction is very important to us. Please email merchant@mig.me and we will assist you in completing your payment. Alternatively, you may like to try another payment option available to you such as ";
                        if (countryData.allowBankTransfer > 0 && countryData.allowWesternUnion > 0) {
                           message = message + "Local Bank Deposit, or Western Union.";
                        } else if (countryData.allowBankTransfer > 0) {
                           message = message + "Local Bank Deposit.";
                        } else if (countryData.allowWesternUnion > 0) {
                           message = message + "Western Union.";
                        } else {
                           message = message + "PayPal.";
                        }
                     }

                     throw new EJBException(message);
                  }

                  if (rs.getDouble("totalAmountApproved24Hrs") + amountInBaseCurrency > maxAmountInBaseCurrency) {
                     paymentData.decline(CreditCardPaymentData.ErrorEnum.USER_DAILY_RECHARGE_AMOUNT_EXCEEDED, String.valueOf(maxAmountInDbCurrency));
                  } else {
                     double amountInUSD = this.convertCurrency(amountInBaseCurrency + rs.getDouble("totalAmountApproved48Hrs"), CurrencyData.baseCurrency, "USD");
                     if (autoApproveCreditCardEnabled && isNewUser && amountInUSD >= SystemProperty.getDouble((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.MAX_NEW_USER_CC_PURCHASE_USD)) {
                        paymentData.allowAutoApprove = false;
                        log.info("Ineligible for auto-approve: [" + paymentData.username + "] [" + paymentData.currency + " " + paymentData.amount + "] has registered in the past 48 hours and exceeds recharge cc threshold");
                     }
                  }
               }
            }
         }
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

      return paymentData;
   }

   private Double calculateRechargeCreditBonusInBaseCurrency(Double amount, String currency, UserData.TypeEnum userType, boolean isMentor) throws Exception {
      Double bonus = 0.0D;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_RECHARGE_BONUS_ENABLED)) {
         Double usdAmount = currency.equalsIgnoreCase("USD") ? amount : this.convertCurrency(amount, currency, "USD");
         if (userType == UserData.TypeEnum.MIG33_TOP_MERCHANT && usdAmount >= 75.0D) {
            if (usdAmount < 3500.0D) {
               if (usdAmount >= 500.0D && isMentor) {
                  bonus = amount * 0.42D;
               } else {
                  bonus = amount * 0.4D;
               }
            } else if (usdAmount < 20000.0D) {
               bonus = amount * 0.5D;
            } else {
               bonus = amount * 0.6D;
            }
         }

         if (bonus > 0.0D) {
            bonus = currency.equalsIgnoreCase(CurrencyData.baseCurrency) ? bonus : this.convertCurrency(bonus, currency, CurrencyData.baseCurrency);
         }
      }

      return bonus;
   }

   public CreditCardPaymentData getCreditCardPayment(int creditCardPaymentId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      CreditCardPaymentData var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from creditcardpayment where id = ?");
         ps.setInt(1, creditCardPaymentId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = new CreditCardPaymentData(rs);
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

      return var5;
   }

   private CreditCardPaymentData approveCreditCardPayment(int creditCardPaymentId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      return this.approveCreditCardPayment("", creditCardPaymentId, CreditCardPaymentData.ApproveTypeEnum.AUTO, accountEntrySourceData);
   }

   public CreditCardPaymentData approveCreditCardPayment(String staffUsername, int creditCardPaymentId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      return this.approveCreditCardPayment(staffUsername, creditCardPaymentId, CreditCardPaymentData.ApproveTypeEnum.MANUAL, accountEntrySourceData);
   }

   private CreditCardPaymentData approveCreditCardPayment(String staffUsername, int creditCardPaymentId, CreditCardPaymentData.ApproveTypeEnum approveType, AccountEntrySourceData accountEntrySourceData) {
      try {
         CreditCardPaymentData paymentData = this.getCreditCardPayment(creditCardPaymentId);
         if (paymentData == null) {
            throw new EJBException("Invalid credit card payment ID " + creditCardPaymentId);
         } else if (paymentData.status != CreditCardPaymentData.StatusEnum.AWAITING_APPROVAL) {
            throw new EJBException("Credit card payment already transacted");
         } else {
            paymentData.autoApprove = approveType;
            Integer paymentProductId = paymentData.getGlobalCollectPaymentProductId();
            if (paymentProductId == null) {
               throw new EJBException("Unable to determine payment product ID for card type " + paymentData.cardType);
            } else {
               String merchantId = paymentData.getGlobalCollectMerchantId();
               if (merchantId == null) {
                  throw new EJBException("Unable to determine Global Collect merchant ID");
               } else {
                  StringBuilder request = new StringBuilder();
                  request.append("<XML>");
                  request.append("<REQUEST>");
                  request.append("<ACTION>SET_PAYMENT</ACTION>");
                  request.append("<META>");
                  request.append("<MERCHANTID>").append(merchantId).append("</MERCHANTID>");
                  request.append("</META>");
                  request.append("<PARAMS>");
                  request.append("<PAYMENT>");
                  request.append("<ORDERID>").append(creditCardPaymentId).append("</ORDERID>");
                  request.append("<EFFORTID>").append(1).append("</EFFORTID>");
                  request.append("<PAYMENTPRODUCTID>").append(paymentProductId).append("</PAYMENTPRODUCTID>");
                  request.append("</PAYMENT>");
                  request.append("</PARAMS>");
                  request.append("</REQUEST>");
                  request.append("</XML>");
                  SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get("GlobalCollectURL"), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
                  if (reply.containsTag("ERROR")) {
                     throw new EJBException(reply.getTagValue("ERROR", "CODE") + "," + reply.getTagValue("ERROR", "MESSAGE"));
                  } else if (!"OK".equals(reply.getTagValue("RESULT").toUpperCase())) {
                     throw new EJBException("Unknown error");
                  } else {
                     paymentData.status = CreditCardPaymentData.StatusEnum.APPROVED;
                     log.info(staffUsername + " approved credit card transaction " + creditCardPaymentId);
                     AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                     return accountBean.updateCreditCardPaymentStatus(paymentData, accountEntrySourceData);
                  }
               }
            }
         }
      } catch (Exception var11) {
         log.warn("Error in approve credit card transaction", var11);
         throw new EJBException(var11.getMessage());
      }
   }

   public CreditCardPaymentData rejectCreditCardPayment(String staffUsername, int creditCardPaymentId, String reason, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      try {
         CreditCardPaymentData paymentData = this.getCreditCardPayment(creditCardPaymentId);
         if (paymentData == null) {
            throw new EJBException("Invalid credit card payment ID " + creditCardPaymentId);
         } else if (paymentData.status != CreditCardPaymentData.StatusEnum.AWAITING_APPROVAL) {
            throw new EJBException("Credit card payment already transacted");
         } else {
            String merchantId = paymentData.getGlobalCollectMerchantId();
            if (merchantId == null) {
               throw new EJBException("Unable to determine Global Collect merchant ID");
            } else {
               StringBuilder request = new StringBuilder();
               request.append("<XML>");
               request.append("<REQUEST>");
               request.append("<ACTION>CANCEL_PAYMENT</ACTION>");
               request.append("<META>");
               request.append("<MERCHANTID>").append(merchantId).append("</MERCHANTID>");
               request.append("</META>");
               request.append("<PARAMS>");
               request.append("<PAYMENT>");
               request.append("<ORDERID>").append(creditCardPaymentId).append("</ORDERID>");
               request.append("<EFFORTID>").append(1).append("</EFFORTID>");
               request.append("<ATTEMPTID>").append(1).append("</ATTEMPTID>");
               request.append("</PAYMENT>");
               request.append("</PARAMS>");
               request.append("</REQUEST>");
               request.append("</XML>");
               SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get("GlobalCollectURL"), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
               if (reply.containsTag("ERROR")) {
                  throw new EJBException(reply.getTagValue("ERROR", "CODE") + "," + reply.getTagValue("ERROR", "MESSAGE"));
               } else if (!"OK".equals(reply.getTagValue("RESULT").toUpperCase())) {
                  throw new EJBException("Unknown error");
               } else {
                  paymentData.status = CreditCardPaymentData.StatusEnum.DECLINED;
                  paymentData.responseCode = "Rejected by " + staffUsername + ": " + reason;
                  AccountLocal accountBean = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
                  return accountBean.updateCreditCardPaymentStatus(paymentData, accountEntrySourceData);
               }
            }
         }
      } catch (Exception var10) {
         log.warn("Error in reject credit card transaction", var10);
         throw new EJBException(var10.getMessage());
      }
   }

   public AccountEntryData creditCardRefund(int creditCardPaymentId, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      AccountEntryData var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from creditcardpayment where id = ? and status = ?");
         ps.setInt(1, creditCardPaymentId);
         ps.setInt(2, CreditCardPaymentData.StatusEnum.APPROVED.value());
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Invalid credit card payment ID " + creditCardPaymentId);
         }

         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = rs.getString("username");
         accountEntry.type = AccountEntryData.TypeEnum.CREDIT_CARD_REFUND;
         accountEntry.reference = String.valueOf(creditCardPaymentId);
         accountEntry.description = "Credit card refund";
         accountEntry.currency = rs.getString("currency");
         accountEntry.amount = -rs.getDouble("amount");
         accountEntry.fundedAmount = accountEntry.amount;
         accountEntry.tax = 0.0D;
         var7 = this.createAccountEntry(conn, accountEntry, accountEntrySourceData);
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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

   public void creditCardChargeBack(String providerTransactionId, Date date, String reasonCode) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select id, username from creditcardpayment where providertransactionid = ? and status <> ?");
         ps.setString(1, providerTransactionId);
         ps.setInt(2, CreditCardPaymentData.StatusEnum.CHARGE_BACK.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            int transactionId = rs.getInt("id");
            String username = rs.getString("username");
            ps.close();
            ps = conn.prepareStatement("update creditcardpayment set status = ?, chargebackdate = ?, chargebackreasoncode = ? where providertransactionid = ?");
            ps.setInt(1, CreditCardPaymentData.StatusEnum.CHARGE_BACK.value());
            ps.setDate(2, new java.sql.Date(date.getTime()));
            ps.setString(3, reasonCode);
            ps.setString(4, providerTransactionId);
            ps.executeUpdate();
            ps.close();
            ps = conn.prepareStatement("update user set status = ?, notes = ? where username = ? and status = ?");
            ps.setInt(1, UserData.StatusEnum.INACTIVE.value());
            ps.setString(2, "Credit card charge back. Transaction ID " + transactionId);
            ps.setString(3, username);
            ps.setInt(4, UserData.StatusEnum.ACTIVE.value());
            ps.executeUpdate();
         }
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

   }

   public CreditCardPaymentData updateCreditCardPaymentStatus(CreditCardPaymentData paymentData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      Object rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update creditcardpayment set status = ?, providertransactionid = ?, responsecode = ?, autoapprove = ? where id = ?");
         ps.setInt(1, paymentData.status.value());
         ps.setString(2, paymentData.providerTransactionId);
         ps.setString(3, paymentData.responseCode);
         ps.setInt(4, paymentData.autoApprove.value());
         ps.setDouble(5, (double)paymentData.id);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Failed to update credit card payment status");
         }

         if (paymentData.status == CreditCardPaymentData.StatusEnum.APPROVED) {
            this.creditUserFromCreditCardPayment(paymentData, accountEntrySourceData, conn);
         }
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               ((ResultSet)rs).close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

      return paymentData;
   }

   public void creditUserFromCreditCardPayment(CreditCardPaymentData paymentData, AccountEntrySourceData accountEntrySourceData, Connection masterConn) {
      ConnectionHolder ch = null;

      try {
         ch = new ConnectionHolder(this.dataSourceMaster, masterConn);
         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = paymentData.username;
         accountEntry.type = AccountEntryData.TypeEnum.CREDIT_CARD;
         accountEntry.reference = String.valueOf(paymentData.id);
         accountEntry.description = "Credit card payment";
         accountEntry.currency = paymentData.currency;
         accountEntry.amount = paymentData.amount;
         accountEntry.tax = this.calculateTaxComponent(ch.getConnection(), paymentData.username, paymentData.amount);
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CurrencyData currencyData = misBean.getCurrency(paymentData.currency);
         DiscountTierData discountTierData = this.getApplicableDiscountTier(Enums.PaymentEnum.CREDIT_CARD, paymentData.username, Math.abs(paymentData.amount), currencyData, false);
         if (discountTierData != null) {
            accountEntry.amount = accountEntry.amount + discountTierData.discountAmount;
            this.createDiscountTierAdjustmentAccountEntry(ch.getConnection(), paymentData.username, String.valueOf(paymentData.id), discountTierData.percentageDiscount, paymentData.currency, discountTierData.adjustmentAmount, accountEntrySourceData);
         }

         accountEntry.fundedAmount = accountEntry.amount;
         accountEntry = this.createAccountEntry(ch.getConnection(), accountEntry, accountEntrySourceData);
         if (discountTierData != null) {
            this.applyDiscountTier(ch.getConnection(), paymentData.username, discountTierData, accountEntry);
            paymentData.percentageDiscount = discountTierData.percentageDiscount;
            paymentData.discountAmount = discountTierData.discountAmount;
         }

         try {
            this.sendRechargeCreditRewardProgramTrigger(accountEntry.username, accountEntry.fundedAmount, accountEntry.currency, 0);
         } catch (Exception var31) {
            log.warn("Unable to send recharge credit reward program trigger for user: " + accountEntry.username);
         }

         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.AUTO_CREDIT_CARD_RECHARGE_BONUS_ENABLED)) {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            boolean isMentor = false;
            UserData.TypeEnum userType = UserData.TypeEnum.MIG33;

            try {
               MerchantDetailsData merchantData = userBean.getFullMerchantDetails(paymentData.username);
               userType = merchantData.type == MerchantDetailsData.TypeEnum.LEAD ? UserData.TypeEnum.MIG33_MERCHANT : UserData.TypeEnum.MIG33_TOP_MERCHANT;
               isMentor = merchantData.isMerchantMentor();
            } catch (Exception var30) {
               log.error(var30.getMessage());
            }

            try {
               Double bonus = this.calculateRechargeCreditBonusInBaseCurrency(paymentData.amount, paymentData.currency, userType, isMentor);
               if (bonus > 0.0D) {
                  CashReceiptData cashReceiptData = new CashReceiptData();
                  cashReceiptData.amountCredited = bonus;
                  cashReceiptData.amountReceived = 0.0D;
                  cashReceiptData.amountSent = 0.0D;
                  cashReceiptData.dateCreated = paymentData.dateCreated;
                  cashReceiptData.dateReceived = paymentData.dateCreated;
                  cashReceiptData.type = CashReceiptData.TypeEnum.DIRECT_CREDIT;
                  cashReceiptData.providerTransactionID = paymentData.providerTransactionId;
                  cashReceiptData.senderUsername = paymentData.username;
                  cashReceiptData.comments = "Credit Card Bonus";
                  cashReceiptData.paymentDetails = "Credit Card Bonus";
                  cashReceiptData.enteredBy = "";
                  misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
                  misBean.createCashReceipt(cashReceiptData, accountEntrySourceData);
               }
            } catch (Exception var29) {
               try {
                  EmailUserNotification note = new EmailUserNotification();
                  String amountString = (new DecimalFormat("0.00 ")).format(paymentData.amount) + " " + paymentData.currency;
                  note.emailAddress = SystemProperty.get("CreditCardPaymentNotificationEmail");
                  note.subject = "ERROR in giving away bonus credit on credit card payment";
                  note.message = "An error occurred in giving away bonus credit. \n";
                  note.message = note.message + "User: " + paymentData.username + "\n";
                  note.message = note.message + "Amount: " + amountString + "\n";
                  note.message = note.message + "Currency: " + paymentData.currency + "\n";
                  note.message = note.message + "Payment ID: " + paymentData.id;
                  EJBIcePrxFinder.getUserNotificationServiceProxy().notifyUserViaEmail(note);
               } catch (Exception var28) {
               }
            }
         }
      } catch (CreateException var32) {
         log.error("Unable to issue credits to user [" + paymentData.username + "] for payment [" + paymentData.id + "]", var32);
         throw new EJBException("An error occurred in your request. Please contact merchant@mig.me");
      } catch (SQLException var33) {
         log.error("Unable to issue credits to user [" + paymentData.username + "] for payment [" + paymentData.id + "]", var33);
         throw new EJBException("An error occurred in your request. Please contact merchant@mig.me");
      } finally {
         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var27) {
            ch = null;
         }

      }

   }

   private void createDiscountTierAdjustmentAccountEntry(Connection conn, String username, String reference, double newDiscountTierPercentage, String currency, double adjustmentAmount, AccountEntrySourceData accountEntrySourceData) throws CreateException, SQLException {
      if (!(adjustmentAmount <= 0.0D)) {
         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = username;
         accountEntry.type = AccountEntryData.TypeEnum.DISCOUNT_TIER_ADJUSTMENT;
         accountEntry.reference = reference;
         accountEntry.description = "Adjustemnt for reaching the " + Math.round(newDiscountTierPercentage) + "% discount tier";
         accountEntry.currency = currency;
         accountEntry.amount = adjustmentAmount;
         accountEntry.tax = 0.0D;
         accountEntry.fundedAmount = adjustmentAmount;
         this.createAccountEntry(conn, accountEntry, accountEntrySourceData);
      }
   }

   private CreditCardPaymentData makeCreditCardTransactionGlobalCollect(CreditCardPaymentData paymentData, CountryData countryData, String mobilePhone, String merchantID) {
      try {
         Integer paymentProductId = paymentData.getGlobalCollectPaymentProductId();
         if (paymentProductId == null) {
            throw new EJBException("Unable to determine payment product ID for card type " + paymentData.cardType);
         } else {
            String customerID = mobilePhone;
            if (mobilePhone.length() > 15) {
               customerID = mobilePhone.substring(0, 15);
            }

            StringBuilder request = new StringBuilder();
            request.append("<XML>");
            request.append("<REQUEST>");
            request.append("<ACTION>INSERT_ORDERWITHPAYMENT</ACTION>");
            request.append("<META>");
            request.append("<MERCHANTID>").append(merchantID).append("</MERCHANTID>");
            request.append("</META>");
            request.append("<PARAMS>");
            request.append("<ORDER>");
            request.append("<ORDERID>").append(paymentData.id).append("</ORDERID>");
            request.append("<FIRSTNAME>").append(paymentData.firstName).append("</FIRSTNAME>");
            request.append("<SURNAME>").append(paymentData.lastName).append("</SURNAME>");
            request.append("<CUSTOMERID>").append(customerID).append("</CUSTOMERID>");
            request.append("<IPADDRESSCUSTOMER>").append(paymentData.ipAddress).append("</IPADDRESSCUSTOMER>");
            request.append("<AMOUNT>").append((int)(paymentData.amount * 100.0D)).append("</AMOUNT>");
            request.append("<CURRENCYCODE>").append(paymentData.currency).append("</CURRENCYCODE>");
            request.append("<LANGUAGECODE>").append(countryData.isoLanguageCode).append("</LANGUAGECODE>");
            request.append("<COUNTRYCODE>").append(countryData.isoCountryCode).append("</COUNTRYCODE>");
            request.append("</ORDER>");
            request.append("<PAYMENT>");
            request.append("<PAYMENTPRODUCTID>").append(paymentProductId).append("</PAYMENTPRODUCTID>");
            request.append("<AMOUNT>").append((int)(paymentData.amount * 100.0D)).append("</AMOUNT>");
            request.append("<CURRENCYCODE>").append(paymentData.currency).append("</CURRENCYCODE>");
            request.append("<LANGUAGECODE>").append(countryData.isoLanguageCode).append("</LANGUAGECODE>");
            request.append("<COUNTRYCODE>").append(countryData.isoCountryCode).append("</COUNTRYCODE>");
            request.append("<EXPIRYDATE>").append(paymentData.cardExpiryDate).append("</EXPIRYDATE>");
            request.append("<CREDITCARDNUMBER>").append(paymentData.cardNumber).append("</CREDITCARDNUMBER>");
            if (paymentData.cardVerificationNumber != null) {
               request.append("<CVV>").append(paymentData.cardVerificationNumber).append("</CVV>");
               request.append("<CVVINDICATOR>1</CVVINDICATOR>");
            }

            if (paymentData.source == CreditCardPaymentData.SourceEnum.WEB) {
               request.append("<CUSTOMERIPADDRESS>").append(paymentData.ipAddress).append("</CUSTOMERIPADDRESS>");
            }

            request.append("</PAYMENT>");
            request.append("</PARAMS>");
            request.append("</REQUEST>");
            request.append("</XML>");
            log.info("request to GC " + request);

            try {
               SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.GLOBAL_COLLECT_URL), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
               if (reply.containsTag("ERROR")) {
                  throw new Exception(reply.getTagValue("ERROR", "CODE") + "," + reply.getTagValue("ERROR", "MESSAGE"));
               }

               paymentData.providerTransactionId = reply.getTagValue("ROW", "ADDITIONALREFERENCE");
               paymentData.responseCode = reply.getTagValue("ROW", "STATUSID");
            } catch (ClientProtocolException var9) {
               log.error("Unable to send cc request to GC: " + var9.getMessage());
            }

            if (paymentData.providerTransactionId == null && paymentData.responseCode == null) {
               throw new Exception("Unable to obtain provider transaction ID");
            } else {
               paymentData.status = CreditCardPaymentData.StatusEnum.AWAITING_APPROVAL;
               return paymentData;
            }
         }
      } catch (Exception var10) {
         paymentData.status = CreditCardPaymentData.StatusEnum.DECLINED;
         paymentData.responseCode = var10.getMessage();
         return paymentData;
      }
   }

   private boolean isValidCreditCardNumber(String cardNumber) {
      int length = cardNumber.length();
      if (length < 13) {
         return false;
      } else {
         int digit = false;
         int sum = 0;
         boolean timesTwo = false;

         for(int i = length - 1; i >= 0; --i) {
            int digit;
            try {
               digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            } catch (NumberFormatException var8) {
               return false;
            }

            if (timesTwo) {
               digit *= 2;
               if (digit > 9) {
                  digit -= 9;
               }
            }

            sum += digit;
            timesTwo = !timesTwo;
         }

         return sum != 0 && sum % 10 == 0;
      }
   }

   private boolean isValidCreditCardExpiryDate(String expiryDate) {
      Matcher matcher = Pattern.compile("^(0[1-9]|1[012])(\\d\\d)$").matcher(expiryDate);
      if (matcher.find() && matcher.groupCount() == 2) {
         Calendar calendar = Calendar.getInstance();
         int month = Integer.parseInt(matcher.group(1));
         int year = Integer.parseInt(matcher.group(2));
         int currentYear = calendar.get(1) % 100;
         int currentMonth = calendar.get(2);
         return year < 50 && (year > currentYear || year == currentYear && month >= currentMonth);
      } else {
         return false;
      }
   }

   public Vector getCreditCardTransactions(String startDate, String endDate, String sortBy, String sortOrder, String showAuth, String showPend, String showRej, String username, int displayLimit) throws Exception {
      Vector transactions = new Vector();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Crypter crypter = new Crypter(CreditCardUtils.CRYPTER_KEY_LOCATION);
      String transType = "AND Creditcardpayment.Status IN (";
      String patternStr = ",$";
      String replaceStr = "";
      String usernameStr = "";
      Pattern pattern = Pattern.compile(patternStr);
      if (showPend != null && !showPend.equals("null")) {
         transType = transType + "0,";
      }

      if (showAuth != null && !showAuth.equals("null")) {
         transType = transType + "1,";
      }

      if (showRej != null && !showRej.equals("null")) {
         transType = transType + "2,";
      }

      if (username.length() > 0) {
         usernameStr = "AND Creditcardpayment.Username = '" + username + "' ";
      }

      Matcher matcher = pattern.matcher(transType);
      String output = matcher.replaceAll(replaceStr);
      transType = output + ") ";
      String sql = "SELECT Creditcardpayment.Id, \t\t DATE(DateCreated) AS DateCreated, \t\t Country.Name AS UserCountry, \t\t Creditcardpayment.Username, \t\t CardNumber, \t\t CardHolder, \t\t CardExpiryDate, \t\t CASE CardType WHEN 1 THEN 'Visa' WHEN 2 THEN 'MasterCard' WHEN 3 THEN 'BankCard' WHEN 4 THEN 'AMEX' WHEN 5 THEN 'Discover' WHEN 6 THEN 'Diners Club' WHEN 7 THEN 'JCB' ELSE 'Unknown' END AS CardType, \t\t Amount, \t\t Creditcardpayment.Currency, \t\t CASE Creditcardpayment.status WHEN 0 then 'PENDING' WHEN 1 THEN 'APPROVED' WHEN 2 THEN 'REJECTED' ELSE 'Unknown' END AS Status, \t\t Country.Name, \t\t AutoApprove,  \t\t details FROM creditcardpayment, User, Country WHERE User.Username = Creditcardpayment.Username AND \t \tUser.CountryId = Country.Id AND \t\tSUBSTR(DateCreated,1,10) >= '" + startDate + "' AND SUBSTR(DateCreated,1,10) <= '" + endDate + "' " + "\t\t" + transType + "\t\t" + usernameStr + " ORDER BY " + sortBy + " " + sortOrder + " LIMIT " + displayLimit;

      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         rs = ps.executeQuery();

         Hashtable cc_transaction;
         for(SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm"); rs.next(); transactions.add(cc_transaction)) {
            cc_transaction = new Hashtable();
            cc_transaction.put("id", String.valueOf(rs.getInt("id")));
            cc_transaction.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
            cc_transaction.put("username", rs.getString("username"));
            cc_transaction.put("cardNumber", rs.getString("cardNumber"));
            cc_transaction.put("cardHolder", rs.getString("CardHolder") != null ? crypter.decrypt(rs.getString("CardHolder")) : null);
            cc_transaction.put("expiry", rs.getString("CardHolder") != null ? crypter.decrypt(rs.getString("CardExpiryDate")) : null);
            cc_transaction.put("cardType", rs.getString("cardType"));
            cc_transaction.put("status", rs.getString("status"));
            cc_transaction.put("country", rs.getString("UserCountry"));
            cc_transaction.put("amount", rs.getString("amount"));
            cc_transaction.put("currency", rs.getString("currency"));
            cc_transaction.put("autoApprove", rs.getBoolean("autoApprove"));

            try {
               if (null != rs.getString("details")) {
                  JSONObject details = new JSONObject(rs.getString("details"));
                  cc_transaction.put("firstName", details.getString("firstname"));
                  cc_transaction.put("lastName", details.getString("lastname"));
               } else {
                  cc_transaction.put("firstName", "");
                  cc_transaction.put("lastName", "");
               }
            } catch (JSONException var41) {
            }
         }

         Vector var44 = transactions;
         return var44;
      } catch (Exception var42) {
         throw new EJBException(var42.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var40) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var39) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var38) {
            conn = null;
         }

      }
   }

   public Vector getCreditCardHMLTransactions(String startDate, String endDate, String sortBy, String sortOrder, String showAuth, String showPend, String showRej, String username, int displayLimit) throws Exception {
      Vector transactions = new Vector();
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      new Crypter(CreditCardUtils.CRYPTER_KEY_LOCATION);
      String transType = "AND p.Status IN (";
      String patternStr = ",$";
      String replaceStr = "";
      String usernameStr = "";
      Pattern pattern = Pattern.compile(patternStr);
      if (showPend != null && !showPend.equals("null")) {
         transType = transType + "0,";
      }

      if (showAuth != null && !showAuth.equals("null")) {
         transType = transType + "1,";
      }

      if (showRej != null && !showRej.equals("null")) {
         transType = transType + "2,";
      }

      if (username.length() > 0) {
         usernameStr = "AND u.Username = '" + username + "' ";
      }

      Matcher matcher = pattern.matcher(transType);
      String output = matcher.replaceAll(replaceStr);
      transType = output + ") ";
      String sql = "SELECT p.Id,        DATE(p.dateupdated) AS DateCreated,        u.Username,        Amount,        p.currency,        CASE p.status WHEN 0 then 'PENDING'                      WHEN 1 THEN 'APPROVED'                      WHEN 2 THEN 'REJECTED'                      WHEN 6 THEN 'VENDOR_FAILED'                      WHEN 7 THEN 'ONHOLD'                      ELSE 'Unknown' END AS Status,        c.Name country,        pmd.detail autoApprove FROM payments p LEFT JOIN paymenttopaymentmetadetails p2pm ON p2pm.paymentid = p.id                 LEFT JOIN paymentmetadetails pmd ON p2pm.paymentmetadetailsid = pmd.id AND pmd.type = 4,      user u, userid ui, country c WHERE p.userid = ui.id       AND u.username = ui.username       AND u.countryid = c.id       AND p.type = ? \t\t" + transType + "\t\t" + usernameStr + "      AND p.dateupdated BETWEEN ? AND ? " + "GROUP BY p.id " + "ORDER BY " + sortBy + " " + sortOrder + " LIMIT " + displayLimit;

      Vector var41;
      try {
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, PaymentData.TypeEnum.CREDIT_CARD.value());
         ps.setString(2, startDate);
         ps.setString(3, endDate);
         rs = ps.executeQuery();
         SimpleDateFormat df = new SimpleDateFormat("d MMM yy HH:mm");

         while(rs.next()) {
            Hashtable cc_transaction = new Hashtable();
            cc_transaction.put("id", String.valueOf(rs.getInt("id")));
            cc_transaction.put("dateCreated", df.format(rs.getTimestamp("dateCreated")));
            cc_transaction.put("username", rs.getString("username"));
            cc_transaction.put("status", rs.getString("status"));
            cc_transaction.put("country", rs.getString("country"));
            cc_transaction.put("amount", rs.getString("amount"));
            cc_transaction.put("currency", rs.getString("currency"));
            cc_transaction.put("autoApprove", rs.getBoolean("autoApprove"));
            transactions.add(cc_transaction);
         }

         var41 = transactions;
      } catch (Exception var39) {
         throw new EJBException(var39.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var38) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var37) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var36) {
            conn = null;
         }

      }

      return var41;
   }

   private String getCreditCardCountryIso(String creditCardNumber, String merchantID) throws Exception {
      Locale errorMessageLocale = Locale.ENGLISH;
      creditCardNumber = creditCardNumber.substring(0, 13);
      StringBuilder request = new StringBuilder();
      request.append("<XML>");
      request.append("<REQUEST>");
      request.append("<ACTION>DO_BINLOOKUP</ACTION>");
      request.append("<META>");
      request.append("<MERCHANTID>").append(merchantID).append("</MERCHANTID>");
      request.append("</META>");
      request.append("<PARAMS>");
      request.append("<GENERAL>");
      request.append("<BIN>").append(creditCardNumber).append("</BIN>");
      request.append("</GENERAL>");
      request.append("</PARAMS>");
      request.append("</REQUEST>");
      request.append("</XML>");
      SimpleXMLParser reply = new SimpleXMLParser(SystemProperty.get("GlobalCollectURL"), request.toString(), SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.CREDIT_CARD_PAYMENT_STRICT_SSL_CHECK));
      if (reply.containsTag("ERROR")) {
         log.error("Unable to get country data for creditcard [" + creditCardNumber + "] " + reply.getTagValue("ERROR", "CODE") + "," + reply.getTagValue("ERROR", "MESSAGE"));
         throw new Exception(CreditCardPaymentData.ErrorEnum.UNABLE_TO_GET_CC_DETAIL.message(errorMessageLocale));
      } else if (!reply.containsTag("COUNTRYCODE")) {
         throw new Exception(CreditCardPaymentData.ErrorEnum.UNABLE_TO_GET_CC_DETAIL.message(errorMessageLocale));
      } else {
         return reply.getTagValue("ROW", "COUNTRYCODE");
      }
   }

   public CurrencyData getUsersLocalCurrency(String username) throws EJBException {
      return this.getAccountBalance(username).currency;
   }

   public void setUsersLocalCurrency(String username, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         AccountBalanceData accountBalance = this.getAccountBalance(username);
         if (!accountBalance.currency.code.equalsIgnoreCase(currency)) {
            conn = this.dataSourceMaster.getConnection();
            AccountEntryData accountEntry = null;
            if (accountBalance.balance != 0.0D) {
               accountEntry = new AccountEntryData();
               accountEntry.username = username;
               accountEntry.type = AccountEntryData.TypeEnum.CURRENCY_CONVERSION;
               accountEntry.reference = username;
               accountEntry.description = "Currency conversion from " + accountBalance.currency.code + " to " + currency;
               accountEntry.currency = accountBalance.currency.code;
               accountEntry.amount = -accountBalance.balance;
               accountEntry.fundedAmount = -accountBalance.fundedBalance;
               accountEntry.tax = 0.0D;
               this.createAccountEntry(conn, accountEntry, accountEntrySourceData);
               if (this.getAccountBalance(username).balance != 0.0D) {
                  throw new EJBException("Failed to set user's account balance to 0");
               }
            }

            ps = conn.prepareStatement("update user set currency = ? where username = ?");
            ps.setString(1, currency);
            ps.setString(2, username);
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Failed to change user's currency to " + currency);
            }

            if (accountEntry != null) {
               accountEntry.amount = accountEntry.amount * -1.0D;
               accountEntry.fundedAmount = accountEntry.fundedAmount * -1.0D;
               this.createAccountEntry(conn, accountEntry, accountEntrySourceData);
            }
         }
      } catch (SQLException var20) {
         throw new EJBException(var20.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var18) {
            conn = null;
         }

      }

   }

   public int getPaymentType(String paymentReference) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from banktransferintent where paymentreference = ?");
         ps.setString(1, paymentReference);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = Enums.PaymentEnum.BANK_TRANSFER.value();
            return var5;
         }

         rs.close();
         ps.close();
         ps = conn.prepareStatement("select * from westernunionintent where paymentreference = ?");
         ps.setString(1, paymentReference);
         rs = ps.executeQuery();
         if (!rs.next()) {
            byte var28 = 0;
            return var28;
         }

         var5 = Enums.PaymentEnum.WESTERN_UNION.value();
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var23) {
            conn = null;
         }

      }

      return var5;
   }

   public <T extends PaymentData> List<T> getPaymentsByMetaData(PaymentData.TypeEnum paymentType, PaymentMetaDetails... paymentMetaDetails) throws FusionEJBException {
      if (paymentType == null) {
         throw new FusionEJBException("Undefined payment type.");
      } else if (paymentMetaDetails.length == 0) {
         throw new FusionEJBException("No meta details defined.");
      } else {
         Connection conn = null;
         PreparedStatement ps = null;
         PreparedStatement ps1 = null;
         ResultSet rs = null;
         ResultSet rs1 = null;

         ArrayList var44;
         try {
            String sql = "SELECT p.*, COUNT(pm.id) matches, ui.username FROM payments p, paymentmetadetails pm, paymenttopaymentmetadetails p2pm, userid ui WHERE p.userid = ui.id AND p.id = p2pm.paymentid AND pm.id = p2pm.paymentmetadetailsid AND p.type = ? AND (";
            List<String> appendSqlConditions = new ArrayList();

            int paramIdx;
            for(paramIdx = 0; paramIdx < paymentMetaDetails.length; ++paramIdx) {
               appendSqlConditions.add("(pm.detail = ? AND pm.type = ?)");
            }

            sql = sql + StringUtil.join((Collection)appendSqlConditions, " OR ") + ") " + "GROUP BY p.id " + "HAVING matches = ?";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, paymentType.value());
            paramIdx = 1;
            PaymentMetaDetails[] arr$ = paymentMetaDetails;
            int len$ = paymentMetaDetails.length;

            for(int i$ = 0; i$ < len$; ++i$) {
               PaymentMetaDetails paymentMetaDetail = arr$[i$];
               ++paramIdx;
               ps.setString(paramIdx, paymentMetaDetail.value);
               ++paramIdx;
               ps.setInt(paramIdx, paymentMetaDetail.type.code());
            }

            ++paramIdx;
            ps.setInt(paramIdx, paymentMetaDetails.length);
            rs = ps.executeQuery();
            List<T> paymentDataList = new ArrayList();
            String sqlMetaDetails = "SELECT * FROM paymentmetadetails pm, paymenttopaymentmetadetails p2pm WHERE pm.id = p2pm.paymentmetadetailsid AND p2pm.paymentid = ?";
            if (rs.next()) {
               T pData = PaymentDataFactory.getPayment(rs);
               ps1 = conn.prepareStatement(sqlMetaDetails);
               ps1.setInt(1, pData.id);
               rs1 = ps1.executeQuery();
               ArrayList meta = new ArrayList();

               while(rs1.next()) {
                  meta.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.fromCode(rs1.getInt("type")), rs1.getString("detail")));
               }

               pData.setDetails(meta);
               paymentDataList.add(pData);
            }

            var44 = paymentDataList;
         } catch (SQLException var37) {
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. Please contact merchant@mig.me", var37);
         } catch (FusionEJBException var38) {
            throw var38;
         } catch (Exception var39) {
            throw new FusionEJBException("Sorry, we are unable to process your request at the moment. Please contact merchant@mig.me", var39);
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var36) {
               rs = null;
            }

            try {
               if (rs1 != null) {
                  rs1.close();
               }
            } catch (SQLException var35) {
               rs1 = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var34) {
               ps = null;
            }

            try {
               if (ps1 != null) {
                  ps1.close();
               }
            } catch (SQLException var33) {
               ps1 = null;
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var32) {
               conn = null;
            }

         }

         return var44;
      }
   }

   public int getBankTransferProductID(int countryID) throws EJBException {
      try {
         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misBean.getCountry(countryID);
         if (countryData == null) {
            throw new EJBException("Invalid country ID " + countryID);
         } else {
            return countryData.allowBankTransfer == null ? 0 : countryData.allowBankTransfer;
         }
      } catch (Exception var4) {
         throw new EJBException(var4.getMessage());
      }
   }

   public BankTransferIntentData bankTransfer(BankTransferIntentData bankTransferIntentData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BankTransferIntentData var45;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(bankTransferIntentData.username, false, false);
         if (userData == null || userData.status != UserData.StatusEnum.ACTIVE) {
            throw new EJBException("Invalid user " + bankTransferIntentData.username);
         }

         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.MAKE_BANK_TRANSFER, userData)) {
            throw new EJBException("You must authenticate your account before requesting bank transfer");
         }

         if (bankTransferIntentData.countryID == null) {
            bankTransferIntentData.countryID = userData.countryID;
         }

         MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
         CountryData countryData = misBean.getCountry(userData.countryID);
         if (countryData == null) {
            throw new EJBException("Invalid country ID " + userData.countryID);
         }

         if (bankTransferIntentData.currency == null) {
            bankTransferIntentData.currency = countryData.currency;
         }

         CurrencyData usdCurrencyData = misBean.getCurrency("USD");
         CurrencyData currencyData = misBean.getCurrency(bankTransferIntentData.currency);
         double minAmountInLocalCurrency = currencyData.convertFrom(SystemProperty.getDouble("MinBankTransferAmount"), usdCurrencyData);
         if (bankTransferIntentData.amount < minAmountInLocalCurrency) {
            throw new EJBException("Bank transfer amount must be greater than " + currencyData.formatWithCode(minAmountInLocalCurrency));
         }

         double maxAmountInLocalCurrency = currencyData.convertFrom(SystemProperty.getDouble("MaxBankTransferAmount"), usdCurrencyData);
         if (bankTransferIntentData.amount > maxAmountInLocalCurrency) {
            throw new EJBException("Bank transfer amount must be less than " + currencyData.formatWithCode(maxAmountInLocalCurrency));
         }

         bankTransferIntentData.dateCreated = new Date(System.currentTimeMillis());
         bankTransferIntentData.status = BankTransferIntentData.StatusEnum.OPEN;
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("insert into banktransferintent (username, datecreated, countryid, paymentproductid, surname, fiscalnumber, amount, currency, status) values (?,?,?,?,?,?,?,?,?)", 1);
         ps.setString(1, bankTransferIntentData.username);
         ps.setTimestamp(2, new Timestamp(bankTransferIntentData.dateCreated.getTime()));
         ps.setObject(3, bankTransferIntentData.countryID);
         ps.setObject(4, bankTransferIntentData.paymentProductID);
         ps.setString(5, bankTransferIntentData.surname);
         ps.setString(6, bankTransferIntentData.fiscalNumber);
         ps.setObject(7, bankTransferIntentData.amount);
         ps.setString(8, bankTransferIntentData.currency);
         ps.setObject(9, bankTransferIntentData.status == null ? null : bankTransferIntentData.status.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Failed to create bank transfer intent");
         }

         bankTransferIntentData.id = rs.getInt(1);
         double amount = bankTransferIntentData.amount;
         if (!bankTransferIntentData.currency.equals(countryData.bankTransferCurrency)) {
            CurrencyData toCurrency = misBean.getCurrency(countryData.bankTransferCurrency);
            amount = currencyData.convertTo(amount, toCurrency);
         }

         String customerID = userData.mobilePhone;
         if (customerID.length() > 15) {
            customerID = customerID.substring(0, 15);
         }

         StringBuilder request = new StringBuilder();
         request.append("<XML>");
         request.append("<REQUEST>");
         request.append("<ACTION>INSERT_ORDERWITHPAYMENT</ACTION>");
         request.append("<META>");
         request.append("<MERCHANTID>").append(SystemProperty.get("BankTransferMerchantID")).append("</MERCHANTID>");
         request.append("</META>");
         request.append("<PARAMS>");
         request.append("<ORDER>");
         request.append("<ORDERID>").append(bankTransferIntentData.id * 10).append("</ORDERID>");
         request.append("<CUSTOMERID>").append(customerID).append("</CUSTOMERID>");
         request.append("<AMOUNT>").append((int)(amount * 100.0D)).append("</AMOUNT>");
         request.append("<CURRENCYCODE>").append(countryData.bankTransferCurrency).append("</CURRENCYCODE>");
         request.append("<LANGUAGECODE>").append(countryData.isoLanguageCode).append("</LANGUAGECODE>");
         request.append("<COUNTRYCODE>").append(countryData.isoCountryCode).append("</COUNTRYCODE>");
         request.append("</ORDER>");
         request.append("<PAYMENT>");
         request.append("<PAYMENTPRODUCTID>").append(bankTransferIntentData.paymentProductID).append("</PAYMENTPRODUCTID>");
         request.append("<AMOUNT>").append((int)(amount * 100.0D)).append("</AMOUNT>");
         request.append("<CURRENCYCODE>").append(countryData.bankTransferCurrency).append("</CURRENCYCODE>");
         request.append("<LANGUAGECODE>").append(countryData.isoLanguageCode).append("</LANGUAGECODE>");
         request.append("<COUNTRYCODE>").append(countryData.isoCountryCode).append("</COUNTRYCODE>");
         if (bankTransferIntentData.firstname != null && bankTransferIntentData.firstname.length() > 0) {
            request.append("<FIRSTNAME>").append(bankTransferIntentData.firstname).append("</FIRSTNAME>");
         }

         if (bankTransferIntentData.middlename != null && bankTransferIntentData.middlename.length() > 0) {
            request.append("<PREFIXSURNAME>").append(bankTransferIntentData.middlename).append("</PREFIXSURNAME>");
         }

         if (bankTransferIntentData.surname != null && bankTransferIntentData.surname.length() > 0) {
            request.append("<SURNAME>").append(bankTransferIntentData.surname).append("</SURNAME>");
         }

         if (bankTransferIntentData.fiscalNumber != null && bankTransferIntentData.fiscalNumber.length() > 0) {
            request.append("<FISCALNUMBER>").append(bankTransferIntentData.fiscalNumber).append("</FISCALNUMBER>");
         }

         request.append("</PAYMENT>");
         request.append("</PARAMS>");
         request.append("</REQUEST>");
         request.append("</XML>");
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER79_ENABLED)) {
            log.info("request to GC " + request);
         }

         URL url = new URL(SystemProperty.get("GlobalCollectURL"));
         HttpURLConnection httpConn = (HttpURLConnection)url.openConnection();
         httpConn.setDoOutput(true);
         httpConn.setUseCaches(false);
         httpConn.getOutputStream().write(request.toString().getBytes("UTF-8"));
         if (httpConn.getResponseCode() != 200) {
            throw new IOException("HTTP " + httpConn.getResponseCode() + " " + httpConn.getResponseMessage());
         }

         Document reply = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(httpConn.getInputStream());
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.ER79_ENABLED)) {
            log.info("reply from GC: " + this.getStringFromDocument(reply));
         }

         NodeList rows = reply.getElementsByTagName("ROW");
         Node row;
         if (rows == null || rows.getLength() == 0) {
            rows = reply.getElementsByTagName("ERROR");
            if (rows != null && rows.getLength() > 0) {
               for(row = rows.item(0).getFirstChild(); row != null; row = row.getNextSibling()) {
                  if (row.getNodeName().equalsIgnoreCase("MESSAGE")) {
                     throw new IOException(row.getFirstChild().getNodeValue());
                  }
               }
            }

            throw new IOException("Unknown error");
         }

         String nodeName;
         for(row = rows.item(0).getFirstChild(); row != null; row = row.getNextSibling()) {
            nodeName = row.getNodeName();
            String nodeText = row.getFirstChild().getNodeValue();
            if ("COUNTRYDESCRIPTION".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.countryDescription = nodeText;
            } else if ("STATUSID".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.statusID = Integer.parseInt(nodeText);
            } else if ("ADDITIONALREFERENCE".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.additionalReference = nodeText;
            } else if ("ACCOUNTHOLDER".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.accountHolder = nodeText;
            } else if ("BANKNAME".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.bankName = nodeText;
            } else if ("EXTERNALREFERENCE".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.externalReference = nodeText;
            } else if ("EFFORTID".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.effortID = Integer.parseInt(nodeText);
            } else if ("PAYMENTREFERENCE".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.paymentReference = nodeText;
            } else if ("ATTEMPTID".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.attemptID = Integer.parseInt(nodeText);
            } else if ("MERCHANTID".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.merchantID = Integer.parseInt(nodeText);
            } else if ("BANKACCOUNTNUMBER".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.bankAccountNumber = nodeText;
            } else if ("STATUSDATE".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.statusDate = nodeText;
            } else if ("CITY".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.city = nodeText;
            } else if ("ORDERID".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.orderID = Integer.parseInt(nodeText);
            } else if ("SPECIALID".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.specialID = nodeText;
            } else if ("SWIFTCODE".equalsIgnoreCase(nodeName)) {
               bankTransferIntentData.swiftCode = nodeText;
            }
         }

         if (ps != null) {
            ps.close();
         }

         ps = conn.prepareStatement("update banktransferintent set countrydescription=?, statusid=?, additionalreference=?, accountholder=?, bankname=?, externalreference=?, effortid=?, paymentreference=?, attemptid=?, merchantid=?, bankaccountnumber=?, statusdate=?, city=?, orderid=?, specialid=?, swiftcode=? where id = ?");
         ps.setString(1, bankTransferIntentData.countryDescription);
         ps.setObject(2, bankTransferIntentData.statusID);
         ps.setString(3, bankTransferIntentData.additionalReference);
         ps.setString(4, bankTransferIntentData.accountHolder);
         ps.setString(5, bankTransferIntentData.bankName);
         ps.setString(6, bankTransferIntentData.externalReference);
         ps.setObject(7, bankTransferIntentData.effortID);
         ps.setString(8, bankTransferIntentData.paymentReference);
         ps.setObject(9, bankTransferIntentData.attemptID);
         ps.setObject(10, bankTransferIntentData.merchantID);
         ps.setString(11, bankTransferIntentData.bankAccountNumber);
         ps.setString(12, bankTransferIntentData.statusDate);
         ps.setString(13, bankTransferIntentData.city);
         ps.setObject(14, bankTransferIntentData.orderID);
         ps.setString(15, bankTransferIntentData.specialID);
         ps.setString(16, bankTransferIntentData.swiftCode);
         ps.setInt(17, bankTransferIntentData.id);
         ps.executeUpdate();

         try {
            nodeName = SystemProperty.get("BankTransferInstructionEmail");
            if (nodeName.length() > 0) {
               nodeName = nodeName.replaceAll("%1", bankTransferIntentData.paymentReference).replaceAll("%2", bankTransferIntentData.accountHolder).replaceAll("%3", bankTransferIntentData.bankName).replaceAll("%4", bankTransferIntentData.city).replaceAll("%5", bankTransferIntentData.bankAccountNumber).replaceAll("%6", bankTransferIntentData.swiftCode == null ? "" : bankTransferIntentData.swiftCode).replaceAll("%7", bankTransferIntentData.specialID == null ? "" : bankTransferIntentData.specialID).replaceAll("%8", (new DecimalFormat("0.00 ")).format(bankTransferIntentData.amount) + bankTransferIntentData.currency).replaceAll("%u", bankTransferIntentData.username);
               com.projectgoth.fusion.interfaces.Message messageBean = (com.projectgoth.fusion.interfaces.Message)EJBHomeCache.getObject("ejb/Message", MessageHome.class);
               messageBean.sendSystemEmail(bankTransferIntentData.username, "Bank transfer instruction", nodeName);
            }
         } catch (Exception var41) {
            log.warn("Unable to send bank transfer instruction email to [" + bankTransferIntentData.username + "]", var41);
         }

         var45 = bankTransferIntentData;
      } catch (Exception var42) {
         throw new EJBException(var42.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var40) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var39) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var38) {
            conn = null;
         }

      }

      return var45;
   }

   public String getStringFromDocument(Document doc) {
      try {
         DOMSource domSource = new DOMSource(doc);
         StringWriter writer = new StringWriter();
         StreamResult result = new StreamResult(writer);
         TransformerFactory.newInstance().newTransformer().transform(domSource, result);
         return writer.toString();
      } catch (TransformerException var5) {
         log.error("invalid GC response xml" + var5.getMessage());
         return null;
      }
   }

   public BankTransferReceivedData updateBankTransferStatus(BankTransferReceivedData bankTransferReceivedData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      BankTransferReceivedData var40;
      try {
         conn = this.dataSourceMaster.getConnection();
         String username = null;
         String mobilePhone = null;
         if (bankTransferReceivedData.paymentReference != null) {
            ps = conn.prepareStatement("select banktransferintent.id, user.username, user.mobilephone, user.type from banktransferintent, user where banktransferintent.username = user.username and banktransferintent.paymentreference = ?");
            ps.setString(1, bankTransferReceivedData.paymentReference);
            rs = ps.executeQuery();
            if (rs.next()) {
               bankTransferReceivedData.bankTransferIntentID = rs.getInt("id");
               username = rs.getString("username");
               mobilePhone = rs.getString("mobilephone");
            }

            rs.close();
            ps.close();
         }

         ps = conn.prepareStatement("select * from banktransferreceived where filename = ? and row = ?");
         ps.setString(1, bankTransferReceivedData.fileName);
         ps.setInt(2, bankTransferReceivedData.row);
         rs = ps.executeQuery();
         if (rs.next()) {
            bankTransferReceivedData.id = rs.getInt("id");
            bankTransferReceivedData.dateCreated = new Date(rs.getTimestamp("dateCreated").getTime());
            Integer intVal = (Integer)rs.getObject("status");
            if (intVal != null) {
               bankTransferReceivedData.status = BankTransferReceivedData.StatusEnum.fromValue(intVal);
            }
         } else {
            bankTransferReceivedData.dateCreated = new Date(System.currentTimeMillis());
            bankTransferReceivedData.status = BankTransferReceivedData.StatusEnum.NEW;
            ps = conn.prepareStatement("insert into banktransferreceived (banktransferintentid, datecreated, type, filename, row, paymentreference, invoicenumber, customerid, additionalreference, effortnumber, invoicecurrencydeliv, invoiceamountdeliv, invoicecurrencylocal, invoiceamountlocal, paymentmethod, creditcardcompany, uncleanindicator, paymentcurrency, paymentamount, currencydue, amountdue, datedue, reversalcurrency, reversalamount, reversalreasonid, reversalreasondescription, datecollect, status) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)", 1);
            ps.setObject(1, bankTransferReceivedData.bankTransferIntentID);
            ps.setTimestamp(2, new Timestamp(bankTransferReceivedData.dateCreated.getTime()));
            ps.setObject(3, bankTransferReceivedData.type == null ? null : bankTransferReceivedData.type.value());
            ps.setString(4, bankTransferReceivedData.fileName);
            ps.setObject(5, bankTransferReceivedData.row);
            ps.setString(6, bankTransferReceivedData.paymentReference);
            ps.setString(7, bankTransferReceivedData.invoiceNumber);
            ps.setString(8, bankTransferReceivedData.customerID);
            ps.setString(9, bankTransferReceivedData.additionalReference);
            ps.setObject(10, bankTransferReceivedData.effortNumber);
            ps.setString(11, bankTransferReceivedData.invoiceCurrencyDeliv);
            ps.setObject(12, bankTransferReceivedData.invoiceAmountDeliv);
            ps.setString(13, bankTransferReceivedData.invoiceCurrencyLocal);
            ps.setObject(14, bankTransferReceivedData.invoiceAmountLocal);
            ps.setString(15, bankTransferReceivedData.paymentMethod);
            ps.setString(16, bankTransferReceivedData.creditCardCompany);
            ps.setString(17, bankTransferReceivedData.uncleanIndicator);
            ps.setString(18, bankTransferReceivedData.paymentCurrency);
            ps.setObject(19, bankTransferReceivedData.paymentAmount);
            ps.setString(20, bankTransferReceivedData.currencyDue);
            ps.setObject(21, bankTransferReceivedData.amountDue);
            ps.setObject(22, bankTransferReceivedData.dateDue);
            ps.setString(23, bankTransferReceivedData.reversalCurrency);
            ps.setObject(24, bankTransferReceivedData.reversalAmount);
            ps.setString(25, bankTransferReceivedData.reversalReasonID);
            ps.setString(26, bankTransferReceivedData.reversalReasonDescription);
            ps.setObject(27, bankTransferReceivedData.dateCollect);
            ps.setObject(28, bankTransferReceivedData.status == null ? null : bankTransferReceivedData.status.value());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new SQLException("Failed to create bank transfer received");
            }

            bankTransferReceivedData.id = rs.getInt(1);
         }

         if (bankTransferReceivedData.status == BankTransferReceivedData.StatusEnum.NEW && bankTransferReceivedData.bankTransferIntentID != null) {
            MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            CurrencyData currencyData = misBean.getCurrency(bankTransferReceivedData.paymentCurrency);
            DiscountTierData discountTier = this.getApplicableDiscountTier(Enums.PaymentEnum.BANK_TRANSFER, username, bankTransferReceivedData.paymentAmount, currencyData, false);
            double discount = discountTier == null ? 0.0D : discountTier.discountAmount;
            AccountEntryData accountEntryData = new AccountEntryData();
            accountEntryData.username = username;
            accountEntryData.reference = String.valueOf(bankTransferReceivedData.id);
            accountEntryData.tax = 0.0D;
            switch(bankTransferReceivedData.type) {
            case PAYMENT:
               accountEntryData.type = AccountEntryData.TypeEnum.BANK_TRANSFER;
               accountEntryData.description = "Bank transfer. Payment reference " + bankTransferReceivedData.paymentReference;
               accountEntryData.currency = bankTransferReceivedData.paymentCurrency;
               accountEntryData.amount = bankTransferReceivedData.paymentAmount + discount;
               accountEntryData.fundedAmount = accountEntryData.amount;
               accountEntryData = this.createAccountEntry(conn, accountEntryData, accountEntrySourceData);
               if (discount > 0.0D) {
                  this.applyDiscountTier(conn, username, discountTier, accountEntryData);
               }

               if (discountTier != null) {
                  this.createDiscountTierAdjustmentAccountEntry(conn, username, accountEntryData.reference, discountTier.percentageDiscount, bankTransferReceivedData.paymentCurrency, discountTier.adjustmentAmount, accountEntrySourceData);
               }

               try {
                  this.sendRechargeCreditRewardProgramTrigger(accountEntryData.username, accountEntryData.fundedAmount, accountEntryData.currency, 0);
               } catch (Exception var34) {
                  log.warn("Unable to send recharge credit reward program trigger for user: " + accountEntryData.username);
               }
               break;
            case CORRECTION:
               accountEntryData.type = AccountEntryData.TypeEnum.BANK_TRANSFER_REVERSAL;
               accountEntryData.description = "Bank transfer correction. Payment reference " + bankTransferReceivedData.paymentReference;
               accountEntryData.currency = bankTransferReceivedData.paymentCurrency;
               accountEntryData.amount = -bankTransferReceivedData.paymentAmount - discount;
               accountEntryData.fundedAmount = accountEntryData.amount;
               accountEntryData = this.createAccountEntry(conn, accountEntryData, accountEntrySourceData);
               break;
            case REVERSAL:
               DiscountTierData reversalDiscountTierData = this.getApplicableDiscountTier(Enums.PaymentEnum.BANK_TRANSFER, username, bankTransferReceivedData.paymentAmount - bankTransferReceivedData.reversalAmount, currencyData, false);
               double reversalBonus = reversalDiscountTierData == null ? 0.0D : reversalDiscountTierData.discountAmount;
               accountEntryData.type = AccountEntryData.TypeEnum.BANK_TRANSFER_REVERSAL;
               accountEntryData.description = "Bank transfer reversal. Payment reference " + bankTransferReceivedData.paymentReference;
               accountEntryData.currency = bankTransferReceivedData.reversalCurrency;
               accountEntryData.amount = reversalBonus - discount - bankTransferReceivedData.reversalAmount;
               accountEntryData.fundedAmount = accountEntryData.amount;
               accountEntryData = this.createAccountEntry(conn, accountEntryData, accountEntrySourceData);
               break;
            case REVERSAL_CORRECTION:
               accountEntryData.type = AccountEntryData.TypeEnum.BANK_TRANSFER;
               accountEntryData.description = "Bank transfer reversal correction. Payment reference " + bankTransferReceivedData.paymentReference;
               accountEntryData.currency = bankTransferReceivedData.reversalCurrency;
               accountEntryData.amount = bankTransferReceivedData.reversalAmount;
               accountEntryData.fundedAmount = accountEntryData.amount;
               accountEntryData = this.createAccountEntry(conn, accountEntryData, accountEntrySourceData);
            }

            ps.close();
            ps = conn.prepareStatement("update banktransferreceived set status = ? where id = ?");
            ps.setInt(1, BankTransferReceivedData.StatusEnum.PROCESSED.value());
            ps.setInt(2, bankTransferReceivedData.id);
            if (ps.executeUpdate() != 1) {
               throw new SQLException("Failed to update bank transfer received. ID = " + bankTransferReceivedData.id);
            }

            ps.close();
            ps = conn.prepareStatement("update banktransferintent set status = ? where id = ?");
            ps.setInt(1, BankTransferIntentData.StatusEnum.MATCHED.value());
            ps.setInt(2, bankTransferReceivedData.bankTransferIntentID);
            ps.executeUpdate();
            if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.BANK_TRANSFER_CONFIRMATION, username)) {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.username = username;
               systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.BANK_TRANSFER_CONFIRMATION;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = mobilePhone;
               switch(bankTransferReceivedData.type) {
               case PAYMENT:
               case CORRECTION:
                  systemSMSData.messageText = SystemProperty.get("BankTransferConfirmationSMS");
                  systemSMSData.messageText = systemSMSData.messageText.replaceAll("%1", (new DecimalFormat("0.00 ")).format(bankTransferReceivedData.paymentAmount) + bankTransferReceivedData.paymentCurrency).replaceAll("%2", (new DecimalFormat("0.00 ")).format(Math.abs(accountEntryData.amount)) + accountEntryData.currency).replaceAll("%3", this.getAccountBalance(username).formatWithCode());
                  break;
               case REVERSAL:
               case REVERSAL_CORRECTION:
                  systemSMSData.messageText = SystemProperty.get("BankTransferReversalSMS");
                  systemSMSData.messageText = systemSMSData.messageText.replaceAll("%1", (new DecimalFormat("0.00 ")).format(bankTransferReceivedData.reversalAmount) + bankTransferReceivedData.reversalCurrency).replaceAll("%2", (new DecimalFormat("0.00 ")).format(Math.abs(accountEntryData.amount)) + accountEntryData.currency).replaceAll("%3", this.getAccountBalance(username).formatWithCode());
               }

               MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
            }
         }

         var40 = bankTransferReceivedData;
      } catch (SQLException var35) {
         throw new EJBException(var35.getMessage());
      } catch (CreateException var36) {
         throw new EJBException(var36.getMessage());
      } catch (NoSuchFieldException var37) {
         throw new EJBException(var37.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var31) {
            conn = null;
         }

      }

      return var40;
   }

   public DiscountTierData getApplicableDiscountTier(Enums.PaymentEnum paymentType, String username, double amount, CurrencyData currency, boolean useDisplayMin) {
      Vector<DiscountTierData> eligibleDiscountTiers = this.getEligibleDiscountTiers(paymentType, username, currency);
      if (eligibleDiscountTiers == null) {
         return null;
      } else {
         double creditPurchasedThisMonth = this.getCreditPurchasedThisMonth(username, currency.code);
         double lastPercentageDiscountReceivedThisMonth = 0.0D;
         Iterator i$ = eligibleDiscountTiers.iterator();

         while(i$.hasNext()) {
            DiscountTierData discountTier = (DiscountTierData)i$.next();
            if (discountTier.canBeApplied && discountTier.actualMin < creditPurchasedThisMonth) {
               lastPercentageDiscountReceivedThisMonth = discountTier.percentageDiscount;
               break;
            }
         }

         DiscountTierData applicableDiscountTier = null;
         Iterator i$ = eligibleDiscountTiers.iterator();

         while(i$.hasNext()) {
            DiscountTierData discountTier = (DiscountTierData)i$.next();
            if (discountTier.canBeApplied && amount / (1.0D - discountTier.percentageDiscount / 100.0D) + creditPurchasedThisMonth >= discountTier.actualMin) {
               applicableDiscountTier = discountTier;
               break;
            }
         }

         if (applicableDiscountTier == null) {
            return null;
         } else {
            applicableDiscountTier.discountAmount = amount / (1.0D - applicableDiscountTier.percentageDiscount / 100.0D) - amount;
            applicableDiscountTier.discountAmount = Math.rint(applicableDiscountTier.discountAmount * 100.0D) / 100.0D;
            applicableDiscountTier.adjustmentAmount = creditPurchasedThisMonth * ((applicableDiscountTier.percentageDiscount - lastPercentageDiscountReceivedThisMonth) / 100.0D);
            applicableDiscountTier.adjustmentAmount = Math.rint(applicableDiscountTier.adjustmentAmount * 100.0D) / 100.0D;
            return applicableDiscountTier;
         }
      }
   }

   private double getCreditPurchasedThisMonth(String username, String currencyCode) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      double var7;
      try {
         conn = this.dataSourceMaster.getConnection();
         String sql = "SELECT SUM(accountentry.Amount/accountentry.ExchangeRate) * currency.ExchangeRate FROM accountentry, currency  WHERE accountentry.Username = ? AND currency.Code = ? AND accountentry.DateCreated > DATE_FORMAT(now(), '%Y-%m-01') AND accountentry.Type IN (1,15,21,22,25,26)";
         ps = conn.prepareStatement(sql);
         ps.setString(1, username);
         ps.setString(2, currencyCode);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unable to obtain previous purchases");
         }

         var7 = rs.getDouble(1);
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

      return var7;
   }

   public Vector<DiscountTierData> getEligibleDiscountTiers(Enums.PaymentEnum paymentType, String username, CurrencyData currency) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      Vector discountTiers = new Vector();

      MISLocal misBean;
      try {
         misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
      } catch (CreateException var32) {
         throw new EJBException(var32.getMessage());
      }

      HashSet discountsAppliedThisMonth;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("SELECT type FROM user WHERE username=?");
         ps.setString(1, username);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("User not found");
         }

         UserData.TypeEnum userType = UserData.TypeEnum.fromValue(rs.getInt(1));
         if (userType == UserData.TypeEnum.MIG33_MERCHANT || userType == UserData.TypeEnum.MIG33_TOP_MERCHANT) {
            rs.close();
            ps.close();
            boolean receivedDiscountBefore = false;
            ps = conn.prepareStatement("(SELECT id FROM applieddiscount WHERE username=? LIMIT 1) UNION (SELECT id FROM appliedbonus WHERE username=? LIMIT 1)");
            ps.setString(1, username);
            ps.setString(2, username);
            rs = ps.executeQuery();
            if (rs.next()) {
               receivedDiscountBefore = true;
            }

            rs.close();
            ps.close();
            ps = conn.prepareStatement("SELECT DISTINCT DiscountTierID FROM applieddiscount WHERE username=? AND MONTH(DateCreated) = MONTH(now()) AND YEAR(DateCreated) = YEAR(now())");
            ps.setString(1, username);
            rs = ps.executeQuery();
            discountsAppliedThisMonth = new HashSet();

            while(rs.next()) {
               discountsAppliedThisMonth.add(rs.getInt(1));
            }

            rs.close();
            ps.close();
            String sql = "SELECT ID, Name, Type, ActualMin, DisplayMin, Max, Currency, PercentageDiscount, ApplyToCreditCard, ApplyToBankTransfer, ApplyToWesternUnion, ApplyToTT, ApplyToVoucher, Status FROM discounttier WHERE Status=1 ORDER BY PercentageDiscount DESC, ActualMin";
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();

            while(rs.next()) {
               DiscountTierData discountTier = new DiscountTierData();
               discountTier.id = rs.getInt("Id");
               discountTier.name = rs.getString("Name");
               discountTier.type = DiscountTierData.TypeEnum.fromValue(rs.getInt("Type"));
               discountTier.percentageDiscount = rs.getDouble("PercentageDiscount");
               discountTier.applyToCreditCard = rs.getBoolean("ApplyToCreditCard");
               discountTier.applyToTelegraphicTransfer = rs.getBoolean("ApplyToTT");
               discountTier.applyToBankTransfer = rs.getBoolean("ApplyToBankTransfer");
               discountTier.applyToWesternUnion = rs.getBoolean("ApplyToWesternUnion");
               discountTier.applyToVoucher = rs.getBoolean("ApplyToVoucher");
               discountTier.status = DiscountTierData.StatusEnum.fromValue(rs.getInt("Status"));
               discountTier.dbActualMin = rs.getDouble("ActualMin");
               discountTier.dbDisplayMin = rs.getDouble("DisplayMin");
               discountTier.dbMax = rs.getDouble("Max");
               discountTier.dbCurrency = rs.getString("Currency");
               if (paymentType == null || paymentType == Enums.PaymentEnum.ALL || (paymentType != Enums.PaymentEnum.CREDIT_CARD || discountTier.applyToCreditCard) && (paymentType != Enums.PaymentEnum.TELEGRAPHIC_TRANSFER || discountTier.applyToTelegraphicTransfer) && (paymentType != Enums.PaymentEnum.BANK_TRANSFER || discountTier.applyToBankTransfer) && (paymentType != Enums.PaymentEnum.WESTERN_UNION || discountTier.applyToWesternUnion) && (paymentType != Enums.PaymentEnum.VOUCHER || discountTier.applyToVoucher)) {
                  CurrencyData tierCurrency = misBean.getCurrency(rs.getString("Currency"));
                  discountTier.actualMin = currency.convertFrom(rs.getDouble("ActualMin"), tierCurrency);
                  discountTier.displayMin = currency.convertFrom(rs.getDouble("DisplayMin"), tierCurrency);
                  discountTier.max = currency.convertFrom(rs.getDouble("Max"), tierCurrency);
                  discountTier.currency = currency.code;
                  if (discountTier.type == DiscountTierData.TypeEnum.FIRST_TIME_ONLY && receivedDiscountBefore) {
                     discountTier.canBeApplied = false;
                  } else {
                     discountTier.canBeApplied = true;
                  }

                  if (discountsAppliedThisMonth.contains(discountTier.id)) {
                     discountTier.appliedThisMonth = true;
                  } else {
                     discountTier.appliedThisMonth = false;
                  }

                  discountTiers.add(discountTier);
               }
            }

            return discountTiers.size() > 0 ? discountTiers : null;
         }

         discountsAppliedThisMonth = null;
      } catch (SQLException var33) {
         throw new EJBException(var33.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var29) {
            conn = null;
         }

      }

      return discountsAppliedThisMonth;
   }

   public double discountTierRounding(double amount) {
      if (amount < 0.5D) {
         return Math.rint(amount * 100.0D) / 100.0D;
      } else if (amount < 5.0D) {
         return this.roundDown(amount, 0.1D);
      } else if (amount < 10.0D) {
         return this.roundDown(amount, 0.5D);
      } else if (amount < 25.0D) {
         return this.roundDown(amount, 1.0D);
      } else if (amount < 100.0D) {
         return this.roundDown(amount, 5.0D);
      } else if (amount < 10000.0D) {
         return this.roundDown(amount, 10.0D);
      } else {
         return amount < 100000.0D ? this.roundDown(amount, 100.0D) : this.roundDown(amount, 1000.0D);
      }
   }

   private double roundDown(double val, double unit) {
      return (double)Math.round(Math.floor(val / unit) * unit * 100.0D) / 100.0D;
   }

   public double[] getCreditCardPaymentAmounts(String username, boolean isMerchant, String currency) throws EJBException {
      Connection conn = null;

      double[] var25;
      try {
         conn = this.dataSourceMaster.getConnection();
         double[] amounts;
         if (isMerchant && this.isOnCreditCardWhiteList(conn, username, (String)null)) {
            amounts = SystemProperty.getDoubleArray("CreditCardTrustedPaymentAmounts");
         } else {
            amounts = SystemProperty.getDoubleArray("CreditCardPaymentAmounts");
         }

         String creditCardPaymentCurrency = SystemProperty.get("CreditCardPaymentCurrency");
         if (!creditCardPaymentCurrency.equals(currency)) {
            MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
            CurrencyData fromCurrency = misBean.getCurrency(creditCardPaymentCurrency);
            CurrencyData toCurrency = misBean.getCurrency(currency);

            for(int i = 0; i < amounts.length; ++i) {
               amounts[i] = this.discountTierRounding(fromCurrency.convertTo(amounts[i], toCurrency));
            }
         }

         var25 = amounts;
      } catch (CreateException var21) {
         throw new EJBException(var21.getMessage());
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
      } catch (NoSuchFieldException var23) {
         throw new EJBException(var23.getMessage());
      } finally {
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

   public ServiceData getService(int serviceID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      ServiceData var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from service where id = ?");
         ps.setInt(1, serviceID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            var5 = null;
            return var5;
         }

         var5 = new ServiceData(rs);
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

      return var5;
   }

   public SubscriptionData getSubscription(int subscriptionID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      SubscriptionData var5;
      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from subscription where id = ?");
         ps.setInt(1, subscriptionID);
         rs = ps.executeQuery();
         if (rs.next()) {
            var5 = new SubscriptionData(rs);
            return var5;
         }

         var5 = null;
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
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

      return var5;
   }

   public List<SubscriptionData> getSubscriptions(String username, Integer serviceID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (serviceID == null) {
            ps = conn.prepareStatement("select * from subscription where username = ?");
            ps.setString(1, username);
         } else {
            ps = conn.prepareStatement("select * from subscription where username = ? and serviceid = ?");
            ps.setString(1, username);
            ps.setInt(2, serviceID);
         }

         List<SubscriptionData> subscriptions = new LinkedList();
         rs = ps.executeQuery();

         while(rs.next()) {
            subscriptions.add(new SubscriptionData(rs));
         }

         LinkedList var7 = subscriptions;
         return var7;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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
   }

   public List<SubscriptionData> getExpiringSubscriptions(int fromID, int limit) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from subscription where id >= ? and status = ? and timestampdiff(hour, ?, expirydate) < 48 limit ?");
         ps.setInt(1, fromID);
         ps.setInt(2, SubscriptionData.StatusEnum.ACTIVE.value());
         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         ps.setInt(4, limit);
         List<SubscriptionData> subscriptions = new LinkedList();
         rs = ps.executeQuery();

         while(rs.next()) {
            subscriptions.add(new SubscriptionData(rs));
         }

         LinkedList var7 = subscriptions;
         return var7;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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
   }

   public List<SubscriptionData> getExpiredSubscriptions(int fromID, int limit) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from subscription where id >= ? and status = ? and expirydate < ? limit ?");
         ps.setInt(1, fromID);
         ps.setInt(2, SubscriptionData.StatusEnum.ACTIVE.value());
         ps.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
         ps.setInt(4, limit);
         List<SubscriptionData> subscriptions = new LinkedList();
         rs = ps.executeQuery();

         while(rs.next()) {
            subscriptions.add(new SubscriptionData(rs));
         }

         LinkedList var7 = subscriptions;
         return var7;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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
   }

   public List<SubscriptionData> getPendingSubscriptions(int fromID, int limit) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from subscription where id >= ? and status = ? limit ?");
         ps.setInt(1, fromID);
         ps.setInt(2, SubscriptionData.StatusEnum.PENDING.value());
         ps.setInt(3, limit);
         List<SubscriptionData> subscriptions = new LinkedList();
         rs = ps.executeQuery();

         while(rs.next()) {
            subscriptions.add(new SubscriptionData(rs));
         }

         LinkedList var7 = subscriptions;
         return var7;
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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
   }

   public SubscriptionData subscribeService(String username, int serviceID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      SubscriptionData var39;
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(username, false, false);
         if (userData == null || userData.status != UserData.StatusEnum.ACTIVE) {
            throw new EJBException("Invalid user " + username);
         }

         ServiceData serviceData = this.getService(serviceID);
         if (serviceData == null) {
            throw new EJBException("Invalid service ID " + serviceID);
         }

         if (serviceData.billingMethod == ServiceData.BillingMethodEnum.USER_ACCOUNT) {
            double costInBaseCurrency = this.convertCurrency(serviceData.cost, serviceData.costCurrency, CurrencyData.baseCurrency);
            if (this.getAccountBalance(username).getBaseBalance() < costInBaseCurrency) {
               throw new EJBException("You do not have enough credit to susbcribe the service");
            }
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("select * from subscription where username = ? and serviceid = ? and status in (?,?) limit 1");
         ps.setString(1, username);
         ps.setInt(2, serviceID);
         ps.setInt(3, SubscriptionData.StatusEnum.PENDING.value());
         ps.setInt(4, SubscriptionData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         if (rs.next()) {
            throw new EJBException("You are already subscribed");
         }

         rs.close();
         ps.close();
         boolean isFreeTrial = false;
         if (serviceData.freeTrialDays > 0) {
            ps = conn.prepareStatement("select * from subscription where username = ? and serviceid = ? and status in (?,?,?) limit 1 union all select * from subscription where mobilephone = ? and serviceid = ? and status in (?,?,?) limit 1");
            ps.setString(1, username);
            ps.setInt(2, serviceID);
            ps.setInt(3, SubscriptionData.StatusEnum.ACTIVE.value());
            ps.setInt(4, SubscriptionData.StatusEnum.EXPIRED.value());
            ps.setInt(5, SubscriptionData.StatusEnum.CANCELLED.value());
            ps.setString(6, userData.mobilePhone);
            ps.setInt(7, serviceID);
            ps.setInt(8, SubscriptionData.StatusEnum.ACTIVE.value());
            ps.setInt(9, SubscriptionData.StatusEnum.EXPIRED.value());
            ps.setInt(10, SubscriptionData.StatusEnum.CANCELLED.value());
            rs = ps.executeQuery();
            isFreeTrial = !rs.next();
            rs.close();
            ps.close();
         }

         SubscriptionData subscriptionData = new SubscriptionData();
         subscriptionData.username = username;
         subscriptionData.mobilePhone = userData.mobilePhone;
         subscriptionData.serviceID = serviceID;
         subscriptionData.dateCreated = new Date();
         subscriptionData.ipAddress = accountEntrySourceData.ipAddress;
         subscriptionData.expiryReminderSent = false;
         subscriptionData.status = SubscriptionData.StatusEnum.PENDING;
         if (isFreeTrial) {
            subscriptionData.type = SubscriptionData.TypeEnum.FREE_TRIAL;
            subscriptionData.expiryDate = DateTimeUtils.daysFromNow(serviceData.freeTrialDays);
            subscriptionData.billingAttempts = 0;
         } else {
            subscriptionData.type = SubscriptionData.TypeEnum.PAID;
            subscriptionData.expiryDate = DateTimeUtils.daysFromNow(serviceData.durationDays);
            subscriptionData.billingAttempts = 1;
            subscriptionData.lastBillingAttempt = subscriptionData.dateCreated;
         }

         ps = conn.prepareStatement("insert into subscription (username, serviceid, datecreated, type, ipaddress, mobilephone, expirydate, expiryremindersent, billingattempts, lastbillingattempt, status) values (?,?,?,?,?,?,?,?,?,?,?)", 1);
         ps.setString(1, subscriptionData.username);
         ps.setInt(2, subscriptionData.serviceID);
         ps.setTimestamp(3, new Timestamp(subscriptionData.dateCreated.getTime()));
         ps.setInt(4, subscriptionData.type.value());
         ps.setString(5, subscriptionData.ipAddress);
         ps.setString(6, subscriptionData.mobilePhone);
         ps.setTimestamp(7, new Timestamp(subscriptionData.expiryDate.getTime()));
         ps.setInt(8, subscriptionData.expiryReminderSent ? 1 : 0);
         ps.setInt(9, subscriptionData.billingAttempts);
         ps.setTimestamp(10, subscriptionData.lastBillingAttempt == null ? null : new Timestamp(subscriptionData.lastBillingAttempt.getTime()));
         ps.setInt(11, subscriptionData.status.value());
         ps.executeUpdate();
         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Unable to add subscription to database");
         }

         subscriptionData.id = rs.getInt(1);
         if (isFreeTrial) {
            this.updateSubscriptionBillingStatus(username, subscriptionData.id, true, accountEntrySourceData);
            var39 = subscriptionData;
            return var39;
         }

         switch(serviceData.billingMethod) {
         case PREMIUM_SMS:
            if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.MIG33_PREMIUM_SMS, username)) {
               SystemSMSData systemSMSData = new SystemSMSData();
               systemSMSData.username = username;
               systemSMSData.type = SystemSMSData.TypeEnum.PREMIUM;
               systemSMSData.subType = SystemSMSData.SubTypeEnum.MIG33_PREMIUM_SMS;
               systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
               systemSMSData.destination = userData.mobilePhone;
               systemSMSData.messageText = serviceData.billingConfirmationSMS;
               MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
               messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
            }
            break;
         case USER_ACCOUNT:
         default:
            AccountEntryData accountEntryData = new AccountEntryData();
            accountEntryData.username = username;
            accountEntryData.type = AccountEntryData.TypeEnum.SUBSCRIPTION;
            accountEntryData.reference = subscriptionData.id.toString();
            accountEntryData.description = "Subscription to " + serviceData.name;
            accountEntryData.currency = serviceData.costCurrency;
            accountEntryData.amount = -serviceData.cost;
            accountEntryData.tax = 0.0D;
            this.createAccountEntry(conn, accountEntryData, accountEntrySourceData);
            this.updateSubscriptionBillingStatus(username, subscriptionData.id, true, accountEntrySourceData);
         }

         var39 = subscriptionData;
      } catch (CreateException var33) {
         throw new EJBException(var33.getMessage());
      } catch (SQLException var34) {
         throw new EJBException(var34.getMessage());
      } catch (NoSuchFieldException var35) {
         throw new EJBException(var35.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var32) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var31) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var30) {
            conn = null;
         }

      }

      return var39;
   }

   public void updateSubscriptionBillingStatus(String username, int subscriptionID, boolean paid, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         if (paid) {
            ps = conn.prepareStatement("select service.name, service.awardedcredit, service.awardedcreditcurrency from service, subscription where service.id = subscription.serviceid and subscription.id = ? and subscription.status = ? and subscription.username = ?");
            ps.setInt(1, subscriptionID);
            ps.setInt(2, SubscriptionData.StatusEnum.PENDING.value());
            ps.setString(3, username);
            rs = ps.executeQuery();
            if (!rs.next()) {
               throw new EJBException(username + " does not have a pending subscription " + subscriptionID);
            }

            String serviceName = rs.getString("name");
            double awardedCredit = rs.getDouble("awardedCredit");
            String awardedCreditCurrency = rs.getString("awardedCreditCurrency");
            rs.close();
            ps.close();
            ps = conn.prepareStatement("update subscription set status = ? where username = ? and id = ? and status = ?");
            ps.setInt(1, SubscriptionData.StatusEnum.ACTIVE.value());
            ps.setString(2, username);
            ps.setInt(3, subscriptionID);
            ps.setInt(4, SubscriptionData.StatusEnum.PENDING.value());
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to change subscription " + subscriptionID + " from pending to active state");
            }

            ps.close();
            if (awardedCredit > 0.0D && awardedCreditCurrency != null) {
               AccountEntryData accountEntryData = new AccountEntryData();
               accountEntryData.username = username;
               accountEntryData.type = AccountEntryData.TypeEnum.SUBSCRIPTION_CREDIT;
               accountEntryData.reference = String.valueOf(subscriptionID);
               accountEntryData.description = "Bonue credit for subscription to " + serviceName;
               accountEntryData.currency = awardedCreditCurrency;
               accountEntryData.amount = awardedCredit;
               accountEntryData.tax = 0.0D;
               this.createAccountEntry(conn, accountEntryData, accountEntrySourceData);
            }
         }
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var24) {
            conn = null;
         }

      }

   }

   public void cancelSubscription(String username, int subscriptionID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update subscription set status = ?, cancellationdate = ? where username = ? and id = ? and status = ?");
         ps.setInt(1, SubscriptionData.StatusEnum.CANCELLED.value());
         ps.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
         ps.setString(3, username);
         ps.setInt(4, subscriptionID);
         ps.setInt(5, SubscriptionData.StatusEnum.ACTIVE.value());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to cancel subscription " + subscriptionID);
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void expireSubscription(String username, int subscriptionID) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update subscription set status = ? where username = ? and id = ? and status = ? and expirydate < ?");
         ps.setInt(1, SubscriptionData.StatusEnum.EXPIRED.value());
         ps.setString(2, username);
         ps.setInt(3, subscriptionID);
         ps.setInt(4, SubscriptionData.StatusEnum.ACTIVE.value());
         ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to expire subscription " + subscriptionID);
         }
      } catch (SQLException var17) {
         throw new EJBException(var17.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var16) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var15) {
            conn = null;
         }

      }

   }

   public void sendSubscriptionExpiryReminderSMS(String username, int subscriptionID, String text, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(username, false, false);
         if (userData == null || userData.status != UserData.StatusEnum.ACTIVE) {
            throw new EJBException("Invalid user " + username);
         }

         conn = this.dataSourceMaster.getConnection();
         ps = conn.prepareStatement("update subscription set expiryremindersent = 1 where id = ? and username = ?");
         ps.setInt(1, subscriptionID);
         ps.setString(2, username);
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to update reminder sending status for subscription " + subscriptionID);
         }

         ps.close();
         conn.close();
         if (SMSControl.isSendEnabledForSubtype(SystemSMSData.SubTypeEnum.SUBSCRIPTION_EXPIRY_NOTIFICATION, username)) {
            SystemSMSData systemSMSData = new SystemSMSData();
            systemSMSData.username = username;
            systemSMSData.type = SystemSMSData.TypeEnum.STANDARD;
            systemSMSData.subType = SystemSMSData.SubTypeEnum.SUBSCRIPTION_EXPIRY_NOTIFICATION;
            systemSMSData.source = SystemProperty.get("TwoWaySMSNumber");
            systemSMSData.destination = userData.mobilePhone;
            systemSMSData.messageText = text;
            MessageLocal messageBean = (MessageLocal)EJBHomeCache.getLocalObject("MessageLocal", MessageLocalHome.class);
            messageBean.sendSystemSMS(systemSMSData, accountEntrySourceData);
         }
      } catch (CreateException var24) {
         throw new EJBException(var24);
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } catch (NoSuchFieldException var26) {
         throw new EJBException(var26.getMessage());
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var22) {
            conn = null;
         }

      }

   }

   public AccountEntryData chargeUserForGame(String username, String reference, AccountEntryData.TypeEnum accountEntryType, String description, double billingAmount, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = accountEntryType;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = CurrencyData.baseCurrency;
      accountEntry.amount = -billingAmount;
      accountEntry.tax = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public boolean userCanAffordCost(String username, double cost, String currency, Connection masterConn) throws EJBException {
      ConnectionHolder ch = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      boolean var10;
      try {
         ch = new ConnectionHolder(this.dataSourceMaster, masterConn);
         String sql = "SELECT user.balance/usercurrency.exchangerate >= ?/costcurrency.exchangerate FROM user, currency usercurrency, currency costcurrency WHERE user.username=? AND user.currency=usercurrency.code AND costcurrency.code=?";
         ps = ch.getConnection().prepareStatement(sql);
         ps.setDouble(1, cost);
         ps.setString(2, username);
         ps.setString(3, currency);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("User or currency not found");
         }

         var10 = rs.getBoolean(1);
      } catch (SQLException var25) {
         throw new EJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (ch != null) {
               ch.close();
            }
         } catch (SQLException var22) {
            ch = null;
         }

      }

      return var10;
   }

   public UserPotEligibilityData userCanAffordToEnterPot(String username, double cost, String currency, String baseCurrency) throws EJBException {
      double nonTransferableAmount = 0.0D;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.ENABLE_MIN_BALANCE_CALC_ON_POT_GAMES)) {
         Date now = new Date(System.currentTimeMillis());
         int numDaysToLookBack = SystemProperty.getInt((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.MIN_BALANCE_MAX_LOOK_BACK_DAYS);
         nonTransferableAmount = this.getNonTransferableFundsAtSystemBaseCurrency(username, now, numDaysToLookBack, (Connection)null, false);
      }

      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      UserPotEligibilityData var13;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         String sql = " SELECT  user.type as type,  user.balance/usercurrency.exchangerate >= (?/costcurrency.exchangerate    + (CASE WHEN ? <= country.minbalanceaftertransfer THEN country.minbalanceaftertransfer     ELSE ? END   ) ) as eligible,  ?/costcurrency.exchangerate*basecurrency.exchangerate as costInBaseCurrency  FROM user, currency usercurrency, currency costcurrency, currency basecurrency, country  WHERE user.username=?  AND user.countryid = country.id  AND user.currency=usercurrency.code  AND costcurrency.code=?  AND basecurrency.code=?";
         ps = connMaster.prepareStatement(sql);
         ps.setDouble(1, cost);
         ps.setDouble(2, nonTransferableAmount);
         ps.setDouble(3, nonTransferableAmount);
         ps.setDouble(4, cost);
         ps.setString(5, username);
         ps.setString(6, currency);
         ps.setString(7, baseCurrency);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("User or currency not found");
         }

         UserPotEligibilityData data = UserPotEligibilityData.fromResultSet(rs);
         var13 = data;
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var25) {
         }

      }

      return var13;
   }

   public PotData createPot(int botID, String botInstanceID) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      PotData var7;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         PotData potData = new PotData(botID, botInstanceID, SystemProperty.getDouble("RakePercent"));
         ps = connMaster.prepareStatement("insert into pot (botid, botinstanceid, datecreated, rakepercent, status) values (?,?,?,?,?)", 1);
         ps.setInt(1, potData.getBotID());
         ps.setString(2, potData.getBotInstanceID());
         ps.setTimestamp(3, new Timestamp(potData.getDateCreated().getTime()));
         ps.setDouble(4, potData.getRakePercent());
         ps.setInt(5, potData.getStatus().value());
         if (ps.executeUpdate() != 1) {
            throw new EJBException("Unable to create pot");
         }

         rs = ps.getGeneratedKeys();
         if (!rs.next()) {
            throw new EJBException("Unable to retrieve pot ID");
         }

         potData.setId(rs.getInt(1));
         var7 = potData;
      } catch (SQLException var23) {
         throw new EJBException(var23.getMessage());
      } catch (NoSuchFieldException var24) {
         throw new EJBException(var24.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var22) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var21) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var20) {
         }

      }

      return var7;
   }

   public PotStakeData enterUserIntoPot(String gameName, int botID, int potID, String username, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      PotStakeData var38;
      try {
         if (!this.userCanAffordToEnterPot(username, amount, currency, currency).isEligible()) {
            throw new EJBException("Insufficient credit");
         }

         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         int userID = userBean.getUserID(username, (Connection)null);
         connMaster = this.dataSourceMaster.getConnection();
         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = username;
         accountEntry.type = AccountEntryData.TypeEnum.POT_ENTRY;
         accountEntry.reference = Integer.toString(potID);
         accountEntry.description = "Entry into a game of " + gameName;
         accountEntry.currency = currency;
         accountEntry.amount = -amount;
         accountEntry.tax = 0.0D;
         accountEntry = this.createAccountEntry(connMaster, accountEntry, accountEntrySourceData);
         PotStakeData stake = null;
         ps = connMaster.prepareStatement("select * from potstake where potid=? and userid=?");
         ps.setInt(1, potID);
         ps.setInt(2, userID);
         rs = ps.executeQuery();
         if (rs.next()) {
            stake = PotStakeData.fromResultSet(rs);
            if (!accountEntry.currency.equalsIgnoreCase(stake.getCurrency())) {
               throw new EJBException("A new entry must be in the same currency as any existing entry");
            }

            ps = connMaster.prepareStatement("update potstake set amount=amount+?, fundedamount=fundedamount+?, eligible=1 where id=?");
            ps.setDouble(1, -accountEntry.amount);
            ps.setDouble(2, -accountEntry.fundedAmount);
            ps.setInt(3, stake.getId());
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to increase stake");
            }

            stake.addToAmount(-accountEntry.amount, -accountEntry.fundedAmount);
            stake.setEligible(true);
         } else {
            rs.close();
            ps.close();
            stake = new PotStakeData(potID, userID, -accountEntry.amount, -accountEntry.fundedAmount, accountEntry.currency, accountEntry.exchangeRate);
            ps = connMaster.prepareStatement("insert into potstake (potid, userid, datecreated, amount, fundedamount, currency, exchangerate, eligible) values (?,?,?,?,?,?,?,?)", 1);
            ps.setInt(1, stake.getPotID());
            ps.setInt(2, stake.getUserID());
            ps.setTimestamp(3, new Timestamp(stake.getDateCreated().getTime()));
            ps.setDouble(4, stake.getAmount());
            ps.setDouble(5, stake.getFundedAmount());
            ps.setString(6, stake.getCurrency());
            ps.setDouble(7, stake.getExchangeRate());
            ps.setBoolean(8, stake.isEligible());
            if (ps.executeUpdate() != 1) {
               throw new EJBException("Unable to create stake");
            }

            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new EJBException("Unable to retrieve stake ID");
            }

            stake.setId(rs.getInt(1));
         }

         if (!SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.SEND_BOT_SPENDING_TRIGGER_AFTER_PAYOUT_TRANSACTION)) {
            try {
               UserData playerUserData = userBean.loadUser(username, false, false);
               BotGameSpendingTrigger trigger = new BotGameSpendingTrigger(playerUserData);
               trigger.amountDelta = Math.abs(accountEntry.amount);
               trigger.quantityDelta = 1;
               trigger.currency = accountEntry.currency;
               trigger.botID = botID;
               RewardCentre.getInstance().sendTrigger(trigger);
            } catch (Exception var34) {
               log.error("Unable to send BotGameSpendingTrigger for user [" + username + "] for winning botID[" + botID + "] [" + accountEntry.amount + "][" + accountEntry.currency + "] :" + var34.getMessage(), var34);
            }
         }

         var38 = stake;
      } catch (SQLException var35) {
         log.error("SQLException in enterUserIntoPot(" + gameName + ", " + potID + ", " + username + ", " + amount + ", " + currency + ")", var35);
         throw new EJBException("Unable to process game entry");
      } catch (CreateException var36) {
         log.error("CreateException in enterUserIntoPot(" + gameName + ", " + potID + ", " + username + ", " + amount + ", " + currency + ")", var36);
         throw new EJBException("Unable to process game entry");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var33) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var32) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var31) {
         }

      }

      return var38;
   }

   public void removeUserFromPot(int potStakeID) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("update potstake set eligible=0 where id=?");
         ps.setInt(1, potStakeID);
         ps.executeUpdate();
      } catch (Exception var16) {
         log.error("Exception in removeUserFromPot(" + potStakeID + ")", var16);
         throw new EJBException("Unable to mark user's stake as ineligible");
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var15) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var14) {
         }

      }

   }

   /** @deprecated */
   public double payoutPot(String gameName, int potID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      PayoutData payoutData = this.performPayout(gameName, potID, accountEntrySourceData);
      return payoutData.getTotalPayoutPerUser();
   }

   public PayoutData performPayout(String gameName, int potID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PayoutData potPayoutData = new PayoutData();

      PayoutData var54;
      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select * from pot where id=?");
         ps.setInt(1, potID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Game winnings cannot be paid out (record not found)");
         }

         PotData potData = PotData.fromResultSet(rs);
         rs.close();
         ps.close();
         if (potData.getStatus() != PotData.StatusEnum.ACTIVE) {
            throw new EJBException("Game winnings cannot be paid out (game is not in progress)");
         }

         Set<PotStakeData> stakes = new HashSet();
         ps = connMaster.prepareStatement("select potstake.*, userid.username from potstake, userid where potstake.potid=? and potstake.userid=userid.id");
         ps.setInt(1, potID);
         rs = ps.executeQuery();

         while(rs.next()) {
            stakes.add(PotStakeData.fromResultSetWithUserName(rs));
         }

         rs.close();
         ps.close();
         double mig33Rake = 0.0D;
         double mig33RakeFunded = 0.0D;
         double payoutPerUserTotal = 0.0D;
         double payoutPerUserFunded = 0.0D;
         if (stakes.size() > 0) {
            double potTotal = 0.0D;
            double potFunded = 0.0D;
            HashMap<String, Integer> winnerUsernameUserIDs = new HashMap();

            PotStakeData stake;
            double totalPayoutFunded;
            for(Iterator i$ = stakes.iterator(); i$.hasNext(); potPayoutData.add(new GameSpenderData(stake.getUserID(), Math.abs(stake.getAmount()), Math.abs(stake.getFundedAmount()), stake.getCurrency()))) {
               stake = (PotStakeData)i$.next();
               potTotal += stake.getAmountInBaseCurrency();
               potFunded += stake.getFundedAmountInBaseCurrency();
               totalPayoutFunded = stake.getAmountInBaseCurrency() * potData.getRakePercent() / 100.0D;
               mig33Rake += totalPayoutFunded;
               double stakeUnfunded = stake.getAmountInBaseCurrency() - stake.getFundedAmountInBaseCurrency();
               if (stakeUnfunded < totalPayoutFunded) {
                  mig33RakeFunded += totalPayoutFunded - stakeUnfunded;
               }

               if (stake.isEligible()) {
                  winnerUsernameUserIDs.put(stake.getUsername(), stake.getUserID());
               }
            }

            if (winnerUsernameUserIDs.size() == 0) {
               mig33Rake = potTotal;
               mig33RakeFunded = potFunded;
            } else {
               payoutPerUserTotal = (potTotal - mig33Rake) / (double)winnerUsernameUserIDs.size();
               payoutPerUserFunded = (potFunded - mig33RakeFunded) / (double)winnerUsernameUserIDs.size();
               double totalPayout = 0.0D;
               totalPayoutFunded = 0.0D;
               UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
               boolean sendBotGameWonTriggerAfterPayoutTransaction = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.SEND_BOT_GAME_WON_TRIGGER_AFTER_PAYOUT_TRANSACTION);

               Integer winnerUserInteger;
               AccountEntryData accountEntry;
               for(Iterator i$ = winnerUsernameUserIDs.entrySet().iterator(); i$.hasNext(); potPayoutData.add(new GameWinnerData(winnerUserInteger, Math.abs(accountEntry.amount), Math.abs(accountEntry.fundedAmount), accountEntry.currency))) {
                  Entry<String, Integer> winnerEntry = (Entry)i$.next();
                  String winnerUsername = (String)winnerEntry.getKey();
                  winnerUserInteger = (Integer)winnerEntry.getValue();
                  accountEntry = new AccountEntryData();
                  accountEntry.username = winnerUsername;
                  accountEntry.type = AccountEntryData.TypeEnum.POT_PAYOUT;
                  accountEntry.reference = Integer.toString(potID);
                  accountEntry.description = "Payout from a game of " + gameName;
                  accountEntry.currency = CurrencyData.baseCurrency;
                  accountEntry.amount = payoutPerUserTotal;
                  accountEntry.fundedAmount = payoutPerUserFunded;
                  accountEntry.tax = 0.0D;
                  accountEntry = this.createAccountEntry(connMaster, accountEntry, accountEntrySourceData);
                  totalPayout += accountEntry.amount / accountEntry.exchangeRate;
                  totalPayoutFunded += accountEntry.fundedAmount / accountEntry.exchangeRate;
                  if (!sendBotGameWonTriggerAfterPayoutTransaction) {
                     try {
                        UserData winnerUserData = userBean.loadUser(winnerUsername, false, false);
                        BotGameWonTrigger botGameWonTrigger = new BotGameWonTrigger(winnerUserData);
                        botGameWonTrigger.amountDelta = accountEntry.amount;
                        botGameWonTrigger.quantityDelta = 1;
                        botGameWonTrigger.currency = accountEntry.currency;
                        botGameWonTrigger.botID = potData.getBotID();
                        RewardCentre.getInstance().sendTrigger(botGameWonTrigger);
                     } catch (Exception var51) {
                        log.error("Unable to send BotGameWonTrigger for user [" + winnerUsername + "] for winning botID[" + potData.getBotID() + "] [" + accountEntry.amount + "][" + accountEntry.currency + "] :" + var51.getMessage(), var51);
                     }
                  }
               }

               mig33Rake = potTotal - totalPayout;
               mig33RakeFunded = potFunded - totalPayoutFunded;
            }
         }

         ps = connMaster.prepareStatement("update pot set datepaidout=now(), rakeamount=?, rakefundedamount=?, status=? where id=?");
         ps.setDouble(1, mig33Rake);
         ps.setDouble(2, mig33RakeFunded);
         ps.setInt(3, PotData.StatusEnum.PAID_OUT.value());
         ps.setInt(4, potID);
         ps.executeUpdate();
         potPayoutData.setTotalPayoutPerUser(payoutPerUserTotal);
         potPayoutData.setBotId(potData.getBotID());
         var54 = potPayoutData;
      } catch (Throwable var52) {
         log.error("Exception in payoutPot(" + potID + ")", var52);
         throw new EJBException("Unable to pay out game winnings");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var50) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var49) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var48) {
         }

      }

      return var54;
   }

   public double payoutPotAndNotify(String gameName, int potID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      try {
         AccountLocal accountEJB = (AccountLocal)EJBHomeCache.getLocalObject("AccountLocal", AccountLocalHome.class);
         PayoutData potPayoutData = accountEJB.performPayout(gameName, potID, accountEntrySourceData);
         boolean sendBotSpendingTrigger = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.SEND_BOT_SPENDING_TRIGGER_AFTER_PAYOUT_TRANSACTION);
         boolean sendBotGameWonTrigger = SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.BOT.SEND_BOT_GAME_WON_TRIGGER_AFTER_PAYOUT_TRANSACTION);
         this.sendRewardProgramTriggers(sendBotSpendingTrigger, sendBotGameWonTrigger, potPayoutData);
         return potPayoutData.getTotalPayoutPerUser();
      } catch (EJBException var8) {
         throw var8;
      } catch (CreateException var9) {
         log.error("Unable to create EJB exception. Failed to payoutPot (" + potID + ") Ex:" + var9, var9);
         throw new EJBException("Failed to pay out game winnings", var9);
      }
   }

   private void sendRewardProgramTriggers(boolean sendBotSpendingTrigger, boolean sendBotGameWonTrigger, PayoutData potPayoutData) {
      if (log.isDebugEnabled()) {
         log.debug(String.format("sendRewardProgramTriggers(sendBotSpendingTrigger:[%s],sendBotGameWonTrigger:[%s],potPayoutData:[%s])", sendBotSpendingTrigger, sendBotGameWonTrigger, potPayoutData));
      }

      if (sendBotSpendingTrigger || sendBotGameWonTrigger) {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            Map<Integer, UserData> userDataCache = new HashMap();
            int botID = potPayoutData.getBotId();
            Iterator i$;
            if (sendBotSpendingTrigger) {
               i$ = potPayoutData.getGameSpenderData().iterator();

               while(i$.hasNext()) {
                  GameSpenderData spendingData = (GameSpenderData)i$.next();

                  try {
                     this.sendRewardProgramTriggers(botID, userDataCache, userBean, (GameSpenderData)spendingData);
                  } catch (Throwable var11) {
                     log.error("Unable to send reward program trigger for game spendings [" + spendingData + "]." + "Exception:" + var11, var11);
                  }
               }
            }

            if (sendBotGameWonTrigger) {
               i$ = potPayoutData.getGameWinnerDataList().iterator();

               while(i$.hasNext()) {
                  GameWinnerData winningData = (GameWinnerData)i$.next();

                  try {
                     this.sendRewardProgramTriggers(botID, userDataCache, userBean, (GameWinnerData)winningData);
                  } catch (Throwable var10) {
                     log.error("Unable to send reward program trigger for game winnings [" + winningData + "]." + "Exception:" + var10, var10);
                  }
               }
            }
         } catch (Throwable var12) {
            log.error("Unable to send reward program trigger for payoutData [" + potPayoutData + "]." + "Exception:" + var12, var12);
         }

      }
   }

   private UserData getUserData(Map<Integer, UserData> userDataShortCache, UserLocal userBean, int userId) {
      UserData cachedUserData = (UserData)userDataShortCache.get(userId);
      if (cachedUserData != null) {
         return cachedUserData;
      } else {
         UserData loadedUserData = userBean.loadUserFromID(userId);
         userDataShortCache.put(userId, loadedUserData);
         return loadedUserData;
      }
   }

   private void sendRewardProgramTriggers(int botId, Map<Integer, UserData> userDataShortCache, UserLocal userBean, GameSpenderData gameSpendingData) throws Exception {
      UserData playerUserData = this.getUserData(userDataShortCache, userBean, gameSpendingData.getSpenderUserid());
      BotGameSpendingTrigger trigger = new BotGameSpendingTrigger(playerUserData);
      trigger.amountDelta = gameSpendingData.getSpendingAmount();
      trigger.quantityDelta = 1;
      trigger.currency = gameSpendingData.getCurrency();
      trigger.botID = botId;
      RewardCentre.getInstance().sendTrigger(trigger);
      if (log.isDebugEnabled()) {
         log.info("Sent trigger [" + trigger + "] for gameSpendingData [" + gameSpendingData + "]");
      }

   }

   private void sendRewardProgramTriggers(int botId, Map<Integer, UserData> userDataShortCache, UserLocal userBean, GameWinnerData winnerData) throws Exception {
      UserData winnerUserData = this.getUserData(userDataShortCache, userBean, winnerData.getWinnerUserID());
      BotGameWonTrigger botGameWonTrigger = new BotGameWonTrigger(winnerUserData);
      botGameWonTrigger.amountDelta = winnerData.getWinningAmount();
      botGameWonTrigger.quantityDelta = 1;
      botGameWonTrigger.currency = winnerData.getCurrency();
      botGameWonTrigger.botID = botId;
      RewardCentre.getInstance().sendTrigger(botGameWonTrigger);
      if (log.isDebugEnabled()) {
         log.info("Sent trigger [" + botGameWonTrigger + "] for gameWinnerData [" + winnerData + "]");
      }

   }

   public void cancelPot(int potID, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select * from pot where id=?");
         ps.setInt(1, potID);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Record not found");
         } else {
            PotData potData = PotData.fromResultSet(rs);
            rs.close();
            ps.close();
            if (potData.getStatus() != PotData.StatusEnum.ACTIVE) {
               throw new EJBException("Pot is not active");
            } else {
               Set<AccountEntryData> potEntries = new HashSet();
               ps = connMaster.prepareStatement("select * from accountentry where type=? and reference=?");
               ps.setInt(1, AccountEntryData.TypeEnum.POT_ENTRY.value());
               ps.setString(2, Integer.toString(potID));
               rs = ps.executeQuery();

               while(rs.next()) {
                  potEntries.add(new AccountEntryData(rs));
               }

               rs.close();
               ps.close();
               Iterator i$ = potEntries.iterator();

               while(i$.hasNext()) {
                  AccountEntryData accountEntry = (AccountEntryData)i$.next();
                  AccountEntryData refundEntry = new AccountEntryData();
                  refundEntry.type = AccountEntryData.TypeEnum.POT_ENTRY_REVERSAL;
                  refundEntry.username = accountEntry.username;
                  refundEntry.reference = accountEntry.reference;
                  refundEntry.description = "Game entry refund";
                  refundEntry.currency = accountEntry.currency;
                  refundEntry.amount = -accountEntry.amount;
                  refundEntry.fundedAmount = -accountEntry.fundedAmount;
                  refundEntry.tax = 0.0D;
                  refundEntry.costOfGoodsSold = -accountEntry.costOfGoodsSold;
                  refundEntry.costOfTrial = -accountEntry.costOfTrial;
                  this.createAccountEntry(connMaster, refundEntry, accountEntrySourceData);
               }

               ps = connMaster.prepareStatement("update pot set status=? where id=?");
               ps.setInt(1, PotData.StatusEnum.CANCELED.value());
               ps.setInt(2, potID);
               ps.executeUpdate();
            }
         }
      } catch (Exception var25) {
         log.error("Exception in cancelPot(" + potID + ")", var25);
         throw new EJBException("Unable to cancel game");
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var22) {
         }

      }
   }

   public void cancelAllPots(AccountEntrySourceData accountEntrySourceData) throws EJBException {
      Connection connMaster = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connMaster = this.dataSourceMaster.getConnection();
         ps = connMaster.prepareStatement("select id from pot where status=?");
         ps.setInt(1, PotData.StatusEnum.ACTIVE.value());
         rs = ps.executeQuery();
         LinkedList activePotIDs = new LinkedList();

         while(rs.next()) {
            activePotIDs.add(rs.getInt(1));
         }

         rs.close();
         ps.close();
         Iterator i$ = activePotIDs.iterator();

         while(i$.hasNext()) {
            int potID = (Integer)i$.next();

            try {
               this.cancelPot(potID, accountEntrySourceData);
            } catch (EJBException var24) {
               log.warn("cancelAllPots(): cancelPot(" + potID + ") threw exception", var24);
            }
         }

      } catch (Exception var25) {
         log.error("Exception in cancelAllPots()", var25);
         throw new EJBException("Unable to cancel all pots. Exception: " + var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
         }

         try {
            if (connMaster != null) {
               connMaster.close();
            }
         } catch (SQLException var21) {
         }

      }
   }

   public double convertCurrency(double amount, String fromCurrency, String toCurrency) throws EJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      double var8;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement("select ?/fromcurrency.exchangerate*tocurrency.exchangerate from currency fromcurrency, currency tocurrency where fromcurrency.code=? and tocurrency.code=?");
         ps.setDouble(1, amount);
         ps.setString(2, fromCurrency);
         ps.setString(3, toCurrency);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unknown currency code(s)");
         }

         var8 = rs.getDouble(1);
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
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var21) {
            connSlave = null;
         }

      }

      return var8;
   }

   public List<UserReferralData> getUnpaidUserReferrals(int fromActivationID) {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         connSlave = this.dataSourceSlave.getConnection();
         if (fromActivationID == 0) {
            ps = connSlave.prepareStatement("SELECT r.* FROM accountentry a, userreferral r WHERE a.reference = r.id AND a.type = ? AND a.datecreated > 0 ORDER BY a.type, a.datecreated DESC LIMIT 1");
            ps.setInt(1, AccountEntryData.TypeEnum.REFERRAL_CREDIT.value());
            rs = ps.executeQuery();
            if (rs.next()) {
               UserReferralData lastUserReferralPaid = new UserReferralData(rs);
               rs.close();
               ps.close();
               ps = connSlave.prepareStatement("SELECT id FROM activation WHERE mobilephone = ? AND datecreated >= ? ORDER BY id");
               ps.setString(1, lastUserReferralPaid.mobilePhone);
               ps.setTimestamp(2, new Timestamp(lastUserReferralPaid.dateCreated.getTime()));
               rs = ps.executeQuery();
               if (rs.next()) {
                  fromActivationID = rs.getInt("id") + 1;
               }
            }

            rs.close();
            ps.close();
         }

         ps = connSlave.prepareStatement("SELECT r.*, a.id ActivationID, a.username ReferredUsername FROM  activation a, user u, userreferral r WHERE a.username = u.username AND u.referredby = r.username AND a.mobilephone = r.mobilephone AND r.paid = 0 AND u.merchantcreated is null AND a.id >= ?");
         ps.setInt(1, fromActivationID);
         rs = ps.executeQuery();
         LinkedList userReferrals = new LinkedList();

         while(rs.next()) {
            userReferrals.add(new UserReferralData(rs));
         }

         LinkedList var6 = userReferrals;
         return var6;
      } catch (SQLException var21) {
         throw new EJBException(var21.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var20) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var19) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var18) {
            connSlave = null;
         }

      }
   }

   public AccountEntryData chargeUserForGameItem(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.GAME_ITEM_PURCHASE;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = -amount;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      accountEntry = this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);

      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(username, false, false);
         GameItemPurchasedTrigger trigger = new GameItemPurchasedTrigger(userData);
         trigger.amountDelta = amount;
         trigger.currency = currency;
         trigger.quantityDelta = 1;
         trigger.reference = reference;
      } catch (Exception var12) {
         log.warn("Unable to notify reward system", var12);
      }

      return accountEntry;
   }

   public AccountEntryData giveGameReward(String username, String reference, String description, double amount, double fundedAmount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.GAME_REWARD;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = amount;
      accountEntry.fundedAmount = fundedAmount;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData giveMarketingReward(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.MARKETING_REWARD;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = amount;
      accountEntry.fundedAmount = 0.0D;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData giveUnfundedCredits(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (amount <= 0.0D) {
         throw new EJBException("Give unfunded balance amount can not be negative, amount:" + amount);
      } else {
         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = username;
         accountEntry.type = AccountEntryData.TypeEnum.BONUS_CREDIT;
         accountEntry.reference = reference;
         accountEntry.description = description;
         accountEntry.currency = currency;
         accountEntry.amount = amount;
         accountEntry.fundedAmount = 0.0D;
         accountEntry.tax = 0.0D;
         accountEntry.wholesaleCost = 0.0D;
         return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
      }
   }

   public AccountEntryData deductUnfundedCredits(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      if (amount >= 0.0D) {
         throw new EJBException("Deduct unfunded balance amount can not be positive, amount:" + amount);
      } else {
         AccountBalanceData userBalance = this.getAccountBalance(username);
         double positiveAmount = Math.abs(amount);
         double unfundedBalance = userBalance.balance - userBalance.fundedBalance;
         positiveAmount = Math.min(positiveAmount, unfundedBalance);
         amount = -positiveAmount;
         AccountEntryData accountEntry = new AccountEntryData();
         accountEntry.username = username;
         accountEntry.type = AccountEntryData.TypeEnum.DEDUCT_UNFUNDED_BALANCE;
         accountEntry.reference = reference;
         accountEntry.description = description;
         accountEntry.currency = currency;
         accountEntry.amount = amount;
         accountEntry.fundedAmount = 0.0D;
         accountEntry.tax = 0.0D;
         accountEntry.wholesaleCost = 0.0D;
         return amount == 0.0D ? accountEntry : this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
      }
   }

   public Map<Integer, Double> getMerchantRevenueTrailReport(Date startDate, Date endDate) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashMap var8;
      try {
         double trailRate = SystemProperty.getDouble("MerchantTrailRate", 0.1D);
         if (trailRate != 0.0D) {
            String sql = "SELECT aes.MerchantUserID, -sum(fundedamount/exchangerate) * ? RevenueTrail FROM  accountentry ae, accountentrysource aes WHERE ae.id = aes.accountentryid AND ae.datecreated >= ? AND ae.datecreated < ? AND ae.type in (" + SystemProperty.get("MerchantTrailAccountEntryTypes", "27, 28, 29, 32, 41") + ") AND " + "aes.merchantuserid is not null " + "GROUP BY " + "aes.merchantuserid";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, trailRate);
            ps.setDate(2, new java.sql.Date(startDate.getTime()));
            ps.setDate(3, new java.sql.Date(endDate.getTime()));
            rs = ps.executeQuery();
            HashMap revenueTrailMap = new HashMap();

            while(rs.next()) {
               revenueTrailMap.put(rs.getInt("merchantuserid"), rs.getDouble("revenuetrail"));
            }

            HashMap var10 = revenueTrailMap;
            return var10;
         }

         var8 = new HashMap();
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage(), var28);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var8;
   }

   public Map<Integer, Double> getMerchantRevenueGameTrailReport(Date startDate, Date endDate) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      try {
         double trailRate = SystemProperty.getDouble("MerchantGameTrailRate", 0.01D);
         if (trailRate == 0.0D) {
            HashMap var30 = new HashMap();
            return var30;
         } else {
            String sql = "SELECT aes.merchantuserid, SUM(-(ae.fundedamount/ae.exchangerate) * ?) revenuetrail FROM accountentry ae, accountentrysource aes WHERE ae.type in (?, ?, ?) AND ae.datecreated >= ? AND ae.datecreated < ? AND aes.accountentryid = ae.id AND aes.merchantuserid IS NOT NULL GROUP BY aes.merchantuserid HAVING revenuetrail >= ?";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, trailRate);
            ps.setInt(2, AccountEntryData.TypeEnum.POT_ENTRY.value());
            ps.setInt(3, AccountEntryData.TypeEnum.POT_ENTRY_REVERSAL.value());
            ps.setInt(4, AccountEntryData.TypeEnum.GAME_ITEM_PURCHASE.value());
            ps.setDate(5, new java.sql.Date(startDate.getTime()));
            ps.setDate(6, new java.sql.Date(endDate.getTime()));
            ps.setDouble(7, SystemProperty.getDouble("MinimumMerchantGameTrailAmount", 0.01D));
            rs = ps.executeQuery();
            HashMap revenueTrailMap = new HashMap();

            while(rs.next()) {
               revenueTrailMap.put(rs.getInt("merchantuserid"), rs.getDouble("revenuetrail"));
            }

            HashMap var10 = revenueTrailMap;
            return var10;
         }
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage(), var28);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }
   }

   public Map<Integer, Double> getMerchantRevenueThirdPartyTrailReport(Date startDate, Date endDate) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashMap var8;
      try {
         double trailRate = SystemProperty.getDouble("MerchantThirdPartyGameTrailRate", 0.05D);
         if (trailRate != 0.0D) {
            String sql = "SELECT aes.MerchantUserID, (-SUM(ae.fundedamount/ae.exchangerate) * ?) revenuetrail FROM accountentry ae, accountentrysource aes WHERE ae.id = aes.accountentryid AND ae.type = ? AND ae.datecreated >= ? AND ae.datecreated < ? AND aes.merchantuserid IS NOT NULL GROUP BY aes.merchantuserid HAVING revenuetrail >= ?";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, trailRate);
            ps.setInt(2, AccountEntryData.TypeEnum.THIRD_PARTY_API_DEBIT.value());
            ps.setDate(3, new java.sql.Date(startDate.getTime()));
            ps.setDate(4, new java.sql.Date(endDate.getTime()));
            ps.setDouble(5, SystemProperty.getDouble("MinimumMerchantGameTrailAmount", 0.01D));
            rs = ps.executeQuery();
            HashMap revenueTrailMap = new HashMap();

            while(rs.next()) {
               revenueTrailMap.put(rs.getInt("merchantuserid"), rs.getDouble("revenuetrail"));
            }

            HashMap var10 = revenueTrailMap;
            return var10;
         }

         var8 = new HashMap();
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage(), var28);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
            conn = null;
         }

      }

      return var8;
   }

   public List<MerchantTagUserData> getExpiredNonTopMerchantTags(int limit, int minimumId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      List<MerchantTagUserData> merchantTagData = new LinkedList();
      if (minimumId < 0) {
         minimumId = 0;
      }

      try {
         String sql = "SELECT mt.*, ui1.username username, ui2.username merchantusername, u1.type usertype, u2.type merchantusertype FROM merchanttag mt, userid ui1, userid ui2, user u1, user u2 WHERE ui1.id = mt.userid AND ui1.username = u1.username AND ui2.id = mt.merchantuserid AND ui2.username = u2.username AND u1.type < 3 AND mt.status = ? AND mt.lastsalesdate < DATE_SUB(NOW(), INTERVAL ? MINUTE) AND mt.id > ? ORDER BY mt.id LIMIT ?";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, MerchantTagData.StatusEnum.ACTIVE.value());
         ps.setInt(2, MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
         ps.setInt(3, minimumId);
         ps.setInt(4, limit);
         rs = ps.executeQuery();

         while(rs.next()) {
            merchantTagData.add(new MerchantTagUserData(rs));
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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

      return merchantTagData;
   }

   public List<MerchantTagUserData> getExpiredTopMerchantTags(int limit, int minimumId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      List<MerchantTagUserData> merchantTagData = new LinkedList();
      if (minimumId < 0) {
         minimumId = 0;
      }

      try {
         String sql = "SELECT mt.*, ui1.username username, ui2.username merchantusername, u1.type usertype, u2.type merchantusertype FROM merchanttag mt, userid ui1, userid ui2, user u1, user u2 WHERE ui1.id = mt.userid AND ui1.username = u1.username AND ui2.id = mt.merchantuserid AND ui2.username = u2.username AND u1.type = 3 AND mt.status = ? AND mt.lastsalesdate < DATE_SUB(NOW(), INTERVAL ? MINUTE) AND mt.id > ? ORDER BY mt.id LIMIT ?";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, MerchantTagData.StatusEnum.ACTIVE.value());
         ps.setInt(2, MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
         ps.setInt(3, minimumId);
         ps.setInt(4, limit);
         rs = ps.executeQuery();

         while(rs.next()) {
            merchantTagData.add(new MerchantTagUserData(rs));
         }
      } catch (SQLException var22) {
         throw new EJBException(var22.getMessage());
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

      return merchantTagData;
   }

   public Map<Integer, Double> getSuperMerchantRevenueTrailReport(Date startDate, Date endDate) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      HashMap var8;
      try {
         double trailRate = SystemProperty.getDouble("SuperMerchantTrailRate", 0.05D);
         if (trailRate != 0.0D) {
            Calendar topMerchantCal = Calendar.getInstance();
            topMerchantCal.add(12, -MerchantTagData.TypeEnum.TOP_MERCHANT_TAG.validity());
            Timestamp topMerchantCutoff = new Timestamp(topMerchantCal.getTimeInMillis());
            Calendar nonTopMerchantCal = Calendar.getInstance();
            topMerchantCal.add(12, -MerchantTagData.TypeEnum.NON_TOP_MERCHANT_TAG.validity());
            Timestamp nonTopMerchantCutoff = new Timestamp(nonTopMerchantCal.getTimeInMillis());
            String sql = "SELECT mt.merchantuserid, -SUM(fundedamount/exchangerate) * ? RevenueTrail FROM   accountentry ae \t   , accountentrysource aes \t   , merchanttag mt \t   , userid ui \t   , user u WHERE ae.id = aes.accountentryid AND aes.merchantuserid = mt.userid AND mt.status = 1 AND ui.id = mt.userid AND ui.username = u.username AND ((u.type = ? AND mt.lastsalesdate > ?) OR (u.type < ? AND mt.lastsalesdate > ?)) AND ae.datecreated >= ? AND ae.datecreated < ? AND ae.type IN (" + SystemProperty.get("SuperMerchantTrailAccountEntryTypes", "27, 28, 29, 32, 41") + ") " + "AND aes.merchantuserid IS NOT NULL " + "GROUP BY mt.merchantuserid";
            conn = this.dataSourceSlave.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setDouble(1, trailRate);
            ps.setInt(2, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setTimestamp(3, topMerchantCutoff);
            ps.setInt(4, UserData.TypeEnum.MIG33_TOP_MERCHANT.value());
            ps.setTimestamp(5, nonTopMerchantCutoff);
            ps.setDate(6, new java.sql.Date(startDate.getTime()));
            ps.setDate(7, new java.sql.Date(endDate.getTime()));
            rs = ps.executeQuery();
            HashMap revenueTrailMap = new HashMap();

            while(rs.next()) {
               revenueTrailMap.put(rs.getInt("merchantuserid"), rs.getDouble("revenuetrail"));
            }

            HashMap var14 = revenueTrailMap;
            return var14;
         }

         var8 = new HashMap();
      } catch (SQLException var32) {
         throw new EJBException(var32.getMessage(), var32);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var31) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var30) {
            ps = null;
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var29) {
            conn = null;
         }

      }

      return var8;
   }

   public AccountEntryData giveMerchantRevenueTrail(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.MERCHANT_REVENUE_TRAIL;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = amount;
      accountEntry.fundedAmount = 0.0D;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData giveMerchantRevenueGameTrail(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.MERCHANT_GAME_TRAIL;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = amount;
      accountEntry.fundedAmount = 0.0D;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData giveMerchantRevenueThirdPartyTrail(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.MERCHANT_THIRD_PARTY_APP_TRAIL;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = amount;
      accountEntry.fundedAmount = 0.0D;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      return this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
   }

   public AccountEntryData thirdPartyAPIDebit(String username, String reference, String description, double amount, String currency, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      AccountEntryData accountEntry = new AccountEntryData();
      accountEntry.username = username;
      accountEntry.type = AccountEntryData.TypeEnum.THIRD_PARTY_API_DEBIT;
      accountEntry.reference = reference;
      accountEntry.description = description;
      accountEntry.currency = currency;
      accountEntry.amount = -amount;
      accountEntry.tax = 0.0D;
      accountEntry.wholesaleCost = 0.0D;
      AccountEntryData result = this.createAccountEntry((Connection)null, accountEntry, accountEntrySourceData);
      if (result != null) {
         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData recipientUserData = userBean.loadUser(username, false, false);
            ThirdPartyAppPurchaseTrigger trigger = new ThirdPartyAppPurchaseTrigger(recipientUserData);
            trigger.amountDelta = -result.amount;
            trigger.quantityDelta = 1;
            trigger.currency = result.currency;
            trigger.applicationName = accountEntry.reference.split(":")[0];
            log.debug("Sending ThirdPartyAppPurchaseTrigger for username[" + username + "] [" + trigger.applicationName + "][" + trigger.amountDelta + "][" + trigger.currency + "] [" + reference + "] [" + description + "]");
            RewardCentre.getInstance().sendTrigger(trigger);
         } catch (Exception var13) {
            log.error("Error sending ThirdPartyAppPurchaseTrigger for username[" + username + "] [" + amount + "][" + currency + "] [" + reference + "] [" + description + "] :" + var13.getMessage(), var13);
         }
      }

      return result;
   }

   public boolean userCanAffordPaidEmote(String username, PaidEmoteData paidEmoteData, Connection masterConn) throws EJBException {
      return paidEmoteData.getPrice() <= 0.0D ? true : this.userCanAffordCost(username, paidEmoteData.getPrice(), paidEmoteData.getCurrency(), masterConn);
   }

   public boolean sendRechargeCreditRewardProgramTrigger(String username, double amount, String currency, int waitInSeconds) throws EJBException {
      try {
         UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
         UserData userData = userBean.loadUser(username, false, false);
         CreditRechargeTrigger trigger = new CreditRechargeTrigger(userData);
         trigger.amountDelta = amount;
         trigger.quantityDelta = 1;
         trigger.currency = currency;
         Future<Boolean> future = RewardCentre.getInstance().sendTrigger(trigger);
         if (future == null) {
            throw new EJBException("Unable to submit trigger - future object is null.");
         } else {
            return waitInSeconds == 0 ? true : (Boolean)future.get((long)waitInSeconds, TimeUnit.SECONDS);
         }
      } catch (TimeoutException var10) {
         throw new EJBException("sending of credit recharge trigger timed out :" + var10.getMessage());
      } catch (Exception var11) {
         throw new EJBException("Unxpected Exception: " + var11.getMessage(), var11);
      }
   }

   public PaymentData createPayment(PaymentData paymentData) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      PaymentData var85;
      try {
         if (paymentData.vendorType == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"VendorType"});
         }

         if (paymentData.currency == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"Currency"});
         }

         if (paymentData.fetchCreatedTime() == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"CreatedTime"});
         }

         String normalizedCurrency = PaymentUtils.normalizeCurrency(paymentData.currency);
         Object var6 = null;

         byte[] extraFields;
         try {
            extraFields = paymentData.serializeExtraFieldsToJSON().toString().getBytes();
         } catch (JSONException var77) {
            log.error("Failed to save extrafields for payment [" + paymentData.userId + "]: " + paymentData.vendorType.value() + " " + paymentData.currency + " " + paymentData.amount + " :: " + var77.getMessage());
            extraFields = PaymentData.EMPTY_EXTRA_FIELDS;
         }

         conn = this.dataSourceMaster.getConnection();
         double exchangeRate = 0.0D;
         String sql = "SELECT ROUND(tocurrency.exchangerate/fromcurrency.exchangerate, 8) exchangerate FROM currency fromcurrency     , currency tocurrency WHERE fromcurrency.code = ? AND tocurrency.code = ?";
         ps = conn.prepareStatement(sql);
         ps.setString(1, paymentData.currency);
         ps.setString(2, SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.DEFAULT_RATE_LIMIT_CURRENCY));

         for(rs = ps.executeQuery(); rs.next(); exchangeRate = rs.getDouble("exchangerate")) {
         }

         if (exchangeRate <= 0.0D) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.INTERNAL_SYSTEM_ERROR, new Object[0]);
         }

         rs.close();
         ps.close();
         String paymentsInsertSQL = "INSERT INTO payments(userid, type, currency, amount, description,datecreated,status,exchangerateUSD) VALUES( ?, ?, ?, ?, ?, ?, ?, ?)";
         ps = conn.prepareStatement(paymentsInsertSQL, 1);
         PaymentData.StatusEnum status = paymentData.getInitialStatus();

         try {
            ps.setInt(1, paymentData.userId);
            ps.setInt(2, paymentData.vendorType.value());
            ps.setString(3, normalizedCurrency);
            ps.setDouble(4, paymentData.amount);
            ps.setBytes(5, extraFields);
            ps.setTimestamp(6, new Timestamp(paymentData.fetchCreatedTime().getTime()));
            ps.setInt(7, status.value());
            ps.setDouble(8, exchangeRate);
            if (ps.executeUpdate() != 1) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to create paymentData for userid " + paymentData.userId + " vendorType " + paymentData.vendorType.displayName()});
            }

            rs = ps.getGeneratedKeys();
            if (!rs.next()) {
               throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to retrieve generated paymentData ID"});
            }

            paymentData.id = rs.getInt(1);
            paymentData.status = status;
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var73) {
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var72) {
            }

         }

         if (paymentData instanceof VendorVoucherData) {
            VendorVoucherData paymentVendorVoucherData = (VendorVoucherData)paymentData;
            String vendorVoucherInsertSQL = "INSERT INTO vendorvoucher(paymentid,vouchercode) VALUES( ?, ?)";
            ps = conn.prepareStatement(vendorVoucherInsertSQL, 1);

            try {
               ps.setInt(1, paymentData.id);
               ps.setString(2, paymentVendorVoucherData.voucherCode);
               if (ps.executeUpdate() != 1) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to create vendorvoucher for userid " + paymentData.userId + " vendorType " + paymentData.vendorType.displayName()});
               }

               rs = ps.getGeneratedKeys();
               if (!rs.next()) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to retrieve generated vendorVoucher ID"});
               }

               paymentVendorVoucherData.voucherVoucherId = rs.getInt(1);
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var75) {
               }

               try {
                  if (ps != null) {
                     ps.close();
                  }
               } catch (SQLException var74) {
               }

            }
         }

         this.storePaymentDetails(paymentData, conn);
         var85 = paymentData;
      } catch (SQLException var79) {
         throw new EJBException(var79.getMessage(), var79);
      } catch (PaymentException var80) {
         throw var80;
      } catch (EJBException var81) {
         throw var81;
      } catch (Exception var82) {
         throw new EJBException("Unhandled exception ", var82);
      } finally {
         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var71) {
         }

      }

      return var85;
   }

   private void storePaymentDetails(PaymentData paymentData, Connection conn) throws PaymentException, SQLException {
      List<PaymentMetaDetails> details = paymentData.getDetails();
      if (!details.isEmpty()) {
         ConnectionHolder ch = null;
         ResultSet rs = null;
         PreparedStatement ps = null;

         try {
            new ConnectionHolder(this.dataSourceMaster, conn);
            String sql = "";
            Iterator i$ = details.iterator();

            while(i$.hasNext()) {
               PaymentMetaDetails metaData = (PaymentMetaDetails)i$.next();
               sql = "SELECT id FROM paymentmetadetails WHERE detail = ? AND type = ? LIMIT 1";
               ps = conn.prepareStatement(sql);
               ps.setString(1, metaData.value);
               ps.setInt(2, metaData.type.code());

               for(rs = ps.executeQuery(); rs.next(); metaData.id = rs.getInt("id")) {
               }

               rs.close();
               ps.close();
               if (metaData.id == null) {
                  sql = "INSERT into paymentmetadetails(type, detail) VALUES (?,?)";
                  ps = conn.prepareStatement(sql, 1);
                  ps.setInt(1, metaData.type.code());
                  ps.setString(2, metaData.value);
                  if (ps.executeUpdate() != 1) {
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to store payment details [" + paymentData.userId + " vendorType " + paymentData.vendorType.displayName() + "]"});
                  }

                  rs = ps.getGeneratedKeys();
                  if (!rs.next()) {
                     throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to retrieve generated payment detail id"});
                  }

                  metaData.id = rs.getInt(1);
               }

               sql = "INSERT into paymenttopaymentmetadetails(paymentid, paymentmetadetailsid) VALUES (?,?)";
               ps = conn.prepareStatement(sql, 1);
               ps.setInt(1, paymentData.id);
               ps.setInt(2, metaData.id);
               if (ps.executeUpdate() != 1) {
                  throw new PaymentException(ErrorCause.PaymentErrorReasonType.DATABASE_ERROR, new Object[]{"Unable to store payment details [" + paymentData.userId + " vendorType " + paymentData.vendorType.displayName() + "]"});
               }
            }
         } catch (PaymentException var24) {
            throw var24;
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var23) {
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var22) {
            }

            try {
               if (conn != null) {
                  conn.close();
               }
            } catch (SQLException var21) {
            }

         }

      }
   }

   public <T extends PaymentData> PaymentData getPaymentById(int paymentId) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PaymentData pData = null;

      try {
         String sql = "SELECT p.*, u.username,v.id as vendorVoucherId,v.voucherCode FROM payments p   join userid u ON (p.userid = u.id)\tleft join vendorvoucher v on (v.paymentid = p.id)WHERE p.id = ?";
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql);
         ps.setInt(1, paymentId);
         rs = ps.executeQuery();
         if (!rs.next()) {
            throw new EJBException("Unknown paymentData ID:" + paymentId);
         } else {
            pData = PaymentDataFactory.getPayment(rs);
            sql = "SELECT type, detail FROM paymentmetadetails pmd, paymenttopaymentmetadetails p2pmd WHERE pmd.id = p2pmd.paymentmetadetailsid AND p2pmd.paymentid = ?";
            ps = conn.prepareStatement(sql);
            ps.setInt(1, paymentId);
            rs = ps.executeQuery();
            ArrayList meta = new ArrayList();

            while(rs.next()) {
               meta.add(new PaymentMetaDetails(PaymentMetaDetails.MetaType.fromCode(rs.getInt("type")), rs.getString("detail")));
            }

            pData.setDetails(meta);
            return pData;
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage(), var24);
      } catch (EJBException var25) {
         throw var25;
      } catch (Exception var26) {
         throw new EJBException("Unhandled exception ", var26);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
         }

      }
   }

   public PaymentData getPaymentByVoucher(int vendorType, Integer statusCode, String voucherCode, Date timeOfAction, int maxRecycledPeriodDays) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PaymentData result = null;
      long timeOfActionMillis = 0L;
      if (timeOfAction != null) {
         timeOfActionMillis = timeOfAction.getTime();
      } else {
         timeOfActionMillis = System.currentTimeMillis();
      }

      Calendar beginCalendar = Calendar.getInstance();
      beginCalendar.setTimeInMillis(timeOfActionMillis);
      beginCalendar.add(5, -maxRecycledPeriodDays);

      try {
         StringBuilder sql = new StringBuilder(" SELECT p.*, u.username,v.id as vendorVoucherId,v.voucherCode  FROM payments p   join userid u ON (p.userid = u.id)\tleft join vendorvoucher v on (v.paymentid = p.id) WHERE  p.type = ?  AND v.vouchercode = ?  AND p.dateCreated >=? ");
         if (statusCode != null) {
            sql.append(" AND p.status = ?");
         }

         sql.append(" ORDER BY v.id DESC LIMIT 1");
         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sql.toString());
         ps.setInt(1, vendorType);
         ps.setString(2, voucherCode);
         ps.setTimestamp(3, new Timestamp(beginCalendar.getTimeInMillis()));
         if (statusCode != null) {
            ps.setInt(4, statusCode);
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            result = PaymentDataFactory.getPayment(rs);
         }
      } catch (SQLException var30) {
         throw new EJBException(var30.getMessage(), var30);
      } catch (EJBException var31) {
         throw var31;
      } catch (Exception var32) {
         throw new EJBException("Unhandled exception ", var32);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var29) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var28) {
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var27) {
         }

      }

      return result;
   }

   public List<PaymentData> getPendingPaymentTransactionsForRequery(int vendorType, Integer limitRowCount) {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList payments = new ArrayList();

      try {
         StringBuilder sqlStrBuilder = new StringBuilder("SELECT p.*, u.username,v.id as vendorVoucherId,v.voucherCode FROM payments p   join userid u ON (p.userid = u.id)\tleft join vendorvoucher v on (v.paymentid = p.id)\tWHERE p.type = ?   AND p.status = ?  \tOrder By p.dateUpdated ASC ");
         if (limitRowCount != null) {
            sqlStrBuilder.append("LIMIT 0,?");
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sqlStrBuilder.toString());
         ps.setInt(1, vendorType);
         ps.setInt(2, PaymentData.StatusEnum.PENDING.value());
         if (limitRowCount != null) {
            ps.setInt(3, limitRowCount);
         }

         rs = ps.executeQuery();

         while(rs.next()) {
            payments.add(PaymentDataFactory.getPayment(rs));
         }
      } catch (SQLException var24) {
         throw new EJBException(var24.getMessage(), var24);
      } catch (EJBException var25) {
         throw var25;
      } catch (Exception var26) {
         throw new EJBException("Unhandled exception ", var26);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var23) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var22) {
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var21) {
         }

      }

      return payments;
   }

   public List<PaymentData> getPaymentTransactions(String username, Integer vendorType, Integer status, Integer limitRowCount) throws EJBException {
      Connection conn = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      ArrayList payments = new ArrayList();

      try {
         StringBuilder sqlStrBuilder = new StringBuilder("SELECT p.*, u.username,v.id as vendorVoucherId,v.voucherCode  FROM payments p  join userid u ON (p.userid = u.id) left join vendorvoucher v on (v.paymentid = p.id)");
         boolean hasConstraint = false;
         if (username != null) {
            hasConstraint = true;
            sqlStrBuilder.append(" where username = ? ");
         }

         if (vendorType != null) {
            sqlStrBuilder.append(hasConstraint ? " and" : " where");
            hasConstraint = true;
            sqlStrBuilder.append(" type = ?");
         }

         if (status != null) {
            sqlStrBuilder.append(hasConstraint ? " and" : " where");
            hasConstraint = true;
            sqlStrBuilder.append(" status = ? ");
         }

         if (limitRowCount != null) {
            sqlStrBuilder.append(" LIMIT 0,?");
         }

         conn = this.dataSourceSlave.getConnection();
         ps = conn.prepareStatement(sqlStrBuilder.toString());
         int index = 1;
         if (username != null) {
            ps.setString(index, username);
            ++index;
         }

         if (vendorType != null) {
            ps.setInt(index, vendorType);
            ++index;
         }

         if (status != null) {
            ps.setInt(index, status);
            ++index;
         }

         if (limitRowCount != null) {
            ps.setInt(index, limitRowCount);
            ++index;
         }

         rs = ps.executeQuery();

         while(rs.next()) {
            payments.add(PaymentDataFactory.getPayment(rs));
         }
      } catch (SQLException var28) {
         throw new EJBException(var28.getMessage(), var28);
      } catch (EJBException var29) {
         throw var29;
      } catch (Exception var30) {
         throw new EJBException("Unhandled exception ", var30);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var27) {
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var26) {
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var25) {
         }

      }

      return payments;
   }

   public PaymentData updatePayment(PaymentData paymentData, AccountEntrySourceData accountEntrySourceData) throws PaymentException {
      Connection conn = null;
      PreparedStatement ps = null;

      PaymentData var58;
      try {
         if (paymentData.id == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"paymentData.id"});
         }

         if (paymentData.vendorType == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"paymentData.VendorType"});
         }

         if (paymentData.currency == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"paymentData.Currency"});
         }

         if (paymentData.fetchUpdatedTime() == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"paymentData.updatedTime"});
         }

         if (paymentData.status == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.MISSING_FIELD, new Object[]{"paymentData.status"});
         }

         PaymentData currentStoredPaymentData = this.getPaymentById(paymentData.id);
         if (currentStoredPaymentData == null) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.RECORD_NOT_FOUND, new Object[]{paymentData.id});
         }

         String normalizedCurrency = PaymentUtils.normalizeCurrency(paymentData.currency);
         if (currentStoredPaymentData.enableStrictAmountCheck() && (currentStoredPaymentData.amount != paymentData.amount || !currentStoredPaymentData.currency.equals(normalizedCurrency))) {
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.ILLEGAL_TRANSACTION, new Object[]{"Amount and currency for paymentData.id " + currentStoredPaymentData.id + " is about to be altered to " + paymentData.currency + " " + paymentData.amount});
         }

         Object var7 = null;

         byte[] extraFields;
         try {
            extraFields = paymentData.serializeExtraFieldsToJSON().toString().getBytes();
         } catch (JSONException var49) {
            log.error("Failed to save extrafields for payment [" + paymentData.userId + "]: " + paymentData.vendorType.value() + " " + paymentData.currency + " " + paymentData.amount + " :: " + var49.getMessage());
            extraFields = PaymentData.EMPTY_EXTRA_FIELDS;
         }

         String parentSql = "UPDATE payments SET amount = ?     ,currency = ?     ,status = ?     ,dateupdated = ?     ,description = ? ";
         if (paymentData.vendorTransactionId != null) {
            parentSql = parentSql + ", vendortransactionid = ? ";
         }

         parentSql = parentSql + " WHERE id = ?  AND type = ? ";
         PaymentData.StatusEnum[] expectedStates;
         if (!(paymentData instanceof VendorVoucherData) && !(paymentData instanceof CreditCardData)) {
            parentSql = parentSql + " AND status = ? ";
            expectedStates = new PaymentData.StatusEnum[]{PaymentData.StatusEnum.PENDING};
         } else if (paymentData.status == PaymentData.StatusEnum.ONHOLD) {
            parentSql = parentSql + " AND status = ? ";
            expectedStates = new PaymentData.StatusEnum[]{PaymentData.StatusEnum.ONHOLD};
         } else {
            parentSql = parentSql + " AND status IN (?,?) ";
            expectedStates = new PaymentData.StatusEnum[]{PaymentData.StatusEnum.ONHOLD, PaymentData.StatusEnum.PENDING};
         }

         conn = this.dataSourceMaster.getConnection();
         int updateCount = false;
         ps = conn.prepareStatement(parentSql);

         int updateCount;
         try {
            ps.setDouble(1, paymentData.amount);
            ps.setString(2, normalizedCurrency);
            ps.setInt(3, paymentData.status.value());
            ps.setTimestamp(4, new Timestamp(paymentData.fetchUpdatedTime().getTime()));
            int paramIdx = 9;
            if (paymentData.vendorTransactionId != null) {
               ps.setBytes(5, extraFields);
               ps.setString(6, paymentData.vendorTransactionId);
               ps.setInt(7, paymentData.id);
               ps.setInt(8, paymentData.vendorType.value());
            } else {
               paramIdx = 8;
               ps.setBytes(5, extraFields);
               ps.setInt(6, paymentData.id);
               ps.setInt(7, paymentData.vendorType.value());
            }

            PaymentData.StatusEnum[] arr$ = expectedStates;
            int len$ = expectedStates.length;
            int i$ = 0;

            while(true) {
               if (i$ >= len$) {
                  updateCount = ps.executeUpdate();
                  break;
               }

               PaymentData.StatusEnum expectedState = arr$[i$];
               ps.setInt(paramIdx, expectedState.value());
               ++paramIdx;
               ++i$;
            }
         } finally {
            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var48) {
            }

         }

         if (updateCount != 1) {
            log.warn("Status for payment " + paymentData.id + " has not been updated");
            throw new PaymentException(ErrorCause.PaymentErrorReasonType.DB_UPDATE_PAYMENT_FAILED, new Object[0]);
         }

         if (paymentData.status == PaymentData.StatusEnum.APPROVED) {
            if (paymentData.vendorType == PaymentData.TypeEnum.CREDIT_CARD) {
               CreditCardData creditCardData = (CreditCardData)paymentData;
               this.creditUserFromCreditCardPayment(creditCardData.toCreditCardPaymentData(), accountEntrySourceData, (Connection)null);
            } else {
               this.creditUserViaThirdPartyPayment(paymentData, accountEntrySourceData);
            }
         }

         String sql = "DELETE FROM paymenttopaymentmetadetails WHERE paymentid = ?";
         ps = conn.prepareStatement(sql);
         ps.setInt(1, paymentData.id);
         ps.executeUpdate();
         this.storePaymentDetails(paymentData, conn);
         var58 = paymentData;
      } catch (SQLException var51) {
         throw new EJBException(var51.getMessage(), var51);
      } catch (PaymentException var52) {
         throw var52;
      } catch (EJBException var53) {
         throw var53;
      } catch (Exception var54) {
         throw new EJBException("Unhandled exception ", var54);
      } finally {
         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var47) {
         }

         try {
            if (conn != null) {
               conn.close();
            }
         } catch (SQLException var46) {
         }

      }

      return var58;
   }

   private AccountEntryData creditUserViaThirdPartyPayment(PaymentData paymentData, AccountEntrySourceData accountEntrySourceData) throws EJBException {
      try {
         AccountEntryData creditEntry = new AccountEntryData();
         creditEntry.username = paymentData.username;
         creditEntry.type = AccountEntryData.TypeEnum.THIRD_PARTY_PAYMENT;
         creditEntry.reference = paymentData.id.toString();
         creditEntry.description = String.format("Credit %s%s worth of migme credits via %s", paymentData.currency, paymentData.amount, paymentData.vendorType.displayName());
         creditEntry.currency = paymentData.currency;
         creditEntry.amount = paymentData.amount;
         creditEntry.fundedAmount = paymentData.amount;
         creditEntry.tax = 0.0D;
         creditEntry = this.createAccountEntry((Connection)null, creditEntry, accountEntrySourceData);
         if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.BONUS_ENABLED)) {
            Double bonus = this.calculateThirdPartyCreditRechargeBonusInBaseCurrency(paymentData.amount, paymentData.currency, paymentData.vendorType);
            if (bonus > 0.0D) {
               CashReceiptData cashReceiptData = new CashReceiptData();
               cashReceiptData.amountCredited = bonus;
               cashReceiptData.amountReceived = 0.0D;
               cashReceiptData.amountSent = 0.0D;
               cashReceiptData.dateCreated = paymentData.fetchCreatedTime();
               cashReceiptData.dateReceived = paymentData.fetchCreatedTime();
               cashReceiptData.type = CashReceiptData.TypeEnum.DIRECT_CREDIT;
               cashReceiptData.providerTransactionID = paymentData.id.toString();
               cashReceiptData.senderUsername = paymentData.username;
               cashReceiptData.comments = "Credit Card Bonus";
               cashReceiptData.paymentDetails = "Credit Card Bonus";
               cashReceiptData.enteredBy = "";
               MISLocal misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
               misBean = (MISLocal)EJBHomeCache.getLocalObject("MISLocal", MISLocalHome.class);
               misBean.createCashReceipt(cashReceiptData, accountEntrySourceData);
            }
         }

         try {
            UserLocal userBean = (UserLocal)EJBHomeCache.getLocalObject("UserLocal", UserLocalHome.class);
            UserData user = userBean.loadUser(paymentData.username, false, false);
            if (!user.mobileVerified && !user.emailVerified) {
               log.warn("User [" + paymentData.username + "] has been credited [" + paymentData.currency + " " + paymentData.amount + "] but is neither email verified nor mobile verified.");
            } else {
               if (user.emailVerified) {
                  this.sendCreditNotificationViaEmail(creditEntry, user.emailAddress);
               }

               if (user.mobileVerified) {
                  this.sendCreditNotificationViaSMS(creditEntry, user.mobilePhone, accountEntrySourceData);
               }
            }
         } catch (Exception var7) {
            log.error("An exception occurred on sending credit notification to user: " + var7.getMessage());
         }

         return creditEntry;
      } catch (CreateException var8) {
         throw new EJBException(var8.getMessage());
      }
   }

   private Double calculateThirdPartyCreditRechargeBonusInBaseCurrency(double amount, String currency, PaymentData.TypeEnum vendorType) {
      Double bonus = 0.0D;
      if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Payments.BONUS_ENABLED)) {
      }

      return bonus;
   }

   public boolean isPaymentAllowedToUser(int userID, int guardCapabilityID) throws FusionEJBException {
      GuardCapabilityEnum guardCapability = GuardCapabilityEnum.fromValue(guardCapabilityID);
      if (guardCapability == null) {
         throw new FusionEJBException("Unknown payment type.");
      } else {
         Connection connSlave = null;
         PreparedStatement ps = null;
         ResultSet rs = null;

         try {
            connSlave = this.dataSourceSlave.getConnection();
            String sql = "SELECT COUNT(*) ctr_capability FROM guardcapability gc, guardsetcapability gsc, guardsetmember gsm, guardset gs WHERE gc.id = gsc.guardcapabilityid AND gsm.guardsetid = gsc.guardsetid AND gsm.guardsetid = gs.id AND gc.id = ? AND gsm.memberid = ?";
            ps = connSlave.prepareStatement(sql);
            ps.setInt(1, guardCapabilityID);
            ps.setInt(2, userID);
            rs = ps.executeQuery();
            if (rs.next()) {
               int value = rs.getInt(1);
               if (value > 0) {
                  boolean var9 = true;
                  return var9;
               }
            }

            boolean var29 = false;
            return var29;
         } catch (SQLException var27) {
            log.error("SQLException occurred in isPaymentAllowedToUser: " + var27);
            throw new FusionEJBException(var27.getMessage());
         } finally {
            try {
               if (rs != null) {
                  rs.close();
               }
            } catch (SQLException var26) {
               rs = null;
            }

            try {
               if (ps != null) {
                  ps.close();
               }
            } catch (SQLException var25) {
               ps = null;
            }

            try {
               if (connSlave != null) {
                  connSlave.close();
               }
            } catch (SQLException var24) {
               connSlave = null;
            }

         }
      }
   }

   public int getPendingPaymentsCount(int userId, Integer paymentType) throws FusionEJBException {
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;

      int var7;
      try {
         connSlave = this.dataSourceSlave.getConnection();
         String sql = "SELECT COUNT(*) pendingPaymentsCount FROM payments WHERE userid = ? AND status = 0 ";
         if (paymentType != null) {
            sql = sql + "AND type = ?";
         }

         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, userId);
         if (paymentType != null) {
            ps.setInt(2, paymentType);
         }

         rs = ps.executeQuery();
         if (!rs.next()) {
            byte var27 = 0;
            return var27;
         }

         var7 = rs.getInt(1);
      } catch (SQLException var25) {
         log.error("SQLException occurred in isPaymentAllowedToUser: " + var25);
         throw new FusionEJBException(var25.getMessage());
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var24) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var23) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var22) {
            connSlave = null;
         }

      }

      return var7;
   }

   public PaymentSummaryData getPaymentSummaryByUserType(int paymentVendorType, int userType, int paymentStatus, Date startDateTime, Date endDateTime) {
      String sql = " SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) AS cummValue  FROM userid ui      ,payments p      ,user u  WHERE ui.id = p.userID  AND u.username = ui.username  AND p.type = ?  AND u.type = ?  AND p.status = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ";
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PaymentSummaryData summaryData = new PaymentSummaryData();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(" SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) AS cummValue  FROM userid ui      ,payments p      ,user u  WHERE ui.id = p.userID  AND u.username = ui.username  AND p.type = ?  AND u.type = ?  AND p.status = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ");
         ps.setInt(1, paymentVendorType);
         ps.setInt(2, userType);
         ps.setInt(3, paymentStatus);
         ps.setTimestamp(4, new Timestamp(startDateTime.getTime()));
         ps.setTimestamp(5, new Timestamp(endDateTime.getTime()));
         rs = ps.executeQuery();
         if (rs.next()) {
            summaryData.populateFrom(rs);
         }
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage(), var26);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var23) {
            connSlave = null;
         }

      }

      return summaryData;
   }

   public PaymentSummaryData getPaymentSummaryByCountryAndUserType(int paymentVendorType, Integer countryID, int userType, int paymentStatus, Date startDateTime, Date endDateTime) {
      String sql = " SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) as cummValue  FROM userid ui      ,payments p      ,user u  WHERE ui.id = p.userID  AND u.username = ui.username  AND p.type = ?  AND u.type = ?  AND p.status = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ";
      if (countryID == null) {
         sql = sql + "AND u.countryID is NULL ";
      } else {
         sql = sql + "AND u.countryID = ? ";
      }

      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PaymentSummaryData summaryData = new PaymentSummaryData();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(sql);
         ps.setInt(1, paymentVendorType);
         ps.setInt(2, userType);
         ps.setInt(3, paymentStatus);
         ps.setTimestamp(4, new Timestamp(startDateTime.getTime()));
         ps.setTimestamp(5, new Timestamp(endDateTime.getTime()));
         if (countryID != null) {
            ps.setInt(6, countryID);
         }

         rs = ps.executeQuery();
         if (rs.next()) {
            summaryData.populateFrom(rs);
         }
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage(), var27);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }

      return summaryData;
   }

   public PaymentSummaryData getPaymentSummaryByUserID(int paymentVendorType, int userID, int paymentStatus, Date startDateTime, Date endDateTime) {
      String sql = " SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) as cummValue  FROM payments p  WHERE p.type = ?  AND p.userID= ?  AND p.status = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ";
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PaymentSummaryData summaryData = new PaymentSummaryData();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(" SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) as cummValue  FROM payments p  WHERE p.type = ?  AND p.userID= ?  AND p.status = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ");
         ps.setInt(1, paymentVendorType);
         ps.setInt(2, userID);
         ps.setInt(3, paymentStatus);
         ps.setTimestamp(4, new Timestamp(startDateTime.getTime()));
         ps.setTimestamp(5, new Timestamp(endDateTime.getTime()));
         rs = ps.executeQuery();
         if (rs.next()) {
            summaryData.populateFrom(rs);
         }
      } catch (SQLException var26) {
         throw new EJBException(var26.getMessage(), var26);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var25) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var24) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var23) {
            connSlave = null;
         }

      }

      return summaryData;
   }

   public PaymentSummaryData getPaymentSummaryByMetaDetails(int paymentVendorType, int paymentStatus, int paymentMetaDetailField, String paymentMetaDetailValue, Date startDateTime, Date endDateTime) {
      String sql = " SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) as cummValue  FROM payments p       , paymenttopaymentmetadetails p2pmd      , paymentmetadetails pmd  WHERE p.id = p2pmd.paymentid  AND p2pmd.paymentmetadetailsid = pmd.id  AND p.type = ?  AND p.status = ?  AND pmd.type = ?  AND pmd.detail = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ";
      Connection connSlave = null;
      PreparedStatement ps = null;
      ResultSet rs = null;
      PaymentSummaryData summaryData = new PaymentSummaryData();

      try {
         connSlave = this.dataSourceSlave.getConnection();
         ps = connSlave.prepareStatement(" SELECT COUNT(p.id) as count,         SUM(p.amount*p.exchangeRateUSD) as cummValue  FROM payments p       , paymenttopaymentmetadetails p2pmd      , paymentmetadetails pmd  WHERE p.id = p2pmd.paymentid  AND p2pmd.paymentmetadetailsid = pmd.id  AND p.type = ?  AND p.status = ?  AND pmd.type = ?  AND pmd.detail = ?  AND p.dateupdated >= ?  AND p.dateupdated <= ? ");
         ps.setInt(1, paymentVendorType);
         ps.setInt(2, paymentStatus);
         ps.setInt(3, paymentMetaDetailField);
         ps.setString(4, paymentMetaDetailValue);
         ps.setTimestamp(5, new Timestamp(startDateTime.getTime()));
         ps.setTimestamp(6, new Timestamp(endDateTime.getTime()));
         rs = ps.executeQuery();
         if (rs.next()) {
            summaryData.populateFrom(rs);
         }
      } catch (SQLException var27) {
         throw new EJBException(var27.getMessage(), var27);
      } finally {
         try {
            if (rs != null) {
               rs.close();
            }
         } catch (SQLException var26) {
            rs = null;
         }

         try {
            if (ps != null) {
               ps.close();
            }
         } catch (SQLException var25) {
            ps = null;
         }

         try {
            if (connSlave != null) {
               connSlave.close();
            }
         } catch (SQLException var24) {
            connSlave = null;
         }

      }

      return summaryData;
   }

   public double getNonTransferableFundsAtSystemBaseCurrency(String username, Date timeOfAction, int numDaysToLookBack, Connection conn, boolean fromMasterDB) {
      double value = 0.0D;
      int[] transactionTypesForMinBalanceCalc = SystemProperty.getIntArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountTransaction.TRANSACTION_TYPES_FOR_MIN_BALANCE_CALC);
      if (transactionTypesForMinBalanceCalc != null && transactionTypesForMinBalanceCalc.length > 0) {
         try {
            StringBuilder sqlBuilder = new StringBuilder();
            sqlBuilder.append(" select sum(amount/exchangerate)  from accountentry  where username=?  and datecreated >= date_sub(?, interval ? day)  and type in (");
            StringUtil.repeat(sqlBuilder, "?", ",", transactionTypesForMinBalanceCalc.length);
            sqlBuilder.append(")");
            boolean createdConnection = conn == null;
            if (conn == null) {
               conn = fromMasterDB ? this.dataSourceMaster.getConnection() : this.dataSourceSlave.getConnection();
            }

            try {
               PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString());

               try {
                  pstmt.setString(1, username);
                  pstmt.setTimestamp(2, new Timestamp(timeOfAction.getTime()));
                  pstmt.setInt(3, numDaysToLookBack);
                  int paramIndex = 4;

                  for(int i = 0; i < transactionTypesForMinBalanceCalc.length; ++paramIndex) {
                     pstmt.setInt(paramIndex, transactionTypesForMinBalanceCalc[i]);
                     ++i;
                  }

                  ResultSet rs = pstmt.executeQuery();
                  if (rs.next()) {
                     value = rs.getDouble(1);
                  }
               } finally {
                  pstmt.close();
               }
            } finally {
               if (createdConnection) {
                  conn.close();
               }

            }
         } catch (SQLException var26) {
            throw new EJBException(var26.getMessage(), var26);
         }
      }

      return value;
   }

   private static class BannedCreditSendersLoader extends LazyLoader<Set<String>> {
      public BannedCreditSendersLoader() {
         super("BannedCreditSendersLoader", 120000L);
      }

      protected Set<String> fetchValue() {
         String[] usernames = SystemProperty.getArray((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.AccountEntry.BANNED_CREDIT_SENDERS);
         return Collections.unmodifiableSet(new HashSet(Arrays.asList(usernames)));
      }
   }

   public static enum AccountTransactionErrorEnum {
      AMOUNT_AND_FUNDED_AMOUNT_HAVE_DIFFERENT_SIGNS(100),
      FUNDED_AMOUNT_GREATER_THAN_TRANSACTION_AMOUNT(101),
      FUNDED_AMOUNT_LESS_THAN_TRANSACTION_AMOUNT(102),
      CREATE_ACCOUNTENTRY_FAILED(103),
      CREATE_ACCOUNTENTRY_SOURCE_FAILED(104),
      UPDATE_SENDER_ACCOUNT_ENTRY_FAILED(201);

      private int code;

      private AccountTransactionErrorEnum(int code) {
         this.code = code;
      }
   }
}
