package edu.missouri.niaaa.pain.survey;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import edu.missouri.niaaa.pain.R;
import edu.missouri.niaaa.pain.Util;

public class SurveyBroadcast extends BroadcastReceiver {
    String TAG = "SurveyBroad.java";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Util.Log_lifeCycle(TAG, "OnReceive~~~ "+intent.getAction()+" "+intent.getIntExtra(Util.SV_TYPE, -1));
        Util.Log_lifeCycle(TAG, "~~~seq is "+intent.getIntExtra(Util.SV_SEQ, -1));

//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        String action = intent.getAction();

        int surveyType = intent.getIntExtra(Util.SV_TYPE, -1);//protect -1 later
        int surveySeq = intent.getIntExtra(Util.SV_SEQ, -1);//protect -1 later

        if(action.equals(Util.BD_ACTION_SURVEY_TRIGGER)){
            Util.Log_debug(TAG, "action~~~ "+action);
            
            //if morning, check if activate today, or bypass
            
            Util.scheduleSurveyReminders(context, surveyType, surveySeq);
            
        }
        else if(action.equals(Util.BD_ACTION_SURVEY_REMINDS)){
            int remindSeq = intent.getIntExtra(Util.SV_REMIND_SEQ, -1);//protect -1 later
            Util.Log_debug(TAG, "action~~~ "+action+" "+intent.getIntExtra(Util.SV_SEQ, -1)+" "+intent.getIntExtra(Util.SV_REMIND_SEQ, -1));

            Intent launchSurvey = new Intent(context, SurveyActivity.class);
            launchSurvey.putExtra(Util.SV_TYPE, surveyType);
            launchSurvey.putExtra(Util.SV_SEQ, surveySeq);
            launchSurvey.putExtra(Util.SV_REMIND_SEQ, remindSeq);

            launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            if(Util.isSuspensionFlag(context)){
                //under suspension
                Util.Log_debug(TAG, "### write event, noPrompt_under Suspension ->  survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq);
                
                //write
                Util.writeEvent(context, Util.getSurveyCode(surveyType) + Util.CODE_SV_NO_PROMPT + "_1", 
                        "", 
                        "", "", "", 
                        "", Util.sdf.format(Calendar.getInstance().getTime()));
                
                Toast.makeText(context, "An auto-triggered survey is just blocked by suspension!", Toast.LENGTH_LONG).show();
            }
            else if(Util.isIsolateFlag(context)){
              //under survey isolater
                Util.Log_debug(TAG, "### write event, noPrompt_under survey isolater ->  survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq);
                
                //write
                Util.writeEvent(context, Util.getSurveyCode(surveyType) + Util.CODE_SV_NO_PROMPT + "_4", 
                        "", 
                        "", "", "", 
                        "", Util.sdf.format(Calendar.getInstance().getTime()));
                
                Toast.makeText(context, "An auto-triggered survey is just blocked by survey isolating!", Toast.LENGTH_LONG).show();
            }
            else{
                context.startActivity(launchSurvey);
            }
        }
        else if(action.equals(Util.BD_ACTION_SURVEY_ISOLATE)){
            Util.Log_debug(TAG, "action~~~ "+action);
            
            Util.cancelSurveyIsolater(context);
            
        }
        else if(action.equals(Util.BD_ACTION_SUSPENSION)){
            Util.Log_debug(TAG, "action~~~ "+action);
            
            
            //write break suspension ###
            Util.Log_debug(TAG, "### write break suspension");
            
            Util.cancelSuspension(context, false);
            
            Toast.makeText(context, R.string.suspension_end, Toast.LENGTH_LONG).show();
        }

    }

}
