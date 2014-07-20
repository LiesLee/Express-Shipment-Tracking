package org.lies.ExpressShipmentTracking;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.lies.domain.History;
import org.lies.utils.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckPackageHisActivity extends ListActivity implements AbsListView.OnScrollListener, AdapterView.OnItemClickListener {
    private LayoutInflater mLayoutInflater;

    private MyAdapter adapter;

    private boolean[] isSelectedItems;

    /**
     * 数据源
     */
    private List<Map<String, Object>> contentDefileList;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_package_his);
        mLayoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        initView();
        this.setListAdapter(adapter);
        this.getListView().setOnScrollListener(this);
        this.getListView().setOnItemClickListener(this);
    }

    /**
     * 初始化数据
     */
    private void initView() {


        contentDefileList = new ArrayList<Map<String, Object>>();
        contentDefileList.addAll(History.getHistoryList(Common.dbh));
        for (int i = 0; i < contentDefileList.size(); i++) {
            contentDefileList.get(i).put("status", false);
        }

        isSelectedItems = new boolean[contentDefileList.size()];
        for (int i = 0; i < isSelectedItems.length; i++) {
            isSelectedItems[i] = false;
        }

        adapter = new MyAdapter(this, contentDefileList);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {
        if (isSelectedItems[position]) {
            isSelectedItems[position] = false;
        } else {
            isSelectedItems[position] = true;
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 自定义适配器
     *
     * @author
     */
    class MyAdapter extends BaseAdapter {
        private Context context;
        private List<Map<String, Object>> contentDefileList;

        public MyAdapter(Context context, List<Map<String, Object>> contentDefileList) {
            this.context = context;
            this.contentDefileList = contentDefileList;
        }

        public int getCount() {
            return contentDefileList.size();
        }

        public Object getItem(int position) {
            return contentDefileList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup p) {
            CustomLinearLayout view = null;
            if (null == convertView) {
                view = new CustomLinearLayout(CheckPackageHisActivity.this, contentDefileList.get(position), position, false);
            } else {
                view = (CustomLinearLayout) convertView;
                view.setDetailInfoLayout(contentDefileList.get(position), position, isSelectedItems[position]);
            }
            return view;
        }
    }

    /**
     * 一个view组合体 自定义layout
     */
    public class CustomLinearLayout extends LinearLayout {

        private LinearLayout layout;
        private RelativeLayout contentTitleLayout;
        private LinearLayout contentDetailLayout;

        private ImageView statusImgView;
        private TextView his_companynamenumber;
        private TextView his_checkpackagetime;
        private TextView detailinfotxt;

        public CustomLinearLayout(Context context, final Map<String, Object> contextDefailMap, final int position, boolean isCurrentItem) {

            super(context);
            layout = (LinearLayout) mLayoutInflater.inflate(R.layout.check_package_hisinfo, null);
            contentTitleLayout = (RelativeLayout) layout.findViewById(R.id.titlelayout);
            contentTitleLayout.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            his_companynamenumber.setTextColor(Color.WHITE);
                            his_checkpackagetime.setTextColor(Color.WHITE);
                            v.setBackgroundColor(Color.parseColor("#2DDBB0"));

                            break;
                        case MotionEvent.ACTION_UP:
                            his_companynamenumber.setTextColor(Color.parseColor("#4F4F4F"));
                            his_checkpackagetime.setTextColor(Color.parseColor("#4F4F4F"));
                            v.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            break;
                    }

                    return false;
                }
            });

            contentDetailLayout = (LinearLayout) layout.findViewById(R.id.DetailLayout);
            his_companynamenumber = (TextView) layout.findViewById(R.id.his_companynamenumber);
            statusImgView = (ImageView) layout.findViewById(R.id.workStatusImg);
            his_checkpackagetime = (TextView) layout.findViewById(R.id.his_checkpackagetime);
            his_companynamenumber.setTextColor(Color.parseColor("#4F4F4F"));
            his_checkpackagetime.setTextColor(Color.parseColor("#4F4F4F"));
            detailinfotxt = (TextView) layout.findViewById(R.id.detailinfotxt);
            this.addView(layout);
            setDetailInfoLayout(contextDefailMap, position, isCurrentItem);
        }

        /**
         * 显示具体内容 （查询历史）
         *
         * @param detailInfoMap
         * @param position
         * @param isCurrentItem
         */
        public void setDetailInfoLayout(final Map<String, Object> detailInfoMap, final int position, boolean isCurrentItem) {

            contentTitleLayout.setBackgroundColor(Color.WHITE);
            his_companynamenumber.setText(Common.object2String(detailInfoMap.get("name")) + "：" + Common.object2String(detailInfoMap.get("code")));
            his_checkpackagetime.setText("上次查询时间：" + Common.object2String(detailInfoMap.get("create_time")));
            his_companynamenumber.setTextColor(Color.parseColor("#4F4F4F"));
            his_checkpackagetime.setTextColor(Color.parseColor("#4F4F4F"));
            statusImgView.setImageResource((detailInfoMap.get("status").equals("true")) ? R.drawable.ic_action_expand : R.drawable.ic_action_next_item);

            if (isCurrentItem) {
                String strArr = Common.object2String(detailInfoMap.get("info")).replace("-LiesLee-", "\n");
                detailinfotxt.setText(strArr);
                statusImgView.setImageResource(R.drawable.ic_action_expand);
            }

            contentDetailLayout.setVisibility(isCurrentItem ? VISIBLE : GONE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.check_package_his, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            new AlertDialog.Builder(this).setTitle("确认删除查询所有历史?").setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    History.deleteAll(Common.dbh);
                    Toast.makeText(CheckPackageHisActivity.this, "历史记录删除成功！",Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                    CheckPackageHisActivity.this.finish();
                }
            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            }).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void onScroll(AbsListView v, int i, int j, int k) {
    }

    public void onScrollStateChanged(AbsListView v, int state) {
        if (state == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
        }
    }
}
