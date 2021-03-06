# 概览

该应用是基于 netty4 框架开发的支持 [shadowsocks协议](https://github.com/lzh06550107/shadowsocks-java/blob/master/shadowsock_protocol.md) 的客户端和服务端。

主要支持如下加密方式：

- aes-128-cfb
- aes-192-cfb
- aes-256-cfb
- camellia-128-cfb
- camellia-192-cfb
- camellia-256-cfb
- chacha20
- chacha20-ietf
- rc4-md5
- salsa20

# 下载

[下载地址](https://github.com/lzh06550107/shadowsocks-java/releases)

# 使用

## shadowsocks-client

使用之前，请确保安装window系统的java虚拟机，[下载地址](https://www.java.com/zh_CN/download/)

usage:
```
java -jar shadowsocks-client-xxx.jar -l 1080 -s xxx.xxx.xxx.xxx -P 8080 -m aes-128-cfb -p 123456
```

more:

```
usage: java -jar shadowsocks-client-xxx.jar -help
 -help                   usage help
 -l,--listen_port <arg>   local expose port
 -m,--method <arg>       encrypt method
 -P,--port <arg>         remote port
 -p,--password <arg>     remote secret key
 -s,--server_host             remote ip
```



## shadowsocks-server

使用之前，请确保在linux系统安装java虚拟机，请使用包管理器安装。在centos7中，可以使用如下命令安装

```
yum install java-1.8.0-openjdk* -y
```

usage:
```
java -jar shadowsocks-server-xxx.jar -P 8080 -m aes-128-cfb -p 123456
```

more :
```
usage: java -jar shadowsocks-server-xxx.jar -help
 -bn,--boss_number <arg>          boss thread number
 -d,--address                     address bind
 -help                            usage help
 -level,--log_level <arg>         log level
 -m,--method <arg>                encrypt method
 -P,--port <arg>                  port bind
 -p,--password <arg>              password of ssserver
 -wn,--workers_number <arg>       workers thread number
 
```

## 参考

- [另一种实现](https://github.com/TongxiJi/shadowsocks-java)
- [纯java实现](https://github.com/blakey22/shadowsocks-java)


