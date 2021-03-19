#!/bin/sh
home=`dirname $0`
java -Dorg.slf4j.simpleLogger.defaultLogLevel=TRACE -jar $home/testbench/build/libs/openjavacard-testbench-fat-0.1-SNAPSHOT.jar "$@"
