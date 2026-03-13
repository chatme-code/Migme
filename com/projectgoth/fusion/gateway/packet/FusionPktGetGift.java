package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.VirtualGiftData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetGift;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;

public class FusionPktGetGift extends FusionPktDataGetGift {
   public FusionPktGetGift(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetGift(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         List<VirtualGiftData> list = contentEJB.getPopularVirtualGifts(connection.getUsername(), 0, this.getLimit());
         if (list == null) {
            return new FusionPacket[]{new FusionPktOk(this.transactionId)};
         } else {
            String[] hotkeysReturned = new String[list.size()];
            String[] giftNameReturned = new String[list.size()];
            String[] giftPrice = new String[list.size()];
            int index = 0;

            for(Iterator i$ = list.iterator(); i$.hasNext(); ++index) {
               VirtualGiftData giftData = (VirtualGiftData)i$.next();
               hotkeysReturned[index] = giftData.getHotKey();
               giftNameReturned[index] = giftData.getName();
               giftPrice[index] = giftData.getRoundedPrice() + " " + giftData.getCurrency();
            }

            FusionPktGiftHotkeys returnPacket = new FusionPktGiftHotkeys(this.transactionId);
            returnPacket.setHotkeyList(hotkeysReturned);
            returnPacket.setNameList(giftNameReturned);
            returnPacket.setPriceList(giftPrice);
            return new FusionPacket[]{returnPacket};
         }
      } catch (CreateException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get gift - Failed to create ContentEJB")).toArray();
      } catch (RemoteException var11) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get gift - " + var11.getClass().getName() + ":" + RMIExceptionHelper.getRootMessage(var11))).toArray();
      } catch (Exception var12) {
         return var12.getMessage() == null ? (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get gift - " + var12.getClass().getName() + ":" + var12.getMessage())).toArray() : (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get gift - " + var12.getMessage())).toArray();
      }
   }
}
