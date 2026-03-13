package com.projectgoth.fusion.merchant;

import com.projectgoth.fusion.common.EnumUtils;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

public class MerchantPointsLogData implements Serializable {
   private long id;
   private final Date dateCreated;
   private final int userid;
   private final int points;
   private final MerchantPointsLogData.EntryTypeEnum type;

   public MerchantPointsLogData(MerchantPointsLogData.EntryTypeEnum type, Date dateCreated, int userid, int points) {
      this.dateCreated = dateCreated;
      this.userid = userid;
      this.points = points;
      this.type = type;
   }

   public void setId(long id) {
      this.id = id;
   }

   public long getId() {
      return this.id;
   }

   public Date getDateCreated() {
      return this.dateCreated;
   }

   public int getUserid() {
      return this.userid;
   }

   public int getPoints() {
      return this.points;
   }

   public MerchantPointsLogData.EntryTypeEnum getType() {
      return this.type;
   }

   public static enum EntryTypeEnum implements EnumUtils.IEnumValueGetter<Short> {
      MANUAL_ADJUSTMENT(1),
      MECHANIC_REWARD(2);

      private short value;
      private static final HashMap<Short, MerchantPointsLogData.EntryTypeEnum> lookupByCode = new HashMap();

      private EntryTypeEnum(int value) {
         this.value = (short)value;
      }

      public short code() {
         return this.value;
      }

      public static MerchantPointsLogData.EntryTypeEnum fromCode(int code) {
         return (MerchantPointsLogData.EntryTypeEnum)lookupByCode.get(code);
      }

      public Short getEnumValue() {
         return this.value;
      }

      static {
         EnumUtils.populateLookUpMap(lookupByCode, MerchantPointsLogData.EntryTypeEnum.class);
      }
   }
}
