package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EmoticonPackData extends ReferenceStoreItemData implements Serializable {
   private static final long serialVersionUID = 3270071183111285012L;
   private Integer id;
   private EmoticonPackData.TypeEnum type;
   private String name;
   private String description;
   private Double price;
   private Integer serviceID;
   private Integer groupID;
   private boolean groupVIPOnly;
   private Integer sortOrder;
   private boolean forSale;
   private EmoticonPackData.StatusEnum status;
   private List<Integer> emoticonIDs = new ArrayList();
   private EmoticonPackData.ContentTypeEnum contentType;
   private Integer version;
   private String thumbnailFile;
   private String catalogImage;

   public String getCatalogImage() {
      return this.catalogImage;
   }

   public void setCatalogImage(String catalogImage) {
      this.catalogImage = catalogImage;
   }

   public String getThumbnailFile() {
      return this.thumbnailFile;
   }

   public void setThumbnailFile(String thumbnailURL) {
      this.thumbnailFile = thumbnailURL;
   }

   public Integer getVersion() {
      return this.version;
   }

   public void setVersion(Integer version) {
      this.version = version;
   }

   public EmoticonPackData.ContentTypeEnum getContentType() {
      return this.contentType;
   }

   public void setContentType(EmoticonPackData.ContentTypeEnum contentType) {
      this.contentType = contentType;
   }

   public Integer getId() {
      return this.id;
   }

   public void setId(Integer id) {
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

   public Double getPrice() {
      return this.price;
   }

   public void setPrice(Double price) {
      this.price = price;
   }

   public EmoticonPackData.StatusEnum getStatus() {
      return this.status;
   }

   public void setStatus(EmoticonPackData.StatusEnum status) {
      this.status = status;
   }

   public List<Integer> getEmoticonIDs() {
      return this.emoticonIDs;
   }

   public void addEmoticonID(Integer emoticonID) {
      this.emoticonIDs.add(emoticonID);
   }

   public Integer getGroupID() {
      return this.groupID;
   }

   public void setGroupID(Integer groupID) {
      this.groupID = groupID;
   }

   public boolean isGroupVIPOnly() {
      return this.groupVIPOnly;
   }

   public void setGroupVIPOnly(boolean groupVIPOnly) {
      this.groupVIPOnly = groupVIPOnly;
   }

   public Integer getServiceID() {
      return this.serviceID;
   }

   public void setServiceID(Integer serviceID) {
      this.serviceID = serviceID;
   }

   public Integer getSortOrder() {
      return this.sortOrder;
   }

   public void setSortOrder(Integer sortOrder) {
      this.sortOrder = sortOrder;
   }

   public boolean isForSale() {
      return this.forSale;
   }

   public void setForSale(boolean forSale) {
      this.forSale = forSale;
   }

   public EmoticonPackData.TypeEnum getType() {
      return this.type;
   }

   public void setType(EmoticonPackData.TypeEnum type) {
      this.type = type;
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

      public static EmoticonPackData.StatusEnum fromValue(int value) {
         EmoticonPackData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            EmoticonPackData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ContentTypeEnum {
      EMOTICON(1),
      STICKER(2);

      private int value;

      private ContentTypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static EmoticonPackData.ContentTypeEnum fromValue(int value) {
         EmoticonPackData.ContentTypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            EmoticonPackData.ContentTypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum TypeEnum {
      FREE(1),
      PREMIUM_PURCHASE(2),
      PREMIUM_SUBSCRIPTION(3);

      private int value;

      private TypeEnum(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static EmoticonPackData.TypeEnum fromValue(int value) {
         EmoticonPackData.TypeEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            EmoticonPackData.TypeEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
