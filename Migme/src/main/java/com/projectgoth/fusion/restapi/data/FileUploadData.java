package com.projectgoth.fusion.restapi.data;

public class FileUploadData {
   public final String url;
   public String thumbnailUrl = null;

   public FileUploadData(String url) {
      this.url = url;
   }
}
