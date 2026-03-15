package com.projectgoth.fusion.uns.domain;

import java.util.Date;
import java.util.UUID;

public class Note {
   private String text;
   private Date created;
   private UUID ID;

   public UUID getID() {
      return this.ID;
   }

   public Note() {
      this.ID = UUID.randomUUID();
   }

   public Note(String text) {
      this();
      this.text = text;
      this.created = new Date();
   }

   public Note(String text, Date created) {
      this();
      this.text = text;
      this.created = created;
   }

   public String getText() {
      return this.text;
   }

   public Date getCreated() {
      return this.created;
   }
}
