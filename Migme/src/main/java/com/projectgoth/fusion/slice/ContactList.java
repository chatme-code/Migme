package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;
import java.util.Arrays;

public final class ContactList implements Cloneable, Serializable {
   public ContactGroupDataIce[] contactGroups;
   public ContactDataIce[] contacts;
   public int version;

   public ContactList() {
   }

   public ContactList(ContactGroupDataIce[] contactGroups, ContactDataIce[] contacts, int version) {
      this.contactGroups = contactGroups;
      this.contacts = contacts;
      this.version = version;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         ContactList _r = null;

         try {
            _r = (ContactList)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (!Arrays.equals(this.contactGroups, _r.contactGroups)) {
               return false;
            } else if (!Arrays.equals(this.contacts, _r.contacts)) {
               return false;
            } else {
               return this.version == _r.version;
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __i1;
      if (this.contactGroups != null) {
         for(__i1 = 0; __i1 < this.contactGroups.length; ++__i1) {
            if (this.contactGroups[__i1] != null) {
               __h = 5 * __h + this.contactGroups[__i1].hashCode();
            }
         }
      }

      if (this.contacts != null) {
         for(__i1 = 0; __i1 < this.contacts.length; ++__i1) {
            if (this.contacts[__i1] != null) {
               __h = 5 * __h + this.contacts[__i1].hashCode();
            }
         }
      }

      __h = 5 * __h + this.version;
      return __h;
   }

   public Object clone() {
      Object o = null;

      try {
         o = super.clone();
      } catch (CloneNotSupportedException var3) {
         assert false;
      }

      return o;
   }

   public void __write(BasicStream __os) {
      ContactGroupDataIceArrayHelper.write(__os, this.contactGroups);
      ContactDataIceArrayHelper.write(__os, this.contacts);
      __os.writeInt(this.version);
   }

   public void __read(BasicStream __is) {
      this.contactGroups = ContactGroupDataIceArrayHelper.read(__is);
      this.contacts = ContactDataIceArrayHelper.read(__is);
      this.version = __is.readInt();
   }
}
