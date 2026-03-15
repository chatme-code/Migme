package com.projectgoth.fusion.data;

import com.projectgoth.leto.common.utils.enums.IEnumValueGetter;
import com.projectgoth.leto.common.utils.enums.ValueToEnumMap;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonValue;

public class CampaignData implements Serializable {
   private static final long serialVersionUID = 3967651652469541912L;
   private int id;
   private String name;
   private CampaignData.TypeEnum type;
   private String description;
   private boolean status;
   private Date startDate;
   private Date endDate;

   public CampaignData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.type = CampaignData.TypeEnum.fromValue(rs.getInt("type"));
      this.description = rs.getString("description");
      this.name = rs.getString("name");
      this.status = rs.getBoolean("status");
      this.startDate = rs.getTimestamp("startDate");
      this.endDate = rs.getTimestamp("endDate");
   }

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

   public boolean isStatus() {
      return this.status;
   }

   public void setStatus(boolean status) {
      this.status = status;
   }

   public Date getStartDate() {
      return this.startDate;
   }

   public void setStartDate(Date startDate) {
      this.startDate = startDate;
   }

   public Date getEndDate() {
      return this.endDate;
   }

   public void setEndDate(Date endDate) {
      this.endDate = endDate;
   }

   public CampaignData.TypeEnum getType() {
      return this.type;
   }

   public void setType(CampaignData.TypeEnum type) {
      this.type = type;
   }

   public String getDescription() {
      return this.description;
   }

   public void setDescription(String description) {
      this.description = description;
   }

   public boolean isActive() {
      Date currentDate = Calendar.getInstance().getTime();
      return this.isStatus() && this.getStartDate().compareTo(currentDate) < 0 && this.getEndDate().compareTo(currentDate) > 0;
   }

   public static enum TypeEnum implements IEnumValueGetter<Integer> {
      OTHERS(0),
      INVITE_FRIENDS_TO_SIGN_UP(43);

      private final Integer value;

      private TypeEnum(int v) {
         this.value = v;
      }

      @JsonValue
      public Integer getEnumValue() {
         return this.value;
      }

      @JsonCreator
      public static CampaignData.TypeEnum fromValue(Integer value) {
         return (CampaignData.TypeEnum)CampaignData.TypeEnum.ValueToEnumMapInstance.INSTANCE.toEnum(value);
      }

      private static final class ValueToEnumMapInstance {
         private static final ValueToEnumMap<Integer, CampaignData.TypeEnum> INSTANCE = new ValueToEnumMap(CampaignData.TypeEnum.class);
      }
   }
}
