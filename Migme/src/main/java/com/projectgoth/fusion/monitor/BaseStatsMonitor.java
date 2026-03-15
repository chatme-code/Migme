package com.projectgoth.fusion.monitor;

import com.projectgoth.fusion.slice.BaseServiceStats;
import java.text.DecimalFormat;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TreeItem;

public abstract class BaseStatsMonitor extends BaseMonitor {
   protected TreeItem versionTreeItem;
   protected String versionText = new String("Fusion Version: ");
   protected TreeItem jvmMemoryUsageTreeItem;
   protected String jvmMemoryUsageText = new String("JVM Memory Usage: ");
   protected TreeItem uptimeTreeItem;
   protected String uptimeText = "Uptime: ";
   protected int port;
   protected IceStatsTreeItems iceStatsTreeItems;
   protected static DecimalFormat millionFormatter = new DecimalFormat("###,###");

   public BaseStatsMonitor(String hostName, TreeItem parentTreeItem) {
      super(hostName, parentTreeItem);
      this.jvmMemoryUsageTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.jvmMemoryUsageTreeItem.setText(this.jvmMemoryUsageText);
      this.uptimeTreeItem = new TreeItem(this.baseTreeItem, 0);
      this.uptimeTreeItem.setText(this.uptimeText);
      this.iceStatsTreeItems = new IceStatsTreeItems(this.baseTreeItem);
   }

   protected void updateWithLatestStats(BaseServiceStats latestStats, boolean isOnline) {
      if (!isOnline) {
         this.baseTreeItem.setText(this.hostName + " OFFLINE");
         this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 192, 0, 0));
      } else {
         this.baseTreeItem.setText(this.hostName + " ONLINE " + latestStats.version);
         this.baseTreeItem.setForeground(new Color(Display.getCurrent(), 0, 160, 0));
         this.jvmMemoryUsageTreeItem.setText(this.jvmMemoryUsageText + this.toMegaBytes(latestStats.jvmTotalMemory) + " allocated; " + this.toMegaBytes(latestStats.jvmTotalMemory - latestStats.jvmFreeMemory) + " used");
         this.uptimeTreeItem.setText(this.uptimeText + this.toNiceDuration(latestStats.uptime));
         this.iceStatsTreeItems.update(latestStats);
      }

   }
}
