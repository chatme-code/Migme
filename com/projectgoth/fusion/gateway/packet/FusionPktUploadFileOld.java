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
import java.io.InputStream;
import java.rmi.RemoteException;
import java.util.Date;
import javax.ejb.CreateException;

public class FusionPktUploadFileOld extends FusionRequest {
   public FusionPktUploadFileOld() {
      super((short)504);
   }

   public FusionPktUploadFileOld(short transactionId) {
      super((short)504, transactionId);
   }

   public FusionPktUploadFileOld(FusionPacket packet) {
      super(packet);
   }

   public Short getFileType() {
      return this.getShortField((short)1);
   }

   public void setFileType(short contactType) {
      this.setField((short)1, contactType);
   }

   public byte[] getContent() {
      return this.getByteArrayField((short)2);
   }

   public void setContent(byte[] content) {
      this.setField((short)2, content);
   }

   public String getDescription() {
      return this.getStringField((short)3);
   }

   public void setDescription(String description) {
      this.setField((short)3, description);
   }

   public Short getDestination() {
      return this.getShortField((short)4);
   }

   public void setDestination(short destination) {
      this.setField((short)4, destination);
   }

   public Byte getUseAsDisplayPicture() {
      return this.getByteField((short)5);
   }

   public void setUseAsDisplayPicture(byte useAsDisplayPicture) {
      this.setField((short)5, useAsDisplayPicture);
   }

   public boolean sessionRequired() {
      return true;
   }

   protected FusionPacket[] processRequest(ConnectionI connection) {
      FusionPktError pktError;
      try {
         FloodControl.detectFlooding(connection.getUsername(), connection.getUserPrx(), new FloodControl.Action[]{FloodControl.Action.FILE_UPLOAD_PER_MINUTE, FloodControl.Action.FILE_UPLOAD_DAILY.setMaxHits(SystemProperty.getLong("UploadFileDailyRateLimit", 1000L))});
         UserDataIce userData = connection.getUserPrx().getUserData();
         if (!AuthenticatedAccessControl.hasAccess(AuthenticatedAccessControlTypeEnum.UPLOAD_FILE, new UserData(userData))) {
            throw new Exception("You must authenticate your account before uploading.");
         } else {
            Short fileType = this.getFileType();
            if (fileType != null && fileType == 1) {
               byte[] content = this.getContent();
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
                           Byte useAsDisplayPicture = this.getUseAsDisplayPicture();
                           if (useAsDisplayPicture != null && useAsDisplayPicture == 1) {
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
            } else {
               throw new Exception("Unsupported file type " + fileType);
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
