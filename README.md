<div align=center><img width = "200" height = "200" src="./images/975x975-logo.001.png"></div>

<br>
<p align="center">
<img src="https://img.shields.io/github/v/release/yajuhua/podcast2?color=&label=release" alt="Release" />
<img src="https://shields.io/docker/pulls/yajuhua/podcast2" alt="docker-pull" />
<img src="https://img.shields.io/badge/jdk-8-blue.svg" alt="JDK" />
<img src="https://img.shields.io/badge/tomcat-8.5.59-blue.svg" alt="Tomcat" />
<img src="https://img.shields.io/badge/license-Apache2.0-green.svg" alt="apache-licenses" />
</p>
<h2 align=center><p>Podcast2</p></h2>
<hr>

## 项目说明

将视频网站转换成播客订阅。

## 快速开始

### 1.安装并启动Docker

```shell
curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh && systemctl start docker
```

#### 2.创建并启动容器

````shell
docker volume create podcast2
mkdir  ~/podcast2
cd ~/podcast2
docker run -id --name=podcast2 \
-p 8088:8088 \
--restart=always \
--mount source=podcast2,destination=/data \
yajuhua/podcast2:latest
````
### 3.防火墙放行端口
````shell
#以下是centos7，其他系统自行搜索。
firewall-cmd --add-port=8088/tcp --permanent
firewall-cmd --reload
````
#### 4.进入面板

> 默认访问地址 [http://你的IP地址:8088]()
>
> 默认用户名：admin <br>
> 默认密码：123456

![登录](https://yajuhua.github.io/images/login.png)


#### 4.扫描二维码添加到播客APP（如：AntennaPod）

<img width="500" src="https://yajuhua.github.io/images/AntennaPod-1.jpg" alt="链接地址" style="zoom:25%;" /><img width="500" src="https://yajuhua.github.io/images/AntennaPod-2.jpg" alt="AntennaPod-2" style="zoom:25%;" />

## 播客APP

<a href="https://www.apple.com/apple-podcasts/" target="_blank">
              <img src="https://www.apple.com/v/apple-podcasts/c/images/overview/hero_icon__c135x5gz14mu_large.png" width="35" alt="Apple Podcasts">
            </a><a href="https://podcastaddict.com/" target="_blank"><img title="Podcast Addict" alt="Podcast Addict" src="https://pod.link/assets/apps/podcastaddict.svg" width="35"></a>  <a href="https://antennapod.org/" target="_blank">
              <img src="https://antennapod.org/assets/branding/logo-full-horizontal-dynamic.svg" width="230" alt="AntennaPod">          </a>

## 交流群
**945797272**
## Star History
![Star History](https://api.star-history.com/svg?repos=yajuhua/podcast2)
## 使用到的项目
- [https://github.com/yt-dlp/yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [https://github.com/nilaoda/N_m3u8DL-RE](https://github.com/nilaoda/N_m3u8DL-RE)
- [https://github.com/aria2/aria2](https://github.com/aria2/aria2)
- [https://github.com/SocialSisterYi/bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect)
## 参考
- [https://github.com/mxpv/podsync](https://github.com/mxpv/podsync)
## 免责声明

**此项目仅供研究、学习和交流，请勿用于商业或非法用途， 开发者与协作者不对使用者负任何法律责任， 使用者自行承担因不当使用所产生的后果与责任。**



