#!/bin/bash

if [ ! -e "/usr/fusion/Fusion.jar" ]; then
	echo "Copying Fusion.jar file... "
	cp -f /data/fusion/target/Fusion-*.jar /usr/fusion/Fusion.jar
	echo "OK"
fi

RCMD=restart
if [ $# -gt 0 ] ; then
	RCMD=$1
fi
echo Running $RCMD
#/etc/init.d/jboss stop
#/etc/init.d/jboss start
if [ $RCMD == restart -o $RCMD == stop ] ; then
  bash `dirname $0`/stop-jboss.sh
fi
if [ $RCMD == restart -o $RCMD == start ] ; then
  bash `dirname $0`/start-jboss.sh
fi

for app in Registry1 AuthenticationService ObjectCache1 EventStore EventSystem ReputationService
do 
    /etc/init.d/mig33.generic $RCMD $app;
done;

for app in MessageLogger SessionCache GatewayTCP_9119 GatewayHTTP_83 GatewayTCP_25
do 
    /etc/init.d/mig33.generic $RCMD $app;
done;

#for app in BotService BlueLabelService CricketFeed UserNotificationService JobSchedulingService
for app in BotService UserNotificationService JobSchedulingService
do
    /etc/init.d/mig33.generic $RCMD $app;
done;

for app in RewardDispatcher EventQueueWorker0 ImageServer RDCS0
do 
    /etc/init.d/mig33.generic $RCMD $app;
done;

echo "=== Done with restartMIAB.sh ==="

