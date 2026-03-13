package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.fdl.enums.ImType;
import com.projectgoth.fusion.fdl.enums.PresenceType;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.UserPrx;
import com.projectgoth.fusion.slice.UserPrxHelper;
import org.apache.log4j.Logger;

public class FusionPktIMLoginOld extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktIMLoginOld.class));

   public FusionPktIMLoginOld() {
      super((short)206);
   }

   public FusionPktIMLoginOld(short transactionId) {
      super((short)206, transactionId);
   }

   public FusionPktIMLoginOld(FusionPacket packet) {
      super(packet);
   }

   public Byte getIMType() {
      return this.getByteField((short)1);
   }

   public void setIMType(byte imType) {
      this.setField((short)1, imType);
   }

   public Byte getInitialPresence() {
      return this.getByteField((short)2);
   }

   public void setInitialPresence(byte initialPresence) {
      this.setField((short)2, initialPresence);
   }

   public Byte getShowOfflineContacts() {
      return this.getByteField((short)3);
   }

   public void setShowOfflineContacts(byte showOfflineContacts) {
      this.setField((short)3, showOfflineContacts);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      ImType imType = null;
      UserPrx userPrx = null;

      FusionPktError pktError;
      try {
         Byte byteVal = this.getIMType();
         if (byteVal == null) {
            throw new Exception("No IM type specified");
         } else {
            imType = ImType.fromValue(byteVal);
            if (imType == null) {
               throw new Exception("Unsupported IM type " + byteVal);
            } else {
               byteVal = this.getInitialPresence();
               PresenceType presence = null;
               if (byteVal != null) {
                  presence = PresenceType.fromValue(byteVal);
               }

               if (byteVal == null) {
                  presence = PresenceType.AVAILABLE;
               }

               byteVal = this.getShowOfflineContacts();
               boolean showOfflineContacts = byteVal != null && byteVal.intValue() == 1;
               userPrx = connection.getUserPrx();
               if (userPrx == null) {
                  throw new Exception("You are no longer logged in");
               } else {
                  UserPrxHelper.checkedCast(userPrx.ice_timeout(connection.getGateway().getImLoginTimeout())).otherIMLogin(imType.value(), presence.value(), showOfflineContacts);
                  return new FusionPacket[]{new FusionPktOk(this.transactionId)};
               }
            }
         }
      } catch (LocalException var7) {
         log.error("An error occurred during IM login [IMTYPE:" + imType + "] [SID:" + connection.getSessionID() + "] userPrx=" + userPrx + "e=" + var7, var7);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to sign into " + imType);
         return new FusionPacket[]{pktError};
      } catch (FusionException var8) {
         log.error("An error occurred during IM login [IMTYPE:" + imType + "]", var8);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to sign into " + imType + " - " + var8.message);
         return new FusionPacket[]{pktError};
      } catch (Exception var9) {
         log.error("An error occurred during IM login [IMTYPE:" + imType + "]", var9);
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to sign into other IM - " + var9.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
