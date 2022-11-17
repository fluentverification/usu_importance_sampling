#!/bin/bash

if [ "$PRISM_DIR" = "" ]; then
	PRISM_DIR="/usr/local/prism-src"
fi

# Class to run
if [ "$PRISM_MAINCLASS" = "" ]; then
	PRISM_MAINCLASS=imsam.Main
fi

# Locate imsam repository
REPODIR="$(dirname "$(readlink -f "$0")")"/..

# Set up CLASSPATH:
PRISM_CLASSPATH="$REPODIR"/build/libs/*:"$PRISM_DIR"/prism:"$PRISM_DIR"/prism/classes:"$PRISM_DIR"/prism/lib/*

# Set up pointers to libraries
# As above, we look in both the top-level and the prism sub-directory
PRISM_LIB_PATH="$PRISM_DIR"/prism/lib
if [[ "$OSTYPE" == "darwin"* ]]; then
	export DYLD_LIBRARY_PATH="$PRISM_LIB_PATH"
else
	export LD_LIBRARY_PATH="$PRISM_LIB_PATH"
fi

# Command to launch Java
if [ "$PRISM_JAVA" = "" ]; then
	# On OS X, we want to avoiding calling java from the /usr/bin link
	# since it causes problems with dynamic linking (DYLD_LIBRARY_PATH)
	if [ -x /usr/libexec/java_home ]; then
		PRISM_JAVA=`/usr/libexec/java_home`"/bin/java"
	else
		PRISM_JAVA=java
	fi
fi

# Run PRISM through Java
# grep used to suppress warning message from log4j
"$PRISM_JAVA" -Djava.library.path="$PRISM_LIB_PATH" -classpath "$PRISM_CLASSPATH" "$PRISM_MAINCLASS" "$@" | grep -v "getCallerClass"
