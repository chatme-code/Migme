package com.projectgoth.fusion.slice;

import Ice.LocalException;
import Ice.OperationMode;
import Ice.UnknownUserException;
import Ice.UserException;
import Ice._ObjectDelM;
import IceInternal.BasicStream;
import IceInternal.LocalExceptionWrapper;
import IceInternal.Outgoing;
import java.util.Map;

public final class _UserDelM extends _ObjectDelM implements _UserDel {
   public ContactDataIce acceptContactRequest(ContactDataIce contact, UserPrx contactProxy, int inviterContactListVersion, int inviteeContactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("acceptContactRequest", OperationMode.Normal, __ctx);

      ContactDataIce var10;
      try {
         try {
            BasicStream __os = __og.os();
            contact.__write(__os);
            UserPrxHelper.__write(__os, contactProxy);
            __os.writeInt(inviterContactListVersion);
            __os.writeInt(inviteeContactListVersion);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var16) {
                  throw new UnknownUserException(var16.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ContactDataIce __ret = new ContactDataIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var10 = __ret;
         } catch (LocalException var18) {
            throw new LocalExceptionWrapper(var18, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var10;
   }

   public void accountBalanceChanged(double balance, double fundedBalance, CurrencyDataIce currency, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("accountBalanceChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeDouble(balance);
            __os.writeDouble(fundedBalance);
            currency.__write(__os);
         } catch (LocalException var17) {
            __og.abort(var17);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var15) {
                     throw new UnknownUserException(var15.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var16) {
               throw new LocalExceptionWrapper(var16, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void addContact(ContactDataIce contact, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("addContact", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            contact.__write(__os);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void addPendingContact(String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("addPendingContact", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void addToContactAndBroadcastLists(ContactDataIce contact, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("addToContactAndBroadcastLists", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            contact.__write(__os);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void addToCurrentChatroomList(String chatroom, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("addToCurrentChatroomList", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(chatroom);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void anonymousCallSettingChanged(int setting, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("anonymousCallSettingChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(setting);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void blockUser(String username, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("blockUser", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void contactChangedDisplayPictureOneWay(String source, String displayPicture, long timeStamp, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactChangedDisplayPictureOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(source);
            __os.writeString(displayPicture);
            __os.writeLong(timeStamp);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var14) {
                     throw new UnknownUserException(var14.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var15) {
               throw new LocalExceptionWrapper(var15, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void contactChangedPresenceOneWay(int imType, String source, int presence, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactChangedPresenceOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeString(source);
            __os.writeInt(presence);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var13) {
                     throw new UnknownUserException(var13.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var14) {
               throw new LocalExceptionWrapper(var14, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void contactChangedStatusMessageOneWay(String source, String statusMessage, long timeStamp, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactChangedStatusMessageOneWay", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(source);
            __os.writeString(statusMessage);
            __os.writeLong(timeStamp);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var14) {
                     throw new UnknownUserException(var14.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var15) {
               throw new LocalExceptionWrapper(var15, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void contactDetailChanged(ContactDataIce contact, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactDetailChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            contact.__write(__os);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void contactGroupDeleted(int contactGroupID, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactGroupDeleted", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(contactGroupID);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void contactGroupDetailChanged(ContactGroupDataIce contactGroup, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactGroupDetailChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            contactGroup.__write(__os);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public ContactDataIce contactRequestWasAccepted(ContactDataIce contact, String statusMessage, String displayPicture, int overallFusionPresence, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactRequestWasAccepted", OperationMode.Normal, __ctx);

      ContactDataIce var11;
      try {
         try {
            BasicStream __os = __og.os();
            contact.__write(__os);
            __os.writeString(statusMessage);
            __os.writeString(displayPicture);
            __os.writeInt(overallFusionPresence);
            __os.writeInt(contactListVersion);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ContactDataIce __ret = new ContactDataIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var11 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var11;
   }

   public void contactRequestWasRejected(String contactRequestUsername, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("contactRequestWasRejected", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(contactRequestUsername);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public PresenceAndCapabilityIce contactUpdated(ContactDataIce contact, String oldusername, boolean acceptedContactRequest, boolean changedFusionContact, UserPrx newContactUserProxy, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("contactUpdated", OperationMode.Normal, __ctx);

      PresenceAndCapabilityIce var12;
      try {
         try {
            BasicStream __os = __og.os();
            contact.__write(__os);
            __os.writeString(oldusername);
            __os.writeBool(acceptedContactRequest);
            __os.writeBool(changedFusionContact);
            UserPrxHelper.__write(__os, newContactUserProxy);
            __os.writeInt(contactListVersion);
         } catch (LocalException var21) {
            __og.abort(var21);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var19) {
                  throw var19;
               } catch (UserException var20) {
                  throw new UnknownUserException(var20.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            PresenceAndCapabilityIce __ret = new PresenceAndCapabilityIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var12 = __ret;
         } catch (LocalException var22) {
            throw new LocalExceptionWrapper(var22, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var12;
   }

   public SessionPrx createSession(String sessionID, int presence, int deviceType, int connectionType, int imType, int port, int remotePort, String IP, String mobileDevice, String userAgent, short clientVersion, String language, ConnectionPrx connectionProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("createSession", OperationMode.Normal, __ctx);

      SessionPrx var19;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(sessionID);
            __os.writeInt(presence);
            __os.writeInt(deviceType);
            __os.writeInt(connectionType);
            __os.writeInt(imType);
            __os.writeInt(port);
            __os.writeInt(remotePort);
            __os.writeString(IP);
            __os.writeString(mobileDevice);
            __os.writeString(userAgent);
            __os.writeShort(clientVersion);
            __os.writeString(language);
            ConnectionPrxHelper.__write(__os, connectionProxy);
         } catch (LocalException var28) {
            __og.abort(var28);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var26) {
                  throw var26;
               } catch (UserException var27) {
                  throw new UnknownUserException(var27.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            SessionPrx __ret = SessionPrxHelper.__read(__is);
            __is.endReadEncaps();
            var19 = __ret;
         } catch (LocalException var29) {
            throw new LocalExceptionWrapper(var29, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var19;
   }

   public void disconnect(String reason, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("disconnect", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(reason);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void disconnectFlooder(String reason, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("disconnectFlooder", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(reason);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void emailNotification(int unreadEmailCount, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("emailNotification", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(unreadEmailCount);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void emoticonPackActivated(int emoticonPackId, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("emoticonPackActivated", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(emoticonPackId);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void enteringGroupChat(boolean isCreator, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("enteringGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeBool(isCreator);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public int executeEmoteCommandWithState(String emoteCommand, MessageDataIce message, SessionPrx sessionProxy, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("executeEmoteCommandWithState", OperationMode.Normal, __ctx);

      int var9;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(emoteCommand);
            message.__write(__os);
            SessionPrxHelper.__write(__os, sessionProxy);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var9 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var9;
   }

   public SessionPrx findSession(String sid, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("findSession", OperationMode.Normal, __ctx);

      SessionPrx var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(sid);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            SessionPrx __ret = SessionPrxHelper.__read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public String[] getBlockList(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getBlockList", OperationMode.Normal, __ctx);

      String[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String[] getBlockListFromUsernames(String[] usernames, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getBlockListFromUsernames", OperationMode.Normal, __ctx);

      String[] var7;
      try {
         try {
            BasicStream __os = __og.os();
            StringArrayHelper.write(__os, usernames);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public String[] getBroadcastList(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getBroadcastList", OperationMode.Normal, __ctx);

      String[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int[] getConnectedOtherIMs(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getConnectedOtherIMs", OperationMode.Normal, __ctx);

      int[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int[] __ret = IntArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public ContactList getContactList(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getContactList", OperationMode.Normal, __ctx);

      ContactList var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ContactList __ret = new ContactList();
            __ret.__read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getContactListVersion(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getContactListVersion", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public ContactDataIce[] getContacts(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getContacts", OperationMode.Normal, __ctx);

      ContactDataIce[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ContactDataIce[] __ret = ContactDataIceArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String[] getCurrentChatrooms(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getCurrentChatrooms", OperationMode.Normal, __ctx);

      String[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String[] getEmoticonAlternateKeys(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getEmoticonAlternateKeys", OperationMode.Normal, __ctx);

      String[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String[] getEmoticonHotKeys(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getEmoticonHotKeys", OperationMode.Normal, __ctx);

      String[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getOnlineContactsCount(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getOnlineContactsCount", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public String[] getOtherIMConferenceParticipants(int imType, String otherIMConferenceID, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getOtherIMConferenceParticipants", OperationMode.Normal, __ctx);

      String[] var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeString(otherIMConferenceID);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String[] __ret = StringArrayHelper.read(__is);
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public ContactDataIce[] getOtherIMContacts(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getOtherIMContacts", OperationMode.Normal, __ctx);

      ContactDataIce[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            ContactDataIce[] __ret = ContactDataIceArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public Credential[] getOtherIMCredentials(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getOtherIMCredentials", OperationMode.Normal, __ctx);

      Credential[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            Credential[] __ret = CredentialArrayHelper.read(__is);
            __is.readPendingObjects();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getOverallFusionPresence(String requestingUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getOverallFusionPresence", OperationMode.Normal, __ctx);

      int var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(requestingUsername);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public int getReputationDataLevel(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getReputationDataLevel", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public SessionPrx[] getSessions(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getSessions", OperationMode.Normal, __ctx);

      SessionPrx[] var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            SessionPrx[] __ret = SessionProxyArrayHelper.read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public int getUnreadEmailCount(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getUnreadEmailCount", OperationMode.Normal, __ctx);

      int var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            int __ret = __is.readInt();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public UserDataIce getUserData(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("getUserData", OperationMode.Normal, __ctx);

      UserDataIce var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserDataIce __ret = new UserDataIce();
            __ret.__read(__is);
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public boolean isOnBlockList(String contactUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("isOnBlockList", OperationMode.Normal, __ctx);

      boolean var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(contactUsername);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public boolean isOnContactList(String contactUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("isOnContactList", OperationMode.Normal, __ctx);

      boolean var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(contactUsername);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public void leavingGroupChat(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("leavingGroupChat", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void messageSettingChanged(int setting, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("messageSettingChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(setting);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void newUserContactUpdated(String usernameThatWasModified, boolean acceptedContactRequest, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("newUserContactUpdated", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(usernameThatWasModified);
            __os.writeBool(acceptedContactRequest);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void notifySessionsOfNewContact(ContactDataIce newContact, int contactListVersion, boolean guaranteedIsNew, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("notifySessionsOfNewContact", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            newContact.__write(__os);
            __os.writeInt(contactListVersion);
            __os.writeBool(guaranteedIsNew);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var13) {
                     throw new UnknownUserException(var13.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var14) {
               throw new LocalExceptionWrapper(var14, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void notifyUserJoinedGroupChat(String groupChatId, String username, boolean isMuted, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("notifyUserJoinedGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatId);
            __os.writeString(username);
            __os.writeBool(isMuted);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var13) {
                     throw new UnknownUserException(var13.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var14) {
               throw new LocalExceptionWrapper(var14, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void notifyUserLeftGroupChat(String groupChatId, String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("notifyUserLeftGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatId);
            __os.writeString(username);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void oldUserContactUpdated(String usernameThatWasModified, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("oldUserContactUpdated", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(usernameThatWasModified);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void otherIMAddContact(int imType, String otherIMUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("otherIMAddContact", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeString(otherIMUsername);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public String otherIMInviteToConference(int imType, String otherIMConferenceID, String otherIMUsername, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("otherIMInviteToConference", OperationMode.Normal, __ctx);

      String var9;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeString(otherIMConferenceID);
            __os.writeString(otherIMUsername);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            String __ret = __is.readString();
            __is.endReadEncaps();
            var9 = __ret;
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var9;
   }

   public void otherIMLeaveConference(int imType, String otherIMConferenceID, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("otherIMLeaveConference", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeString(otherIMConferenceID);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void otherIMLogin(int imType, int presence, boolean showOfflineContacts, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("otherIMLogin", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeInt(presence);
            __os.writeBool(showOfflineContacts);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void otherIMLogout(int imType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("otherIMLogout", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void otherIMRemoveContact(int contactId, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("otherIMRemoveContact", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(contactId);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void otherIMRemoved(int imType, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("otherIMRemoved", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void otherIMSendMessage(int imType, String otherIMUsername, String message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("otherIMSendMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(imType);
            __os.writeString(otherIMUsername);
            __os.writeString(message);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void privateChatNowAGroupChat(String groupChatID, String creator, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("privateChatNowAGroupChat", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(groupChatID);
            __os.writeString(creator);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public boolean privateChattedWith(String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("privateChattedWith", OperationMode.Normal, __ctx);

      boolean var7;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var7 = __ret;
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var7;
   }

   public void pushNotification(Message msg, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("pushNotification", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(msg);
            __os.writePendingObjects();
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putAlertMessage(String message, String title, short timeout, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putAlertMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(message);
            __os.writeString(title);
            __os.writeShort(timeout);
         } catch (LocalException var16) {
            __og.abort(var16);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var14) {
                  throw var14;
               } catch (UserException var15) {
                  throw new UnknownUserException(var15.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var17) {
            throw new LocalExceptionWrapper(var17, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putAnonymousCallNotification(String requestingUsername, String requestingMobilePhone, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putAnonymousCallNotification", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(requestingUsername);
            __os.writeString(requestingMobilePhone);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putEvent(UserEventIce event, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putEvent", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeObject(event);
            __os.writePendingObjects();
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putFileReceived(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putFileReceived", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putMessage(MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putMessage", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            message.__write(__os);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putMessageStatusEvent(MessageStatusEventIce mseIce, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putMessageStatusEvent", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            mseIce.__write(__os);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putServerQuestion(String message, String url, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putServerQuestion", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(message);
            __os.writeString(url);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var13) {
                  throw var13;
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void putWebCallNotification(String source, String destination, int gateway, String gatewayName, int protocol, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("putWebCallNotification", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(source);
            __os.writeString(destination);
            __os.writeInt(gateway);
            __os.writeString(gatewayName);
            __os.writeInt(protocol);
         } catch (LocalException var18) {
            __og.abort(var18);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var16) {
                  throw var16;
               } catch (UserException var17) {
                  throw new UnknownUserException(var17.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var19) {
            throw new LocalExceptionWrapper(var19, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void rejectContactRequest(String inviterUsername, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("rejectContactRequest", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(inviterUsername);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void removeContact(int contactid, int contactListVersion, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("removeContact", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeInt(contactid);
            __os.writeInt(contactListVersion);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var12) {
                     throw new UnknownUserException(var12.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var13) {
               throw new LocalExceptionWrapper(var13, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void removeFromCurrentChatroomList(String chatroom, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("removeFromCurrentChatroomList", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(chatroom);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void setCurrentChatListGroupChatSubset(ChatListIce ccl, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("setCurrentChatListGroupChatSubset", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            ccl.__write(__os);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void stopBroadcastingTo(String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("stopBroadcastingTo", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public boolean supportsBinaryMessage(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("supportsBinaryMessage", OperationMode.Normal, __ctx);

      boolean var6;
      try {
         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var11) {
                  throw new UnknownUserException(var11.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            boolean __ret = __is.readBool();
            __is.endReadEncaps();
            var6 = __ret;
         } catch (LocalException var12) {
            throw new LocalExceptionWrapper(var12, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var6;
   }

   public void themeChanged(String themeLocation, Map<String, String> __ctx) throws LocalExceptionWrapper, FusionException {
      Outgoing __og = this.__handler.getOutgoing("themeChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(themeLocation);
         } catch (LocalException var14) {
            __og.abort(var14);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (FusionException var12) {
                  throw var12;
               } catch (UserException var13) {
                  throw new UnknownUserException(var13.ice_name());
               }
            }

            __og.is().skipEmptyEncaps();
         } catch (LocalException var15) {
            throw new LocalExceptionWrapper(var15, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void unblockUser(String username, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("unblockUser", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public UserErrorResponse userCanContactMe(String username, MessageDataIce message, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("userCanContactMe", OperationMode.Normal, __ctx);

      UserErrorResponse var8;
      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(username);
            message.__write(__os);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();

         try {
            if (!__ok) {
               try {
                  __og.throwUserException();
               } catch (UserException var14) {
                  throw new UnknownUserException(var14.ice_name());
               }
            }

            BasicStream __is = __og.is();
            __is.startReadEncaps();
            UserErrorResponse __ret = new UserErrorResponse();
            __ret.__read(__is);
            __is.endReadEncaps();
            var8 = __ret;
         } catch (LocalException var16) {
            throw new LocalExceptionWrapper(var16, false);
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

      return var8;
   }

   public void userDetailChanged(UserDataIce user, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("userDetailChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            user.__write(__os);
         } catch (LocalException var13) {
            __og.abort(var13);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var11) {
                     throw new UnknownUserException(var11.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var12) {
               throw new LocalExceptionWrapper(var12, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void userDisplayPictureChanged(String displayPicture, long timeStamp, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("userDisplayPictureChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(displayPicture);
            __os.writeLong(timeStamp);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var13) {
                     throw new UnknownUserException(var13.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var14) {
               throw new LocalExceptionWrapper(var14, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void userReputationChanged(Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("userReputationChanged", OperationMode.Normal, __ctx);

      try {
         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var9) {
                     throw new UnknownUserException(var9.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var10) {
               throw new LocalExceptionWrapper(var10, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }

   public void userStatusMessageChanged(String statusMessage, long timeStamp, Map<String, String> __ctx) throws LocalExceptionWrapper {
      Outgoing __og = this.__handler.getOutgoing("userStatusMessageChanged", OperationMode.Normal, __ctx);

      try {
         try {
            BasicStream __os = __og.os();
            __os.writeString(statusMessage);
            __os.writeLong(timeStamp);
         } catch (LocalException var15) {
            __og.abort(var15);
         }

         boolean __ok = __og.invoke();
         if (!__og.is().isEmpty()) {
            try {
               if (!__ok) {
                  try {
                     __og.throwUserException();
                  } catch (UserException var13) {
                     throw new UnknownUserException(var13.ice_name());
                  }
               }

               __og.is().skipEmptyEncaps();
            } catch (LocalException var14) {
               throw new LocalExceptionWrapper(var14, false);
            }
         }
      } finally {
         this.__handler.reclaimOutgoing(__og);
      }

   }
}
