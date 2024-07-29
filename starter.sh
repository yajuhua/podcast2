#!/bin/bash

# 设置JAR包基础目录
BASE_DIR="/data/package"
echo "Base directory: $BASE_DIR"

# 如果环境变量设置了beta=true，将开启远程调试
if [ "$beta" = "true" ]; then
  JAVA_OPTS="$JAVA_OPTS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005"
  echo "开启调试: $JAVA_OPTS"
else
  # 设置Java选项（例如内存设置等）
  echo "Java Options: $JAVA_OPTS"
fi

# 初始化变量
JAR_FOUND=0

# 检查BASE_DIR是否存在且包含子目录
if [ ! -d "$BASE_DIR" ] || [ -z "$(find "$BASE_DIR" -maxdepth 1 -mindepth 1 -type d)" ]; then
  echo "/data/package目录为空,将运行根目录下app.jar"
  if [ -f /app.jar ]; then
      JAR_FOUND=1;
      # 运行根目录下的app.jar
      java $JAVA_OPTS -jar /app.jar
  fi
else
  # 获取纯数字目录，并按数字降序排序
  DIRS_DESC=$(find "$BASE_DIR" -maxdepth 1 -mindepth 1 -type d | grep -P '/\d+$' | sort -V)
  DIRS_DESC=$(echo "$DIRS_DESC" | tac)
  echo "Directories found (descending): $DIRS_DESC"

  # 尝试启动最大数字的子目录中的app.jar
  for DIR in $DIRS_DESC; do
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

  # 如果启动失败，尝试启动最小数字的子目录中的app.jar
  if [ $JAR_FOUND -eq 0 ]; then
    echo "尝试启动最小数字目录中的jar包"

    # 获取按数字从小到大排序的目录
    DIRS_ASC=$(find "$BASE_DIR" -maxdepth 1 -mindepth 1 -type d | grep -P '/\d+$' | sort -V)

    for DIR in $DIRS_ASC; do
      JAR_PATH="$DIR/app.jar"

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
  fi
fi

# 如果没有成功启动任何JAR包
if [ $JAR_FOUND -eq 0 ]; then
  echo "Error: 无法启动任何jar包，请重新创建容器！"
  exit 1
fi
