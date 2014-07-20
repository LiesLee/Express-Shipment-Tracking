package org.lies.domain;

import android.database.Cursor;

import org.lies.utils.Common;
import org.lies.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by LiesLee on 2014/7/7.
 */
public class History {
    public String his_cd = "0";
    // 订单号
    public String code = "";
    // 公司id
    public String id = "0";
    public String name = "";
    // 查询内容
    public String info = "";
    // 查询历史信息的创建时间
    public String create_time = "";

    /**
     * 获取历史信息
     *
     * @param dbh
     * @return
     */
    public static List<String> getHistoryInfoList(DatabaseHelper dbh,
                                                  String code, String id) {

        if (code == null && code.equals("") || id == null && id.equals("")) {

            return null;
        }

        List<String> resultList = new ArrayList<String>();
        String sql = " select info from t_savehistory  where code = '" + code
                + "' and  id= '" + id + "' " + " order by  create_time";
        Cursor cursor = dbh.rawQuery(sql);

        while (cursor.moveToNext()) {

            String str = cursor.getString(cursor.getColumnIndex("info"));
            resultList.add(str);
        }

        if (cursor != null && !cursor.isClosed()) {

            cursor.close();
        }
        return resultList;

    }
    // 获取数据
    public static List<Map<String, Object>> getHistoryList(DatabaseHelper dbh) {

        List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();

        StringBuffer sql = new StringBuffer();
        sql.append("select * from t_savehistory");
        Cursor cursor = dbh.rawQuery(sql.toString());
        while (cursor.moveToNext()) {
            Map<String, Object> map = Common.getListObjectBycursor(cursor);
            resultList.add(map);
        }
        cursor.close();

        return resultList;
    }

    /**
     * 查询历史信息 返回保存History的list
     *
     * @param dbh
     * @return
     */
    public static List<History> getHistoryInfoList(DatabaseHelper dbh) {

        List<History> resultList = new ArrayList<History>();
        String sql = " select * from t_savehistory  order by  create_time";
        Cursor cursor = dbh.rawQuery(sql);
        History savehistory;
        while (cursor.moveToNext()) {
            savehistory = new History();
            Common.setClassValueBycursor(savehistory, cursor);
            resultList.add(savehistory);
        }

        if (cursor != null && !cursor.isClosed()) {

            cursor.close();
        }
        return resultList;

    }

    // 删除所有数据
    public static void deleteAll(DatabaseHelper dbh) {
        String sql = "delete from t_savehistory";
        dbh.execSQL(sql);
    }

    // 追加数据
    public boolean addData(DatabaseHelper dbh) {

        StringBuffer sql = new StringBuffer();
        sql.append(" insert into t_savehistory ");
        sql.append(" (his_cd,code,id,name,info,create_time)");
        sql.append(" values ( ");
        sql.append(" '" + his_cd + "',");
        sql.append(" '" + code + "',");
        sql.append(" '" + id + "',");
        sql.append(" '" + name + "',");
        sql.append(" '" + info + "',");
        sql.append(" '" + create_time + "')");
        return dbh.execSQL(sql.toString());
    }

    /**
     * 查看该表是否存在数据
     * @param dbh
     * @return
     */
    public static boolean checkHasDataOrNot(DatabaseHelper dbh) {

        String sql = "select * from t_savehistory ";

        Cursor cursor = dbh.rawQuery(sql);

        if (cursor.getCount() <= 0) {

            return false;
        }
        return true;
    }

    // 修改数据
    public boolean updateData(DatabaseHelper dbh) {

        StringBuffer sql = new StringBuffer();
        sql.append(" update t_savehistory set ");
        sql.append(" his_cd = '" + his_cd + "',");
        sql.append(" code = '" + code + "',");
        sql.append(" id = '" + id + "',");
        sql.append(" name = '" + name + "',");
        sql.append(" info = '" + info + "'");

        return dbh.execSQL(sql.toString());
    }

    // 查询数据是否存在
    public boolean checkExits(DatabaseHelper dbh, String code) {

        Boolean bolRtn = false;
        StringBuffer sql = new StringBuffer();
        sql.append(" select * from  t_savehistory ");
        sql.append(" where code = '" + code + "'");
        Cursor cursor = dbh.rawQuery(sql.toString());

        if (cursor.moveToFirst())
            bolRtn = true;

        cursor.close();
        return bolRtn;
    }

    /**
     * 获取最大列表数
     *
     * @param dbh
     * @return
     */
    public String getMaxIndexNo(DatabaseHelper dbh) {

        String sql = "select ifnull(max(his_cd),0)+1 as his_cd from t_savehistory ";
        Cursor cursor = dbh.rawQuery(sql);

        String strvalue = null;
        if (cursor.moveToNext()) {
            strvalue = cursor.getString(cursor.getColumnIndex("his_cd"));
            if (strvalue == null) strvalue = "1";
        } else {
            strvalue = "1";
        }
        cursor.close();
        return strvalue;
    }
}
