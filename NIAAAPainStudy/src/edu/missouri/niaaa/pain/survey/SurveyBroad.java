package edu.missouri.niaaa.pain.survey;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.Utilities;

public class SurveyBroad extends BroadcastReceiver {
    String TAG = "SurveyBroad.java";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "OnReceive~~~ "+intent.getAction()+" "+intent.getStringExtra(Util.SV_NAME));
        Util.Log_lifeCycle(TAG, "~~~seq is "+intent.getIntExtra(Util.SV_SEQ, -1));
        
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String action = intent.getAction();
        
        String surveyName = intent.getStringExtra(Util.SV_NAME);
        int surveySeq = intent.getIntExtra(Util.SV_SEQ, -1);
        
        if(action.equals(Util.BD_ACTION_SURVEY_TRIGGER)){
            Util.Log_debug(TAG, "action~~~ "+action);
            Calendar c = Calendar.getInstance();
            long time = c.getTimeInMillis();
            long unit = 5000;
            for(int i=1; i<=4; i++){
                int seq = i%4;
                Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_REMINDS);
                itTrigger.putExtra(Util.SV_NAME, surveyName);
                itTrigger.putExtra(Util.SV_SEQ, seq);
                PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);
                am.setExact(AlarmManager.RTC_WAKEUP, time + (i-1)*unit, piTrigger);
                
                Log.d(TAG, "---i "+time + (i-1)*unit);
            }
        }
        else if(action.equals(Util.BD_ACTION_SURVEY_REMINDS)){
            Intent launchSurvey = new Intent(context, SurveyActivity.class);
            launchSurvey.putExtra(Utilities.SV_NAME, surveyName);
            launchSurvey.putExtra(Util.SV_SEQ, surveySeq);
            launchSurvey.putExtra(Utilities.SV_AUTO_TRIGGERED, true);
            launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//            context.startActivity(launchSurvey);
        }
        
    }
    
}
