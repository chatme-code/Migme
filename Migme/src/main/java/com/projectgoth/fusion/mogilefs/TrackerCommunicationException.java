package com.projectgoth.fusion.mogilefs;

public class TrackerCommunicationException extends MogileException {
   public TrackerCommunicationException(String message) {
      super(message);
   }

   public TrackerCommunicationException(String message, Throwable t) {
      super(message, t);
   }
}
