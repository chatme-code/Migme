package com.projectgoth.fusion.objectcache;

import java.io.Serializable;

public class ChatSessionState implements Serializable {
   private static final long serialVersionUID = 7526472295622776147L;
   String sessionID;
   int presence;
   int deviceType;
   int connectionType;
   int imType;
   int port;
   int remotePort;
   String remoteAddress;
   String mobileDevice;
   String userAgent;
   short clientVersion;
   String language;
}
