@echo off
cls
java -server -Xmx1536m -cp .;fusion.jar;C:\Dev\common\Ice-3.0.0\lib\Ice.jar;C:\Dev\common\log4j-1.2.9\log4j-1.2.9.jar;C:\Dev\common\JBoss\jbossall-client.jar;C:\Dev\common\JBoss\activation.jar;C:\Dev\common\JBoss\mail.jar com.projectgoth.fusion.smsengine.SMSEngine %1
