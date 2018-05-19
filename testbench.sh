#!/bin/sh
home=`dirname $0`
java -Dorg.slf4j.simpleLogger.defaultLogLevel=TRACE -jar $home/testbench/build/jar/openjavacard-testbench-0.1-SNAPSHOT-fat.jar $@
