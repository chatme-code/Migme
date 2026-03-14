package com.projectgoth.fusion.chat.external.facebook;

import org.jivesoftware.smack.XMPPException;

public class FacebookException extends XMPPException {
   private static final long serialVersionUID = -3901304995485925354L;

   public FacebookException(String message) {
      super(message);
   }

   public FacebookException(Throwable t) {
      super(t);
   }
}
