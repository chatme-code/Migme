@echo off
cls
java -server -Xmx1536m -cp .;fusion.jar;C:\Dev\common\log4j-1.2.9\log4j-1.2.9.jar;C:\Dev\common\JBoss\jbossall-client.jar com.projectgoth.fusion.externalfeed.ExchangeRateFeed AUD 60
