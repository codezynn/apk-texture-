#!/bin/sh
# Minimal gradlew that uses the system gradle if wrapper jar is missing
if [ -f gradle/wrapper/gradle-wrapper.jar ]; then
  exec java -jar gradle/wrapper/gradle-wrapper.jar "$@"
else
  exec gradle "$@"
fi
