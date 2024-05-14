FROM yajuhua/podcast2-base-v2:latest
ADD app.jar /
EXPOSE 8088
ENV LANG zh_CN.utf8
RUN yt-dlp -U
CMD ["start-app"]

LABEL author.email="yajuhua@outlook.com"  author.name="yajuhua"


