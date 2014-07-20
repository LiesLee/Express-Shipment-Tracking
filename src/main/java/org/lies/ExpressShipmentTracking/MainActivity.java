package org.lies.ExpressShipmentTracking;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidquery.AQuery;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.lies.domain.CompanyInfo;
import org.lies.domain.History;
import org.lies.myApp.MyApplication;
import org.lies.utils.Common;
import org.lies.zBar.CameraTestActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends ActionBarActivity implements View.OnClickListener {

    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        myApplication = MyApplication.getInstance();
        initView();
    }

    /**
     * TODO 初始化界面 *
     */
    private void initView() {
        AQuery aq = new AQuery(this);
        aq.find(R.id.selectCompany).clicked(this);

        aq.find(R.id.select_lyt).clicked(this);
        aq.find(R.id.select_ic).clicked(this);
        aq.find(R.id.select_tv).clicked(this);

        aq.find(R.id.selectHistory_lyt).clicked(this);
        aq.find(R.id.selectHistory_ic).clicked(this);
        aq.find(R.id.selectHistory_tv).clicked(this);

        aq.find(R.id.scan_lyt).clicked(this);
        aq.find(R.id.scan_ic).clicked(this);
        aq.find(R.id.scan_tv).clicked(this);

        changeBtnBackground();

        //软键盘和焦点控制
        EditText selectCompany = (EditText) findViewById(R.id.selectCompany);
        selectCompany.setInputType(InputType.TYPE_NULL);
        selectCompany.clearFocus();
        selectCompany.setCursorVisible(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setMessage("正在为您查询...");
        progressDialog.setCancelable(true);
        progressDialog.setTitle("提示信息");
        //创建快捷方式
        if (!Common.hasShortCut(this)) {
            Common.addShortcut(this);
        }

        if (!Common.onlineFlag(MainActivity.this)) {
            Toast.makeText(this, "网络不可用，非联网状态下无法查询订单信息!", Toast.LENGTH_SHORT).show();
        }

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }

    long waitTime = 2000;
    long touchTime = 0;

    /**
     * 两秒内双击返回键退出
     */
    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if ((currentTime - touchTime) >= waitTime) {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
            touchTime = currentTime;
        } else {
            finish();
        }
    }

    HashMap<String, String> infoMap = new HashMap<String, String>();

    /**
     * 按钮单击事件 *
     */
    @Override
    public void onClick(View v) {
        AQuery aq = new AQuery(this);

        int id = v.getId();
        if (id == R.id.select_lyt || id == R.id.select_ic || id == R.id.select_tv) { // TODO 点击查询
            if (TextUtils.isEmpty(aq.find(R.id.selectCompany).getText())) {
                //动画
                Animation shakeAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_x);
                aq.find(R.id.selectCompany).animate(shakeAnim);
                Toast.makeText(this, "未选择快递公司!", Toast.LENGTH_SHORT).show();
                return;
            } else if (TextUtils.isEmpty(aq.find(R.id.shipmentNumber).getText())) {
                Animation shakeAnim = AnimationUtils.loadAnimation(MainActivity.this, R.anim.shake_x);
                aq.find(R.id.shipmentNumber).animate(shakeAnim);
                Toast.makeText(this, "未输入运单号!", Toast.LENGTH_SHORT).show();
                return;
            }
            int len = aq.find(R.id.shipmentNumber).getText().toString().length();
            //单号非法,不足5位或者超出20位
            if (len < 5 || len > 20) {
                Toast.makeText(this, "运单号非法,不足5位或者超出20位！", Toast.LENGTH_SHORT).show();
                return;
            }

            String shipmentNumber = aq.find(R.id.shipmentNumber).getText().toString().trim();
            Log.i("+++shipmentNumber++++>", shipmentNumber);
            Pattern p = Pattern.compile("[\\da-zA-Z]+?");
            Matcher m = p.matcher(shipmentNumber);
            if (!m.matches()) {
                Toast.makeText(MainActivity.this, "您的订单号不符合标准，重新输入!", Toast.LENGTH_SHORT).show();
                return;
            }


            infoMap.put("shipmentNumber", shipmentNumber);
            // 备注信息暂时存为空
            infoMap.put("mark", "");

            new MyThread(infoMap).start();

        } else if (id == R.id.selectCompany) { // TODO 点击选择快递公司
            Intent intent = new Intent(MainActivity.this, SelectCompanyActivity.class);
            startActivityForResult(intent, 6);
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        } else if (id == R.id.selectHistory_lyt || id == R.id.selectHistory_ic || id == R.id.selectHistory_tv) { // TODO 点击查询历史
            boolean bol = History.checkHasDataOrNot(Common.dbh);
            if (bol) {
                // 可伸缩的listview
                Intent intent = new Intent();
                intent.setClass(MainActivity.this, CheckPackageHisActivity.class);
                overridePendingTransition(R.anim.slide_left, R.anim.slide_right);
                startActivity(intent);
            } else {

                Toast.makeText(MainActivity.this, "暂无历史记录!", Toast.LENGTH_SHORT).show();
            }
        } else if(id == R.id.scan_lyt || id == R.id.scan_ic || id == R.id.scan_tv){ //点击扫描条形码
            startActivityForResult(new Intent(this, CameraTestActivity.class), 3);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        AQuery aq = new AQuery(this);

        if (resultCode == 8 && requestCode == 6) {

            Bundle bundle = data.getExtras();
            if (bundle == null) {

                Toast.makeText(MainActivity.this, "获取数据失败!", Toast.LENGTH_SHORT).show();
                return;
            }
            infoMap = new HashMap<String, String>();
            String name = bundle.getString("companyName");
            String str = bundle.getString("companyId");
            String id = TextUtils.isEmpty(str) ? "" : str;
            infoMap.put("companyName", name);
            infoMap.put("companyId", id);

            List<String> resultList = CompanyInfo.getCompanyName(Common.dbh, id);

            if (resultList.get(0).equals("")) {
                aq.find(R.id.helpinfotext).text("");
                aq.find(R.id.title).text("");

            } else {
                aq.find(R.id.title).text("温馨提示：");
                aq.find(R.id.helpinfotext).text("查询须知：" + resultList.get(0));
            }

            if (resultList.get(1).equals("")) {
                aq.find(R.id.phonenumbertxt).text("");
                aq.find(R.id.title).text("");
            } else {
                aq.find(R.id.title).text("温馨提示：");
                aq.find(R.id.phonenumbertxt).text(TextUtils.isEmpty(name) ? "" : name + "查询电话：" + resultList.get(1));
            }

            aq.find(R.id.selectCompany).text(TextUtils.isEmpty(name) ? "" : name);

        }else if(requestCode == 3 && resultCode == RESULT_OK){
            aq.find(R.id.shipmentNumber).text(data.getStringExtra("Code"));
        }

        super.onActivityResult(requestCode, resultCode, data);

    }

    private static final int WAITTING = 0;
    private static final int FINISHED = 1;
    private static final int ERROR = 2;
    private ProgressDialog progressDialog;
    // TODO Handler处理
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case WAITTING:
                    progressDialog.show();
                    break;
                case ERROR:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    break;
                case FINISHED:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    // 关闭键盘
                    Common.closeKeyboardCommAct(MainActivity.this);
                    break;

                case 10:
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    Toast.makeText(MainActivity.this, "未获取到相关信息，请检查查询条件!", Toast.LENGTH_LONG).show();
                    break;
            }

            super.handleMessage(msg);
        }
    };


    /**
     * 用于存放结果的map
     * key: timeList
     * value: infoList
     */
    private HashMap<List<String>, List<String>> resultMap = new HashMap<List<String>, List<String>>();

    /**
     * TODO 子线程联网操作
     */
    class MyThread extends Thread {
        private HashMap<String, String> threadMap = null;

        public MyThread(HashMap<String, String> map) {
            threadMap = new HashMap<String, String>();
            this.threadMap = map;
        }

        @Override
        public void run() {
            int rand = (int) Math.random() * 1000 + 1000;
            String strurl = "http://wap.kuaidi100.com/q.jsp?rand=" + rand + "&id=" + threadMap.get("companyId") + "&postid=" + threadMap.get("shipmentNumber") + "&fromWeb=null";
            Message msg = new Message();
            msg.what = WAITTING;
            mHandler.sendMessage(msg);
            // http://wap.kuaidi100.com/q.jsp?rand=1000&id=shunfeng&postid=118852822202&fromWeb=null
            // http://m.kuaidi100.com/index_all.html?type=shunfeng&postid=118852822202
            String result = getData(strurl);
            Log.i("==========result===========", result);
            if (result.equals("")) {

                msg = new Message();
                msg.what = 10;
                mHandler.sendMessage(msg);
                return;
            }

            /*
                <p>顺丰速运单号：<strong>118852822202</strong></p>
                <p><strong>查询结果如下所示：</strong></p>
                <p>&middot;2014-06-28 22:46:36<br /> 已收件</p>
                <p>&middot;2014-06-29 01:24:07<br /> 快件在 深圳集散中心 ,准备送往下一站 广州集散中心 </p>
                <p>&middot;2014-06-29 03:18:53<br /> 快件到达广州集散中心</p>
                <p>&middot;2014-06-29 03:59:40<br /> 快件在 广州集散中心 ,准备送往下一站 广州集散中心 </p>
                <p>&middot;2014-06-29 06:35:52<br /> 快件在 广州集散中心 ,准备送往下一站 广州 </p>
                <p>&middot;2014-06-29 11:10:16<br /> 快件在 广州 ,准备送往下一站 广州集散中心 </p>
                <p>&middot;2014-06-29 12:52:54<br /> 快件在 广州集散中心 ,准备送往下一站 广州 </p>
                <p>&middot;2014-06-29 14:16:12<br /> 正在派件..</p>
                <p>&middot;2014-06-29 15:35:48<br /> 派件已签收</p>
                <p>&middot;2014-06-29 15:35:59<br /> 签收人是：已签收</p>
            */

            //开始解析
            Document doc = Jsoup.parse(result);
            Elements elements = doc.body().getElementsByTag("p");
            Object[] objArr = elements.toArray();
            Pattern p = Pattern.compile("<.+?>|\\&gt;|\\&middot;", Pattern.DOTALL);
            String tempStr = null;
            // 存放快递的时间
            ArrayList<String> timeList = new ArrayList<String>();
            // 存放快递的对应时间点的信息
            ArrayList<String> infoList = new ArrayList<String>();

            for (int i = 3; i < objArr.length - 2; i++) {
                Matcher m = p.matcher(objArr[i].toString());
                tempStr = m.replaceAll("");
                if (TextUtils.isEmpty(tempStr)) {
                    continue;
                } else if (tempStr.startsWith("建议操作")) {
                    msg = new Message();
                    msg.what = 10;
                    mHandler.sendMessage(msg);
                    return;
                }
                // 2014-06-29 03:18:53 快件在 深圳集散中心 ,准备送往下一站 广州集散中心
                timeList.add(new String(tempStr.substring(0, 19)));
                infoList.add(new String(tempStr.substring(20)));
            }

            Log.i("----------时间---------最后结果", timeList.toString());
            Log.i("----------物流的详细信息---------最后结果", infoList.toString());
            msg = new Message();
            msg.what = FINISHED;
            mHandler.sendMessage(msg);

            if (timeList.isEmpty() || infoList.isEmpty()) {

                msg = new Message();
                msg.what = 10;
                mHandler.sendMessage(msg);

                return;
            }
            //跳转到显示信息的Activity
            Intent intent = new Intent(MainActivity.this, PackageDetailInfoActivity.class);
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("timeList", timeList);
            bundle.putStringArrayList("infoList", infoList);
            // 公司名称
            bundle.putString("companyName", infoMap.get("companyName"));
            bundle.putString("companyId", infoMap.get("companyId"));
            // 运单号
            bundle.putString("shipmentNumber", infoMap.get("shipmentNumber"));
            intent.putExtras(bundle);
            startActivity(intent);
            //动画
            overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
        }
    }

    /**
     * TODO 获取返回的HTML响应包
     *
     * @param strurl
     * @return HTML
     */
    private String getData(String strurl) {
        StringBuilder output = new StringBuilder("");
        URL url;
        try {
            url = new URL(strurl);
            InputStream is = url.openStream();
            BufferedReader buffer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String tem = "";
            while ((tem = buffer.readLine()) != null) {
                output.append(tem);
            }
            return output.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


    /**
     * 修改按钮背景图片
     */
    public void changeBtnBackground() {

        class MyBtnOnTouch implements View.OnTouchListener {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                LinearLayout ll = (LinearLayout) v.getParent();
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        ll.setBackgroundColor(Color.rgb(62,200,200));
                        break;
                    case MotionEvent.ACTION_UP:
                        ll.setBackgroundColor(android.R.color.transparent);
                        break;
                }

                return false;
            }

        }

        findViewById(R.id.select_lyt).setOnTouchListener(new MyBtnOnTouch());
        findViewById(R.id.select_ic).setOnTouchListener(new MyBtnOnTouch());
        findViewById(R.id.select_tv).setOnTouchListener(new MyBtnOnTouch());

        findViewById(R.id.selectHistory_lyt).setOnTouchListener(new MyBtnOnTouch());
        findViewById(R.id.selectHistory_ic).setOnTouchListener(new MyBtnOnTouch());
        findViewById(R.id.selectHistory_tv).setOnTouchListener(new MyBtnOnTouch());

        findViewById(R.id.scan_lyt).setOnTouchListener(new MyBtnOnTouch());
        findViewById(R.id.scan_ic).setOnTouchListener(new MyBtnOnTouch());
        findViewById(R.id.scan_tv).setOnTouchListener(new MyBtnOnTouch());




        /*searchBtn.setOnTouchListener(new MyBtnOnTouch());
        historyBtn.setOnTouchListener(new MyBtnOnTouch());*/
    }

    /**
     * 点击EditText的地方隐藏键盘
     * @param ev
     * @return
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
}
