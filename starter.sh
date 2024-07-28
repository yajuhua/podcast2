#!/bin/bash

# 设置JAR包基础目录
BASE_DIR="/data/package"
echo "Base directory: $BASE_DIR"

# 获取纯数字目录，并按数字排序
DIRS=$(find "$BASE_DIR" -maxdepth 1 -mindepth 1 -type d | \
       grep -P '/\d+$' | \
       sort -t'/' -k2,2nr)
echo "Directories found: $DIRS"

# 设置Java选项（例如内存设置等）
echo "Java Options: $JAVA_OPTS"

# 如果环境变量设置了beta=true,将开启远程调试
if [ "$beta" = "true" ]; then
    echo "开启调试"
	JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
	echo "$JAVA_OPTS"
fi

# 初始化变量
JAR_FOUND=0

# 循环尝试启动每个JAR包
for DIR in $DIRS; do
  JAR_PATH="$DIR/app.jar"
  echo "Checking $JAR_PATH"
  
  if [ -f "$JAR_PATH" ]; then
    echo "正在尝试启动jar包 $JAR_PATH"
    
    java $JAVA_OPTS -jar "$JAR_PATH"
    
    # 检查应用是否成功启动（假设应用以0状态码成功启动）
    if [ $? -eq 0 ]; then
      echo "启动成功 $JAR_PATH"
      JAR_FOUND=1
      break
    else
      echo "启动失败 $JAR_PATH"
    fi
  else
    echo "文件不存在 $JAR_PATH"
  fi
done

# 如果没有成功启动任何JAR包
if [ $JAR_FOUND -eq 0 ]; then
  echo "Error: 无法启动任何jar包，请重新创建容器！"
  exit 1
fi
