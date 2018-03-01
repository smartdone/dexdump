package com.smartdone.dexdump;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;


import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("hook");
    }

    private ListView listView;
    private AppAdapter adapter;
    private List<Appinfo> appinfos;
    private List<String> selected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Dumpper.dump();
        selected = Config.getConfig();
        appinfos = new ArrayList<>();
        listView = (ListView) findViewById(R.id.applist);
        adapter = new AppAdapter(appinfos);
        listView.setAdapter(adapter);
        new Thread(){
            @Override
            public void run() {
                super.run();
                getInstallAppList();
            }
        }.start();

    }


    private void getInstallAppList() {
        try {
            if(getApiLevel() <= 23) {
                List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
                for (PackageInfo packageInfo : packageInfos) {
                    if ((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                        Appinfo info = new Appinfo();
                        info.setIcon(zoomDrawable(packageInfo.applicationInfo.loadIcon(getPackageManager()), DensityUtil.dip2px(this, 128), DensityUtil.dip2px(this, 128)));
                        info.setAppName(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
                        info.setAppPackage(packageInfo.packageName);
                        if (Config.contains(selected, info.getAppPackage())) {
                            info.setChecked(true);
                        } else {
                            info.setChecked(false);
                        }
                        appinfos.add(info);
                    }
                }
            } else {
                Intent startupIntent = new Intent(Intent.ACTION_MAIN);
                startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                final PackageManager pm =  getPackageManager();
                List<ResolveInfo> activities = pm.queryIntentActivities(startupIntent, 0);
                for(ResolveInfo resolveInfo : activities) {
                    Appinfo appinfo = new Appinfo();
                    appinfo.setIcon(zoomDrawable(resolveInfo.loadIcon(pm), DensityUtil.dip2px(this, 128), DensityUtil.dip2px(this, 128)));
                    appinfo.setAppName(resolveInfo.loadLabel(pm).toString());
                    appinfo.setAppPackage(resolveInfo.resolvePackageName);

                    if (Config.contains(selected, appinfo.getAppPackage())) {
                        appinfo.setChecked(true);
                    } else {
                        appinfo.setChecked(false);
                    }
                    appinfos.add(appinfo);
                }
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getApiLevel() {
        return Integer.parseInt(android.os.Build.VERSION.SDK);
    }

    class AppAdapter extends BaseAdapter {

        private List<Appinfo> appinfos;

        private AppAdapter(List<Appinfo> appinfos) {
            this.appinfos = appinfos;
        }

        @Override
        public int getCount() {
            return appinfos.size();
        }

        @Override
        public Object getItem(int i) {
            return appinfos.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            View v = LayoutInflater.from(MainActivity.this).inflate(R.layout.item, null);
            final int posi = i;
            final TextView appname = v.findViewById(R.id.tv_appname);
            appname.setText(appinfos.get(i).getAppName());
            TextView appPackage = v.findViewById(R.id.tv_apppackage);
            appPackage.setText(appinfos.get(i).getAppPackage());
            CheckBox checkBox = v.findViewById(R.id.cb_select);
            ImageView icon = v.findViewById(R.id.iv_icon);
            icon.setImageDrawable(appinfos.get(i).getIcon());
            if (appinfos.get(i).isChecked()) {
                checkBox.setChecked(true);
            } else {
                checkBox.setChecked(false);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        Config.addOne(appinfos.get(posi).getAppPackage());
                    } else {
                        Config.removeOne(appinfos.get(posi).getAppPackage());
                    }
                }
            });
            return v;
        }
    }

    private Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable);
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
                matrix, true);
        return new BitmapDrawable(null, newbmp);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }
}
