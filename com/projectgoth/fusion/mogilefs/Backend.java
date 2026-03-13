package com.projectgoth.fusion.mogilefs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.channels.IllegalBlockingModeException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;

class Backend {
   private static Logger log;
   private List hosts;
   private Map deadHosts;
   private String lastErr;
   private String lastErrStr;
   private SocketWithReaderAndWriter cachedSocket;
   private Pattern ERROR_PATTERN = Pattern.compile("^ERR\\s+(\\w+)\\s*(\\S*)");
   private static final int ERR_PART = 1;
   private static final int ERRSTR_PART = 2;
   private Pattern OK_PATTERN = Pattern.compile("^OK\\s+\\d*\\s*(\\S*)");
   private static final int ARGS_PART = 1;

   public Backend(List trackers, boolean connectNow, Logger logger) throws NoTrackersException {
      log = logger;
      this.reload(trackers, connectNow);
   }

   public void reload(List trackers, boolean connectNow) throws NoTrackersException {
      this.hosts = trackers;
      if (this.hosts.size() == 0) {
         throw new NoTrackersException();
      } else {
         this.deadHosts = new HashMap();
         this.lastErr = null;
         this.lastErrStr = null;
         this.cachedSocket = null;
         if (connectNow) {
            this.cachedSocket = this.getSocket();
         }

      }
   }

   private SocketWithReaderAndWriter getSocket() throws NoTrackersException {
      int hostSize = this.hosts.size();
      int tries = hostSize > 15 ? 15 : hostSize;
      int index = (int)Math.floor((double)this.hosts.size() * Math.random());
      long now = System.currentTimeMillis();

      while(true) {
         while(tries-- > 0) {
            InetSocketAddress host = (InetSocketAddress)this.hosts.get(index++ % hostSize);
            Long deadTime = (Long)this.deadHosts.get(host);
            if (deadTime != null && deadTime > now - 5000L) {
               if (log.isDebugEnabled()) {
                  log.debug(" skipping connect attempt to dead host " + host);
               }
            } else {
               try {
                  Socket socket = new Socket();
                  socket.setSoTimeout(30000);
                  socket.connect(host, 3000);
                  if (log.isDebugEnabled()) {
                     log.debug("connected to tracker " + socket.getInetAddress().getHostName());
                  }

                  return new SocketWithReaderAndWriter(socket);
               } catch (IOException var9) {
                  log.warn("Unable to connect to tracker at " + host.toString(), var9);
               } catch (IllegalBlockingModeException var10) {
                  log.warn("Unable to connect to tracker at " + host.toString(), var10);
               } catch (IllegalArgumentException var11) {
                  log.warn("Unable to connect to tracker " + host.toString(), var11);
               }

               log.warn("marking host " + host + " as dead");
               this.deadHosts.put(host, new Long(now));
            }
         }

         throw new NoTrackersException();
      }
   }

   public synchronized Map doRequest(String command, String[] args) throws NoTrackersException, TrackerCommunicationException {
      if (command != null && args != null) {
         String argString = this.encodeURLString(args);
         String request = command + " " + argString + "\r\n";
         if (log.isDebugEnabled()) {
            log.debug("command: " + request);
         }

         if (this.cachedSocket != null) {
            try {
               this.cachedSocket.getWriter().write(request);
               this.cachedSocket.getWriter().flush();
            } catch (IOException var10) {
               log.debug("cached socket went bad while sending request");
               this.cachedSocket = null;
            }
         }

         if (this.cachedSocket == null) {
            SocketWithReaderAndWriter socket = this.getSocket();

            try {
               socket.getWriter().write(request);
               socket.getWriter().flush();
            } catch (IOException var9) {
               throw new TrackerCommunicationException("problem finding a working tracker in this list: " + this.listKnownTrackers());
            }

            this.cachedSocket = socket;
         }

         try {
            String response = this.cachedSocket.getReader().readLine();
            if (response == null) {
               throw new TrackerCommunicationException("received null response from tracker at " + this.cachedSocket.getSocket().getInetAddress());
            } else {
               if (log.isDebugEnabled()) {
                  log.debug("response: " + response);
               }

               Matcher ok = this.OK_PATTERN.matcher(response);
               if (ok.matches()) {
                  return this.decodeURLString(ok.group(1));
               } else {
                  Matcher err = this.ERROR_PATTERN.matcher(response);
                  if (err.matches()) {
                     this.lastErr = err.group(1);
                     this.lastErrStr = err.group(2);
                     if (log.isDebugEnabled()) {
                        log.debug("error message from tracker: " + this.lastErr + ", " + this.lastErrStr);
                     }

                     return null;
                  } else {
                     throw new TrackerCommunicationException("invalid server response from " + this.cachedSocket.getSocket().getInetAddress() + ": " + response);
                  }
               }
            }
         } catch (IOException var8) {
            log.warn("problem reading response from server (" + this.cachedSocket.getSocket().getInetAddress() + ")", var8);
            throw new TrackerCommunicationException("problem talking to server at " + this.cachedSocket.getSocket().getInetAddress(), var8);
         }
      } else {
         log.error("null command or args sent to doRequest");
         return null;
      }
   }

   public String getLastErr() {
      return this.lastErr;
   }

   public String getLastErrStr() {
      return this.lastErrStr;
   }

   private String listKnownTrackers() {
      StringBuffer trackers = new StringBuffer();

      InetSocketAddress host;
      for(Iterator it = this.hosts.iterator(); it.hasNext(); trackers.append(host.toString())) {
         host = (InetSocketAddress)it.next();
         if (trackers.length() > 0) {
            trackers.append(", ");
         }
      }

      return trackers.toString();
   }

   private String encodeURLString(String[] args) {
      try {
         StringBuffer encoded = new StringBuffer();

         for(int i = 0; i < args.length; i += 2) {
            String key = args[i];
            String value = args[i + 1];
            if (encoded.length() > 0) {
               encoded.append("&");
            }

            encoded.append(key);
            encoded.append("=");
            encoded.append(URLEncoder.encode(value, "UTF-8"));
         }

         return encoded.toString();
      } catch (UnsupportedEncodingException var6) {
         log.error("problem encoding URL for tracker", var6);
         return null;
      }
   }

   private Map decodeURLString(String encoded) {
      HashMap map = new HashMap();

      try {
         if (encoded != null && encoded.length() != 0) {
            String[] parts = encoded.split("&");

            for(int i = 0; i < parts.length; ++i) {
               String[] pair = parts[i].split("=");
               if (pair != null && pair.length == 2) {
                  map.put(pair[0], URLDecoder.decode(pair[1], "UTF-8"));
               } else {
                  log.error("poorly encoded string: " + encoded);
               }
            }

            return map;
         } else {
            return map;
         }
      } catch (UnsupportedEncodingException var6) {
         log.error("problem decoding URL from tracker", var6);
         return null;
      }
   }

   public String getTracker() {
      return this.cachedSocket == null ? null : this.cachedSocket.getTracker();
   }
}
