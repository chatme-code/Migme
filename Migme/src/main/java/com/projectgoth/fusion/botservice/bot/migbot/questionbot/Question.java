package com.projectgoth.fusion.botservice.bot.migbot.questionbot;

public class Question {
   int id;
   String question;
   String answer;

   public Question(int id, String question, String answer) {
      this.id = id;
      this.question = question;
      this.answer = answer;
   }

   public int getID() {
      return this.id;
   }

   public String getQuestion() {
      return this.question;
   }

   public String getAnswer() {
      return this.answer;
   }

   public String toString() {
      return "ID: " + this.id + "; Question: " + this.question + "; Answer: " + this.answer;
   }
}
