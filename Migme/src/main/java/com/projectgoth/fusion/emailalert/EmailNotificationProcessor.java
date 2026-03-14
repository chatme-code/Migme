package com.projectgoth.fusion.emailalert;

import Ice.LocalException;
import com.projectgoth.fusion.common.RMIExceptionHelper;
import com.projectgoth.fusion.data.AccountEntrySourceData;
import com.projectgoth.fusion.ejb.EJBHomeCache;
import com.projectgoth.fusion.interfaces.User;
import com.projectgoth.fusion.interfaces.UserHome;
import com.projectgoth.fusion.slice.UserPrx;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.Iterator;

public class EmailNotificationProcessor implements Runnable {
   Socket socket;
   String from;
   String subject;
   boolean parsedFirstToLine = false;
   boolean parsedFirstFromLine = false;
   boolean parsedFirstSubjectLine = false;
   boolean finishedProcessing = false;

   public EmailNotificationProcessor(Socket socket) {
      this.socket = socket;
   }

   public void run() {
      PrintWriter out = null;
      BufferedReader in = null;
      String subject = null;
      String from = null;

      try {
         out = new PrintWriter(this.socket.getOutputStream(), true);
         in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
         out.println("220 " + EmailAlert.hostName + " ESMTP");
         EmailAlert.logger.debug(">" + this.socket.getInetAddress().getHostAddress() + ": 220 " + EmailAlert.hostName + " ESMTP");
         String inputLine = in.readLine();
         if (inputLine == null) {
            EmailAlert.logger.warn("Accepted connection from " + this.socket.getInetAddress().getHostAddress() + " but no data was received. Terminating connection");
            out.close();
            in.close();
            this.socket.close();
            return;
         }

         EmailAlert.logger.debug("<" + this.socket.getInetAddress().getHostAddress() + ": " + inputLine);
         if (inputLine.startsWith("EHLO") || inputLine.startsWith("HELO")) {
            out.println("250 Hello");
            EmailAlert.logger.debug(">" + this.socket.getInetAddress().getHostAddress() + ": 250 Hello");

            while(true) {
               while(true) {
                  while((inputLine = in.readLine()) != null) {
                     EmailAlert.logger.debug("<" + this.socket.getInetAddress().getHostAddress() + ": " + inputLine);
                     if (!inputLine.startsWith("MAIL") && !inputLine.startsWith("RCPT") && !inputLine.startsWith("RSET") && !inputLine.equals(".")) {
                        if (inputLine.startsWith("DATA")) {
                           out.println("354 Ok.");
                           EmailAlert.logger.debug(">" + this.socket.getInetAddress().getHostAddress() + ": 354 Ok.");
                        } else {
                           if (inputLine.startsWith("QUIT")) {
                              out.println("221 Bye.");
                              EmailAlert.logger.debug(">" + this.socket.getInetAddress().getHostAddress() + ": 221 Bye.");
                              out.close();
                              in.close();
                              this.socket.close();
                              return;
                           }

                           if (inputLine.startsWith("Subject: ")) {
                              if (this.parsedFirstSubjectLine) {
                                 subject = inputLine.substring(9, inputLine.length());
                              } else {
                                 this.parsedFirstSubjectLine = true;
                              }
                           } else if (inputLine.startsWith("From: ")) {
                              if (this.parsedFirstFromLine) {
                                 from = inputLine.substring(6, inputLine.length());
                              } else {
                                 this.parsedFirstFromLine = true;
                              }
                           } else if (inputLine.startsWith("To: ")) {
                              if (this.parsedFirstToLine && !this.finishedProcessing) {
                                 do {
                                    HashSet<String> recipients = this.findEmailAliases(inputLine.toLowerCase());
                                    if (recipients != null) {
                                       Iterator i$ = recipients.iterator();

                                       while(i$.hasNext()) {
                                          String recipient = (String)i$.next();
                                          EmailAlert.receivedNotificationsCounter.add();
                                          this.processNotification(recipient, from, subject);
                                       }
                                    }

                                    inputLine = in.readLine();
                                 } while(inputLine != null && inputLine.startsWith("  "));

                                 this.finishedProcessing = true;
                              } else {
                                 this.parsedFirstToLine = true;
                              }
                           }
                        }
                     } else {
                        out.println("250 Ok.");
                        EmailAlert.logger.debug(">" + this.socket.getInetAddress().getHostAddress() + ": 250 Ok.");
                     }
                  }

                  return;
               }
            }
         }

         EmailAlert.logger.warn("Accepted connection from " + this.socket.getInetAddress().getHostAddress() + " but didn't receive EHLO or HELO. Instead we received \"" + inputLine + "\". Terminating connection");
         out.close();
         in.close();
         this.socket.close();
      } catch (Exception var22) {
         EmailAlert.logger.warn("Socket exception on port " + this.socket.getLocalPort() + ": " + var22.getMessage());

         try {
            out.close();
            in.close();
            this.socket.close();
         } catch (Exception var21) {
         }

         return;
      } finally {
         try {
            EmailAlert.logger.debug("Closing connection from " + this.socket.getInetAddress().getHostAddress());
            out.close();
            in.close();
            this.socket.close();
         } catch (Exception var20) {
         }

      }

   }

   private HashSet<String> findEmailAliases(String line) {
      int atIndex = line.indexOf("@" + EmailAlert.mailDomain);
      if (atIndex <= 0) {
         EmailAlert.logger.warn("Unable to process email notification as I couldn't find a recipient where I was supposed to. SMTP line: " + line);
         return null;
      } else {
         HashSet recipients = new HashSet();

         do {
            int startIndex;
            for(startIndex = atIndex - 1; startIndex >= 0 && line.charAt(startIndex) != '<' && line.charAt(startIndex) != '\'' && line.charAt(startIndex) != ' ' && line.charAt(startIndex) != '"'; --startIndex) {
            }

            String alias = null;

            try {
               alias = line.substring(startIndex + 1, atIndex);
            } catch (Exception var7) {
            }

            if (alias != null && alias.length() > 0) {
               recipients.add(alias.toLowerCase());
            }

            atIndex = line.indexOf("@" + EmailAlert.mailDomain, atIndex + 1);
         } while(atIndex > 0);

         return recipients;
      }
   }

   private void processNotification(String recipient, String from, String subject) {
      if (EmailAlert.aliasesToIgnore.indexOf(recipient) < 0) {
         EmailAlert.logger.debug("Processing email notification. To=" + recipient + " From=" + from + " Subject=" + subject);
         User userEJB = null;
         String password = null;

         try {
            userEJB = (User)EJBHomeCache.getObject("ejb/User", UserHome.class);
         } catch (Exception var15) {
            EmailAlert.logger.warn("Unable to create EJB to retrieve the password of the user " + recipient + ". Exception: " + var15.getMessage());
            return;
         }

         try {
            password = userEJB.processEmailNotification(recipient, from, subject, new AccountEntrySourceData(EmailAlert.class));
         } catch (RemoteException var13) {
            EmailAlert.logger.warn("Unable to call UserEJB to process email notification. Exception: " + RMIExceptionHelper.getRootMessage(var13));
            return;
         } catch (Exception var14) {
            EmailAlert.logger.warn("Unable to call UserEJB to process email notification. Exception: " + var14.getMessage());
            return;
         }

         UserPrx userPrx = null;

         try {
            userPrx = EmailAlert.icePrxFinder.findOnewayUserPrx(recipient);
         } catch (LocalException var11) {
            EmailAlert.logger.warn("Unable to search for User proxy for the user " + recipient + ": " + var11.getMessage());
            return;
         } catch (Exception var12) {
            EmailAlert.logger.warn("Unable to search for User proxy for the user " + recipient + ": " + var12.getMessage());
            return;
         }

         if (userPrx != null) {
            int unreadEmailCount;
            try {
               unreadEmailCount = EmailAlert.getUnreadEmailCountFromIMAP(recipient, password);
            } catch (Exception var10) {
               EmailAlert.logger.warn("Unable to query IMAP server for unread email count for the user " + recipient + ": " + var10.getMessage());
               return;
            }

            try {
               userPrx.emailNotification(unreadEmailCount);
            } catch (LocalException var9) {
               EmailAlert.logger.warn("Unable to notify User proxy for the user " + recipient + ": " + var9.getMessage());
            }
         }

         EmailAlert.processedNotificationsCounter.add();
      }
   }
}
