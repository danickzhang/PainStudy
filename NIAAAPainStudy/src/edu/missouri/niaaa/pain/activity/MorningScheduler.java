package edu.missouri.niaaa.pain.activity;

import java.util.Calendar;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.Utilities;

public class MorningScheduler extends Activity {

    String TAG = "MorningScheduler.java";

    TextView title;
    TextView timeText;
    CheckBox timeBox;
    TimePicker timePicker;
    Button setPicker;
    Button backButton;

    int hour = Utilities.defHour;
    int minute = Utilities.defMinute;

    SharedPreferences sp;
    Calendar bedtimeReportStartTS;
    
    public static final String INTENT_TS = "START_TIME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_morning_scheduler);
        bedtimeReportStartTS = Calendar.getInstance();

        sp = getSharedPreferences(Util.SP_BEDTIME, MODE_PRIVATE);
        boolean setDefault = (sp.getInt(Util.SP_BEDTIME_KEY_HOUR, -1) == -1?false:true);
        if(setDefault){
            hour = sp.getInt(Util.SP_BEDTIME_KEY_HOUR, -1);
            minute = sp.getInt(Util.SP_BEDTIME_KEY_MINUTE, -1);
        }

        title = (TextView) findViewById(R.id.morning_title);
        timeText = (TextView) findViewById(R.id.morning_text);
        timeBox = (CheckBox) findViewById(R.id.morning_box);
        timePicker = (TimePicker) findViewById(R.id.morning_picker);
        setPicker = (Button) findViewById(R.id.btnSchedule);
        backButton = (Button) findViewById(R.id.btnReturn);

        title.setText(bedtimeReportStartTS.get(Calendar.HOUR_OF_DAY)>3 ? R.string.morning_report_set_text1: R.string.morning_report_set_text2);
        timeText.setText(Utilities.getMorningTimeWithFlag(this));

        timeBox.setChecked(setDefault);
        timeBox.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
                // TODO Auto-generated method stub
                if(arg1){
                    Util.Log_debug(TAG, "checked");
                    timePicker.setEnabled(false);
                }
                else{
                    Util.Log_debug(TAG, "unchecked");
                    timePicker.setEnabled(true);
                }
            }});


        timePicker.setEnabled(!setDefault);
//      timePicker.setIs24HourView(true)
        timePicker.setCurrentHour(hour);
        timePicker.setCurrentMinute(minute);
        timePicker.setOnTimeChangedListener(new OnTimeChangedListener(){

            @Override
            public void onTimeChanged(TimePicker arg0, int arg1, int arg2) {
                // TODO Auto-generated method stub

                Utilities.Log(TAG, "on time changed listener");

                hour = arg1;
                minute = arg2;

            }});

        setPicker.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub

                Utilities.Log(TAG, ""+hour+":"+minute);

                if(hour >= 3 && hour <12 || (hour == 12 && minute == 0)){

                    saveDefault();
                    
                    Util.bedtimeComplete(MorningScheduler.this);
                    
                    Intent i = new Intent();
                    i.putExtra(INTENT_TS,bedtimeReportStartTS);
                    setResult(Activity.RESULT_OK, i);
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(),R.string.bedtime_alert,Toast.LENGTH_LONG).show();
                }
            }});

        backButton.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                finish();
            }});
    }

    private void saveDefault(){
        if(timeBox.isChecked()){
            sp.edit().putInt(Util.SP_BEDTIME_KEY_HOUR, hour).commit();
            sp.edit().putInt(Util.SP_BEDTIME_KEY_MINUTE, minute).commit();
        }
        else{
            sp.edit().putInt(Util.SP_BEDTIME_KEY_HOUR, -1).commit();
            sp.edit().putInt(Util.SP_BEDTIME_KEY_MINUTE, -1).commit();
        }
        
        Calendar c = Util.getProperMorningScheduleTime(hour, minute);
        sp.edit().putLong(Util.SP_BEDTIME_KEY_LONG, c.getTimeInMillis()).commit();
    }


    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

}
