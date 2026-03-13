package com.projectgoth.fusion.gateway.packet;

import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControl;
import com.projectgoth.fusion.accesscontrol.AuthenticatedAccessControlTypeEnum;
import com.projectgoth.fusion.common.FloodControl;
import com.projectgoth.fusion.common.ImageInfo;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.URLUtil;
import com.projectgoth.fusion.data.FileData;
import com.projectgoth.fusion.data.ScrapbookData;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.fdl.packets.FusionPktDataUploadFile;
import com.projectgoth.fusion.gateway.ConnectionI;
import com.projectgoth.fusion.interfaces.MIS;
import com.projectgoth.fusion.interfaces.MISHome;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.mogilefs.DFSManager;
import com.projectgoth.fusion.mogilefs.NoTrackersException;
import com.projectgoth.fusion.packet.FusionPacket;
import com.projectgoth.fusion.slice.UserDataIce;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;
import java.util.Date;
import javax.ejb.CreateException;

public class FusionPktUploadFile extends FusionPktDataUploadFile {
   public FusionPktUploadFile(ByteBuffer byteBuffer) throws IOException {
      super(byteBuffer);
   }

   public FusionPktUploadFile(FusionPacket packet) {
      super(packet);
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), new FloodControl.Action[]{FloodControl.Action.FILE_UPLOAD_PER_MINUTE, FloodControl.Action.FILE_UPLOAD_DAILY.setMaxHits(SystemProperty.getLong("UploadFileDailyRateLimit", 1000L))});
         UserDataIce userData = connection.getUserPrx().getUserData();
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.UPLOAD_FILE, new UserData(userData))) {
            throw new Exception("You must authenticate your account before uploading.");
         } else {
            FusionPktDataUploadFile.FileType fileType = this.getFileType();
            if (fileType != FusionPktDataUploadFile.FileType.IMAGE) {
               throw new Exception("Unsupported file type " + fileType);
            } else {
               byte[] content = this.getFileContent();
               if (content == null) {
                  throw new Exception("Empty file content");
               } else {
                  MIS misEJB = (MIS)EJBHomeCache.getObject("ejb/MIS", MISHome.class);
                  int limit = SystemProperty.getInt("MaxImageUploadSize", Integer.MAX_VALUE);
                  if (content.length > limit) {
                     throw new Exception("Image exceeds size limit");
                  } else {
                     FileData fileData = new FileData();
                     fileData.id = misEJB.newFileID();
                     fileData.dateCreated = new Date(System.currentTimeMillis());
                     fileData.size = content.length;
                     fileData.uploadedBy = connection.getUsername();
                     ImageInfo imageInfo = new ImageInfo();
                     imageInfo.setInput((InputStream)(new ByteArrayInputStream(content)));
                     if (!imageInfo.check()) {
                        throw new Exception("Invalid image input");
                     } else {
                        fileData.mimeType = "image/" + imageInfo.getFormatName().toLowerCase();
                        fileData.width = imageInfo.getWidth();
                        fileData.height = imageInfo.getHeight();
                        DFSManager mogileFSManager = connection.getGatewayContext().getMogileFSManager();
                        if (mogileFSManager == null) {
                           throw new Exception("MogileFS not initialized");
                        } else {
                           mogileFSManager.storeFile(fileData.id, "image", content);
                           ScrapbookData scrapbookData = misEJB.saveFile(fileData, (String)null);
                           connection.getSessionPrx().photoUploaded();
                           Boolean useAsDisplayPicture = this.getUseAsDisplayPicture();
                           if (useAsDisplayPicture != null && useAsDisplayPicture) {
                              User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
                              userEJB.updateDisplayPicture(connection.getUsername(), fileData.id);
                           }

                           FusionPktOk pkt = new FusionPktOk(this.transactionId);
                           String editPhotoURL = URLUtil.replaceViewTypeToken(SystemProperty.get("EditPhotoURL", ""), connection.getDeviceType());
                           if (!StringUtil.isBlank(editPhotoURL)) {
                              pkt.setServerResponse(editPhotoURL + scrapbookData.id);
                           }

                           return new FusionPacket[]{pkt};
                        }
                     }
                  }
               }
            }
         }
      } catch (CreateException var14) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "EJB create exception - " + var14.getMessage());
         return new FusionPacket[]{pktError};
      } catch (RemoteException var15) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to upload file - " + RMIExceptionHelper.getRootMessage(var15));
         return new FusionPacket[]{pktError};
      } catch (NoTrackersException var16) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to upload file - No MogileFS tracker");
         return new FusionPacket[]{pktError};
      } catch (Exception var17) {
         pktError = new FusionPktError(this.transactionId, FusionPktError.Code.UNDEFINED, "Failed to upload file - " + var17.getMessage());
         return new FusionPacket[]{pktError};
      }
   }
}
