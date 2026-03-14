package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

public abstract class FusionPktDataUploadAddressBookContacts extends FusionRequest {
   public FusionPktDataUploadAddressBookContacts() {
      super(PacketType.UPLOAD_ADDRESS_BOOK_CONTACTS);
   }

   public FusionPktDataUploadAddressBookContacts(short transactionId) {
      super(PacketType.UPLOAD_ADDRESS_BOOK_CONTACTS, transactionId);
   }

   public FusionPktDataUploadAddressBookContacts(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktDataUploadAddressBookContacts(FusionPacket packet) {
      super(packet);
   }

   public final boolean sessionRequired() {
      return true;
   }

   public final FusionPktDataUploadAddressBookContacts.DataType getDataType() {
      return FusionPktDataUploadAddressBookContacts.DataType.fromValue(this.getByteField((short)1));
   }

   public final void setDataType(FusionPktDataUploadAddressBookContacts.DataType dataType) {
      this.setField((short)1, dataType.value());
   }

   public final String[] getDataList() {
      return this.getStringArrayField((short)2);
   }

   public final void setDataList(String[] dataList) {
      this.setField((short)2, dataList);
   }

   public final String getTicketId() {
      return this.getStringField((short)3);
   }

   public final void setTicketId(String ticketId) {
      this.setField((short)3, ticketId);
   }

   public static enum DataType {
      PHONE_NUMBER((byte)1),
      EMAIL_ADDRESS((byte)2);

      private byte value;
      private static final HashMap<Byte, FusionPktDataUploadAddressBookContacts.DataType> LOOKUP = new HashMap();

      private DataType(byte value) {
         this.value = value;
      }

      public byte value() {
         return this.value;
      }

      public static FusionPktDataUploadAddressBookContacts.DataType fromValue(int value) {
         return (FusionPktDataUploadAddressBookContacts.DataType)LOOKUP.get((byte)value);
      }

      public static FusionPktDataUploadAddressBookContacts.DataType fromValue(Byte value) {
         return (FusionPktDataUploadAddressBookContacts.DataType)LOOKUP.get(value);
      }

      static {
         FusionPktDataUploadAddressBookContacts.DataType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktDataUploadAddressBookContacts.DataType dataType = arr$[i$];
            LOOKUP.put(dataType.value, dataType);
         }

      }
   }
}
