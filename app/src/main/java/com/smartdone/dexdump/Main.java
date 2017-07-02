package com.smartdone.dexdump;

import android.content.Context;
import android.util.Log;

import java.util.List;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by smartdone on 2017/7/1.
 */

public class Main implements IXposedHookLoadPackage {
    private static final String TAG = "DEX_DUMP";
    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam loadPackageParam) throws Throwable {

        List<String> hooklist = Config.getConfig();

        if(!Config.contains(hooklist, loadPackageParam.packageName))
            return;

        XposedBridge.log("对" + loadPackageParam.packageName + "进行处理");
        Log.e(TAG, "开始处理: " + loadPackageParam.packageName);

        try{
            System.load("/data/local/tmp/libhook.so");
        }catch (Exception e) {
            Log.e(TAG, "加载动态库失败：" + e.getMessage());
        }
        Log.e(TAG, "加载动态库成功");
        XposedHelpers.findAndHookMethod("android.app.Application", loadPackageParam.classLoader, "attach", Context.class, new XC_MethodHook() {
            private Context context;
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Dumpper.dump();
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
            }
        });
    }
}
