package com.projectgoth.fusion.data;

import java.io.Serializable;
import java.util.List;

public class ListDataWrapper<T> implements Serializable {
   private List<T> listData;
   private int totalResults;

   public List<T> getListData() {
      return this.listData;
   }

   public void setListData(List<T> listData) {
      this.listData = listData;
   }

   public int getTotalResults() {
      return this.totalResults;
   }

   public void setTotalResults(int totalResults) {
      this.totalResults = totalResults;
   }

   public ListDataWrapper(List<T> listData) {
      this.listData = listData;
   }
}
