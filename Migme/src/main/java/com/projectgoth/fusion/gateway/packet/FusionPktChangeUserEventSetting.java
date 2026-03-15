package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserEventIce;
import com.projectgoth.fusion.userevent.domain.ShortTextStatusUserEvent;

public class FusionPktChangeUserEventSetting extends FusionRequest {
   public FusionPktChangeUserEventSetting() {
      super((short)921);
   }

   public FusionPktChangeUserEventSetting(short transactionId) {
      super((short)921, transactionId);
   }

   public FusionPktChangeUserEventSetting(FusionPacket packet) {
      super(packet);
   }

   public Byte getAllEvents() {
      return this.getByteField((short)1);
   }

   public void setAllEvents(byte allEvents) {
      this.setField((short)1, allEvents);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      Byte allEvents = this.getAllEvents();
      if (allEvents != null && allEvents == 1) {
         try {
            if (SystemProperty.getBool((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_ENABLED) && connection.findEventSystem() != null) {
               connection.findEventSystem().streamEventsToLoggingInUser(connection.getUsername(), connection.getConnectionPrx());
            } else {
               ShortTextStatusUserEvent event = new ShortTextStatusUserEvent();
               event.setGeneratingUsername("migme");
               event.setStatus(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Default.EVENT_SYSTEM_WELCOME_MESSAGE));
               event.setTimestamp(System.currentTimeMillis());
               UserEventIce eventIce = event.toIceEvent();
               connection.putEvent(eventIce);
            }
         } catch (FusionException var5) {
            return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to change user event setting - " + var5.message)};
         } catch (Exception var6) {
            return new FusionPacket[]{new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to change user event setting - " + var6.getMessage())};
         }
      }

      return new FusionPacket[]{new FusionPktOk(this.transactionId)};
   }
}
