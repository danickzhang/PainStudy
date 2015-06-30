package edu.missouri.niaaa.pain.activity;

import java.util.Calendar;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;

public class SuspensionTimePicker extends Activity {
    String TAG = "SuspensionTimePicker.java";
    
//  String[] display = {"  15 minutes  ","  30 minutes  ","  45 minutes  ","  60 minutes  ","  1 hour & 15 minutes  ","  1 & half hour  ","  1 hour & 45 minutes  ","  2 hours  "};
    int selection = 0;
    int interval = Util.SUSPENSION_INTERVAL_IN_SECONDS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_suspension_picker);

        NumberPicker np = (NumberPicker) findViewById(R.id.suspension_picker);
        Button setPicker = (Button) findViewById(R.id.btnSuspension);
        Button backButton = (Button) findViewById(R.id.btnReturn);

        np.setMinValue(0);
        np.setMaxValue(Util.SUSPENSION_DISPLAY.length-1);
        np.setDisplayedValues(Util.SUSPENSION_DISPLAY);

        np.setOnValueChangedListener(new OnValueChangeListener(){

            @Override
            public void onValueChange(NumberPicker picker, int oldValue, int newValue) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "selection is "+selection+" and item is "+Util.SUSPENSION_DISPLAY[selection]);

                selection = newValue;
            }});

        setPicker.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Calendar c = Calendar.getInstance();
                long time = c.getTimeInMillis();
                
                Util.scheduleSuspension(SuspensionTimePicker.this, time + (selection + 1) * Util.SUSPENSION_INTERVAL_IN_SECONDS * 1000);
                
                //close volume
                AudioManager audiom = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
                audiom.setStreamVolume(AudioManager.STREAM_MUSIC, 3, AudioManager.FLAG_PLAY_SOUND);

                //set result and finish
                setResult(Activity.RESULT_OK);// set text to break suspension
                finish();
            }
        });

        backButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
