package com.projectgoth.fusion.objectcache;

import java.io.Serializable;

public class ChatUserState implements Serializable {
   private static final long serialVersionUID = 7526472295622776147L;
   String username;
   ChatSessionState[] sessions;
}
