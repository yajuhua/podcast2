FROM debian:bookworm-slim

COPY app.jar /
COPY starter.sh /

EXPOSE 8088
EXPOSE 5005

ENV LANG=zh_CN.utf8
ENV RUNNING_IN_DOCKER=true
ENV BIN_DIR=/usr/local/bin
ENV WORKDIR=/tmp/setup_tools

RUN set -eux; \
    apt update && \
    apt install -y --no-install-recommends libicu-dev gnupg ca-certificates curl wget tar && \
    curl -s https://repos.azul.com/azul-repo.key | gpg --dearmor -o /usr/share/keyrings/azul.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/azul.gpg] https://repos.azul.com/zulu/deb stable main" > /etc/apt/sources.list.d/zulu.list && \
    apt update && \
    mkdir -p ${WORKDIR} && cd ${WORKDIR} && \
    ARCH=$(uname -m) && \
    echo "系统架构: $ARCH" && \
    if [ "$ARCH" = "armv7l" ]; then \
        echo "使用 tar.gz 安装 JRE for armv7l"; \
        wget -q https://cdn.azul.com/zulu-embedded/bin/zulu8.76.0.17-ca-jre8.0.402-linux_aarch32hf.tar.gz; \
        tar -xzf zulu8.76.0.17-ca-jre8.0.402-linux_aarch32hf.tar.gz -C /usr/local; \
        ln -sf /usr/local/zulu8.76.0.17-ca-jre8.0.402-linux_aarch32hf/bin/java /usr/local/bin/java; \
        rm -f zulu8.76.0.17-ca-jre8.0.402-linux_aarch32hf.tar.gz; \
    else \
        apt install -y --no-install-recommends zulu8-ca-jre-headless; \
    fi && \
    if [ "$ARCH" = "armv7l" ]; then \
        wget -q https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_armv7l -O yt-dlp && \
        chmod +x yt-dlp && mv yt-dlp ${BIN_DIR}/yt-dlp && \
        wget -q https://github.com/P3TERX/Aria2-Pro-Core/releases/download/1.36.0_2021.08.22/aria2-1.36.0-static-linux-armhf.tar.gz && \
        tar -xzf aria2-1.36.0-static-linux-armhf.tar.gz -C ${BIN_DIR} && \
        rm -f aria2-1.36.0-static-linux-armhf.tar.gz && \
        wget -q https://github.com/yajuhua/podcast2/raw/refs/heads/v2/ffmpeg-package/linux/armv7/ffmpeg -O ${BIN_DIR}/ffmpeg && \
        chmod +x ${BIN_DIR}/ffmpeg; \
    elif [ "$ARCH" = "aarch64" ]; then \
        wget -q https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_aarch64 -O yt-dlp && \
        chmod +x yt-dlp && mv yt-dlp ${BIN_DIR}/yt-dlp && \
        wget -q https://github.com/nilaoda/N_m3u8DL-RE/releases/download/v0.3.0-beta/N_m3u8DL-RE_v0.3.0-beta_linux-arm64_20241203.tar.gz && \
        tar -xzf N_m3u8DL-RE_v0.3.0-beta_linux-arm64_20241203.tar.gz -C ${BIN_DIR} && \
        rm -f N_m3u8DL-RE_v0.3.0-beta_linux-arm64_20241203.tar.gz && \
        wget -q https://github.com/P3TERX/Aria2-Pro-Core/releases/download/1.36.0_2021.08.22/aria2-1.36.0-static-linux-arm64.tar.gz && \
        tar -xzf aria2-1.36.0-static-linux-arm64.tar.gz -C ${BIN_DIR} && \
        rm -f aria2-1.36.0-static-linux-arm64.tar.gz && \
        wget -q https://github.com/yajuhua/podcast2/raw/refs/heads/v2/ffmpeg-package/linux/arm64/ffmpeg -O ${BIN_DIR}/ffmpeg && \
        chmod +x ${BIN_DIR}/ffmpeg; \
    elif [ "$ARCH" = "x86_64" ]; then \
        wget -q https://github.com/nilaoda/N_m3u8DL-RE/releases/download/v0.3.0-beta/N_m3u8DL-RE_v0.3.0-beta_linux-x64_20241203.tar.gz && \
        tar -xzf N_m3u8DL-RE_v0.3.0-beta_linux-x64_20241203.tar.gz -C ${BIN_DIR} && \
        rm -f N_m3u8DL-RE_v0.3.0-beta_linux-x64_20241203.tar.gz && \
        wget -q https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux -O yt-dlp && \
        chmod +x yt-dlp && mv yt-dlp ${BIN_DIR}/yt-dlp && \
        wget -q https://github.com/P3TERX/Aria2-Pro-Core/releases/download/1.36.0_2021.08.22/aria2-1.36.0-static-linux-amd64.tar.gz && \
        tar -xzf aria2-1.36.0-static-linux-amd64.tar.gz -C ${BIN_DIR} && \
        rm -f aria2-1.36.0-static-linux-amd64.tar.gz && \
        wget -q https://github.com/yajuhua/podcast2/raw/refs/heads/v2/ffmpeg-package/linux/amd64/ffmpeg -O ${BIN_DIR}/ffmpeg && \
        chmod +x ${BIN_DIR}/ffmpeg; \
    else \
        echo "不支持的架构: $ARCH" >&2 && exit 1; \
    fi && \
    rm -rf ${WORKDIR} && \
    apt clean && rm -rf /var/lib/apt/lists/*

WORKDIR /
CMD ["sh", "starter.sh"]
