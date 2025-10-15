* mysql版本：8.0.40
* jdk版本：17
* node版本：v22.14.0
* vue版本：@vue/cli 5.0.8

一个后端服务（novel）、前端服务（free-novel-web）、两个mysql服务（主库数据：djangoblog.sql、扩容库数据：chapter_scaling_up_one.sql）

后端服务配置如下：
```
spring.datasource.primary.jdbc-url表示主库
spring.datasource.primary.jdbc-url表示扩容库
proxy.host表示代理ip
proxy.port表示代理端口
（当服务器ip被拉黑之后，可以开启代理）
```

linux服务器使用网络代理的便宜方法如下：
```
访问梯子的订阅链接，链接返回的内容如下所示：
mixed-port: 7890
allow-lan: true
bind-address: '*'
mode: rule
log-level: info
external-controller: '127.0.0.1:9090'
dns:
    enable: true
    ipv6: false
    default-nameserver: [223.5.5.5, 119.29.29.29]
    enhanced-mode: fake-ip
    fake-ip-range: 198.18.0.1/16
    use-hosts: true
    nameserver: ['https://doh.pub/dns-query', 'https://dns.alidns.com/dns-query']
    fallback: ['https://doh.dns.sb/dns-query', 'https://dns.cloudflare.com/dns-query', 'https://dns.twnic.tw/dns-query', 'tls://8.8.4.4:853']
    fallback-filter: { geoip: true, ipcidr: [240.0.0.0/4, 0.0.0.0/32] }
proxies:
    - { name: '🇭🇰 HK04', type: ss, server: hk04.nicenode.top, port: 20014, cipher: chacha20-ietf-poly1305, password: ae02f134-efc4, udp: true }
    - { name: '🇸🇬 VIP SG05', type: ss, server: vipsg05.nicenode.top, port: 20255, cipher: chacha20-ietf-poly1305, password: ae02f134-efc4, udp: true }
proxy-groups:
    - { name: 耐思云, type: select, proxies: ['🇭🇰 HK08', '🇭🇰 HK09', '🇭🇰 HK10'] }
    - { name: 高级节点, type: select, proxies: ['🇺🇸 VIP US01'] }
rules:
    - 'DOMAIN,nicecloud.io,DIRECT'
    - 'MATCH,耐思云'

将proxies中的数据整理成下面的形式：
{
    "server": "hk17.nicenode.top",
    "server_port": 20087,
    "password": "ae02f134-efc4",
    "method": "chacha20-ietf-poly1305",
    "local_port": 1080,
    "timeout": 300
}

然后通过ss-local启动代理即可

```


user表 upload字段（0：禁止上传、1：允许上传）

platform表 platform_type字段（novel：小说平台、ai翻译平台）

platform_api_key表 platform_id字段（表示platform表中ai翻译平台对应的apikey）

chapter_sync表 host_server_name字段（当扩容库过多时，用于区分）

dictionary表中的数据解释如下：
```
key_field                                                   value_field
siliconflow、xianyu、hlypay表示ai平台的名称                   表示ai平台的接口
enableTrModel                                              启用哪个ai平台
shutuTr                                                    是否允许翻译书兔内容
AIproxyClient                                              是否通过网络代理访问ai翻译平台
controllAllTr                                              控制所有翻译进程，true开启、false关闭
ScalingUp                                                  当扩容库过多时，用于区分
RunScalingUp                                               是否将数据同步到扩容库
repeatTrNovelNum                                           积分>500的用户每天可以重新翻译的次数
createFeedbackNum                                          用户每天可以执行翻译反馈的次数


其它配置要么不重要、要么已经在表中解释过。
```



### 3、前端打包命令
```vue
npm install
npm run build --no-source-map
```