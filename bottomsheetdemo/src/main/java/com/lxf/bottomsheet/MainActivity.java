package com.lxf.bottomsheet;

import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.TextView;
import android.widget.ViewSwitcher;

public class MainActivity extends AppCompatActivity {

    private CoordinatorLayout coordinatorLayout;
    private TextView tv_qwe;
    private NestedScrollView nestSc;
    private ViewSwitcher view_switcher;
    private TextView tv_green;
    private BottomSheetBehavior bottomSheetBehavior;
    private int peekHeight;

    private boolean isHasNavigationBar = false;
    private boolean isSetBottomSheetHeight;
    private int fraBottomSheetHeight;
    private boolean isHid = false;
    private int listBehaviorHeight = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        coordinatorLayout = findViewById(R.id.coordinatorLayout);
        tv_qwe = findViewById(R.id.tv_qwe);
        nestSc = findViewById(R.id.nest_sc);
        view_switcher = findViewById(R.id.view_switcher);
        tv_green = findViewById(R.id.tv_green);


        view_switcher.setDisplayedChild(1);

        bottomSheetBehavior = BottomSheetBehavior.from(nestSc);
        peekHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        bottomSheetBehavior.setPeekHeight(peekHeight);
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        try {
            getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                    //这个监听的方法时为了让有NavigationBar处理布局的变化的
                    if (isHasNavigationBar) {
                        boolean is = isNavigationBarShow();
                        if (isHid != is) {
                            CoordinatorLayout.LayoutParams linearParams = (CoordinatorLayout.LayoutParams) nestSc.getLayoutParams();
                            linearParams.height = coordinatorLayout.getHeight() - DensityUtil.dpToPx(90);
                            fraBottomSheetHeight = linearParams.height;
                            nestSc.setLayoutParams(linearParams);
                            isSetBottomSheetHeight = true;
                        }
                        isHid = is;
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    tv_qwe.setVisibility(View.GONE);

                } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                    tv_qwe.setVisibility(View.VISIBLE);

                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


        tv_qwe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isHasNavigationBar) {
                    CoordinatorLayout.LayoutParams linearParams = (CoordinatorLayout.LayoutParams) nestSc.getLayoutParams();
                    linearParams.height = coordinatorLayout.getHeight() - DensityUtil.dpToPx(90);
                    fraBottomSheetHeight = linearParams.height;
                    nestSc.setLayoutParams(linearParams);
                    isSetBottomSheetHeight = true;
                }
                bottomSheetBehavior.setPeekHeight((fraBottomSheetHeight - DensityUtil.dpToPx(45) > listBehaviorHeight) ? listBehaviorHeight : (int) (fraBottomSheetHeight / 2));
                nestSc.setVisibility(View.VISIBLE);
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });


        tv_green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }


    /**
     * 判断NavigationBar（就是虚拟返回键 home键）是否显示
     *
     * @return
     */
    public boolean isNavigationBarShow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            Display display = getWindowManager().getDefaultDisplay();
            Point size = new Point();
            Point realSize = new Point();
            display.getSize(size);
            display.getRealSize(realSize);
            return realSize.y != size.y;
        } else {
            boolean menu = ViewConfiguration.get(this).hasPermanentMenuKey();
            boolean back = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
            if (menu || back) {
                return false;
            } else {
                return true;
            }
        }
    }
}
