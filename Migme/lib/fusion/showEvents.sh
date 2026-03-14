COMMON_BASE=/usr/fusion
FUSION_BASE=/usr/fusion

CLASSPATH=$FUSION_BASE/c3p0-0.9.1.2.jar:$FUSION_BASE/java_memcached-release_2.0.1.jar:$FUSION_BASE/log4j-1.2.9.jar:$FUSION_BASE/Fusion.jar:$FUSION_BASE/mysql-connector-java-5.1.6-bin.jar:$FUSION_BASE/spring-2.5.5.jar:$FUSION_BASE/je-3.3.74.jar:$FUSION_BASE/Ice.jar:$FUSION_BASE:etc

java $JAVA_ARGS -server -Xmx256m -Dconfig.dir=$FUSION_BASE/etc/ -classpath $CLASSPATH com.projectgoth.fusion.userevent.ShowEventsForUser $@

