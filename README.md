# dexdump

# 已经用纯frida实现了，frida版本的会去遍历所有的类，并且加载，对于抽类的，能够修复那种执行的时候把代码填充回dex的
[https://github.com/smartdone/Frida-Scripts/tree/master/unpack](https://github.com/smartdone/Frida-Scripts/tree/master/unpack)
> 还是frida方便，环境配置简单，喜欢请点star哦

## 感谢
- `ele7enxxh`大佬的[Android-Inline-Hook](https://github.com/ele7enxxh/Android-Inline-Hook)
- `rrrfff`大佬的[ndk_dlopen](https://github.com/rrrfff/ndk_dlopen)

# Usage

1. 把libhook.so复制到/data/local/tmp下面，并且更改权限为777 (`chmod 777 /data/local/tmp/libhook.so`)
2. 安装xposed插件
3. 激活插件并且重启手机
4. 在xposed插件的界面里面勾选你需要脱壳的应用
5. 干掉你需要脱壳的应用的进程，然后重新启动应用 (`kill -9 {pid}`)
6. 如果脱壳成功的话，就会在应用的`/data/data/{packagename}`下面写入dex文件
7. `grep {activity name} -r *` 筛选出dex

# Notice

> 在5.x，6.0目前正常。

- 根据[@jwchen119](https://github.com/jwchen119)大佬的建议，写的6.0

- 7.x应该有问题，刚弄了一个7.x的设备，最近跟新一下

- 4.4及以下的也的Dalvik也没有试，可以正常使用了会在这个地方声明

- 感谢大家
