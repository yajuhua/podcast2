FROM yajuhua/podcast2-base-v2:latest
COPY app.jar /
EXPOSE 8088
ENV LANG zh_CN.utf8
ENV RUNNING_IN_DOCKER true
RUN yt-dlp -U
WORKDIR /
CMD ["java","-jar","app.jar"]

LABEL author.email="yajuhua@outlook.com"  author.name="yajuhua"


