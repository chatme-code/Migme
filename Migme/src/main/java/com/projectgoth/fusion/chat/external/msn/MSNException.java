package com.projectgoth.fusion.chat.external.msn;

public class MSNException extends Exception {
   private int msnErrorCode;

   public MSNException(String message) {
      super(message);
   }

   public MSNException(int msnErrorCode) {
      super("MSN error (" + msnErrorCode + ") " + getErrorDescription(msnErrorCode));
      this.msnErrorCode = msnErrorCode;
   }

   public int getMSNErrorCode() {
      return this.msnErrorCode;
   }

   public static String getErrorDescription(int msnErrorCode) {
      switch(msnErrorCode) {
      case 200:
         return "Invalid syntax";
      case 201:
         return "Invalid parameter";
      case 205:
         return "Invalid user";
      case 206:
         return "Domain name missing";
      case 207:
         return "Already logged in";
      case 208:
         return "Invalid username";
      case 209:
         return "Nickname change illegal";
      case 210:
         return "User list full";
      case 215:
         return "User already on list";
      case 216:
         return "User not on list";
      case 217:
         return "User not online";
      case 218:
         return "Already in mode";
      case 219:
         return "User is in the opposite list";
      case 223:
         return "Too many groups";
      case 224:
         return "Invalid group";
      case 225:
         return "User not in group";
      case 229:
         return "Group name too long";
      case 230:
         return "Cannot remove group zero";
      case 231:
         return "Invalid group";
      case 280:
         return "Switchboard failed";
      case 281:
         return "Transfer to switchboard failed";
      case 300:
         return "Required field missing";
      case 301:
         return "Too many hits to a FND";
      case 302:
         return "Not logged in";
      case 500:
         return "Internal server error";
      case 501:
         return "Database server error";
      case 502:
         return "Command disabled";
      case 510:
         return "File operation failed";
      case 520:
         return "Memory allocation failed";
      case 540:
         return "Challenge response failed";
      case 600:
         return "Server is busy";
      case 601:
         return "Server is unavailable";
      case 602:
         return "Peer nameserver is down";
      case 603:
         return "Database connection failed";
      case 604:
         return "Server is going down";
      case 605:
         return "Server unavailable";
      case 707:
         return "Could not create connection";
      case 710:
         return "Bad CVR parameters sent";
      case 711:
         return "Write is blocking";
      case 712:
         return "Session is overloaded";
      case 713:
         return "Too many active users";
      case 714:
         return "Too many sessions";
      case 715:
      case 731:
         return "Not expected";
      case 717:
         return "Bad friend file";
      case 800:
         return "Friendly name changes too rapidly";
      case 910:
      case 912:
      case 918:
      case 919:
      case 921:
      case 922:
         return "Server too busy";
      case 911:
         return "Authentication failed. Invalid account name.";
      case 913:
         return "Not allowed when offline";
      case 914:
      case 915:
      case 916:
         return "Server unavailable";
      case 917:
         return "Authentication failed";
      case 920:
         return "Not accepting new users";
      case 923:
         return "Kids Passport without parental consent";
      case 924:
         return "Passport account not yet verified";
      default:
         return "";
      }
   }
}
