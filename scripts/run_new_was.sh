#!/bin/bash

REPOSITORY=/home/ubuntu/blitz
cd $REPOSITORY

JAR_NAME=$(ls $REPOSITORY/build/libs/ | grep 'SNAPSHOT.jar' | tail -n 1)
JAR_PATH=$REPOSITORY/build/libs/$JAR_NAME

CURRENT_PORT=$(cat /home/ubuntu/service_url.inc | grep -Po '[0-9]+' | tail -1)
TARGET_PORT=0

echo "> Current port of running WAS is ${CURRENT_PORT}."

if [ ${CURRENT_PORT} -eq 8081 ]; then
  TARGET_PORT=8082
elif [ ${CURRENT_PORT} -eq 8082 ]; then
  TARGET_PORT=8081
else
  echo "> No WAS is connected to nginx"
fi

# 스프링 부트 애플리케이션에 프로파일을 dev로 설정
nohup java -jar -Dserver.port=${TARGET_PORT} -Dspring.profiles.active=dev $JAR_PATH > /home/ubuntu/nohup.out 2>&1 &

echo "> Now new WAS runs at ${TARGET_PORT} with 'dev' profile."
exit 0
