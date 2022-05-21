#!/bin/bash

ARTIFACT_ENV=${JAR_ENV}
ARTIFACT_NAME=${JAR_NAME}

ARTIFACT=${ARTIFACT_NAME}
PROJECT=/mnt/${ARTIFACT}

if [[ -z ${ARTIFACT_NAME} ]] || [[ -z ${ARTIFACT_ENV} ]] ; then
    echo "must require 2 params: JAR_NAME, JAR_ENV"
    exit 1
fi

function CheckCmd(){
    if [[ $? != 0 ]] ; then
        echo "$1"
        exit 1
    fi
}

mkdir -p "${PROJECT}"
# shellcheck disable=SC2119
CheckCmd
cd "${PROJECT}"

if [[ ${ARTIFACT_ENV} ==  http* ]] || [[ ${ARTIFACT_ENV} ==  https* ]] || [[ ${ARTIFACT_ENV} ==  file* ]]; then
    START_ARGS="--boot.active-conf=$ARTIFACT_ENV"
elif [[ -n ${ARTIFACT_ENV} ]] ; then
    START_ARGS="--spring.profiles.active=$ARTIFACT_ENV"
fi
### jvm optimization args，
#jvm 的最小 heap 大小，建议和-Xmx一样， 防止因为内存收缩／突然增大带来的性能影响。默认值512M
if [[ -z ${JVM_Xms} ]]; then
    JVM_Xms=512M
fi
#jvm 的最大 heap 大小。默认值512M
if [[ -z ${JVM_Xmx} ]]; then
    JVM_Xmx=512M
fi
#jvm 中 New Generation 的大小，此值对系统性能影响较大,Sun官方推荐配置为整个堆的3/8
if [[ -z ${JVM_Xmn} ]]; then
    JVM_Xmn=192M
fi
#Direct Memory使用到达了这个大小，就会强制触发Full GC。默认值64M
if [[ -z ${JVM_MaxDirectMemorySize} ]]; then
    JVM_MaxDirectMemorySize=64M
fi

JAVA_OPTS="-server -Xms${JVM_Xms} -Xmx${JVM_Xmx} -Xmn${JVM_Xmn} -XX:MaxDirectMemorySize=${JVM_MaxDirectMemorySize} -XX:+UseNUMA -XX:+UseParallelGC -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=${PROJECT}"
if [ $GC_PATH ] ; then
    JAVA_OPTS=" -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCApplicationStoppedTime -Xloggc:${GC_PATH}"
fi
nohup java $JAVA_OPTS -jar ${ARTIFACT}.jar "$START_ARGS" > nohup.out 2>&1 &
