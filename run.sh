#!/bin/sh
home=`dirname $0`
java -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO -jar $home/build/jar/openjavacard-tools-0.1-SNAPSHOT-fat.jar $@
