package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataGetStickerPackList;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktGetStickerPackList extends FusionPktDataGetStickerPackList {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetStickerPackList.class));

   public FusionPktGetStickerPackList(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktGetStickerPackList(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         return processRequest(this.transactionId, connection.getUsername());
      } catch (CreateException var3) {
         log.error("Create exception", var3);
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get sticker pack list - Failed to create contentEJB")).toArray();
      } catch (RemoteException var4) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get sticker pack list - " + var4.getClass().getName() + ":" + RMIExceptionHelper.getRootMessage(var4))).toArray();
      } catch (Exception var5) {
         log.error("Unhandled exception on FusionPktGetStickerPackList", var5);
         return var5.getMessage() == null ? (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get sticker pack list - " + var5.getClass().getName() + ":" + var5.getMessage())).toArray() : (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Unable to get sticker pack list - " + var5.getMessage())).toArray();
      }
   }

   public static FusionPacket[] processRequest(short transactionId, String username) throws RemoteException, CreateException {
      if (!ContentUtils.isStickersEnabled()) {
         return (new FusionPktError(transactionId, FusionPktError.Code.UNDEFINED, "Feature disabled")).toArray();
      } else {
         Content contentEJB = (Content)EJBHomeCache.getObject("ejb/Content", ContentHome.class);
         List<EmoticonPackData> stickerPackList = contentEJB.getStickerPackDataListForUser(username);
         String[] packIdList = new String[stickerPackList.size()];
         String[] versionList = new String[stickerPackList.size()];

         for(int i = 0; i < stickerPackList.size(); ++i) {
            EmoticonPackData epd = (EmoticonPackData)stickerPackList.get(i);
            packIdList[i] = epd.getId().toString();
            versionList[i] = epd.getVersion().toString();
         }

         FusionPktStickerPackList pktStickerPackList = new FusionPktStickerPackList(transactionId);
         pktStickerPackList.setStickerPackIdList(packIdList);
         pktStickerPackList.setVersionList(versionList);
         return pktStickerPackList.toArray();
      }
   }
}
