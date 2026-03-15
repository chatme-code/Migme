package com.projectgoth.fusion.smsengine;

public enum SMPPID {
   UNKNOWN(0),
   GENERIC_NACK(Integer.MIN_VALUE),
   BIND_RECEIVER(1),
   BIND_RECEIVER_RESP(-2147483647),
   BIND_TRANSMITTER(2),
   BIND_TRANSMITTER_RESP(-2147483646),
   QUERY_SM(3),
   QUERY_SM_RESP(-2147483645),
   SUBMIT_SM(4),
   SUBMIT_SM_RESP(-2147483644),
   DELIVER_SM(5),
   DELIVER_SM_RESP(-2147483643),
   UNBIND(6),
   UNBIND_RESP(-2147483642),
   REPLACE_SM(7),
   REPLACE_SM_RESP(-2147483641),
   CANCEL_SM(8),
   CANCEL_SM_RESP(-2147483640),
   BIND_TRANSCEIVER(9),
   BIND_TRANSCEIVER_RESP(-2147483639),
   OUTBIND(11),
   ENQUIRE_LINK(21),
   ENQUIRE_LINK_RESP(-2147483627),
   SUBMIT_MULTI(33),
   SUBMIT_MULTI_RESP(-2147483615),
   ALERT_NOTIFICATION(258),
   DATA_SM(259),
   DATA_SM_RESP(-2147483389);

   private int value;

   private SMPPID(int value) {
      this.value = value;
   }

   public int value() {
      return this.value;
   }

   public boolean isResponse() {
      return (this.value & Integer.MIN_VALUE) == Integer.MIN_VALUE;
   }

   public static SMPPID fromValue(int value) {
      SMPPID[] arr$ = values();
      int len$ = arr$.length;

      for(int i$ = 0; i$ < len$; ++i$) {
         SMPPID e = arr$[i$];
         if (e.value() == value) {
            return e;
         }
      }

      return UNKNOWN;
   }
}
