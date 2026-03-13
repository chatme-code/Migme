package com.projectgoth.fusion.imageserver;

import com.projectgoth.fusion.common.ConfigUtils;
import com.projectgoth.fusion.common.StringUtil;
import com.projectgoth.fusion.common.SystemProperty;
import com.projectgoth.fusion.common.SystemPropertyEntities;
import com.projectgoth.fusion.data.UserData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.gateway.HTTPRequest;
import com.projectgoth.fusion.gateway.HTTPResponse;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.restapi.data.SettingsEnums;
import com.projectgoth.fusion.restapi.util.RedisDataUtil;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import org.apache.log4j.Logger;

public class Connection implements Runnable {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(Connection.class));
   private static final int READ_BUFFER_SIZE = 1024;
   private static final int MAX_READ_BUFFER_SIZE = 10240;
   private static final int MAX_EMPTY_READS = 100;
   private static final String DISPLAY_PICTURE_PATH = "/u/";
   private static final String AVATAR_PATH = "/a/";
   private static final String FULL_BODY_AVATAR_PATH = "/avatar/";
   private static final String USER_SELECTED_PICTURE_PATH = "/dp/";
   private static final String DEFAULT_DISPLAY_PICTURE = "f2ca1ea477bf4a66bdab77d956719e59";
   private static final String DEFAULT_AVATAR_PICTURE = "a7a77a652a7e467dab0fe43aa232fbaa";
   private static final String DEFAULT_FULL_BODY_AVATAR_PICTURE = "7d225ec71de64867bad11a3666ccdbcd";
   protected ImageServer imageServer;
   protected SocketChannel channel;
   private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
   private ByteBuffer writeBuffer;
   protected long lastAccessed;
   private Connection.State state;
   private int emptyReads;

   public Connection(ImageServer imageServer, SocketChannel channel) {
      this.state = Connection.State.READING;
      this.imageServer = imageServer;
      this.channel = channel;
      this.lastAccessed = System.currentTimeMillis();
   }

   public long getLastAccessed() {
      return this.lastAccessed;
   }

   public void disconnect() {
      this.imageServer.onConnectionDisconnected(this);

      try {
         Thread.sleep(100L);
      } catch (Exception var6) {
      }

      try {
         this.channel.socket().shutdownOutput();
      } catch (Exception var5) {
      }

      try {
         this.channel.socket().shutdownInput();
      } catch (Exception var4) {
      }

      try {
         this.channel.socket().close();
      } catch (Exception var3) {
      }

      try {
         this.channel.close();
      } catch (Exception var2) {
      }

   }

   public void run() {
      try {
         this.lastAccessed = System.currentTimeMillis();
         if (this.state == Connection.State.READING) {
            this.writeHTTPResponse(this.processHTTPRequest(this.readHTTPRequest()).toString());
         } else {
            this.writeHTTPResponse();
         }

         log.info("Request processed in " + (System.currentTimeMillis() - this.lastAccessed) + " ms");
      } catch (BufferUnderflowException var5) {
         int capacity = this.readBuffer.capacity();
         int position = this.readBuffer.position();
         if (capacity == position) {
            if (capacity > 10240) {
               log.warn("Terminating connection on port " + this.channel.socket().getPort() + " - Read buffer is full");
               this.disconnect();
            } else {
               this.readBuffer = ByteBuffer.allocate(capacity * 2).put(this.readBuffer.array());
               this.imageServer.registerConnection(this, 1);
            }
         } else if (capacity > 1024 && position < 1024) {
            this.readBuffer = ByteBuffer.allocate(1024).put(this.readBuffer.array(), 0, position);
            this.imageServer.registerConnection(this, 1);
         } else {
            this.imageServer.registerConnection(this, 1);
         }
      } catch (Exception var6) {
         log.debug(var6.getClass().getName() + " caught while servicing client on port " + this.channel.socket().getPort() + " - " + var6.getMessage(), var6);
         if (this.state != Connection.State.WRITING) {
            try {
               this.writeHTTPResponse("HTTP/1.1 500 ERROR\r\n\r\n");
            } catch (Exception var4) {
            }
         }

         this.disconnect();
      }

   }

   private HTTPRequest readHTTPRequest() throws IOException {
      int bytesRead = this.channel.read(this.readBuffer);
      if (bytesRead == -1) {
         throw new IOException("Socket closed - EOF");
      } else if (bytesRead == 0) {
         if (++this.emptyReads > 100) {
            log.warn("Too many empty reads. Capacity = " + this.readBuffer.capacity() + " Position = " + this.readBuffer.position() + " Limit = " + this.readBuffer.limit());
            throw new IOException("Sokcet closed - Too many empty reads");
         } else {
            throw new BufferUnderflowException();
         }
      } else {
         this.emptyReads = 0;
         ByteBuffer buffer = (ByteBuffer)this.readBuffer.duplicate().flip();
         HTTPRequest httpRequest = new HTTPRequest();
         httpRequest.read(buffer);
         if (log.isDebugEnabled()) {
            log.debug(httpRequest);
         }

         log.info("URI [" + httpRequest.getRequestMethod().toString() + " " + httpRequest.getPath() + "] USERAGENT [" + httpRequest.getProperty("User-Agent") + "] REFERRER [" + httpRequest.getProperty("Referer") + "]");
         this.readBuffer = buffer.compact();
         return httpRequest;
      }
   }

   private void writeHTTPResponse(String response) throws IOException {
      this.state = Connection.State.WRITING;
      this.writeBuffer = ByteBuffer.wrap(response.getBytes("8859_1"));
      this.writeHTTPResponse();
   }

   private void writeHTTPResponse() throws IOException {
      this.channel.write(this.writeBuffer);
      if (this.writeBuffer.hasRemaining()) {
         this.imageServer.registerConnection(this, 4);
      } else {
         this.state = Connection.State.READING;
         this.imageServer.registerConnection(this, 1);
      }

   }

   protected HTTPResponse processHTTPRequest(HTTPRequest httpRequest) throws Exception {
      if (httpRequest.getRequestMethod() != HTTPRequest.RequestMethod.GET) {
         throw new Exception(httpRequest.getRequestMethod() + " method is not supported");
      } else {
         Integer width = httpRequest.getParameterAsInt("w");
         Integer height = httpRequest.getParameterAsInt("h");
         Integer keepAspectRatio = httpRequest.getParameterAsInt("a");
         Integer crop = httpRequest.getParameterAsInt("c");
         Float compressionQuality = httpRequest.getParameterAsFloat("q");
         String sessionId = httpRequest.getSessionIdFromPath();
         int idx = sessionId.lastIndexOf(46);
         String id;
         String format;
         if (idx == -1) {
            id = sessionId;
            format = null;
         } else {
            format = sessionId.substring(idx + 1).toLowerCase();
            if (!format.equals("jpg") && !format.equals("jpeg") && !format.equals("gif") && !format.equals("png")) {
               id = sessionId;
               format = null;
            } else {
               id = sessionId.substring(0, idx);
            }
         }

         String path = httpRequest.getPath();
         if (path.matches(SystemProperty.get((SystemPropertyEntities.SystemPropertyEntryInterface)SystemPropertyEntities.Temp.WW119_IMAGE_SERVER_ENABLED_REQUEST_URL_PATHS_REGEX))) {
            User userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
            UserData userData = null;

            try {
               Integer userId = Integer.parseInt(id);
               userData = userEJB.loadUserFromID(userId);
            } catch (NumberFormatException var15) {
               userData = userEJB.loadUser(id, false, false);
            }

            if (userData == null) {
               throw new Exception("Invalid username/user ID " + id);
            }

            if (path.startsWith("/dp/")) {
               int userSelectedDisplayPictureType = RedisDataUtil.getUserDisplayPictureSetting(userData.userID);
               if (userSelectedDisplayPictureType == SettingsEnums.DisplayPictureChoice.PROFILE_PICTURE.value()) {
                  id = userData.displayPicture == null ? "f2ca1ea477bf4a66bdab77d956719e59" : userData.displayPicture;
               } else {
                  id = userData.avatar == null ? "a7a77a652a7e467dab0fe43aa232fbaa" : userData.avatar;
               }
            } else if (path.startsWith("/u/")) {
               id = userData.displayPicture == null ? "f2ca1ea477bf4a66bdab77d956719e59" : userData.displayPicture;
            } else if (path.startsWith("/avatar/")) {
               id = userData.fullbodyAvatar == null ? "7d225ec71de64867bad11a3666ccdbcd" : userData.fullbodyAvatar;
            } else {
               id = userData.avatar == null ? "a7a77a652a7e467dab0fe43aa232fbaa" : userData.avatar;
            }
         }

         if (StringUtil.isBlank(id)) {
            throw new IllegalArgumentException("Missing image id. Please provide an image id.");
         } else {
            ImageItem imageItem;
            if (width != null && height != null) {
               imageItem = this.imageServer.getImageCache().getImage(id, format, width, height, keepAspectRatio != null && keepAspectRatio == 1, crop != null && crop == 1, compressionQuality == null ? 0.0F : compressionQuality);
            } else {
               imageItem = this.imageServer.getImageCache().getImage(id, format);
            }

            return new HTTPResponse("image/" + imageItem.getFormat(), imageItem.getBytes());
         }
      }
   }

   private static enum State {
      READING,
      PROCESSING,
      WRITING;
   }
}
