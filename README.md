## 1.2.x

### 1.安装并启动docker

```shell
curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh && systemctl start docker
```

### 2.创建并启动容器

```shell
#防火墙放行端口：8088
firewall-cmd --add-port=8088/tcp --permanent
firewall-cmd --reload

mkdir ~/podcast2
cd ~/podcast2
docker run -id --name=podcast2 \
-p 8088:8088 \
-p 6800:6800 \
-v ~/podcast2/xml:/opt/tomcat/tomcat8/webapps/podcast2/xml/ \
-v ~/podcast2/video:/opt/tomcat/tomcat8/webapps/podcast2/video/ \
-v ~/podcast2/audio:/opt/tomcat/tomcat8/webapps/podcast2/audio/ \
-v ~/podcast2/plugin:/opt/tomcat/tomcat8/webapps/podcast2/plugin/ \
-v ~/podcast2/logs:/logs \
yajuhua/podcast2:1.2.5.10
```


