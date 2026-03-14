#ant jar.inc
REMOTESVR=da1
if [ $# -gt 0 ] ; then
	REMOTESVR=$1
fi
REMOTEJDIR=/etc/jboss/server/default/
REMOTEFDIR=/etc/fusion

#WARFILE=migbo_datasvc.war
echo "deploying to ${REMOTESVR}"
#scp -p target/artifacts/${WARFILE} root@${REMOTESVR}:/tmp/
#scp -p target/artifacts/lib/fusion.jar target/artifacts/datasyncsvc.war root@${REMOTESVR}:/tmp/
. deploy-get-jarfile.sh
ssh -t ${REMOTESVR} \
"
sudo rm -fv /tmp/fusion.jar
"
scp -p $JARFILE ${REMOTESVR}:/tmp/fusion.jar
if [ $? -ne 0 ] ; then exit 2; fi;
DTSTR=`date +'%Y%m%d-%H%M%S'`
ssh -t ${REMOTESVR} \
"
sudo cp -v /tmp/fusion.jar ${REMOTEFDIR}/Fusion.jar
sudo cp -v /tmp/fusion.jar ${REMOTEJDIR}/deploy/Fusion.jar
#mv -v /tmp/datasyncsvc.war ${REMOTEJDIR}/deploy/
echo files deployed on ${REMOTESVR};
#sleep 2;
#export PATH=$PATH:~/bin

sudo nohup ${REMOTEFDIR}/restartAll.sh &> /tmp/restartAll.log.$DTSTR &
sleep 2;
tail -f /tmp/restartAll.log.$DTSTR
"
if [ $? -ne 0 ] ; then echo failed to run deploy on ${REMOTESVR}; exit 2; fi;
echo ===== done, deployed to jboss on ${REMOTESVR} =====
