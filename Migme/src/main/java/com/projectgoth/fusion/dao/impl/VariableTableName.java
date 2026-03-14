package com.projectgoth.fusion.dao.impl;

import java.util.Date;

public class VariableTableName {
   private String tablename;
   private Date lastUpdated;

   public synchronized String getTablename() {
      return this.tablename;
   }

   public synchronized void setTablename(String tablename) {
      this.tablename = tablename;
   }

   public synchronized Date getLastUpdated() {
      return this.lastUpdated;
   }

   public synchronized void setLastUpdated(Date lastUpdated) {
      this.lastUpdated = lastUpdated;
   }
}
