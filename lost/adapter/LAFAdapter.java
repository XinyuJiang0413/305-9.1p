package com.example.lost.adapter;

import androidx.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.example.lost.R;
import com.example.lost.entity.LostAndFound;

import java.util.List;


public class LAFAdapter extends BaseQuickAdapter<LostAndFound, BaseViewHolder> {
    public LAFAdapter(@Nullable List<LostAndFound> data) {
        super(R.layout.traffic_item, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, LostAndFound item) {
        helper.setText(R.id.tv,item.getName());
    }
}
