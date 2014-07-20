package org.lies.myApp;

import android.app.Application;
import android.content.Context;

import org.lies.domain.CompanyInfo;
import org.lies.utils.Common;
import org.lies.utils.DatabaseHelper;

import java.util.List;

/**
 * Created by LiesLee on 2014/7/7.
 */
public class MyApplication extends Application {
    private Context mContext;
    private List<CompanyInfo> companyInfoList = null;
    private static MyApplication myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        this.mContext = mContext;
        myApplication = this;
        new Thread(task).start();
    }

    private Runnable task = new Runnable() {

        @Override
        public void run() {
            try {
                Common.loadDatabase(MyApplication.this);
            } catch (Exception e) {
                e.printStackTrace();
            }

            String hasLoadDB = Common.readConfig(MyApplication.this, Common.HASLOAD_DATABASE, "0");
            Common.dbh = new DatabaseHelper(MyApplication.this, "mypackage");

            if (hasLoadDB.equals("0")) {
                Common.readData2Db(MyApplication.this);
                Common.writeConfig(MyApplication.this, Common.HASLOAD_DATABASE, "1");
            }
        }
    };

    public synchronized static MyApplication getInstance(){
        return myApplication ;
    }

    public List<CompanyInfo> getCompanyInfoList() {

        return companyInfoList;
    }

    public  void setCompanyInfoList() {
        this.companyInfoList = CompanyInfo.getCompanyInfoList(Common.dbh);
    }
}
