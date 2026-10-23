#!/bin/sh

#
# Copyright © 2015-2021 the original authors.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# ... Gradle Wrapper Script
#

APP_BASE_NAME=`basename "$0"`

# Use the maximum available, or set MAX_FD != -1 to use that value.
MAX_FD="maximum"

warn () {
    echo "$*"
}

die () {
    echo
    echo "$*"
    echo
    exit 1
}

# OS specific support (both for $0 and for report).
cygwin=false
darwin=false
os400=false
case "`uname`" in
  CYGWIN* ) cygwin=true ;;
  Darwin* ) darwin=true ;;
  OS400* ) os400=true ;;
esac

APP_HOME="`pwd -P`"
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine the Java command to use to start the JVM.
if [ -n "$JAVA_HOME" ] ; then
    if [ -x "$JAVA_HOME/jre/sh/java" ] ; then
        # IBM's JDK on AIX uses strange locations for the executables
        JAVACMD="$JAVA_HOME/jre/sh/java"
    else
        JAVACMD="$JAVA_HOME/bin/java"
    fi
else
    if [ -x "/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java" ]; then
        JAVACMD="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home/bin/java"
    elif [ -x "/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java" ]; then
        JAVACMD="/Applications/Android Studio.app/Contents/jbr/Contents/Home/bin/java"
    else
        JAVACMD="java"
    fi
fi

exec "$JAVACMD" "-Dorg.gradle.appname=$APP_BASE_NAME" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
