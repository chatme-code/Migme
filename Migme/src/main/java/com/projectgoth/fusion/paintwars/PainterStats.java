package com.projectgoth.fusion.paintwars;

public class PainterStats {
   public static final String TOTAL_PAINTWARS_POINTS = "TotalPaintWarsPoints";
   public static final String TOTAL_PAINTS_SENT = "TotalPaintsSent";
   public static final String TOTAL_PAINTS_RECEIVED = "TotalPaintsReceived";
   public static final String TOTAL_CLEANS_SENT = "TotalCleansSent";
   public static final String TOTAL_CLEANS_RECEIVED = "TotalCleansReceived";
   public static final String IDENTICON_INDEX = "IdenticonIndex";
   public static final String PAINTS_REMAINING = "PaintsRemaining";
   public static final String CLEANS_REMAINING = "CleansRemaining";
   private int totalPaintWarsPoints = 0;
   private int totalPaintsSent = 0;
   private int totalPaintsReceived = 0;
   private int totalCleansSent = 0;
   private int totalCleansReceived = 0;
   private int identiconIndex = 0;
   private int paintsRemaining = 0;
   private int cleansRemaining = 0;

   public int getPaintsRemaining() {
      return this.paintsRemaining;
   }

   public void setPaintsRemaining(int paintsRemaining) {
      this.paintsRemaining = paintsRemaining;
   }

   public int getCleansRemaining() {
      return this.cleansRemaining;
   }

   public void setCleansRemaining(int cleansRemaining) {
      this.cleansRemaining = cleansRemaining;
   }

   public int getTotalPaintWarsPoints() {
      return this.totalPaintWarsPoints;
   }

   public void setTotalPaintWarsPoints(int totalPaintWarsPoints) {
      this.totalPaintWarsPoints = totalPaintWarsPoints;
   }

   public int getTotalPaintsSent() {
      return this.totalPaintsSent;
   }

   public void setTotalPaintsSent(int totalPaintsSent) {
      this.totalPaintsSent = totalPaintsSent;
   }

   public int getTotalPaintsReceived() {
      return this.totalPaintsReceived;
   }

   public void setTotalPaintsReceived(int totalPaintsReceived) {
      this.totalPaintsReceived = totalPaintsReceived;
   }

   public int getTotalCleansSent() {
      return this.totalCleansSent;
   }

   public void setTotalCleansSent(int totalCleansSent) {
      this.totalCleansSent = totalCleansSent;
   }

   public int getTotalCleansReceived() {
      return this.totalCleansReceived;
   }

   public void setTotalCleansReceived(int totalCleansReceived) {
      this.totalCleansReceived = totalCleansReceived;
   }

   public int getIdenticonIndex() {
      return this.identiconIndex;
   }

   public void setIdenticonIndex(int identiconIndex) {
      this.identiconIndex = identiconIndex;
   }
}
