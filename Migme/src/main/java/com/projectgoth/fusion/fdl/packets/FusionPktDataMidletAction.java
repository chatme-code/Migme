package com.projectgoth.fusion.fdl.packets;

import com.projectgoth.fusion.fdl.enums.PacketType;
import com.projectgoth.fusion.packet.FusionPacket;
import java.util.HashMap;

public class FusionPktDataMidletAction extends FusionPacket {
   public FusionPktDataMidletAction() {
      super(PacketType.MIDLET_ACTION);
   }

   public FusionPktDataMidletAction(short transactionId) {
      super(PacketType.MIDLET_ACTION, transactionId);
   }

   public FusionPktDataMidletAction(FusionPacket packet) {
      super(packet);
   }

   public final FusionPktDataMidletAction.ActionType getAction() {
      return FusionPktDataMidletAction.ActionType.fromValue(this.getIntField((short)1));
   }

   public final void setAction(FusionPktDataMidletAction.ActionType action) {
      this.setField((short)1, action.value());
   }

   public static enum ActionType {
      MAKE_CONTACTLIST_ACTIVE(1),
      USE_DEFAULT_THEME(2),
      CLEAR_EMOTICON_CACHE(3),
      USE_DEFAULT_LANGUAGE(4),
      REFRESH_CONTACT_LIST(5);

      private int value;
      private static final HashMap<Integer, FusionPktDataMidletAction.ActionType> LOOKUP = new HashMap();

      private ActionType(int value) {
         this.value = value;
      }

      public int value() {
         return this.value;
      }

      public static FusionPktDataMidletAction.ActionType fromValue(Integer value) {
         return (FusionPktDataMidletAction.ActionType)LOOKUP.get(value);
      }

      static {
         FusionPktDataMidletAction.ActionType[] arr$ = values();
         int len$ = arr$.length;

         for(int i$ = 0; i$ < len$; ++i$) {
            FusionPktDataMidletAction.ActionType actionType = arr$[i$];
            LOOKUP.put(actionType.value, actionType);
         }

      }
   }
}
