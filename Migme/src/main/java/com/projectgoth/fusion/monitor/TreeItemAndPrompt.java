package com.projectgoth.fusion.monitor;

import org.eclipse.swt.widgets.TreeItem;

public class TreeItemAndPrompt {
   private final String prompt;
   private final TreeItem ti;

   public TreeItemAndPrompt(TreeItem parentItem, int style, String prompt) {
      this.prompt = prompt;
      this.ti = new TreeItem(parentItem, style);
      this.update("");
   }

   public void update(String s) {
      this.ti.setText(this.prompt + ": " + s);
   }

   public void update(Boolean value) {
      this.update(value.toString());
   }

   public void update(Number value) {
      this.update(value.toString());
   }

   public void offline() {
      this.update("");
   }

   public TreeItem getTreeItem() {
      return this.ti;
   }
}
