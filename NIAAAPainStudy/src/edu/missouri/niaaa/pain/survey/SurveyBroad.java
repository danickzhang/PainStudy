package edu.missouri.niaaa.pain.survey;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import edu.missouri.niaaa.pain.Util;

public class SurveyBroad extends BroadcastReceiver {
    String TAG = "SurveyBroad.java";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "OnReceive~~~ "+intent.getAction()+" "+intent.getIntExtra(Util.SV_TYPE, -1));
        Util.Log_lifeCycle(TAG, "~~~seq is "+intent.getIntExtra(Util.SV_SEQ, -1));

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String action = intent.getAction();

        int surveyType = intent.getIntExtra(Util.SV_TYPE, -1);//protect -1 later
        int surveySeq = intent.getIntExtra(Util.SV_SEQ, -1);//protect -1 later

        if(action.equals(Util.BD_ACTION_SURVEY_TRIGGER)){
            Util.Log_debug(TAG, "action~~~ "+action);
            Calendar c = Calendar.getInstance();
            long time = c.getTimeInMillis()+500;
            long unit = 10000;
            for(int i=1; i<=4; i++){
                int seq = i%4;
                Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_REMINDS);
                itTrigger.putExtra(Util.SV_TYPE, surveyType);
                itTrigger.putExtra(Util.SV_SEQ, surveySeq);
                itTrigger.putExtra(Util.SV_REMIND_SEQ, seq);

                PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, Intent.FLAG_ACTIVITY_NEW_TASK);
                am.setExact(AlarmManager.RTC_WAKEUP, time + (i-1)*unit, piTrigger);

                /*it looks like wait a little bit of time is good*/
                Log.d(TAG, "---i "+seq+" "+time + (i-1)*unit);
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    // TODO Auto-generated catch block
//                    e.printStackTrace();
//                }
            }
        }
        else if(action.equals(Util.BD_ACTION_SURVEY_REMINDS)){
            int remind_seq = intent.getIntExtra(Util.SV_REMIND_SEQ, -1);//protect -1 later
            Util.Log_debug(TAG, "action~~~ "+action+" "+intent.getIntExtra(Util.SV_SEQ, -1)+" "+intent.getIntExtra(Util.SV_REMIND_SEQ, -1));

            Intent launchSurvey = new Intent(context, SurveyAct.class);
            launchSurvey.putExtra(Util.SV_TYPE, surveyType);
            launchSurvey.putExtra(Util.SV_SEQ, surveySeq);
            launchSurvey.putExtra(Util.SV_REMIND_SEQ, remind_seq);

            launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(launchSurvey);
        }

    }

}
