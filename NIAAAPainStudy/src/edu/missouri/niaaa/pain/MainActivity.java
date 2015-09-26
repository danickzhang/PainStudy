package edu.missouri.niaaa.pain;


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.pain.activity.AdminManageActivity;
import edu.missouri.niaaa.pain.activity.MorningScheduler;
import edu.missouri.niaaa.pain.activity.SupportActivity;
import edu.missouri.niaaa.pain.activity.SuspensionTimePicker;
import edu.missouri.niaaa.pain.location.LocationUtilities;
import edu.missouri.niaaa.pain.monitor.MonitorUtilities;
import edu.missouri.niaaa.pain.survey.SurveyMenu;


public class MainActivity extends Activity {
    static String TAG = "MainActivity.java";
    boolean logEnable = true;
    boolean randomSkip = false;

    /*onActivityResult Result Code*/
    static final int INTENT_REQUEST_BLUETOOTH = 1;
    static final int INTENT_REQUEST_MAMAGE = 2;
    static final int INTENT_REQUEST_WAKEUP = 3;
    static final int INTENT_REQUEST_SUSPENSION = 4;


    private Button section_1;
    private Button section_2;
    private Button section_3;
    private Button section_4;
    private Button section_5;
    private Button section_6;
    private Button section_7;
    private Button section_8;
    private Button section_9;

    /*adapter for bluetooth switch*/
    private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

    private SharedPreferences shp = null;
    private InputMethodManager imm = null;

    //backup upload
    static ProgressDialog progressBar;
    static Context mContext;

    //this is for recording hardwareInfo
    //boolean start = false;
    //static boolean resultOnResume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.Log_lifeCycle(TAG, "OnCreate~~~");

        /* thread policy
         * help to check if there is misuse of threads, such as read large files or network communication, that
         * should not be in the main UI thread.
         * Should be bypass when product released */
//        if(!Util.RELEASE){
        if(true){
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            //.detectAll()
//            .detectNetwork()
//            .detectCustomSlowCalls()
//            .detectDiskWrites()
            .permitAll()
            .build());
        }

        /* initialization
         * set initial parameters, register broadcasts,
         * but not time-consuming tasks such as animation or file reading cursor reading;
         * unregister broadcasts @onDestroy, put time-consuming tasks @onResume*/

        shp = getSharedPreferences(Util.SP_LOGIN, Context.MODE_PRIVATE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setContentView(R.layout.activity_main);

        setListeners();

        registerReceiver(suspensionReceiver, new IntentFilter(Util.BD_ACTION_SUSPENSION));
        
        prepareRecording();

    }


    private void prepareRecording() {
        // TODO Auto-generated method stub
        
        /* this is for the hardware info recording
         * in order to determine if the app has already sent the phone has started to the server 
         * instead of having a global activity variable 
         */
        boolean actualOnCreate = shp.getBoolean(MonitorUtilities.ACTION_ONDESTROY, false);
        if(actualOnCreate){
            MonitorUtilities.makeSharedPreferencesMonitorOnStartToFalse(shp);
        }
        
        
        /* this is for the hardware info TimeChangedReceiver class
         * in order to determine the current difference from the current time
         * in order to calculate the previous time after the user changes the system clock 
         */
        MonitorUtilities.timeDifferenceOnStartup = MonitorUtilities.getCurrentTimeDifference();
    }


    @Override
    protected void onResume() {
        super.onResume();
        Util.Log_lifeCycle(TAG, "OnResume~~~");

        /*read shared resources*/
        setSharedValue();

        /* check if ID is assigned
         * if not, jump to admin manage page and FINISH with this
         * if yes, go and restore all the schedules and status
         * then, check if PWD is assigned
         * */
        checkUserStatus();

        //recording
//        recordingOnResume();

        //
        setSuspensionText();
//        setServiceText();
    }


    /**
     * ID is null - go admin and set one
     * PWD is null - set user pin
     * both ok - start normal procedure
     */
    private void checkUserStatus() {
        // TODO Auto-generated method stub
        //check if device is assigned with an ID
        String ID = shp.getString(Util.SP_LOGIN_KEY_USERID, "");
        String PWD = shp.getString(Util.SP_LOGIN_KEY_USERPWD, "");

        Util.Log_debug(TAG, "assigned user ID is "+ID);

        if(ID.equals("")){
            adminManagePage();
            imm.toggleSoftInput(0, InputMethodManager.RESULT_HIDDEN);

        }else if(PWD.equals("")){
            //set password
            userPinSetDialog(this, ID).show();

        }else{
            Util.Log_debug(TAG, "assigned user PWD is "+PWD);

            //with full ID and PWD

            /*is app launched by RebootReceiver*/
            if(getIntent().getBooleanExtra(RebootReceiver.REBOOT, false)){
                Util.Log_debug(TAG, "app is just launched by RebootReceiver");

                restoreStatusForTheFirstTime();//contain restoreStatus()

            }else{
                restoreStatus();
            }

        }
    }


    /**
     * jump to admin manage page for assign ID
     * return OK if id assigned
     */
    private void adminManagePage(){
        Intent intent = new Intent(this, AdminManageActivity.class);
        startActivityForResult(intent, INTENT_REQUEST_MAMAGE);
    }


    /**
     * each time MainActivity onResume will call this.
     * to restore, must check flag like "activated" or "suspension" first.
     */
    private void restoreStatus() {
        // TODO Auto-generated method stub

//        /*check suspension status*/
//        Util.reScheduleSuspension(MainActivity.this);
//
//        /*check survey isolater status*/
//        Util.reScheduleSurveyIsolater(MainActivity.this);
//
//        /*schedule*/
////        Util.rescheduleMorningSurvey(MainActivity.this);
//        
//        if(after 12:20)
//        Util.activate(MainActivity.this, true, true);
        
        //daemon


        //restart gps
        if(Util.isTodayActivated(this) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
            sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
        }

        //recording
//        Intent i = new Intent(MainActivity.this, RecordingService.class);
//        startService(i);

        MonitorUtilities.scheduleRecording(MainActivity.this);
        Log.d(TAG, "onCreate is scheduling Monitor Recording");

    }

    /**
     * Similar with {@link #restoreStatus()}, but this is called for
     * the first time ID is assigned
     */
    private void restoreStatusForTheFirstTime(){

        /*check suspension status*/
        Util.reScheduleSuspension(MainActivity.this);

        /*check survey isolater status*/
        Util.reScheduleSurveyIsolater(MainActivity.this);

        /*schedule*/
        Util.rescheduleMorningSurvey(MainActivity.this);

        //followup
        Util.cancelFollowups(MainActivity.this, 4);
        Util.cancelFollowups(MainActivity.this, 6);
        Util.cancelFollowups(MainActivity.this, 7);
        
        //daemon
        Util.scheduleDaemon(MainActivity.this);
        
        //
//      startSService();

        //input mediction
//        inputMedicationDialog(MainActivity.this).show();
        inputMedNameForTheFirstTime();
        
        restoreStatus();
    }

    private void inputMedNameForTheFirstTime(){
        if(!shp.getBoolean(Util.SP_LOGIN_HAD_INPUT, false)){
            inputMedicationDialog(MainActivity.this).show();
            shp.edit().putBoolean(Util.SP_LOGIN_HAD_INPUT, true).commit();
        }
    }

    private void setSharedValue(){

        //ID

    }


    private Dialog userPinSetDialog(Context context, final String ID) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View textEntryView = inflater.inflate(R.layout.pin_input, null);
        TextView pinText = (TextView) textEntryView.findViewById(R.id.pin_text);
        pinText.setText(getString(R.string.user_setpwd_msg)+ID);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.user_setpwd_title);
        builder.setView(textEntryView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText pinEdite = (EditText) textEntryView.findViewById(R.id.pin_edit);
                String pinStr = pinEdite.getText().toString();
                Util.Log_debug("Pin Dialog", "pin String is "+pinStr);

                String data = null;
                try {
                    data = Util.encryption(MainActivity.this, ID + "," + "3" + "," + pinStr);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

/*              check network*/

/*              prepare params for server*/
                HttpPost request = new HttpPost(Util.VALIDATE_ADDRESS);

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("data", data));

                //              //file_name
                //              params.add(new BasicNameValuePair("userID",ID));
                //              //function
                //              params.add(new BasicNameValuePair("pre","3"));
                //              //data
                //              params.add(new BasicNameValuePair("password",pinStr));

/*              check identity*/

                try {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

                    HttpResponse response = new DefaultHttpClient().execute(request);
                    if(response.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(response.getEntity());
                        Log.d("~~~~~~~~~~http post result3 ",result);

                        if(result.equals("NewUserIsCreated")){
                            //new pwd created
                            //format check

                            shp.edit().putString(Util.SP_LOGIN_KEY_USERPWD, pinStr).commit();

                            restoreStatusForTheFirstTime();

                        }else{
                            //imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                            //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                            Toast.makeText(getApplicationContext(), R.string.set_upin_failed, Toast.LENGTH_SHORT).show();
                            //set return code

                            finish();
                        }
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block

                    imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    Toast.makeText(getApplicationContext(), R.string.set_upin_error, Toast.LENGTH_SHORT).show();
                    //set return code

                    finish();
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                finish();
            }
        });

        return builder.create();
    }


    private void setListeners() {
        // TODO Auto-generated method stub

        /*start/stop service*/
        section_1 = (Button) findViewById(R.id.section_label1);
        section_2 = (Button) findViewById(R.id.section_label2);
        /*survey menu*/
        section_3 = (Button) findViewById(R.id.section_label3);
        /*sensor connection*/
        section_4 = (Button) findViewById(R.id.section_label4);
        /*bed time setting*/
        section_5 = (Button) findViewById(R.id.section_label5);
        /*suspension*/
        section_6 = (Button) findViewById(R.id.section_label6);
        /*support*/
        section_7 = (Button) findViewById(R.id.section_label7);
        /*test buttons for debugging*/
        section_8 = (Button) findViewById(R.id.section_label8);
        section_9 = (Button) findViewById(R.id.section_label9);

//        setServiceText();
        section_1.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, logEnable, "section 1 on click listener");

                String act = ((Button) view).getText().toString();
                //start service
                if(act.equals(getString(R.string.section_1))){
//                  ((Button)view).setText(R.string.section_2);

//                    StartSensorLocationService();

                }

                //stop service
                else{
//                  ((Button)view).setText(R.string.section_1);

//                    confirmStopService();
                }

            }
        });

        section_2.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, logEnable, "section 2 on click listener");

            }
        });

        section_3.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, logEnable, "section 3 on click listener");

                if(!Util.isSuspensionFlag(MainActivity.this)){
                    startActivity(new Intent(MainActivity.this, SurveyMenu.class));
                }else{
                    suspensionAlert();
                }

            }
        });

        section_4.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "section 4 on click listener");

//                startActivity(new Intent(getApplicationContext(), SensorConnections.class));
            }
        });

        section_5.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "section 5 on click listener");

                if(!Util.isSuspensionFlag(MainActivity.this)){
                    int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                    if(hour >= 21 || hour <3){
                        //verify user pin
                        userPinCheckDialogForBedtime(MainActivity.this).show();
                    }else{
                        //alert dialog
                        bedTimeWrongDialog();
                    }
                }
                else{
                    suspensionAlert();
                }

                //startActivity(new Intent(getApplicationContext(), MorningScheduler.class));

            }
        });

        setSuspensionText();
        section_6.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "section 6 on click listener");

                if(Util.isTodayActivated(MainActivity.this)){
                    if(section_6.getText().equals(MainActivity.this.getString(R.string.section_6))){
                        Log.d("test text 6", "suspension~~~~~~~~~~~");

                        new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.suspension_title)
                        .setMessage(R.string.suspension_msg)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                Intent intent = new Intent(getApplicationContext(), SuspensionTimePicker.class);
                                startActivityForResult(intent, INTENT_REQUEST_SUSPENSION);
                            }
                        }).create().show();
                    }
                    else{
                        Log.d("test text 6", "break suspension~~~~~~~~~~~");

                        new AlertDialog.Builder(MainActivity.this)
                        .setTitle(R.string.suspension_break_title)
                        .setMessage(R.string.suspension_break_msg)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.cancel, null)
                        .setPositiveButton(android.R.string.ok, new android.content.DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                section_6.setText(R.string.section_6);


                                //write break suspension ###
                                Util.Log_debug(TAG, "### write break suspension");

                                Util.cancelSuspension(MainActivity.this, true);


                                //volume
                                AudioManager audiom = (AudioManager) MainActivity.this.getSystemService(Context.AUDIO_SERVICE);
                                audiom.setStreamVolume(AudioManager.STREAM_MUSIC, Util.VOLUME, AudioManager.FLAG_PLAY_SOUND);

                                Vibrator v = (Vibrator) MainActivity.this.getSystemService(Context.VIBRATOR_SERVICE);
                                v.vibrate(500);
                                Toast.makeText(getApplicationContext(), R.string.suspension_end, Toast.LENGTH_LONG).show();
                            }
                        }).create().show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this, R.string.morning_report_unfinished, Toast.LENGTH_LONG).show();
                }
            }
        });

        section_7.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "section 7 on click listener");

                startActivity(new Intent(MainActivity.this, SupportActivity.class));
            }
        });

        section_8.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "section 8 on click listener");

//                Util.scheduleDaemon(MainActivity.this);
//                Util.rescheduleMorningSurvey(MainActivity.this);
                Util.cancelFollowups(MainActivity.this, 4);
                Util.cancelFollowups(MainActivity.this, 6);
                Util.cancelFollowups(MainActivity.this, 7);
//                Util.getUpdatedFollowupSchedules(MainActivity.this, Util.getExistingFollowupSchedules(MainActivity.this).split(",").length, false);
//                Util.Log_debug(TAG, "in cycle? "+Util.getSP(MainActivity.this, Util.SP_SURVEY).getBoolean(Util.SP_SURVEY_KEY_FLAG_CYCLE, false));
//                Util.scheduleFollowups(MainActivity.this, Util.getUpdatedFollowupSchedules(MainActivity.this, 1, false));
//                Util.cancelRandomSurvey(MainActivity.this);
            }
        });

        section_9.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Util.Log_debug(TAG, "section 9 on click listener");

//                Util.Log_debug(TAG, ""+Util.isIsolateFlag(MainActivity.this));
//                Util.Log_debug(TAG, ""+Util.isSuspensionFlag(MainActivity.this));
//                Util.bedtimeComplete(MainActivity.this, 12, 23);
//                Util.rescheduleMorningSurvey(MainActivity.this);
                
                Util.Log_debug(TAG, ""+Util.isTodayActivated(MainActivity.this));
                Util.Log_debug(TAG, ""+Util.hasTodayActivated(MainActivity.this));
                Util.Log_debug(TAG, ""+Util.isSuspensionFlag(MainActivity.this));
                Util.Log_debug(TAG, ""+Util.isIsolateFlag(MainActivity.this));
                Util.Log_debug(TAG, ""+Util.isInCycle(MainActivity.this));

//                Util.scheduleRandomSurvey(MainActivity.this, Util.getNewRandomSchedules(MainActivity.this, false, true));
//                Util.scheduleFollowups(MainActivity.this, Util.getNewFollowupSchedules(MainActivity.this));

            }
        });

    }

    
//    private void setServiceText(){
//        section_1.setText(SensorLocationService.mIsRunning? getString(R.string.section_2): getString(R.string.section_1));
//    }
//    
//    private void StartSensorLocationService(){
//        Intent i = new Intent(MainActivity.this,SensorLocationService.class);
//        startService(i);
//
//        section_1.setText(getString(R.string.section_2));
//        Toast.makeText(getApplicationContext(), R.string.service_start, Toast.LENGTH_LONG).show();
//    }
//
//    private void StopSensorLocationService(){
//        Intent i = new Intent(MainActivity.this,SensorLocationService.class);
//        stopService(i);
//
//        section_1.setText(getString(R.string.section_1));
//        Toast.makeText(getApplicationContext(), R.string.service_stop, Toast.LENGTH_LONG).show();
//    }
//
//    private void confirmStopService(){
//        Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
//        .setCancelable(false)
//        .setTitle(R.string.service_stop_title)
//        .setMessage(R.string.service_stop_msg)
//        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // TODO Auto-generated method stub
//                StopSensorLocationService();
//            }
//        })
//        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
//
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                // TODO Auto-generated method stub
//
//            }
//        })
//        .create();
//        alertDialog.show();
//    }

    

    private void setSuspensionText(){
        section_6.setText(!Util.isSuspensionFlag(MainActivity.this) ? R.string.section_6:R.string.section_62);
    }

    private void suspensionAlert(){
        Toast.makeText(getApplicationContext(), R.string.suspension_under, Toast.LENGTH_LONG).show();
    }

    private void bedTimeWrongDialog(){
        new AlertDialog.Builder(MainActivity.this)
        .setTitle(R.string.bedtime_title)
        .setMessage(R.string.bedtime_message)
        .setCancelable(false)
        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        })
        .create().show();
    }

    private Dialog userPinCheckDialogForBedtime(final Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View DialogView = inflater.inflate(R.layout.pin_input, null);
        TextView pinText = (TextView) DialogView.findViewById(R.id.pin_text);
        pinText.setText(R.string.pin_message);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.pin_title);
        builder.setView(DialogView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                EditText pinEdite = (EditText) DialogView.findViewById(R.id.pin_edit);
                String pinStr = pinEdite.getText().toString();
                Util.Log_debug("Pin Dialog", "pin String is "+pinStr);

                if (pinStr.equals(Util.getPWD(context))){
                    //Send the intent and trigger new Survey Activity....
                    bedtimeReportConfirmDialog();
                    dialog.cancel();
                }
                else {
                    //New AlertDialog to show instruction.
                    new AlertDialog.Builder(MainActivity.this)
                    .setTitle(R.string.pin_title_wrong)
                    .setMessage(R.string.pin_message_wrong)
                    .setPositiveButton(android.R.string.yes, null)
                    .create().show();
                }

                dialog.cancel();

            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        return builder.create();
    }

    private void bedtimeReportConfirmDialog(){
        new AlertDialog.Builder(MainActivity.this)
        .setTitle(R.string.bedtime_title)
        .setMessage(R.string.bedtime_message_confirm)
        .setCancelable(false)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Intent intent = new Intent(getApplicationContext(), MorningScheduler.class);
                startActivityForResult(intent, INTENT_REQUEST_WAKEUP);

                dialog.cancel();
            }
        })
        .setNegativeButton(R.string.no, null)
        .create().show();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(Config.getMenu(), menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub

        //ENABLE BLUETOOTH
        if (item.getItemId() == R.id.Enable){
            if(btAdapter.isEnabled())           {
                Toast.makeText(getApplicationContext(), R.string.bluetooth_enabled ,Toast.LENGTH_LONG).show();
            }
            else{
                turnOnBluetooth();
            }
            return true;
        }

        //DISABLE BLUETOOTH
        else if (item.getItemId() == R.id.Disable){
            btAdapter.disable();
            Toast.makeText(getApplicationContext(), R.string.bluetooth_disabled, Toast.LENGTH_LONG).show();
            return true;
        }

        //MANAGEMENT
        else if(item.getItemId() == R.id.manage){
            adminManagePage();
        }

        //ABOUT
        else if(item.getItemId() == R.id.about){

            //initial versionCode
            int versionCode = 0;
            String versionName = "";
            PackageInfo pinfo;
            try {
                pinfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
                versionCode = pinfo.versionCode;
                versionName = pinfo.versionName;
            } catch (NameNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            // show current version, which is defined in Android Manifest
            String uid = shp.getString(Util.SP_LOGIN_KEY_USERID, "");
            Dialog alertDialog = new AlertDialog.Builder(MainActivity.this)
            .setCancelable(false)
            .setTitle(getString(R.string.menu_about)+"  ver."+versionName+"."+versionCode)
            .setMessage("User ID: "+uid+"\n"+Util.getScheduleForToady(MainActivity.this))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            })
            .create();
            alertDialog.show();
        }

        //upload backup
        else if(item.getItemId() == R.id.upload){
            Dialog DialadminPin = AdminPinCheckDialog(this, admin_upload);
            DialadminPin.show();
        }
        
        //training mode
        else if(item.getItemId() == R.id.training){
            Dialog DialadminPin = AdminPinCheckDialog(this, admin_training);
            DialadminPin.show();
        }

        //input medication names
        else if(item.getItemId() == R.id.medication){
            Dialog DialadminPin = AdminPinCheckDialog(this, admin_medication);
            DialadminPin.show();
        }
        
        //randomly skip pain follows
        else if(item.getItemId() == R.id.randomskip){
            Dialog DialadminPin = AdminPinCheckDialog(this, admin_randomskip);
            DialadminPin.show();
        }
        
        return super.onOptionsItemSelected(item);
    }


    private void turnOnBluetooth(){
        // TODO Auto-generated method stub
        Intent Enable_Bluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(Enable_Bluetooth, INTENT_REQUEST_BLUETOOTH);
    }


//================================================================================================================================

    private static final int admin_upload = 0;
    private static final int admin_training = 1;
    private static final int admin_medication = 2;
    private static final int admin_randomskip = 3;
    
    /*it's really bad to copy&paste large chunk of code*/
    private Dialog AdminPinCheckDialog(final Context context, final int mode) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View textEntryView = inflater.inflate(R.layout.pin_input, null);
        TextView pinText = (TextView) textEntryView.findViewById(R.id.pin_text);
        pinText.setText(R.string.admin_set_msg);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.admin_set_title);
        builder.setView(textEntryView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {



                EditText pinEdite = (EditText) textEntryView.findViewById(R.id.pin_edit);
                String pinStr = pinEdite.getText().toString();
                Util.Log_debug("Pin Dialog", "pin String is "+pinStr);

                String data = null;
                try {
                    data = Util.encryption(context, Util.ADMIN_UID + "," + "1" + "," + pinStr);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

/*              check network*/

/*              prepare params for server*/
                HttpPost request = new HttpPost(Util.VALIDATE_ADDRESS);

                List<NameValuePair> params = new ArrayList<NameValuePair>();

                params.add(new BasicNameValuePair("data", data));

//              //file_name
//              params.add(new BasicNameValuePair("userID","0000"));
//              //function
//              params.add(new BasicNameValuePair("pre","1"));
//              //data
//              params.add(new BasicNameValuePair("password",pinStr));

/*              check identity*/

                try {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));

                    HttpResponse response = new DefaultHttpClient().execute(request);
                    if(response.getStatusLine().getStatusCode() == 200){
                        String result = EntityUtils.toString(response.getEntity());
                        Log.d("~~~~~~~~~~http post result",result);

                        if(result.equals("AdminIsChecked")){
                            //admin pin verify successfully
                            
                            switch(mode){
                            case admin_upload:
                                uploadSurveyData();
                                break;
                            case admin_training:
                                TrainingMenu();
                                break;
                            case admin_medication:
                                InputMedication(context);
                                break;
                            case admin_randomskip:
                                RandomSkipSwitcher(context);
                                break;
                                
                            default:
                                //do nothing
                                break;
                            }

                        }else if(result.equals("AdminPinIsInvalid")){

                            imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                            Toast.makeText(getApplicationContext(), R.string.input_apin_failed, Toast.LENGTH_SHORT).show();
                            finish();
                        }else{

                            imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                            Toast.makeText(getApplicationContext(), R.string.input_apin_error, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                    else{
                        Toast.makeText(getApplicationContext(), R.string.input_apin_return, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();

                    imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                    Toast.makeText(getApplicationContext(), R.string.input_apin_net_error, Toast.LENGTH_SHORT).show();;
                    finish();
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

                imm.toggleSoftInput(0, InputMethodManager.RESULT_SHOWN);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

//                finish();
            }
        });

        return builder.create();
    }

    
    protected void RandomSkipSwitcher(Context context) {
        // TODO Auto-generated method stub
        LayoutInflater inflater = LayoutInflater.from(context);
        final View textEntryView = inflater.inflate(R.layout.skip_switcher, null);
        TextView pinText = (TextView) textEntryView.findViewById(R.id.med_text);
        pinText.setText(R.string.skip_set_msg);
        
        boolean skip = shp.getBoolean(Util.SP_LOGIN_RANDOM_SKIP, false);
        Switch skipSwitcher = (Switch) textEntryView.findViewById(R.id.random_switcher);
        skipSwitcher.setChecked(skip);
        skipSwitcher.setOnCheckedChangeListener(new OnCheckedChangeListener(){

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                    boolean isChecked) {
                // TODO Auto-generated method stub
                
                if(isChecked){//randomly skipped
                    randomSkip = true;
                }
                
                else{//always on
                    randomSkip = false;
                }  
            }});
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.med_set_title);
        builder.setView(textEntryView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                
                
                Util.Log_debug("Random Skip Dialog", "randomly skipped "+randomSkip);
                shp.edit().putBoolean(Util.SP_LOGIN_RANDOM_SKIP, randomSkip).commit();
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        builder.create().show();;
    }


    private void InputMedication(final Context context) {
        // TODO Auto-generated method stub
        inputMedicationDialog(context).show();
    }
    
    
    /*it's really bad to copy&paste large chunk of code*/
    private Dialog inputMedicationDialog(final Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        final View textEntryView = inflater.inflate(R.layout.medication_input, null);
        TextView pinText = (TextView) textEntryView.findViewById(R.id.med_text);
        final EditText pinEdit1 = (EditText) textEntryView.findViewById(R.id.med_edit1);
        final EditText pinEdit2 = (EditText) textEntryView.findViewById(R.id.med_edit2);
        final EditText pinEdit3 = (EditText) textEntryView.findViewById(R.id.med_edit3);
        final EditText pinEdit4 = (EditText) textEntryView.findViewById(R.id.med_edit4);
        pinEdit1.setText(shp.getString(Util.SP_LOGIN_PRIMARY_MED, ""));
        pinEdit2.setText(shp.getString(Util.SP_LOGIN_SECONDARY_MED, ""));
        pinEdit3.setText(shp.getString(Util.SP_LOGIN_OTHER_MED1, ""));
        pinEdit4.setText(shp.getString(Util.SP_LOGIN_OTHER_MED2, ""));
        pinText.setText(R.string.med_set_msg);
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        builder.setTitle(R.string.med_set_title);
        builder.setView(textEntryView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {
                
                String primary = pinEdit1.getText().toString();
                String secondary = pinEdit2.getText().toString();
                String other1 = pinEdit3.getText().toString();
                String other2 = pinEdit4.getText().toString();
                
                Util.Log_debug("Med Dialog", "Medication names are "+primary + ", " + secondary+", "+other1+", "+other2);
                
                //Primary
                if(primary != null && !primary.equals("")){
                    shp.edit().putString(Util.SP_LOGIN_PRIMARY_MED, primary).commit();
                }
                else{
                    shp.edit().remove(Util.SP_LOGIN_PRIMARY_MED).commit();
                }
                
                //Secondary
                if(secondary != null && !secondary.equals("")){
                    shp.edit().putString(Util.SP_LOGIN_SECONDARY_MED, secondary).commit();
                }
                else{
                    shp.edit().remove(Util.SP_LOGIN_SECONDARY_MED).commit();
                }
                
                //Other1
                if(other1 != null && !other1.equals("")){
                    shp.edit().putString(Util.SP_LOGIN_OTHER_MED1, other1).commit();
                }
                else{
                    shp.edit().remove(Util.SP_LOGIN_OTHER_MED1).commit();
                }
                
                //Other2
                if(other2 != null && !other2.equals("")){
                    shp.edit().putString(Util.SP_LOGIN_OTHER_MED2, other2).commit();
                }
                else{
                    shp.edit().remove(Util.SP_LOGIN_OTHER_MED2).commit();
                }
            }
        });

        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });

        return builder.create();
    }
    
    
    private void TrainingMenu() {
        // TODO Auto-generated method stub
        
        Intent intent = new Intent(MainActivity.this, SurveyMenu.class);
        intent.putExtra(Util.SV_TRAINING_MODE, true);
        startActivity(intent);
    }
    
    
    private void uploadSurveyData() {
        String upload_file_name = "";

        String ID = shp.getString(Util.SP_LOGIN_KEY_USERID, "");
        Log.d(TAG, "ID: "+ID);

        if(!(ID.equals(""))){
            upload_file_name = "SurveyData."+ID+".txt";
        }
        //This probably won't happen
        else{
            Toast.makeText(getApplicationContext(), "The userID is not set!", Toast.LENGTH_LONG).show();
            finish();
        }


        /*File file = new File(Utilities.PHONE_BASE_PATH + upload_file_name);

        if(!file.exists()){
            Toast.makeText(getApplicationContext(), "There is no such file!!", Toast.LENGTH_LONG).show();
        }
        else{
            for(int i = 0; i < file.length(); i++){
                String data =
            }
        }*/

//      boolean finalResult = false;
        String data = "";

        try {
            // open the file for reading
            InputStream instream = new FileInputStream(Util.PHONE_BASE_PATH + upload_file_name);

            // if file the available for reading
            if (instream != null) {
                // prepare the file for reading
                InputStreamReader inputreader = new InputStreamReader(instream);
                BufferedReader buffreader = new BufferedReader(inputreader);

                String line = "";

                StringBuilder stringBuilder = new StringBuilder();

                while((line = buffreader.readLine()) != null){
                    stringBuilder.append(line);
                    Log.d(TAG, "Line: "+line);
                }

                inputreader.close();
                data = stringBuilder.toString();

                Log.d(TAG, "Data: "+data);

                if(Util.checkDataConnectivity(this)){

                    mContext = this;
                    UploadTransmitData t = new UploadTransmitData();

                    progressBar = new ProgressDialog(this);
                    progressBar.setMessage("Uploading Survey Data...");
                    progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                    progressBar.setIndeterminate(true);
                    progressBar.setCancelable(false);


                    t.execute(data);
                }
            }
        }catch (FileNotFoundException e) {
            Log.e(TAG, "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "Can not read file: " + e.toString());
        }
    }

//================================================================================================================================
//================================================================================================================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);
        Util.Log_lifeCycle(TAG, "OnCreate~~~");
        Util.Log_lifeCycle(TAG, "~~~"+requestCode+" "+resultCode);

         switch (requestCode) {
            case INTENT_REQUEST_MAMAGE:
                if(resultCode == Activity.RESULT_OK){
//                    String uid = shp.getString(Util.SP_LOGIN_KEY_USERID, "");
//                    setUserPwdDialog(this, uid).show();
                }
                else{ //if(resultCode == Activity.RESULT_CANCELED){
//                  stopSService();
                    finish();
                }
                break;

            case INTENT_REQUEST_WAKEUP:
                if(resultCode == Activity.RESULT_OK){
                    //write
                    Calendar morning = Calendar.getInstance();
                    morning.setTimeInMillis(Util.getSP(MainActivity.this, Util.SP_BEDTIME).getLong(Util.SP_BEDTIME_KEY_LONG, 0));

                    Toast.makeText(getApplicationContext(), getString(R.string.bedtime_set)+" "+ DateFormat.getDateTimeInstance().format(morning.getTime()),Toast.LENGTH_LONG).show();
                    Util.Log_debug(TAG, "Morning Survey scheduled at " + DateFormat.getDateTimeInstance().format(morning.getTime()));

                    //keep delivered
                    Util.writeEvent(MainActivity.this, Util.CODE_BEDTIME,
                            Util.dtF.format(morning.getTime()),
                            "", "", "",
                            Util.dtF.format(((Calendar)data.getSerializableExtra(MorningScheduler.INTENT_TS)).getTime()),
                            Util.dtF.format(Calendar.getInstance().getTime()));
                }
                else{ //if(resultCode == Activity.RESULT_CANCELED){

                }
                break;

            case INTENT_REQUEST_SUSPENSION:
                if(resultCode == Activity.RESULT_OK){
                    section_6.setText(R.string.section_62);

                    //write suspension ###
                    Util.Log_debug(TAG, "### write suspension");

                    Calendar c = Calendar.getInstance();

                    Util.getSP(MainActivity.this, Util.SP_SURVEY).edit().putLong(Util.SP_SURVEY_KEY_SUSPENSION_START, c.getTimeInMillis()).commit();

                    Util.writeEvent(MainActivity.this, Util.CODE_SUSPENSION,
                            "", "", "", "",
                            Util.dtF.format(c.getTime()), "");
                }

                break;
         }


    }

    @Override
    protected void onRestart() {
        // TODO Auto-generated method stub
        super.onRestart();
        Util.Log_lifeCycle(TAG, "OnRestart~~~");
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        Util.Log_lifeCycle(TAG, "OnStart~~~");
    }


    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        Util.Log_lifeCycle(TAG, "OnPause~~~");

        if(getIntent().getBooleanExtra(RebootReceiver.REBOOT, false)){
            Intent i = getIntent();
            i.removeExtra(RebootReceiver.REBOOT);
            setIntent(i);
        }
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
        Util.Log_lifeCycle(TAG, "onStop~~~");

    }

    @Override
    protected void onDestroy() {
        Util.Log_lifeCycle(TAG, "onDestroy~~~");
        // implementation here
        unregisterReceiver(suspensionReceiver);

//        recordingOnDestroy();

        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Util.Log_lifeCycle(TAG, "onBackPressed~~~");
    }

    /**
     * change suspension text when breaking suspension
     */
    BroadcastReceiver suspensionReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Util.Log_debug(TAG, "on receiver break suspension");

            section_6.setText(R.string.section_6);

        }
    };



    //=============================================================================================================
    //upload backup

    public static class UploadTransmitData extends AsyncTask<String,Void, Boolean>{

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            progressBar.show();
        }

        @Override
        protected Boolean doInBackground(String... strings) {
            // TODO Auto-generated method stub

            boolean finalResult = true;

            String data = strings[0];

            String[] seperated = data.split(";;;;;;;;;;");

            for(int i = 0; i < seperated.length; i++){

                //           String fileName=strings[0];
                //           String dataToSend=strings[1];
                if(true){

                    Log.d("((((((((((((((((((((((((+", ""+Thread.currentThread().getId());
                    HttpPost request = new HttpPost(Util.RECOVERY_ADDRESS);
                    List<NameValuePair> params = new ArrayList<NameValuePair>();
                    params.add(new BasicNameValuePair("data", seperated[i]));

//                    //file_name
//                    params.add(new BasicNameValuePair("file_name",fileName));
//                    //data
//                    params.add(new BasicNameValuePair("data",dataToSend));
                    try {
                        Log.d("MainActivity", "upload survey data result try");
                        request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                        HttpResponse response = new DefaultHttpClient().execute(request);
                        if(response.getStatusLine().getStatusCode() == 200){
                            String result = EntityUtils.toString(response.getEntity());
                            Log.d("upload survey data result: ",result);

                             //MainActivity.uploadFinalResult = true;
                            // Log.d("Wrist Sensor Data Point Info","Data Point Successfully Uploaded!");
                        }
                    }catch (Exception e){
                        Log.d("MainActivity", "upload survey data result catch");
                        e.printStackTrace();
                        finalResult = false;
                    }
                }
                else{
                    Log.d("upload survey data result","No Network Connection:Data Point was not uploaded");
                    finalResult = false;
                }
            }

            return finalResult;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            // TODO Auto-generated method stub
            super.onPostExecute(result);

            Log.d("Upload result: ", String.valueOf(result));

            if (progressBar.isShowing()){
                progressBar.dismiss();
            }

            if(result){
                new AlertDialog.Builder(mContext)
                .setTitle("Success")
                .setMessage("The Survey Data was sent to the Server Successfully!!")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int id) {
                                // User cancelled the dialog
                            }
                        }).create().show();
            }
            else{
                new AlertDialog.Builder(mContext)
                .setTitle("Warning")
                .setMessage("Failed to get Survey Data to server :( ")
                .setNeutralButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int id) {
                                // User cancelled the dialog
                            }
                        }).create().show();
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            // TODO Auto-generated method stub
            super.onProgressUpdate(values);
        }
    }




    //=============================================================================================================
    //recording

    private void recordingOnResume() {
        // TODO Auto-generated method stub
        /* Nick added this on april 6nd 2015 for slu app
         * this will determine if the user PAUSES the app
         * and will write it to the file and send it to the server
         */

        mContext = this;
        /*String nick = null;
        String whichOne = null;
        if(!start){
            nick = "STARTED";
            whichOne = "onStart";
        }
        else{
            nick = "RESUMED";
            whichOne = "onResume";
        }*/

        String ID = shp.getString(Util.SP_LOGIN_KEY_USERID, "");
        Boolean start = shp.getBoolean(MonitorUtilities.ACTION_ONSTART, true);

        if(  (!(start))  && (!(ID.equals("")))  ){
            String message = "User has just STARTED the app!";
            String whichOne = "onStart";
            //boolean send = writeAndSend(message, whichOne);
            //Log.d(TAG, "onStart send to server: "+send);

            boolean result = false;

            String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + ID + "." + MonitorUtilities.getFileDate();
            String toWrite = MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + message
                    + MonitorUtilities.LINEBREAK + MonitorUtilities.SPLIT;

            try {
                Util.writeToFile(fileName + ".txt", toWrite);
                Util.Log_debug(TAG, whichOne + " writing info to file");
            } catch (IOException e) {
                Util.Log_debug(TAG, whichOne + " not write to file!");
                e.printStackTrace();
            }

            String fileHead = getFileHead(fileName);
            // Log.d("RecordingReceiver", fileHead);

            String toSend = fileHead + toWrite;
            String enformattedData = null;

            try {
                enformattedData = Util.encryption(MainActivity.this, toSend);
            } catch (Exception e) {
                Log.d(TAG, whichOne +" utilities monitorEncryption failed!!");
                e.printStackTrace();
            }

            RecordingTransmitData transmitData = new RecordingTransmitData();
            try {
				result = transmitData.execute(enformattedData, whichOne).get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            Log.d(TAG, "onStart send to server: "+result);

            MonitorUtilities.makeSharedPreferencesMonitorOnStartToTrue(shp);
        }
    }


    private void recordingOnDestroy() {
        // TODO Auto-generated method stub
        /* Nick added this on april 2nd 2015 for slu app
         * this will determine if the user closes the app
         * and will write it to the file and send it to the server
         */

        String ID = shp.getString(Util.SP_LOGIN_KEY_USERID, "");

        if( !(ID.equals("")) ){
            String message = "User has just CLOSED the app!";
            String whichOne = "onDestroy";
            //boolean send = writeAndSend(message, whichOne);
            //Log.d(TAG, "onDestroy send to server: "+send);
            boolean resultOnResume = false;

            String fileName = MonitorUtilities.RECORDING_CATEGORY + "." + ID + "." + MonitorUtilities.getFileDate();
            String toWrite = MonitorUtilities.getCurrentTimeStamp() + MonitorUtilities.LINEBREAK + message
                    + MonitorUtilities.LINEBREAK + MonitorUtilities.SPLIT;

            try {
                Util.writeToFile(fileName + ".txt", toWrite);
                Util.Log_debug(TAG, whichOne + " writing info to file");
            } catch (IOException e) {
                Util.Log_debug(TAG, whichOne + " not write to file!");
                e.printStackTrace();
            }

            String fileHead = getFileHead(fileName);
            // Log.d("RecordingReceiver", fileHead);

            String toSend = fileHead + toWrite;
            String enformattedData = null;

            try {
                enformattedData = Util.encryption(MainActivity.this, toSend);
            } catch (Exception e) {
                Log.d(TAG, whichOne + " utilities monitorEncryption failed!!");
                e.printStackTrace();
            }

            RecordingTransmitData transmitData = new RecordingTransmitData();
            transmitData.execute(enformattedData, whichOne);


            Log.d(TAG, "onDestroy send to server: "+resultOnResume);
        }

    }


    static class RecordingTransmitData extends AsyncTask<String,Void, Boolean>{

        @Override
        protected Boolean doInBackground(String... strings) {
            boolean result = false; 

            String data = strings[0];
            String whichOne = strings[1];
            //           String fileName=strings[0];
            //           String dataToSend=strings[1];

            if (MonitorUtilities.checkNetwork(mContext)) {
                HttpPost request = new HttpPost(Util.UPLOAD_ADDRESS);
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("data", data));
                // // file_name
                // params.add(new BasicNameValuePair("file_name", fileName));
                // // data
                // params.add(new BasicNameValuePair("data", dataToSend));
                try {
                    request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                    HttpResponse response = new DefaultHttpClient().execute(request);
                    Log.d("Sensor Data Point Info", String.valueOf(response.getStatusLine().getStatusCode()));
                    if(response.getStatusLine().getStatusCode() == 200){
                        result = true;
                        Util.Log_debug(TAG, whichOne + " send info to server");
                    }
                } catch (Exception e){
                    Util.Log_debug(TAG, whichOne + " did not send info to server!!");
                    e.printStackTrace();
                }
            }

            return result;
        }
    }


    private String getFileHead(String fileName) {
        StringBuilder prefix_sb = new StringBuilder(Util.PREFIX_LEN);
        prefix_sb.append(fileName);

        for (int i = fileName.length(); i <= Util.PREFIX_LEN; i++) {
            prefix_sb.append(" ");
        }
        return prefix_sb.toString();
    }
}
