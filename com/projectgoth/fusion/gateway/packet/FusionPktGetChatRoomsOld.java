package com.projectgoth.fusion.gateway.packet;

import Ice.LocalException;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.ChatRoomData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.ChatRoomList;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.RegistryPrx;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.ejb.CreateException;

public class FusionPktGetChatRoomsOld extends FusionRequest {
   public FusionPktGetChatRoomsOld() {
      super((short)700);
   }

   public FusionPktGetChatRoomsOld(short transactionId) {
      super((short)700, transactionId);
   }

   public FusionPktGetChatRoomsOld(FusionPacket packet) {
      super(packet);
   }

   public String getSearchString() {
      return this.getStringField((short)1);
   }

   public void setSearchString(String searchString) {
      this.setField((short)1, searchString);
   }

   public Byte getPage() {
      return this.getByteField((short)2);
   }

   public void setPage(byte page) {
      this.setField((short)2, page);
   }

   public String getChatRoomNames() {
      return this.getStringField((short)3);
   }

   public void setChatRoomNames(String chatRoomNames) {
      this.setField((short)3, chatRoomNames);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      try {
         RegistryPrx registryPrx = connection.findRegistry();
         if (registryPrx == null) {
            throw new Exception("Failed to locate registry");
         } else {
            List<ChatRoomData> chatRooms = null;
            String infoText = null;
            String chatRoomNames = this.getChatRoomNames();
            Integer pages;
            ChatRoomList list;
            if (chatRoomNames == null) {
               list = connection.getChatRoomList();
               Byte page = this.getPage();
               if (page == null) {
                  chatRooms = list.getList(registryPrx, connection.getCountryID(), this.getSearchString());
               } else {
                  chatRooms = list.getPage(registryPrx, page.intValue());
               }

               pages = list.pages();
            } else {
               list = connection.getChatRoomList();
               chatRooms = list.getInitialList(registryPrx, connection.getUsername(), chatRoomNames.split(";"));
               pages = 1;
               MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
               infoText = misEJB.getInfoText(24);
            }

            ArrayList<FusionPacket> fusionPkts = new ArrayList();
            if (chatRooms != null) {
               Iterator i$ = chatRooms.iterator();

               while(i$.hasNext()) {
                  ChatRoomData room = (ChatRoomData)i$.next();
                  fusionPkts.add(new FusionPktChatRoomOld(this.transactionId, room));
               }
            }

            FusionPktGetChatRoomsCompleteOld complete = new FusionPktGetChatRoomsCompleteOld(this.transactionId);
            complete.setPages(pages.byteValue());
            if (infoText != null && infoText.length() > 0) {
               complete.setInfoText(infoText);
            }

            fusionPkts.add(complete);
            return (FusionPacket[])fusionPkts.toArray(new FusionPacket[fusionPkts.size()]);
         }
      } catch (CreateException var10) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat rooms - " + var10.getMessage())).toArray();
      } catch (RemoteException var11) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat rooms - " + RMIExceptionHelper.getRootMessage(var11))).toArray();
      } catch (LocalException var12) {
         return (new FusionPktInternalServerError(this.transactionId, var12, "Failed to get chat rooms")).toArray();
      } catch (Exception var13) {
         return (new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to get chat rooms - " + var13.getMessage())).toArray();
      }
   }
}
