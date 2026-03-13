package com.projectgoth.fusion.data;

import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.Numerics;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Map.Entry;

public class VirtualGiftData extends ReferenceStoreItemData implements Serializable {
   private Integer id;
   private String name;
   private String hotKey;
   private Double price;
   private String currency;
   private Integer numAvailable;
   private Integer numSold;
   private Integer sortOrder;
   private Integer groupID;
   private boolean groupVIPOnly;
   private VirtualGiftData.StatusEnum status;
   private String giftAllMessage;
   private StoreCategoryData storeCategory = null;
   private StoreRatingSummaryData storeRatingSummary = null;
   private int migLevelMin = -1;
   private int storeItemId;
   private final Map<String, SortedMap<Short, String>> imageLocations = new HashMap();

   public int getStoreitemId() {
      return this.storeItemId;
   }

   public void setStoreitemId(int storeItemId) {
      this.storeItemId = storeItemId;
   }

   public void setImageLocation(String imageFormatType, short resolution, String location) {
      String normalizedImageFormatType = ContentUtils.normalizeImageFormatType(imageFormatType);
      if (location == null) {
         SortedMap<Short, String> resolutionToLocation = (SortedMap)this.imageLocations.get(normalizedImageFormatType);
         if (resolutionToLocation != null) {
            resolutionToLocation.remove(resolution);
            if (resolutionToLocation.size() == 0) {
               this.imageLocations.remove(normalizedImageFormatType);
            }
         }
      } else {
         SortedMap<Short, String> resolutionToLocation = (SortedMap)this.imageLocations.get(normalizedImageFormatType);
         if (resolutionToLocation == null) {
            resolutionToLocation = new TreeMap();
            this.imageLocations.put(normalizedImageFormatType, resolutionToLocation);
         }

         ((SortedMap)resolutionToLocation).put(resolution, location);
      }

   }

   public String getImageLocation(String imageFormatType, short resolution) {
      String normalizedImageFormatType = ContentUtils.normalizeImageFormatType(imageFormatType);
      Map<Short, String> resolutionToLocation = (Map)this.imageLocations.get(normalizedImageFormatType);
      return resolutionToLocation != null ? (String)resolutionToLocation.get(resolution) : null;
   }

   public String getImageLocation(VirtualGiftData.ImageFormatType imageFormatType, short resolution) {
      return this.getImageLocation(imageFormatType.getCode(), resolution);
   }

   public void setImageLocation(VirtualGiftData.ImageFormatType imageFormatType, short resolution, String location) {
      this.setImageLocation(imageFormatType.getCode(), resolution, location);
   }

   public SortedMap<Short, String> getAvailableImageLocations(String imageFormatType) {
      return (SortedMap)this.imageLocations.get(ContentUtils.normalizeImageFormatType(imageFormatType));
   }

   public SortedMap<Short, String> getAvailableImageLocations(VirtualGiftData.ImageFormatType imageFormatType) {
      return this.getAvailableImageLocations(imageFormatType.getCode());
   }

   public String toString() {
      return this.id.toString();
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

   public String getHotKey() {
      return this.hotKey;
   }

   public void setHotKey(String hotKey) {
      this.hotKey = hotKey;
   }

   public Double getRoundedPrice() {
      return this.price == null ? null : -1.0D * Numerics.floor(-1.0D * this.price, 2);
   }

   public Double getPrice() {
      return this.price;
   }

   public void setPrice(Double price) {
      this.price = price;
   }

   public String getCurrency() {
      return this.currency;
   }

   public void setCurrency(String currency) {
      this.currency = currency;
   }

   public Integer getNumAvailable() {
      return this.numAvailable;
   }

   public void setNumAvailable(Integer numAvailable) {
      this.numAvailable = numAvailable;
   }

   public Integer getNumSold() {
      return this.numSold;
   }

   public void setNumSold(Integer numSold) {
      this.numSold = numSold;
   }

   public Integer getSortOrder() {
      return this.sortOrder;
   }

   public void setSortOrder(Integer sortOrder) {
      this.sortOrder = sortOrder;
   }

   public String getLocation12x12GIF() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.GIF, (short)12);
   }

   public void setLocation12x12GIF(String location12x12GIF) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.GIF, (short)12, location12x12GIF);
   }

   public String getLocation12x12PNG() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)12);
   }

   public void setLocation12x12PNG(String location12x12PNG) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)12, location12x12PNG);
   }

   public String getLocation14x14GIF() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.GIF, (short)14);
   }

   public void setLocation14x14GIF(String location14x14GIF) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.GIF, (short)14, location14x14GIF);
   }

   public String getLocation14x14PNG() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)14);
   }

   public void setLocation14x14PNG(String location14x14PNG) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)14, location14x14PNG);
   }

   public String getLocation16x16GIF() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.GIF, (short)16);
   }

   public void setLocation16x16GIF(String location16x16GIF) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.GIF, (short)16, location16x16GIF);
   }

   public String getLocation16x16PNG() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)16);
   }

   public void setLocation16x16PNG(String location16x16PNG) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)16, location16x16PNG);
   }

   public String getLocation64x64PNG() {
      return this.getImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)64);
   }

   public void setLocation64x64PNG(String location64x64PNG) {
      this.setImageLocation((VirtualGiftData.ImageFormatType)VirtualGiftData.ImageFormatType.PNG, (short)64, location64x64PNG);
   }

   public VirtualGiftData.StatusEnum getStatus() {
      return this.status;
   }

   public void setStatus(VirtualGiftData.StatusEnum status) {
      this.status = status;
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

   public void setStoreCategory(StoreCategoryData cat) {
      this.storeCategory = cat;
   }

   public StoreCategoryData getStoreCategory() {
      return this.storeCategory;
   }

   public void setStoreRatingSummary(StoreRatingSummaryData summary) {
      this.storeRatingSummary = summary;
   }

   public StoreRatingSummaryData getStoreRatingSummary() {
      return this.storeRatingSummary;
   }

   public int getMigLevelMin() {
      return this.migLevelMin;
   }

   public void setMigLevelMin(int minLevel) {
      this.migLevelMin = minLevel;
   }

   public String getGiftAllMessage() {
      return this.giftAllMessage;
   }

   public VirtualGiftData() {
   }

   public VirtualGiftData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("ID");
      this.name = rs.getString("Name");
      this.hotKey = rs.getString("HotKey");
      this.price = rs.getDouble("Price");
      this.currency = rs.getString("Currency");
      this.numAvailable = rs.getInt("NumAvailable");
      this.numSold = rs.getInt("NumSold");
      this.sortOrder = rs.getInt("SortOrder");
      this.groupID = rs.getInt("GroupID");
      this.groupVIPOnly = rs.getBoolean("GroupVIPOnly");
      this.status = VirtualGiftData.StatusEnum.fromValue(rs.getInt("Status"));

      try {
         this.giftAllMessage = rs.getString("GiftAllMessage");
      } catch (SQLException var17) {
      }

      try {
         this.migLevelMin = rs.getInt("migLevelMin");
      } catch (SQLException var16) {
      }

      try {
         this.storeItemId = rs.getInt("storeitemid");
      } catch (SQLException var15) {
      }

      Entry[] arr$ = ContentUtils.getSupportedVirtualGiftResolutions();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         Entry<String, short[]> imageTypeResolution = arr$[i$];
         String imgFormat = (String)imageTypeResolution.getKey();
         short[] resolutions = (short[])imageTypeResolution.getValue();
         short[] arr$ = resolutions;
         int len$ = resolutions.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            short resolution = arr$[i$];
            String resultSetColumn = this.getResultSetColumn(imgFormat, resolution);

            try {
               String location = rs.getString(resultSetColumn);
               if (StringUtil.isBlank(location)) {
                  this.setImageLocation((String)imgFormat, resolution, (String)null);
               } else {
                  this.setImageLocation(imgFormat, resolution, location);
               }
            } catch (SQLException var14) {
               this.setImageLocation((String)imgFormat, resolution, (String)null);
            }
         }
      }

   }

   private String getResultSetColumn(String imageFormatType, int resolution) {
      return (new StringBuilder(30)).append("Location").append(resolution).append('x').append(resolution).append(ContentUtils.normalizeImageFormatType(imageFormatType)).toString();
   }

   public String getGiftUrl(VirtualGiftData.ImageFormatType imageFormatType, short resolution) {
      String path = this.getImageLocation(imageFormatType, resolution);
      return StringUtil.isBlank(path) ? "" : String.format("%s/%s", SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.MIG33_WEB_BASE_URL), path);
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

      public static VirtualGiftData.StatusEnum fromValue(int value) {
         VirtualGiftData.StatusEnum[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            VirtualGiftData.StatusEnum e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum GiftingType {
      GIFT(0),
      GIFT_SHOWER(1);

      private int value;

      private GiftingType(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static VirtualGiftData.GiftingType fromValue(int value) {
         VirtualGiftData.GiftingType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            VirtualGiftData.GiftingType e = arr$[i$];
            if (e.value() == value) {
               return e;
            }
         }

         return null;
      }
   }

   public static enum ImageFormatType {
      PNG("PNG"),
      GIF("GIF");

      private String code;

      private ImageFormatType(String code) {
         this.code = code;
      }

      public String getCode() {
         return this.code;
      }
   }
}
