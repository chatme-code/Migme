package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.data.ContactData;
import java.util.Set;

public interface ContactDAO {
   Set<ContactData> getContactListForUser(String var1);
}
