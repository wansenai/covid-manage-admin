FROM sacher/centos7-jdk8:2

MAINTAINER sacher <sacherer@163.com>

#设置启动命令
ENTRYPOINT ["/bin/sh","-c","cd /mnt/$JAVA_NAME &&  nohup java -server -Xms512M -Xmx512M -Xmn192M -XX:MaxDirectMemorySize=64M -XX:+UseNUMA -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/mnt/$JAVA_NAME  -jar $JAVA_NAME.jar \"--spring.profiles.active=$JAVA_EVN\" > nohup.out 2>&1"]
