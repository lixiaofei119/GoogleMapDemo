package com.lxf.googlemapsdemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
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
import java.util.Iterator;

public class SystemLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private static final String TAG = "lxf-SystemLocationActivity";
    private GoogleMap mMap;
    private TextView tvStatus;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    private ArrayList<LatLng> routeLists = new ArrayList<>();
    private LocationManager locationManager;
    private GpsStatus.Listener satelliteSignalListener;
    private SharedPreferences.Editor editor;
    /**
     * 地图上的轨迹线
     */
    private PolylineOptions rectOptions = new PolylineOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_location);
        tvStatus = findViewById(R.id.tv_status);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_system);
        // 地图的初始化
        mapFragment.getMapAsync(this);

        addGPSListener();

        addLocationUpdates();
    }

    private void addLocationUpdates() {
        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
        // 修改位置监听时间&&距离
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        /**
         * 添加位置变化监听
         * 设置最小时间 5秒
         */
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 5, 1, locationListener);
    }

    /**
     * GPS信号监听
     */
    private void addGPSListener() {
        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (satelliteSignalListener == null) {
            satelliteSignalListener = new GpsStatus.Listener() {
                @SuppressLint("LongLogTag")
                @Override
                public void onGpsStatusChanged(int event) {
                    switch (event) {
                        //第一次定位
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            Log.e(TAG, "第一次定位");
                            saveInforToSp("第一次定位" + Utils.getCurrentTime());
                            break;
                        // 卫星状态改变
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            Log.e(TAG, "卫星状态改变");
                            // 获取当前状态 
                            @SuppressLint("MissingPermission") GpsStatus gpsStatus = locationManager.getGpsStatus(null);
                            // 获取卫星颗数的默认最大值 
                            int maxSatellites = gpsStatus.getMaxSatellites();
                            // 创建一个迭代器保存所有卫星  
                            Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                            int count = 0;
                            while (iters.hasNext() && count <= maxSatellites) {
                                GpsSatellite s = iters.next();
                                count++;
                                float snr = s.getSnr();
                                Log.e(TAG, "onGpsStatusChanged: 信噪比 :" + snr);
                                if (snr > 30) {
                                    count++;
                                }
                            }
                            // 定位最少四颗卫星才开始
                            if (count > 20) {
                                // 高
                                tvStatus.setText("GPS信号：高");
                                saveInforToSp("GPS信号：高" + Utils.getCurrentTime());
                            } else if (count <= 20 && count > 10) {
                                // 中
                                tvStatus.setText("GPS信号：中");
                                saveInforToSp("GPS信号：中" + Utils.getCurrentTime());
                            } else if (count <= 10 && count > 5) {
                                // 低
                                tvStatus.setText("GPS信号：低");
                                saveInforToSp("GPS信号：低" + Utils.getCurrentTime());
                            } else {
                                // 小于5颗卫星，无法定位
                                tvStatus.setText("GPS信号：无");
                                saveInforToSp("GPS信号：无" + Utils.getCurrentTime());
                            }
                            Log.e(TAG, "搜索到 :" + count + "颗卫星  max : " + maxSatellites);
                            break;// 定位启动 
                        case GpsStatus.GPS_EVENT_STARTED:
                            Log.e(TAG, "定位启动");
                            saveInforToSp("定位启动" + Utils.getCurrentTime());
                            break;
                        //定位结束 
                        case GpsStatus.GPS_EVENT_STOPPED:
                            Log.e(TAG, "定位结束");
                            saveInforToSp("定位结束" + Utils.getCurrentTime());
                            break;
                        default:
                            break;
                    }
                }
            };
        }
        locationManager.addGpsStatusListener(satelliteSignalListener);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapSettings(googleMap);

        modifyGoogleMapDisplayStyle(googleMap);

        // addMarkerPoint();

        // addRoute();

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
                                polyline.setColor(ContextCompat.getColor(SystemLocationActivity.this, R.color.color_ff9d0a));
                                // 将地图移动到当前所在位置
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        routeLists.get(routeLists.size() - 1), 18f));
                            } else {
                                Log.d(TAG, "mLastKnownLocation is null.");
                                Toast.makeText(SystemLocationActivity.this, "定位成功，位置信息返回null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SystemLocationActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
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
     * 添加标记点 Add a marker in Sydney and move the camera
     */
    private void addMarkerPoint() {
        LatLng sydney = new LatLng(39.884108, 116.314864);
        mMap.addMarker(new MarkerOptions().position(sydney).title("起点"));
        mMap.addMarker(new MarkerOptions().position(new LatLng(22.819203, 120.544794)).title("终点"));

        LatLng sydney2 = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney2).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney2));
    }

    /**
     * 添加线/路径 Instantiates a new Polyline object and adds points to define a rectangle
     */
    private void addRoute() {
        PolylineOptions rectOptions = new PolylineOptions();
        //rectOptions.add(new LatLng(39.884108, 116.314864));
        ArrayList<LatLng> latLngs = new ArrayList<>();
        latLngs.add(new LatLng(39.932025, 116.446304));
        latLngs.add(new LatLng(39.931823, 116.446139));
        latLngs.add(new LatLng(39.931684, 116.446032));
        latLngs.add(new LatLng(39.931363, 116.445925));
        latLngs.add(new LatLng(39.930919, 116.445866));
        latLngs.add(new LatLng(39.930565, 116.445941));
        latLngs.add(new LatLng(39.930224, 116.446129));
        latLngs.add(new LatLng(39.929850, 116.446445));
        latLngs.add(new LatLng(39.929611, 116.446955));
        latLngs.add(new LatLng(39.929549, 116.447411));
        latLngs.add(new LatLng(39.929644, 116.447883));
        latLngs.add(new LatLng(39.929825, 116.448237));
        latLngs.add(new LatLng(39.930080, 116.448489));
        latLngs.add(new LatLng(39.930405, 116.448612));
        latLngs.add(new LatLng(39.930833, 116.448703));
        latLngs.add(new LatLng(39.931240, 116.448660));
        latLngs.add(new LatLng(39.931549, 116.448547));
        latLngs.add(new LatLng(39.931964, 116.448257));
        latLngs.add(new LatLng(39.932235, 116.447801));
        latLngs.add(new LatLng(39.932284, 116.447286));
        latLngs.add(new LatLng(39.932245, 116.446792));
        latLngs.add(new LatLng(39.932048, 116.446362));
        latLngs.add(new LatLng(39.931820, 116.446126));
        latLngs.add(new LatLng(39.931492, 116.445964));

        rectOptions.addAll(latLngs);

        Polyline polyline = mMap.addPolyline(rectOptions);

        polyline.setColor(ContextCompat.getColor(SystemLocationActivity.this, R.color.color_ff9d0a));

        LatLng bj = latLngs.get(latLngs.size() - 1);
        // 缩放级别范围2-21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bj, 17));
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

    /**
     * 位置变化监听
     */
    private LocationListener locationListener = new LocationListener() {
        /**
         * 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         *
         * @param location
         */
        @SuppressLint("LongLogTag")
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                // 获取纬度
                double latitude = location.getLatitude();
                // 获取经度
                double longitude = location.getLongitude();
                // 获得速度  1m/s=0.001km/(1/3600)h=3.6km/h
                float speed = location.getSpeed();
                Log.e(TAG, "GPS位置变化监听 latitude ==" + latitude + ";longitude ==" + longitude + "\n speed==" + speed);
                saveInforToSp("GPS位置变化监听：" + location.getLatitude() + "," + location.getLongitude() + "-" + Utils.getCurrentTime());
                if (mMap != null && rectOptions != null) {
                    // 添加起点标记
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
//                    // 添加自定义起点标记
//                    View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
//                    TextView tvMarker = marker.findViewById(R.id.tv_marker);
//                    tvMarker.setText(getResources().getString(R.string.sports_go));
//                    mMap.addMarker(new MarkerOptions()
//                            .position(latLng)
//                            .icon(BitmapDescriptorFactory.fromBitmap(Utils.createDrawableFromView(this, marker))));
                    // 添加运动路线
                    rectOptions.add(latLng);
                    Polyline polyline = mMap.addPolyline(rectOptions);
                    // 设置地图轨迹颜色
                    polyline.setColor(ContextCompat.getColor(SystemLocationActivity.this, R.color.color_ff9d0a));
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 15));
                }
            }
        }

        /**
         * Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
         *
         * @param provider
         * @param status
         * @param extras
         */
        @SuppressLint("LongLogTag")
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "provider == " + provider + ";status == " + status + ";extras == " + extras.toString());
            saveInforToSp("GPS位置变化监听：" + "provider == " + provider + ";status == " + status + ";extras == " + extras.toString() + "-" + Utils.getCurrentTime());
        }

        /**
         * Provider被enable时触发此函数，比如GPS被打开
         *
         * @param provider
         */
        @SuppressLint("LongLogTag")
        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled provider == " + provider);
            saveInforToSp("GPS位置变化监听：" + "onProviderEnabled provider == " + provider + "-" + Utils.getCurrentTime());
        }

        /**
         * Provider被disable时触发此函数，比如GPS被关闭
         *
         * @param provider
         */
        @SuppressLint("LongLogTag")
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled provider == " + provider);
            saveInforToSp("GPS位置变化监听：" + "onProviderDisabled provider == " + provider + "-" + Utils.getCurrentTime());
        }
    };

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
        editor.clear();
        editor.commit();

        if (satelliteSignalListener != null) {
            locationManager.removeGpsStatusListener(satelliteSignalListener);
        }
        satelliteSignalListener = null;
        if (locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
        if (locationManager != null) {
            locationManager = null;
        }
    }
}
