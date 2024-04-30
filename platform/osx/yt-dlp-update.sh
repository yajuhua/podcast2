#!/bin/bash

PATH="$PWD/yt-dlp:$PATH"

NOW_VERSION=$(yt-dlp --version | head -n 1)

LATEST_VERSION=$(curl -s "https://api.github.com/repos/yt-dlp/yt-dlp/releases/latest" | grep -o '"tag_name": ".*"' | sed 's/"tag_name": "//' | sed 's/"//')


if [ "$LATEST_VERSION" == "$NOW_VERSION" ]; then
    echo "No update available"
    echo "$NOW_VERSION"
else
    echo "Update available"
    curl -L "https://github.moeyy.xyz/https://github.com/yt-dlp/yt-dlp/releases/download/$LATEST_VERSION/yt-dlp_macos.zip" -o "$PWD/yt-dlp.zip"
    rm -rf yt-dlp
    unzip -d yt-dlp yt-dlp.zip
    mv yt-dlp/yt-dlp_macos yt-dlp/yt-dlp
    chmod a+x yt-dlp/yt-dlp
    rm -rf yt-dlp.zip
    echo "Update completed"
    yt-dlp --version
fi
