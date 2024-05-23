FROM openjdk:8-jre-slim-buster
COPY app.jar /
COPY yt-dlp /usr/sbin/
COPY ffmpeg /usr/sbin/
EXPOSE 8088
WORKDIR /
ENV TZ=Asia/Shanghai
CMD ["java","-jar","app.jar"]

LABEL author.email="yajuhua@outlook.com"  author.name="yajuhua"


