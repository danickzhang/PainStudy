package edu.missouri.niaaa.pain;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import edu.missouri.niaaa.pain.activity.DialogActivity;

/**
 * @author Chen
 *
 * @params
 *
 */
public class DaemonReceiver extends BroadcastReceiver {

    final static String TAG = "DaemonReceiver.java";
    boolean logEnable = true;

    public static final String BD_ACTION_DAEMON_FUNC    = "Intent_Daemon";

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        Util.Log_debug(TAG, "on receiver daemon");

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        int fun = intent.getIntExtra(BD_ACTION_DAEMON_FUNC, 0);

        String ID = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERID, "");
        String PWD = Util.getSP(context, Util.SP_LOGIN).getString(Util.SP_LOGIN_KEY_USERPWD, "");

        if(ID.equals("") || PWD.equals("")){
            //bypass daemon if no id and pwd assigned
            Util.Log_debug(TAG, "No ID or PWD, daemon bypassed");

        }
        else if(fun == 0){//set all daemon alarms
            Util.Log_debug(TAG, "on receiver daemon 0");

            //Noon
            setAlarm(context, am, 1, getProperTime(12, 20));

            //Midnight
            setAlarm(context, am, 2, getProperTime(23, 59));

            //Three oclock
            setAlarm(context, am, 3, getProperTime(3, 0));

            //9pm charging reminder
//            setAlarm(context, am, 4, getProperTime(20, 0));
//            cancel reminder charging alarm

        }
        else if(fun == 1){//Noon
            Util.Log_debug(TAG, "on receiver daemon 1");

            //today at noon
            Util.morningComplete(context, true, true);

            Toast.makeText(context, "Noon daemon trigger random schedules", Toast.LENGTH_LONG).show();

            //Reset Noon
            setAlarm(context, am, 1, getProperTime(12, 20));
            Util.debugDT("noon", getProperTime(12, 20));
        }
        else if(fun == -1){//cancel noon
            Util.Log_debug(TAG, "on receiver daemon -1");

            //Reset Noon
            setAlarm(context, am, 1, getProperTime(12, 20));
            Util.debugDT("cancel noon", getProperTime(12, 20));
        }
        else if(fun == 2){//Midnight
            Util.Log_debug(TAG, "on receiver daemon 2");

            //close sensor
//            SensorUtilities.closeSensor(context);
//            Intent i = new Intent(SensorUtilities.ACTION_DISCONNECT_SENSOR);
//            context.sendBroadcast(i);

            //cancel all survey except followups (which means only need to cancel randoms, but randoms are not able to reach after midnight, so just remove the random_sets)
            //modify: do not remove random_sets, because this will allow isActivate() = true, so that still allow initial drinking
//            Util.deActivate(context);
//            Util.cancelRandomSurvey(context);//##??

            Toast.makeText(context, "MIDNIGHT close sensor and cancel survey", Toast.LENGTH_LONG).show();

            //Reset Midnight
            setAlarm(context, am, 2, getProperTime(23, 59));
        }
        else if(fun == 3){//three o'clock
            Util.Log_debug(TAG, "on receiver daemon 3");

            //close location
            Util.stopRecordingLocation(context);

            //for morning
            Util.rescheduleMorningSurvey(context);

            //also remove random_sets
            Util.deActivate(context);
            //cancel followup
//            Util.cancelFollowups(context, 4);
//            Util.cancelFollowups(context, 6);
//            Util.cancelFollowups(context, 7);

            Toast.makeText(context, "THREE'O close gps & cancel followups", Toast.LENGTH_LONG).show();

            //Reset Three o'clock
            setAlarm(context, am, 3, getProperTime(3, 0));

            //reset all daemon, send 0 broadcast?? but currently works fine

        }
        else if(fun == -3){//cancel three o'clock //useless for now
            Util.Log_debug(TAG, "on receiver daemon -3");

        }
        else if (fun == 4) {// 9pm alarm dialog
            Intent dialogIntent = new Intent(context, DialogActivity.class);
            dialogIntent.putExtra(DialogActivity.DIALOG_FLAG, DialogActivity.DIALOG_CHARGE_REMIND);

            dialogIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            context.startActivity(dialogIntent);

            Toast.makeText(context, "Reseting the 9pm reminder for tomorrow", Toast.LENGTH_LONG).show();

//            setAlarm(context, am, 4, getProperTime(20, 0));
            //cancel reminder charging alarm
            
//            Intent itTrigger4 = new Intent(Util.BD_ACTION_DAEMON);
//            itTrigger4.putExtra(BD_ACTION_DAEMON_FUNC, 4);// int
//            PendingIntent piTrigger4 = PendingIntent.getBroadcast(context, 4, itTrigger4, PendingIntent.FLAG_CANCEL_CURRENT);
//
//            Util.setAlarmExact(am, getProperTime(20, 0), piTrigger4);
        }
        else{
            Util.Log_debug(TAG, "on receiver daemon else");
        }

    }

    private void setAlarm(Context context, AlarmManager am, int num, long time){
        Intent itTrigger = new Intent(Util.BD_ACTION_DAEMON);
        itTrigger.putExtra(BD_ACTION_DAEMON_FUNC, num);// int
        PendingIntent piTrigger = PendingIntent.getBroadcast(context, num, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

        Util.setAlarmExact(am, time, piTrigger);
    }

    private long getProperTime(int hour, int minute){
        Calendar c = Calendar.getInstance();
        Calendar s = Calendar.getInstance();
        s.set(Calendar.HOUR_OF_DAY, hour);
        s.set(Calendar.MINUTE, minute);
        s.set(Calendar.SECOND, 0);
        s.set(Calendar.MILLISECOND, 0);
        if(c.after(s)){
            return s.getTimeInMillis() + getDayLong();
        }
        return s.getTimeInMillis();
    }

    public static long getDayLong(){
        return 24*60*60*1000;
    }
}
