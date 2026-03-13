package com.projectgoth.fusion.gateway.packet.sticker;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.ContentUtils;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.EmoticonPackData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.gateway.packet.FusionPktError;
import com.projectgoth.fusion.gateway.packet.FusionRequest;
import com.projectgoth.fusion.interfaces.Content;
import com.projectgoth.fusion.interfaces.ContentHome;
import com.projectgoth.fusion.packet.FusionPacket;
import java.rmi.RemoteException;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;
import org.apache.log4j.Logger;

public class FusionPktGetStickerPackListOld extends FusionRequest {
   private static Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FusionPktGetStickerPackListOld.class));
   private static final char DELIMITER = ' ';

   public FusionPktGetStickerPackListOld() {
      super((short)938);
   }

   public FusionPktGetStickerPackListOld(short transactionId) {
      super((short)938, transactionId);
   }

   public FusionPktGetStickerPackListOld(FusionPacket packet) {
      super(packet);
   }

   public boolean sessionRequired() {
      return true;
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
         StringBuilder packIdsStrBuilder = new StringBuilder();
         StringBuilder packVersionStrBuilder = new StringBuilder();

         EmoticonPackData packData;
         for(Iterator i$ = stickerPackList.iterator(); i$.hasNext(); packVersionStrBuilder.append(packData.getVersion())) {
            packData = (EmoticonPackData)i$.next();
            if (packIdsStrBuilder.length() > 0) {
               packIdsStrBuilder.append(' ');
            }

            packIdsStrBuilder.append(packData.getId());
            if (packVersionStrBuilder.length() > 0) {
               packVersionStrBuilder.append(' ');
            }
         }

         FusionPktStickerPackListOld pktStickerPackList = new FusionPktStickerPackListOld(transactionId);
         pktStickerPackList.setStickerPackIDs(packIdsStrBuilder.toString());
         pktStickerPackList.setStickerPackVersions(packVersionStrBuilder.toString());
         return pktStickerPackList.toArray();
      }
   }
}
