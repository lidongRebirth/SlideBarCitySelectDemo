package com.dlm.slidebarcityselectdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dlm.slidebarcityselectdemo.adapter.CityAdapter;
import com.dlm.slidebarcityselectdemo.bean.ItemBean;
import com.dlm.slidebarcityselectdemo.util.CityComparator;
import com.dlm.slidebarcityselectdemo.util.HanziToPinYin;
import com.dlm.slidebarcityselectdemo.view.SideBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


/**
 * @Author LD
 * @Time 2019.5.13 10：:2
 * @Describe 城市右侧字母滑动选择
 * @Modify
 */
public class MainActivity extends AppCompatActivity implements OnRecyclerViewClickListener {

    private List<ItemBean> itemList;    //所有的item子项，可能是城市、可能是字母
    private List<String> cityList;      //所有的城市名

    private CityAdapter cityAdapter;
    private RecyclerView recyclerView;
    private TextView tvDialog;
    private SideBar sideBar;


    //目标项是否在最后一个可见项之后
    private boolean mShouldScroll;
    //记录目标项位    置(要移动到的位置)
    private int mToPosition;

    private static final String TAG = "ceshi";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        tvDialog = findViewById(R.id.tv_dialog);
        sideBar = findViewById(R.id.sideBar);
        sideBar.setTextDialog(tvDialog);


        initData();

        sideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(int position) {
                String city_label = SideBar.characters[position];      //滑动到的字母

                for (int i = 0; i < cityList.size(); i++) {
                    if (itemList.get(i).getItemName().equals(city_label)) {


                        moveToPosition(i);                              //直接滚过去
//                        smoothMoveToPosition(recyclerView,i);         //平滑的滚动

                        tvDialog.setVisibility(View.VISIBLE);
                        break;
                    }
                    if (i == cityList.size() - 1) {
                        tvDialog.setVisibility(View.INVISIBLE);
                    }

                }
            }
        });
    }
    /**
     * 初始化数据，将所有城市进行排序，且加上字母和他们一起  最重要的在这里
     */
    private void initData() {

        //获取所有的城市名
        String[] cityArray = getResources().getStringArray(R.array.mycityarray);
        cityList = Arrays.asList(cityArray);

        itemList = new ArrayList<>();
        //*----------------------------------------------------------------------------------------
        Collections.sort(cityList, new CityComparator());                           //将所有城市进行排序(按从A到Z)

        String currentLetter = HanziToPinYin.toPinYin(cityList.get(0)) + "";    //获取到的是首字母
        //第一个以字母开头，所以设置类型为head,内容就为该字母
        ItemBean itemBean = new ItemBean();
        itemBean.setItemName(currentLetter);
        itemBean.setItemType("head");
        itemList.add(itemBean);         //加入到整理后的list集合中

        //将剩余的城市加进去
        for (int i = 0; i < cityList.size(); i++) {

            String city = cityList.get(i);
            String letter = null;                          //当前字母

            if (city.contains("重庆")) {
                letter = HanziToPinYin.toPinYin("崇庆") + "";

            } else {
                letter = HanziToPinYin.toPinYin(cityList.get(i)) + "";
            }

            if (letter.equals(currentLetter)) {           //在A字母下，属于当前字母
                itemBean = new ItemBean();
                itemBean.setItemName(city);             //把汉字放进去
                itemBean.setItemType(letter);           //把拼音放进去
                itemList.add(itemBean);
            } else {                                     //不属于当前字母
                //添加标签
                itemBean = new ItemBean();
                itemBean.setItemName(letter);           //把首字母进去
                itemBean.setItemType("head");          //把head标签放进去
                currentLetter = letter;
                itemList.add(itemBean);

                //添加城市
                itemBean = new ItemBean();
                itemBean.setItemName(city);             //把汉字放进去
                itemBean.setItemType(letter);           //把拼音放进去
                itemList.add(itemBean);
            }
        }

        cityAdapter = new CityAdapter(itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(cityAdapter);
        cityAdapter.setOnItemClickListener(this);
    }

    @Override
    public void onItemClickListener(View view) {
        int position = recyclerView.getChildAdapterPosition(view);
        Toast.makeText(view.getContext(), itemList.get(position).getItemName(), Toast.LENGTH_SHORT).show();
    }

    private void moveToPosition(int position) {
        if (position != -1) {
            recyclerView.scrollToPosition(position);
            LinearLayoutManager mLayoutManager =
                    (LinearLayoutManager) recyclerView.getLayoutManager();
            mLayoutManager.scrollToPositionWithOffset(position, 0);
        }
    }

    /**
     * 使指定的项平滑到顶部
     * @param mRecyclerView
     * @param position      待指定的项
     */
    private void smoothMoveToPosition(RecyclerView mRecyclerView, final int position) {
        int firstItemPosition = -1;
        int lastItemPosition = -1;

        //todo 获取第一个和最后一个可见位置方式1
        // 第一个可见位置
        firstItemPosition = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(0));
        // 最后一个可见位置
        lastItemPosition = mRecyclerView.getChildLayoutPosition(mRecyclerView.getChildAt(mRecyclerView.getChildCount() - 1));

        //todo 获取第一个和最后一个可见位置方式2
        // 判断是当前layoutManager是否为LinearLayoutManager
        // 只有LinearLayoutManager才有查找第一个和最后一个可见view位置的方法
//        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
//        if (layoutManager instanceof LinearLayoutManager) {
//            LinearLayoutManager linearManager = (LinearLayoutManager) layoutManager;
//            //获取第一个可见view的位置
//            firstItemPosition = linearManager.findFirstVisibleItemPosition();
//            //获取最后一个可见view的位置
//            lastItemPosition = linearManager.findLastVisibleItemPosition();
//
//        }

        Log.i(TAG, "smoothMoveToPosition: firstItemPosition::" + firstItemPosition + " lastItemPosition::" + lastItemPosition + "\n");

        if (position < firstItemPosition) {
            // 第一种可能:跳转位置在第一个可见位置之前
            mRecyclerView.smoothScrollToPosition(position);
        } else if (position <= lastItemPosition) {
            // 第二种可能:跳转位置在第一个可见位置之后,在最后一个可见项之前
            int movePosition = position - firstItemPosition;
            if (movePosition >= 0 && movePosition < mRecyclerView.getChildCount()) {
                int top = mRecyclerView.getChildAt(movePosition).getTop();
                mRecyclerView.smoothScrollBy(0, top);//dx>0===>向左  dy>0====>向上
            }
        } else {
            // 第三种可能:跳转位置在最后可见项之后
            mRecyclerView.smoothScrollToPosition(position);
            mToPosition = position;
            mShouldScroll = true;
        }
    }


}
