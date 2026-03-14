package com.projectgoth.fusion.paintwars;

import java.io.Serializable;

public class ItemData implements Serializable {
   public static final String ID = "ID";
   public static final String NAME = "Name";
   public static final String DESCRIPTION = "Description";
   public static final String CURRENCY = "Currency";
   public static final String PRICE = "Price";
   private int id = 0;
   private String name = "";
   private String description = "";
   private String currency = "";
   private double price = 0.0D;

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getName() {
      return this.name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public String getCurrency() {
      return this.currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public double getPrice() {
      return this.price;
   }

   public void setPrice(double price) {
      this.price = price;
   }
}
