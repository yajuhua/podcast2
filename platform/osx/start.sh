#!/bin/bash

JAVA="$PWD/jre8/bin"
DATA="$PWD/data"
PATH="$JAVA:$PWD:$PATH"
directory="$PWD/lib"
classpath="$PWD/classes"

for file in $(find "$directory" -type f); do
    classpath="$classpath:$file"
done

java -Dfile.encoding=utf-8 -classpath "$classpath" -Dpodcast2.data.data-path="$DATA" io.github.yajuhua.podcast2.Podcast2Application
