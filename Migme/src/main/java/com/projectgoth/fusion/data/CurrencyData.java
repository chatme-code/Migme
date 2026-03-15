package com.projectgoth.fusion.data;

import com.projectgoth.fusion.slice.CurrencyDataIce;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

public class CurrencyData implements Serializable {
   public String code;
   public String name;
   public String symbol;
   public Double exchangeRate;
   public Date lastUpdated;
   public static String baseCurrency = "AUD";

   public CurrencyData() {
   }

   public CurrencyData(ResultSet rs) throws SQLException {
      this.code = rs.getString("code");
      this.name = rs.getString("name");
      this.symbol = rs.getString("symbol");
      this.exchangeRate = rs.getDouble("exchangeRate");
      this.lastUpdated = rs.getDate("lastupdated");
   }

   public CurrencyData(CurrencyDataIce currencyIce) {
      this.code = currencyIce.code.equals("\u0000") ? null : currencyIce.code;
      this.name = currencyIce.name.equals("\u0000") ? null : currencyIce.name;
      this.symbol = currencyIce.symbol.equals("\u0000") ? null : currencyIce.symbol;
      this.exchangeRate = currencyIce.exchangeRate == Double.MIN_VALUE ? null : currencyIce.exchangeRate;
      this.lastUpdated = currencyIce.lastUpdated == Long.MIN_VALUE ? null : new Date(currencyIce.lastUpdated);
   }

   public CurrencyDataIce toIceObject() {
      CurrencyDataIce currencyIce = new CurrencyDataIce();
      currencyIce.code = this.code == null ? "\u0000" : this.code;
      currencyIce.name = this.name == null ? "\u0000" : this.name;
      currencyIce.symbol = this.symbol == null ? "\u0000" : this.symbol;
      currencyIce.exchangeRate = this.exchangeRate == null ? Double.MIN_VALUE : this.exchangeRate;
      currencyIce.lastUpdated = this.lastUpdated == null ? Long.MIN_VALUE : this.lastUpdated.getTime();
      return currencyIce;
   }

   public boolean isBaseCurrency() {
      return baseCurrency.equalsIgnoreCase(this.code);
   }

   public double convert(double amount) {
      return this.convertFromBaseCurrency(amount);
   }

   public double convertFromBaseCurrency(double amount) {
      return amount * this.exchangeRate;
   }

   public double convertFrom(double amount, CurrencyData currency) {
      return amount / currency.exchangeRate * this.exchangeRate;
   }

   public double convertToBaseCurrency(double amount) {
      return amount / this.exchangeRate;
   }

   public double convertTo(double amount, CurrencyData currency) {
      return currency.convertFrom(amount, this);
   }

   public String format(double amount) {
      DecimalFormat df = new DecimalFormat("0.00");
      return this.symbol == null ? df.format(amount) + " " + this.code : this.symbol + df.format(amount);
   }

   public String formatWithCode(double amount) {
      DecimalFormat df = new DecimalFormat("0.00 ");
      return df.format(amount) + this.code;
   }

   public String formatBaseCurrency(double amount) {
      return this.isBaseCurrency() ? this.format(amount) : (new DecimalFormat("0.00 ")).format(amount) + baseCurrency + " (approx. " + this.format(this.convertFromBaseCurrency(amount)) + ")";
   }

   public String formatExchangeRate() {
      return "1 " + baseCurrency + " = Approx. " + this.format(this.convertFromBaseCurrency(1.0D));
   }
}
