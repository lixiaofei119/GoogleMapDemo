package com.lxf.googlemapsdemo.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lxf.googlemapsdemo.R;

public class MainActivity extends AppCompatActivity {

    private Button btnSystemLocation;
    private Button btnSystemLocationService;
    private Button btnGoogleLocation;
    private Button btnGoogleLocationService;
    /**
     * 要申请的权限
     */
    private String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.WAKE_LOCK};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSystemLocation = findViewById(R.id.btn_system_location);
        btnSystemLocationService = findViewById(R.id.btn_system_location_service);
        btnGoogleLocation = findViewById(R.id.btn_google_location);
        btnGoogleLocationService = findViewById(R.id.btn_google_location_service);


        btnSystemLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent intent = new Intent(MainActivity.this, SystemLocationActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "请打开定位权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSystemLocationService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent intent = new Intent(MainActivity.this, SystemLocationWithServiceActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "请打开定位权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoogleLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent intent = new Intent(MainActivity.this, GoogleLocationActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "请打开定位权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnGoogleLocationService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission()) {
                    Intent intent = new Intent(MainActivity.this, GoogleLocationWithServiceActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "请打开定位权限", Toast.LENGTH_SHORT).show();
                }
            }
        });

        if (!checkPermission()) {
            ActivityCompat.requestPermissions(MainActivity.this, permissions, 1101);
        }
    }

    /**
     * 检查权限
     *
     * @return
     */
    private boolean checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1101:
                Log.e("TAG", "grantResults length == " + grantResults.length + ";grantResults==" + grantResults.toString());
                Log.e("TAG", "permissions == " + permissions.toString());
                break;
            default:
                break;
        }
    }
}
