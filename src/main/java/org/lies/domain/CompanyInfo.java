package org.lies.domain;

import android.database.Cursor;

import org.lies.utils.Common;
import org.lies.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LiesLee on 2014/7/7.
 */
public class CompanyInfo {

    /**
     *
     * 编号
     */
    public String info_cd = "";
    /**
     *
     * 快递公司名称
     */
    public String name = "";
    /**
     *
     * id与code对应
     */
    public String id = "";
    public String count = "";

    /** 帮助信息 **/
    public String helpinfo = "";
    /** 电话号码 **/
    public String phonenumber = "";

    /**
     * 获取快递公司各种信息
     *
     * @param dbh
     * @return
     */
    public static List<CompanyInfo> getCompanyInfoList(DatabaseHelper dbh) {

        List<CompanyInfo> resultList = new ArrayList<CompanyInfo>();
        String sql = " select * from t_companyinfo order by info_cd ,id ";
        Cursor cursor = dbh.rawQuery(sql);

        CompanyInfo companyInfo = null;
        while (cursor.moveToNext()) {
            companyInfo = new CompanyInfo();
            Common.setClassValueBycursor(companyInfo, cursor);
            resultList.add(companyInfo);

        }

        if (cursor != null && !cursor.isClosed()) {

            cursor.close();
        }
        return resultList;

    }

    /**
     * 、根据快递公司的id查询帮助信息
     *
     * @param dbh
     * @param id
     * @return
     */
    public static List<String> getCompanyName(DatabaseHelper dbh, String id) {

        if (id == null || id.equals("")) {
            return null;
        }
        String sql = "select helpinfo,phonenumber from t_companyinfo where id = '"
                + id + "'";

        Cursor cursor = dbh.rawQuery(sql);

        List<String> list = new ArrayList<String>();

        while (cursor.moveToNext()) {

            String helpinfo = cursor.getString(cursor
                    .getColumnIndex("helpinfo"));
            String phonenumber = cursor.getString(cursor
                    .getColumnIndex("phonenumber"));
            list.add(helpinfo);
            list.add(phonenumber);
        }
        return list;
    }

    /**
     * 删除数据
     *
     * @param dbh
     */
    public static void deleteAll(DatabaseHelper dbh) {
        String sql = "delete from t_companyinfo";
        dbh.execSQL(sql);
    }

    /**
     * 追加数据
     *
     * @param dbh
     * @return
     */
    public boolean addData(DatabaseHelper dbh) {

        StringBuffer sql = new StringBuffer();
        sql.append(" insert into t_companyinfo ");
        sql.append(" (info_cd,name,id,count,helpinfo,phonenumber)");
        sql.append(" values ( ");
        sql.append(" '" + info_cd + "',");
        sql.append(" '" + name + "',");
        sql.append(" '" + id + "',");
        sql.append(" '" + count + "',");
        sql.append(" '" + helpinfo + "',");
        sql.append(" '" + phonenumber + "')");
        return dbh.execSQL(sql.toString());
    }

    /**
     * 修改数据
     *
     * @param dbh
     * @return
     */
    public boolean updateData(DatabaseHelper dbh) {

        StringBuffer sql = new StringBuffer();
        sql.append(" update t_companyinfo set ");
        sql.append(" info_cd = '" + info_cd + "',");
        sql.append(" name = '" + name + "',");
        sql.append(" id = '" + id + "',");
        sql.append(" count = '" + count + "',");
        sql.append(" helpInfo = '" + helpinfo + "',");
        sql.append(" phoneNumber = '" + phonenumber + "'");

        return dbh.execSQL(sql.toString());
    }

    /**
     * 查询数据是否存在
     *
     * @param dbh
     * @param id
     * @return
     */
    public boolean checkExits(DatabaseHelper dbh, String id) {

        Boolean bolRtn = false;
        StringBuffer sql = new StringBuffer();
        sql.append(" select * from  t_companyinfo ");
        sql.append(" where id = '" + id + "'");
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

        String sql = "select ifnull(max(info_cd),0)+1 as info_cd from t_companyinfo ";
        Cursor cursor = dbh.rawQuery(sql);

        String strvalue = null;
        if (cursor.moveToNext()) {
            strvalue = cursor.getString(cursor.getColumnIndex("info_cd"));
            if (strvalue == null)
                strvalue = "1";
        } else {
            strvalue = "1";
        }
        cursor.close();
        return strvalue;
    }

}
