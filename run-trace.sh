#!/bin/sh
home=`dirname $0`
java -Dorg.slf4j.simpleLogger.defaultLogLevel=TRACE -jar $home/tool/build/libs/openjavacard-tool-fat-0.1-SNAPSHOT.jar "$@"
