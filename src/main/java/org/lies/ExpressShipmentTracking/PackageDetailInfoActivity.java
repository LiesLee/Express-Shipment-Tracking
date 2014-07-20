package org.lies.ExpressShipmentTracking;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import org.lies.domain.History;
import org.lies.utils.Common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class PackageDetailInfoActivity extends Activity implements View.OnTouchListener{
    private HashMap<String, String> detailMap = new HashMap<String, String>();
    /**
     * 存放该运单所有相关信息
     */
    private HashMap<String, String> infoMap = new HashMap<String, String>();
    private ArrayList<String> timeList;
    private ArrayList<String> infoList;

    AQuery aq = new AQuery(this);
    private MyAdaper myAdaper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_package_detail_info);
        setActionBarLayout(R.layout.actionbar_package_detial_info);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        findViewById(R.id.save).setOnTouchListener(this);

        Intent intent = getIntent();

        Bundle bundle = intent.getExtras();
        timeList = bundle.getStringArrayList("timeList");
        infoList = bundle.getStringArrayList("infoList");
        infoMap.put("companyName", bundle.getString("companyName"));
        infoMap.put("companyId", bundle.getString("companyId"));
        infoMap.put("shipmentNumber", bundle.getString("shipmentNumber"));

        aq.find(R.id.shipmentInfo).text(infoMap.get("companyName") + "：" + infoMap.get("shipmentNumber"));

        myAdaper = new MyAdaper(infoList, timeList, this);

        aq.find(R.id.resultlistview).adapter(myAdaper);
    }

    private void setActionBarLayout(int layoutId) {
        ActionBar actionBar = getActionBar();

        if (null != actionBar) {
            /*
             *  使左上角图标是否显示
			 *  如果设成false则没有程序图标,仅仅就个标题
			 *  否则显示应用程序图标
			 *  对应id为android.R.id.home
			 *  对应ActionBar.DISPLAY_SHOW_HOME
			 */
            actionBar.setDisplayShowHomeEnabled(false);
			/*
			 * 使自定义的普通View能在title栏显示
			 * 即actionBar.setCustomView能起作用
			 * 对应ActionBar.DISPLAY_SHOW_CUSTOM
			 */
            actionBar.setDisplayShowCustomEnabled(true);

            LayoutInflater inflator = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflator.inflate(layoutId, null);
            ActionBar.LayoutParams layout = new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            actionBar.setCustomView(v, layout);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:
                v.setBackgroundColor(Color.rgb(62,200,200));
                break;
            case MotionEvent.ACTION_UP:
                v.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                break;
        }

        return false;
    }


    class Holder {

        TextView timeText;
        TextView infoText;
        ImageView imageView;

    }

    /**
     * 自定义适配器
     *
     */
    public class MyAdaper extends BaseAdapter {

        LayoutInflater layoutInflater = null;
        List<String> timeList = null;
        List<String> infoList = null;
        Holder holder = null;
        Context mContext;
        String[] strArr = null;

        public MyAdaper(List<String> infoList, List<String> timeList, Context context) {

            mContext = context;
            layoutInflater = (LayoutInflater) context .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            this.timeList = new ArrayList<String>() ;
            this.infoList = new ArrayList<String>() ;
            this.timeList = timeList;
            this.infoList = infoList;
            this.strArr = new String[2];

        }


        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return infoList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            strArr[0] = timeList.get(position);
            strArr[1] = infoList.get(position);
            return strArr;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {

                convertView = layoutInflater.inflate(R.layout.resultlistview_item, null);

                holder = new Holder();

                holder.timeText = (TextView) convertView.findViewById(R.id.timetxt);
                holder.infoText = (TextView) convertView.findViewById(R.id.infotxt);
                // holder.imageView = (ImageView) convertView
                // .findViewById(R.id.imageview);
                convertView.setTag(holder);

            } else {

                holder = (Holder) convertView.getTag();
            }

            // holder.imageView.setVisibility(View.VISIBLE);

            holder.timeText.setTextColor(Color.parseColor("#1C86EE"));
            holder.infoText.setTextColor(Color.parseColor("#515151"));

            String[] str = (String[]) getItem(position);
            // 如果是最后一个并且包含已经签收三个字就显示红色
            if ((position == timeList.size() - 1) && str[1].contains("已签收")) {
                // holder.imageView.setVisibility(View.GONE);
                holder.timeText.setTextColor(Color.parseColor("#CD0000"));
                holder.infoText.setTextColor(Color.parseColor("#CD0000"));
            }
            holder.timeText.setText(str[0]);
            holder.infoText.setText(str[1]);
            return convertView;
        }

    }

    /**
     * 按钮单击
     * @param v
     */
    public void onClick(View v) {
        int id = v.getId();
        //保存
        if (id == R.id.save) {
            // 保存历史记录
            // 当前时间 、快递信息 、
            History saveHistory = new History();
            saveHistory.his_cd = saveHistory.getMaxIndexNo(Common.dbh);
            saveHistory.id = infoMap.get("companyId");
            saveHistory.name = infoMap.get("companyName");
            saveHistory.code = infoMap.get("shipmentNumber");// 订单号
            saveHistory.create_time = Common.getCurTime();

            int size = timeList.size();

            if (size != infoList.size()) {
                Toast.makeText(PackageDetailInfoActivity.this, "信息保存失败,该信息暂时不能保存!", Toast.LENGTH_SHORT).show();
                return;
            }

            StringBuffer buffer = new StringBuffer();
            // 处理信息保存查询历史
            for (int i = 0; i < size; i++) {

                if(i!=size-1){

                    buffer.append(timeList.get(i)).append("-LiesLee-").append(infoList.get(i)).append("-LiesLee-");
                }else{
                    buffer.append(timeList.get(i)).append("-LiesLee-").append(infoList.get(i));
                }

            }
            saveHistory.info = buffer.toString();
            Log.e("--------保存查询的历史信息---------", buffer.toString());
            Common.dbh.beginTransaction();
            try {
                boolean bol = saveHistory.addData(Common.dbh);
                if (!bol) {

                    Toast.makeText(PackageDetailInfoActivity.this, "保存信息失败!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Common.dbh.setTransactionSuccessful();
            } catch (Exception ex) {

                ex.printStackTrace();
                Log.e("------保存历史信息-----", "保存失败");
            }finally{
                // 不管回滚不会滚，都要执行这句话
                Common.dbh.endTransaction();
            }



            timeList.clear() ;
            infoList.clear() ;
            myAdaper.notifyDataSetChanged() ;
            Toast.makeText(PackageDetailInfoActivity.this, "保存信息成功!", Toast.LENGTH_SHORT).show();
            this.finish() ;
        }
    }



}
