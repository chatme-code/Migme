package com.projectgoth.fusion.voiceengine;

public class ChannelRequest {
   public static final String COMMAND_CHANSTAT = "CHANNEL STATUS";
   public static final String COMMAND_EXECAPP = "EXEC";
   public static final String COMMAND_SETVAR = "SET VARIABLE";
   public static final String COMMAND_GETVAR = "GET VARIABLE";
   public static final String COMMAND_GETDIGIT = "WAIT FOR DIGIT";
   public static final String APP_ANSWER = "ANSWER";
   public static final String APP_WAIT = "WAIT";
   public static final String APP_HANGUP = "HANGUP";
   public static final String APP_DIAL = "DIAL";
   public static final String APP_PLAYBACK = "PLAYBACK";
   public static final String APP_BACKGROUND = "BACKGROUND";
   public static final String APP_READ = "READ";
   public static final String APP_RECORD = "RECORD";
   public static final String APP_SAYDIGITS = "SAYDIGITS";
   public static final String APP_SAYNUMBER = "SAYNUMBER";
   public static final String APP_PLAYTONES = "PLAYTONES";
   public static final String APP_STOPPLAYTONES = "STOPPLAYTONES";
   public static final String APP_SETCDRUSERFIELD = "SETCDRUSERFIELD";
   public static final String APP_SIPADDHEADER = "SIPADDHEADER";
   public static final String APP_RESETCDR = "RESETCDR";
   protected String command = null;
   protected String param = null;
   protected String data = null;

   public ChannelRequest() {
   }

   public ChannelRequest(String command, String param, String data) {
      this.command = command;
      this.param = param;
      this.data = data;
   }

   public void setCommand(String command) {
      this.command = command;
   }

   public String getCommand() {
      return this.command;
   }

   public void setParam(String param) {
      this.param = param;
   }

   public String getParam() {
      return this.param;
   }

   public void setData(String data) {
      this.data = data;
   }

   public String getData() {
      return this.data;
   }
}
