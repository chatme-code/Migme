package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.Date;

public class FileData implements Serializable {
   public String id;
   public Date dateCreated;
   public String mimeType;
   public Integer size;
   public Integer width;
   public Integer height;
   public Integer length;
   public String uploadedBy;
}
