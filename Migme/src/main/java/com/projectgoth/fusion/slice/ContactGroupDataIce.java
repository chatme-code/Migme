package com.projectgoth.fusion.slice;

import IceInternal.BasicStream;
import java.io.Serializable;

public final class ContactGroupDataIce implements Cloneable, Serializable {
   public int id;
   public String username;
   public String name;

   public ContactGroupDataIce() {
   }

   public ContactGroupDataIce(int id, String username, String name) {
      this.id = id;
      this.username = username;
      this.name = name;
   }

   public boolean equals(Object rhs) {
      if (this == rhs) {
         return true;
      } else {
         ContactGroupDataIce _r = null;

         try {
            _r = (ContactGroupDataIce)rhs;
         } catch (ClassCastException var4) {
         }

         if (_r != null) {
            if (this.id != _r.id) {
               return false;
            } else if (this.username != _r.username && this.username != null && !this.username.equals(_r.username)) {
               return false;
            } else {
               return this.name == _r.name || this.name == null || this.name.equals(_r.name);
            }
         } else {
            return false;
         }
      }
   }

   public int hashCode() {
      int __h = 0;
      int __h = 5 * __h + this.id;
      if (this.username != null) {
         __h = 5 * __h + this.username.hashCode();
      }

      if (this.name != null) {
         __h = 5 * __h + this.name.hashCode();
      }

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
      __os.writeInt(this.id);
      __os.writeString(this.username);
      __os.writeString(this.name);
   }

   public void __read(BasicStream __is) {
      this.id = __is.readInt();
      this.username = __is.readString();
      this.name = __is.readString();
   }
}
