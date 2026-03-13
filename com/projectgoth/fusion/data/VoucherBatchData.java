package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class VoucherBatchData implements Serializable {
   public Integer id;
   public String username;
   public Date datecreated;
   public Date expirydate;
   public String currency;
   public Double amount;
   public Integer numvoucher;
   public String notes;
   public int num_active;
   public int num_cancelled;
   public int num_redeemed;
   public int num_expired;

   public VoucherBatchData() {
   }

   public VoucherBatchData(ResultSet rs) throws SQLException {
      this.id = rs.getInt("id");
      this.username = rs.getString("username");
      this.datecreated = rs.getTimestamp("dateCreated");
      this.expirydate = rs.getTimestamp("expirydate");
      this.currency = rs.getString("currency");
      this.amount = rs.getDouble("amount");
      this.numvoucher = rs.getInt("numvoucher");
      this.notes = rs.getString("notes");
   }
}
