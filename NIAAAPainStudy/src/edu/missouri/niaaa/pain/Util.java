package edu.missouri.niaaa.pain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.location.DetectedActivity;

import edu.missouri.niaaa.Craving;
import edu.missouri.niaaa.pain.location.ActivityRecognitionService;
import edu.missouri.niaaa.pain.location.LocationBroadcast;
import edu.missouri.niaaa.pain.location.LocationUtilities;
import edu.missouri.niaaa.pain.survey.SurveyActivity;
import edu.missouri.niaaa.pain.survey.parser.SurveyInfo;
import edu.missouri.niaaa.pain.survey.parser.XMLConfigParser;

public class Util {
    static String TAG = "Util.java";

    public static final String ADMIN_UID = "0000";
    public static final String PHONE_BASE_PATH = Config.getPhoneBasePath();
    public static final String PKG_BASE = "edu.missouri.niaaa.pain.";

    /*for debug*/
    public static final boolean DEBUG_LIFECYCLE         = true;
    public static final boolean DEBUG                   = true;
    public static final boolean RELEASE                 = false;
    public static final boolean REMIND_SEPLIT = true;
    public final static boolean WRITE_RAW               = !RELEASE;

    /*survey type*/
    public static final int SV_TYPE_MORNING             = 1;
    public static final int SV_TYPE_RANDOM              = 2;
    public static final int SV_TYPE_PAIN                = 3;
    public static final int SV_TYPE_PAIN_FOLLOWUP       = 4;
    public static final int SV_TYPE_DRINKING            = 5;
    public static final int SV_TYPE_DRINKING_FOLLOWUP   = 6;
    public static final int SV_TYPE_DUAL_FOLLOWUP       = 7;


    /*uploading code*/
    public final static String CODE_NAME_MORNING = "1";
    public final static String CODE_NAME_DRINKING = "2";
    public final static String CODE_NAME_MOOD = "3";
    public final static String CODE_NAME_CRAVING = "4";
    public final static String CODE_NAME_RANDOM = "5";
    public final static String CODE_NAME_FOLLOW = "6";

    public final static String CODE_SUSPENSION          = "7";//12
    public final static String CODE_BEDTIME             = "8";//13
    public final static String CODE_SENSOR_CONN         = "9";//14
    public final static String CODE_SCHEDULE_MANUALLY   = "10";
    public final static String CODE_SCHEDULE_AUTOMATIC  = "11";

    public final static String CODE_SV_FULLY_FINISHED   = "_20";
    public final static String CODE_SV_IGNORED          = "_21";
    public final static String CODE_SV_QUIT             = "_22";
    public final static String CODE_SV_TIMEOUT          = "_23";
    public final static String CODE_SV_REFUSED          = "_24";
    public final static String CODE_SV_NO_PROMPT        = "_25";


    /*survey config*/
    public static final int MAX_REMINDER                = 3;
    public static final int MAX_TRIGGER_RANDOM          = 6;//6
    public static final int MAX_TRIGGER_FOLLOWUP        = 3;//3
    public static final int VOLUME                      = !RELEASE ? 2 : 9;


    /*constant value*/
    public static final int SURVEY_TIMEOUT_IN_SECONDS           = 15*60;//7*60;
    public static final int SURVEY_REMINDS_IN_SECONDS           = 5*60;
    public static final int SURVEY_ISOLATE_IN_SECONDS           = 29*60;
    public final static int SUSPENSION_INTERVAL_IN_SECONDS      = 15*60;
    public final static int FOLLOWUP_IN_SECONDS                 = 30*60;

    public final static String TIME_NONE = "none";
    public final static int defHour = 12;
    public final static int defMinute = 0;
    public final static int PREFIX_LEN = 50;//35;

    public static final String SV_TYPE                  = "Survey_Type";
    public static final String SV_SEQ                   = "Survey_Seq";
    public static final String SV_REMIND_SEQ            = "Survey_Reminder_Seq";
    public static final String SV_MANUAL                = "Survey_manual_triggered";
    public static final String SV_END_CYCLE             = "Survey_end_cycle";


    public static final SimpleDateFormat dtF = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    public static final SimpleDateFormat mdF = new SimpleDateFormat("MMM_dd");


    //*sharedPreference*//
    public static final String SP_BASE                          = PKG_BASE;

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
    public static final String SP_SURVEY_KEY_ISOLATE_START      = "SURVEY_ISOLATE_START_DT";
    public static final String SP_SURVEY_KEY_SUSPENSION_EXPIRE  = "SURVEY_SUSPENSION_EXPIRE_DT";
    public static final String SP_SURVEY_KEY_SUSPENSION_START   = "SURVEY_SUSPENSION_START_DT";
    public static final String SP_SURVEY_KEY_FLAG_ACTIVATE      = "SURVEY_ACTIVATE_TIME";
    public static final String SP_SURVEY_KEY_RANDOM_SETS        = "SURVEY_RANDOM_SETS";
    public static final String SP_SURVEY_KEY_FLAG_CYCLE         = "SURVEY_CYCLE_TIME";
    public static final String SP_SURVEY_KEY_FOLLOWUP_SETS      = "SURVEY_FOLLOWUP_SETS";

    /*sensor*/
    public static final String SP_SENSOR                        = SP_BASE + "SENSOR";
    public final static String SP_SENSOR_KEY_CONN_TS            = "SENSOR_CONN_TS";


    /*broadcast actions*/
    public static final String BD_ACTION_BASE                   = PKG_BASE + "action.";

    public static final String BD_ACTION_DAEMON                 = BD_ACTION_BASE    + "DAEMON";

    public static final String BD_ACTION_SURVEY_TRIGGER_RANDOM  = BD_ACTION_BASE    + "SURVEY_TRIGGER";
    public static final String BD_ACTION_SURVEY_TRIGGER_FOLLOW  = BD_ACTION_BASE    + "SURVEY_TRIGGER_";
    public static final String BD_ACTION_SURVEY_REMINDS_RANDOM  = BD_ACTION_BASE    + "SURVEY_REMINDS";
    public static final String BD_ACTION_SURVEY_REMINDS_FOLLOW  = BD_ACTION_BASE    + "SURVEY_REMINDS_";
    
    public static final String BD_ACTION_SURVEY_ISOLATE         = BD_ACTION_BASE    + "SURVEY_ISOLATE";
    public static final String BD_ACTION_SUSPENSION             = BD_ACTION_BASE    + "SUSPENSION";


    /*server addresses*/

    /*Craving Study*/
    public final static String VALIDATE_ADDRESS             = Craving.VALIDATE_ADDRESS;
    public final static String WRITE_ARRAY_TO_FILE          = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtTest/Crt2/writeArrayToFile.php";
    public final static String WRITE_ARRAY_TO_FILE_DEC      = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtTest/Crt2/writeArrayToFileDec.php";
    public final static String COMPLIANCE_ADDRESS           = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtTest/Crt2/complianceDec.php";
    public final static String STUDY_DAY_MODIFY_ADDRESS     = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtTest/Crt2/changeStudyWeekDec.php";
    public final static String WRITE_ARRAY_TO_RECOVERY_DEC  = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtTest/Crt2/recoverDec.php";

    /*EMA-STL Study*/
//    public final static String VALIDATE_ADDRESS             = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/validateUser.php";
//    public final static String WRITE_ARRAY_TO_FILE          = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/writeArrayToFile.php";
//    public final static String WRITE_ARRAY_TO_FILE_DEC      = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/writeArrayToFileDec.php";
//    public final static String COMPLIANCE_ADDRESS           = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/compliance.php";
//    public final static String STUDY_DAY_MODIFY_ADDRESS     = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/changeStudyWeek.php";
//    public final static String WRITE_ARRAY_TO_RECOVERY_DEC  = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtEMA/recoverDec.php";

    /*NIMH Emotion Study*/
//    public final static String VALIDATE_ADDRESS             = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/validateUserDec.php";
//    public final static String WRITE_ARRAY_TO_FILE          = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/writeArrayToFile.php";
//    public final static String WRITE_ARRAY_TO_FILE_DEC      = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/writeArrayToFileDec.php";
//    public final static String COMPLIANCE_ADDRESS           = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/complianceDec.php";
//    public final static String STUDY_DAY_MODIFY_ADDRESS     = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/changeStudyWeekDec.php";
//    public final static String WRITE_ARRAY_TO_RECOVERY_DEC  = "http://dslsrv8.cs.missouri.edu/~hw85f/Server/CrtNIMH/recoverDec.php";

//  public final static String UPLOAD_ADDRESS = WRITE_ARRAY_TO_FILE;
    public final static String UPLOAD_ADDRESS = WRITE_ARRAY_TO_FILE_DEC;
    public final static String RECOVERY_ADDRESS = WRITE_ARRAY_TO_RECOVERY_DEC;



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

    public static void debugDT(String tag, long milliseconds){
        Calendar printC = Calendar.getInstance();
        printC.setTimeInMillis(milliseconds);
        Util.Log_debug(tag, "scheduled @ "+Util.dtF.format(printC.getTime()));
    }



    /*survey getters*/
    public static List<SurveyInfo> getSurveyList(Context context) throws IOException{
        //Try to read surveys from give file
        return new XMLConfigParser().parseQuestion(new InputSource(context.getAssets().open("config.xml")));
    }

    public static SharedPreferences getSP(Context context, String name){
        SharedPreferences shp = context.getSharedPreferences(name, Context.MODE_MULTI_PROCESS);
        return shp;
    }

    public static String getPWD(Context context){// need modify
        SharedPreferences shp = context.getSharedPreferences(SP_LOGIN, Context.MODE_PRIVATE);
//      ID = shp.getString(AdminManageActivity.ASID, "");
        String PWD = shp.getString(SP_LOGIN_KEY_USERPWD, "");
        return PWD;
    }


//    public static final int SV_NAME_MORNING              = 1;
//    public static final int SV_NAME_RANDOM               = 2;
//    public static final int SV_NAME_PAIN                 = 3;
//    public static final int SV_NAME_PAIN_FOLLOWUP        = 4;
//    public static final int SV_NAME_DRINKING             = 5;
//    public static final int SV_NAME_DRINKING_FOLLOWUP    = 6;
//    public static final int SV_NAME_DUAL_FOLLOWUP        = 7;


    public static String getSurveyCode(int surveyType){

        switch(surveyType){
        case 1:
            return CODE_NAME_MORNING;
        case 2:
            return CODE_NAME_RANDOM;
        case 3:
            return "3";
        case 4:
            return "4";
        case 5:
            return CODE_NAME_DRINKING;
        case 6:
            return CODE_NAME_FOLLOW;
        case 7:
            return CODE_NAME_MOOD;//##??
        default:
            return "-1";
        }
    }


    public static String getSurveyScheduleDT(Context context, int surveyType, int surveySeq){
        Util.Log_debug(TAG, "get scheduled time "+surveyType+" "+surveySeq);

        long datetime = -1;
        Calendar cal = Calendar.getInstance();

        switch(surveyType){
        case 1:
            datetime = getSP(context, SP_BEDTIME).getLong(SP_BEDTIME_KEY_LONG, -1);
            break;
        case 2:
            String set = getExistRandomSchedules(context);
            if(!set.equals("")){
                String strRandom[] = set.split(",");
                if(set.length() != 1){
                    try{
                        datetime = Long.parseLong(strRandom[surveySeq-1]);
                    }
                    catch(Exception e){
                        datetime = -1;
                    }
                }
            }
            break;
        case SV_TYPE_DRINKING_FOLLOWUP:
        case SV_TYPE_PAIN_FOLLOWUP:
        case SV_TYPE_DUAL_FOLLOWUP:
            String set2 = getExistingFollowupSchedules(context);
            if(!set2.equals("")){
                String strRandom[] = set2.split(",");
                if(set2.length() != 1){
                    try{
                        datetime = Long.parseLong(strRandom[surveySeq-1]);
                    }
                    catch(Exception e){
                        datetime = -1;
                    }
                }
            }
            break;
        default:
            
        }

        if(datetime == -1){
            return "";
        }
        else{
            cal.setTimeInMillis(datetime);
            return dtF.format(cal.getTime());
        }
    }


    public static String getSurveyAlarmDT(Calendar cal, int remindSeq){

        String str = ",,";
        if(cal == null){
            return str;
        }

        Util.Log_debug(TAG, "get survey alarm time "+cal.getTime()+" "+remindSeq);
        String datetime = dtF.format(cal.getTime());

        switch(remindSeq){
        case 1:
            str = datetime+",,";
            break;
        case 2:
            str = ","+datetime+",";
            break;
        case 3:
            str = ",,"+datetime;
            break;
        default:
            break;
        }
        return str;
    }


    public static void setAlarmExact(AlarmManager am, long time, PendingIntent pi){
        am.setExact(AlarmManager.RTC_WAKEUP, time, pi);
    }


    /*************************************************************************************************************/
    /*followups*/
    
    public static String getNewFollowupSchedules(Context context){
        
        Calendar now  = Calendar.getInstance();
        long base = now.getTimeInMillis();
        
        long half = 30*60*1000;
        long h1 = base + half;
        long h2 = h1 + half;
        long h3 = h2 + 2*half;
        String res = h1 + "," + h2 + "," + h3;
        
        return res;
    }
    
    public static String getUpdatedFollowupSchedules(Context context, int current, boolean hasTrigger){
        String exist[] = getExistingFollowupSchedules(context).split(",");
        long now = Calendar.getInstance().getTimeInMillis();
        Calendar cal = Calendar.getInstance();
        String res = "";
        
        for(int i = 0; i < exist.length; i++){
            if(i < current){
                res += exist[i]+",";
            }
            else{
                res += (now+=(i<=1 ? 30*60*1000 : 60*60*1000))+",";
            }
        }
        if(hasTrigger && (current > 3 || (exist.length <= 3))){
            res += now+=60*60*1000;
        }
        
        
        //for debug
        for(String str: res.split(",")){
            cal.setTimeInMillis(Long.parseLong(str));
            Log.d(TAG, "~~~"+cal.getTime());
        }
        
//        SharedPreferences sp = getSP(context, SP_SURVEY);
//        sp.edit().putString(SP_SURVEY_KEY_FOLLOWUP_SETS, res).commit();
        
        return res;
    }
    
    public static String getExistingFollowupSchedules(Context context){
        Log_debug(TAG, "get Followup Schedule from existing. ");
        return getSP(context, SP_SURVEY).getString(SP_SURVEY_KEY_FOLLOWUP_SETS, "");
    }
    
    
    public static void scheduleFollowups(Context context, String time_sets, int surveyType){
        
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Calendar now = Calendar.getInstance();

        //write to file followup schedule
        String strArr[] = time_sets.split(",");

        if(strArr.length != 1){//##??

            int i = 0;
            boolean flag = false;
            for(String str: strArr){
                i++;
                long time = Long.parseLong(str);

                Calendar r = Calendar.getInstance();
                r.setTimeInMillis(time);

                if(r.after(now)){
                    Intent itTrigger = new Intent(BD_ACTION_SURVEY_TRIGGER_FOLLOW);
                    itTrigger.putExtra(SV_TYPE, surveyType);//SV_TYPE_DRINKING_FOLLOWUP);
                    itTrigger.putExtra(SV_SEQ, i);

                    PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

                    am.cancel(piTrigger);
                    setAlarmExact(am, time, piTrigger);

                    Log.d("Followup Schedule ", "~~~@ "+i+" "+str+" "+r.get(Calendar.HOUR_OF_DAY)+":"+r.get(Calendar.MINUTE)+":"+r.get(Calendar.SECOND));
                    
                    //flag
                    flag = true;
                }
            }
            
            
            
            if(flag){
                SharedPreferences  sp = getSP(context, SP_SURVEY);
                sp.edit().putString(SP_SURVEY_KEY_FOLLOWUP_SETS, time_sets).commit();
                
                sp.edit().putBoolean(SP_SURVEY_KEY_FLAG_CYCLE, true).commit();
            }
            else{
                cancelFollowups(context, surveyType);
            }
        }
    }
    
    public static void cancelFollowups(Context context, int surveyType){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        String sets[] = getExistingFollowupSchedules(context).split(",");
        int num = sets.length + 1;
        for(int i = 1; i < num; i++){
            Intent itTrigger = new Intent(BD_ACTION_SURVEY_TRIGGER_FOLLOW);
            itTrigger.putExtra(SV_TYPE, surveyType);
            itTrigger.putExtra(SV_SEQ, i);

            PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

            am.cancel(piTrigger);

            cancelSurveyReminders(context, surveyType, i);
        }

        Log_debug(TAG, "---followup surveys canceled");

        SharedPreferences sp = getSP(context, SP_SURVEY);
        sp.edit().remove(SP_SURVEY_KEY_FOLLOWUP_SETS).commit();
        
        sp.edit().remove(SP_SURVEY_KEY_FLAG_CYCLE).commit();
    }
    
    public static void removeCycleFlag(Context context){
        getSP(context, SP_SURVEY).edit().remove(SP_SURVEY_KEY_FLAG_CYCLE).commit();
    }
    
    public static void reScheduleFollowups(Context context){
        
    }
    
    public static boolean isInCycle(Context context){
        return getSP(context, SP_SURVEY).getBoolean(SP_SURVEY_KEY_FLAG_CYCLE, false);
    }

    /*************************************************************************************************************/
    /* random */

    public static String getNewRandomSchedules(Context context, boolean startFromNoon, boolean systemTriggered){
        Log_debug(TAG, "get Random Schedule : create new start from noon? " + startFromNoon);

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

            strArr[i] = dtF.format(c.getTime());
            i++;
        }

        try {
            //craving gonna be different
            writeEventToFile(context, (systemTriggered ? CODE_SCHEDULE_AUTOMATIC : CODE_SCHEDULE_MANUALLY),
                    strArr[0], strArr[1], strArr[2], strArr[3], strArr[4], strArr[5]);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return random_schedule;
    }

    public static String getExistRandomSchedules(Context context){
        Log_debug(TAG, "get Random Schedule from existing. ");
        return getSP(context, SP_SURVEY).getString(SP_SURVEY_KEY_RANDOM_SETS, "");
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
                    Intent itTrigger = new Intent(BD_ACTION_SURVEY_TRIGGER_RANDOM);
                    itTrigger.putExtra(SV_TYPE, SV_TYPE_RANDOM);
                    itTrigger.putExtra(SV_SEQ, i);

                    PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

                    am.cancel(piTrigger);
                    setAlarmExact(am, time, piTrigger);

                    Log.d("Random Schedule ", "each time is "+i+" "+str+" "+r.get(Calendar.HOUR_OF_DAY)+":"+r.get(Calendar.MINUTE)+":"+r.get(Calendar.SECOND));
                }
            }
        }

        SharedPreferences sp = getSP(context, SP_SURVEY);
        sp.edit().putString(SP_SURVEY_KEY_RANDOM_SETS, time_sets).commit();

        sp.edit().putLong(SP_SURVEY_KEY_FLAG_ACTIVATE, Calendar.getInstance().getTimeInMillis()).commit();

    }



    public static void cancelRandomSurvey(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int num = MAX_TRIGGER_RANDOM + 1;//7
        for(int i = 1; i < num; i++){
            Intent itTrigger = new Intent(BD_ACTION_SURVEY_TRIGGER_RANDOM);
            itTrigger.putExtra(SV_TYPE, SV_TYPE_RANDOM);
            itTrigger.putExtra(SV_SEQ, i);

            PendingIntent piTrigger = PendingIntent.getBroadcast(context, i, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

            am.cancel(piTrigger);

            cancelSurveyReminders(context, SV_TYPE_RANDOM, i);
        }

        Log_debug(TAG, "---random surveys canceled");

        SharedPreferences sp = getSP(context, SP_SURVEY);
        sp.edit().remove(SP_SURVEY_KEY_RANDOM_SETS).commit();

//        sp.edit().remove(SP_SURVEY_KEY_FLAG_ACTIVATE).commit();//leave not delete until delete user id...
    }


    public static void reScheduleRandomSurvey(Context context){

        scheduleRandomSurvey(context, getExistRandomSchedules(context));
    }





    /*************************************************************************************************************/
    /*suspension*/

    public static void scheduleSuspension(Context context, long expire){
        //set suspension alarm
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent suspensionIntent = new Intent(BD_ACTION_SUSPENSION);

        PendingIntent piSuspension = PendingIntent.getBroadcast(context, 0, suspensionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piSuspension);
        setAlarmExact(am, expire, piSuspension);

        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(expire);
            Log_debug(TAG, "---suspension scheduled @time " + dtF.format(tempc.getTime()) + " for seconds: " + SUSPENSION_INTERVAL_IN_SECONDS);
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

        Intent suspensionIntent = new Intent(BD_ACTION_SUSPENSION);

        PendingIntent piSuspension = PendingIntent.getBroadcast(context, 0, suspensionIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piSuspension);

        Log_debug(TAG, "---suspension canceled");

        //write
        SharedPreferences sp = getSP(context, SP_SURVEY);

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_SUSPENSION_START, 0));//protect 0

        Calendar c2 = Calendar.getInstance();
        if(!writeCurrentDatetime){
            c2.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_SUSPENSION_EXPIRE, 0));//protect 0
        }

        writeEvent(context, CODE_SUSPENSION,
                "", "", "", "",
                dtF.format(c.getTime()), dtF.format(c2.getTime()));

        resetSuspensionFlag(context);
    }

    public static void setSuspensionFlag(Context context, long datetime){
        SharedPreferences sp = getSP(context, SP_SURVEY);
        sp.edit().putLong(SP_SURVEY_KEY_SUSPENSION_EXPIRE, datetime).commit();

    }

    public static void resetSuspensionFlag(Context context){
        SharedPreferences sp = getSP(context, SP_SURVEY);
        sp.edit().remove(SP_SURVEY_KEY_SUSPENSION_EXPIRE).commit();

        sp.edit().remove(SP_SURVEY_KEY_SUSPENSION_START).commit();

    }

    public static boolean isSuspensionFlag(Context context){

        SharedPreferences sp = getSP(context, SP_SURVEY);
        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();

        if(!sp.contains(SP_SURVEY_KEY_SUSPENSION_EXPIRE)){
            return false;
        }
        else{
            expire.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_SUSPENSION_EXPIRE, 0));
            Log.d(TAG, dtF.format(expire.getTime()));
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
        SharedPreferences sp = getSP(context, SP_SURVEY);

        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();

        if(!sp.contains(SP_SURVEY_KEY_SUSPENSION_EXPIRE)){
            // do nothing
            Log_debug(TAG, "reschedule suspension ~ do nothing");
        }
        else{
            expire.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_SUSPENSION_EXPIRE, 0));
            if(now.before(expire)){
                //schedule
                Log_debug(TAG, "reschedule suspension ~ schedule @ "+dtF.format(expire.getTime()));
                long datetime = sp.getLong(SP_SURVEY_KEY_SUSPENSION_EXPIRE, 0);
                scheduleSuspension(context, datetime);
            }
            else{
                //cancel and write break suspension ###
                Log_debug(TAG, "reschedule suspension ~ cancel and write ###");
                cancelSuspension(context, false);
            }
        }
    }





    /*************************************************************************************************************/
    /*Survey scheduler*/

    public static void scheduleSurveyIsolater(Context context, long start){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        long time = start+500;

        Intent itTrigger =  new Intent(BD_ACTION_SURVEY_ISOLATE);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);
        setAlarmExact(am, time + SURVEY_ISOLATE_IN_SECONDS * 1000, piTrigger);

        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(time + SURVEY_ISOLATE_IN_SECONDS * 1000);
            Log_debug(TAG, "---isolater scheduled @time " + dtF.format(tempc.getTime()) + " for seconds: " + SURVEY_ISOLATE_IN_SECONDS);
        }

        setIsolateFlag(context, time);
    }

    public static void cancelSurveyIsolater(Context context){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent itTrigger =  new Intent(BD_ACTION_SURVEY_ISOLATE);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, 0, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);

        Log_debug(TAG, "---isolater canceled");

        resetIsolateFlag(context);
    }

    public static void setIsolateFlag(Context context, long datetime){
        getSP(context, SP_SURVEY).edit().putLong(SP_SURVEY_KEY_ISOLATE_START, datetime).commit();
    }

    public static void resetIsolateFlag(Context context){
        getSP(context, SP_SURVEY).edit().remove(SP_SURVEY_KEY_ISOLATE_START).commit();
    }

    public static boolean isIsolateFlag(Context context){

        SharedPreferences sp = getSP(context, SP_SURVEY);
        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();

        if(!sp.contains(SP_SURVEY_KEY_ISOLATE_START)){
            return false;
        }
        else{
            expire.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_ISOLATE_START, 0) + SURVEY_ISOLATE_IN_SECONDS * 1000);
            Log.d(TAG, dtF.format(expire.getTime()));
            if(now.before(expire)){//not after
                return true;
            }
            else{
                return false;
            }
        }
    }


    /**
     * when app reboot, check if there is any survey isolater at the time phone shut down.
     * if it still under that time period, set isolater again,
     * if it expired, clear the isolater flag.
     */
    public static void reScheduleSurveyIsolater(Context context){
        SharedPreferences sp = getSP(context, SP_SURVEY);

        Calendar now = Calendar.getInstance();
        Calendar expire = Calendar.getInstance();

        if(!sp.contains(SP_SURVEY_KEY_ISOLATE_START)){
            // do nothing
            Log_debug(TAG, "reschedule survey isolater ~ do nothing");
        }
        else{
            expire.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_ISOLATE_START, 0) + SURVEY_ISOLATE_IN_SECONDS * 1000);
            if(now.before(expire)){
                //schedule
                Log_debug(TAG, "reschedule survey isolater ~ schedule @ "+dtF.format(expire.getTime()));
                long datetime = sp.getLong(SP_SURVEY_KEY_ISOLATE_START, 0);
                scheduleSurveyIsolater(context, datetime);
            }
            else{
                //cancel and clear survey isolater ###
                Log_debug(TAG, "reschedule survey isolater ~ cancel and write ###");
                cancelSurveyIsolater(context);
            }
        }
    }



    /*************************************************************************************************************/
    /*--*/

    public static void scheduleSurveyTimeout(Context context, int surveyType, int surveySeq){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Calendar c = Calendar.getInstance();
        long time = c.getTimeInMillis()+500;

        int seq = SurveyActivity.REMIND_TIMEOUT;
        Intent itTrigger = getReminderIntent(surveyType, surveySeq, seq);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);
        setAlarmExact(am, time + SURVEY_TIMEOUT_IN_SECONDS * 1000, piTrigger);

        if(DEBUG){
            Calendar tempc = Calendar.getInstance();
            tempc.setTimeInMillis(time + SURVEY_TIMEOUT_IN_SECONDS * 1000);
            Log_debug(TAG, "---timeout scheduled @time " + dtF.format(tempc.getTime()) + " for seconds: " + SURVEY_TIMEOUT_IN_SECONDS);
        }

    }

    public static void cancelSurveyTimeout(Context context, int surveyType, int surveySeq){
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int seq = SurveyActivity.REMIND_TIMEOUT;
        Intent itTrigger = getReminderIntent(surveyType, surveySeq, seq);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(piTrigger);

        Log_debug(TAG, "---timeout canceled");

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
            setAlarmExact(am, time + (i-1) * SURVEY_REMINDS_IN_SECONDS * 1000, piTrigger);

            if(DEBUG){
                Calendar tempc = Calendar.getInstance();
                tempc.setTimeInMillis(time + (i-1) * SURVEY_REMINDS_IN_SECONDS * 1000);
                Log_debug(TAG, "---reminder scheduled seq "+i+" @time " + dtF.format(tempc.getTime()));
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

        Log_debug(TAG, "---reminder canceled");
    }

    public static Intent getReminderIntent(int surveyType, int surveySeq, int seq){
        Intent i;
        if(surveyType == SV_TYPE_RANDOM)
            i = new Intent(BD_ACTION_SURVEY_REMINDS_RANDOM);
        else
            i = new Intent(BD_ACTION_SURVEY_REMINDS_FOLLOW);
        
        i.putExtra(SV_TYPE, surveyType);
        i.putExtra(SV_SEQ, surveySeq);
        i.putExtra(SV_REMIND_SEQ, seq);

        return i;
    }




    /*************************************************************************************************************/
    /*Morning & bedtime*/

    public static void scheduleMorningSurvey(Context context, long expire){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int seq = 0;

        Intent itTrigger = new Intent(BD_ACTION_SURVEY_TRIGGER_RANDOM);//using random action but with seq = 0
        itTrigger.putExtra(SV_TYPE, SV_TYPE_MORNING);
        itTrigger.putExtra(SV_SEQ, seq);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(piTrigger);
        setAlarmExact(am, expire, piTrigger);

        Log_debug(TAG, "---MorningSruvey scheduled at "+getTimeFromLong(expire)+" context "+context.hashCode());

        getSP(context, SP_BEDTIME).edit().putLong(SP_BEDTIME_KEY_LONG, expire).commit();

    }

    public static void cancelMorningSurvey(Context context){

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        int seq = 0;

        Intent itTrigger = new Intent(BD_ACTION_SURVEY_TRIGGER_RANDOM);//using random action but with seq = 0
        itTrigger.putExtra(SV_TYPE, SV_TYPE_MORNING);
        itTrigger.putExtra(SV_SEQ, seq);

        PendingIntent piTrigger = PendingIntent.getBroadcast(context, seq, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

        am.cancel(piTrigger);
        cancelSurveyReminders(context, SV_TYPE_MORNING, seq);

        Log_debug(TAG, "---MorningSruvey canceled");

        getSP(context, SP_BEDTIME).edit().remove(SP_BEDTIME_KEY_LONG).commit();
    }



    /**
     * This is called when you not sure whether or not it's right time to schedule morning survey.
     * like what it needs to restore from reboot. (x)
     * reboot only
     */
    public static void rescheduleMorningSurvey(Context context){//schedule all
        SharedPreferences sp = getSP(context, SP_BEDTIME);

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
        expire.setTimeInMillis(sp.getLong(SP_BEDTIME_KEY_LONG, defTime));

        debugDT("deftime", defTime);
        debugDT("exprire", expire.getTimeInMillis());

        //set morning & schedule random
        if(c.after(n) || isTodayActivated(context)){
            Log_debug(TAG, "reschedule morning ~ after noon");
            morningComplete(context, true, true);
        }

        //before previous set morning time & before noon
        else if(c.before(expire)){
            Log_debug(TAG, "reschedule morning ~ schedule @ "+dtF.format(expire.getTime()));

            //if not activate today
            scheduleMorningSurvey(context, expire.getTimeInMillis());
        }
        //after morning time but earlier than noon
        else{
            //do nothing
            Log_debug(TAG, "reschedule morning ~ do nothing waiting user manually do morning survey");
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

        SharedPreferences sp = getSP(context, SP_BEDTIME);
        int hour = defHour;
        int minute = defMinute;

        boolean setDefault = (sp.getInt(SP_BEDTIME_KEY_HOUR, -1) == -1?false:true);
        if(setDefault){
            hour = sp.getInt(SP_BEDTIME_KEY_HOUR, -1);
            minute = sp.getInt(SP_BEDTIME_KEY_MINUTE, -1);
        }

        return getProperMorningScheduleTime(hour, minute);
    }


    public static String getTimeFromLong(long l){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(l);

        Calendar t = Calendar.getInstance();
        t.set(Calendar.HOUR_OF_DAY, 0);
        t.set(Calendar.MINUTE, 1);
        t.set(Calendar.SECOND, 0);

        if(c.after(t)){
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumIntegerDigits(2);

            return nf.format(c.get(Calendar.HOUR_OF_DAY))+":"+nf.format(c.get(Calendar.MINUTE));
        }
        else{
            return TIME_NONE;
        }
    }




    /*************************************************************************************************************/


    public static void activate(Context context, boolean startFromNoon, boolean systemTriggered){

        if(hasTodayActivated(context)){
            if(isTodayActivated(context)){
                reScheduleRandomSurvey(context);
            }
            else{
                //do nothing
                //modify on Aug 3, for tag Activited not been cleaned when device is shutdown, overnight.
                scheduleRandomSurvey(context, getNewRandomSchedules(context, startFromNoon, systemTriggered));
            }
        }
        else{
            scheduleRandomSurvey(context, getNewRandomSchedules(context, startFromNoon, systemTriggered));
        }

        //restart gps
        restartRecordingLocation(context);

    }


    public static void deActivate(Context context){
        cancelRandomSurvey(context);
        
        cancelFollowups(context, 4);//base on requirement
        cancelFollowups(context, 6);
        cancelFollowups(context, 7);
    }


    /**
     * Test if today is activated.
     * it will test is today has been activated before, if so, then test if random schedule set is there?
     * Then indicates today is now being activated.
     * @param context
     * @return
     */
    public static boolean isTodayActivated(Context context){
        SharedPreferences sp = getSP(context, SP_SURVEY);
        if(!hasTodayActivated(context)){
            return false;
        }
        else{
            if(sp.contains(SP_SURVEY_KEY_RANDOM_SETS)){
                
                Calendar now = Calendar.getInstance();
                
                //write to file random schedule
                String strArr[] = sp.getString(SP_SURVEY_KEY_RANDOM_SETS, "").split(",");

                if(strArr.length != 1){

                    long time = Long.parseLong(strArr[0]);

                    Calendar r = Calendar.getInstance();
                    r.setTimeInMillis(time);

                    Util.Log_debug(TAG, "random set date is "+r.get(Calendar.MONTH)+1+"/"+r.get(Calendar.DAY_OF_MONTH)+"/"+r.get(Calendar.YEAR));
                    if(now.get(Calendar.HOUR_OF_DAY) < 3){
                        if(now.get(Calendar.DAY_OF_YEAR) == r.get(Calendar.DAY_OF_YEAR)+1){
                            return true;
                        }
                        else{
                            return false;
                        }
                    }
                    else{
                        if(now.get(Calendar.DAY_OF_YEAR) == r.get(Calendar.DAY_OF_YEAR)){
                            return true;
                        }
                        else{
                            return false;
                        }
                    }
                }
                else{
                    return false;
                }
            }
            else{
                return false;
            }
        }
    }

    
    /**
     * @return
     */
    public static boolean hasTodayActivated(Context context){

        SharedPreferences sp = getSP(context, SP_SURVEY);
        if(!sp.contains(SP_SURVEY_KEY_FLAG_ACTIVATE)){
            return false;
        }
        else{
            Calendar now = Calendar.getInstance();
            Calendar day = Calendar.getInstance();
            day.setTimeInMillis(sp.getLong(SP_SURVEY_KEY_FLAG_ACTIVATE, 0));
//            Log.d(TAG, dtF.format(day.getTime())+" "+sp.getLong(SP_SURVEY_KEY_FLAG_ACTIVATE, 0));
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
     * use this only when noon and reboot, not appropriate for every time MainActivity start
     * @param context
     */
    public static void morningComplete(Context context, boolean startFromNoon, boolean systemTriggered){

        activate(context, startFromNoon, systemTriggered);

        cancelDaemonNoon(context);

        cancelMorningSurvey(context);
        
        //cancel Followup in case flag is not clear before this
        cancelFollowups(context, 4);
        cancelFollowups(context, 6);
        cancelFollowups(context, 7);
        
    }


    private static void cancelDaemonNoon(Context context) {
        // TODO Auto-generated method stub
        Intent i = new Intent(BD_ACTION_DAEMON);
        i.putExtra(DaemonReceiver.BD_ACTION_DAEMON_FUNC, -1);
        context.sendBroadcast(i);
    }


    /**
     * deactivate and cancel surveys today,
     * schedule for next morning survey.
     */
    public static void bedtimeComplete(Context context, long expire){

        deActivate(context);

        scheduleMorningSurvey(context, expire);
    }



    /*Daemon*/

    public static void scheduleDaemon(Context context){
        Intent i = new Intent(BD_ACTION_DAEMON);
        i.putExtra(DaemonReceiver.BD_ACTION_DAEMON_FUNC, 0);
        context.sendBroadcast(i);
    }


    /*GPS location*/

    /**
     * start recording location with condition checking first
     * check if today is activated (from activated time to 3am next day)
     */
    public static void restartRecordingLocation(Context context){
        if(isTodayActivated(context) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
            startRecordingLocation(context);
        }
    }

    public static void startRecordingLocation(Context context){
        context.sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
    }

    public static void stopRecordingLocation(Context context){
        context.sendBroadcast(new Intent(LocationUtilities.ACTION_STOP_LOCATION));
    }

    private static String getNameFromType(int activityType) {
        switch(activityType) {
            case DetectedActivity.IN_VEHICLE:
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "on_bicycle";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.UNKNOWN:
                return "unknown";
            case DetectedActivity.TILTING:
                return "tilting";

        }
        return "unknown";
    }

    /*************************************************************************************************************/



    /*for about*/
    public static String getScheduleForToady(Context context){
        //morning
        long morning = getSP(context, SP_BEDTIME).getLong(SP_BEDTIME_KEY_LONG, -1);

        //follow-ups
        String strFollowup[] = getSP(context, SP_SURVEY).getString(SP_SURVEY_KEY_FOLLOWUP_SETS, "").split(",");
        String follow = "";
        if(strFollowup.length != 1){
            for(String s: strFollowup){
                if(getTimeFromLong(Long.parseLong(s)).equals(TIME_NONE)){
                    follow = TIME_NONE;
                    break;
                }
                follow += getTimeFromLong(Long.parseLong(s));
                follow += ", ";
            }
        }
        else{
            follow = TIME_NONE;
        }

        //random
        String strRandom[] = getSP(context, SP_SURVEY).getString(SP_SURVEY_KEY_RANDOM_SETS, "").split(",");
        String random = "";
        if(strRandom.length != 1){
            for(String s: strRandom){
                if(getTimeFromLong(Long.parseLong(s)).equals(TIME_NONE)){
                    random = TIME_NONE;
                    break;
                }
                random += getTimeFromLong(Long.parseLong(s));
                random += ", ";
            }
        }
        else{
            random = TIME_NONE;
        }

        String str =
                "\nStudy Day: "+getStudyDay(context) +
                //(!getSP(context, SP_SURVEY).getBoolean(SP_SURVEY_KEY_SUSPENSION, false)?"\n":"\nUnder suspension.\n") +
                (RELEASE? "" :
                "\nMorning  survey at: " + (morning == -1 ? TIME_NONE : getTimeFromLong(morning))+
                "\nFollowup survey at: " + follow +
                "\nRandom  survey at: "+random);

        return str;
    }


    public static int getStudyDay(Context context){
        String startStr = getSP(context, SP_LOGIN).getString(SP_LOGIN_KEY_STUDY_STARTTIME, "");
        if(!startStr.equals("")){
            long start = Long.parseLong(startStr);
            long current = Calendar.getInstance().getTimeInMillis();

            Calendar s = Calendar.getInstance();
            s.setTimeInMillis(start);
            s.set(Calendar.HOUR_OF_DAY, 3);
            s.set(Calendar.MINUTE, 0);
            s.set(Calendar.SECOND, 0);
            s.set(Calendar.MILLISECOND, 0);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(current);
//          c.set(Calendar.HOUR_OF_DAY, 12);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

            start = s.getTimeInMillis();
            current = c.getTimeInMillis();

            return (int) ((current - start) / (24*60*60*1000));
        }else{
            return -1;
        }
    }






















    /*************************************************************************************************************/
    /*writer*/

    public static void writeLocationToFile(PublicKey publicKey, Location location) throws IOException{

        String toWrite;
        Calendar cal=Calendar.getInstance();

        toWrite = String.valueOf(cal.getTime())+","+
            location.getLatitude()+","+location.getLongitude()+","+
            location.getAccuracy()+","+location.getProvider()+","+getNameFromType(ActivityRecognitionService.currentUserActivity);


        String userID = LocationBroadcast.ID;
        //filename
        Calendar cl=Calendar.getInstance();
        String dateObj =mdF.format(cl.getTime());

        StringBuilder prefix_sb = new StringBuilder(PREFIX_LEN);
        String prefix = "locations." + userID + "." + dateObj;
        prefix_sb.append(prefix);

        for (int i = prefix.length(); i <= PREFIX_LEN; i++) {
            prefix_sb.append(" ");
        }


        //danick
        String toWriteArr = null;
        try {
            toWriteArr = encryption(publicKey, prefix_sb.toString() + toWrite);
            if(WRITE_RAW){
                writeToFile("Location."+userID+"."+dateObj+".txt", toWrite);
            }else{
                writeToFileEnc("Location." + userID + "." + dateObj + ".txt", toWriteArr);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


        //Ricky
        TransmitData transmitData = new TransmitData();
        transmitData.execute(toWriteArr);

    }


    /*
     * for survey event
     * make-ups for random seq
     */
    public static void writeEvent(Context context, int type, String status, int randomSeq, String scheduleTS, String r3, String startTS, String endTS){
        String seq = "";
        if(type == SV_TYPE_RANDOM){
            seq = "," + randomSeq;
        }

        try {
            writeEventToFile(context, getSurveyCode(type)+status, scheduleTS, r3, startTS, endTS + seq);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /*
     * for control event
     */
    public static void writeEvent(Context context, String code, String scheduleTS, String r1, String r2, String r3, String startTS, String endTS){
        try {
            writeEventToFile(context, code, scheduleTS, r1, r2, r3, startTS, endTS);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    public static void writeEventToFile(Context context, String code, String scheduleTS, String r1, String r2, String r3, String startTS, String endTS) throws IOException{
        writeEventToFile(context, code, scheduleTS, r1+","+r2+","+r3, startTS, endTS);
    }


    //upload
    public static void writeEventToFile(Context context, String code, String scheduleTS, String r3, String startTS, String endTS) throws IOException{
        Log.d("###", "write event to file");

        Calendar endCal = Calendar.getInstance();

        String userID = getSP(context, SP_LOGIN).getString(SP_LOGIN_KEY_USERID, "");
        int studyDay = getStudyDay(context);


        StringBuilder sb = new StringBuilder(100);

//      Calendar c = Calendar.getInstance();
//      c.setTimeInMillis(time);
        sb.append(endCal.getTime().toString());
        sb.append(",");

        sb.append(userID+","+studyDay+","+code+","+scheduleTS+","+r3+","+startTS+","+endTS+",");
//      sb.append("\n");

        Calendar cl = Calendar.getInstance();
        String dateObj = mdF.format(cl.getTime());

        StringBuilder prefix_sb = new StringBuilder(PREFIX_LEN);
        String prefix = "Excel." + userID + "." + dateObj;
        prefix_sb.append(prefix);

        for (int i = prefix.length(); i <= PREFIX_LEN; i++) {
            prefix_sb.append(" ");
        }

        // file name for backup upload
        //Added by nick and Haidong on May 26 2015
        String backup_file_name = "SurveyData."+userID+".txt";

        /*
         * Chen
         *
         * Data encryption
         * Stringbuilder sb -> String ensb
         */
        String ensb = null;
        try {
            ensb = encryption(context, prefix_sb.toString() + sb.toString());
            if(WRITE_RAW){
                writeToFile("Event."+userID+".txt", sb.toString());
                
                Util.writeToFile(backup_file_name, prefix_sb.toString() + sb.toString());
            }else{
                writeToFileEnc("Event."+userID+".txt", ensb);
                
                Util.writeToBackupFileEnc(backup_file_name, ensb);
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Ricky 2013/12/09
        TransmitData transmitData=new TransmitData();
        transmitData.execute(ensb);

    }


    public static PublicKey getPublicKey(Context context) throws Exception {
        // TODO Auto-generated method stub
        InputStream is = context.getResources().openRawResource(R.raw.publickey);
        ObjectInputStream ois = new ObjectInputStream(is);

        BigInteger m = (BigInteger)ois.readObject();
        BigInteger e = (BigInteger)ois.readObject();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);


        KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
        PublicKey pubKey = fact.generatePublic(keySpec);

        return pubKey;
    }


    public static String encryption(Context context, String string) throws Exception {
        return encryption(getPublicKey(context), string);
    }


    //Chen
    public static String encryption(PublicKey publicKey, String string) throws Exception {
        // TODO Auto-generated method stub

        //generate symmetric key
        KeyGenerator keygt = KeyGenerator.getInstance("AES");
        keygt.init(128);
        SecretKey symkey =keygt.generateKey();

        //get it encoded
        byte[] aes_ba = symkey.getEncoded();

        //create cipher
        SecretKeySpec skeySpec = new SecretKeySpec(aes_ba, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE,skeySpec);

        //encryption
        byte [] EncSymbyteArray =cipher.doFinal(string.getBytes());

        //encrypt symKey with PublicKey
        Key pubKey = publicKey;

        //RSA cipher
        Cipher cipherAsm = Cipher.getInstance("RSA", "BC");
        cipherAsm.init(Cipher.ENCRYPT_MODE, pubKey);

        //RSA encryption
        byte [] asymEncsymKey = cipherAsm.doFinal(aes_ba);

//          File f3 = new File(BASE_PATH,"enc.txt");
//          File f3key = new File(BASE_PATH,"enckey.txt");
//          File f3file = new File(BASE_PATH,"encfile.txt");
//          writeToFile2(f3,f3key,f3file, asymEncsymKey, EncSymbyteArray);

        //byte != new String
        //return new String(byteMerger(asymEncsymKey, EncSymbyteArray));
        return Base64.encodeToString(byteMerger(asymEncsymKey, EncSymbyteArray),Base64.DEFAULT);
    }


    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2){
        byte[] byte_3 = new byte[byte_1.length+byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }


    public static void writeToFile(String fileName, String toWrite) throws IOException{
        File dir =new File(PHONE_BASE_PATH);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(PHONE_BASE_PATH,fileName);
        FileWriter fw = new FileWriter(f, true);
        fw.write(toWrite+'\n');
        fw.flush();
        fw.close();
        f = null;
    }


    public static void writeToFileEnc(String fileName, String toWrite) throws IOException{
        Log_debug("write to file", "enc");
        File dir =new File(PHONE_BASE_PATH);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(PHONE_BASE_PATH,fileName);
        FileWriter fw = new FileWriter(f, true);
        fw.write(toWrite+'\n');
        fw.flush();
        fw.close();
        f = null;
    }

    public static void writeToBackupFileEnc(String fileName, String toWrite) throws IOException{
        Log_debug("write to file", "enc for backup");
        File dir =new File(PHONE_BASE_PATH);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(PHONE_BASE_PATH,fileName);
        FileWriter fw = new FileWriter(f, true);
        fw.write(toWrite);
        fw.flush();
        fw.write(";;;;;;;;;;");
        fw.flush();
        fw.close();
        f = null;
    }


    static class TransmitData extends AsyncTask<String,Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
            // TODO Auto-generated method stub

            String data = strings[0];
            //           String fileName=strings[0];
            //           String dataToSend=strings[1];
//           if(checkDataConnectivity())
            if(true){

                Log.d("((((((((((((((((((((((((", ""+Thread.currentThread().getId());
                HttpPost request = new HttpPost(UPLOAD_ADDRESS);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("data", data));

    //            //file_name
    //            params.add(new BasicNameValuePair("file_name",fileName));
    //            //data
    //            params.add(new BasicNameValuePair("data",dataToSend));
                try {

                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    HttpResponse response = new DefaultHttpClient().execute(request);
                    if(response.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(response.getEntity());
                        Log.d("Sensor Data Point Info",result);
                        // Log.d("Wrist Sensor Data Point Info","Data Point Successfully Uploaded!");
                    }
                    return true;
                }
                catch (Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
            else{
                Log.d("Sensor Data Point Info","No Network Connection:Data Point was not uploaded");
                return false;
            }
        }
    }


    public static boolean checkDataConnectivity(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
