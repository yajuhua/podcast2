#!/bin/bash
LASTEST_VERSION=$(curl --silent "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest" | grep '"tag_name":' | sed -E 's/.*"([^"]+)".*/\1/')
ARCH=$(uname -m)
echo "系统架构：$ARCH"
echo "最新版本：$LASTEST_VERSION"

# 安装
function install()
{
    if [ $ARCH == "x86_64" ] || [ $ARCH == "amd64" ]; then
        curl -o /usr/sbin/yt-dlp "https://github.moeyy.xyz/https://github.com/yt-dlp/yt-dlp/releases/download/${LASTEST_VERSION}/yt-dlp_linux"
        chmod +x /usr/sbin/yt-dlp
    elif [ $ARCH == "aarch64" ]; then
        curl -o /usr/sbin/yt-dlp "https://github.moeyy.xyz/https://github.com/yt-dlp/yt-dlp/releases/download/${LASTEST_VERSION}/yt-dlp_linux_aarch64"
        chmod +x /usr/sbin/yt-dlp
    elif [ $ARCH == "armv7l" ]; then
        curl -o /usr/sbin/yt-dlp "https://github.moeyy.xyz/https://github.com/yt-dlp/yt-dlp/releases/download/${LASTEST_VERSION}/yt-dlp_linux_armv7l"
        chmod +x /usr/sbin/yt-dlp
    else
        echo "未匹配，安装失败。"
    fi
}

if [ ! -f /usr/sbin/yt-dlp ]; then
    echo "未发现yt-dlp,开始安装。"
    install
elif [ "$LASTEST_VERSION" == "$(yt-dlp --version)" ]; then
    echo "无更新"
    echo "目前版本是：$(yt-dlp --version)"
else
    echo "有更新"
    install
    echo "更新完成！"
    echo "目前版本是:$(yt-dlp --version)"
fi