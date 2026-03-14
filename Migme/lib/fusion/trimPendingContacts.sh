echo "JAVA_ARGS=$JAVA_ARGS"

COMMON_BASE=/usr/fusion
FUSION_BASE=/usr/fusion

CLASSPATH=$FUSION_BASE/trimPendingContacts.jar:$FUSION_BASE/c3p0-0.9.1.2.jar:$FUSION_BASE/java_memcached-release_2.0.1.jar:$FUSION_BASE/log4j-1.2.9.jar:$FUSION_BASE/mysql-connector-java-5.1.6-bin.jar:$FUSION_BASE/spring-2.5.5.jar:$FUSION_BASE:etc:$FUSION_BASE/jbossall-client.jar

echo "CLASSPATH = $CLASSPATH"

java $JAVA_ARGS -server -Xmx256m -DuserCount=20000 -Dlog.filename=trimPendingContacts -Dlog.dir=$FUSION_BASE/logs/ -Dconfig.dir=$FUSION_BASE/etc/ -classpath $CLASSPATH com.projectgoth.fusion.maintenance.TrimPendingContacts $@

