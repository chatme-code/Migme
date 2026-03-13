package com.projectgoth.fusion.movedeletedcontacts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

public class MoveDeletedContacts {
   private static final String APP_NAME = "Move Deleted Contacts";
   static String dbHost;
   static String dbUsername;
   static String dbPassword;
   static Connection conn = null;
   static PreparedStatement psContactsToMove = null;
   static PreparedStatement psUpdatePhoneCall = null;
   static PreparedStatement psUpdateMessageDestination = null;
   static PreparedStatement psCopyContact = null;
   static PreparedStatement psDeleteContact = null;
   static ResultSet rs = null;
   static Vector<Integer> contactIDs = new Vector();
   static int startContactId;
   static int endContactId;
   static int numContactsMoved = 0;
   static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

   public static void main(String[] args) {
      System.out.println("Move Deleted Contacts version @version@\nCopyright (c) 2005-2008 Project Goth Inc. All rights reserved");
      if (args.length != 5) {
         System.out.print("Usage: MoveDeletedContacts [Master Database Host] [Database Username] [Database Password] [Start Contact.ID (optional)] [End Contact.ID (optional)]");
      } else {
         System.out.println("Connecting to DB on host " + args[0] + " as " + args[1] + "/" + args[2]);
         String userName = args[1];
         String password = args[2];
         String url = "jdbc:mysql://" + args[0] + "/fusion";

         try {
            startContactId = Integer.parseInt(args[3]);
            endContactId = Integer.parseInt(args[4]);
         } catch (NumberFormatException var44) {
            System.out.print("Error: Start and End Contact.IDs must be numeric");
            return;
         }

         try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(url, userName, password);
         } catch (Exception var43) {
            System.out.println("Unable to connect to DB. Exception: " + var43.getMessage());
            return;
         }

         System.out.println(dateFormatter.format(new Date()) + " Starting process to move deleted contacts with IDs between " + startContactId + " and " + endContactId);

         label376: {
            try {
               psUpdatePhoneCall = conn.prepareStatement("UPDATE PhoneCall SET ContactID = NULL WHERE ContactID = ?");
               psUpdateMessageDestination = conn.prepareStatement("UPDATE MessageDestination SET ContactID = NULL WHERE ContactID = ?");
               psCopyContact = conn.prepareStatement("INSERT INTO ContactDeleted SELECT * FROM Contact WHERE ID = ?");
               psDeleteContact = conn.prepareStatement("DELETE FROM Contact WHERE ID = ?");

               while(true) {
                  conn.setAutoCommit(true);
                  psContactsToMove = conn.prepareStatement("SELECT ID FROM Contact WHERE ID >= ? AND ID <= ? AND Status = 0");
                  psContactsToMove.setInt(1, startContactId);
                  System.out.print(dateFormatter.format(new Date()) + " Moving deleted contacts with IDs between " + startContactId + " and ");
                  startContactId += 10000;
                  if (startContactId > endContactId) {
                     startContactId = endContactId;
                  }

                  System.out.println(startContactId + " (have moved " + numContactsMoved + " so far)");
                  psContactsToMove.setInt(2, startContactId);
                  rs = psContactsToMove.executeQuery();
                  contactIDs.clear();

                  while(rs.next()) {
                     contactIDs.add(rs.getInt(1));
                  }

                  rs.close();
                  psContactsToMove.close();
                  conn.setAutoCommit(false);
                  Iterator i$ = contactIDs.iterator();

                  while(i$.hasNext()) {
                     int contactId = (Integer)i$.next();
                     psUpdatePhoneCall.setInt(1, contactId);
                     psUpdatePhoneCall.executeUpdate();
                     psUpdateMessageDestination.setInt(1, contactId);
                     psUpdateMessageDestination.executeUpdate();
                     psCopyContact.setInt(1, contactId);
                     if (psCopyContact.executeUpdate() != 1) {
                        System.out.println("Unable to copy contact with ID " + contactId);
                        conn.rollback();
                     } else {
                        psDeleteContact.setInt(1, contactId);
                        if (psDeleteContact.executeUpdate() != 1) {
                           System.out.println("Unable to delete contact with ID " + contactId);
                           conn.rollback();
                        } else {
                           conn.commit();
                           ++numContactsMoved;
                        }
                     }
                  }

                  if (startContactId >= endContactId) {
                     break label376;
                  }
               }
            } catch (Exception var45) {
               try {
                  if (conn != null) {
                     conn.rollback();
                  }
               } catch (SQLException var42) {
               }

               System.out.println("An exception occurred:");
               var45.printStackTrace();
            } finally {
               try {
                  if (rs != null) {
                     rs.close();
                  }
               } catch (SQLException var41) {
                  rs = null;
               }

               try {
                  if (psContactsToMove != null) {
                     psContactsToMove.close();
                  }
               } catch (SQLException var40) {
                  psContactsToMove = null;
               }

               try {
                  if (psUpdatePhoneCall != null) {
                     psUpdatePhoneCall.close();
                  }
               } catch (SQLException var39) {
                  psUpdatePhoneCall = null;
               }

               try {
                  if (psUpdateMessageDestination != null) {
                     psUpdateMessageDestination.close();
                  }
               } catch (SQLException var38) {
                  psUpdateMessageDestination = null;
               }

               try {
                  if (psCopyContact != null) {
                     psCopyContact.close();
                  }
               } catch (SQLException var37) {
                  psCopyContact = null;
               }

               try {
                  if (psDeleteContact != null) {
                     psDeleteContact.close();
                  }
               } catch (SQLException var36) {
                  psDeleteContact = null;
               }

               try {
                  if (conn != null) {
                     conn.close();
                  }
               } catch (SQLException var35) {
                  conn = null;
               }

            }

            return;
         }

         System.out.println("Successfully moved " + numContactsMoved + " deleted contacts");
      }
   }
}
