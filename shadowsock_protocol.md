# 协议
ss是一个基于SOCKS5的安全拆分代理

```
client <---> ss-local <-- [encrypted] --> ss-remtoe <---> target
```

ss本地组件（ss-local）就像传统的SOCKS5服务器，为客户端提供代理服务。它将数据流和数据包从客户端加密和转发到Shadowsocks远程组件（ss-remote），ss-remote再解密并转发到目标地址。来自目标地址的回复类似地加密并由ss-remote中继回ss-local，后者解密并最终返回到原始客户端

## 地址
Shadowsocks中使用的地址遵循SOCKS5地址格式

```
[1-byte type][variable-length host][2-byte port]
```

第一个字节的地址类型如下：
+ `0x01`：host是4字节的`ipv4`地址
+ `0x03`: host是一个可变长度的字符串，第一个字节是host的长度，接下来是不超过255字节的域名
+ `0x04`: host是16字节的`ipv6`地址

端口号是2字节的无符号整型值

## TCP
ss-local启动ss-remote的TCP连接，接下来通过发送以目标地址开始的加密数据流，然后发送有效负载数据。确切的加密模式因使用的密码而异

```
[target address][payload]
```

ss-remote接收加密数据流，解密并解析前导目标地址；然后它将建立一个新的TCP连接到目标地址并转发有效负载数据。ss-remote接收来自目标的回复，加密并将其转发回ss-local，直到ss-local断开连接

## UDP
ss-local将包含目标地址和有效负载的加密数据包发送到ss-remote。

```
[target address][payload]
```

ss-remote收到加密数据包后，解密并解析目标地址。然后，它将仅包含有效负载的新数据包发送到目标。 ss-remote从目标接收数据包，并将目标地址预先添加到每个数据包中的有效负载，然后将加密的副本发送回ss-local。

```
[target address][payload]
```
本质上，ss-remote正在为ss-local执行网络地址转换。

translate from [Official website](https://shadowsocks.org/en/spec/Protocol.html)

> [shadowsocks协议](https://shadowsocks.org/en/spec/Protocol.html)
