package com.smartdone.dexdump;

import android.content.pm.PackageInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("hook");
    }
    private List<Appinfo> appinfos;
    private ListView listView;
    private AppAdapter adapter;
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
        try{
            List<PackageInfo> packageInfos = getPackageManager().getInstalledPackages(0);
            for(PackageInfo packageInfo : packageInfos) {
                Appinfo info = new Appinfo();
                info.setAppName(packageInfo.applicationInfo.loadLabel(getPackageManager()).toString());
                info.setAppPackage(packageInfo.packageName);
                if(Config.contains(selected, info.getAppPackage())) {
                    info.setChecked(true);
                }else {
                    info.setChecked(false);
                }
                appinfos.add(info);
                adapter.notifyDataSetChanged();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    class AppAdapter extends BaseAdapter{

        private List<Appinfo> appinfos;

        public AppAdapter(List<Appinfo> appinfos){
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
            final TextView appname = (TextView) v.findViewById(R.id.tv_appname);
            appname.setText(appinfos.get(i).getAppName());
            TextView appPackage = (TextView) v.findViewById(R.id.tv_apppackage);
            appPackage.setText(appinfos.get(i).getAppPackage());
            CheckBox checkBox = (CheckBox) v.findViewById(R.id.cb_select);
            if(appinfos.get(i).isChecked()) {
                checkBox.setChecked(true);
            }else {
                checkBox.setChecked(false);
            }
            checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(b) {
                        Config.addOne(appinfos.get(posi).getAppPackage());
                    }else {
                        Config.removeOne(appinfos.get(posi).getAppPackage());
                    }
                }
            });
            return v;
        }
    }
}
