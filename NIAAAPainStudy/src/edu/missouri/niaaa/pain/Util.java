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
import edu.missouri.niaaa.pain.location.LocationUtilities;
import edu.missouri.niaaa.pain.survey.SurveyAct;
import edu.missouri.niaaa.pain.survey.parser.SurveyInfo;
import edu.missouri.niaaa.pain.survey.parser.XMLConfigParser;

public class Util {
    static String TAG = "Util.java";

    public static final String  ADMIN_UID = "0000";

    public final static int CODE_NAME_MORNING = 1;
    public final static int CODE_NAME_DRINKING = 2;
    public final static int CODE_NAME_MOOD = 3;
    public final static int CODE_NAME_CRAVING = 4;
    public final static int CODE_NAME_RANDOM = 5;
    public final static int CODE_NAME_FOLLOW = 6;
    public final static int CODE_SUSPENSION             = 7;
    public final static int CODE_BEDTIME                = 8;
    public final static int CODE_SENSOR_CONN = 9;
    public final static int CODE_SCHEDULE_MANUALLY = 10;
    public final static int CODE_SCHEDULE_AUTOMATIC = 11;



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

//    public static final String BD_ACTION_SURVEY_FUNC    = "Intent_Survey";
//    String schedule

    public static final int SURVEY_TIMEOUT_IN_SECONDS           = 7*60;
    public static final int SURVEY_REMINDS_IN_SECONDS           = 5*60;
    public static final int SURVEY_ISOLATE_IN_SECONDS           = 29*60;
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
    public static final String SP_SURVEY_KEY_FLAG_ISOLATE       = "SURVEY_ISOLATE_EXPIRE";
    public static final String SP_SURVEY_KEY_FLAG_SUSPENSION    = "SURVEY_SUSPENSION_EXPIRE";
    public static final String SP_SURVEY_KEY_SUSPENSION_START   = "SURVEY_SUSPENSION_START_DATETIME";
    public static final String SP_SURVEY_KEY_FLAG_ACTIVATE      = "SURVEY_ACTIVATE_TIME";
    public static final String SP_SURVEY_KEY_RANDOM_SETS        = "SURVEY_RANDOM_SETS";




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


    public static List<SurveyInfo> getSurveyList(Context context) throws IOException{
        //Try to read surveys from give file
        return new XMLConfigParser().parseQuestion(new InputSource(context.getAssets().open("config.xml")));
    }
    
    
    
    
    
    
    
    
    
    
    
    

    /*************************************************************************************************************/
    /* random */
    
    public static String setRandomSchedule(Context context, boolean startFromNoon, boolean systemTriggered){
        Util.Log_debug(TAG, "set Random Schedule time sets " + startFromNoon);
        
        Calendar midnight = Calendar.getInstance();
        midnight.set(Calendar.HOUR_OF_DAY, 23);//23
        midnight.set(Calendar.MINUTE, 59);//59
        long peak = midnight.getTimeInMillis();

        long base = Calendar.getInstance().getTimeInMillis();
        if (startFromNoon) {
            Calendar b = Calendar.getInstance();
            b.set(Calendar.HOUR_OF_DAY, 12);
            b.set(Calendar.MINUTE, 0);
            b.set(Calendar.SECOND, 0);
            base = b.getTimeInMillis();
        }
        
        //calcuate MAX_TRIGGER_RANDOM random time sets
        long unit = (peak-base)/(MAX_TRIGGER_RANDOM+1);//7
        long r_unit = (peak-base)/((MAX_TRIGGER_RANDOM+1)*3);//21

        String random_schedule = new String();

        for(int i=1;i<MAX_TRIGGER_RANDOM+1;i++){//7
            random_schedule = random_schedule + (base+unit*i+(new Random().nextInt((int) (2*r_unit))-r_unit)+",");
        }
        
        
        //write Random Survey Schedules
        String strArr[] = random_schedule.split(",");
        
        int i =0;
        for(String str: strArr){
            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(Long.parseLong(str));

            strArr[i] = sdf.format(c.getTime());
            i++;
        }
        
        try {
            //craving gonna be different
            Utilities.writeEventToFile(context, (systemTriggered ? CODE_SCHEDULE_AUTOMATIC : CODE_SCHEDULE_MANUALLY),
                    strArr[0], strArr[1], strArr[2], strArr[3], strArr[4], strArr[5]);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return random_schedule;
    }
    
    
    public static void scheduleRandomSurvey(Context context, String time_sets){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar now = Calendar.getInstance();

        //write to file random schedule
        String strArr[] = time_sets.split(",");

        if(strArr.length != 1){
            
            int i =0;
            for(String str: strArr){
                i++;
                long time = Long.parseLong(str);
                
                Calendar r = Calendar.getInstance();
                r.setTimeInMillis(time);
                
                if(r.after(now)){
                    Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
                    itTrigger.putExtra(Util.SV_TYPE, Util.SV_NAME_RANDOM);
                    itTrigger.putExtra(Util.SV_SEQ, i);
                    
                    PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
                    
                    am.cancel(piTrigger);
                    am.setExact(AlarmManager.RTC_WAKEUP, time, piTrigger);
                    
                    Log.d("Random Schedule ", "each item is "+i+" "+str+" "+r.get(Calendar.HOUR_OF_DAY)+":"+r.get(Calendar.MINUTE));
                }
            }
        }

        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        sp.edit().putString(Util.SP_SURVEY_KEY_RANDOM_SETS, time_sets).commit();
        
        sp.edit().putLong(Util.SP_SURVEY_KEY_FLAG_ACTIVATE, Calendar.getInstance().getTimeInMillis()).commit();

    }

    
    
    public static void cancelRandomSurvey(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        
        int num = MAX_TRIGGER_RANDOM + 1;//7
        for(int i = 1; i < num; i++){
            Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
            itTrigger.putExtra(Util.SV_TYPE, Util.SV_NAME_RANDOM);
            itTrigger.putExtra(Util.SV_SEQ, i);

            PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
            
            am.cancel(piTrigger);
            
            cancelSurveyReminders(context, Util.SV_NAME_RANDOM, i);
        }
        
        Util.Log_debug(TAG, "---reminder canceled");
        
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        sp.edit().remove(Util.SP_SURVEY_KEY_RANDOM_SETS).commit();
        
//        sp.edit().remove(Util.SP_SURVEY_KEY_FLAG_ACTIVATE).commit();//leave not delete until ...
    }
    
    
    
    
    
    public static void reScheduleRandomSurvey(Context context){
        String sets = Utilities.getSP(context, Util.SP_SURVEY).getString(Util.SP_SURVEY_KEY_RANDOM_SETS, "");
        
        scheduleRandomSurvey(context, sets);
    }
    
    
    
    
    
    
    
    /*************************************************************************************************************/
    /*suspension*/
    
    public static void scheduleSuspension(Context context, long expire){
        //set suspension alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent suspensionIntent = new Intent(Util.BD_ACTION_SUSPENSION);
        
        PendingIntent piSuspension = PendingIntent.getBroadcast(context, 0, suspensionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piSuspension);
        am.setExact(AlarmManager.RTC_WAKEUP, expire, piSuspension);
        
        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(expire);
            Util.Log_debug(TAG, "---suspension scheduled @time " + sdf.format(tempc.getTime()) + " for seconds: " + SUSPENSION_INTERVAL_IN_SECOND);
        }
        
        setSuspensionFlag(context, expire);
    }
    
    
    /**
     * write datetime using "SP_SURVEY_KEY_SUSPENSION_START" before calling this function
     * @param context
     */
    public static void cancelSuspension(Context context, boolean writeCurrentDatetime){
        //set suspension alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent suspensionIntent = new Intent(Util.BD_ACTION_SUSPENSION);
        
        PendingIntent piSuspension = PendingIntent.getBroadcast(context, 0, suspensionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piSuspension);
        
        Util.Log_debug(TAG, "---suspension canceled");
        
        //write
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(sp.getLong(Util.SP_SURVEY_KEY_SUSPENSION_START, 0));//protect 0
        
        Calendar c2 = Calendar.getInstance();
        if(!writeCurrentDatetime){
            c2.setTimeInMillis(sp.getLong(Util.SP_SURVEY_KEY_FLAG_SUSPENSION, 0));//protect 0
        }
        
        try {
            Utilities.writeEventToFile(context, Util.CODE_SUSPENSION, 
                    "", "", "", "",
                    Util.sdf.format(c.getTime()), Util.sdf.format(c2.getTime()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        resetSuspensionFlag(context);
    }
    
    public static void setSuspensionFlag(Context context, long datetime){
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        sp.edit().putLong(Util.SP_SURVEY_KEY_FLAG_SUSPENSION, datetime).commit();
        
    }
    
    public static void resetSuspensionFlag(Context context){
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        sp.edit().remove(Util.SP_SURVEY_KEY_FLAG_SUSPENSION).commit();
        
        sp.edit().remove(Util.SP_SURVEY_KEY_SUSPENSION_START).commit();
        
    }
    
    public static boolean isSuspensionFlag(Context context){
        
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        
        if(!sp.contains(Util.SP_SURVEY_KEY_FLAG_SUSPENSION)){
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

    
    /**
     * when app reboot, check if there is any suspension at the time phone shut down.
     * if it still under that time period, set suspension again,
     * if it expired, clear the suspension flag.
     */
    public static void reScheduleSuspension(Context context){
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);

        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();
        
        if(!sp.contains(Util.SP_SURVEY_KEY_FLAG_SUSPENSION)){
            // do nothing
            Util.Log_debug(TAG, "reschedule suspension ~ do nothing");
        }
        else{
            expire.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_FLAG_SUSPENSION, 0));
            if(now.before(expire)){
                //schedule
                Util.Log_debug(TAG, "reschedule suspension ~ schedule @ "+sdf.format(expire.getTime()));
                long datetime = sp.getLong(Util.SP_SURVEY_KEY_FLAG_SUSPENSION, 0);
                scheduleSuspension(context, datetime);
            }
            else{
                //cancel and write break suspension ###
                Util.Log_debug(TAG, "reschedule suspension ~ cancel and write ###");
                cancelSuspension(context, false);
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
    
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
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

    public static void scheduleMorningSurvey(Context context, long expire){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int seq = 0;
        
        Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
        itTrigger.putExtra(Util.SV_TYPE, Util.SV_NAME_MORNING);
        itTrigger.putExtra(Util.SV_SEQ, seq);
        
        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        
        am.cancel(piTrigger);
        am.setExact(AlarmManager.RTC_WAKEUP, expire, piTrigger);
        
        Util.Log_debug(TAG, "---MorningSruvey scheduled at "+Utilities.getTimeFromLong(expire)+" context "+context.hashCode());
        
        Utilities.getSP(context, Util.SP_BEDTIME).edit().putLong(Util.SP_BEDTIME_KEY_LONG, expire).commit();
        
    }
    
    public static void cancelMorningSurvey(Context context){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int seq = 0;
        
        Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
        itTrigger.putExtra(Util.SV_TYPE, Util.SV_NAME_MORNING);
        itTrigger.putExtra(Util.SV_SEQ, seq);
        
        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(piTrigger);
        cancelSurveyReminders(context, Util.SV_NAME_MORNING, seq);
        
        Util.Log_debug(TAG, "---MorningSruvey canceled");
        
        Utilities.getSP(context, Util.SP_BEDTIME).edit().remove(Util.SP_BEDTIME_KEY_LONG).commit();
    }

    
    
    /**
     * This is called when you not sure whether or not it's right time to schedule morning survey.
     * like what it needs to restore from reboot.
     */
    public static void rescheduleMorningSurvey(Context context){//schedule all
        SharedPreferences sp = Utilities.getSP(context, Util.SP_BEDTIME);

        //noon
        Calendar n = Calendar.getInstance();
        n.set(Calendar.HOUR_OF_DAY, 12);
        n.set(Calendar.MINUTE, 0);
        n.set(Calendar.SECOND, 0);

        //default time to 12:00 at noon
        Calendar def = getDefaultMorningCal(context);
        long defTime = def.getTimeInMillis();

        //current
        Calendar c = Calendar.getInstance();

        //morning
        Calendar expire = Calendar.getInstance();
        expire.setTimeInMillis(sp.getLong(Util.SP_BEDTIME_KEY_LONG, defTime));

        //set morning & schedule random
        if(c.after(n) || isTodayActive(context)){
            Util.Log_debug(TAG, "reschedule morning ~ after noon");
            morningComplete(context, true, true);
        }

        //before previous set morning time & before noon
        else if(c.before(expire)){
            Util.Log_debug(TAG, "reschedule morning ~ schedule @ "+sdf.format(expire.getTime()));

            //if not activate today
            scheduleMorningSurvey(context, expire.getTimeInMillis());
        }
        //after morning time but earlier than noon
        else{
            //do nothing
            Util.Log_debug(TAG, "reschedule morning ~ do nothing waiting user manually do morning survey");
        }
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
    
    
    public static Calendar getDefaultMorningCal(Context context){

        SharedPreferences sp = Utilities.getSP(context, Util.SP_BEDTIME);
        int hour = Utilities.defHour;
        int minute = Utilities.defMinute;

        boolean setDefault = (sp.getInt(Util.SP_BEDTIME_KEY_HOUR, -1) == -1?false:true);
        if(setDefault){
            hour = sp.getInt(Util.SP_BEDTIME_KEY_HOUR, -1);
            minute = sp.getInt(Util.SP_BEDTIME_KEY_MINUTE, -1);
        }

        return Util.getProperMorningScheduleTime(hour, minute);
    }
    
    
    
    /*************************************************************************************************************/
    
    

    public static void activate(Context context, boolean startFromNoon, boolean systemTriggered){
        
        if(isActFlagToday(context)){
            if(isTodayActive(context)){
                reScheduleRandomSurvey(context);
            }
            else{
                
            }
        }
        else{
            scheduleRandomSurvey(context, setRandomSchedule(context, startFromNoon, systemTriggered));
        }
        
        //restart gps
        if(Util.isTodayActive(context) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
            context.sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
        }
    }

    
    public static void deActivate(Context context){
        cancelRandomSurvey(context);
    }

    
    
    public static boolean isTodayActive(Context context){//##??
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        if(!isActFlagToday(context)){
            return false;
        }
        else{
            if(sp.contains(Util.SP_SURVEY_KEY_RANDOM_SETS)){
                return true;
            }
            else{
                return false;
            }
        }
    }
    
    
    /**
     * @return
     */
    public static boolean isActFlagToday(Context context){
        
        SharedPreferences sp = Utilities.getSP(context, Util.SP_SURVEY);
        if(!sp.contains(Util.SP_SURVEY_KEY_FLAG_ACTIVATE)){
            return false;
        }
        else{
            Calendar now = Calendar.getInstance();
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_FLAG_ACTIVATE, 0));
            Log.d(TAG, sdf.format(day.getTime()));
            if(now.get(Calendar.HOUR_OF_DAY) < 3){
                if(now.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)+1){
                    return true;
                }
                else{
                    return false;
                }
            }
            else{
                if(now.get(Calendar.DAY_OF_YEAR) == day.get(Calendar.DAY_OF_YEAR)){
                    return true;
                }
                else{
                    return false;
                }
            }
        }
    }

    /**
     * when reboot from 3pm, this is called and should be good
     * @param context
     */
    public static void morningComplete(Context context, boolean startFromNoon, boolean systemTriggered){
         
        activate(context, startFromNoon, systemTriggered);
        
        cancelDaemonNoon();//##??
        
        cancelMorningSurvey(context);
        
    }
    
    private static void cancelDaemonNoon() {
        // TODO Auto-generated method stub
        
    }

    
    /**
     * deactivate and cancel surveys today,
     * schedule for next morning survey.
     */
    public static void bedtimeComplete(Context context, long expire){

        deActivate(context);

        scheduleMorningSurvey(context, expire);
    }







    

    /*GPS location*/

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



    
    
}
