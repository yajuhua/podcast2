FROM debian:stable-slim
COPY app.jar /
COPY yt-dlp /usr/sbin/
COPY ffmpeg /usr/sbin/
COPY jre8 /usr/sbin/
RUN ln -s /usr/sbin/jre8/bin/java /usr/sbin/java
ENV LANG=C.UTF-8
EXPOSE 8088
WORKDIR /
ENV TZ=Asia/Shanghai
CMD ["java","-jar","app.jar"]

LABEL author.email="yajuhua@outlook.com"  author.name="yajuhua"


