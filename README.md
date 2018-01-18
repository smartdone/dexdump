# dexdump

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

> 在5.x，6.0目前正常。7.0没设备未测试

- 根据[@jwchen119](https://github.com/jwchen119)大佬的建议，写的6.0

- 加了7.x的，求有7.x机器的大佬试一下，一把梭写的，不知道能不能用