package com.example.lost.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.lost.R;
import com.example.lost.entity.LostAndFound;
import com.example.lost.sqlite.DAOService;
import com.example.lost.utils.ToastUtils;

public class LostAndFoundDetailActivity extends MyBaseActivity{
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_botany_info);

        final int healthId = getIntent().getIntExtra("healthId",-1);

        LostAndFound lostAndFound = DAOService.getInstance().searchByHealthId(healthId);
        if(null == lostAndFound) {
            return;
        }
        ((TextView)findViewById(R.id.tv1)).setText("name:"+lostAndFound.getName());
        ((TextView)findViewById(R.id.tv2)).setText("date:"+lostAndFound.getDate());
        ((TextView)findViewById(R.id.tv3)).setText("location:"+lostAndFound.getLocation());


        findViewById(R.id.remove_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DAOService.getInstance().deleteTrafficInfoById(healthId);
                ToastUtils.showShortToast(getApplicationContext(),"delete success");
                finish();
            }
        });
    }
}
