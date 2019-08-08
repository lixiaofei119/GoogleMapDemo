package com.lxf.googlemapsdemo.listener;

import android.location.Location;

/**
 * @author: lixiaofei
 * @date: 2019/8/8
 * @version: 1.0.0
 */
public interface SportsUpdateUICallBack {
    /**
     * 刷新gps信号等级强度
     *
     * @param gradle 0-高；1-中；2-低；3-无
     */
    void refreshGpsSignal(int gradle);

    /**
     * 刷新位置点
     * @param location
     */
    void refreshLocation(Location location);
}
