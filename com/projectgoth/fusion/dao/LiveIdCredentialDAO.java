package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.domain.LiveIdCredential;

public interface LiveIdCredentialDAO {
   LiveIdCredential getCredential(String var1);

   void persistCredential(LiveIdCredential var1);
}
