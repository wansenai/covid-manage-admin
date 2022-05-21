#!/bin/bash

function CheckCmd(){
    if [[ $? != 0 ]]; then
        echo "$1"
        exit -1
    fi
}

JAR_NAME=$1
dockerN=$2
ENV=$3
BRANCH=$4

if [[ -z ${BRANCH} ]]; then
    BRANCH=master
fi

git checkout -f ${BRANCH}
CheckCmd

git submodule update --init --recursive

git submodule foreach git checkout -f master
git submodule foreach git pull
git pull


### create

mvn clean install
CheckCmd

JARS=/data/deploys/${JAR_NAME}
mkdir -p ${JARS}
CheckCmd

mv ${JAR_NAME}/target/${JAR_NAME}-1.0-SNAPSHOT.jar ${JARS}/${JAR_NAME}.jar
CheckCmd

echo "the jar at $JARS directory"

deploy ${JAR_NAME} ${dockerN} ${ENV}
