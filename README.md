# dexdump

> 一个用来快速脱一代壳的工具(稍微改下就可以脱类抽取那种壳，这里就先不放出来了)，这里用到了xposed框架，然后还有`ele7enxxh`大佬的[Android-Inline-Hook](https://github.com/ele7enxxh/Android-Inline-Hook)

# Usage

> 需要arm的手机，api level 21以上

1. 把libhook.so复制到/data/local/tmp下面，并且更改权限为777
2. 安装xposed插件
3. 激活插件并且重启手机
4. 在xposed插件的界面里面勾选你需要脱壳的应用
5. 干掉你需要脱壳的应用的进程，然后重新启动应用
6. 如果脱壳成功的话，就会在应用的`/data/data/{packagename}`下面写入dex文件
7. 配合jeb，找到哪个dex才是脱壳脱下了的

# Notice

> 因为我就只有个垃圾魅蓝note1，系统是5.1的，不知道在其他系统上面兼容不，如果步兼容，请联系我(smartdone@163.com)，然后把libart.so给我提供一份，多谢。

> 需要的apk和so可以在看雪上面下载[http://bbs.pediy.com/thread-218936.htm](http://bbs.pediy.com/thread-218936.htm)