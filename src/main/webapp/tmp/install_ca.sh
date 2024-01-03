#!/bin/bash
# 使用 install_ca.sh 你的域名 密钥命名
yum install curl -y
yum install openssl -y
yum install cronie -y
#安装acme
curl https://get.acme.sh | sh

#创建软链接
ln -s  /root/.acme.sh/acme.sh /usr/local/bin/acme.sh

#安装socat
yum install socat -y

#开放80端口
#firewall-cmd --add-port=80/tcp --permanent && firewall-cmd --reload

#注册账号
acme.sh --register-account -m $(date +%s)@podcast2.com
#申请证书
acme.sh  --issue -d $1 --standalone -k ec-256

if [ $? -ne 0 ];then
	#进行强制
	acme.sh  --force  --issue -d $1 --standalone -k ec-256
	if [ $? -ne 0  ];then
	  #申请失败！，检测域名是否正确、容器是否开放80端口
		return 1
	fi

fi

#安装证书
acme.sh --installcert -d $1 --ecc  --key-file   $2.key   --fullchain-file $2.crt
if [ $? -ne 0 ];then
  #安装证书失败!
	return 2
fi
