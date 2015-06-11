package edu.missouri.niaaa.pain.survey;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import edu.missouri.niaaa.pain.Util;
import edu.missouri.niaaa.pain.Utilities;

public class SurveyBroad extends BroadcastReceiver {
    String TAG = "SurveyBroad.java";
    
    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "OnReceive~~~ "+intent.getAction()+" "+intent.getIntExtra(Utilities.SV_NAME, -1));
        Util.Log_lifeCycle(TAG, "~~~seq is "+intent.getIntExtra(Utilities.SV_NAME, -1));
        
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String action = intent.getAction();
        
        if(action.equals(Util.BD_ACTION_SURVEY_TRIGGER)){
            Util.Log_debug(TAG, "action~~~ "+action);
            Calendar c = Calendar.getInstance();
            long time = c.getTimeInMillis();
            long unit = 5000;
            for(int i=0; i<3; i++){
                Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_REMINDS);
                itTrigger.putExtra(Utilities.SV_NAME, i);
                PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);
                am.setExact(AlarmManager.RTC_WAKEUP, time+i*unit, piTrigger);
            }
        }
        else if(action.equals(Util.BD_ACTION_SURVEY_REMINDS)){
            Intent launchSurvey = new Intent(context, SurveyActivity.class);
//          launchSurvey.putExtra(Utilitieslities.SV_FILE, Utilitieslities.SV_MAP.get(surveyName));
            launchSurvey.putExtra(Utilities.SV_NAME, 1);
            launchSurvey.putExtra(Utilities.SV_AUTO_TRIGGERED, true);
            launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(launchSurvey);
        }
        
    }
    
}
