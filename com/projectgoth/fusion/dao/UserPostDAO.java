package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.data.UserPostData;

public interface UserPostDAO {
   UserPostData getUserPost(int var1);

   UserPostData getTopicForUserPost(int var1);
}
