package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.Numerics;
import com.projectgoth.leto.common.storeitem.StoreItemType;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class StoreItemData implements Serializable {
   public Integer id;
   public StoreItemData.TypeEnum type;
   public Integer referenceID;
   public String name;
   public String description;
   public Double price;
   public String currency;
   public Integer numAvailable;
   public Integer numSold;
   public Boolean featured;
   public Boolean forSale;
   public Date expiryDate;
   public String catalogImage;
   public String previewImage;
   public Date dateListed;
   public Integer migLevelMin;
   public Integer groupId;
   public StoreItemData.StatusEnum status;
   public Integer storeCategoryID;
   public Integer parentStoreCategoryID;
   public Double localPrice;
   public String localCurrency;
   public String groupName;
   public boolean isGroupMember;
   public ReferenceStoreItemData referenceData;

   public StoreItemData() {
   }

   public StoreItemData(ResultSet rs) throws SQLException {
      this.id = (Integer)rs.getObject("id");
      this.referenceID = (Integer)rs.getObject("referenceID");
      this.name = rs.getString("name");
      this.description = rs.getString("description");
      this.price = (Double)rs.getObject("price");
      this.currency = rs.getString("currency");
      this.numAvailable = (Integer)rs.getObject("numAvailable");
      this.numSold = (Integer)rs.getObject("numSold");
      this.expiryDate = rs.getTimestamp("expiryDate");
      this.catalogImage = rs.getString("catalogImage");
      this.previewImage = rs.getString("previewImage");
      this.dateListed = rs.getTimestamp("dateListed");
      this.migLevelMin = (Integer)rs.getObject("migLevelMin");
      this.groupId = (Integer)rs.getObject("groupId");
      Integer intVal = (Integer)rs.getObject("type");
      if (intVal != null) {
         this.type = StoreItemData.TypeEnum.fromValue(intVal);
      }

      intVal = (Integer)rs.getObject("featured");
      if (intVal != null) {
         this.featured = intVal != 0;
      }

      intVal = (Integer)rs.getObject("forSale");
      if (intVal != null) {
         this.forSale = intVal != 0;
      }

      intVal = (Integer)rs.getObject("status");
      if (intVal != null) {
         this.status = StoreItemData.StatusEnum.fromValue(intVal);
      }

   }

   public void roundLocalPrice() {
      if (this.localPrice != null) {
         this.localPrice = Numerics.ceil(this.localPrice, 2);
      }

   }

   public static enum StatusEnum {
      INACTIVE(0),
      ACTIVE(1);

      private int value;

      private StatusEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static StoreItemData.StatusEnum fromValue(int value) {
         StoreItemData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            StoreItemData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      VIRTUAL_GIFT(StoreItemType.VIRTUAL_GIFT),
      AVATAR(StoreItemType.AVATAR),
      EMOTICON(StoreItemType.EMOTICON),
      SUPER_EMOTICON(StoreItemType.SUPER_EMOTICON),
      THEME(StoreItemType.THEME),
      STICKER(StoreItemType.STICKER);

      private StoreItemType storeItemType;

      private TypeEnum(StoreItemType storeItemType) {
         this.storeItemType = storeItemType;
      }

      public int value() {
         return this.storeItemType.getEnumValue();
      }

      public static StoreItemData.TypeEnum fromValue(int value) {
         StoreItemData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            StoreItemData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }

      public StoreItemType toStoreItemType() {
         return this.storeItemType;
      }
   }
}
