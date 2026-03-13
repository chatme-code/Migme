package com.projectgoth.fusion.restapi.data;

import java.util.LinkedList;
import java.util.List;

public class RSSFeedForUserList {
   List<RSSFeedForUser> rss = new LinkedList();

   public void addRSSFeedForUser(RSSFeedForUser r) {
      this.rss.add(r);
   }

   public List<RSSFeedForUser> getEntries() {
      return this.rss;
   }
}
