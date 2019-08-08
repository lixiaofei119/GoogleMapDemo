package com.lxf.googlemapsdemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
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
import com.lxf.googlemapsdemo.listener.SportsUpdateUICallBack;
import com.lxf.googlemapsdemo.service.SystemSportsService;
import com.lxf.googlemapsdemo.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SystemLocationWithServiceActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMyLocationButtonClickListener, GoogleMap.OnMyLocationClickListener {
    private static final String TAG = "lxf-SystemLocationWithServiceActivity";
    private GoogleMap mMap;
    private TextView tvStatus;
    private FusedLocationProviderClient mFusedLocationClient;
    private ArrayList<LatLng> routeLists = new ArrayList<>();
    private SharedPreferences.Editor editor;
    /**
     * 地图上的轨迹线
     */
    private PolylineOptions rectOptions = new PolylineOptions();
    private boolean isBind;
    private SystemSportsService sportsService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_location_with_service);
        tvStatus = findViewById(R.id.tv_status);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_system_service);
        // 地图的初始化
        mapFragment.getMapAsync(this);

        startService();

    }

    /**
     * 启动服务计步
     */
    @SuppressLint("LongLogTag")
    private void startService() {
        try {
            Intent intent = new Intent(this, SystemSportsService.class);
            isBind = this.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            Log.e(TAG, "isBind == " + isBind);
//            this.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        googleMapSettings(googleMap);

        modifyGoogleMapDisplayStyle(googleMap);

        getDeviceLocation();
    }

    /**
     * 用于查询应用服务（application Service）的状态的一种interface，
     * 更详细的信息可以参考Service 和 context.bindService()中的描述，
     * 和许多来自系统的回调方式一样，ServiceConnection的方法都是进程的主线程中调用的。
     */
    private ServiceConnection serviceConnection = new ServiceConnection() {
        /**
         * 在建立起于Service的连接时会调用该方法，目前Android是通过IBind机制实现与服务的连接。
         * @param name 实际所连接到的Service组件名称
         * @param service 服务的通信信道的IBind，可以通过Service访问对应服务
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (service instanceof SystemSportsService.SystemSportsBinder) {
                sportsService = ((SystemSportsService.SystemSportsBinder) service).getService();
                sportsService.registerUiUpadteCallback(new SportsUpdateUICallBack() {
                    @SuppressLint("LongLogTag")
                    @Override
                    public void refreshGpsSignal(int gradle) {
                        Log.e(TAG,"1qazzzzzzzz");
                        switch (gradle) {
                            case 0:
                                tvStatus.setText("GPS信号：高");
                                break;
                            case 1:
                                tvStatus.setText("GPS信号：中");
                                break;
                            case 2:
                                tvStatus.setText("GPS信号：低");
                                break;
                            case 3:
                                tvStatus.setText("GPS信号：无");
                                break;
                            default:
                                break;
                        }
                    }

                    @Override
                    public void refreshLocation(Location location) {
                        addRouteLines(location);
                    }
                });

            }
        }

        /**
         * 当与Service之间的连接丢失的时候会调用该方法，
         * 这种情况经常发生在Service所在的进程崩溃或者被Kill的时候调用，
         * 此方法不会移除与Service的连接，当服务重新启动的时候仍然会调用 onServiceConnected()。
         * @param name 丢失连接的组件名称
         */
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    /**
     * 画地图线
     *
     * @param location
     */
    @SuppressLint("LongLogTag")
    private void addRouteLines(Location location) {
        Log.e(TAG, "刷新位置点");
        if (location != null) {
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
                polyline.setColor(ContextCompat.getColor(SystemLocationWithServiceActivity.this, R.color.color_ff9d0a));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
            }
        }
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
                                polyline.setColor(ContextCompat.getColor(SystemLocationWithServiceActivity.this, R.color.color_ff9d0a));
                                // 将地图移动到当前所在位置
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        routeLists.get(routeLists.size() - 1), 18f));
                            } else {
                                Log.d(TAG, "mLastKnownLocation is null.");
                                Toast.makeText(SystemLocationWithServiceActivity.this, "定位成功，位置信息返回null", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SystemLocationWithServiceActivity.this, "定位失败", Toast.LENGTH_SHORT).show();
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

    private void saveInforToSp(String infor) {
        if (editor == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(this.getLocalClassName(), Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        editor.putString(this.getLocalClassName(), infor);
        editor.commit();
    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (editor != null) {
            editor.clear();
            editor.commit();
        }
        this.unbindService(serviceConnection);
    }
}
