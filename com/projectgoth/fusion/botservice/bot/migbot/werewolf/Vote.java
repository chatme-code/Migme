package com.projectgoth.fusion.botservice.bot.migbot.werewolf;

public class Vote {
   String name;
   String vote;

   public void setName(String name) {
      this.name = name;
   }

   public void setVote(String vote) {
      this.vote = vote;
   }

   public String getName() {
      return this.name;
   }

   public String getVote() {
      return this.vote;
   }

   public Vote(String name, String vote) {
      this.setName(name);
      this.setVote(vote);
   }
}
