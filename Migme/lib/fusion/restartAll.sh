#!/bin/bash

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

for app in Registry{1..1} AuthenticationService ObjectCache{1..4} EventStore EventSystem ReputationService
do 
    /etc/init.d/mig33.generic $RCMD $app;
done;

for app in MessageLogger SessionCache GatewayTCP_91{19..22} GatewayHTTP_8{0..3} GatewayTCP_25
do 
    /etc/init.d/mig33.generic $RCMD $app;
done;

#for app in BotService BlueLabelService CricketFeed UserNotificationService JobSchedulingService
for app in BotService UserNotificationService JobSchedulingService
do
    /etc/init.d/mig33.generic $RCMD $app;
done;

for app in RewardDispatcher SMSEngine EventQueueWorker{0..1}
do 
    /etc/init.d/mig33.generic $RCMD $app;
done;


echo "=== Done with restartAll.sh ==="

