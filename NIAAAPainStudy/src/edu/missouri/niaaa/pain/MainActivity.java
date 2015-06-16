package edu.missouri.niaaa.pain;


import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import edu.missouri.niaaa.pain.activity.AdminManageActivity;
import edu.missouri.niaaa.pain.activity.MorningScheduler;
import edu.missouri.niaaa.pain.activity.SupportActivity;
import edu.missouri.niaaa.pain.activity.SuspensionTimePicker;
import edu.missouri.niaaa.pain.location.LocationUtilities;
import edu.missouri.niaaa.pain.survey.SurveyMenu;


public class MainActivity extends Activity {
    String TAG = "MainActivity.java";
    boolean logEnable = true;

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Util.Log_lifeCycle(TAG, "OnCreate~~~");

        /* thread policy
         * help to check if there is misuse of threads, such as read large files or network communication, that
         * should not be in the main UI thread.
         * Should be bypass when product released */
        if(!Util.RELEASE){
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
        //restoreStatus();

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

                restoreStatusForTheFirstTime();

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

        /*check suspension status*/
        //if suspension expired, remove suspension flag

      //restart gps
        if(Utilities.completedMorningToday(this) || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) < 3){
            sendBroadcast(new Intent(LocationUtilities.ACTION_START_LOCATION));
        }

    }

    /**
     * Similar with {@link #restoreStatus()}, but this is called for
     * the first time ID is assigned
     */
    private void restoreStatusForTheFirstTime(){

        //schedule
        //or reschedule if already there
        //Util.scheduleRandomSurvey(MainActivity.this, true, true);
        //scheduleAll();



        Utilities.scheduleDaemon(MainActivity.this);
//      startSService();

        restoreStatus();
    }


    private void setSharedValue(){

        //public key
        try {
            Utilities.publicKey = getPublicKey();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.public_key_lost, Toast.LENGTH_SHORT).show();
            finish();
        }

        //ID

//      locationM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                Utilities.Log("Pin Dialog", "pin String is "+pinStr);

                String data = null;
                try {
                    data = Utilities.encryption(ID + "," + "3" + "," + pinStr);
                } catch (Exception e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }

/*              check network*/

/*              prepare params for server*/
                HttpPost request = new HttpPost(Utilities.VALIDATE_ADDRESS);

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

                if(!Util.isSuspension()){
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
                Utilities.Log(TAG, "section 4 on click listener");

            }
        });

        section_5.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Utilities.Log(TAG, "section 5 on click listener");

                if(!Util.isSuspension()){
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
                Utilities.Log(TAG, "section 6 on click listener");

                if(Utilities.completedMorningToday(MainActivity.this)){
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

                                Util.cancelSuspension(MainActivity.this);

                                //write to server
//                                Calendar c = Calendar.getInstance();
//                                SharedPreferences sp = getSharedPreferences(Util.SP_LOGIN, Context.MODE_PRIVATE);
//                                long startTimeStamp = sp.getLong(Utilities.SP_KEY_SUSPENSION_TS, c.getTimeInMillis());
//                                c.setTimeInMillis(startTimeStamp);
//
//                                try {
//                                    Utilities.writeEventToFile(MainActivity.this, Utilities.CODE_SUSPENSION, "", "", "", "",
//                                            Utilities.sdf.format(c.getTime()), Utilities.sdf.format(Calendar.getInstance().getTime()));
//                                } catch (IOException e) {
//                                    // TODO Auto-generated catch block
//                                    e.printStackTrace();
//                                }
//                                sp.edit().remove(Utilities.SP_KEY_SUSPENSION_TS).commit();

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
                Utilities.Log(TAG, "section 7 on click listener");

                startActivity(new Intent(MainActivity.this, SupportActivity.class));
            }
        });

        section_8.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Utilities.Log(TAG, "section 8 on click listener");


                AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

                Intent itTrigger = new Intent(Util.BD_ACTION_SURVEY_TRIGGER);
                itTrigger.putExtra(Util.SV_TYPE, Util.SV_NAME_MORNING);
                PendingIntent piTrigger = PendingIntent.getBroadcast(MainActivity.this, 1, itTrigger, PendingIntent.FLAG_CANCEL_CURRENT);

                am.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), piTrigger);//do not add any delay in real use
            }
        });
        
        section_9.setOnClickListener(new OnClickListener(){

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                Utilities.Log(TAG, "section 9 on click listener");

//                Util.Log_debug(TAG, ""+Util.isIsolateFlag(MainActivity.this));
//                Util.Log_debug(TAG, ""+Util.isSuspensionFlag(MainActivity.this));
                Util.bedtimeComplete(MainActivity.this, 12, 23);
                
            }
        });

    }


    private void setSuspensionText(){
        section_6.setText(!Util.isSuspensionFlag(MainActivity.this) ? R.string.section_6:R.string.section_62);
    }


//    private boolean getSuspension(){
//        return Utilities.getSP(MainActivity.this, Utilities.SP_SURVEY).getBoolean(Utilities.SP_KEY_SURVEY_SUSPENSION, false);
//    }

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
                Utilities.Log("Pin Dialog", "pin String is "+pinStr);

                if (pinStr.equals(Utilities.getPWD(context))){
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
        getMenuInflater().inflate(R.menu.main, menu);
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
            .setMessage("User ID: "+uid+"\n"+Utilities.getScheduleForToady(MainActivity.this))
            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // TODO Auto-generated method stub

                }
            })
            .create();
            alertDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }


    private void turnOnBluetooth(){
        // TODO Auto-generated method stub
        Intent Enable_Bluetooth=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(Enable_Bluetooth, INTENT_REQUEST_BLUETOOTH);
    }




    private PublicKey getPublicKey() throws Exception {
        // TODO Auto-generated method stub
        InputStream is = getResources().openRawResource(R.raw.publickey);
        ObjectInputStream ois = new ObjectInputStream(is);

        BigInteger m = (BigInteger)ois.readObject();
        BigInteger e = (BigInteger)ois.readObject();
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(m, e);


        KeyFactory fact = KeyFactory.getInstance("RSA", "BC");
        PublicKey pubKey = fact.generatePublic(keySpec);

        return pubKey;
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
                    morning.setTimeInMillis(Utilities.getSP(MainActivity.this, Util.SP_BEDTIME).getLong(Util.SP_BEDTIME_KEY_LONG, 0));

                    Toast.makeText(getApplicationContext(), getString(R.string.bedtime_set)+" "+ DateFormat.getDateTimeInstance().format(morning.getTime()),Toast.LENGTH_LONG).show();
                    Util.Log_debug(TAG, "Morning Survey scheduled at " + DateFormat.getDateTimeInstance().format(morning.getTime()));

                    //keep delivered
                    try {
                        Utilities.writeEventToFile(MainActivity.this, Utilities.CODE_BEDTIME,
                                Utilities.sdf.format(morning.getTime()), "", "", "",
                                Utilities.sdf.format(((Calendar)data.getSerializableExtra(MorningScheduler.INTENT_TS)).getTime()),
                                Utilities.sdf.format(Calendar.getInstance().getTime()));
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
                else{ //if(resultCode == Activity.RESULT_CANCELED){

                }
                break;

            case INTENT_REQUEST_SUSPENSION:
                if(resultCode == Activity.RESULT_OK){
                    section_6.setText(R.string.section_62);
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

        super.onDestroy();
    }



    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
        super.onBackPressed();
        Util.Log_lifeCycle(TAG, "onBackPressed~~~");
    }

    /**
     * write the start and end timestamp to server
     */
    BroadcastReceiver suspensionReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            Utilities.Log(TAG, "on receiver break suspension");

//            section_6.setText(R.string.section_6);
////          Uti.getSP(MainActivity.this, Uti.SP_SURVEY).edit().putBoolean(Uti.SP_KEY_SURVEY_SUSPENSION, false).commit();
//
//            //write to server
//            Calendar c = Calendar.getInstance();
//            SharedPreferences sp = getSharedPreferences(Uti.SP_LOGIN, Context.MODE_PRIVATE);
//            long startTimeStamp = sp.getLong(Uti.SP_KEY_SUSPENSION_TS, c.getTimeInMillis());
//            c.setTimeInMillis(startTimeStamp);
//
//            try {
//                Uti.writeEventToFile(MainActivity.this, Uti.CODE_SUSPENSION, "", "", "", "",
//                        Uti.sdf.format(c.getTime()), Uti.sdf.format(Calendar.getInstance().getTime()));
//            } catch (IOException e) {
//                // TODO Auto-generated catch block
//                e.printStackTrace();
//            }
//            sp.edit().remove(Uti.SP_KEY_SUSPENSION_TS).commit();
//
//            Toast.makeText(getApplicationContext(), R.string.suspension_end, Toast.LENGTH_LONG).show();
        }
    };

}
