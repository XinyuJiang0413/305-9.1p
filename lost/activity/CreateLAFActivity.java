package com.example.lost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.example.lost.R;
import com.example.lost.entity.LostAndFound;
import com.example.lost.sqlite.DAOService;
import com.example.lost.utils.ToastUtils;
import com.example.lost.utils.Utils;
import com.google.android.material.textfield.TextInputLayout;

public class CreateLAFActivity extends MyBaseActivity {
    private String mCurrentCity;
    private double latitude;
    private double longitude;
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    private TextView tvAddress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_botany);
        tvAddress = findViewById(R.id.edit_5);

        
        findViewById(R.id.save_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str1 = ((EditText) findViewById(R.id.edit_1)).getText().toString().trim();
                String str2 = ((EditText) findViewById(R.id.edit_2)).getText().toString().trim();
                String str3 = ((EditText) findViewById(R.id.edit_3)).getText().toString().trim();
                String str4 = ((EditText) findViewById(R.id.edit_4)).getText().toString().trim();
                String str5 = tvAddress.getText().toString().trim();
                if (TextUtils.isEmpty(str1) || TextUtils.isEmpty(str2) || TextUtils.isEmpty(str3)
                || TextUtils.isEmpty(str4) || TextUtils.isEmpty(str5) ) {
                    ToastUtils.showShortToast(getApplicationContext(), "info is empty");
                } else {
                    // 插入失物信息
                    LostAndFound botany = new LostAndFound();
                    botany.setName(str1);
                    botany.setPhone(str2);
                    botany.setDesc(str3);
                    botany.setDate(str4);
                    botany.setLocation(str5);
                    botany.setLongitude(longitude);
                    botany.setLatitude(latitude);
                    DAOService.getInstance().insertHealthInfo(botany);
                    // 社团关系表里加入一条人员 就是创建者
                    ToastUtils.showShortToast(getApplicationContext(), "insert success");
                    setResult(-1);
                    finish();
                }
            }
        });
        findViewById(R.id.get_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //开始定位
                mLocClient.restart();
            }
        });
        findViewById(R.id.edit_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CreateLAFActivity.this, FindActivity.class);
                intent.putExtra("city", mCurrentCity);
                startActivityForResult(intent, 100);
            }
        });
        mLocClient = new LocationClient(getApplicationContext());
        LocationClientOption locationOption = new LocationClientOption();
        mLocClient.registerLocationListener(myListener);
        //可选，默认false，设置是否开启Gps定位
        locationOption.setOpenGps(true);
        //可选，设置是否需要地址信息，默认不需要
        locationOption.setIsNeedAddress(true);
        //需将配置好的LocationClientOption对象，通过setLocOption方法传递给LocationClient对象使用
        mLocClient.setLocOption(locationOption);
        //开始定位
        mLocClient.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 100) {
            latitude = data.getDoubleExtra("latitude", 0);
            longitude = data.getDoubleExtra("longitude", 0);
            String address = data.getStringExtra("address");
            if (latitude != 0 && longitude != 0) {
                tvAddress.setText(address);
            } else {
                Toast.makeText(this, "请选择正确的地址", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            int errorCode = location.getLocType();
            //获取纬度信息
            latitude = location.getLatitude();
            //获取经度信息
            longitude = location.getLongitude();
            mCurrentCity = location.getAddrStr();
            tvAddress.setText(mCurrentCity);
        }

    }
}
