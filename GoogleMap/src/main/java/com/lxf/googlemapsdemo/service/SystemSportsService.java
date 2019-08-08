package com.lxf.googlemapsdemo.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.lxf.googlemapsdemo.listener.SportsUpdateUICallBack;
import com.lxf.googlemapsdemo.utils.Utils;

import java.util.Iterator;

/**
 * @author: lixiaofei
 * @date: 2019/8/7
 * @version: 1.0.0
 */
public class SystemSportsService extends Service {
    private static final String TAG = "lxf-SystemSportsService";
    private SportsUpdateUICallBack mCallback;
    private LocationManager locationManager;
    private GpsStatus.Listener satelliteSignalListener;
    private SharedPreferences.Editor editor;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new SystemSportsBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        boolean hasPermission = checkMotionPermission();
        if (hasPermission) {
            addLocationUpdates();
            addGPSListener();
        }
    }

    /**
     * gps信号变化监听
     */
    private void addGPSListener() {
        if (satelliteSignalListener == null) {
            satelliteSignalListener = new GpsStatus.Listener() {
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
                                if (mCallback != null) {
                                    Log.e(TAG, "oooooooooooooooooooooooooo");
                                    mCallback.refreshGpsSignal(0);
                                    saveInforToSp("GPS信号：高" + Utils.getCurrentTime());
                                } else {
                                    Log.e(TAG, "zzzzzzzzzzzzzzzzzzzzzzz");
                                }
                            } else if (count <= 20 && count > 10) {
                                // 中
                                if (mCallback != null) {
                                    Log.e(TAG, "eeeeeeeeeeeeeeeeeeeeeeeeeeeeee");
                                    mCallback.refreshGpsSignal(1);
                                    saveInforToSp("GPS信号：中" + Utils.getCurrentTime());
                                } else {
                                    Log.e(TAG, "aaaaaaaaaaaaaaaaaaaaaaaa");
                                }
                            } else if (count <= 10 && count > 5) {
                                // 低
                                if (mCallback != null) {
                                    Log.e(TAG, "rrrrrrrrrrrrrrrrrrrrrrrrrrrrrr");
                                    mCallback.refreshGpsSignal(2);
                                    saveInforToSp("GPS信号：低" + Utils.getCurrentTime());
                                } else {
                                    Log.e(TAG, "ssssssssssssssssssssssssss");
                                }
                            } else {
                                // 小于5颗卫星，无法定位
                                if (mCallback != null) {
                                    Log.e(TAG, "tttttttttttttttttttttttt");
                                    mCallback.refreshGpsSignal(3);
                                    saveInforToSp("GPS信号：无" + Utils.getCurrentTime());
                                } else {
                                    Log.e(TAG, "dddddddddddddddddddddd");
                                }
                            }
                            Log.e(TAG, "搜索到 :" + count + "颗卫星  max : " + maxSatellites);
                            break;
                        case GpsStatus.GPS_EVENT_STARTED:
                            // 定位启动 
                            Log.e(TAG, "定位启动");
                            saveInforToSp("定位启动" + Utils.getCurrentTime());
                            break;
                        case GpsStatus.GPS_EVENT_STOPPED:
                            //定位结束 
                            Log.e(TAG, "定位结束");
                            saveInforToSp("定位结束" + Utils.getCurrentTime());
                            break;
                        default:
                            break;
                    }
                }
            };
        }
        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.addGpsStatusListener(satelliteSignalListener);
    }

    /**
     * 位置变化监听添加
     */
    private void addLocationUpdates() {
        if (locationManager == null) {
            locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        }
        /**
         * 添加位置变化监听
         * 设置最小时间 10秒
         */
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * 10, 1, locationListener);
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
                if (mCallback != null) {
                    mCallback.refreshLocation(location);
                } else {
                    Log.e(TAG, "vvvvvvvvvvvvvvvvvvvvvvvv");
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
        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled provider == " + provider);
            saveInforToSp("GPS位置变化监听：" + "onProviderDisabled provider == " + provider + "-" + Utils.getCurrentTime());
        }
    };


    public class SystemSportsBinder extends Binder {
        /**
         * 获取当前service对象
         *
         * @return StepService
         */
        public SystemSportsService getService() {
            return SystemSportsService.this;
        }
    }

    /**
     * 注册UI更新监听
     *
     * @param updateUiCallBack
     */
    public void registerUiUpadteCallback(SportsUpdateUICallBack updateUiCallBack) {
        Log.e(TAG, "lllllllllllllllllllllllllllll");
        this.mCallback = updateUiCallBack;
    }

    /**
     * 检查定位权限，读写权限是否存在
     *
     * @return
     */
    private boolean checkMotionPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void saveInforToSp(String infor) {
        if (editor == null) {
            SharedPreferences sharedPreferences = getSharedPreferences(TAG, Context.MODE_PRIVATE);
            editor = sharedPreferences.edit();
        }
        editor.putString(TAG, infor);
        editor.commit();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (editor != null) {
            editor.clear();
            editor.commit();
        }

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
