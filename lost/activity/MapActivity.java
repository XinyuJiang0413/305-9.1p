package com.example.lost.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.navi.BaiduMapAppNotSupportNaviException;
import com.baidu.mapapi.navi.BaiduMapNavigation;
import com.baidu.mapapi.navi.NaviParaOption;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.lost.R;
import com.example.lost.entity.LostAndFound;
import com.example.lost.sqlite.DAOService;
import com.example.lost.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity {
    private Activity myActivity;
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private double stratLatitude;//定位纬度
    private double stratLongitude;//定位经度
    private MyLocationListener mLocationListener;
    private LocationClient mLocationClient;
    private BitmapDescriptor othersCurrentMarker;
    private RequestOptions headerRO = new RequestOptions().circleCrop();//圆角变换

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        myActivity = this;
        mMapView = findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        initView();

    }

    private void initView() {
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        //声明LocationClient类
        LocationClient mLocationClient = new LocationClient(myActivity);
        //注册监听函数
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);
        initLocation();//定位
        //获取位置
        initFound();

    }

    private void initFound() {

        final List<LostAndFound> list = DAOService.getInstance().searchHealthInfoByStartAndEnd();
        for (LostAndFound found : list) {
            addOthersLocation(found.getLatitude(),found.getLongitude());
        }

    }

    //定位自己
    private void initLocation() {
        //开启定位
        mBaiduMap.setMyLocationEnabled(true);
        //声明LocationClient类
        mLocationClient = new LocationClient(myActivity);
        //注册监听函数
        mLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mLocationListener);

        //==配置参数
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系---注意和初始化时的设置对应
        //设置定位间隔为10s，不能小于1s即1000ms
        option.setScanSpan(10000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setIgnoreKillProcess(false);//可选，默认false，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认杀死
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
        //调用LocationClient的start()方法，便可发起定位请求
        //start()：启动定位SDK；stop()：关闭定位SDK。调用start()之后只需要等待定位结果自动回调即可。
        mLocationClient.start();
    }

    class MyLocationListener extends BDAbstractLocationListener {
        boolean isZoomMap = true;//标识是否以定位位置为中心缩放地图

        @Override
        public void onReceiveLocation(BDLocation location) {
            //显示当前位置
            // Toast.makeText(myActivity, location.getAddress().address,Toast.LENGTH_LONG).show();
            //获取当前位置纬度
            stratLatitude = location.getLatitude();
            //获取当前位置经度
            stratLongitude = location.getLongitude();
            //第一次定位时调整地图缩放
            if (isZoomMap) {
                //纬度，经度
                LatLng latLng = new LatLng(stratLatitude, stratLongitude);
                // 改变地图状态，使地图以定位地址为目标，显示缩放到恰当的大小
                MapStatus mapStatus = new MapStatus.Builder()
                        .target(latLng)//目标
                        .zoom(16.0f)//缩放
                        .build();
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mapStatus));
                isZoomMap = false;
            }
            //在地图上显示当前位置
            MyLocationData locationData = new MyLocationData.Builder()
                    .latitude(stratLatitude)//纬度
                    .longitude(stratLongitude)//经度
                    .build();
            mBaiduMap.setMyLocationData(locationData);
        }
    }


    // 添加他人位置
    public void addOthersLocation(final double longitude, final double latitude) {
        Glide.with(myActivity)
                .asBitmap()
                .load(R.drawable.re)
                .apply(headerRO)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        //定义Maker坐标点
                        LatLng point = new LatLng(longitude, latitude);
                        //构建MarkerOption，用于在地图上添加Marker

                        othersCurrentMarker = BitmapDescriptorFactory
                                .fromBitmap(zoomImage(resource, 100, 100));
                        OverlayOptions option = new MarkerOptions()  //构建Marker图标
                                .position(point)
                                .icon(othersCurrentMarker);
                        mBaiduMap.addOverlay(option);
                    }
                });
    }

    //缩放头像图片
    public Bitmap zoomImage(Bitmap bgimage, double newWidth,
                            double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

    //申请权限的回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mLocationClient.isStarted()) {
            mLocationClient.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient.isStarted()) {
            mLocationClient.stop();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == 1) {
            initFound();
        }
    }
}