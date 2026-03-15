package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class AvatarItemData extends ReferenceStoreItemData implements Serializable {
   private Integer id;
   private String name;
   private String description;
   private String previewImage;
   private String image;
   private Integer type;
   private Integer zOrder;
   private Integer categoryID;
   private Integer usedOnBody;
   private Integer status;
   private Integer ownershipRequired;
   private Date dateListed;

   public AvatarItemData() {
   }

   public AvatarItemData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.name = rs.getString("name");
      this.description = rs.getString("description");
      this.previewImage = rs.getString("previewImage");
      this.image = rs.getString("image");
      this.description = rs.getString("description");
      this.type = rs.getInt("type");
      this.zOrder = rs.getInt("zOrder");
      this.categoryID = rs.getInt("categoryID");
      this.usedOnBody = rs.getInt("usedOnBody");
      this.status = rs.getInt("status");
      this.ownershipRequired = rs.getInt("ownershipRequired");
      this.dateListed = rs.getDate("dateListed");
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

   public String getPreviewImage() {
      return this.previewImage;
   }

   public void setPreviewImage(String previewImage) {
      this.previewImage = previewImage;
   }

   public String getImage() {
      return this.image;
   }

   public void setImage(String image) {
      this.image = image;
   }

   public Integer getType() {
      return this.type;
   }

   public void setType(Integer type) {
      this.type = type;
   }

   public Integer getzOrder() {
      return this.zOrder;
   }

   public void setzOrder(Integer zOrder) {
      this.zOrder = zOrder;
   }

   public Integer getCategoryID() {
      return this.categoryID;
   }

   public void setCategoryID(Integer categoryID) {
      this.categoryID = categoryID;
   }

   public Integer getUsedOnBody() {
      return this.usedOnBody;
   }

   public void setUsedOnBody(Integer usedOnBody) {
      this.usedOnBody = usedOnBody;
   }

   public Integer getStatus() {
      return this.status;
   }

   public void setStatus(Integer status) {
      this.status = status;
   }

   public Integer getOwnershipRequired() {
      return this.ownershipRequired;
   }

   public void setOwnershipRequired(Integer ownershipRequired) {
      this.ownershipRequired = ownershipRequired;
   }

   public Date getDateListed() {
      return this.dateListed;
   }

   public void setDateListed(Date dateListed) {
      this.dateListed = dateListed;
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

      public static AvatarItemData.StatusEnum fromValue(int value) {
         AvatarItemData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            AvatarItemData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }
}
