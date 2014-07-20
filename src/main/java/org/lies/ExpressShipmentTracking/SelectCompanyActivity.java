package org.lies.ExpressShipmentTracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;

import org.lies.domain.CompanyInfo;
import org.lies.myApp.MyApplication;
import org.lies.sortlistview.CharacterParser;
import org.lies.sortlistview.ClearEditText;
import org.lies.sortlistview.PinyinComparator;
import org.lies.sortlistview.SideBar;
import org.lies.sortlistview.SortAdapter;
import org.lies.sortlistview.SortModel;
import org.lies.utils.Common;
import org.lies.utils.DatabaseHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SelectCompanyActivity extends Activity {
    private ListView sortListView;
    private SideBar sideBar;
    private TextView dialog;

    private SortAdapter adapter;
    private ClearEditText mClearEditText;

    /**
     * 汉字转换成拼音的类
     */
    private CharacterParser characterParser;
    private List<SortModel> SourceDateList;

    /**
     * 根据拼音来排列ListView里面的数据类
     */
    private PinyinComparator pinyinComparator;

    private Context mcontext;

    List<CompanyInfo> companyInfoList = null;

    private itemOnclickCallBack onclickCallBack;
    private MyApplication myApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_company);

        getActionBar().setDisplayUseLogoEnabled(false);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
        myApplication = MyApplication.getInstance();
        companyInfoList = new ArrayList<CompanyInfo>();
        mcontext = this;
        initViews();
    }

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    // 刷新UI

                    break;
            }
        }
    };

    /**
     * 初始化View
     */
    private void initViews() {
        // 实例化汉字转拼音类
        characterParser = CharacterParser.getInstance();
        pinyinComparator = new PinyinComparator();

        sideBar = (SideBar) findViewById(R.id.sidrbar);
        dialog = (TextView) findViewById(R.id.dialog);
        sideBar.setTextView(dialog);

        // TODO 设置右侧触摸监听
        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                if (s != null) {
                    // 该字母首次出现的位置
                    int position = adapter.getPositionForSection(s.charAt(0));
                    if (position != -1) {
                        sortListView.setSelection(position);
                    }
                }
            }
        });


        sortListView = (ListView) findViewById(R.id.country_lvcountry);
        sortListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // 这里要利用adapter.getItem(position)来获取当前position所对应的对象
                Toast.makeText(getApplication(), ((SortModel) adapter.getItem(position)).getName(), Toast.LENGTH_SHORT).show();

                // view.setBackgroundColor(Color.parseColor("#E0EEEE"));
                SortModel model = (SortModel) adapter.getItem(position);

                Bundle bundle = new Bundle();
                bundle.putString("companyName", model.getName());
                bundle.putString("companyId", model.getCompanyId());
                Intent intent = new Intent();
                intent.putExtras(bundle);
                setResult(8, intent);
                overridePendingTransition(R.anim.push_left_out, R.anim.push_left_in);
                SelectCompanyActivity.this.finish();
            }
        });


        // 动画效果
        AnimationSet set = new AnimationSet(true);
        //透明到完全显示
        Animation animation = new AlphaAnimation(0.0f, 1.0f);
        animation.setDuration(50);
        set.addAnimation(animation);
        animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(100);
        set.addAnimation(animation);
        LayoutAnimationController controller = new LayoutAnimationController(set, 0.5f);
        sortListView.setLayoutAnimation(controller);

        mClearEditText = (ClearEditText) findViewById(R.id.filter_edit);

        // 根据输入框输入值的改变来过滤搜索
        mClearEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // 当输入框里面的值为空，更新为原来的列表，否则为过滤数据列表
                filterData(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        companyInfoList = CompanyInfo.getCompanyInfoList(Common.dbh);
        // companyInfoList.addAll(myApplication.getCompanyInfoList()) ;
        // 数据源
        SourceDateList = filledData(companyInfoList);

        // 根据a-z进行排序源数据
        Collections.sort(SourceDateList, pinyinComparator);
        adapter = new SortAdapter(SelectCompanyActivity.this, SourceDateList);
        sortListView.setAdapter(adapter);

    }

    /**
     * TODO 为ListView填充数据
     *
     * @param list
     * @return
     */
    private List<SortModel> filledData(List<CompanyInfo> list) {
        List<SortModel> mSortList = new ArrayList<SortModel>();

        int size = list.size();
        for (int i = 0; i < size; i++) {
            SortModel sortModel = new SortModel();
            sortModel.setName(list.get(i).name);
            sortModel.setCompanyId(list.get(i).id);
            // 汉字转换成拼音
            String pinyin = characterParser.getSelling(list.get(i).name);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            // 正则表达式，判断首字母是否是英文字母
            if (sortString.matches("[A-Z]")) {
                sortModel.setSortLetters(sortString.toUpperCase());
            } else {
                sortModel.setSortLetters("#");
            }

            mSortList.add(sortModel);
        }
        return mSortList;

    }

    /**
     * TODO 根据输入框中的值来过滤数据并更新ListView
     *
     * @param filterStr
     */
    private void filterData(String filterStr) {
        List<SortModel> filterDateList = new ArrayList<SortModel>();

        if (TextUtils.isEmpty(filterStr)) {
            filterDateList = SourceDateList;
        } else {
            filterDateList.clear();
            for (SortModel sortModel : SourceDateList) {
                String name = sortModel.getName();
                if (name.indexOf(filterStr.toString()) != -1 || characterParser.getSelling(name).startsWith(filterStr.toString())) {
                    filterDateList.add(sortModel);
                }
            }
        }

        // 根据a-z进行排序
        Collections.sort(filterDateList, pinyinComparator);
        adapter.updateListView(filterDateList);
    }

    public void setOnclickCallBack(itemOnclickCallBack onclickCallBack) {
        this.onclickCallBack = onclickCallBack;
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


    /**
     * 接口用于回调
     *
     * @author
     */
    public interface itemOnclickCallBack {

        void itemOnClick(HashMap<String, String> map);
    }


}
