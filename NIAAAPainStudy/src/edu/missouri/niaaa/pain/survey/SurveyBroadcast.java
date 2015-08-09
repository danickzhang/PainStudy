package edu.missouri.niaaa.pain.survey;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
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

        //acquire wl for a while
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
//      WakeLock wl = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "SurveyBroadcast");
        WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "SurveyBroadcast");
        wl.acquire(1*60*1000);

        //restart gps
        Util.restartRecordingLocation(context);

        String action = intent.getAction();

        int surveyType = intent.getIntExtra(Util.SV_TYPE, -1);//protect -1 later
        int surveySeq = intent.getIntExtra(Util.SV_SEQ, -1);//protect -1 later

        String ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
        String PWD = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERPWD, "");

        if(ID.equals("") || PWD.equals("")){
            //bypass daemon if no id and pwd assigned
            Util.Log_debug(TAG, "No ID or PWD, daemon bypassed");

        }
        else if(action.equals(Util.BD_ACTION_SURVEY_TRIGGER_RANDOM) || action.equals(Util.BD_ACTION_SURVEY_TRIGGER_FOLLOW)){
            Util.Log_debug(TAG, "action~~~ "+action);

            //if morning, check if activate today, or bypass

            Util.scheduleSurveyReminders(context, surveyType, surveySeq);

        }
        else if(action.equals(Util.BD_ACTION_SURVEY_REMINDS_RANDOM) || action.equals(Util.BD_ACTION_SURVEY_REMINDS_FOLLOW)){
            int remindSeq = intent.getIntExtra(Util.SV_REMIND_SEQ, -1);//protect -1 later
            Util.Log_debug(TAG, "action~~~ "+action+" "+intent.getIntExtra(Util.SV_SEQ, -1)+" "+intent.getIntExtra(Util.SV_REMIND_SEQ, -1));

            Intent launchSurvey = new Intent(context, SurveyActivity.class);
            launchSurvey.putExtra(Util.SV_TYPE, surveyType);
            launchSurvey.putExtra(Util.SV_SEQ, surveySeq);
            launchSurvey.putExtra(Util.SV_REMIND_SEQ, remindSeq);

            launchSurvey.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            launchSurvey.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            
            //end cycle
            if((surveyType == Util.SV_TYPE_DRINKING_FOLLOWUP || surveyType == Util.SV_TYPE_PAIN_FOLLOWUP || surveyType == Util.SV_TYPE_DUAL_FOLLOWUP) && surveySeq == Util.getExistingFollowupSchedules(context).split(",").length){
                Util.Log_debug(TAG, "end cycle~~~ "+surveySeq+" "+remindSeq);
                launchSurvey.putExtra(Util.SV_END_CYCLE, true);
                
                if(remindSeq == SurveyActivity.REMIND_LASTONE){
                    //end cycle 2/2
                    Util.Log_debug(TAG, "cancel cycle~~~2/2 "+surveySeq+" "+remindSeq);
                    Util.removeCycleFlag(context);
                }
            }

            if((Util.isSuspensionFlag(context))// && surveyType != Util.SV_TYPE_DRINKING_FOLLOWUP ) //pain study wants followups being suspended 
                    || (Util.isInCycle(context) && (surveyType != Util.SV_TYPE_DRINKING_FOLLOWUP && surveyType != Util.SV_TYPE_PAIN_FOLLOWUP && surveyType != Util.SV_TYPE_DUAL_FOLLOWUP)) 
                    || Util.isIsolateFlag(context) && remindSeq != SurveyActivity.REMIND_TIMEOUT
                    ){
                //bypass last remind
                if(remindSeq == SurveyActivity.REMIND_LASTONE){//##??
                    return;
                }
                
                //distinguish
                String reason = "";
                String subCode = "";
                if(Util.isSuspensionFlag(context)){
                    reason = "Suspension";
                    subCode = "_1";
                }
                else if(Util.isInCycle(context)){
                    reason = "Followup Cycle";
                    subCode = "_2";
                }
                else{//Util.isIsolateFlag(context)
                    reason = "Survey Isolator";
                    subCode = "_3";
                }
                
                //under 
                Util.Log_debug(TAG, "### write event, noPrompt_under "+ reason +" ->  survey: "+surveyType+" seq: "+surveySeq+" remind: "+remindSeq);

                //write
                Util.writeEvent(context, surveyType, Util.CODE_SV_NO_PROMPT + subCode, surveySeq,
                        Util.getSurveyScheduleDT(context, surveyType, surveySeq),
                        Util.getSurveyAlarmDT(Calendar.getInstance(),remindSeq),
                        "", Util.dtF.format(Calendar.getInstance().getTime()));

                Toast.makeText(context, "An auto-triggered survey is just blocked by "+reason+"!", Toast.LENGTH_LONG).show();
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
