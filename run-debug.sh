#!/bin/sh
home=`dirname $0`
java -Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG -jar $home/build/jar/openjavacard-tools-plain.jar $@
