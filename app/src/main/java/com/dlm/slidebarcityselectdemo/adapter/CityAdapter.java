package com.dlm.slidebarcityselectdemo.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dlm.slidebarcityselectdemo.OnRecyclerViewClickListener;
import com.dlm.slidebarcityselectdemo.R;
import com.dlm.slidebarcityselectdemo.bean.ItemBean;
import java.util.List;

/**
 * @Author LD
 * @Time 2019/5/13 10:50
 * @Describe 城市适配器，用来填充城市(多布局)
 * @Modify
 */
public class CityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    //数据项
    private List<ItemBean> dataList;
    //点击事件监听接口
    private OnRecyclerViewClickListener onRecyclerViewClickListener;


    public void setOnItemClickListener(OnRecyclerViewClickListener onItemClickListener) {
        this.onRecyclerViewClickListener = onItemClickListener;
    }


    public CityAdapter(List<ItemBean> dataList) {
        this.dataList = dataList;
    }

    //创建ViewHolder实例
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {

        if (viewType == 0) {    //Head头字母名称
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_head, viewGroup,false);
            RecyclerView.ViewHolder headViewHolder = new HeadViewHolder(view);
            return headViewHolder;
        } else {             //城市名
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_city, viewGroup,false);
            RecyclerView.ViewHolder cityViewHolder = new CityViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onRecyclerViewClickListener != null) {
                        onRecyclerViewClickListener.onItemClickListener(v);
                    }
                }
            });

            return cityViewHolder;
        }
    }

    //对子项数据进行赋值
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        int itemType = dataList.get(position).getType();
        if (itemType == 0) {
            HeadViewHolder headViewHolder = (HeadViewHolder) viewHolder;
            headViewHolder.tvHead.setText(dataList.get(position).getItemName());
        } else {
            CityViewHolder cityViewHolder = (CityViewHolder) viewHolder;
            cityViewHolder.tvCity.setText(dataList.get(position).getItemName());
        }
    }

    //数据项个数
    @Override
    public int getItemCount() {
        return dataList.size();
    }

    //区分布局类型
    @Override
    public int getItemViewType(int position) {

        int type = dataList.get(position).getType();
        return type;
    }

    //字母头
    public static class HeadViewHolder extends RecyclerView.ViewHolder {

        private TextView tvHead;

        public HeadViewHolder(View itemView) {
            super(itemView);

            tvHead = itemView.findViewById(R.id.tv_item_head);


        }
    }

    //城市
    public static class CityViewHolder extends RecyclerView.ViewHolder {

        private TextView tvCity;


        public CityViewHolder(View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tv_item_city);


        }
    }


}
