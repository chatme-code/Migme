package com.projectgoth.fusion.mogilefs;

public class StorageCommunicationException extends MogileException {
   public StorageCommunicationException(String message) {
      super(message);
   }

   public StorageCommunicationException(String message, Throwable t) {
      super(message, t);
   }
}
