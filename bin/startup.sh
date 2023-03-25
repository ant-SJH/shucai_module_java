#!/bin/bash
export JAVA_HOME=/usr/local/jdk1.8.0_211
export JRE_HOME=$JAVA_HOME/jre
export CLASSPATH=$JAVA_HOME/lib:$JRE_HOME/lib:$CLASSPATH
export PATH=$JAVA_HOME/bin:$JRE_HOME/bin:$PATH
cd /home/robot/shucai_module_java/bin/
time=$(date "+%Y%m%d%H")
DEPLOY_DIR=$(dirname "$PWD")
PROJECT=${DEPLOY_DIR##*/}
echo "PROJECT ${PROJECT}"
MAIN_JARS=`ls $DEPLOY_DIR|grep .jar|awk '{print "'$DEPLOY_DIR'/"$0}'|tr "\n" " "`
echo "main jar:$MAIN_JARS"
cd $DEPLOY_DIR
is_exist()
{
    pid=`ps -ef | grep ${MAIN_JARS} | grep -v grep | awk '{print $2}' `
    if [ "$pid" ]
    then
        return 1
    else
        return 0
    fi
}
case "$1" in

  start)
    echo -n "Starting: ${PROJECT}"
    is_exist
    if [ "${?}" -eq "1" ]
      then
      echo "${PROJECT} is already running. pid=${pid} ."
      else
     nohup java -Dlogging.config=${DEPLOY_DIR}/config/logback-spring.xml -Dspring.profiles.active=${2} -Dspring.config.location=${DEPLOY_DIR}/config/application-${2}.yml -Duser.timezone=Asia/Shanghai -Djava.awt.headless=true -Djava.net.preferIPv4Stack=true -server -Xmx${3} -Xms${4} -Xmn${5} -Xss${6} -XX:MetaspaceSize=128m -XX:+DisableExplicitGC -XX:+UseParNewGC -XX:ParallelGCThreads=4 -XX:+UseConcMarkSweepGC -XX:+CMSParallelRemarkEnabled -XX:+UseCMSCompactAtFullCollection -XX:LargePageSizeInBytes=128m -XX:+UseFastAccessorMethods -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=85 -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintTenuringDistribution -XX:+PrintGCApplicationStoppedTime -Xloggc:/home/robot/data/log/gc.log -XX:+UseGCLogFileRotation -XX:NumberOfGCLogFiles=32 -XX:GCLogFileSize=64m -XX:ErrorFile=/home/robot/data/log/${PROJECT}${time}.log -jar ${MAIN_JARS} >> /dev/null 2>&1 &
  while [ 1 ]
    do
        echo -e ".\c"
        sleep 1
        is_exist
        if [ "${?}" -eq "1" ]
        then
            echo "${PROJECT} is ok!. pid=${pid} ."
            break
        fi
    done
    fi
    exit 0
    ;;

  stop)
    echo -n "Stopping ${PROJECT}: "
    is_exist
    if [ "${?}" -eq "1" ]
    then
        kill -9 $pid
        echo "${PROJECT} kill"
    else
        echo "${PROJECT} is not running"
    fi
    exit 0
    ;;

  status)
    is_exist
    if [ "${?}" -eq "1" ]
    then
        echo "Running $pid"
    else
        echo "Stopped"
    fi
    exit 0
    ;;

  restart)
    $DEPLOY_DIR$0 stop
    sleep 3
    $DEPLOY_DIR$0 start
    ;;

  *)
    echo "Usage: {start|stop|status|restart}"
    exit 1
    ;;
esac
