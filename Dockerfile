FROM yajuhua/podcast2-base-v2:latest
COPY app.jar /data/package/0/
COPY app.jar /
COPY starter.sh /
EXPOSE 8088
EXPOSE 5005
ENV LANG=zh_CN.utf8
ENV RUNNING_IN_DOCKER=true
RUN yt-dlp -U
WORKDIR /
CMD ["sh","starter.sh"]

LABEL author.email="yajuhua@outlook.com"  author.name="yajuhua"


