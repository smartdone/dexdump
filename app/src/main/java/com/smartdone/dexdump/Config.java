package com.smartdone.dexdump;

import android.os.Environment;
import android.util.Log;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smartdone on 2017/7/2.
 */

public class Config {
    private static final String TAG = "DEX_DUMP";
    private static final String FILENAME = Environment.getExternalStorageDirectory().getPath() + File.separator + "dumdex.js";

    private static void writeConfig(String s) {
        try {
            FileOutputStream fout = new FileOutputStream(FILENAME);
            fout.write(s.getBytes("utf-8"));
            fout.flush();
            fout.close();
            Log.e("DEX_DUMP", "write file");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addOne(String name) {
        List<String> ori = getConfig();
        if(ori == null) {
            JSONArray jsonArray = new JSONArray();
            jsonArray.put(name);
            writeConfig(jsonArray.toString());
        }else {
            ori.add(name);
            JSONArray jsonArray = new JSONArray();
            for(String o : ori) {
                jsonArray.put(o);
            }
            writeConfig(jsonArray.toString());
        }
    }

    public static void removeOne(String name) {
        List<String> ori = getConfig();
        if(ori != null) {
            for(int i = 0; i < ori.size(); i++) {
                if(ori.get(i).equals(name)) {
                    ori.remove(i);
                }
            }
            JSONArray jsonArray = new JSONArray();
            for(String s : ori) {
                jsonArray.put(s);
            }
            writeConfig(jsonArray.toString());
        }
    }

    public static List<String> getConfig() {
        File file = new File(FILENAME);
        if (file.exists() && file.canRead()) {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
                String line = br.readLine();
                JSONArray jsonArray = new JSONArray(line);
                List<String> apps = new ArrayList<>();
                for(int i = 0; i < jsonArray.length(); i++) {
                    apps.add(jsonArray.getString(i));
                }
                br.close();
//                Log.e("DEX_DUMP", "需要hook的列表: " + line);
                return apps;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else  {
            try {
                if (!file.exists())
                    file.createNewFile();
                if(!file.canRead())
                    Log.e(TAG, "文件没有读取权限");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static boolean contains(List<String> lists, String name) {
        if(lists == null) {
            return false;
        }
        for(String l : lists) {
            if(l.equals(name)) {
                return true;
            }
        }
        return false;
    }

}
