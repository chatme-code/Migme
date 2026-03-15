package com.projectgoth.fusion.data.pot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PayoutData implements Serializable {
   private double totalPayoutPerUser = 0.0D;
   private int botId;
   private final List<GameWinnerData> gameWinnerDataList = new ArrayList();
   private final List<GameSpenderData> gameSpenderData = new ArrayList();
   private final Collection<GameWinnerData> readOnlyGameWinnerDataList;
   private final Collection<GameSpenderData> readOnlyGameSpenderData;

   public PayoutData() {
      this.readOnlyGameWinnerDataList = Collections.unmodifiableList(this.gameWinnerDataList);
      this.readOnlyGameSpenderData = Collections.unmodifiableList(this.gameSpenderData);
   }

   public int getBotId() {
      return this.botId;
   }

   public void setBotId(int botId) {
      this.botId = botId;
   }

   public Collection<GameWinnerData> add(GameWinnerData potWinner) {
      this.gameWinnerDataList.add(potWinner);
      return this.readOnlyGameWinnerDataList;
   }

   public Collection<GameSpenderData> add(GameSpenderData gameSpending) {
      this.gameSpenderData.add(gameSpending);
      return this.readOnlyGameSpenderData;
   }

   public Collection<GameSpenderData> getGameSpenderData() {
      return this.readOnlyGameSpenderData;
   }

   public Collection<GameWinnerData> getGameWinnerDataList() {
      return this.readOnlyGameWinnerDataList;
   }

   public void setTotalPayoutPerUser(double totalPayoutPerUser) {
      this.totalPayoutPerUser = totalPayoutPerUser;
   }

   public double getTotalPayoutPerUser() {
      return this.totalPayoutPerUser;
   }

   public String toString() {
      StringBuilder builder = new StringBuilder();
      builder.append("PayoutData [totalPayoutPerUser=");
      builder.append(this.totalPayoutPerUser);
      builder.append(", botId=");
      builder.append(this.botId);
      builder.append(", gameWinnerDataList=");
      builder.append(this.gameWinnerDataList);
      builder.append(", gameSpenderData=");
      builder.append(this.gameSpenderData);
      builder.append(", readOnlyGameWinnerDataList=");
      builder.append(this.readOnlyGameWinnerDataList);
      builder.append(", readOnlyGameSpenderData=");
      builder.append(this.readOnlyGameSpenderData);
      builder.append("]");
      return builder.toString();
   }
}
