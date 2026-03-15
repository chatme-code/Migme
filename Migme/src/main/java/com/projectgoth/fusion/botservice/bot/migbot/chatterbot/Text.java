package com.projectgoth.fusion.botservice.bot.migbot.chatterbot;

public class Text {
   private int id;
   private String content;

   public Text(int id, String content) {
      this.id = id;
      this.content = content;
   }

   public int getId() {
      return this.id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public String getContent() {
      return this.content;
   }

   public void setContent(String content) {
      this.content = content;
   }

   public String toString() {
      return "Text [id=" + this.id + ", content=" + this.content + "]";
   }
}
