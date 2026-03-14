package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.data.GroupEventData;

public interface GroupEventDAO {
   GroupEventData getGroupEvent(int var1);

   int persistGroupEvent(GroupEventData var1);

   int updateGroupEvent(GroupEventData var1);

   int updateGroupEventStatus(int var1, int var2);
}
