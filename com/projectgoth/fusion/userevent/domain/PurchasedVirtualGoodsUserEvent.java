package com.projectgoth.fusion.userevent.domain;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.slice.PurchasedVirtualGoodsUserEventIce;
import com.projectgoth.fusion.slice.UserEventIce;
import com.sleepycat.persist.model.Persistent;
import java.util.Map;
import org.apache.log4j.Logger;

@Persistent
public class PurchasedVirtualGoodsUserEvent extends UserEvent {
   public static final String EVENT_NAME = "PURCHASED_VIRTUAL_GOODS";
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(PurchasedVirtualGoodsUserEvent.class));
   private byte itemType;
   private int itemId;
   private String itemName;

   public PurchasedVirtualGoodsUserEvent() {
   }

   public PurchasedVirtualGoodsUserEvent(UserEvent event, byte itemType, int itemId, String itemName) {
      super(event);
      this.itemType = itemType;
      this.itemId = itemId;
      this.itemName = itemName;
   }

   public PurchasedVirtualGoodsUserEvent(PurchasedVirtualGoodsUserEventIce event) {
      super((UserEventIce)event);
      this.itemId = event.itemId;
      this.itemName = event.itemName;
      this.itemType = event.itemType;
   }

   public byte getItemType() {
      return this.itemType;
   }

   public void setItemType(byte itemType) {
      this.itemType = itemType;
   }

   public int getItemId() {
      return this.itemId;
   }

   public void setItemId(int itemId) {
      this.itemId = itemId;
   }

   public String getItemName() {
      return this.itemName;
   }

   public void setItemName(String itemName) {
      this.itemName = itemName;
   }

   public PurchasedVirtualGoodsUserEventIce toIceEvent() {
      if (log.isDebugEnabled()) {
         log.debug("creating " + this.getClass().getName());
      }

      PurchasedVirtualGoodsUserEventIce iceEvent = new PurchasedVirtualGoodsUserEventIce(this.getTimestamp(), this.getGeneratingUsername(), (String)null, this.getText(), this.itemType, this.itemId, this.itemName);
      return iceEvent;
   }

   public String toString() {
      StringBuffer buffer = new StringBuffer(super.toString());
      buffer.append(" itemId [").append(this.itemId).append("]");
      buffer.append(" itemName [").append(this.itemName).append("]");
      buffer.append(" itemType [").append(VirtualGoodType.fromValue(this.itemType)).append("]");
      return buffer.toString();
   }

   public static Map<String, String> findSubstitutionParameters(PurchasedVirtualGoodsUserEventIce event) {
      Map<String, String> map = UserEvent.findSubstitutionParameters(event);
      map.put("itemid", Integer.toString(event.itemId));
      map.put("itemname", event.itemName);
      map.put("itemtype", VirtualGoodType.fromValue(event.itemType).name());
      return map;
   }
}
