package com.lxf.googlemapsdemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.lxf.googlemapsdemo.R;
import com.lxf.googlemapsdemo.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GoogleLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private static final String TAG = "lxf-GoogleLocationActivity";
    private GoogleMap mMap;
    private TextView tvStatus;
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<LatLng> routeLists = new ArrayList<>();
    private SharedPreferences.Editor editor;
    /**
     * 地图上的轨迹线
     */
    private PolylineOptions rectOptions = new PolylineOptions();
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_location);
        tvStatus = findViewById(R.id.tv_status);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_google);
        // 地图的初始化
        mapFragment.getMapAsync(this);


        // 初始化位置服务
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // 位置变化回调
        mLocationCallback = new LocationCallback() {

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);

            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e(TAG, "接收到位置变化信息");
                if (locationResult == null) {
                    return;
                }
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    // 此位置的高度
                    double altitude = lastLocation.getAltitude();
                    // 获取纬度
                    double latitude = lastLocation.getLatitude();
                    // 获取经度
                    double longitude = lastLocation.getLongitude();
                    Log.e(TAG, "latitude==" + latitude + ";longitude==" + longitude);
                    // 返回生成此修复程序的提供程序的名称。
                    String provider = lastLocation.getProvider();
                    // 获得速度
                    float speed = lastLocation.getSpeed();
                    // 返回此修复的UTC时间，以1970年1月1日以来的毫秒数为单位。
                    long time = lastLocation.getTime();

                    routeLists.add(new LatLng(lastLocation.getLatitude(),
                            lastLocation.getLongitude()));
                    PolylineOptions rectOptions = new PolylineOptions();
                    rectOptions.addAll(routeLists);

                    Polyline polyline = mMap.addPolyline(rectOptions);
                    // 棕色
                    polyline.setColor(Color.parseColor("#A52A2A"));

                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                            routeLists.get(routeLists.size() - 1), 18f));
                }
            }
        };
        // 创建位置服务请求
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.e(TAG, "开始位置变化请求");
            // 目前测试，该监听在华为手机上可用，在小米手机上监听不到信息
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapSettings(googleMap);

        modifyGoogleMapDisplayStyle(googleMap);

        getDeviceLocation();
    }

    /**
     * 设置一些google地图的配置
     *
     * @param googleMap
     */
    private void googleMapSettings(GoogleMap googleMap) {
        mMap = googleMap;
        if (mMap == null) {
            return;
        }
        UiSettings mMapUiSettings = mMap.getUiSettings();
        // 禁用旋转手势
        mMapUiSettings.setRotateGesturesEnabled(false);
        // 禁用倾斜手势
        mMapUiSettings.setTiltGesturesEnabled(false);
        // 启动缩放按钮
        mMapUiSettings.setZoomControlsEnabled(true);
        // 禁用地图工具栏
        mMapUiSettings.setMapToolbarEnabled(false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // 启用我的位置图层
            mMap.setMyLocationEnabled(true);
            // 启动我的位置定位按钮
            mMapUiSettings.setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
        }
    }

    /**
     * google定位，获取当前位置（不准，拿到的位置是上一次记录的位置）
     */
    private void getDeviceLocation() {
        if (mFusedLocationClient == null) {
            // 初始化位置服务
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        }
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Location mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                saveInforToSp("Google定位成功：" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + "-" + Utils.getCurrentTime());

                                routeLists.add(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()));
                                mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("当前位置"));
                                PolylineOptions rectOptions = new PolylineOptions();
                                rectOptions.addAll(routeLists);

                                Polyline polyline = mMap.addPolyline(rectOptions);
                                polyline.setColor(ContextCompat.getColor(GoogleLocationActivity.this, R.color.color_ff9d0a));
                                // 将地图移动到当前所在位置
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        routeLists.get(routeLists.size() - 1), 18f));
                            } else {
                                Log.d(TAG, "mLastKnownLocation is null.");
                                Toast.makeText(GoogleLocationActivity.this, "定位成功，位置信息返回null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(GoogleLocationActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.884108, 116.314864), 15f));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * 修改google地图显示样式：https://mapstyle.withgoogle.com/
     *
     * @param googleMap
     */
    @SuppressLint("LongLogTag")
    private void modifyGoogleMapDisplayStyle(GoogleMap googleMap) {
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        if (location != null) {
            LatLng sydney = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(sydney).title("当前位置"));

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), 18f));
        }
        Toast.makeText(this, "获取当前定位", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void saveInforToSp(String infor) {
        if (editor == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(this.getLocalClassName(), Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        editor.putString(this.getLocalClassName(), infor);
        editor.commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}
