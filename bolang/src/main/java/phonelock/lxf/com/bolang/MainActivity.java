package phonelock.lxf.com.bolang;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.SeekBar;

public class MainActivity extends AppCompatActivity {
    private MyWaveView myview;
    private MyWaveView2 myview2;
    private SeekBar seekbar;
    private SeekBar seekbarspeed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initEvent();
    }

    private void initEvent() {

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("TAG", "height == " + progress);
                myview.setWaveHeight(progress);
                myview2.setWaveHeight(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekbarspeed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("TAG", "speed == " + progress);
                myview.setWaveSpeed(progress);
                myview2.setWaveSpeed(progress - 40);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }

    private void initView() {
        myview = (MyWaveView) findViewById(R.id.myview);
        myview2 = (MyWaveView2) findViewById(R.id.myview2);
        seekbar = (SeekBar) findViewById(R.id.seekbar);
        seekbarspeed = (SeekBar) findViewById(R.id.seekbarspeed);

        seekbar.setProgress(myview.getWaveHeight());
        seekbarspeed.setProgress(myview.getWaveSpeed());
    }
}
