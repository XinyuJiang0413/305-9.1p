package com.example.lost.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.example.lost.R;

import java.util.ArrayList;
import java.util.List;

public class FindActivity extends AppCompatActivity {
    private ListView lv_address;
    private EditText ed_input;
    private String city;
    SuggestionSearch mSuggestionSearch;
    private static final String TAG = "jcy-InputActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find);
        ed_input = findViewById(R.id.ed_input);
        lv_address = findViewById(R.id.lv_address);
        city = getIntent().getStringExtra("city");
        mItemBeans.clear();
        lv_address.setAdapter(mAdapter);
        lv_address.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ItemBean itemBean = mItemBeans.get(position);
                Intent intent = new Intent();
                intent.putExtra("longitude", itemBean.point.longitude);
                intent.putExtra("latitude", itemBean.point.latitude);
                intent.putExtra("address", itemBean.key);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        mSuggestionSearch = SuggestionSearch.newInstance();
        OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
            @Override
            public void onGetSuggestionResult(SuggestionResult suggestionResult) {
                //处理sug检索结果
                List<SuggestionResult.SuggestionInfo> suggestionInfos = suggestionResult.getAllSuggestions();
                Log.d(TAG, "error: " + suggestionResult.error);
                Log.d(TAG, "status: " + suggestionResult.status);
                Log.d(TAG, "describeContents: " + suggestionResult.describeContents());

                mItemBeans.clear();
                if (suggestionInfos != null) {
                    for (int i = 0; i < suggestionInfos.size(); i++) {
                        SuggestionResult.SuggestionInfo info = suggestionInfos.get(i);
                        Log.d(TAG, "Address: " + info.getAddress());
                        Log.d(TAG, "City: " + info.getCity());
                        Log.d(TAG, "District: " + info.getDistrict());
                        Log.d(TAG, "Key: " + info.getKey());
                        mItemBeans.add(new ItemBean(info.getPt(), info.getAddress(),info.getCity(),info.getDistrict(),info.getKey()));
                    }
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        };
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);
        ed_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String key = s.toString();
                Log.d(TAG, "onTextChanged: key " + key);
                if (!TextUtils.isEmpty(key)) {
                    search(key);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String key = s.toString();
                Log.d(TAG, "afterTextChanged: key " + key);
                if (!TextUtils.isEmpty(key)) {
                    search(key);
                }
            }
        });

    }


    private void search(String key) {
        /**
         * 在您的项目中，keyword为随您的输入变化的值
         */
        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                .city(city).keyword(key) // 关键字
        );
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSuggestionSearch.destroy();
    }

    class ItemBean {
        LatLng point;
        String address;
        String city;
        String dis;
        String key;

        public ItemBean(LatLng point, String address, String city, String dis, String key) {
            this.point = point;
            this.address = address;
            this.city = city;
            this.dis = dis;
            this.key = key;
        }
    }

    private ArrayList<ItemBean> mItemBeans = new ArrayList<>();
    private BaseAdapter mAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return mItemBeans.size();
        }

        @Override
        public Object getItem(int position) {
            return mItemBeans.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = LayoutInflater.from(FindActivity.this).inflate(R.layout.item_address, parent, false);
            TextView sug_key = view.findViewById(R.id.sug_key);
            TextView sug_city = view.findViewById(R.id.sug_city);
            TextView sug_dis = view.findViewById(R.id.sug_dis);
            if(position==0){
                sug_key.setText(mItemBeans.get(position).address);
            }else {
                sug_key.setText(mItemBeans.get(position).key);
                sug_city.setText(mItemBeans.get(position).city);
                sug_dis.setText(mItemBeans.get(position).dis);
            }

            return view;
        }
    };
}
