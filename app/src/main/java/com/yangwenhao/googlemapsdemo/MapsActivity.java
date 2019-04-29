package com.yangwenhao.googlemapsdemo;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {

    private static final String TAG = "lxf-MapsActivity";

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    private Location mLastKnownLocation;
    /**
     * 是否开启位置更新回调
     */
    private boolean mRequestingLocationUpdates = true;
    /**
     * 位置更新的回调
     */
    private LocationCallback mLocationCallback;
    /**
     * 位置更新请求参数设置
     */
    private LocationRequest mLocationRequest;
    private LocationManager locationMgr;
    private GpsStatus.Listener listener;
    private TextView tvGpsInfo;
    private StringBuffer info;
    private ArrayList<LatLng> routeLists = new ArrayList<>();
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // 地图的初始化
        mapFragment.getMapAsync(this);

        SharedPreferences sharedPreferences = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        tvGpsInfo = findViewById(R.id.tv_gps_info);
        tvGpsInfo.setMovementMethod(new ScrollingMovementMethod());
        info = new StringBuffer();

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
                if (locationResult == null) {
                    return;
                }
                String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                editor.putString("LocationCallback-onLocationResult" + format, locationResult.getLastLocation().getLatitude() + ";" + locationResult.getLastLocation().getLongitude() + ";数量：" + locationResult.getLocations().size());
                editor.commit();
                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    // 此位置的高度
                    double altitude = lastLocation.getAltitude();
                    // 获取纬度
                    double latitude = lastLocation.getLatitude();
                    // 获取经度
                    double longitude = lastLocation.getLongitude();
                    // 返回生成此修复程序的提供程序的名称。
                    String provider = lastLocation.getProvider();
                    // 获得速度
                    float speed = lastLocation.getSpeed();
                    // 返回此修复的UTC时间，以1970年1月1日以来的毫秒数为单位。
                    long time = lastLocation.getTime();
                    MyLocationBean myLocationBean = new MyLocationBean(altitude, latitude, longitude, provider, speed, time);
                    String data = myLocationBean.toString();
                    String format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                    editor.putString(format2, data);
                    editor.commit();

//                showInfoToTextView("2 22 当前位置  :" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + " && 点总数：" + routeLists.size(), false);
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


//                for (Location location : locationResult.getLocations()) {
//                    // 用位置数据更新UI
//                    Log.e("ywh_update_location", "onLocationResult: " + location.toString());
//                    String format2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
//                    editor.putString("LocationCallback-onLocationResult2" + format2, location.getLatitude() + ";" + location.getLongitude());
//                    editor.commit();
//                    showInfoToTextView("onLocationResult: " + location.toString(), true);
//                    mMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("位置变化"));
//                }
            }
        };
        // 创建位置服务请求
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(2000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0, locationListener);
//
//        // 计算两点间的距离
//        float[] distanceAn = new float[3];
//        Location.distanceBetween(39.948973, 116.373044, 39.949029, 116.394011, distanceAn);
//
//        Log.e("lxf-dis", "distanceAn 0 == " + distanceAn[0]);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGpsStatus();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    /**
     * 位置更新（注册位置更新回调监听）
     */
    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
//            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
//        locationMgr.removeGpsStatusListener(listener);
    }

    /**
     * 停止位置更新
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapSettings(googleMap);

        modifyGoogleMapDisplayStyle(googleMap);

        // addMarkerPoint();

//        addRoute();

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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            // 启用我的位置图层
            mMap.setMyLocationEnabled(true);
            // 启动我的位置定位按钮
            mMapUiSettings.setMyLocationButtonEnabled(true);
            mMap.setOnMyLocationButtonClickListener(this);
            mMap.setOnMyLocationClickListener(this);
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
//        rectOptions.add(new LatLng(39.884108, 116.314864));
//        rectOptions.add(new LatLng(39.831838, 116.276422)); // North of the previous point, but at the same longitude
//        rectOptions.add(new LatLng(39.383446, 116.431908));// Same latitude, and 30km to the west
//        rectOptions.add(new LatLng(23.857996, 117.409341));// Same longitude, and 16km to the south
//        rectOptions.add(new LatLng(22.819203, 120.544794)); // Closes the polyline.


//        PolylineOptions rectOptions = new PolylineOptions();
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

        polyline.setColor(Color.parseColor("#0729EA"));

        LatLng bj = latLngs.get(latLngs.size() - 1);
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(bj));
        // 缩放级别范围2-21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bj, 17));

//        mMap.addMarker(new MarkerOptions().position(bj).title("北京"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(bj, 15));
    }

    /**
     * 修改google地图显示样式：https://mapstyle.withgoogle.com/
     *
     * @param googleMap
     */
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

    /**
     * 显示本机位置（地图中的小蓝点）
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult = mFusedLocationClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            if (mLastKnownLocation != null) {
                                String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                editor.putString("location oncomplete" + format, mLastKnownLocation.getLatitude() + ";" + mLastKnownLocation.getLongitude());
                                editor.commit();

                                routeLists.add(new LatLng(mLastKnownLocation.getLatitude(),
                                        mLastKnownLocation.getLongitude()));
                                // 将地图移动到当前所在位置
//                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
//                                        new LatLng(mLastKnownLocation.getLatitude(),
//                                                mLastKnownLocation.getLongitude()), 18f));
                                showInfoToTextView("1 1 当前位置  :" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + " && 点总数：" + routeLists.size(), false);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude())).title("当前位置"));
                                PolylineOptions rectOptions = new PolylineOptions();
                                rectOptions.addAll(routeLists);

                                Polyline polyline = mMap.addPolyline(rectOptions);
                                // 珊瑚
                                polyline.setColor(Color.parseColor("#FF7F50"));

                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        routeLists.get(routeLists.size() - 1), 18f));
                            } else {
                                Log.d(TAG, "mLastKnownLocation is null.");
                            }
                        } else {
                            showInfoToTextView("定位失败 ：" + " && 点总数：" + routeLists.size(), false);
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
     * 获取Gps状态（GPS信号）
     */
    private void getGpsStatus() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            listener = new GpsStatus.Listener() {
                @Override
                public void onGpsStatusChanged(int event) {
                    switch (event) {             //第一次定位
                        case GpsStatus.GPS_EVENT_FIRST_FIX:
                            Log.i("getGpsStatus", "第一次定位");
                            showInfoToTextView("第一次定位", false);
                            break;
                        // 卫星状态改变
                        case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                            Log.i("getGpsStatus", "卫星状态改变");
//                            showInfoToTextView("卫星状态改变", false);
                            // 获取当前状态 
                            if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                return;
                            }
                            GpsStatus gpsStatus = locationMgr.getGpsStatus(null);
                            // 获取卫星颗数的默认最大值 
                            int maxSatellites = gpsStatus.getMaxSatellites();
                            // 创建一个迭代器保存所有卫星  
                            Iterator<GpsSatellite> iters = gpsStatus.getSatellites().iterator();
                            int count = 0;
                            while (iters.hasNext() && count <= maxSatellites) {
                                GpsSatellite s = iters.next();
                                count++;
                                float snr = s.getSnr();
                                Log.i("getGpsStatus", "onGpsStatusChanged: 信噪比 :" + snr);
//                                showInfoToTextView("信噪比 : " + snr, false);
                                if (snr > 30) {
                                    count++;
                                    if (count >= 4) {
                                        // 表示有信号
                                        Log.i("getGpsStatus", "onGpsStatusChanged: 表示有信号");
//                                        showInfoToTextView("表示有信号", false);
                                    } else {
                                        // 信号弱或无信号
                                        Log.i("getGpsStatus", "onGpsStatusChanged: 信号弱或无信号");
//                                        showInfoToTextView("信号弱或无信号", false);
                                    }
                                }
                            }
                            Log.i("getGpsStatus", "搜索到 :" + count + "颗卫星  max : " + maxSatellites);
//                            showInfoToTextView("搜索到 :" + count + "颗卫星", false);
                            break;// 定位启动 
                        case GpsStatus.GPS_EVENT_STARTED:
                            Log.i("getGpsStatus", "定位启动");
                            showInfoToTextView("定位启动", false);
                            break;
                        //定位结束 
                        case GpsStatus.GPS_EVENT_STOPPED:
                            Log.i("getGpsStatus", "定位结束");
                            showInfoToTextView("定位结束", false);
                            break;
                        default:
                            break;
                    }
                }

            };
            locationMgr.addGpsStatusListener(listener);
        }
    }

    private void showInfoToTextView(String s, boolean isSignificance) {
        String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS").format(new Date());
        if (isSignificance) {
            info.append("★  " + format + "  " + s + "\n");
        } else {
            info.append(format + "  " + s + "\n");
        }
        tvGpsInfo.setText(info);
        int offset = tvGpsInfo.getLineCount() * tvGpsInfo.getLineHeight();
        if (offset > tvGpsInfo.getHeight()) {
            tvGpsInfo.scrollTo(0, offset - tvGpsInfo.getHeight() + 20);
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
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    private LocationListener locationListener = new LocationListener() {
        /**
         * 当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
         * @param location
         */
        @Override
        public void onLocationChanged(Location location) {
            if (location != null) {
                Log.i("SuperMap", "Location changed : Lat: "
                        + location.getLatitude() + " Lng: "
                        + location.getLongitude());

                // 此位置的高度
                double altitude = location.getAltitude();
                // 获取纬度
                double latitude = location.getLatitude();
                // 获取经度
                double longitude = location.getLongitude();
                // 返回生成此修复程序的提供程序的名称。
                String provider = location.getProvider();
                // 获得速度
                float speed = location.getSpeed();
                // 返回此修复的UTC时间，以1970年1月1日以来的毫秒数为单位。
                long time = location.getTime();
                MyLocationBean myLocationBean = new MyLocationBean(altitude, latitude, longitude, provider, speed, time);
                String data = myLocationBean.toString();
                String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                editor.putString(format, data);
                editor.commit();

//                showInfoToTextView("2 22 当前位置  :" + mLastKnownLocation.getLatitude() + "," + mLastKnownLocation.getLongitude() + " && 点总数：" + routeLists.size(), false);
                routeLists.add(new LatLng(location.getLatitude(),
                        location.getLongitude()));
                PolylineOptions rectOptions = new PolylineOptions();
                rectOptions.addAll(routeLists);

                Polyline polyline = mMap.addPolyline(rectOptions);
                // 棕色
                polyline.setColor(Color.parseColor("#A52A2A"));

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        routeLists.get(routeLists.size() - 1), 18f));
            }
        }

        /**
         * Provider的转态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
         * @param provider
         * @param status
         * @param extras
         */
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        /**
         *Provider被enable时触发此函数，比如GPS被打开
         * @param provider
         */
        @Override
        public void onProviderEnabled(String provider) {

        }

        /**
         * Provider被disable时触发此函数，比如GPS被关闭
         * @param provider
         */
        @Override
        public void onProviderDisabled(String provider) {

        }
    };
}
