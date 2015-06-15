package edu.missouri.niaaa.pain;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import org.xml.sax.InputSource;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import edu.missouri.niaaa.pain.survey.SurveyAct;
import edu.missouri.niaaa.pain.survey.parser.SurveyInfo;
import edu.missouri.niaaa.pain.survey.parser.XMLConfigParser;

public class Util {
    static String TAG = "Util.java";

    public static final String  ADMIN_UID = "0000";




    /*for debug*/
    public static final boolean DEBUG_LIFECYCLE = true;
    public static final boolean DEBUG           = true;
    public static final boolean RELEASE         = false;
    public static final boolean REMIND_SEPLIT   = true;


    /*survey config*/
    public static final int MAX_REMINDER = 3;
    public static final int MAX_TRIGGER_MORNING = 1;//1
    public static final int MAX_TRIGGER_RANDOM = 6;//6
    public static final int MAX_TRIGGER_FOLLOWUP = 3;//3
    public static final int VOLUME = 6;//10
    public static final String PHONE_BASE_PATH = "sdcard/TestResult_craving/";

    /*survey type*/
    public static final String SV_TYPE                      = "Survey_Type";
    public static final String SV_SEQ                       = "Survey_Seq";
    public static final String SV_REMIND_SEQ                = "Survey_Reminder_Seq";
    public static final String SV_MANUAL                    = "Survey_manual_triggered";

    public static final int SV_NAME_MORNING              = 1;
    public static final int SV_NAME_RANDOM               = 2;
    public static final int SV_NAME_PAIN                 = 3;
    public static final int SV_NAME_PAIN_FOLLOWUP        = 4;
    public static final int SV_NAME_DRINKING             = 5;
    public static final int SV_NAME_DRINKING_FOLLOWUP    = 6;
    public static final int SV_NAME_DUAL_FOLLOWUP        = 7;


    /*constant value*/
    public static final String PKG_BASE = "edu.missouri.niaaa.pain.";

    public static final String BD_ACTION_DAEMON_FUNC    = "Intent_Daemon";

    public static final String BD_ACTION_SURVEY_FUNC    = "Intent_Survey";
//    String schedule

    public static final int SURVEY_TIMEOUT_IN_SECONDS           = 20;//7*60;
    public static final int SURVEY_REMINDS_IN_SECONDS           = 5;//5*60;
    public static final int SURVEY_ISOLATE_IN_SECONDS           = 30;//29*60;
    public final static int SUSPENSION_INTERVAL_IN_SECOND       = 15*60;


    //*sharedPreference*//
    public static final String SP_BASE = PKG_BASE;

    /*login info*/
    public static final String SP_LOGIN                         = SP_BASE + "LOGIN";
    public static final String SP_LOGIN_KEY_STUDY_STARTTIME     = "STUDY_DAY_START";
    public static final String SP_LOGIN_KEY_USERID              = "USER_ID";
    public static final String SP_LOGIN_KEY_USERPWD             = "USER_PWD";
    /*bed time info*/
    public static final String SP_BEDTIME                       = SP_BASE + "BEDTIME";
    public static final String SP_BEDTIME_KEY_HOUR              = "BEDTIME_HOUR";
    public static final String SP_BEDTIME_KEY_MINUTE            = "BEDTIME_MINUTE";
    public static final String SP_BEDTIME_KEY_LONG              = "BEDTIME_LONG";
    /*survey*/
    public static final String SP_SURVEY                        = SP_BASE + "SURVEY";
    public static final String SP_SURVEY_KEY_FLAG_ISOLATE       = "SURVEY_ISOLATE";
    public static final String SP_SURVEY_KEY_FLAG_SUSPENSION    = "SURVEY_SUSPENSION";
    public static final String SP_SURVEY_KEY_SUSPENSION_SELECT  = "SUSPENSION_SELECTION";  




    /*broadcast actions*/
    public static final String BD_ACTION_BASE           = PKG_BASE;//+"action.";

    public static final String BD_ACTION_DAEMON         = BD_ACTION_BASE    + "DAEMON";

    public static final String BD_ACTION_SURVEY_TRIGGER = BD_ACTION_BASE    + "SURVEY_TRIGGER";
    public static final String BD_ACTION_SURVEY_REMINDS = BD_ACTION_BASE    + "SURVEY_REMINDS";
    public static final String BD_ACTION_SURVEY_ISOLATE = BD_ACTION_BASE    + "SURVEY_ISOLATE";

    public static final String BD_ACTION_SUSPENSION     = BD_ACTION_BASE    + "SUSPENSION";


    public static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");




    /**
     * Logs to debug system life cycle, which is triggered by system inherently.
     *
     * @param s1 Class name
     * @param s2 Name of life cycle function, contain "~~~" for logcat messages filtering
     */
    public static void Log_lifeCycle(String s1, String s2){
        if(DEBUG_LIFECYCLE) {
            Log.d(s1,s2);
        }
    }

    /**
     * @param s1
     * @param s2 contain "---" for logcat filtering if needed.
     */
    public static void Log_debug(String s1, String s2){
        if(DEBUG){
            Log.d(s1,s2);
        }
    }

    public static void Log_debug(String s1, boolean enableByClass, String s2){
        if(enableByClass) {
            Log_debug(s1,s2);
        }
    }

//    public static void Log_debug(String s1, boolean enableByClass, String s2, boolean enable){
//        if(enable)
//            Log_debug(s1, enableByClass, s2);
//    }


    public static List<SurveyInfo> getSurverList(Context context) throws IOException{
        //Try to read surveys from give file
        return new XMLConfigParser().parseQuestion(new InputSource(context.getAssets().open("config.xml")));
    }
    
    
    
    
    
    
    
    
    
    
    
    

    public static final int CODE_SCHEDULE_MANUALLY = 10;
    public static final int CODE_SCHEDULE_AUTOMATIC = 11;

    /* random */
    public static void scheduleRandomSurvey(Context context, boolean startFromNoon, boolean autoTriggered) {

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 23);//23
        c.set(Calendar.MINUTE, 59);//59

        long base = Calendar.getInstance().getTimeInMillis();
        if (startFromNoon) {
            Calendar b = Calendar.getInstance();
            b.set(Calendar.HOUR_OF_DAY, 12);
            b.set(Calendar.MINUTE, 0);
            b.set(Calendar.SECOND, 0);
            base = b.getTimeInMillis();
        }
        long peak = c.getTimeInMillis();

        long unit = (peak-base)/(MAX_TRIGGER_RANDOM+1);//7
        long r_unit = (peak-base)/((MAX_TRIGGER_RANDOM+1)*3);//21

        String random_schedule = new String();

        for(int i=1;i<MAX_TRIGGER_RANDOM+1;i++){//7
            random_schedule = random_schedule + (base+unit*i+(new Random().nextInt((int) (2*r_unit))-r_unit)+",");
        }


        //write to file random schedule
        String strArr[] = random_schedule.split(",");

        if(strArr.length != 1){
            int i =0;
            for(String str: strArr){
                Calendar c2 = Calendar.getInstance();
                c2.setTimeInMillis(Long.parseLong(str));
                Log.d("Random Schedule ", "each item is "+str+" "+c2.get(Calendar.HOUR_OF_DAY)+":"+c2.get(Calendar.MINUTE));

                strArr[i] = sdf.format(c2.getTime());
                i++;


                Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
                itTrigger.putExtra(Utilities.SV_NAME, i);
                PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
                long time = Long.parseLong(str);
                am.setExact(AlarmManager.RTC_WAKEUP, time, piTrigger);
            }

//            try {
//                //craving gonna be different
////                writeEventToFile(context, (autoTriggered ? CODE_SCHEDULE_AUTOMATIC : CODE_SCHEDULE_MANUALLY),
////                        strArr[0], strArr[1], strArr[2], strArr[3], strArr[4], strArr[5]);
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }

        Utilities.getSP(context, Utilities.SP_RANDOM_TIME).edit().putString(Utilities.SP_KEY_RANDOM_TIME_SET, random_schedule).commit();

    }

    
    /*************************************************************************************************************/
    /*suspension*/
    
    public static void scheduleSuspension(Context context, int selection){
        //set suspension alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis()+500;
        
        Intent suspensionIntent = new Intent(Util.BD_ACTION_SUSPENSION);
        
        PendingIntent piSuspension = PendingIntent.getBroadcast(context, 0, suspensionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piSuspension);
        am.setExact(AlarmManager.RTC_WAKEUP, time + (selection + 1) * SUSPENSION_INTERVAL_IN_SECOND * 1000, piSuspension);
        
        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(time + (selection + 1) * SUSPENSION_INTERVAL_IN_SECOND * 1000);
            Util.Log_debug(TAG, "---suspension scheduled @time " + sdf.format(tempc.getTime()) + " for seconds: " + SUSPENSION_INTERVAL_IN_SECOND);
        }
        
        setSuspensionFlag(context, time + (selection + 1) * SUSPENSION_INTERVAL_IN_SECOND * 1000, selection);
    }
    
    public static void cancelSuspension(Context context){
        //set suspension alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent suspensionIntent = new Intent(Util.BD_ACTION_SUSPENSION);
        
        PendingIntent piSuspension = PendingIntent.getBroadcast(context, 0, suspensionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piSuspension);
        
        Util.Log_debug(TAG, "---suspension canceled");
        
        resetSuspensionFlag(context);
    }
    
    public static void setSuspensionFlag(Context context, long datetime, int selection){
        SharedPreferences sp = Utilities.getSP(context, Utilities.SP_SURVEY);
        
        sp.edit().putLong(Util.SP_SURVEY_KEY_FLAG_SUSPENSION, datetime).commit();
        sp.edit().putInt(Util.SP_SURVEY_KEY_SUSPENSION_SELECT, selection + 1).commit();
    }
    
    public static void resetSuspensionFlag(Context context){
        Utilities.getSP(context, Util.SP_SURVEY).edit().remove(Util.SP_SURVEY_KEY_FLAG_SUSPENSION).commit();
    }
    
    public static boolean isSuspensionFlag(Context context){
        
        SharedPreferences sp = Utilities.getSP(context, Utilities.SP_SURVEY);
        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        
        if(!sp.contains(Util.SP_SURVEY_KEY_FLAG_ISOLATE)){
            return false;
        }
        else{
            expire.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_FLAG_SUSPENSION, 0));
            Log.d(TAG, sdf.format(expire.getTime()));
            if(now.before(expire)){
                return true;
            }
            else{
                return false;
            }
        }
    }

    /*************************************************************************************************************/
    /*Survey scheduler*/
    
    public static void scheduleSurveyIsolater(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis()+500;
        
        Intent itTrigger =  new Intent(Util.BD_ACTION_SURVEY_ISOLATE);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);
        am.setExact(AlarmManager.RTC_WAKEUP, time + SURVEY_ISOLATE_IN_SECONDS * 1000, piTrigger);
        
        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(time + SURVEY_ISOLATE_IN_SECONDS * 1000);
            Util.Log_debug(TAG, "---isolater scheduled @time " + sdf.format(tempc.getTime()) + " for seconds: " + SURVEY_ISOLATE_IN_SECONDS);
        }
        
        setIsolateFlag(context, time);
    }
    
    public static void cancelSurveyIsolater(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Intent itTrigger =  new Intent(Util.BD_ACTION_SURVEY_ISOLATE);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);
        
        Util.Log_debug(TAG, "---isolater canceled");
        
        resetIsolateFlag(context);
    }
    
    public static void setIsolateFlag(Context context, long datetime){
        Utilities.getSP(context, Util.SP_SURVEY).edit().putLong(Util.SP_SURVEY_KEY_FLAG_ISOLATE, datetime).commit();
    }
    
    public static void resetIsolateFlag(Context context){
        Utilities.getSP(context, Util.SP_SURVEY).edit().remove(Util.SP_SURVEY_KEY_FLAG_ISOLATE).commit();
    }
    
    public static boolean isIsolateFlag(Context context){
    
        SharedPreferences sp = Utilities.getSP(context, Utilities.SP_SURVEY);
        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        
        if(!sp.contains(Util.SP_SURVEY_KEY_FLAG_ISOLATE)){
            return false;
        }
        else{
            expire.setTimeInMillis(sp.getLong(Util.SP_SURVEY_KEY_FLAG_ISOLATE, 0)+Util.SURVEY_ISOLATE_IN_SECONDS*1000);
            Log.d(TAG, sdf.format(expire.getTime()));
            if(now.before(expire)){//not after
                return true;
            }
            else{
                return false;
            }
        }
    }
    
    /*--*/
    
    public static void scheduleSurveyTimeout(Context context, int surveyType, int surveySeq){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis()+500;
        
        int seq = SurveyAct.REMIND_TIMEOUT;
        Intent itTrigger = getReminderIntent(surveyType, surveySeq, seq);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);
        am.setExact(AlarmManager.RTC_WAKEUP, time + SURVEY_TIMEOUT_IN_SECONDS * 1000, piTrigger);
        
        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(time + SURVEY_TIMEOUT_IN_SECONDS * 1000);
            Util.Log_debug(TAG, "---timeout scheduled @time " + sdf.format(tempc.getTime()) + " for seconds: " + SURVEY_TIMEOUT_IN_SECONDS);
        }
            
    }
    
    public static void cancelSurveyTimeout(Context context, int surveyType, int surveySeq){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        int seq = SurveyAct.REMIND_TIMEOUT;
        Intent itTrigger = getReminderIntent(surveyType, surveySeq, seq);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);
        
        Util.Log_debug(TAG, "---timeout canceled");
            
    }
    
    /*--*/
    
    public static void scheduleSurveyReminders(Context context, int surveyType, int surveySeq){
        
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis()+500;
        
        int num = MAX_REMINDER + 1;//4
        for(int i = 1; i <= num; i++){
            int seq = i % num;
            Intent itTrigger = getReminderIntent(surveyType, surveySeq, seq);

            PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(piTrigger);
            am.setExact(AlarmManager.RTC_WAKEUP, time + (i-1) * SURVEY_REMINDS_IN_SECONDS * 1000, piTrigger);
            
            if(DEBUG){
                Calendar tempc = Calendar.getInstance();
                tempc.setTimeInMillis(time + (i-1) * SURVEY_REMINDS_IN_SECONDS * 1000);
                Util.Log_debug(TAG, "---reminder scheduled seq "+i+" @time " + sdf.format(tempc.getTime()));
            }
            
            /*it that a good way to wait a little bit time*/
            
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
        }
        
    }
    
    public static void cancelSurveyReminders(Context context, int surveyType, int surveySeq){
        
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        int num = MAX_REMINDER + 1;//4
        for(int i = 1; i <= num; i++){
            int seq = i % num;
            Intent itTrigger = getReminderIntent(surveyType, surveySeq, seq);

            PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
            am.cancel(piTrigger);
        }
        
        Util.Log_debug(TAG, "---reminder canceled");
    }
    
    public static Intent getReminderIntent(int surveyType, int surveySeq, int seq){
        Intent i = new Intent(Util.BD_ACTION_SURVEY_REMINDS);
        i.putExtra(Util.SV_TYPE, surveyType);
        i.putExtra(Util.SV_SEQ, surveySeq);
        i.putExtra(Util.SV_REMIND_SEQ, seq);

        return i;
    }


    /*************************************************************************************************************/
    /*Morning & bedtime*/

    public static void scheduleMorningSurvey(Context context){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        //default time to 12:00 at noon
        Calendar c = Utilities.getDefaultMorningCal(context);
        long defTime = c.getTimeInMillis();
        long time = Long.MAX_VALUE;

        time = Utilities.getSP(context, Util.SP_BEDTIME).getLong(Util.SP_BEDTIME_KEY_LONG, defTime);
        Util.Log_debug(TAG, "---MorningSruvey scheduled at "+Utilities.getTimeFromLong(time)+" context "+context.hashCode());

        Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
        itTrigger.putExtra(Utilities.SV_NAME, Utilities.SV_NAME_MORNING);
        PendingIntent piTrigger = PendingIntent.getBroadcast(context, 1, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

        am.setExact(AlarmManager.RTC_WAKEUP, time, piTrigger);
    }

    /**
     * This is called when you not sure whether or not it's right time to schedule morning survey.
     * like what it needs to restore from reboot.
     */
    public static void rescheduleMorningSurvey(Context context){

    }


    public static void activateToday(){

    }

    public static void deActivateToday(){

    }


    /**
     * deactivate and cancel surveys today,
     * schedule for next morning survey.
     */
    public static void bedtimeComplete(Context context){

        deActivateToday();

        scheduleMorningSurvey(context);
    }


    /**
     * Morning scheduler just set for hour and minute, call this to get proper date information.
     * @param hour
     * @param minute
     * @return
     * if set before midnight(after 9pm), morning alarm day is tomorrow,
     * else if set before 3am, morning alarm day is today.
     */
    public static Calendar getProperMorningScheduleTime(int hour, int minute){
        Calendar c = Calendar.getInstance();

        if(c.get(Calendar.HOUR_OF_DAY) > 3){
            //next day
            c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + 1);
        }
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);

        return c;
    }

    /*/Morning & bedtime*/



    
    
    /**
     * when app reboot, check if there is any suspension at the time phone shut down.
     * if it still under that time period, set suspension again,
     * if it expired, clear the suspension flag.
     */
    public static void restoreSuspension(){

    }

    public static boolean isSuspension(){
        return false;
    }


    /**
     * start recording location with condition checking first
     * check if today is activated (from activated time to 3am next day)
     */
    public static void restartRecordingLocation(){
        if(true){
            startRecordingLocation();
        }
    }

    public static void startRecordingLocation(){

    }

    public static void stopRecordingLocation(){

    }


    /**
     * @return
     */
    public static boolean isTodayActivated(){
        return false;
    }
    
    
}
