package com.projectgoth.fusion.voiceengine;

import com.projectgoth.fusion.common.ConfigUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import org.apache.log4j.Logger;

public class FastAGIChannel {
   private static final Logger log = Logger.getLogger(ConfigUtils.getLoggerName(FastAGIChannel.class));
   private static final String CHAR_ENCODING = "UTF-8";
   protected FastAGIWorker worker = null;
   protected FastAGIServer server = null;
   protected FastAGICommand command = null;
   protected Socket socket = null;

   public FastAGIChannel(FastAGIWorker worker, FastAGIServer server, FastAGICommand command, Socket socket) {
      this.worker = worker;
      this.server = server;
      this.command = command;
      this.socket = socket;
   }

   public FastAGIWorker getWorker() {
      return this.worker;
   }

   public FastAGICommand getCommand() {
      return this.command;
   }

   protected String prepareAppParam(String param) {
      return param == null ? "" : param;
   }

   public ChannelResponse setParameter(String name, String value) {
      ChannelResponse response = null;
      if (this.command != null) {
         this.command.setParameter(name, value);
         response = new ChannelResponse(200, 0, (String)null);
      }

      return response;
   }

   public ChannelResponse getParameter(String name) {
      ChannelResponse response = null;
      if (this.command != null) {
         response = new ChannelResponse(200, 0, this.command.getParameter(name));
      }

      return response;
   }

   public void writeRequest(ChannelRequest request) {
      if (this.socket != null && this.socket.isConnected()) {
         if (request != null && request.getCommand() != null) {
            StringBuilder builder = new StringBuilder();
            builder.append(request.getCommand());
            if (request.getParam() != null) {
               builder.append(" " + request.getParam() + "");
            }

            if (request.getData() != null) {
               builder.append(" \"" + request.getData() + "\"");
            }

            builder.append("\n");
            log.debug(builder);

            try {
               OutputStream out = this.socket.getOutputStream();
               out.write(builder.toString().getBytes("UTF-8"));
               out.flush();
            } catch (IOException var4) {
               log.error("Could not write to socket in writeRequest");
            }

         } else {
            log.error("Invalid request in writeRequest");
         }
      } else {
         log.error("Invalid socket in writeRequest");
      }
   }

   public ChannelResponse readResponse() {
      if (this.socket != null && this.socket.isConnected()) {
         ChannelResponse response = null;

         try {
            InputStream in = this.socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            int responseCode = 0;
            int returnValue = 0;
            String returnData = null;

            for(String line = reader.readLine(); line != null && line.length() > 0 && line != "\r" && line != "\r\n"; line = reader.readLine()) {
               log.debug(line);
               String[] parts = line.split(" ");
               if (parts != null) {
                  String part;
                  if (parts.length > 0 && parts[0] != null) {
                     part = parts[0].trim();

                     try {
                        responseCode = Integer.parseInt(part);
                     } catch (Throwable var13) {
                     }
                  }

                  boolean pos;
                  int pos;
                  if (parts.length > 1 && parts[1] != null) {
                     part = parts[1].trim();
                     pos = false;
                     if ((pos = part.indexOf("=")) >= 0) {
                        part = part.substring(pos + 1);

                        try {
                           returnValue = Integer.parseInt(part);
                        } catch (Throwable var12) {
                        }
                     }
                  }

                  if (parts.length > 2 && parts[2] != null) {
                     part = line.trim();
                     pos = false;
                     if ((pos = part.indexOf("(")) >= 0) {
                        part = part.substring(pos + 1);
                        if ((pos = part.lastIndexOf(")")) >= 0) {
                           part = part.substring(0, pos);
                        }
                     }

                     returnData = part;
                  }

                  if (responseCode > 0) {
                     break;
                  }
               }
            }

            response = new ChannelResponse(responseCode, returnValue, returnData);
         } catch (IOException var14) {
            log.error("Could not read from to socket in readResponse");
         }

         return response;
      } else {
         log.error("Invalid socket in readResponse");
         return null;
      }
   }

   public ChannelResponse execCommand(String command, String param, String data) {
      ChannelRequest request = new ChannelRequest(command, param, data);
      ChannelResponse response = null;
      this.writeRequest(request);
      response = this.readResponse();
      return response;
   }

   public ChannelResponse execChannelStatus() {
      ChannelResponse response = null;
      response = this.execCommand("CHANNEL STATUS", (String)null, (String)null);
      return response;
   }

   public ChannelResponse execApp(String app, String data) {
      ChannelResponse response = null;
      response = this.execCommand("EXEC", app, data);
      return response;
   }

   public ChannelResponse execSetVariable(String name, String value) {
      ChannelResponse response = null;
      response = this.execCommand("SET VARIABLE", name, value);
      return response;
   }

   public ChannelResponse execGetVariable(String name) {
      ChannelResponse response = null;
      response = this.execCommand("GET VARIABLE", name, (String)null);
      return response;
   }

   public ChannelResponse execGetDigit(int maxMillis) {
      ChannelResponse response = null;
      response = this.execCommand("WAIT FOR DIGIT", "" + maxMillis, (String)null);
      return response;
   }

   public ChannelResponse execAppAnswer() {
      ChannelResponse response = null;
      response = this.execApp("ANSWER", (String)null);
      return response;
   }

   public ChannelResponse execAppWait(int seconds) {
      ChannelResponse response = null;
      response = this.execApp("WAIT", this.prepareAppParam("" + seconds));
      return response;
   }

   public ChannelResponse execAppHangup() {
      ChannelResponse response = null;
      response = this.execApp("HANGUP", (String)null);
      return response;
   }

   public ChannelResponse execAppDial(String dialString, int seconds, String options) {
      ChannelResponse response = null;
      response = this.execApp("DIAL", this.prepareAppParam(dialString) + "|" + this.prepareAppParam("" + seconds) + "|" + this.prepareAppParam(options));
      return response;
   }

   public ChannelResponse execAppPlayback(String filename, String options) {
      ChannelResponse response = null;
      response = this.execApp("PLAYBACK", this.prepareAppParam(filename) + "|" + this.prepareAppParam(options));
      return response;
   }

   public ChannelResponse execAppBackground(String filename, String options) {
      ChannelResponse response = null;
      response = this.execApp("BACKGROUND", this.prepareAppParam(filename) + "|" + this.prepareAppParam(options));
      return response;
   }

   public ChannelResponse execAppRead(String variableName, String filename, int maxDigits, String options, int attempts, int maxSeconds) {
      ChannelResponse response = null;
      response = this.execApp("READ", this.prepareAppParam(variableName) + "|" + this.prepareAppParam(filename) + "|" + this.prepareAppParam("" + maxDigits) + "|" + this.prepareAppParam(options) + "|" + this.prepareAppParam("" + attempts) + "|" + this.prepareAppParam("" + maxSeconds));
      return response;
   }

   public ChannelResponse execAppRecord(String filename, String format, int maxSilenceSeconds, int maxSeconds, String options) {
      ChannelResponse response = null;
      response = this.execApp("RECORD", this.prepareAppParam(filename) + "." + this.prepareAppParam(format) + "|" + this.prepareAppParam("" + maxSilenceSeconds) + "|" + this.prepareAppParam("" + maxSeconds) + "|" + this.prepareAppParam(options));
      return response;
   }

   public ChannelResponse execAppSayDigits(String digits) {
      ChannelResponse response = null;
      response = this.execApp("SAYDIGITS", this.prepareAppParam(digits));
      return response;
   }

   public ChannelResponse execAppSayNumber(String number) {
      ChannelResponse response = null;
      response = this.execApp("SAYNUMBER", this.prepareAppParam(number));
      return response;
   }

   public ChannelResponse execAppPlayTones(String toneName) {
      ChannelResponse response = null;
      response = this.execApp("PLAYTONES", this.prepareAppParam(toneName));
      return response;
   }

   public ChannelResponse execAppStopPlayTones() {
      ChannelResponse response = null;
      response = this.execApp("STOPPLAYTONES", (String)null);
      return response;
   }

   public ChannelResponse execAppSetCDRUserField(String value) {
      ChannelResponse response = null;
      response = this.execApp("SETCDRUSERFIELD", value);
      return response;
   }

   public ChannelResponse execAppSIPAddHeader(String value) {
      ChannelResponse response = null;
      response = this.execApp("SIPADDHEADER", value);
      return response;
   }

   public ChannelResponse execAppResetCDR() {
      ChannelResponse response = null;
      response = this.execApp("RESETCDR", (String)null);
      return response;
   }
}
