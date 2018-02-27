#!/bin/sh
java -Dorg.slf4j.simpleLogger.defaultLogLevel=INFO -jar build/jar/javacard-tools-plain.jar $@
