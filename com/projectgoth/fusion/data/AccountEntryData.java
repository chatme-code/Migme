package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AccountEntryData implements Serializable {
   public Long id;
   public String username;
   public Date dateCreated;
   public AccountEntryData.TypeEnum type;
   public String reference;
   public String description;
   public String currency;
   public Double exchangeRate;
   public Double amount;
   public Double fundedAmount;
   public Double costOfGoodsSold;
   public Double costOfTrial;
   public Double tax;
   public Double wholesaleCost;
   public Double runningBalance;

   public AccountEntryData() {
   }

   public AccountEntryData(ResultSet rs) throws SQLException {
      this.id = rs.getLong("id");
      this.username = rs.getString("username");
      this.dateCreated = new Date(rs.getTimestamp("dateCreated").getTime());
      this.type = AccountEntryData.TypeEnum.fromValue(rs.getInt("type"));
      this.reference = rs.getString("reference");
      this.description = rs.getString("description");
      this.currency = rs.getString("currency");
      this.exchangeRate = rs.getDouble("exchangerate");
      this.amount = rs.getDouble("amount");
      this.fundedAmount = rs.getDouble("fundedAmount");
      this.tax = rs.getDouble("tax");
      this.costOfGoodsSold = rs.getDouble("costOfGoodsSold");
      this.costOfTrial = rs.getDouble("costOfTrial");
   }

   public static enum TypeEnum {
      CREDIT_CARD(1),
      VOUCHER_RECHARGE(2),
      SMS_CHARGE(3),
      CALL_CHARGE(4),
      SUBSCRIPTION(5),
      PRODUCT_PURCHASE(6),
      REFERRAL_CREDIT(7),
      ACTIVATION_CREDIT(8),
      BONUS_CREDIT(9),
      REFUND(10),
      PREMIUM_SMS_RECHARGE(11),
      PREMIUM_SMS_FEE(12),
      CREDIT_CARD_REFUND(13),
      USER_TO_USER_TRANSFER(14),
      TELEGRAPHIC_TRANSFER(15),
      CREDIT_CARD_CHARGEBACK(16),
      VOUCHERS_CREATED(17),
      VOUCHERS_CANCELLED(18),
      CURRENCY_CONVERSION(19),
      SYSTEM_SMS_CHARGE(20),
      BANK_TRANSFER(21),
      BANK_TRANSFER_REVERSAL(22),
      CHATROOM_KICK_CHARGE(23),
      CREDIT_EXPIRED(24),
      WESTERN_UNION(25),
      WESTERN_UNION_REVERSAL(26),
      EMOTICON_PURCHASE(27),
      CONTENT_ITEM_PURCHASE(28),
      CONTENT_ITEM_REFUND(29),
      DISCOUNT_TIER_ADJUSTMENT(30),
      SUBSCRIPTION_CREDIT(31),
      AVATAR_PURCHASE(32),
      GAME_ITEM_PURCHASE(33),
      GAME_REWARD(34),
      MARKETING_REWARD(35),
      MERCHANT_REVENUE_TRAIL(36),
      THIRD_PARTY_API_DEBIT(37),
      BLUE_LABEL_ONE_VOUCHER(40),
      VIRTUAL_GIFT_PURCHASE(41),
      BOT_START(42),
      GAME_START(43),
      GAME_START_REVERSAL(44),
      GAME_JOIN(45),
      GAME_JOIN_REVERSAL(46),
      POT_ENTRY(47),
      POT_PAYOUT(48),
      POT_ENTRY_REVERSAL(49),
      MERCHANT_GAME_TRAIL(50),
      MERCHANT_THIRD_PARTY_APP_TRAIL(51),
      CREDIT_WRITE_OFF(52),
      EMOTE_PURCHASE(53),
      USSD_PARTNER_PURCHASE(54),
      USSD_PARTNER_BONUS(55),
      USSD_PARTNER_REDEMPTION(56),
      THIRD_PARTY_PAYMENT(57),
      TRANSFER_CREDIT_FEE(58),
      TRANSFER_CREDIT_FEE_TO_TAGGER(59),
      ARCHIVE_SUMMARY(91),
      MANUAL(99),
      SYSTEM_ERROR_REVERSAL(100),
      USER_BALANCE_CORRECTION(101),
      DEDUCT_UNFUNDED_BALANCE(102);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static AccountEntryData.TypeEnum fromValue(int value) {
         AccountEntryData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            AccountEntryData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
