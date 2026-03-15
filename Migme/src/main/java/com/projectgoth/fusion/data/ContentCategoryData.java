package com.projectgoth.fusion.data;

import java.io.Serializable;

public class ContentCategoryData implements Serializable {
   public Integer id;
   public String name;
   public Integer parentContentCategoryID;
   public ContentCategoryData parentContentCategory;
}
