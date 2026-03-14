package com.projectgoth.fusion.bothunter;

import com.projectgoth.fusion.slice.SuspectGroupIce;
import com.projectgoth.fusion.slice.SuspectIce;

public class SuspectGroup {
   private Suspect[] members;
   private int innocentPortCount;

   public SuspectGroup(Suspect[] members, int innocentPortCount) {
      this.members = members;
      this.innocentPortCount = innocentPortCount;
   }

   public SuspectGroup(SuspectGroupIce iceGroup) {
      this.members = new Suspect[iceGroup.members.length];

      for(int i = 0; i < this.members.length; ++i) {
         this.members[i] = new Suspect(iceGroup.members[i]);
      }

      this.innocentPortCount = iceGroup.innocentPortCount;
   }

   public Suspect[] getMembers() {
      return this.members;
   }

   public int getInnocentPortCount() {
      return this.innocentPortCount;
   }

   public String getClientIP() {
      return this.members[0].getClientIP();
   }

   public SuspectGroupIce toIceObject() {
      SuspectIce[] groupIce = new SuspectIce[this.members.length];

      for(int i = 0; i < this.members.length; ++i) {
         groupIce[i] = this.members[i].toIceObject();
      }

      SuspectGroupIce sgi = new SuspectGroupIce();
      sgi.members = groupIce;
      sgi.innocentPortCount = this.innocentPortCount;
      return sgi;
   }
}
