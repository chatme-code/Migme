package com.projectgoth.fusion.cache;

import com.danga.MemCached.MemCachedClient;
import com.projectgoth.fusion.common.MemCachedClientWrapper;
import com.projectgoth.fusion.common.MemCachedUtils;
import com.projectgoth.fusion.data.ContactData;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.springframework.util.StringUtils;

public class ContactList {
   public static Set<ContactData> newContactList() {
      return new HashSet();
   }

   public static Set<ContactData> newContactList(Collection<ContactData> contacts) {
      return contacts == null ? null : new HashSet(contacts);
   }

   public static String getKey(String username) {
      return MemCachedUtils.getCacheKeyInNamespace("CL", username);
   }

   public static Set<ContactData> getContactList(MemCachedClient instance, String username) {
      if (instance == null) {
         instance = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.contactList);
      }

      return (Set)instance.get(getKey(username));
   }

   public static boolean setContactList(MemCachedClient instance, String username, Set<ContactData> contactList) {
      if (instance == null) {
         instance = MemCachedClientWrapper.getMemCachedClient(MemCachedUtils.Instance.contactList);
      }

      Calendar now = Calendar.getInstance();
      now.add(6, 5);
      return instance.set(getKey(username), contactList, now.getTime());
   }

   public static boolean deleteContactList(MemCachedClient instance, String username) {
      return instance.delete(getKey(username));
   }

   public static Set<String> getFusionContactUsernames(MemCachedClient instance, String username) {
      Set<ContactData> contacts = getContactList(instance, username);
      return contacts != null ? fusionContactUsernames(contacts) : null;
   }

   public static Set<String> fusionContactUsernames(Set<ContactData> contacts) {
      Set<String> contactUsernames = new HashSet();
      Iterator i$ = contacts.iterator();

      while(i$.hasNext()) {
         ContactData contactData = (ContactData)i$.next();
         if (StringUtils.hasLength(contactData.fusionUsername)) {
            contactUsernames.add(contactData.fusionUsername);
         }
      }

      return contactUsernames;
   }

   public static SortedSet<ContactData> sortByDisplayName(Set<ContactData> contacts) {
      SortedSet<ContactData> sortedSet = new TreeSet(new ContactList.SortByDisplayName());
      sortedSet.addAll(contacts);
      return sortedSet;
   }

   private static class SortByDisplayName implements Comparator<ContactData> {
      private SortByDisplayName() {
      }

      public int compare(ContactData o1, ContactData o2) {
         return o1.displayName.toUpperCase().compareTo(o2.displayName.toUpperCase());
      }

      // $FF: synthetic method
      SortByDisplayName(Object x0) {
         this();
      }
   }
}
