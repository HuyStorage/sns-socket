#!/bin/bash
RUN_DIR={TARGET_DIR}
MYAPP_CLASSPATH=$RUN_DIR/cfg
PATH_LIBS=$RUN_DIR/lib/*.jar
LIBS=""

for file in $PATH_LIBS
do
  LIBS=$file:$LIBS
done

export JAVA_OPTS="-Djava.awt.headless=true -Dfile.encoding=UTF-8 -server -XX:+DisableExplicitGC"
MYAPP_CLASSPATH=$MYAPP_CLASSPATH:$LIBS

/opt/jdk11/bin/java -Xms1g -Xmx4g -cp $MYAPP_CLASSPATH sns.socket.WebSocketServer

echo "Application Socket Started"