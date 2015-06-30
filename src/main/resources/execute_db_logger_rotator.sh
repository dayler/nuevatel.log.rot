#!/bin/bash

PROPERTIES=/ivr/properties/logrotate.properties
LOGFILE=/ivr/tmp/logrotate.tmp

echo "Starting app..."
cd /ivr/lib
nohup java -Xmx512m -XX:+UseParallelGC -XX:ParallelGCThreads=4 -cp logger.rotator-1.0.jar:. com.nuevatel.logrot.LoggerRotator $PROPERTIES > $LOGFILE &
echo "The App was started..."
