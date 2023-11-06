<div align=center><img width = "200" height = "200" src="./images/975x975-logo.001.png"></div>

<br>
<p align="center">
<img src="https://img.shields.io/badge/Release-1.2.8-<COLOR>.svg" alt="Release" />
<img src="https://img.shields.io/badge/JDK-17-blue.svg" alt="JDK" />
<img src="https://img.shields.io/badge/Tomcat-8.5.59-blue.svg" alt="Tomcat" />
<img src="https://img.shields.io/badge/license-Apache2.0-green.svg" alt="apache-licenses" />
<img src="https://img.shields.io/badge/Vue-2.6.13-blue.svg" alt="Vue" />
<img src="https://img.shields.io/badge/element_ui-2.15.14-blue.svg" alt="Vue" />
</p>
<h2 align=center><p>Podcast2</p></h2>
<hr>

## 项目说明

将视频网站转换成播客。

## 快速开始

### 1.安装并启动Docker

```shell
curl -fsSL https://get.docker.com -o get-docker.sh && sh get-docker.sh && systemctl start docker
```

### 2.创建并启动容器

```shell
mkdir  ~/podcast2
cd ~/podcast2
docker run -id --name=podcast2 \
--restart=always \
-p 8088:8088 \
-p 6800:6800 \
-v ~/podcast2/xml:/opt/tomcat/tomcat8/webapps/podcast2/xml/ \
-v ~/podcast2/video:/opt/tomcat/tomcat8/webapps/podcast2/video/ \
-v ~/podcast2/audio:/opt/tomcat/tomcat8/webapps/podcast2/audio/ \
-v ~/podcast2/plugin:/opt/tomcat/tomcat8/webapps/podcast2/plugin/ \
-v ~/podcast2/logs:/logs \
yajuhua/podcast2:1.2.8
```
### 3.防火墙放行8088端口
````shell
#以下是centos7，其他系统自行搜索。
firewall-cmd --add-port=8088/tcp --permanent
firewall-cmd --reload
````
#### 4.进入面板

> 默认访问地址 [http://你的IP地址:8088/podcast2]()
>
> 默认用户名：admin <br>
> 默认密码：1

![登录](https://yajuhua.github.io/images/login.png)

## 添加插件

> 项目默认是没有添加插件的，自行按需添加

#### 1.下载插件

| 网站 <img width=200/>            | 名称<img width=200/> | 版本<img width=200/> | 下载地址<img width=200/>                                                                                  |
| :------------------------------- | -------------------- | -------------------- |-------------------------------------------------------------------------------------------------------|
| [干净世界](https://ganjing.com/) | ganjing              | 1.2.1                | [点击下载](https://github.com/yajuhua/plugin/raw/master/ganjing/Ganjing3-1.2.1-jar-with-dependencies.jar) |
| [ntdm](https://www.ntdm.tv)              | ntdm8                | 1.2.1                | [点击下载](https://github.com/yajuhua/plugin/raw/master/ntdm8/ntdm8-1.2.1-jar-with-dependencies.jar)      |

#### 2.进入管理页面

![管理页面](https://yajuhua.github.io/images/manage.png)

#### 3.点击选取插件

![添加插件](https://yajuhua.github.io/images/add-plugin.png)

#### 4.点击上传到服务器

![上传到服务器](https://yajuhua.github.io/images/upload-plugin.png)

![上传成功](https://yajuhua.github.io/images/upload-plugin-ok.png)



## 添加订阅

> 以干净世界为例

#### 1.复制主页链接

![复制主页链接](https://yajuhua.github.io/images/add-sub-example.png)

#### 2.粘贴并点击Add

![粘贴并点击](https://yajuhua.github.io/images/add-sub-example-2.png)

#### 3.添加成功后会跳转到订阅列表

![订阅列表](https://yajuhua.github.io/images/add-sub-example-ok.png)

#### 4.扫描二维码添加到播客APP（如：AntennaPod）

<img width="500" src="https://yajuhua.github.io/images/AntennaPod-1.jpg" alt="链接地址" style="zoom:25%;" /><img width="500" src="https://yajuhua.github.io/images/AntennaPod-2.jpg" alt="AntennaPod-2" style="zoom:25%;" />

## 播客APP

<a href="https://www.apple.com/apple-podcasts/" target="_blank">
              <img src="https://www.apple.com/v/apple-podcasts/c/images/overview/hero_icon__c135x5gz14mu_large.png" width="35" alt="Apple Podcasts">
            </a><a href="https://podcastaddict.com/" target="_blank"><img title="Podcast Addict" alt="Podcast Addict" src="https://pod.link/assets/apps/podcastaddict.svg" width="35"></a>  <a href="https://antennapod.org/" target="_blank">
              <img src="https://antennapod.org/assets/branding/logo-full-horizontal-dynamic.svg" width="230" alt="AntennaPod">          </a>

## 免责声明

**此项目仅供研究、学习和交流，请勿用于商业或非法用途， 开发者与协作者不对使用者负任何法律责任， 使用者自行承担因不当使用所产生的后果与责任。**




