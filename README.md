<div align=center><img width = "200" height = "200" src="./images/975x975-logo.png"></div>

<br>
<p align="center">
<img src="https://img.shields.io/github/v/release/yajuhua/podcast2" alt="Release" />
<img src="https://shields.io/docker/pulls/yajuhua/podcast2" alt="docker-pull" />
<img src="https://img.shields.io/badge/jdk-8-blue.svg" alt="JDK" />
<img src="https://img.shields.io/badge/license-Apache2.0-green.svg" alt="apache-licenses" />
<img src="https://github.com/yajuhua/podcast2/actions/workflows/build-test.yml/badge.svg" alt="test" />
<img src="https://github.com/yajuhua/podcast2/actions/workflows/build-latest.yml/badge.svg" alt="latest" />
</p>
<h2 align=center><p>Podcast2</p></h2>
<hr>

## 项目说明
- 基于spring boot开发
- 将视频网站转换成播客订阅
## [文档](https://yajuhua.github.io/)
## 快速开始

#### 使用Docker
1. 安装并启动Docker
```shell
curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh && systemctl start docker
```
2. 创建并启动Docker
````shell
docker volume create podcast2
docker run -id --name=podcast2 \
-p 8088:8088 \
--restart=always \
--mount source=podcast2,destination=/data \
yajuhua/podcast2:latest
````
3. 防火墙放行端口
````shell
#以下是centos7，其他系统自行搜索。
firewall-cmd --add-port=8088/tcp --permanent
firewall-cmd --reload
````
#### 使用Windows
1. 下载podcast2压缩包
- [下载](https://github.com/yajuhua/podcast2/releases/latest/download/podcast2-win-x64.zip)
- [加速下载](https://github.moeyy.xyz/https://github.com/yajuhua/podcast2/releases/latest/download/podcast2-win-x64.zip)
2. 双击启动
![windows-start](./images/windows-start.jpg)

#### 使用macOS
1. 下载podcast2压缩包
- [下载](https://github.com/yajuhua/podcast2/releases/latest/download/podcast2-osx-x64.zip) 
- [加速下载](https://github.moeyy.xyz/https://github.com/yajuhua/podcast2/releases/latest/download/podcast2-osx-x64.zip)
2. 解压得到 platform文件夹
3. 赋执行权限
````shell
chmod -R a+x platform
````
4. 启动
````shell
#进入platform/osx
./start.sh
````
#### 进入面板

> 默认访问地址 [http://你的IP地址:8088]()
>
> 默认用户名：admin <br>
> 默认密码：123456

![登录](./images/login.png)

#### 安装插件
> 项目默认是没有添加插件的，自行按需添加
#### 支持网站
| 网站 <img width=200/>                          | 状态                                                                                                                 |
|:---------------------------------------------|--------------------------------------------------------------------------------------------------------------------|
| [干净世界](https://ganjing.com/)                 | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-ganjingworld.com.yml/badge.svg"   /> |
| [vimeo](https://vimeo.com/)                  | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-vimeo.com.yml/badge.svg"   />        |
| [ntdm](https://www.ntdm.tv)                  | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-ntdm.yml/badge.svg"     />           |
| [girigirilove](https://www.girigirilove.com) | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-girigirilove.com.yml/badge.svg" />   | 
| [youtube](https://www.youtube.com)           | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-youtube.com.yml/badge.svg"   />      | 
| [bilibili](https://www.bilibili.com)         | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-bilibili.com.yml/badge.svg"  />      | 
| [抖音](https://www.douyin.com/)                | <img src="https://github.com/yajuhua/podcast2/actions/workflows/plugin-status-douyin.com.yml/badge.svg"    />      | 

1. 安装插件
![安装插件](./images/installPlugin.png)

2. 设置插件，如果有的话
![设置插件](./images/setting.png)

#### 添加订阅
> 以干净世界为例
1. 复制主页链接
![复制主页链接](./images/channelUrl.png)

2. 粘贴并点击Add
![添加订阅](./images/add.png)

3. 点击二维码
![二维码](./images/QRcode.png)

4. 扫描二维码添加到播客APP（如：AntennaPod）

<img width="500" src="./images/AntennaPod-1.jpg" alt="链接地址" style="zoom:25%;" /><img width="500" src="./images/AntennaPod-2.jpg" alt="AntennaPod-2" style="zoom:25%;" />

#### 播客APP

<a href="https://www.apple.com/apple-podcasts/" target="_blank">
              <img src="https://www.apple.com/v/apple-podcasts/c/images/overview/hero_icon__c135x5gz14mu_large.png" width="35" alt="Apple Podcasts">
            </a><a href="https://podcastaddict.com/" target="_blank"><img title="Podcast Addict" alt="Podcast Addict" src="https://pod.link/assets/apps/podcastaddict.svg" width="35"></a>  <a href="https://antennapod.org/" target="_blank">
              <img src="https://antennapod.org/assets/branding/logo-full-horizontal-dynamic.svg" width="230" alt="AntennaPod">          </a>



## 常见问题
#### 哔哩哔哩设置
> 风控太严，只能选择授权的方式
##### 参考
> 风控太严，只能选择授权的方式，参考 [Web端Cookie刷新](https://socialsisteryi.github.io/bilibili-API-collect/docs/login/cookie_refresh.html)。
##### 打开浏览器登录哔哩哔哩，最好无痕窗口
![登录哔哩哔哩](./images/b-login.png)
##### 复制cookie
![复制cookie](./images/b-cookie.png)

##### 复制ac_time_value
![复制ac_time_valu](./images/ac_time_value.png)

##### 注意！这样子相当于登录了，复制后要清理哔哩哔哩浏览器记录，否则会与本插件冲突
<br>

#### 忘记密码
##### 进入数据卷目录
````shell
[root@centos7 ~]# docker volume inspect podcast2
[
    {
        "CreatedAt": "2024-03-23T19:57:47+08:00",
        "Driver": "local",
        "Labels": null,
        "Mountpoint": "/var/lib/docker/volumes/podcast2/_data",
        "Name": "podcast2",
        "Options": null,
        "Scope": "local"
    }
]
[root@centos7 ~]# cd /var/lib/docker/volumes/podcast2/_data
[root@centos7 _data]# ls
cert  config  database  logs  plugin  resources  tmp
[root@centos7 _data]# cd config/
````
##### 修改config.json
````shell
#改成true
{"initUserNameAndPassword":true}
````
##### 重启后将恢复默认用户名和密码
> 用户名 admin
> 密码 123456

#### 开启HTTPS
> 目前仅支持通过上传证书和密钥文件来实现
##### 文件格式要求
````shell
#证书文件格式必须是crt
#密钥文件格式必须是key
````
##### 重启后并以https访问
<br>

##### 更新podcast2
> 数据保留
````shell
# 停止容器
docker stop podcast2

# 删除容器
docker rm podcast2

# 删除本地镜像
docker rmi yajuhua/podcast2:latest

# 拉取最新镜像
docker pull yajuhua/podcast2:latest

#创建新的容器
docker run -id --name=podcast2 \
-p 8088:8088 \
--restart=always \
--mount source=podcast2,destination=/data \
yajuhua/podcast2:latest

````
##### 重新开始
> 如果使用最新版都无法解决，可以试试删除所有数据
````shell
# 停止容器
docker stop podcast2

# 删除容器
docker rm podcast2

# 删除本地镜像
docker rmi yajuhua/podcast2:latest

# 删除数据
docker volume rm podcast2

# 拉取最新镜像
docker pull yajuhua/podcast2:latest

#创建新的容器
docker run -id --name=podcast2 \
-p 8088:8088 \
--restart=always \
--mount source=podcast2,destination=/data \
yajuhua/podcast2:latest

````
## Invidious API
yt-dlp可能会出现[Sign in to confirm you’re not a bot. This helps protect our community](https://github.com/yt-dlp/yt-dlp/issues/10128)导致无法下载的情况。
目前只能通过设置invidious API进行下载，下面是invidious API列表，找一个能有用的设置即可。
- https://redirect.invidious.io/
- https://api.invidious.io/

## 插件bug或失效
由于插件是并非使用官方接口，难免存在不稳定性。若发现插件失效，请[issues](https://github.com/yajuhua/podcast2/issues/new/choose)


## Star History
![Star History](https://api.star-history.com/svg?repos=yajuhua/podcast2)
## 使用到的项目
- [https://github.com/yt-dlp/yt-dlp](https://github.com/yt-dlp/yt-dlp)
- [https://github.com/nilaoda/N_m3u8DL-RE](https://github.com/nilaoda/N_m3u8DL-RE)
- [https://github.com/aria2/aria2](https://github.com/aria2/aria2)
- [https://github.com/SocialSisterYi/bilibili-API-collect](https://github.com/SocialSisterYi/bilibili-API-collect)

## 使用的加速站
- [https://github.moeyy.xyz](https://github.moeyy.xyz)
- [https://hub.gitmirror.com/](https://hub.gitmirror.com/)
- [https://ghproxy.com/](https://ghproxy.com/)
## 参考
- [https://github.com/mxpv/podsync](https://github.com/mxpv/podsync)
## 免责声明
**此项目仅供研究、学习和交流，请勿用于商业或非法用途， 开发者与协作者不对使用者负任何法律责任， 使用者自行承担因不当使用所产生的后果与责任。**

## 鸣谢
特别感谢 [JetBrains](https://www.jetbrains.com) 为开源项目提供免费的 [IntelliJ IDEA](https://www.jetbrains.com/idea)的授权  
[<img src="./images/jetbrains.svg" width="200"/>](https://www.jetbrains.com)
## 🐧交流群
945797272



