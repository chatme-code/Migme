package com.projectgoth.fusion.imageserver;

import Ice.Current;
import com.projectgoth.fusion.slice.FusionException;
import com.projectgoth.fusion.slice.ImageServerStats;
import com.projectgoth.fusion.slice._ImageServerAdminDisp;

public class ImageServerAdminI extends _ImageServerAdminDisp {
   private ImageServer imageServer;

   public ImageServerAdminI(ImageServer imageServer) {
      this.imageServer = imageServer;
   }

   public ImageServerStats getStats(Current __current) throws FusionException {
      try {
         return this.imageServer.getStats();
      } catch (Exception var4) {
         FusionException fe = new FusionException();
         fe.message = "Initialisation incomplete";
         throw fe;
      }
   }
}
