package com.projectgoth.fusion.exception;

import com.projectgoth.fusion.slice.FusionException;

public class UserNotOnlineException extends FusionException {
   public UserNotOnlineException(String username) {
      super(username + " is not online");
   }
}
