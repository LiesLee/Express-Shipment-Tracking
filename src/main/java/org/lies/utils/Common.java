package org.lies.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import org.json.JSONArray;
import org.json.JSONObject;
import org.lies.ExpressShipmentTracking.MainActivity;
import org.lies.ExpressShipmentTracking.R;
import org.lies.domain.CompanyInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiesLee on 2014/7/7.
 */
public class Common {
    /**
     * 用于写入配置文件 判断是否已经加载了数据库
     */
    public static final String HASLOAD_DATABASE = "hasload_database";
    public static DatabaseHelper dbh;

    /**
     * 判断桌面是否存在快捷方式
     *
     * @param context
     * @return isInstallShortcut
     */
    public static boolean hasShortCut(Context context) {
        boolean isInstallShortcut = false;
        final ContentResolver cr = context.getContentResolver();
        String AUTHORITY = null;
        if (Build.VERSION.SDK_INT < 8) {
            AUTHORITY = "com.android.launcher.settings";
        } else {
            AUTHORITY = "com.android.launcher2.settings";
        }
        final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/favorites?notify=true");

        Cursor c = cr.query(CONTENT_URI, new String[]{"title", "iconResource"}, "title=?", new String[]{context.getString(R.string.app_name).trim()}, null);
        if (c != null && c.getCount() > 0) {
            isInstallShortcut = true;
            c.close();
        }
        return isInstallShortcut;
    }

    /**
     * 为本应用创建快捷方式
     *
     * @param context
     */
    public static void addShortcut(Context context) {
        Intent shortcut = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");
        //创建快捷方式
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME, R.string.app_name);
        //不允许重复创建
        shortcut.putExtra("duplicate", false);

        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(context, context.getClass().getName());
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        // 快捷方式的图标
        Intent.ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(context, R.drawable.ic_launcher);
        shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);
        context.sendBroadcast(shortcut);
    }

    /**
     * 判断是否联网
     *
     * @param context
     * @return 联网状态
     */
    public static boolean onlineFlag(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获得网络连接信息
        NetworkInfo networkInfo = connectivityManager == null ? null : connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isAvailable()) {
            return true;
        }
        return false;
    }

    /**
     * 进入界面时关闭软键盘
     *
     * @param context
     */
    public static void closeWhenOncreate(Context context) {
        // SOFT_INPUT_STATE_ALWAYS_VISIBLE 显示
        //SOFT_INPUT_STATE_ALWAYS_HIDDEN 隐藏
        ((Activity) context).getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    public static void setClassValueBycursor(Object obj, Cursor cursor) {

        int ColCount = cursor.getColumnCount();
        int i = 0;
        for (i = 0; i < ColCount; i++) {
            String ColName = cursor.getColumnName(i);

            try {
                Field f = obj.getClass().getField(ColName);
                String ret = cursor.getString(i);
                if (f == null)
                    continue;
                if (ret == null)
                    ret = "";
                f.set(obj, ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    public static Map<String, Object> getListObjectBycursor(Cursor cursor) {
        int ColCount = cursor.getColumnCount();
        int i = 0;
        Map<String, Object> map = new HashMap<String, Object>();
        for (i = 0; i < ColCount; i++) {
            String ColName = cursor.getColumnName(i);
            try {
                String ret = cursor.getString(i);
                if (ret == null)
                    ret = "";
                map.put(ColName, ret);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return map;
    }

    /**
     * 加载数据流程：第一次安装应用的时候就创建数据库并解析数据插入库
     *
     * @param context
     * @throws Exception
     */
    public static void loadDatabase(Context context) throws Exception {
        //第一次运行应用程序时，加载数据库到data/data/当前应用包/database/<db_name>
        File dir = new File("data/data/" + context.getPackageName() + "/databases");
        //如果目录不存在，或者不是目录
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdir();
        }

        File file = new File(dir, "mypackage");
        if (!file.exists()) {
            //获得封装.db文件的输入流对象
            InputStream is = context.getResources().openRawResource(R.raw.mypackage);
            FileOutputStream fos = new FileOutputStream("data/data/" + context.getPackageName() + "/databases/mypackage");
            byte[] buff = new byte[7168];
            int count = 0;
            while ((count = is.read(buff)) > 0) {
                fos.write(buff, 0, count);
            }
            fos.close();
            is.close();
        }

    }

    public static String readConfig(Context context, String key, String defval) {
        String str = "";
        SharedPreferences share = context.getSharedPreferences("perference", Context.MODE_PRIVATE);
        str = share.getString(key, defval);
        return str;
    }

    /**
     * 解析json数据插入数据库
     *
     * @param context
     */
    public static void readData2Db(Context context) {
        InputStream in = context.getResources().openRawResource(R.raw.companyinfos);
        try {
            // 将in读入reader 中
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "GBK"));
            StringBuilder buffer = new StringBuilder("");
            String tem = "";
            while ((tem = br.readLine()) != null) {
                buffer.append(tem);
            }
            br.close();
            JSONObject myjson = new JSONObject(buffer.toString());
            JSONArray jsonArray = myjson.getJSONArray("companyinfos");
            CompanyInfo companyInfo = new CompanyInfo();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String compName = jsonObject.getString("name");
                String compId = jsonObject.getString("id");
                String helpinfo = jsonObject.getString("helpinfo");
                String phoneNumber = jsonObject.getString("phonenumber");

                companyInfo.info_cd = companyInfo.getMaxIndexNo(dbh);
                companyInfo.name = compName;
                companyInfo.id = compId;
                companyInfo.count = "0";
                companyInfo.helpinfo = helpinfo;
                companyInfo.phonenumber = phoneNumber;
                //添加数据
                companyInfo.addData(Common.dbh);
            }
            // 加载数据库完成，写入成功标识位
            Common.writeConfig(context, Common.HASLOAD_DATABASE, "1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 加载数据库完成，写入成功标识位
     *
     * @param context
     * @param key
     * @param val
     */
    public static void writeConfig(Context context, String key, String val) {
        SharedPreferences share = context.getSharedPreferences("perference", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = share.edit();
        editor.putString(key, val);
        editor.commit();
    }

    /**
     * 普通关闭键盘
     *
     * @param context
     */
    public static void closeKeyboardCommAct(Context context) {
        InputMethodManager imm = (InputMethodManager) ((Activity) context).getSystemService(Context.INPUT_METHOD_SERVICE);
        if (((Activity) context).getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /*
     * 获取当前系统时间,YYYY-MM-DD hh:mm:ss
     */
    public static String getCurTime() {
        String Time = null;

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Time = sDateFormat.format(new java.util.Date());
        return Time;
    }

    public static String object2String(Object obj) {
        String str = "";
        if (obj != null) {

            str = String.valueOf(obj);
        }

        return str;
    }
}
