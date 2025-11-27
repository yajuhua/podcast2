FROM debian:bookworm-slim

COPY app.jar /
COPY starter.sh /

EXPOSE 8088
EXPOSE 5005

ENV LANG=zh_CN.UTF-8
ENV LANGUAGE=zh_CN:zh
ENV LC_ALL=zh_CN.UTF-8
ENV RUNNING_IN_DOCKER=true
ENV BIN_DIR=/usr/local/bin
ENV WORKDIR=/tmp/setup_tools
ENV PATH=${PATH}:/root/.deno/bin

ARG TARGETARCH

RUN set -eux; \
    touch init && \
    apt update && \
    apt install -y --no-install-recommends gnupg ca-certificates curl wget tar fonts-wqy-microhei locales unzip && \
    curl -fsSL https://deno.land/install.sh | sh -s -- -y && \
    sed -i '/zh_CN.UTF-8/s/^# //g' /etc/locale.gen && \
    locale-gen zh_CN.UTF-8 && \
    curl -s https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" > /etc/apt/sources.list.d/zulu.list && \
    apt update && \
    mkdir -p ${WORKDIR} && cd ${WORKDIR} && \
    ARCH="${TARGETARCH:-$(uname -m)}" && \
    echo "系统架构: $ARCH" && \
    if [ "$ARCH" = "arm64" ] || [ "$ARCH" = "aarch64" ] || [ "$ARCH" = "amd64" ] || [ "$ARCH" = "x86_64" ]; then \
        apt install -y --no-install-recommends zulu8-ca-jre-headless libicu-dev; \
    else \
        echo "不支持的架构: $ARCH" >&2 && exit 1; \
    fi && \
    if [ "$ARCH" = "arm64" ] || [ "$ARCH" = "aarch64" ]; then \
        wget -q https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_aarch64 -O yt-dlp && \
        chmod +x yt-dlp && mv yt-dlp ${BIN_DIR}/yt-dlp && \
        wget -q https://github.com/nilaoda/N_m3u8DL-RE/releases/download/v0.3.0-beta/N_m3u8DL-RE_v0.3.0-beta_linux-arm64_20241203.tar.gz && \
        tar -xzf N_m3u8DL-RE_v0.3.0-beta_linux-arm64_20241203.tar.gz -C ${BIN_DIR} && \
        rm -f N_m3u8DL-RE_v0.3.0-beta_linux-arm64_20241203.tar.gz && \
        wget -q https://github.com/P3TERX/Aria2-Pro-Core/releases/download/1.36.0_2021.08.22/aria2-1.36.0-static-linux-arm64.tar.gz && \
        tar -xzf aria2-1.36.0-static-linux-arm64.tar.gz -C ${BIN_DIR} && \
        rm -f aria2-1.36.0-static-linux-arm64.tar.gz && \
        wget -q https://github.com/yajuhua/podcast2-static-resources/raw/refs/heads/master/ffmpeg-package/linux/arm64/ffmpeg -O ${BIN_DIR}/ffmpeg && \
        chmod +x ${BIN_DIR}/ffmpeg; \
    elif [ "$ARCH" = "amd64" ] || [ "$ARCH" = "x86_64" ]; then \
        wget -q https://github.com/nilaoda/N_m3u8DL-RE/releases/download/v0.3.0-beta/N_m3u8DL-RE_v0.3.0-beta_linux-x64_20241203.tar.gz && \
        tar -xzf N_m3u8DL-RE_v0.3.0-beta_linux-x64_20241203.tar.gz -C ${BIN_DIR} && \
        rm -f N_m3u8DL-RE_v0.3.0-beta_linux-x64_20241203.tar.gz && \
        wget -q https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux -O yt-dlp && \
        chmod +x yt-dlp && mv yt-dlp ${BIN_DIR}/yt-dlp && \
        wget -q https://github.com/P3TERX/Aria2-Pro-Core/releases/download/1.36.0_2021.08.22/aria2-1.36.0-static-linux-amd64.tar.gz && \
        tar -xzf aria2-1.36.0-static-linux-amd64.tar.gz -C ${BIN_DIR} && \
        rm -f aria2-1.36.0-static-linux-amd64.tar.gz && \
        wget -q https://github.com/yajuhua/podcast2-static-resources/raw/refs/heads/master/ffmpeg-package/linux/amd64/ffmpeg -O ${BIN_DIR}/ffmpeg && \
        chmod +x ${BIN_DIR}/ffmpeg; \
    else \
        echo "不支持的架构: $ARCH" >&2 && exit 1; \
    fi && \
    rm -rf ${WORKDIR} && \
    apt clean && rm -rf /var/lib/apt/lists/*

WORKDIR /
CMD ["sh", "starter.sh"]
