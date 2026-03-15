package com.projectgoth.fusion.app.dao;

public class DAOException extends Exception {
   public DAOException(String msg) {
      super(msg);
   }

   public DAOException(String msg, Throwable t) {
      super(msg, t);
   }

   public DAOException(Throwable t) {
      super(t);
   }
}
