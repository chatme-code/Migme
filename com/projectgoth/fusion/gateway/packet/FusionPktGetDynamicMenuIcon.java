package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.ByteBufferHelper;
import com.projectgoth.fusion.data.MenuData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class FusionPktGetDynamicMenuIcon extends FusionRequest {
   public FusionPktGetDynamicMenuIcon() {
      super((short)929);
   }

   public FusionPktGetDynamicMenuIcon(short transactionId) {
      super((short)929, transactionId);
   }

   public FusionPktGetDynamicMenuIcon(FusionPacket packet) {
      super(packet);
   }

   public int getIconId() {
      return this.getIntField((short)1);
   }

   public void setIconId(int iconId) {
      this.setField((short)1, iconId);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         int iconId = this.getIconId();
         MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/Content", MISHome.class);
         MenuData menuData = misEJB.getMenu(iconId);
         List<FusionPacket> packetsToReturn = new LinkedList();
         FusionPktDynamicMenuIcon iconsPkt = new FusionPktDynamicMenuIcon(this.transactionId);
         iconsPkt.setIconId(iconId);
         iconsPkt.setIcon(ByteBufferHelper.readFile(new File(menuData.location)).array());
         packetsToReturn.add(iconsPkt);
         return (FusionPacket[])packetsToReturn.toArray(new FusionPacket[packetsToReturn.size()]);
      } catch (Exception var7) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get dynamic menu icon - " + var7.getMessage())).toArray();
      }
   }
}
